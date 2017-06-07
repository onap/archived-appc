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

package org.openecomp.appc.lockmanager.api;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openecomp.appc.lockmanager.api.LockException;
import org.openecomp.appc.lockmanager.api.LockManager;

public abstract class LockManagerBaseTests {

	protected enum Resource {Resource1, Resource2};
	protected enum Owner {A, B};

	protected LockManager lockManager;

	@Before
	public void beforeTest() {
		lockManager = createLockManager();
	}

	protected abstract LockManager createLockManager();

	@Test
	public void testAcquireLock() throws LockException {
		boolean lockRes = lockManager.acquireLock(Resource.Resource1.name(), Owner.A.name());
		try {
			Assert.assertTrue(lockRes);
		} finally {
			lockManager.releaseLock(Resource.Resource1.name(), Owner.A.name());
		}
	}

	@Test
	public void testAcquireLock_AlreadyLockedBySameOwner() throws LockException {
		boolean lockRes1 = lockManager.acquireLock(Resource.Resource1.name(), Owner.A.name());
		try {
			Assert.assertTrue(lockRes1);
			boolean lockRes2 = lockManager.acquireLock(Resource.Resource1.name(), Owner.A.name());
			Assert.assertFalse(lockRes2);
		} finally {
			lockManager.releaseLock(Resource.Resource1.name(), Owner.A.name());
		}
	}

	@Test(expected = LockException.class)
	public void testAcquireLock_AlreadyLockedByOtherOwner() throws LockException {
		String owner2 = "B";
		boolean lockRes1 = lockManager.acquireLock(Resource.Resource1.name(), Owner.A.name());
		try {
			Assert.assertTrue(lockRes1);
			boolean lockRes2 = lockManager.acquireLock(Resource.Resource1.name(), owner2);
			Assert.assertFalse(lockRes2);
		} finally {
			lockManager.releaseLock(Resource.Resource1.name(), Owner.A.name());
		}
	}

	@Test
	public void testAcquireLock_LockDifferentResources() throws LockException {
		boolean lockRes1 = lockManager.acquireLock(Resource.Resource1.name(), Owner.A.name());
		try {
			Assert.assertTrue(lockRes1);
			boolean lockRes2 = lockManager.acquireLock(Resource.Resource2.name(), Owner.B.name());
			try {
				Assert.assertTrue(lockRes2);
			} finally {
				lockManager.releaseLock(Resource.Resource2.name(), Owner.B.name());
			}
		} finally {
			lockManager.releaseLock(Resource.Resource1.name(), Owner.A.name());
		}
	}

	@Test(expected = LockException.class)
	public void testReleaseLock_NotLockedResource() throws LockException {
		lockManager.releaseLock(Resource.Resource1.name(), Owner.A.name());
	}

	@Test(expected = LockException.class)
	public void testReleaseLock_LockedByOtherOwnerResource() throws LockException {
		boolean lockRes1 = lockManager.acquireLock(Resource.Resource1.name(), Owner.A.name());
		try {
			Assert.assertTrue(lockRes1);
			lockManager.releaseLock(Resource.Resource1.name(), Owner.B.name());
		} finally {
			lockManager.releaseLock(Resource.Resource1.name(), Owner.A.name());
		}
	}

	@Test(expected = LockException.class)
	public void testAcquireLock_LockExpired() throws LockException, InterruptedException {
		boolean lockRes1 = lockManager.acquireLock(Resource.Resource1.name(), Owner.A.name(), 50);
		Assert.assertTrue(lockRes1);
		Thread.sleep(1000);
		lockManager.releaseLock(Resource.Resource1.name(), Owner.A.name());
	}

	@Test
	public void testAcquireLock_OtherLockExpired() throws LockException, InterruptedException {
		boolean lockRes1 = lockManager.acquireLock(Resource.Resource1.name(), Owner.A.name(), 50);
		Assert.assertTrue(lockRes1);
		Thread.sleep(1000);
		boolean lockRes2 = lockManager.acquireLock(Resource.Resource1.name(), Owner.B.name());
		try {
			Assert.assertTrue(lockRes2);
		}finally {
			lockManager.releaseLock(Resource.Resource1.name(), Owner.B.name());
		}
	}

	@Test
	public void testIsLocked_WhenLocked() throws LockException, InterruptedException {
		boolean lockRes1 = lockManager.acquireLock(Resource.Resource1.name(), Owner.A.name(), 50);
		try {
		Assert.assertTrue(lockManager.isLocked(Resource.Resource1.name()));
		}finally {
			lockManager.releaseLock(Resource.Resource1.name(), Owner.A.name());
		}
	}


    @Test(expected = LockException.class)
	public void testIsLocked_LockExpired() throws LockException, InterruptedException {
		boolean lockRes1 = lockManager.acquireLock(Resource.Resource1.name(), Owner.A.name(), 50);
		Assert.assertTrue(lockRes1);
		Assert.assertTrue(lockManager.isLocked(Resource.Resource1.name()));
		Thread.sleep(1000);
		try {
			Assert.assertFalse(lockManager.isLocked(Resource.Resource1.name()));
		}finally {
			lockManager.releaseLock(Resource.Resource1.name(), Owner.A.name());
		}
	}

	@Test
	public void testIsLocked_LockReleased() throws LockException, InterruptedException {
		boolean lockRes1 = lockManager.acquireLock(Resource.Resource1.name(), Owner.A.name(), 50);
		lockManager.releaseLock(Resource.Resource1.name(), Owner.A.name());
		Assert.assertFalse(lockManager.isLocked(Resource.Resource1.name()));
	}

	@Test
	public void testIsLocked_NoLock() throws LockException, InterruptedException {
		Assert.assertFalse(lockManager.isLocked(Resource.Resource1.name()));
	}
}
