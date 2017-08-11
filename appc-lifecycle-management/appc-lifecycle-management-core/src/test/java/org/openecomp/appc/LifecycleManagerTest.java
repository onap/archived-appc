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
import org.junit.Test;
import org.openecomp.appc.exceptions.InvalidInputException;
import org.openecomp.appc.lifecyclemanager.LifecycleManager;
import org.openecomp.appc.statemachine.StateMetaDataReader;
import org.openecomp.appc.lifecyclemanager.impl.LifecycleManagerImpl;
import org.openecomp.appc.lifecyclemanager.objects.LifecycleException;
import org.openecomp.appc.lifecyclemanager.objects.NoTransitionDefinedException;
import org.openecomp.appc.statemachine.impl.readers.VnfMetaDataReader;
import org.openecomp.appc.statemachine.impl.readers.VnfMetaDataReader.VNFOperation;
import org.openecomp.appc.statemachine.objects.Event;
import org.openecomp.appc.statemachine.objects.State;
import org.openecomp.appc.statemachine.objects.StateMachineMetadata;
import org.openecomp.appc.statemachine.objects.Transition;

import java.util.*;


public class LifecycleManagerTest {

    private static final State[] VALID_LOCK_STATES = new State[] {
            new State("instantiated"),
            new State("configured"),
            new State("tested"),
            new State("running"),
            new State("error"),
            new State("unknown"),
            new State("created"),
            new State("not orchestrated"),
            new State("stopped"),
    };

    @Test
    public void handleEvent() throws InvalidInputException, LifecycleException, NoTransitionDefinedException {

        StateMachineMetadata metadata = getMetaDataReader().readMetadata();
        LifecycleManagerImpl lifecycleManager = new LifecycleManagerImpl();

        /*
         * Testing Positive Scenario passing the valid events and validating the StateMachineResponse
         */
        for(State state:metadata.getStates()){

            for(Transition transition:state.getTransitions()){
                Event event = transition.getEvent();
                State nextStateFromMetadata = transition.getNextState();

                String expectedNextState = lifecycleManager.getNextState(null,state.toString(),event.toString());
                Assert.assertEquals(expectedNextState,nextStateFromMetadata.toString());
            }
        }

        /*
        Testing Negative Scenarios, 1. Passing the valid Events for which Transition is not defined in
        Metadata and validating the StateMachineResponse 2. Passing the invalid events which are not
        registered as events in the StateMachineMetadata and validating StateMachineResponse
         */
        for(State state:metadata.getStates()){

            for(Transition transition:state.getTransitions()){
                List<Event> negativeEvents = getNegativeEvents(state,metadata.getEvents());

                for(Event negativeEvent:negativeEvents){
                    boolean flag =false;
                    try{
                        lifecycleManager.getNextState(null,state.toString(),negativeEvent.toString());

                    }
                    catch (NoTransitionDefinedException e){
                        flag =true;
                    }
                    Assert.assertEquals(flag,true);

                    flag = false;
                    try{
                        lifecycleManager.getNextState(null,state.toString(),"PUT");
                    }
                    catch(LifecycleException e){
                        flag = true;
                    }
                    Assert.assertTrue(flag);

                }
            }
        }
    }

    @Test
    public void testNotOrchestratedState() throws LifecycleException, NoTransitionDefinedException {
        LifecycleManager lifecycleManager = new LifecycleManagerImpl();
        String nextState = lifecycleManager.getNextState(
                null,"NOT ORCHESTRATED",VNFOperation.Configure.toString());
        Assert.assertEquals(nextState,"Configuring");
    }

    @Test(expected = NoTransitionDefinedException.class)
    public void testBakckingUpState() throws LifecycleException, NoTransitionDefinedException {
        LifecycleManager lifecycleManager = new LifecycleManagerImpl();
        lifecycleManager.getNextState(null,"Software_Uploading",VNFOperation.Configure.toString());
    }

    private List<Event> getNegativeEvents(State state,Set<Event> events) {
        List<Event> negativeEventList = new ArrayList<>();
        negativeEventList.addAll(events);

        for(Transition transition: state.getTransitions()){
            negativeEventList.remove(transition.getEvent());
        }
        return negativeEventList;
    }

    @Test
    public void testLockStates() throws LifecycleException, NoTransitionDefinedException {
        StateMachineMetadata metadata = getMetaDataReader().readMetadata();
        LifecycleManagerImpl lifecycleManager = new LifecycleManagerImpl();

        for(State state: metadata.getStates()) {
            if(isValidState(state, VALID_LOCK_STATES)) {
                assertSameNextState(lifecycleManager, state, VNFOperation.Lock);
                assertSameNextState(lifecycleManager, state, VnfMetaDataReader.VNFOperation.Unlock);
                assertSameNextState(lifecycleManager, state, VNFOperation.CheckLock);
            } else {
                assertNoNextState(lifecycleManager, state, VNFOperation.Lock);
                assertNoNextState(lifecycleManager, state, VNFOperation.Unlock);
                assertNoNextState(lifecycleManager, state, VNFOperation.CheckLock);
            }
        }
    }

    private boolean isValidState(State state, State[] validStates) {
        for(State validState: validStates) {
            if(validState.equals(state)) {
                return true;
            }
        }
        return false;
    }

    private void assertSameNextState(LifecycleManager lifecycleManager, State state, VNFOperation operation)
            throws LifecycleException, NoTransitionDefinedException {
        Assert.assertEquals(state.getStateName(),
                lifecycleManager.getNextState("no-matter", state.getStateName(), operation.toString()));
    }

    private void assertNoNextState(LifecycleManager lifecycleManager, State state, VNFOperation operation)
            throws LifecycleException {
        try {
            lifecycleManager.getNextState("no-matter", state.getStateName(), operation.toString());
            Assert.fail("lifecycleManager.getNextState() should fail for state [" + state
                    + "], operation [" + operation + "]");
        } catch(NoTransitionDefinedException e) {
            // this exception is excepted
        }
    }

    private StateMetaDataReader getMetaDataReader() {
        return new VnfMetaDataReader();
    }
}
