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

package org.onap.appc.lockmanager.impl.sql.optimistic;

import org.onap.appc.dao.util.api.JdbcConnectionFactory;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.onap.appc.lockmanager.api.LockException;
import org.onap.appc.lockmanager.api.LockRuntimeException;
import org.onap.appc.lockmanager.impl.sql.JdbcLockManager;
import org.onap.appc.lockmanager.impl.sql.MySqlLockManagerBaseTests;
import org.onap.appc.lockmanager.impl.sql.Synchronizer;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.sql.Timestamp;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.*;

public class TestMySqlLockManager extends MySqlLockManagerBaseTests {

    @Override
    protected JdbcLockManager createJdbcLockManager(boolean useReal) {
        return new MySqlLockManagerMock(useReal);
    }

    @Test
    public void testConcurrentLockDifferentOwners() throws LockException, InterruptedException, ExecutionException, TimeoutException {

        final int participantsNo = 2;
        Synchronizer synchronizer = new Synchronizer(participantsNo) {

            private boolean wait = true;

            @Override
            public void preAddLockRecord(String resource, String owner) {
                if(Owner.A.name().equals(owner)) {
                    synchronized(this) {
                        if(wait) {
                            waitOn(this);
                        }
                    }
                }
            }

            @Override
            public void postAddLockRecord(String resource, String owner) {
                if(!Owner.A.name().equals(owner)) {
                    synchronized(this) {
                        notifyAll();
                        wait = false;
                    }
                }
            }

            @Override
            public void preUpdateLockRecord(String resource, String owner) {
                preAddLockRecord(resource, owner);
            }

            @Override
            public void postUpdateLockRecord(String resource, String owner) {
                postAddLockRecord(resource, owner);
            }
        };
        if(!setSynchronizer(synchronizer)) {
            return;
        }
        ExecutorService executor = Executors.newFixedThreadPool(participantsNo);
        // acquireLock by owner A should fail as it will wait for acquireLock by owner B
        Future<Boolean> future1 = executor.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    lockManager.acquireLock(Resource.Resource1.name(), Owner.A.name());
                    return false;
                } catch(LockException e) {
                    // this call should fail as Synchronizer delays its lock to make sure the second call locks the resource first
                    Assert.assertEquals("VNF : [" + Resource.Resource1.name() + "] is locked by request id : [" + Owner.B.name() + "]", e.getMessage());
                    return true;
                }
            }
        });
        try {
            // acquireLock by owner B should success
            Future<Boolean> future2 = executor.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    // this call should success as Synchronizer delays the above lock to make sure this call success to lock the resource
                    return lockManager.acquireLock(Resource.Resource1.name(), Owner.B.name());
                }
            });
            try {
                Assert.assertTrue(future2.get(CONCURRENT_TEST_WAIT_TIME, TimeUnit.SECONDS));
                Assert.assertTrue(future1.get(CONCURRENT_TEST_WAIT_TIME, TimeUnit.SECONDS));
            } finally {
                future2.cancel(true);
            }
        } finally {
            future1.cancel(true);
        }
    }

    @Test
    public void testConcurrentLockSameOwner() throws LockException, InterruptedException, ExecutionException, TimeoutException {
        final int participantsNo = 2;
        Synchronizer synchronizer = new Synchronizer(participantsNo) {

            private boolean wait = true;

            @Override
            public void preAddLockRecord(String resource, String owner) {
                synchronized(this) {
                    if(wait) {
                        wait = false;
                        waitOn(this);
                    }
                }
            }

            @Override
            public void postAddLockRecord(String resource, String owner) {
                synchronized(this) {
                    notifyAll();
                }
            }
        };
        if(!setSynchronizer(synchronizer)) {
            return;
        }
        ExecutorService executor = Executors.newFixedThreadPool(participantsNo);
        // one acquireLock should return true and the other should return false
        Callable<Boolean> callable = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return lockManager.acquireLock(Resource.Resource1.name(), Owner.A.name());
            }
        };
        Future<Boolean> future1 = executor.submit(callable);
        try {
            Future<Boolean> future2 = executor.submit(callable);
            try {
                boolean future1Res = future1.get(CONCURRENT_TEST_WAIT_TIME, TimeUnit.SECONDS);
                boolean future2Res = future2.get(CONCURRENT_TEST_WAIT_TIME, TimeUnit.SECONDS);
                // one of the lock requests should return true, the other one false as lock is requested simultaneously from 2 threads by same owner
                Assert.assertNotEquals(future1Res, future2Res);
            } finally {
                future2.cancel(true);
            }
        } finally {
            future1.cancel(true);
        }
    }

    @Test
    public void testConcurrentUnlockSameOwner() throws LockException, InterruptedException, ExecutionException, TimeoutException {
        lockManager.acquireLock(Resource.Resource1.name(), Owner.A.name());
        final int participantsNo = 2;
        Synchronizer synchronizer = new Synchronizer(participantsNo) {

            private boolean wait = true;

            @Override
            public void preUpdateLockRecord(String resource, String owner) {
                synchronized(this) {
                    // make sure second call updates the LockRecord first
                    if(wait) {
                        wait = false;
                        waitOn(this);
                    }
                }
            }

            @Override
            public void postUpdateLockRecord(String resource, String owner) {
                synchronized(this) {
                    notifyAll();
                }
            }
        };
        if(!setSynchronizer(synchronizer)) {
            return;
        }
        ExecutorService executor = Executors.newFixedThreadPool(participantsNo);
        Callable<Boolean> callable = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    lockManager.releaseLock(Resource.Resource1.name(), Owner.A.name());
                    // one of the unlock calls should success
                    return true;
                } catch(LockException e) {
                    // one of the unlock calls should throw the LockException as the resource should already be unlocked by other call
                    Assert.assertEquals("Error unlocking resource [" + Resource.Resource1.name() + "]: resource is not locked", e.getMessage());
                    return false;
                }
            }
        };
        Future<Boolean> future1 = executor.submit(callable);
        try {
            Future<Boolean> future2 = executor.submit(callable);
            try {
                boolean future1Res = future1.get(CONCURRENT_TEST_WAIT_TIME, TimeUnit.SECONDS);
                boolean future2Res = future2.get(CONCURRENT_TEST_WAIT_TIME, TimeUnit.SECONDS);
                // one of the unlock calls should return true, the other one false as unlock is requested simultaneously from 2 threads by same owner
                Assert.assertNotEquals(future1Res, future2Res);
            } finally {
                future2.cancel(true);
            }
        } finally {
            future1.cancel(true);
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
    public void testIsDuplicatePkError() throws SQLException {
        SqlLockManager lockManager = new SqlLockManager() {};
        SQLException sqlException = Mockito.mock(SQLException.class);
        Mockito.when(sqlException.getSQLState()).thenReturn("23xxx");
        assertTrue(lockManager.isDuplicatePkError(sqlException));
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
    public void testLoadLockRecord3arg() throws SQLException {
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
        Mockito.when(connection.prepareStatement(SqlLockManager.SQL_LOAD_LOCK_RECORD_WITH_OWNER)).thenReturn(statement);
        Mockito.when(connectionFactory.openDbConnection()).thenReturn(connection);
        lockManager.setConnectionFactory(connectionFactory);
        assertTrue(lockManager.loadLockRecord(connection, "", "") instanceof LockRecord);
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
        Mockito.when(connection.prepareStatement(String.format(SqlLockManager.SQL_UPDATE_LOCK_RECORD, "TABLE_NAME"))).thenReturn(statement);
        Mockito.when(connectionFactory.openDbConnection()).thenReturn(connection);
        lockManager.setConnectionFactory(connectionFactory);
        assertFalse(lockManager.updateLockRecord(connection, "", "", 0L, 0L));
    }
}
