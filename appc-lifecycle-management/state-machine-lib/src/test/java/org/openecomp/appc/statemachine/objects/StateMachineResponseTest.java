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
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;

public class StateMachineResponseTest {
    private StateMachineResponse stateMachineResponse = new StateMachineResponse();

    @Test
    public void testConstructor() {
        StateMachineResponse stateMachineResponse = new StateMachineResponse();
        Assert.assertTrue("Do not: no change to nextState",
                Whitebox.getInternalState(stateMachineResponse, "nextState") == null);
        Assert.assertTrue("Do not: no change to response",
                Whitebox.getInternalState(stateMachineResponse, "response") == null);
    }

    @Test
    public void testGetAndSetNextState() throws Exception {
        stateMachineResponse.setNextState(null);
        Assert.assertTrue("internal nextState should be null",
                Whitebox.getInternalState(stateMachineResponse, "nextState") == null);
        Assert.assertTrue("should return null", stateMachineResponse.getNextState() == null);

        State state = new State("TestingState");
        stateMachineResponse.setNextState(state);
        Assert.assertEquals("internal nextState should be the state",
                state, Whitebox.getInternalState(stateMachineResponse, "nextState"));
        Assert.assertEquals("should return the state", state, stateMachineResponse.getNextState());
    }

    @Test
    public void testGetAndSetResponse() throws Exception {
        stateMachineResponse.setResponse(null);
        Assert.assertTrue("internal response should be null",
                Whitebox.getInternalState(stateMachineResponse, "response") == null);
        Assert.assertTrue("should return null", stateMachineResponse.getResponse() == null);

        Response response = Response.NO_STATE_CHANGE;
        stateMachineResponse.setResponse(response);
        Assert.assertEquals("internal response should be the response",
                response, Whitebox.getInternalState(stateMachineResponse, "response"));
        Assert.assertEquals("should return the response", response, stateMachineResponse.getResponse());
    }

}
