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

import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.Bundle;

import java.util.HashMap;
import java.util.Map;

public class AppcOamStatesTest {

    @Test
    public void testBasicFunctions() {
        AppcOamStates aState = AppcOamStates.EnteringMaintenanceMode;
        Assert.assertEquals("name() does not match", "EnteringMaintenanceMode", aState.name());
        Assert.assertEquals("toString() does not match", "EnteringMaintenanceMode", aState.toString());
        Assert.assertEquals("osgiBundleState does not match", 0, aState.osgiBundleState);
    }

    @Test
    public void testGetOamStateFromBundleState() {
        Map<Integer, AppcOamStates> resultMap = new HashMap<Integer, AppcOamStates>() {
            {
                put(Bundle.UNINSTALLED,     AppcOamStates.NotInstantiated);
                put(Bundle.INSTALLED,       AppcOamStates.Instantiated);
                put(Bundle.RESOLVED,        AppcOamStates.Stopped);
                put(Bundle.STARTING,        AppcOamStates.Starting);
                put(Bundle.STOPPING,        AppcOamStates.Stopping);
                put(Bundle.ACTIVE,          AppcOamStates.Started);
            }
        };
        for (Map.Entry<Integer, AppcOamStates> aEntry : resultMap.entrySet()) {
            Integer bundleState = aEntry.getKey();
            AppcOamStates oamState = aEntry.getValue();
            Assert.assertEquals(String.format("OSGI bundle state(%d) shoule associate with oamState(%s)",
                    bundleState, oamState), oamState, AppcOamStates.getOamStateFromBundleState(bundleState));
        }

        int bundleState = Bundle.START_TRANSIENT;
        Assert.assertEquals(String.format("OSGI bundle state(%d) shoule associate with NotInstantiated state.",
                bundleState), AppcOamStates.NotInstantiated, AppcOamStates.getOamStateFromBundleState(bundleState));

        bundleState = Bundle.STOP_TRANSIENT;
        Assert.assertEquals(String.format("OSGI bundle state(%d) shoule associate with NotInstantiated state.",
                bundleState), AppcOamStates.NotInstantiated, AppcOamStates.getOamStateFromBundleState(bundleState));
    }
}
