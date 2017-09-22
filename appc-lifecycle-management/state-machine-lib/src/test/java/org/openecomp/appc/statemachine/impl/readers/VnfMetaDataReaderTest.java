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
import org.junit.Before;
import org.junit.Test;
import org.openecomp.appc.lifecyclemanager.objects.VNFOperationOutcome;
import org.openecomp.appc.statemachine.objects.Event;
import org.openecomp.appc.statemachine.objects.State;
import org.openecomp.appc.statemachine.objects.StateMachineMetadata;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class VnfMetaDataReaderTest {
    private List<String> expectedStateNames = new ArrayList<>();
    private List<String> expectedEventNames = new ArrayList<>();

    private StateMachineMetadata stateMachineMetadata = new VnfMetaDataReader().readMetadata();

    @Before
    public void setUp() throws Exception {
        for (VnfMetaDataReader.VNFStates vnfStates : VnfMetaDataReader.VNFStates.values()) {
            expectedStateNames.add(vnfStates.toString());
        }
        for (VnfMetaDataReader.VNFOperation vnfOperation : VnfMetaDataReader.VNFOperation.values()) {
            expectedEventNames.add(vnfOperation.toString());
        }
        for (VNFOperationOutcome vnfOperationOutcome : VNFOperationOutcome.values()) {
            expectedEventNames.add(vnfOperationOutcome.toString());
        }
    }

    @Test
    public void testReadMetadataForState() throws Exception {
        Set<State> stateSet = stateMachineMetadata.getStates();
        for (State state : stateSet) {
            String eventName = state.getStateName();
            Assert.assertTrue(String.format("Event(%s) should exist in expectedEventNames", eventName),
                    expectedStateNames.contains(eventName));
        }
    }

    @Test
    public void testReadMetadataForEvent() throws Exception {
        Set<Event> eventSet = stateMachineMetadata.getEvents();
        for (Event event : eventSet) {
            String eventName = event.getEventName();
            Assert.assertTrue(String.format("Event(%s) should exist in expectedEventNames", eventName),
                    expectedEventNames.contains(eventName));
        }
    }

}
