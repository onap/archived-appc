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

package org.openecomp.appc.statemachine.impl;

import java.util.HashSet;
import java.util.Set;

import org.openecomp.appc.statemachine.StateMachine;
import org.openecomp.appc.statemachine.objects.*;


public class StateMachineImpl implements StateMachine {

	private final Set<State> states;

	private final Set<Event> events;

	StateMachineImpl(StateMachineMetadata metadata){
		this.states = new HashSet<State>();
		this.states.addAll(metadata.getStates());
		this.events = new HashSet<Event>();
		this.events.addAll(metadata.getEvents());
	}

	public StateMachineResponse handleEvent(State inputState, Event event) throws InvalidInputException{

		if(!validateInputs(inputState,event)){
			throw new InvalidInputException("VNF State or incoming event is invalid. State = " +inputState + " event = " + event );
		}

		StateMachineResponse response = new StateMachineResponse();
		State currentState = null,nextState = null;
		for(State stateInSet:states){
			if(stateInSet.equals(inputState)){
				currentState = stateInSet;
				break;
			}
		}
		for(Transition transition : currentState.getTransitions()){
			if(event.equals(transition.getEvent())){
				nextState = transition.getNextState();
			}
		}
		if(nextState == null){
			response.setResponse(Response.NO_TRANSITION_DEFINED);
		}
		else if(inputState.equals(nextState)){
			response.setResponse(Response.NO_STATE_CHANGE);
		}
		else{
			response.setResponse(Response.VALID_TRANSITION);
		}
		response.setNextState(nextState);
		return response;
	}

	private boolean validateInputs(State state,Event event){
		if(state ==null || event == null){
			return false;
		}
		if(!(this.states.contains(state) && this.events.contains(event))){
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "StateMachineImpl{" +
				"states=" + states +
				", events=" + events +
				'}';
	}
}
