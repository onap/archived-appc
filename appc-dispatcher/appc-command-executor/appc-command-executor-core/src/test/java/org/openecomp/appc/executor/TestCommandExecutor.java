/*-
 * ============LICENSE_START=======================================================
 * openECOMP : APP-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 						reserved.
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
 */

package org.openecomp.appc.executor;
/**
 * 
 */


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.appc.domainmodel.lcm.*;
import org.openecomp.appc.domainmodel.lcm.Flags.Mode;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.executionqueue.ExecutionQueueService;
import org.openecomp.appc.executor.impl.CommandExecutorImpl;
import org.openecomp.appc.executor.impl.CommandTaskFactory;
import org.openecomp.appc.executor.impl.LCMCommandTask;
import org.openecomp.appc.executor.impl.LCMReadonlyCommandTask;
import org.openecomp.appc.executor.objects.CommandExecutorInput;
import org.openecomp.appc.lifecyclemanager.LifecycleManager;
import org.openecomp.appc.requesthandler.RequestHandler;
import org.openecomp.appc.workflow.WorkFlowManager;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertTrue;


@SuppressWarnings("deprecation")
public class TestCommandExecutor {

		private static final String TTL_FLAG= "TTL";
		private static final String API_VERSION= "2.0.0";
		private static final String ORIGINATOR_ID= "1";

	CommandExecutorImpl commandExecutor;

	CommandTaskFactory executionTaskFactory;

	private RequestHandler requestHandler;
	private WorkFlowManager workflowManager;
	private LifecycleManager lifecyclemanager;

	private ExecutionQueueService executionQueueService;

	@Before
	public void init()throws Exception {
		requestHandler= Mockito.mock(RequestHandler.class);
		lifecyclemanager= Mockito.mock(LifecycleManager.class);
		workflowManager= Mockito.mock(WorkFlowManager.class);

		executionQueueService = Mockito.mock(ExecutionQueueService.class);

		commandExecutor = new CommandExecutorImpl();
		executionTaskFactory = Mockito.mock(CommandTaskFactory.class);
		commandExecutor.setExecutionTaskFactory(executionTaskFactory);
		commandExecutor.setExecutionQueueService(executionQueueService);
		LCMCommandTask lcmCommandTask = Mockito.mock(LCMCommandTask.class);
		LCMReadonlyCommandTask LCMReadonlyCommandTask = Mockito.mock(LCMReadonlyCommandTask.class);
		Mockito.doReturn(lcmCommandTask).when(executionTaskFactory).getExecutionTask("Configure");
		Mockito.doReturn(LCMReadonlyCommandTask).when(executionTaskFactory).getExecutionTask("Sync");
//		Mockito.when(executionQueueService.putMessage((Runnable) Mockito.anyObject(),Mockito.anyLong(),(TimeUnit)Mockito.anyObject())).thenReturn(true);

	}
		

	@Test
	public void testPositiveFlow_LCM(){
		//Map <String,Object> flags = setTTLInFlags("30");
		Date timeStamp = new Date();
		String requestId = "1";
		CommandExecutorInput commandExecutorInput = pouplateCommandExecutorInput("FIREWALL", 30, "1.0", timeStamp, API_VERSION, requestId, ORIGINATOR_ID, "2", VNFOperation.Configure, "15", "") ;
		try {
			commandExecutor.executeCommand(commandExecutorInput);
		} catch (APPCException e) {
			Assert.fail(e.toString());
		}

	}

	@Test
	public void testPositiveFlow_GetConfig(){
		Date timeStamp = new Date();
		String requestId = "1";

		CommandExecutorInput commandExecutorInput = pouplateCommandExecutorInput("FIREWALL", 30, "1.0", timeStamp, API_VERSION, requestId, ORIGINATOR_ID, "2", VNFOperation.Sync,"15","") ;
		try {
			commandExecutor.executeCommand(commandExecutorInput);
		} catch (APPCException e) {
			Assert.fail(e.toString());
		}

	}

	
	private CommandExecutorInput pouplateCommandExecutorInput(String vnfType, int ttl, String vnfVersion, Date timeStamp, String apiVersion, String requestId, String originatorID, String subRequestID, VNFOperation action, String vnfId , String payload){
		CommandExecutorInput commandExecutorInput = createCommandExecutorInputWithSubObjects();
		RuntimeContext runtimeContext = commandExecutorInput.getRuntimeContext();
		RequestContext requestContext = runtimeContext.getRequestContext();
		requestContext.getCommonHeader().setFlags(new Flags(null, false, ttl));
		requestContext.getCommonHeader().setApiVer(apiVersion);
		requestContext.getCommonHeader().setTimestamp(timeStamp);
		requestContext.getCommonHeader().setRequestId(requestId);
		requestContext.getCommonHeader().setSubRequestId(subRequestID);
		requestContext.getCommonHeader().setOriginatorId(originatorID);
		requestContext.setAction(action);
		requestContext.setPayload(payload);
		requestContext.getActionIdentifiers().setVnfId(vnfId);
		VNFContext vnfContext = runtimeContext.getVnfContext();
		vnfContext.setType(vnfType);
		vnfContext.setId(vnfId);
		vnfContext.setVersion(vnfVersion);
		return commandExecutorInput;
	}

	private CommandExecutorInput createCommandExecutorInputWithSubObjects() {
		RuntimeContext runtimeContext = new RuntimeContext();
        RequestContext requestContext = new RequestContext();
		runtimeContext.setRequestContext(requestContext);
		CommonHeader commonHeader = new CommonHeader();
		requestContext.setCommonHeader(commonHeader);
		commonHeader.setFlags(new Flags(null, false, 0));
		ActionIdentifiers actionIdentifiers = new ActionIdentifiers();
		requestContext.setActionIdentifiers(actionIdentifiers);
		VNFContext vnfContext = new VNFContext();
		runtimeContext.setVnfContext(vnfContext);
		return new CommandExecutorInput(runtimeContext, 0);
	}



}

