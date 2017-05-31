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

package org.openecomp.appc.lifecyclemanager.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.openecomp.appc.i18n.Msg;
import org.openecomp.appc.lifecyclemanager.LifecycleManager;
import org.openecomp.appc.lifecyclemanager.helper.MetadataReader;
import org.openecomp.appc.lifecyclemanager.objects.*;
import org.openecomp.appc.statemachine.*;
import org.openecomp.appc.statemachine.impl.StateMachineFactory;
import org.openecomp.appc.statemachine.objects.*;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;


public class LifecycleManagerImpl implements LifecycleManager{

	private MetadataReader metadataReader;
	private static Map<String,StateMachine> stateMachineMap = new ConcurrentHashMap<String,StateMachine>();
	private static final EELFLogger logger = EELFManager.getInstance().getLogger(LifecycleManagerImpl.class);
	private static EELFLogger errorLogger = EELFManager.getInstance().getErrorLogger();
	public LifecycleManagerImpl(){
		this.metadataReader = new MetadataReader();
	}

	@Override
	public String getNextState(String vnfType, String currentState, String event) throws NoTransitionDefinedException,LifecycleException{
		if (logger.isTraceEnabled()) {
			logger.trace("Entering to getNextState with vnfType = "+ vnfType +	", currentState = " + currentState + ", event = " + event);
		}

		State nextState = null;
		StateMachine machine = null;
		StateMachineResponse response;
		try {
			machine = this.getStateMachine(vnfType);
			response = machine.handleEvent(new State(currentState),new Event(event));
			if(Response.NO_TRANSITION_DEFINED.equals(response.getResponse())){
				errorLogger.error(EELFResourceManager.format(Msg.VF_ILLEGAL_COMMAND, vnfType,event,currentState));
				throw new NoTransitionDefinedException("No Transition Defined for currentState = " +  currentState + ", event = " + event,currentState,event);
			}
			nextState = response.getNextState();
		} catch (InvalidInputException e) {
			logger.error(e.getMessage());
			throw new LifecycleException(e,currentState,event);
		}
		if (logger.isTraceEnabled()) {
			logger.trace("Exiting from getNextState with (nextState = "+nextState.getStateName()!=null?nextState.getStateName():"null"+")");
		}
		return nextState.getStateName();
	}

	private StateMachine getStateMachine(String vnfType){
		if (logger.isTraceEnabled()) {
			logger.trace("Entering to getNextState with vnfType = "+ vnfType);
		}
		if(vnfType == null){
			vnfType = "DEFAULT";
		}
		StateMachine machine = stateMachineMap.get(vnfType);
		if(machine == null){
			StateMachineMetadata metadata = metadataReader.readMetadata(vnfType);
			machine = StateMachineFactory.getStateMachine(metadata);
			stateMachineMap.put(vnfType,machine);
		}

		logger.trace("Exiting getStateMachine with (StateMachine = "+stateMachineMap.get(vnfType)!=null?stateMachineMap.get(vnfType).toString():"null"+")");
		return stateMachineMap.get(vnfType);
	}

}
