/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications (C) 2018 Ericsson
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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.oam.util;

import com.att.eelf.configuration.EELFLogger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.AppcState;
import org.onap.appc.statemachine.impl.readers.AppcOamStates;
import org.osgi.framework.Bundle;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

@RunWith(PowerMockRunner.class)
public class StateHelperTest {
    private StateHelper stateHelper;

    @Before
    public void setUp() throws Exception {
        stateHelper = PowerMockito.spy(new StateHelper(null, null));

        // to avoid operation on logger fail, mock up the logger
        EELFLogger mockLogger = mock(EELFLogger.class);
        Whitebox.setInternalState(stateHelper, "logger", mockLogger);
    }

    @Test
    public void testSetState() throws Exception {
        AppcOamStates appcOamStates = AppcOamStates.Started;
        stateHelper.setState(appcOamStates);
        Assert.assertEquals("Should have the new value", appcOamStates,
                Whitebox.getInternalState(stateHelper, "appcOamCurrentState"));
        // reset to default value
        stateHelper.setState(AppcOamStates.Unknown);
    }

    @Test
    public void testGetState() throws Exception {
        AppcOamStates appcOamStates = stateHelper.getState();
        Assert.assertEquals("Should have the class value", appcOamStates,
                Whitebox.getInternalState(stateHelper, "appcOamCurrentState"));
    }

    @Test
    public void testIsSameState() throws Exception {
        AppcOamStates classValue = Whitebox.getInternalState(stateHelper, "appcOamCurrentState");
        for (AppcOamStates appcOamStates : AppcOamStates.values()) {
            boolean isSame = stateHelper.isSameState(appcOamStates);
            if (appcOamStates == classValue) {
                Assert.assertTrue("Should be the same", isSame);
            } else {
                Assert.assertFalse("Should not be the same", isSame);
            }
        }
    }

    @Test
    public void testGetCurrentOamState() throws Exception {
        AppcOamStates mockResult = AppcOamStates.Started;
        // mock getBundlesState, as we are testing it separately
        PowerMockito.doReturn(mockResult).when(stateHelper, "getBundlesState");

        Whitebox.setInternalState(stateHelper, "appcOamCurrentState", AppcOamStates.Unknown);
        Assert.assertEquals("Should call deriveStatte and return mockeResult",
                mockResult, stateHelper.getCurrentOamState());
        Mockito.verify(stateHelper, times(1)).getBundlesState();


        Whitebox.setInternalState(stateHelper, "appcOamCurrentState", AppcOamStates.Unknown);
        Assert.assertEquals("Should call deriveStatte and return mockeResult",
                mockResult, stateHelper.getCurrentOamState());
        Mockito.verify(stateHelper, times(2)).getBundlesState();

        Whitebox.setInternalState(stateHelper, "appcOamCurrentState", mockResult);
        Assert.assertEquals("Should just return mockeResult", mockResult, stateHelper.getCurrentOamState());
        Mockito.verify(stateHelper, times(2)).getBundlesState();
    }

    @Test
    public void testGetCurrentOamYangState() throws Exception {
        Map<AppcOamStates, AppcState> stateMap = new HashMap<AppcOamStates, AppcState>() {
            {
                put(AppcOamStates.EnteringMaintenanceMode, AppcState.EnteringMaintenanceMode);
                put(AppcOamStates.MaintenanceMode,         AppcState.MaintenanceMode);
                put(AppcOamStates.Instantiated,            AppcState.Instantiated);
                put(AppcOamStates.NotInstantiated,         AppcState.NotInstantiated);
                put(AppcOamStates.Restarting,              AppcState.Restarting);
                put(AppcOamStates.Started,                 AppcState.Started);
                put(AppcOamStates.Starting,                AppcState.Starting);
                put(AppcOamStates.Stopped,                 AppcState.Stopped);
                put(AppcOamStates.Stopping,                AppcState.Stopping);
                put(AppcOamStates.Error,                   AppcState.Error);
                put(AppcOamStates.Unknown,                 AppcState.Unknown);
            }
        };
        for (Map.Entry<AppcOamStates, AppcState> aEntry : stateMap.entrySet()) {
            AppcOamStates aState = aEntry.getKey();
            AppcState appcState = aEntry.getValue();

            PowerMockito.doReturn(aState).when(stateHelper, "getCurrentOamState");

            AppcState resultState = stateHelper.getCurrentOamYangState();
            Assert.assertEquals(
                    String.format("%s state, returned(%s),should return(%s) state", aState, resultState, appcState),
                    appcState, resultState);

        }
    }

    @Test
    public void testGetBundlesState() throws Exception {
        BundleHelper mockBundleHelper = mock(BundleHelper.class);
        Mockito.doReturn(mockBundleHelper).when(stateHelper).getBundleHelper(Mockito.any(EELFLogger.class), Mockito.any(ConfigurationHelper.class));
        
        // test null bundle map
        Mockito.when(mockBundleHelper.getAppcLcmBundles()).thenReturn(null);
        Assert.assertEquals("Should return unknown state", AppcOamStates.Unknown, stateHelper.getBundlesState());

        // tet empty bundle map
        Map<String, Bundle> bundleMap = new HashMap<>();
        Mockito.when(mockBundleHelper.getAppcLcmBundles()).thenReturn(bundleMap);
        Assert.assertEquals("Should return unknown state", AppcOamStates.Unknown, stateHelper.getBundlesState());

        Bundle mockBundle1 = mock(Bundle.class);
        Bundle mockBundle2 = mock(Bundle.class);
        bundleMap.put("1", mockBundle1);
        bundleMap.put("2", mockBundle2);
        Mockito.when(mockBundleHelper.getAppcLcmBundles()).thenReturn(bundleMap);

        // test bundles have differnt states
        Mockito.doReturn(Bundle.RESOLVED).when(mockBundle1).getState();
        Mockito.doReturn(Bundle.ACTIVE).when(mockBundle2).getState();
        Assert.assertEquals("Should return lower state", AppcOamStates.Stopped, stateHelper.getBundlesState());

        // test bundles have the same state
        Mockito.doReturn(Bundle.ACTIVE).when(mockBundle1).getState();
        Assert.assertEquals("Should return the state", AppcOamStates.Started, stateHelper.getBundlesState());
    }
}
