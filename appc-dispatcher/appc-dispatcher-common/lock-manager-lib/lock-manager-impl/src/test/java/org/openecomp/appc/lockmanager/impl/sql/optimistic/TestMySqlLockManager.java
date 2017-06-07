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

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.appc.lockmanager.api.LockException;
import org.openecomp.appc.lockmanager.impl.sql.JdbcLockManager;
import org.openecomp.appc.lockmanager.impl.sql.MySqlLockManagerBaseTests;
import org.openecomp.appc.lockmanager.impl.sql.Synchronizer;

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
					Assert.assertEquals("Cannot lock resource [" + Resource.Resource1.name() + "] for [" + Owner.A.name() + "]: already locked by [" + Owner.B.name() + "]", e.getMessage());
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
}
