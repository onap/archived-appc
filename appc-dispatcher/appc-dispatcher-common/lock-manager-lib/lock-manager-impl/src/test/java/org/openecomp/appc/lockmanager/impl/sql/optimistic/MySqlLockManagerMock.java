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

package org.openecomp.appc.lockmanager.impl.sql.optimistic;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.openecomp.appc.lockmanager.impl.sql.Synchronizer;
import org.openecomp.appc.lockmanager.impl.sql.SynchronizerReceiver;
import org.openecomp.appc.lockmanager.impl.sql.optimistic.LockRecord;
import org.openecomp.appc.lockmanager.impl.sql.optimistic.MySqlLockManager;

class MySqlLockManagerMock extends MySqlLockManager implements SynchronizerReceiver {

	private final ConcurrentMap<String, LockRecord> locks = new ConcurrentHashMap<>();
	private boolean useReal;
	private Synchronizer synchronizer;

	MySqlLockManagerMock(boolean useReal) {
		this.useReal = useReal;
	}

	@Override
	public void setSynchronizer(Synchronizer synchronizer) {
		this.synchronizer = synchronizer;
	}

	@Override
	protected Connection openDbConnection() {
		if(useReal) {
			return super.openDbConnection();
		}
		return null;
	}

	@Override
	protected void closeDbConnection(Connection connection) {
		if(useReal) {
			super.closeDbConnection(connection);
		}
	}

	@Override
	protected LockRecord loadLockRecord(Connection connection, String resource) throws SQLException {
		LockRecord res;
		if(useReal) {
			res = super.loadLockRecord(connection, resource);
		} else {
			res = locks.get(resource);
		}
		if(synchronizer != null) {
			synchronizer.postLoadLockRecord(resource, (res == null) ? null : res.getOwner());
		}
		return res;
	}

	@Override
	protected void addLockRecord(Connection connection, String resource, String owner, long timeout) throws SQLException {
		if(synchronizer != null) {
			synchronizer.preAddLockRecord(resource, owner);
		}
		try {
			if(useReal) {
				super.addLockRecord(connection, resource, owner, timeout);
				return;
			}
			LockRecord lockRecord = new LockRecord(resource);
			lockRecord.setOwner(owner);
			lockRecord.setUpdated(System.currentTimeMillis());
			lockRecord.setTimeout(timeout);
			lockRecord.setVer(1);
			LockRecord prevLockRecord = locks.putIfAbsent(resource, lockRecord);
			if(prevLockRecord != null) {
				// simulate unique constraint violation
				throw new SQLException("Duplicate PK exception", "23000", 1062);
			}
		} finally {
			if(synchronizer != null) {
				synchronizer.postAddLockRecord(resource, owner);
			}
		}
	}

	@Override
	protected boolean updateLockRecord(Connection connection, String resource, String owner, long timeout, long ver) throws SQLException {
		if(synchronizer != null) {
			synchronizer.preUpdateLockRecord(resource, owner);
		}
		try {
			if(useReal) {
				return super.updateLockRecord(connection, resource, owner, timeout, ver);
			}
			LockRecord lockRecord = loadLockRecord(connection, resource);
			synchronized(lockRecord) {
				// should be atomic operation
				if(ver != lockRecord.getVer()) {
					return false;
				}
				lockRecord.setOwner(owner);
				lockRecord.setUpdated(System.currentTimeMillis());
				lockRecord.setTimeout(timeout);
				lockRecord.setVer(ver + 1);
			}
			return true;
		} finally {
			if(synchronizer != null) {
				synchronizer.postUpdateLockRecord(resource, owner);
			}
		}
	}
}
