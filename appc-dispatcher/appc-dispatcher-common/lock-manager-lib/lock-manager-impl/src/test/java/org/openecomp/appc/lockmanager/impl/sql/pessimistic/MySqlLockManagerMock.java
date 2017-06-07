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
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.openecomp.appc.lockmanager.api.LockRuntimeException;
import org.openecomp.appc.lockmanager.impl.sql.Synchronizer;
import org.openecomp.appc.lockmanager.impl.sql.SynchronizerReceiver;
import org.openecomp.appc.lockmanager.impl.sql.pessimistic.LockRecord;
import org.openecomp.appc.lockmanager.impl.sql.pessimistic.MySqlLockManager;

class MySqlLockManagerMock extends MySqlLockManager implements SynchronizerReceiver {

	private final Map<String, LockRecord> locks = new HashMap<>();
	private final Lock lock = new ReentrantLock();
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
			locks.put(resource, lockRecord);
		} finally {
			if(synchronizer != null) {
				synchronizer.postAddLockRecord(resource, owner);
			}
		}
	}

	@Override
	protected void updateLockRecord(Connection connection, String resource, String owner, long timeout) throws SQLException {
		if(synchronizer != null) {
			synchronizer.preUpdateLockRecord(resource, owner);
		}
		try {
			if(useReal) {
				super.updateLockRecord(connection, resource, owner, timeout);
				return;
			}
			LockRecord lockRecord = loadLockRecord(connection, resource);
			lockRecord.setOwner(owner);
			lockRecord.setUpdated(System.currentTimeMillis());
			lockRecord.setTimeout(timeout);
			locks.put(resource, lockRecord);
		} finally {
			if(synchronizer != null) {
				synchronizer.postUpdateLockRecord(resource, owner);
			}
		}
	}

	@Override
	protected void enterCriticalSection(Connection connection, String resource) {
		if(useReal) {
			super.enterCriticalSection(connection, resource);
			return;
		}
		try {
			if(!lock.tryLock(criticalSectionWaitTimeoutSecs, TimeUnit.SECONDS)) {
                throw new LockRuntimeException("Cannot obtain critical section lock for resource [" + resource + "].");
			}
		} catch(InterruptedException e) {
			throw new LockRuntimeException("Cannot obtain critical section lock.", e);
		}
	}

	@Override
	protected void leaveCriticalSection(Connection connection, String resource) {
		if(useReal) {
			super.leaveCriticalSection(connection, resource);
			return;
		}
		lock.unlock();
	}
}
