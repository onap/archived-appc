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

package org.openecomp.appc.statemachine.impl.readers;

import org.openecomp.appc.statemachine.StateMetaDataReader;
import org.openecomp.appc.statemachine.objects.Event;
import org.openecomp.appc.statemachine.objects.State;
import org.openecomp.appc.statemachine.objects.StateMachineMetadata;

public class AppcOamMetaDataReader implements StateMetaDataReader {

    public enum AppcOperation {
        MaintenanceMode,
        Restart,
        Start,
        Stop
    }

    @Override
    public StateMachineMetadata readMetadata() {
        State NOT_INSTANTIATED = new State(AppcOamStates.NotInstantiated.toString());
        State INSTANTIATED = new State(AppcOamStates.Instantiated.toString());
        State RESTARTING = new State(AppcOamStates.Restarting.toString());
        State STARTING = new State(AppcOamStates.Starting.toString());
        State STARTED = new State(AppcOamStates.Started.toString());
        State ENTERING_MAINTENANCE_MODE = new State(AppcOamStates.EnteringMaintenanceMode.toString());
        State MAINTENANCE_MODE = new State(AppcOamStates.MaintenanceMode.toString());
        State ERROR = new State(AppcOamStates.Error.toString());
        State UNKNOWN = new State(AppcOamStates.Unknown.toString());
        State STOPPING = new State(AppcOamStates.Stopping.toString());
        State STOPPED = new State(AppcOamStates.Stopped.toString());

        Event START = new Event(AppcOperation.Start.toString());
        Event STOP = new Event(AppcOperation.Stop.toString());
        Event MAINTENANCE_MODE_EVENT = new Event(AppcOperation.MaintenanceMode.toString());
        Event RESTART = new Event(AppcOperation.Restart.toString());

        StateMachineMetadata.StateMachineMetadataBuilder builder = new StateMachineMetadata
                .StateMachineMetadataBuilder();

        builder = builder.addState(NOT_INSTANTIATED);
        builder = builder.addState(INSTANTIATED);
        builder = builder.addState(STARTING);
        builder = builder.addState(STARTED);
        builder = builder.addState(ERROR);
        builder = builder.addState(UNKNOWN);
        builder = builder.addState(STOPPING);
        builder = builder.addState(STOPPED);
        builder = builder.addState(ENTERING_MAINTENANCE_MODE);
        builder = builder.addState(MAINTENANCE_MODE);
        builder = builder.addState(RESTARTING);

        builder = builder.addEvent(START);
        builder = builder.addEvent(STOP);
        builder = builder.addEvent(RESTART);
        builder = builder.addEvent(MAINTENANCE_MODE_EVENT);

        /*
         *  for addTransition:
         *  param 1: current state; param 2: received command/request; param 3: new transition state
         */
        // start
        builder = builder.addTransition(STOPPED,                   START, STARTING);
        builder = builder.addTransition(MAINTENANCE_MODE,          START, STARTING);
        builder = builder.addTransition(ERROR,                     START, STARTING);
        // stop
        builder = builder.addTransition(STARTED,                   STOP, STOPPING);
        builder = builder.addTransition(STARTING,                  STOP, STOPPING);
        builder = builder.addTransition(ENTERING_MAINTENANCE_MODE, STOP, STOPPING);
        builder = builder.addTransition(MAINTENANCE_MODE,          STOP, STOPPING);
        builder = builder.addTransition(ERROR,                     STOP, STOPPING);
        // maintenance mode
        builder = builder.addTransition(
                STARTED, MAINTENANCE_MODE_EVENT, ENTERING_MAINTENANCE_MODE);
        // restart
        builder = builder.addTransition(STOPPED,                   RESTART, RESTARTING);
        builder = builder.addTransition(STARTING,                  RESTART, RESTARTING);
        builder = builder.addTransition(STARTED,                   RESTART, RESTARTING);
        builder = builder.addTransition(ENTERING_MAINTENANCE_MODE, RESTART, RESTARTING);
        builder = builder.addTransition(MAINTENANCE_MODE,          RESTART, RESTARTING);
        builder = builder.addTransition(ERROR,                     RESTART, RESTARTING);

        return builder.build();
    }
}
