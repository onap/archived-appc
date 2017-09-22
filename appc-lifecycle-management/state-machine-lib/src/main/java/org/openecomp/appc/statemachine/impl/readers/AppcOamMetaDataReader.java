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

/**
 * Reader for APPC OAM MetaData
 */
public class AppcOamMetaDataReader implements StateMetaDataReader {

    /**
     * APPC Operation Enum
     */
    public enum AppcOperation {
        MaintenanceMode,
        Restart,
        Start,
        Stop
    }

    @Override
    public StateMachineMetadata readMetadata() {
        State notInstantiated = new State(AppcOamStates.NotInstantiated.toString());
        State instantiated = new State(AppcOamStates.Instantiated.toString());
        State restarting = new State(AppcOamStates.Restarting.toString());
        State starting = new State(AppcOamStates.Starting.toString());
        State started = new State(AppcOamStates.Started.toString());
        State enteringMaintenanceMode = new State(AppcOamStates.EnteringMaintenanceMode.toString());
        State maintenanceMode = new State(AppcOamStates.MaintenanceMode.toString());
        State error = new State(AppcOamStates.Error.toString());
        State unknown = new State(AppcOamStates.Unknown.toString());
        State stopping = new State(AppcOamStates.Stopping.toString());
        State stopped = new State(AppcOamStates.Stopped.toString());

        Event start = new Event(AppcOperation.Start.toString());
        Event stop = new Event(AppcOperation.Stop.toString());
        Event maintenanceModeEvent = new Event(AppcOperation.MaintenanceMode.toString());
        Event restart = new Event(AppcOperation.Restart.toString());

        StateMachineMetadata.StateMachineMetadataBuilder builder = new StateMachineMetadata
                .StateMachineMetadataBuilder();

        builder = builder.addState(notInstantiated);
        builder = builder.addState(instantiated);
        builder = builder.addState(starting);
        builder = builder.addState(started);
        builder = builder.addState(error);
        builder = builder.addState(unknown);
        builder = builder.addState(stopping);
        builder = builder.addState(stopped);
        builder = builder.addState(enteringMaintenanceMode);
        builder = builder.addState(maintenanceMode);
        builder = builder.addState(restarting);

        builder = builder.addEvent(start);
        builder = builder.addEvent(stop);
        builder = builder.addEvent(restart);
        builder = builder.addEvent(maintenanceModeEvent);

        /*
         *  for addTransition:
         *  param 1: current state; param 2: received command/request; param 3: new transition state
         */
        // start
        builder = builder.addTransition(stopped,                   start, starting);
        builder = builder.addTransition(maintenanceMode,          start, starting);
        builder = builder.addTransition(error,                     start, starting);
        // stop
        builder = builder.addTransition(started,                   stop, stopping);
        builder = builder.addTransition(starting,                  stop, stopping);
        builder = builder.addTransition(enteringMaintenanceMode, stop, stopping);
        builder = builder.addTransition(maintenanceMode,          stop, stopping);
        builder = builder.addTransition(error,                     stop, stopping);
        // maintenance mode
        builder = builder.addTransition(
                started, maintenanceModeEvent, enteringMaintenanceMode);
        // restart
        builder = builder.addTransition(stopped,                   restart, restarting);
        builder = builder.addTransition(starting,                  restart, restarting);
        builder = builder.addTransition(started,                   restart, restarting);
        builder = builder.addTransition(enteringMaintenanceMode, restart, restarting);
        builder = builder.addTransition(maintenanceMode,          restart, restarting);
        builder = builder.addTransition(error,                     restart, restarting);

        return builder.build();
    }
}
