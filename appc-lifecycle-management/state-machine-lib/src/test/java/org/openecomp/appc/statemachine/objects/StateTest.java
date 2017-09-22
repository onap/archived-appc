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

import java.util.List;

public class StateTest {
    private final String STATE_NAME = "Starting";
    private State state = new State(STATE_NAME);

    @SuppressWarnings("unchecked")
    @Test
    public void testConstructor() {
        State state = new State(STATE_NAME);
        Assert.assertEquals("Should set stateName",
                STATE_NAME, Whitebox.getInternalState(state, "stateName"));
        Assert.assertEquals("Should set hash code",
                STATE_NAME.toLowerCase().hashCode(), (int)Whitebox.getInternalState(state, "hashCode"));
        List<Transition> transitions = (List<Transition>) Whitebox.getInternalState(state, "transitions");
        Assert.assertTrue("Should initialized transtiions",
                transitions != null && transitions.isEmpty());
    }

    @Test
    public void testHashCode() throws Exception {
        Assert.assertEquals("Should return proper hash code",
                STATE_NAME.toLowerCase().hashCode(), state.hashCode());
    }

    @Test
    public void testEquals() throws Exception {
        Assert.assertFalse("should return false for null", state.equals(null));
        Assert.assertFalse("should return false for object", state.equals(new Event(STATE_NAME)));
        Assert.assertFalse("should return false for different event",
                state.equals(new Event("Another")));
        Assert.assertTrue("should return true", state.equals(new State(STATE_NAME)));
        Assert.assertTrue("should return true (lower case)", state.equals(new State(STATE_NAME.toLowerCase())));
    }

    @Test
    public void testGetStateName() throws Exception {
        Assert.assertEquals("Should return STATE_NAME", STATE_NAME, state.getStateName());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAddAndGetTransition() throws Exception {
        Transition transition1 = new Transition(new Event("event1"), new State("state2"));
        List<Transition> transitions = (List<Transition>) Whitebox.getInternalState(state, "transitions");
        Assert.assertFalse("should not have transition1", transitions.contains(transition1));
        state.addTransition(transition1);
        transitions = (List<Transition>) Whitebox.getInternalState(state, "transitions");
        Assert.assertTrue("should have added transition1", transitions.contains(transition1));
        Assert.assertEquals("Should return transitions", transitions, state.getTransitions());

        state.addTransition(null);
        Assert.assertEquals("Should not change transitions", transitions,
                Whitebox.getInternalState(state, "transitions"));
    }

    @Test
    public void testToString() throws Exception {
        Assert.assertEquals("Should return STATE_NAME", STATE_NAME, state.toString());
    }
}
