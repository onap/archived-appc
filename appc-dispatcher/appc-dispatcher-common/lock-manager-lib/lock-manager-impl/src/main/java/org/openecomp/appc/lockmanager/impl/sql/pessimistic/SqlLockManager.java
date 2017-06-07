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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.openecomp.appc.lockmanager.api.LockException;
import org.openecomp.appc.lockmanager.api.LockRuntimeException;
import org.openecomp.appc.lockmanager.impl.sql.JdbcLockManager;
import org.openecomp.appc.lockmanager.impl.sql.Messages;

abstract class SqlLockManager extends JdbcLockManager {

	private static final String SQL_LOAD_LOCK_RECORD = "SELECT * FROM %s WHERE RESOURCE_ID=?";
	private static final String SQL_INSERT_LOCK_RECORD = "INSERT INTO %s (RESOURCE_ID, OWNER_ID, UPDATED, TIMEOUT) VALUES (?, ?, ?, ?)";
	private static final String SQL_UPDATE_LOCK_RECORD = "UPDATE %s SET OWNER_ID=?, UPDATED=?, TIMEOUT=? WHERE RESOURCE_ID=?";
	private static final String SQL_CURRENT_TIMESTAMP = "SELECT CURRENT_TIMESTAMP()";

	private String sqlLoadLockRecord;
	private String sqlInsertLockRecord;
	private String sqlUpdateLockRecord;

	@Override
	public boolean acquireLock(String resource, String owner) throws LockException {
		return acquireLock(resource, owner, 0);
	}

	@Override
	public boolean acquireLock(String resource, String owner, long timeout) throws LockException {
		if(owner == null) {
			throw new LockRuntimeException(Messages.ERR_NULL_LOCK_OWNER.format(resource));
		}
		boolean res = false;
		Connection connection = openDbConnection();
		try {
			enterCriticalSection(connection, resource);
			try {
				res = lockResource(connection, resource, owner, timeout);
			} finally {
				leaveCriticalSection(connection, resource);
			}
		} finally {
			closeDbConnection(connection);
		}
		return res;
	}

	@Override
	public void releaseLock(String resource, String owner) throws LockException {
		Connection connection = openDbConnection();
		try {
			enterCriticalSection(connection, resource);
			try {
				unlockResource(connection, resource, owner);
			} finally {
				leaveCriticalSection(connection, resource);
			}
		} finally {
			closeDbConnection(connection);
		}
	}

	@Override
	public boolean isLocked(String resource) {
		Connection connection=openDbConnection();
		try {
			LockRecord lockRecord=loadLockRecord(connection,resource);
			if(lockRecord==null){
				return false;
			}else{
				if(lockRecord.getOwner()==null){
					return false;
				}else if(isLockExpired(lockRecord, connection)){
					return false;
				}else{
					return true;
				}
			}
		} catch (SQLException e) {
			throw new LockRuntimeException(Messages.EXP_CHECK_LOCK.format(resource));
		}finally {
			closeDbConnection(connection);
		}
	}

	private boolean lockResource(Connection connection, String resource, String owner, long timeout) throws LockException {
		try {
			boolean res = false;
			LockRecord lockRecord = loadLockRecord(connection, resource);
			if(lockRecord != null) {
				// lock record already exists
				String currentOwner = lockRecord.getOwner();
				if(currentOwner != null) {
					if(isLockExpired(lockRecord, connection)) {
						currentOwner = null;
					} else if(!owner.equals(currentOwner)) {
						throw new LockException(Messages.ERR_LOCK_LOCKED_BY_OTHER.format(resource, owner, currentOwner));
					}
				}
				// set new owner on the resource lock record
				updateLockRecord(connection, resource, owner, timeout);
				if(currentOwner == null) {
					// no one locked the resource before
					res = true;
				}
			} else {
				// resource record does not exist in lock table => create new record
				addLockRecord(connection, resource, owner, timeout);
				res = true;
			}
			return res;
		} catch(SQLException e) {
			throw new LockRuntimeException(Messages.EXP_LOCK.format(resource), e);
		}
	}

	private void unlockResource(Connection connection, String resource, String owner) throws LockException {
		try {
			LockRecord lockRecord = loadLockRecord(connection, resource);
			if(lockRecord != null) {
				// check if expired
				if(isLockExpired(lockRecord, connection)) {
					// lock is expired => no lock
					lockRecord = null;
				}
			}
			if((lockRecord == null) || (lockRecord.getOwner() == null)) {
				// resource is not locked
				throw new LockException(Messages.ERR_UNLOCK_NOT_LOCKED.format(resource));
			}
			String currentOwner = lockRecord.getOwner();
			if(!owner.equals(currentOwner)) {
				throw new LockException(Messages.ERR_UNLOCK_LOCKED_BY_OTHER.format(resource, owner, currentOwner));
			}
			updateLockRecord(connection, resource, null, 0);
			// TODO delete record from table on lock release?
//			deleteLockRecord(connection, resource);
		} catch(SQLException e) {
			throw new LockRuntimeException(Messages.EXP_UNLOCK.format(resource), e);
		}
	}

	protected abstract void enterCriticalSection(Connection connection, String resource);

	protected abstract void leaveCriticalSection(Connection connection, String resource);

	protected LockRecord loadLockRecord(Connection connection, String resource) throws SQLException {
		LockRecord res = null;
		if(sqlLoadLockRecord == null) {
			sqlLoadLockRecord = String.format(SQL_LOAD_LOCK_RECORD, tableName);
		}
		try(PreparedStatement statement = connection.prepareStatement(sqlLoadLockRecord)) {
			statement.setString(1, resource);
			try(ResultSet resultSet = statement.executeQuery()) {
				if(resultSet.next()) {
					res = new LockRecord(resource);
					res.setOwner(resultSet.getString(2));
					res.setUpdated(resultSet.getLong(3));
					res.setTimeout(resultSet.getLong(4));
				}
			}
		}
		return res;
	}

	protected void addLockRecord(Connection connection, String resource, String owner, long timeout) throws SQLException {
		if(sqlInsertLockRecord == null) {
			sqlInsertLockRecord = String.format(SQL_INSERT_LOCK_RECORD, tableName);
		}
		try(PreparedStatement statement = connection.prepareStatement(sqlInsertLockRecord)) {
			statement.setString(1, resource);
			statement.setString(2, owner);
			statement.setLong(3, getCurrentTime(connection));
			statement.setLong(4, timeout);
			statement.executeUpdate();
		}
	}

	protected void updateLockRecord(Connection connection, String resource, String owner, long timeout) throws SQLException {
		if(sqlUpdateLockRecord == null) {
			sqlUpdateLockRecord = String.format(SQL_UPDATE_LOCK_RECORD, tableName);
		}
		try(PreparedStatement statement = connection.prepareStatement(sqlUpdateLockRecord)) {
			statement.setString(1, owner);
			statement.setLong(2, getCurrentTime(connection));
			statement.setLong(3, timeout);
			statement.setString(4, resource);
			statement.executeUpdate();
		}
	}

//	protected void deleteLockRecord(Connection connection, String resource) throws SQLException {
//		if(sqlDeleteLockRecord == null) {
//			sqlDeleteLockRecord = String.format(SQL_DELETE_LOCK_RECORD, tableName);
//		}
//		try(PreparedStatement statement = connection.prepareStatement(sqlDeleteLockRecord)) {
//			statement.setString(1, resource);
//			statement.executeUpdate();
//		}
//	}

	private boolean isLockExpired(LockRecord lockRecord, Connection connection) throws SQLException {
		long timeout = lockRecord.getTimeout();
		if(timeout == 0) {
			return false;
		}
		long updated = lockRecord.getUpdated();
		long now = getCurrentTime(connection);
		long expiration = updated + timeout;
		return (now > expiration);
	}

	private long getCurrentTime(Connection connection) throws SQLException {
		long res = -1;
		if(connection != null) {
			try(PreparedStatement statement = connection.prepareStatement(SQL_CURRENT_TIMESTAMP)) {
				try(ResultSet resultSet = statement.executeQuery()) {
					if(resultSet.next()) {
						res = resultSet.getTimestamp(1).getTime();
					}
				}
			}
		}
		if(res == -1) {
			res = System.currentTimeMillis();
		}
		return res;
	}
}
