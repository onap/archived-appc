/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications Copyright (C) 2019 Ericsson
 * =============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.lockmanager.impl.sql.pessimistic;

import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.onap.appc.dao.util.api.JdbcConnectionFactory;
import org.onap.appc.lockmanager.api.LockException;
import org.onap.appc.lockmanager.api.LockRuntimeException;
import org.onap.appc.lockmanager.impl.sql.JdbcLockManager;
import org.onap.appc.lockmanager.impl.sql.MySqlLockManagerBaseTests;
import org.onap.appc.lockmanager.impl.sql.Synchronizer;


public class TestMySqlLockManager extends MySqlLockManagerBaseTests {

    private static int CRITICAL_SECTION_WAIT_TIMEOUT = 1; // in secs

    @Override
    protected JdbcLockManager createJdbcLockManager(boolean useReal) {
        return new MySqlLockManagerMock(useReal);
    }

    @Test
    public void testConcurrentLock() throws LockException, InterruptedException, ExecutionException, TimeoutException {
        try {
            callConcurrentTest(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    try {
                        Assert.assertTrue(lockManager.acquireLock(Resource.Resource1.name(), Owner.A.name()));
                        return true;
                    } catch(LockRuntimeException e) {
                        Assert.assertEquals("Cannot obtain critical section lock for resource [" + Resource.Resource1.name() + "].", e.getMessage());
                        return false;
                    }
                }
            });
        } finally {
            lockManager.releaseLock(Resource.Resource1.name(), Owner.A.name());
        }
    }

    @Test
    public void testConcurrentUnlock() throws LockException, InterruptedException, ExecutionException, TimeoutException {
        lockManager.acquireLock(Resource.Resource1.name(), Owner.A.name());
        callConcurrentTest(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    lockManager.releaseLock(Resource.Resource1.name(), Owner.A.name());
                    return true;
                } catch(LockRuntimeException e) {
                    Assert.assertEquals("Cannot obtain critical section lock for resource [" + Resource.Resource1.name() + "].", e.getMessage());
                    return false;
                }
            }
        });
    }

    private void callConcurrentTest(Callable<Boolean> callable) throws LockException, InterruptedException, ExecutionException, TimeoutException {
        final int participantsNo = 2;
        Synchronizer synchronizer = new Synchronizer(participantsNo) {

            @Override
            protected void waitForAllParticipants(Object waitObj, int totalParticipantsNo, int currentParticipantsNo) {
                waitOn(this, TimeUnit.MILLISECONDS.convert(1 + CRITICAL_SECTION_WAIT_TIMEOUT, TimeUnit.SECONDS)); // add 1 sec to make sure timeout occured
            }
        };
        if(!setSynchronizer(synchronizer)) {
            return;
        }
        ((MySqlLockManager)lockManager).setCriticalSectionWaitTimeoutSecs(CRITICAL_SECTION_WAIT_TIMEOUT);
        ExecutorService executor = Executors.newFixedThreadPool(participantsNo);
        Future<Boolean> future1 = executor.submit(callable);
        try {
            for(int i = 0; i < 10; i++) {
                Thread.sleep(100);
                if(synchronizer.getParticipantCount() > 0) {
                    break;
                }
            }
            // make sure 1st thread gets inside critical section
            if(synchronizer.getParticipantCount() < 1) {
                Assert.fail(getClass().getName() + " first thread failed to acquireLock()");
            }
            Future<Boolean> future2 = executor.submit(callable);
            try {
                // 1st thread should acquire the lock
                Assert.assertTrue(future1.get(3 + CRITICAL_SECTION_WAIT_TIMEOUT, TimeUnit.SECONDS));
                // 2nd thread should fail waiting for critical section
                Assert.assertFalse(future2.get(2 + CRITICAL_SECTION_WAIT_TIMEOUT, TimeUnit.SECONDS));
            } finally {
                future2.cancel(true);
            }
        } finally {
            future1.cancel(true);
            setSynchronizer(null);
        }
    }

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void testAcquireLockNullOwner() throws LockException {
        MySqlLockManager lockManager = new MySqlLockManager();
        expectedEx.expect(LockRuntimeException.class);
        lockManager.acquireLock(null, null, 0);
    }

    @Test
    public void testIsLocked() throws SQLException {
        MySqlLockManager lockManager = Mockito.spy(new MySqlLockManager());
        Mockito.doThrow(new SQLException()).when(lockManager).loadLockRecord(Mockito.any(Connection.class), Mockito.anyString());
        JdbcConnectionFactory connectionFactory = Mockito.mock(JdbcConnectionFactory.class);
        Mockito.when(connectionFactory.openDbConnection()).thenReturn(Mockito.mock(Connection.class));
        lockManager.setConnectionFactory(connectionFactory);
        expectedEx.expect(LockRuntimeException.class);
        lockManager.isLocked(" ");
    }

    @Test
    public void testGetLockOwnerExceptionFlow() throws SQLException {
        MySqlLockManager lockManager = Mockito.spy(new MySqlLockManager());
        Mockito.doThrow(new SQLException()).when(lockManager).loadLockRecord(Mockito.any(Connection.class), Mockito.anyString());
        JdbcConnectionFactory connectionFactory = Mockito.mock(JdbcConnectionFactory.class);
        Mockito.when(connectionFactory.openDbConnection()).thenReturn(Mockito.mock(Connection.class));
        lockManager.setConnectionFactory(connectionFactory);
        expectedEx.expect(LockRuntimeException.class);
        lockManager.getLockOwner(" ");
    }

    @Test
    public void testGetLockOwnerNull() throws SQLException {
        MySqlLockManager lockManager = Mockito.spy(new MySqlLockManager());
        Mockito.doReturn(null).when(lockManager).loadLockRecord(Mockito.any(Connection.class), Mockito.anyString());
        JdbcConnectionFactory connectionFactory = Mockito.mock(JdbcConnectionFactory.class);
        Mockito.when(connectionFactory.openDbConnection()).thenReturn(Mockito.mock(Connection.class));
        lockManager.setConnectionFactory(connectionFactory);
        assertNull(lockManager.getLockOwner(" "));
    }

    @Test
    public void testGetLockOwnerExpired() throws SQLException {
        MySqlLockManager lockManager = Mockito.spy(new MySqlLockManager());
        LockRecord lockRecord = Mockito.mock(LockRecord.class);
        Mockito.when(lockRecord.getTimeout()).thenReturn(1L);
        Mockito.when(lockRecord.getUpdated()).thenReturn(System.currentTimeMillis()-100);
        Mockito.when(lockRecord.getOwner()).thenReturn("OWNER");
        Mockito.doReturn(lockRecord).when(lockManager).loadLockRecord(Mockito.any(Connection.class), Mockito.anyString());
        JdbcConnectionFactory connectionFactory = Mockito.mock(JdbcConnectionFactory.class);
        Connection connection = Mockito.mock(Connection.class);
        PreparedStatement statement = Mockito.mock(PreparedStatement.class);
        ResultSet resultSet = Mockito.mock(ResultSet.class);
        Mockito.when(resultSet.next()).thenReturn(true);
        Mockito.when(resultSet.getTimestamp(1)).thenReturn(new Timestamp(System.currentTimeMillis()));
        Mockito.when(statement.executeQuery()).thenReturn(resultSet);
        Mockito.when(connection.prepareStatement(SqlLockManager.SQL_CURRENT_TIMESTAMP)).thenReturn(statement);
        Mockito.when(connectionFactory.openDbConnection()).thenReturn(connection);
        lockManager.setConnectionFactory(connectionFactory);
        assertNull(lockManager.getLockOwner(" "));
    }

    @Test
    public void testGetLockOwnerNotExpired() throws SQLException {
        MySqlLockManager lockManager = Mockito.spy(new MySqlLockManager());
        LockRecord lockRecord = Mockito.mock(LockRecord.class);
        Mockito.when(lockRecord.getTimeout()).thenReturn(1L);
        Mockito.when(lockRecord.getUpdated()).thenReturn(System.currentTimeMillis()+10000);
        Mockito.when(lockRecord.getOwner()).thenReturn("OWNER");
        Mockito.doReturn(lockRecord).when(lockManager).loadLockRecord(Mockito.any(Connection.class), Mockito.anyString());
        JdbcConnectionFactory connectionFactory = Mockito.mock(JdbcConnectionFactory.class);
        Connection connection = Mockito.mock(Connection.class);
        PreparedStatement statement = Mockito.mock(PreparedStatement.class);
        ResultSet resultSet = Mockito.mock(ResultSet.class);
        Mockito.when(resultSet.next()).thenReturn(true);
        Mockito.when(resultSet.getTimestamp(1)).thenReturn(new Timestamp(System.currentTimeMillis()));
        Mockito.when(statement.executeQuery()).thenReturn(resultSet);
        Mockito.when(connection.prepareStatement(SqlLockManager.SQL_CURRENT_TIMESTAMP)).thenReturn(statement);
        Mockito.when(connectionFactory.openDbConnection()).thenReturn(connection);
        lockManager.setConnectionFactory(connectionFactory);
        assertEquals("OWNER", lockManager.getLockOwner(" "));
    }

    @Test
    public void testLoadLockRecord() throws SQLException {
        MySqlLockManager lockManager = Mockito.spy(new MySqlLockManager());
        lockManager.setTableName("TABLE_NAME");
        JdbcConnectionFactory connectionFactory = Mockito.mock(JdbcConnectionFactory.class);
        Connection connection = Mockito.mock(Connection.class);
        PreparedStatement statement = Mockito.mock(PreparedStatement.class);
        ResultSet resultSet = Mockito.mock(ResultSet.class);
        Mockito.when(resultSet.next()).thenReturn(true);
        Mockito.when(resultSet.getString(2)).thenReturn("OWNER");
        Mockito.when(resultSet.getLong(3)).thenReturn(0L);
        Mockito.when(resultSet.getLong(4)).thenReturn(0L);
        Mockito.when(resultSet.getLong(5)).thenReturn(0L);
        Mockito.when(statement.executeQuery()).thenReturn(resultSet);
        Mockito.when(connection.prepareStatement(String.format(SqlLockManager.SQL_LOAD_LOCK_RECORD, "TABLE_NAME"))).thenReturn(statement);
        Mockito.when(connectionFactory.openDbConnection()).thenReturn(connection);
        lockManager.setConnectionFactory(connectionFactory);
        assertTrue(lockManager.loadLockRecord(connection, "") instanceof LockRecord);
    }

    @Test
    public void testAddLockRecord() throws SQLException {
        MySqlLockManager lockManager = Mockito.spy(new MySqlLockManager());
        lockManager.setTableName("TABLE_NAME");
        JdbcConnectionFactory connectionFactory = Mockito.mock(JdbcConnectionFactory.class);
        Connection connection = Mockito.mock(Connection.class);
        PreparedStatement statement2 = Mockito.mock(PreparedStatement.class);
        ResultSet resultSet2 = Mockito.mock(ResultSet.class);
        Mockito.when(resultSet2.next()).thenReturn(true);
        Mockito.when(resultSet2.getTimestamp(1)).thenReturn(new Timestamp(System.currentTimeMillis()));
        Mockito.when(statement2.executeQuery()).thenReturn(resultSet2);
        Mockito.when(connection.prepareStatement(SqlLockManager.SQL_CURRENT_TIMESTAMP)).thenReturn(statement2);

        PreparedStatement statement = Mockito.mock(PreparedStatement.class);
        ResultSet resultSet = Mockito.mock(ResultSet.class);
        Mockito.when(resultSet.next()).thenReturn(true);
        Mockito.when(resultSet.getString(2)).thenReturn("OWNER");
        Mockito.when(resultSet.getLong(3)).thenReturn(0L);
        Mockito.when(resultSet.getLong(4)).thenReturn(0L);
        Mockito.when(resultSet.getLong(5)).thenReturn(0L);
        Mockito.when(statement.executeQuery()).thenReturn(resultSet);
        Mockito.when(connection.prepareStatement(String.format(SqlLockManager.SQL_INSERT_LOCK_RECORD, "TABLE_NAME"))).thenReturn(statement);
        Mockito.when(connectionFactory.openDbConnection()).thenReturn(connection);
        lockManager.setConnectionFactory(connectionFactory);
        lockManager.addLockRecord(connection, "", "", 0L);
        Mockito.verify(statement).executeUpdate();
    }

    @Test
    public void testUpdateLockRecord() throws SQLException {
        MySqlLockManager lockManager = Mockito.spy(new MySqlLockManager());
        lockManager.setTableName("TABLE_NAME");
        JdbcConnectionFactory connectionFactory = Mockito.mock(JdbcConnectionFactory.class);
        Connection connection = Mockito.mock(Connection.class);
        PreparedStatement statement2 = Mockito.mock(PreparedStatement.class);
        ResultSet resultSet2 = Mockito.mock(ResultSet.class);
        Mockito.when(resultSet2.next()).thenReturn(true);
        Mockito.when(resultSet2.getTimestamp(1)).thenReturn(new Timestamp(-1));
        Mockito.when(statement2.executeQuery()).thenReturn(resultSet2);
        Mockito.when(connection.prepareStatement(SqlLockManager.SQL_CURRENT_TIMESTAMP)).thenReturn(statement2);

        PreparedStatement statement = Mockito.mock(PreparedStatement.class);
        ResultSet resultSet = Mockito.mock(ResultSet.class);
        Mockito.when(resultSet.next()).thenReturn(true);
        Mockito.when(resultSet.getString(2)).thenReturn("OWNER");
        Mockito.when(resultSet.getLong(3)).thenReturn(0L);
        Mockito.when(resultSet.getLong(4)).thenReturn(0L);
        Mockito.when(resultSet.getLong(5)).thenReturn(0L);
        Mockito.when(statement.executeQuery()).thenReturn(resultSet);
        Mockito.when(connection.prepareStatement(String.format(SqlLockManager.SQL_UPDATE_LOCK_RECORD, "TABLE_NAME"))).thenReturn(statement);
        Mockito.when(connectionFactory.openDbConnection()).thenReturn(connection);
        lockManager.setConnectionFactory(connectionFactory);
        lockManager.updateLockRecord(connection, "", "", 0L);
        Mockito.verify(statement).executeUpdate();
    }

    @Test
    public void testEnterCriticalSectionLockRuntimeException() throws SQLException {
        MySqlLockManager lockManager = Mockito.spy(new MySqlLockManager());
        Connection connection = Mockito.mock(Connection.class);
        CallableStatement callableStatement = Mockito.mock(CallableStatement.class);
        Mockito.when(connection.prepareCall("SELECT COALESCE(GET_LOCK(?,?),0)")).thenReturn(callableStatement);
        expectedEx.expect(LockRuntimeException.class);
        expectedEx.expectMessage("Cannot obtain critical section lock for resource [null].");
        lockManager.enterCriticalSection(connection, null);
    }

    @Test
    public void testEnterCriticalSectionLockRuntimeException2() throws SQLException {
        MySqlLockManager lockManager = Mockito.spy(new MySqlLockManager());
        Connection connection = Mockito.mock(Connection.class);
        Mockito.when(connection.prepareCall("SELECT COALESCE(GET_LOCK(?,?),0)")).thenThrow(new SQLException());
        expectedEx.expect(LockRuntimeException.class);
        expectedEx.expectMessage("Cannot obtain critical section lock for resource [null].");
        expectedEx.expectCause(isA(SQLException.class));
        lockManager.enterCriticalSection(connection, null);
    }

    @Test
    public void testEnterCriticalSection() throws SQLException {
        MySqlLockManager lockManager = Mockito.spy(new MySqlLockManager());
        Connection connection = Mockito.mock(Connection.class);
        CallableStatement callableStatement = Mockito.mock(CallableStatement.class);
        Mockito.when(callableStatement.execute()).thenReturn(true);
        ResultSet resultSet = Mockito.mock(ResultSet.class);
        Mockito.when(resultSet.getInt(1)).thenReturn(1);
        Mockito.when(resultSet.next()).thenReturn(true);
        Mockito.when(callableStatement.getResultSet()).thenReturn(resultSet);
        Mockito.when(connection.prepareCall("SELECT COALESCE(GET_LOCK(?,?),0)")).thenReturn(callableStatement);
        lockManager.enterCriticalSection(connection, null);
        Mockito.verify(callableStatement).close();
    }

    @Test
    public void testLeaveCriticalSection() throws SQLException {
        MySqlLockManager lockManager = Mockito.spy(new MySqlLockManager());
        Connection connection = Mockito.mock(Connection.class);
        CallableStatement callableStatement = Mockito.mock(CallableStatement.class);
        Mockito.when(connection.prepareCall("SELECT RELEASE_LOCK(?)")).thenReturn(callableStatement);
        lockManager.leaveCriticalSection(connection, null);
        Mockito.verify(callableStatement).close();
    }

    @Test
    public void testLeaveCriticalSectionExceptionFlow() throws SQLException {
        MySqlLockManager lockManager = Mockito.spy(new MySqlLockManager());
        Connection connection = Mockito.mock(Connection.class);
        Mockito.when(connection.prepareCall("SELECT RELEASE_LOCK(?)")).thenThrow(new SQLException());
        expectedEx.expect(LockRuntimeException.class);
        lockManager.leaveCriticalSection(connection, null);
        expectedEx.expectMessage("Error releasing critical section lock.");
        expectedEx.expectCause(isA(SQLException.class));
    }
}
