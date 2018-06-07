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

package org.onap.appc.statemachine.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * State Object
 */
public class State {
    private final String stateName;
    private final int hashCode;
    private final List<Transition> transitions;

    /**
     * Constructor
     * @param stateName String of the state name
     */
    public State(String stateName) {
        this.stateName = stateName;
        this.hashCode = stateName.toLowerCase().hashCode();
        this.transitions = new ArrayList<>();
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof State)) {
            return false;
        }
        State state = (State)obj;
        return this.stateName.equalsIgnoreCase(state.getStateName());
    }

    public String getStateName() {
        return stateName;
    }

    void addTransition(Transition transition) {
        if (transition != null) {
        this.transitions.add(transition);
    }
  }
    public List<Transition> getTransitions() {
        return transitions;
    }

    @Override
    public String toString() {
        return this.stateName;
    }
}
