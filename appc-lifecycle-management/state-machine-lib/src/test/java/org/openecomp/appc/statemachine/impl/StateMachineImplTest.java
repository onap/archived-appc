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

package org.openecomp.appc.statemachine.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;
import org.openecomp.appc.exceptions.InvalidInputException;
import org.openecomp.appc.statemachine.objects.Event;
import org.openecomp.appc.statemachine.objects.Response;
import org.openecomp.appc.statemachine.objects.State;
import org.openecomp.appc.statemachine.objects.StateMachineMetadata;
import org.openecomp.appc.statemachine.objects.StateMachineResponse;

public class StateMachineImplTest {
    private StateMachineMetadata metadata;
    private StateMachineImpl stateMachine;

    private State state1 = new State("TestingState1");
    private State state2 = new State("TestingState2");
    private Event event1 = new Event("TestingEvent1");
    private Event event2 = new Event("TestingEvent2");

    @Before
    public void setUp() throws Exception {
        StateMachineMetadata.StateMachineMetadataBuilder builder =
                new StateMachineMetadata.StateMachineMetadataBuilder();
        builder.addEvent(event1);
        builder.addEvent(event2);
        builder.addState(state1);
        builder.addState(state2);
        builder.addState(new State("TestingState3"));
        builder.addTransition(state1, event1, state2);

        metadata = builder.build();

        stateMachine = new StateMachineImpl(metadata);
    }

    @Test
    public void testConstructor() throws Exception {
        StateMachineImpl stateMachine = new StateMachineImpl(metadata);
        Assert.assertEquals("Should have set internal states",
                metadata.getStates(), Whitebox.getInternalState(stateMachine, "states"));
        Assert.assertEquals("Should have set internal events",
                metadata.getEvents(), Whitebox.getInternalState(stateMachine, "events"));
    }

    @Test(expected = InvalidInputException.class)
    public void testHandleEventThrowsInvalidInputException() throws Exception {
        stateMachine.handleEvent(null, null);
    }

    @Test
    public void testHandleEvent() throws Exception  {
        StateMachineResponse response = stateMachine.handleEvent(state1, event1);
        Assert.assertEquals(Response.VALID_TRANSITION, response.getResponse());
        Assert.assertEquals(state2, response.getNextState());

        response = stateMachine.handleEvent(state2, event1);
        Assert.assertEquals(Response.NO_TRANSITION_DEFINED, response.getResponse());
        Assert.assertTrue(response.getNextState() == null);
    }

    @Test
    public void testValidateInputs() {
        Assert.assertFalse(stateMachine.validateInputs(null, null));
        Assert.assertFalse(stateMachine.validateInputs(new State("state1"), null));
        Assert.assertFalse(stateMachine.validateInputs(null, new Event("event1")));
        Assert.assertFalse(stateMachine.validateInputs(new State("state1"), new Event("event1")));
        Assert.assertFalse(stateMachine.validateInputs(state1, new Event("event1")));
        Assert.assertFalse(stateMachine.validateInputs(new State("state1"), event1));
        Assert.assertTrue(stateMachine.validateInputs(state1, event1));

    }

    @Test
    public void testToString() throws Exception {
        Assert.assertEquals(
                String.format(stateMachine.toStringFormat, metadata.getStates(), metadata.getEvents()),
                stateMachine.toString());
    }

}
