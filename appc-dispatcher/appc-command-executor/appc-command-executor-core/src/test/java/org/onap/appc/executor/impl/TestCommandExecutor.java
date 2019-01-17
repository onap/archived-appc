/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications (C) 2019 Ericsson
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

package org.onap.appc.executor.impl;
/**
 * 
 */


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.appc.domainmodel.lcm.*;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.executionqueue.ExecutionQueueService;
import org.onap.appc.executor.impl.objects.CommandRequest;
import org.onap.appc.executor.objects.CommandExecutorInput;
import org.onap.appc.requesthandler.RequestHandler;
import org.onap.appc.workflow.WorkFlowManager;
import org.powermock.api.mockito.PowerMockito;

import java.util.Date;
import java.util.concurrent.TimeUnit;


public class TestCommandExecutor {

        private static final String API_VERSION= "2.0.0";
        private static final String ORIGINATOR_ID= "1";

    private CommandExecutorImpl commandExecutor;

    private RequestHandler requestHandler;
    private WorkFlowManager workflowManager;
    private ExecutionQueueService executionQueueService;

    private Date timeStamp = new Date();
    private String requestId = "1";
    private CommandExecutorInput commandExecutorInputConfigure = pouplateCommandExecutorInput("FIREWALL", 30000, "1.0",
            timeStamp, API_VERSION, requestId, ORIGINATOR_ID, "2", VNFOperation.Configure,"15","") ;
    private CommandExecutorInput commandExecutorInputSync = pouplateCommandExecutorInput("FIREWALL", 30, "1.0",
            timeStamp, API_VERSION, requestId, ORIGINATOR_ID, "2", VNFOperation.Sync,"15","") ;
    private CommandTask commandTask;

    @Before
    public void init()throws Exception {
        requestHandler= Mockito.mock(RequestHandler.class);
        workflowManager= Mockito.mock(WorkFlowManager.class);

        executionQueueService = Mockito.mock(ExecutionQueueService.class);

        commandExecutor = Mockito.spy(new CommandExecutorImpl());
        commandExecutor.setExecutionQueueService(executionQueueService);
        commandExecutor.setRequestHandler(requestHandler);
        commandExecutor.setWorkflowManager(workflowManager);
        commandExecutor.initialize();
        commandTask = Mockito.mock(CommandTask.class);
        Mockito.when(commandTask.getCommandRequest()).thenReturn(new CommandRequest(commandExecutorInputConfigure));
        PowerMockito.whenNew(CommandTask.class).withParameterTypes(RequestHandler.class,WorkFlowManager.class).withArguments(requestHandler,workflowManager).thenReturn(commandTask);
    }

    @Test
    public void testPositiveFlow_LCM() throws Exception {
        try {
            Mockito.doReturn(commandTask).when(commandExecutor).getCommandTask(Mockito.any(), Mockito.any());
            commandExecutor.executeCommand(commandExecutorInputConfigure);
        } catch (APPCException e) {
            Assert.fail(e.toString());
        }
    }

    @Test(expected = APPCException.class)
    public void testNegativeFlow_LCM() throws APPCException{
            Mockito.doThrow(new APPCException("Failed to enqueue request")).when(executionQueueService).putMessage((Runnable) Mockito.anyObject(),Mockito.anyLong(),(TimeUnit) Mockito.anyObject());
            commandExecutor.executeCommand(commandExecutorInputSync);
    }

    private CommandExecutorInput pouplateCommandExecutorInput(String vnfType, int ttl, String vnfVersion, Date timeStamp, String apiVersion, String requestId, String originatorID, String subRequestID, VNFOperation action, String vnfId , String payload){
        CommandExecutorInput commandExecutorInput = createCommandExecutorInputWithSubObjects();
        RuntimeContext runtimeContext = commandExecutorInput.getRuntimeContext();
        RequestContext requestContext = runtimeContext.getRequestContext();
        requestContext.getCommonHeader().getFlags().setTtl(ttl);
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
        CommandExecutorInput commandExecutorInput = new CommandExecutorInput();
        RuntimeContext runtimeContext = new RuntimeContext();
        commandExecutorInput.setRuntimeContext(runtimeContext);
        RequestContext requestContext = new RequestContext();
        runtimeContext.setRequestContext(requestContext);
        CommonHeader commonHeader = new CommonHeader();
        requestContext.setCommonHeader(commonHeader);
        Flags flags = new Flags();
        commonHeader.setFlags(flags);
        ActionIdentifiers actionIdentifiers = new ActionIdentifiers();
        requestContext.setActionIdentifiers(actionIdentifiers);
        VNFContext vnfContext = new VNFContext();
        runtimeContext.setVnfContext(vnfContext);
        return commandExecutorInput;
    }
}

