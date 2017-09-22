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

package org.openecomp.appc.statemachine.objects;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;

public class StateMachineMetadataTest {
    private StateMachineMetadata.StateMachineMetadataBuilder builder;
    private StateMachineMetadata metadata;
    @Before
    public void setUp() throws Exception {
        builder = new StateMachineMetadata.StateMachineMetadataBuilder();
        builder.addEvent(new Event("TestingEvent1"));
        builder.addEvent(new Event("TestingEvent2"));
        builder.addState(new State("TestingState1"));
        builder.addState(new State("TestingState2"));
        builder.addState(new State("TestingState3"));
        builder.addTransition(
                new State("TestingState1"), new Event("TestingEvent1"), new State("TestingState2"));

        metadata = builder.build();
    }

    @Test
    public void getStates() throws Exception {
        Assert.assertEquals("Should return proper States",
                Whitebox.getInternalState(builder, "states"), metadata.getStates());
    }

    @Test
    public void getEvents() throws Exception {
        Assert.assertEquals("Should return proper Events",
                Whitebox.getInternalState(builder, "events"), metadata.getEvents());
    }

}
