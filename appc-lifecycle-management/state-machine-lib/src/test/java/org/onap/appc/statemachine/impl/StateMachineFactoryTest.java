/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.statemachine.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.onap.appc.statemachine.StateMachine;
import org.onap.appc.statemachine.objects.Event;
import org.onap.appc.statemachine.objects.State;
import org.onap.appc.statemachine.objects.StateMachineMetadata;

public class StateMachineFactoryTest {
    private StateMachineMetadata metadata;

    @Before
    public void setUp() throws Exception {
        StateMachineMetadata.StateMachineMetadataBuilder builder =
                new StateMachineMetadata.StateMachineMetadataBuilder();
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
    public void testGetStateMachine() throws Exception {
        StateMachine stateMachine = StateMachineFactory.getStateMachine(metadata);
        Assert.assertTrue("Should return StateMachineImpl", stateMachine instanceof StateMachineImpl);
    }

}
