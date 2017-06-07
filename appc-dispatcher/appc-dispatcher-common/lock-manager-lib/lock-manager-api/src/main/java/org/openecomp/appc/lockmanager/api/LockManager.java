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

/**
 * Enables locking and unlocking of a resource by id.
 * If the resource is locked, only lock owner can reacquire the lock or unlock the resource.
 */
public interface LockManager {

    /**
     * Lock resource without timeout. Lock never expires.
     *
     * @param resource resource id
     * @param owner lock owner id
     * @return true - if lock is acquired, false - if the resource was already locked by the owner
     * @throws LockException thrown if resource is already locked by other owner
     */
    boolean acquireLock(String resource, String owner) throws LockException;

    /**
     * Lock resource with timeout. After the timeout resource becomes unlocked.
     *
     * @param resource resource id
     * @param owner lock owner id
     * @param timeout in milliseconds, after this timeout lock will expire and resource becomes unlocked,
     *  timeout == 0 means that the lock never expires - same as call acquireLock() without timeout parameter
     * @return true - if lock is acquired, false - if the resource was already locked by the owner
     * @throws LockException thrown if resource is already locked by other owner
     */
    boolean acquireLock(String resource, String owner, long timeout) throws LockException;

    /**
     * Unlock resource.
     *
     * @param resource resource id
     * @param owner lock owner id
     * @throws LockException thrown if resource is locked by other owner
     */
    void releaseLock(String resource, String owner) throws LockException;

    /**
     * check resource lock status.
     *
     * @param resource resource id
     */

    boolean isLocked(String resource);

}
