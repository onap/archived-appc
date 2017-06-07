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

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.appc.lockmanager.api.LockException;
import org.openecomp.appc.lockmanager.api.LockRuntimeException;
import org.openecomp.appc.lockmanager.impl.sql.JdbcLockManager;
import org.openecomp.appc.lockmanager.impl.sql.MySqlLockManagerBaseTests;
import org.openecomp.appc.lockmanager.impl.sql.Synchronizer;
import org.openecomp.appc.lockmanager.impl.sql.pessimistic.MySqlLockManager;

import java.util.concurrent.*;

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
}
