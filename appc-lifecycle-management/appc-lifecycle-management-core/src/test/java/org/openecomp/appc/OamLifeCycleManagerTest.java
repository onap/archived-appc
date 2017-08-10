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


package org.openecomp.appc;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openecomp.appc.lifecyclemanager.impl.LifecycleManagerImpl;
import org.openecomp.appc.lifecyclemanager.objects.LifecycleException;
import org.openecomp.appc.lifecyclemanager.objects.NoTransitionDefinedException;
import org.openecomp.appc.statemachine.impl.readers.AppcOamMetaDataReader;
import org.openecomp.appc.statemachine.impl.readers.AppcOamStates;

import java.util.Arrays;
import java.util.List;


public class OamLifeCycleManagerTest {
    private static final String VNF_TYPE_APPC = "APPC";
    private static final String NO_DEFINITION_FORMAT = "No Transition Defined for currentState = %s, event = %s";

    private LifecycleManagerImpl lifecycleManager;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        lifecycleManager = new LifecycleManagerImpl();
    }

    private void validateProper(String state, String event, String expectedResult)
            throws LifecycleException, NoTransitionDefinedException {
        String nextState = lifecycleManager.getNextState(VNF_TYPE_APPC, state, event);
        Assert.assertEquals(String.format("Should return %s", expectedResult), expectedResult, nextState);
    }

    private void validateException(String state, String event) throws LifecycleException, NoTransitionDefinedException {
        expectedException.expect(NoTransitionDefinedException.class);
        expectedException.expectMessage(String.format(NO_DEFINITION_FORMAT, state, event));
        lifecycleManager.getNextState(VNF_TYPE_APPC, state, event);

        // Reset to no expectation
        expectedException = ExpectedException.none();
    }

    @Test
    public void testOamStateTransitionForMaintenanceMode() throws Exception {
        String event = AppcOamMetaDataReader.AppcOperation.MaintenanceMode.name();
        String expecteResult = AppcOamStates.EnteringMaintenanceMode.toString();

        for (AppcOamStates appcOamStates : AppcOamStates.values()) {
            String state = appcOamStates.toString();
            if (appcOamStates == AppcOamStates.Started) {
                validateProper(state, event, expecteResult);
            } else {
                validateException(state, event);
            }
        }
    }

    @Test
    public void testOamStateTransitionForStart() throws Exception {
        String event = AppcOamMetaDataReader.AppcOperation.Start.name();
        String expectResult = AppcOamStates.Starting.toString();

        List<AppcOamStates> goodStates = Arrays.asList(
                AppcOamStates.MaintenanceMode,
                AppcOamStates.Stopped,
                AppcOamStates.Stopping);

        for (AppcOamStates appcOamStates : AppcOamStates.values()) {
            String state = appcOamStates.toString();
            if (goodStates.contains(appcOamStates)) {
                validateProper(state, event, expectResult);
            } else {
                validateException(state, event);
            }
        }
    }

    @Test
    public void testOamStateTransitionForStop() throws Exception {
        String event = AppcOamMetaDataReader.AppcOperation.Stop.name();
        String expectResult = AppcOamStates.Stopping.toString();

        List<AppcOamStates> goodStates = Arrays.asList(
                AppcOamStates.Error,
                AppcOamStates.EnteringMaintenanceMode,
                AppcOamStates.MaintenanceMode,
                AppcOamStates.Started,
                AppcOamStates.Starting);

        for (AppcOamStates appcOamStates : AppcOamStates.values()) {
            String state = appcOamStates.toString();
            if (goodStates.contains(appcOamStates)) {
                validateProper(state, event, expectResult);
            } else {
                validateException(state, event);
            }
        }
    }


    @Test
    public void testOamStateTransitionForRestart() throws Exception {
        String event = AppcOamMetaDataReader.AppcOperation.Restart.name();
        String expectResult = AppcOamStates.Restarting.toString();

        List<AppcOamStates> goodStates = Arrays.asList(
                AppcOamStates.Error,
                AppcOamStates.EnteringMaintenanceMode,
                AppcOamStates.MaintenanceMode,
                AppcOamStates.Started,
                AppcOamStates.Starting,
                AppcOamStates.Stopped,
                AppcOamStates.Stopping);

        for (AppcOamStates appcOamStates : AppcOamStates.values()) {
            String state = appcOamStates.toString();
            if (goodStates.contains(appcOamStates)) {
                validateProper(state, event, expectResult);
            } else {
                validateException(state, event);
            }
        }
    }
}
