/*-
 * ============LICENSE_START=======================================================
 * APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Amdocs
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */

package org.openecomp.appc.statemachine;


import org.junit.Assert;
import org.junit.Test;
import org.openecomp.appc.statemachine.impl.StateMachineFactory;
import org.openecomp.appc.statemachine.objects.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class TestStateMachine {

    @Test
    public void handleEvent() throws InvalidInputException {

//        MetadataReader metadataReader = new MetadataReader();
//        StateMachineMetadata metadata = metadataReader.readMetadata(null);
//
//        StateMachine machine = StateMachineFactory.getStateMachine(metadata);
//
//        /*
//        Testing Positive Scenario passing the valid events and validating the StateMachineResponse
//         */
//        for(State state:metadata.getStates()){
//
//            for(Transition transition:state.getTransitions()){
//                Event event = transition.getEvent();
//                State nextState = transition.getNextState();
//
//                StateMachineResponse response = machine.handleEvent(state,event);
//                Assert.assertEquals(response.getNextState(),nextState);
//                Assert.assertEquals(response.getResponse(),Response.VALID_TRANSITION);
//            }
//        }
//
//        /*
//        Testing Negative Scenarios, 1. Passing the valid Events for which Transition is not defined in
//        Metadata and validating the StateMachineResponse 2. Passing the invalid events which are not
//        registered as events in the StateMachineMetadata and validating StateMachineResponse
//         */
//        for(State state:metadata.getStates()){
//
//            for(Transition transition:state.getTransitions()){
//                List<Event> negativeEvents = getNegativeEvents(state,metadata.getEvents());
//
//                for(Event negativeEvent:negativeEvents){
//                    StateMachineResponse response = machine.handleEvent(state,negativeEvent);
//                    Assert.assertEquals(response.getNextState(),null);
//                    Assert.assertEquals(response.getResponse(),Response.NO_TRANSITION_DEFINED);
//
//                    boolean flag =false;
//                    try{
//                        response = machine.handleEvent(state,new Event("PUT"));
//                    }
//                    catch(InvalidInputException e){
//                        flag = true;
//                    }
//                    Assert.assertTrue(flag);
//
//                }
//            }
//        }
    }

//    private List<Event> getNegativeEvents(State state,Set<Event> events) {
//        List<Event> negativeEventList = new ArrayList<>();
//        negativeEventList.addAll(events);
//
//        for(Transition transition: state.getTransitions()){
//            negativeEventList.remove(transition.getEvent());
//        }
//        return negativeEventList;
//    }
}
