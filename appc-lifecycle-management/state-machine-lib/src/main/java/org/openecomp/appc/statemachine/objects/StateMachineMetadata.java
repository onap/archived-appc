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

import java.util.HashSet;
import java.util.Set;


public class StateMachineMetadata {

    private Set<State> states;
    private Set<Event> events;

    private StateMachineMetadata(StateMachineMetadataBuilder builder){
        states = builder.states;
        events = builder.events;
    }

    public Set<State> getStates() {
        return states;
    }

    public Set<Event> getEvents() {
        return events;
    }

    public static class StateMachineMetadataBuilder{

		private Set<State> states;
		private Set<Event> events;

		public StateMachineMetadataBuilder(){
			states = new HashSet<>();
			events = new HashSet<>();
		}

		public StateMachineMetadataBuilder addState(State state){
			this.states.add(state);
			return this;
		}

		public StateMachineMetadataBuilder addEvent(Event event){
			this.events.add(event);
			return this;
		}

		public StateMachineMetadataBuilder addTransition(State currentState,Event event,State nextState){
			Transition transition = new Transition(event,nextState);
			currentState.addTransition(transition);
			return this;
		}

		public StateMachineMetadata build(){
			return new StateMachineMetadata(this);
		}
	}
}
