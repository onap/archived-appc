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

public class TransitionTest {
    private final State state = new State("TestingState");
    private final Event event = new Event("TestingEvent");
    private Transition transition = new Transition(event, state);

    @Test
    public void testConstructor() {
        transition = new Transition(event, state);
        Assert.assertEquals("Should set event",
                event, Whitebox.getInternalState(transition, "event"));
        Assert.assertEquals("Should set nextState",
                state, Whitebox.getInternalState(transition, "nextState"));
    }

    @Test
    public void testGetEvent() throws Exception {
        Assert.assertEquals("Should return internal event",
                Whitebox.getInternalState(transition, "event"), transition.getEvent());
    }

    @Test
    public void testGetNextState() throws Exception {
        Assert.assertEquals("Should return internal nextState",
                Whitebox.getInternalState(transition, "nextState"), transition.getNextState());
    }

}
