/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
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
 * 
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.appc.statemachine.impl.readers;

import org.osgi.framework.Bundle;

public enum AppcOamStates {
    EnteringMaintenanceMode(0),
    Error(0),
    Instantiated(Bundle.INSTALLED),
    MaintenanceMode(0),
    NotInstantiated(Bundle.UNINSTALLED),
    Restarting(0),
    Started(Bundle.ACTIVE),
    Starting(Bundle.STARTING),
    Stopped(Bundle.RESOLVED),
    Stopping(Bundle.STOPPING),
    Unknown(0);

    int osgiBundleState;

    AppcOamStates(Integer bundleState) {
        osgiBundleState = bundleState;
    }

    public static AppcOamStates getOamStateFromBundleState(int bundleState) {
        for (AppcOamStates aState : values()) {
            if (aState.osgiBundleState == bundleState) {
                return aState;
            }
        }
        return Unknown;
    }
}
