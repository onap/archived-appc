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

package org.openecomp.appc.lockmanager.impl.sql.pessimistic;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.openecomp.appc.lockmanager.api.LockRuntimeException;

public class MySqlLockManager extends SqlLockManager {

	private static final int DEF_CRITICAL_SECTION_WAIT_TIMEOUT = 3;

	protected int criticalSectionWaitTimeoutSecs = DEF_CRITICAL_SECTION_WAIT_TIMEOUT;

	public void setCriticalSectionWaitTimeoutSecs(int criticalSectionWaitTimeoutSecs) {
		this.criticalSectionWaitTimeoutSecs = criticalSectionWaitTimeoutSecs;
	}

	@Override
	protected void enterCriticalSection(Connection connection, String resource) {
		try {
			CallableStatement statement = connection.prepareCall("SELECT COALESCE(GET_LOCK(?,?),0)");
			try {
				statement.setString(1, resource);
				statement.setInt(2, criticalSectionWaitTimeoutSecs);
				boolean execRes = statement.execute();
				int result = 0;
				if(execRes) {
					ResultSet resultSet = statement.getResultSet();
					try {
						if(resultSet.next()) {
							result = resultSet.getInt(1);
						}
					} finally {
						resultSet.close();
					}
				}
				if(result != 1) { // lock is not obtained
					throw new LockRuntimeException("Cannot obtain critical section lock for resource [" + resource + "].");
				}
			} finally {
				statement.close();
			}
		} catch(SQLException e) {
            throw new LockRuntimeException("Cannot obtain critical section lock for resource [" + resource + "].", e);
		}
	}

	@Override
	protected void leaveCriticalSection(Connection connection, String resource) {
		try {
			CallableStatement statement = connection.prepareCall("SELECT RELEASE_LOCK(?)");
			try {
				statement.setString(1, resource);
				statement.execute();
			} finally {
				statement.close();
			}
		} catch(SQLException e) {
			throw new LockRuntimeException("Error releasing critical section lock.", e);
		}
	}
}
