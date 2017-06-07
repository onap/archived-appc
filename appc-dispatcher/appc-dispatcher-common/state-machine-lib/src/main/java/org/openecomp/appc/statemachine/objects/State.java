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

package org.openecomp.appc.statemachine.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



public class State{
	private String stateName;
	private List<Transition> transitions;

	private State(){
		
	}
	
	public State(String state){
		this();
		this.stateName = state;
		this.transitions = new ArrayList<Transition>();
	}
	
	@Override
	public int hashCode(){
		return this.stateName.hashCode();
	}
	
	@Override
	public boolean equals(Object obj){
		if(obj == null){
			return false;
		}
		if(!(obj instanceof State)){
			return false;
		}
		State state = (State)obj;
		return this.stateName.equals(state.getStateName());
	}
	

	public String getStateName(){
		return stateName;
	}
	
	void addTransition(Transition transition){
		this.transitions.add(transition);
	}

	public List<Transition> getTransitions() {
		return transitions;
	}

	@Override
	public String toString(){
		return this.stateName;
	}
}
