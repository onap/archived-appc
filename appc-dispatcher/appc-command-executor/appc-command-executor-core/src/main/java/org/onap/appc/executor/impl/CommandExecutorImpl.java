/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications Copyright (C) 2019 Ericsson
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


import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.apache.commons.lang.ObjectUtils;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.executionqueue.ExecutionQueueService;
import org.onap.appc.executor.CommandExecutor;
import org.onap.appc.executor.impl.objects.CommandRequest;
import org.onap.appc.executor.objects.CommandExecutorInput;
import org.onap.appc.requesthandler.RequestHandler;
import org.onap.appc.workflow.WorkFlowManager;

import java.util.Date;
import java.util.concurrent.TimeUnit;


public class CommandExecutorImpl implements CommandExecutor {

    private final EELFLogger logger = EELFManager.getInstance().getLogger(CommandExecutorImpl.class);

    private ExecutionQueueService executionQueueService;
    private RequestHandler requestHandler;
    private WorkFlowManager workflowManager;

    /**
     * Initialization.
     * <p>Used through blueprint.
     */
    public void initialize() {
        logger.info("initialization started of CommandExecutorImpl");
    }

    public void setExecutionQueueService(ExecutionQueueService executionQueueService) {
        this.executionQueueService = executionQueueService;
    }

    public void setWorkflowManager(WorkFlowManager workflowManager) {
        this.workflowManager = workflowManager;
    }

    public void setRequestHandler(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }


    /**
     * Execute given command
     * Create command request and enqueue it for execution.
     * @param commandExecutorInput Contains CommandHeader,  command , target Id , payload and conf ID (optional)
     * @throws APPCException in case of error.
     */
    @Override
    public void executeCommand (CommandExecutorInput commandExecutorInput) throws APPCException{
        logger.trace("Entering to executeCommand with CommandExecutorInput = "+ ObjectUtils.toString(commandExecutorInput));
        CommandTask commandTask;
        try {
            commandTask= getCommandTask(requestHandler, workflowManager);
            commandTask.setCommandRequest(new CommandRequest(commandExecutorInput));
            long remainingTTL = getRemainingTTL(commandTask.getCommandRequest());
            logger.trace("Queuing request with CommandRequest = "+ ObjectUtils.toString(commandTask.getCommandRequest()));
            executionQueueService.putMessage(commandTask,remainingTTL, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            logger.error("Exception: "+e.getMessage());
            throw new APPCException(e);
        }
        logger.trace("Exiting from executeCommand");
    }

    private long getRemainingTTL(CommandRequest request) {
        Date requestTimestamp = request.getCommandExecutorInput().getRuntimeContext().getRequestContext().getCommonHeader().getTimeStamp();
        int ttl = request.getCommandExecutorInput().getRuntimeContext().getRequestContext().getCommonHeader().getFlags().getTtl();
        return ttl*1000 + requestTimestamp.getTime() - System.currentTimeMillis();
    }

    protected CommandTask getCommandTask(RequestHandler requestHandler, WorkFlowManager workflowManager) {
        return new CommandTask(requestHandler, workflowManager);
    }
}
