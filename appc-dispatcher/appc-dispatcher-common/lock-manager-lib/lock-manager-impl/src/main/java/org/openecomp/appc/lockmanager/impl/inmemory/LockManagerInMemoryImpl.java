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

package org.openecomp.appc.lockmanager.impl.inmemory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.openecomp.appc.lockmanager.api.LockException;
import org.openecomp.appc.lockmanager.api.LockManager;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;


public class LockManagerInMemoryImpl implements LockManager {

    private static LockManagerInMemoryImpl instance = null;
    private Map<String, LockValue> lockedVNFs;

    private static final EELFLogger debugLogger = EELFManager.getInstance().getDebugLogger();

    private LockManagerInMemoryImpl() {
        lockedVNFs = new ConcurrentHashMap<>();
    }

    public static LockManager getLockManager() {
        if(instance == null) {
            instance = new LockManagerInMemoryImpl();
        }
        return instance;
    }

    @Override
    public boolean acquireLock(String resource, String owner) throws LockException {
        return acquireLock(resource, owner, 0);
    }

    @Override
    public boolean acquireLock(String resource, String owner, long timeout) throws LockException {
        debugLogger.debug("Try to acquire lock on resource " + resource + " with owner " + owner);
        long now = System.currentTimeMillis();
        LockValue lockValue = lockedVNFs.get(resource);
        if (lockValue != null) {
            if (lockIsMine(lockValue, owner, now) || hasExpired(lockValue, now)) {
                setExpirationTime(resource, owner, timeout, now);
                debugLogger.debug("Locked successfully resource " + resource + " with owner " + owner + " for " + timeout + " ms");
                return hasExpired(lockValue, now);
            }
            else {
                debugLogger.debug("Owner " + owner + " tried to lock resource " + resource + " but it is already locked by owner " + lockValue.getOwner());
                throw new LockException("Owner " + owner + " tried to lock resource " + resource + " but it is already locked by owner " + lockValue.getOwner());
            }
        }
        else {
            setExpirationTime(resource, owner, timeout, now);
            debugLogger.debug("Locked successfully resource " + resource + " with owner " + owner + " for " + timeout + " ms");
            return true;
        }
    }

    @Override
    public void releaseLock(String resource, String owner) throws LockException {
        debugLogger.debug("Try to release lock on resource " + resource + " with owner " + owner);
        long now = System.currentTimeMillis();
        LockValue lockValue = lockedVNFs.get(resource);
        if (lockValue != null) {
            if (!hasExpired(lockValue, now)) {
                if (isOwner(lockValue, owner)) {
                    debugLogger.debug("Unlocked successfully resource " + resource + " with owner " + owner);
                    lockedVNFs.remove(resource);
                }
                else {
                    debugLogger.debug("Unlock failed. Tried to release lock on resource " + resource + " from owner " + owner + " but it is held by a different owner");
                    throw new LockException("Unlock failed. Tried to release lock on resource " + resource + " from owner " + owner + " but it is held by a different owner");
                }
            }
            else {
                lockedVNFs.remove(resource);
                debugLogger.debug("Unlock failed. lock on resource " + resource + " has expired");
                throw new LockException("Unlock failed. lock on resource " + resource + " has expired");
            }

        }
        else {
            debugLogger.debug("Tried to release lock on resource " + resource + " from owner " + owner + " but there  is not lock on this resource");
            throw new LockException("Tried to release lock on resource " + resource + " from owner " + owner + " but there  is not lock on this resource");
        }
    }

    @Override
    public boolean isLocked(String resource) {
        return lockedVNFs.get(resource)!=null?true:false;
    }

    private boolean lockIsMine(LockValue lockValue, String owner, long now) {
        return isOwner(lockValue, owner) && !hasExpired(lockValue, now);
    }

    private boolean isOwner(LockValue lockValue, String owner) {
        return lockValue.getOwner() != null && lockValue.getOwner().equals(owner);
    }

    private boolean hasExpired(LockValue lockValue, long now) {
        return (lockValue.getExpirationTime() != 0 && now > lockValue.getExpirationTime());
    }

    private void setExpirationTime(String resource, String owner, long timeout, long now) {
        long expirationTime = timeout == 0 ? 0 : now + timeout;
        LockValue lockValue = new LockValue(owner, expirationTime);
        lockedVNFs.put(resource, lockValue);
    }
}
