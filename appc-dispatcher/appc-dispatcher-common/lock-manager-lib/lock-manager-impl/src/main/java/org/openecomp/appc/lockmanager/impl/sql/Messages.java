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

public enum Messages {

    ERR_NULL_LOCK_OWNER("Cannot acquire lock for resource [%s]: lock owner must be specified"),
    ERR_LOCK_LOCKED_BY_OTHER("Cannot lock resource [%s] for [%s]: already locked by [%s]"),
    ERR_UNLOCK_NOT_LOCKED("Error unlocking resource [%s]: resource is not locked"),
    ERR_UNLOCK_LOCKED_BY_OTHER("Error unlocking resource [%s] by [%s]: resource is locked by [%s]"),
    EXP_LOCK("Error locking resource [%s]."),
    EXP_CHECK_LOCK("Error check locking resource [%s]."),//for checklock operation
    EXP_UNLOCK("Error unlocking resource [%s]."),
    ;

    private String message;

    Messages(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String format(Object... s) {
        return String.format(message, s);
    }
}
