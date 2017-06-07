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

package org.openecomp.appc.requesthandler.impl;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

import java.util.concurrent.atomic.AtomicBoolean;

import org.openecomp.appc.requesthandler.LCMStateManager;

public class LCMStateManagerImpl implements LCMStateManager {
    private static final EELFLogger logger = EELFManager.getInstance().getLogger(LCMStateManagerImpl.class);
    private static AtomicBoolean isLCMEnabled = new AtomicBoolean(true);

    /**
     * This method checks if the LCM operations are enabled or not
     * * @return true if enabled else false
     */
    public boolean isLCMOperationEnabled() {
        return isLCMEnabled.get();
    }

    /**
     * This method disables the LCM operations
     */
    public void disableLCMOperations() {
        if (logger.isTraceEnabled()) {
            logger.trace("Entering to disableLCMOperations");
        }
        isLCMEnabled.set(false);
    }

    /**
     * This method enables the LCM operations
     */
    public void enableLCMOperations() {
        if (logger.isTraceEnabled()) {
            logger.trace("Entering to enableLCMOperations");
        }
        isLCMEnabled.set(true);
    }
}
