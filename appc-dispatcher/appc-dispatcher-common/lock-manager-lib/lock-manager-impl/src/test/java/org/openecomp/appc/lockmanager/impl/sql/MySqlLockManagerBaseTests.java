/*-
 * ============LICENSE_START=======================================================
 * APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Amdocs
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */

package org.openecomp.appc.lockmanager.impl.sql;

import org.junit.Rule;
import org.junit.rules.TestName;
import org.openecomp.appc.dao.util.DefaultJdbcConnectionFactory;
import org.openecomp.appc.lockmanager.api.LockManager;
import org.openecomp.appc.lockmanager.api.LockManagerBaseTests;
import org.openecomp.appc.lockmanager.impl.sql.JdbcLockManager;
import org.openecomp.appc.lockmanager.impl.sql.MySqlConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public abstract class MySqlLockManagerBaseTests extends LockManagerBaseTests {

	private static final boolean USE_REAL_DB = Boolean.getBoolean("lockmanager.tests.useRealDb");
	private static final String TABLE_LOCK_MANAGEMENT = "TEST_LOCK_MANAGEMENT";
	private static final String JDBC_URL = System.getProperty("lockmanager.tests.jdbcUrl", "jdbc:mysql://192.168.1.2/test");
	private static final String JDBC_USERNAME = System.getProperty("lockmanager.tests.jdbcUsername", "test");
	private static final String JDBC_PASSWORD = System.getProperty("lockmanager.tests.jdbcPassword", "123456");

	protected static final int CONCURRENT_TEST_WAIT_TIME = 10; // secs

	@Rule
	public TestName testName = new TestName();

	@Override
	protected LockManager createLockManager() {
		JdbcLockManager jdbcLockManager = createJdbcLockManager(USE_REAL_DB);
		DefaultJdbcConnectionFactory connectionFactory = new MySqlConnectionFactory();
		connectionFactory.setJdbcURL(JDBC_URL);
		connectionFactory.setJdbcUserName(JDBC_USERNAME);
		connectionFactory.setJdbcPassword(JDBC_PASSWORD);
		jdbcLockManager.setConnectionFactory(connectionFactory);
		jdbcLockManager.setTableName(TABLE_LOCK_MANAGEMENT);
		System.out.println("=> Running LockManager test [" + jdbcLockManager.getClass().getName() + "." + testName.getMethodName() + "]" + (USE_REAL_DB ? ". JDBC URL is [" + JDBC_URL + "]" : ""));
		clearTestLocks(jdbcLockManager);
		return jdbcLockManager;
	}

	protected abstract JdbcLockManager createJdbcLockManager(boolean useRealDb);

	protected boolean setSynchronizer(Synchronizer synchronizer) {
		if(!(lockManager instanceof SynchronizerReceiver)) {
			System.err.println("Skipping concurrency test [" + testName.getMethodName() + "] for LockManager of type " + lockManager.getClass());
			return false;
		}
		((SynchronizerReceiver)lockManager).setSynchronizer(synchronizer);
		return true;
	}

	private static final String SQL_DELETE_LOCK_RECORD = String.format("DELETE FROM %s WHERE RESOURCE_ID=?", TABLE_LOCK_MANAGEMENT);
	private void clearTestLocks(JdbcLockManager jdbcLockManager) {
		Connection connection = jdbcLockManager.openDbConnection();
		if(connection == null) {
			return;
		}
		try {
			for(Resource resource: Resource.values()) {
				try(PreparedStatement statement = connection.prepareStatement(SQL_DELETE_LOCK_RECORD)) {
					statement.setString(1, resource.name());
					statement.executeUpdate();
				}
			}
		} catch(SQLException e) {
			throw new RuntimeException("Cannot clear test resources in table", e);
		} finally {
			jdbcLockManager.closeDbConnection(connection);
		}
	}
}
