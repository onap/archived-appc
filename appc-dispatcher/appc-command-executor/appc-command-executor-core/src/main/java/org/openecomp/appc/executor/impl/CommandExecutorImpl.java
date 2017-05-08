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

/**
 *
 */
package org.openecomp.appc.executor.impl;


import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.ObjectUtils;
import org.openecomp.appc.domainmodel.lcm.RuntimeContext;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.executionqueue.ExecutionQueueService;
import org.openecomp.appc.executionqueue.impl.ExecutionQueueServiceFactory;
import org.openecomp.appc.executor.CommandExecutor;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;


public class CommandExecutorImpl implements CommandExecutor {

    private CommandTaskFactory executionTaskFactory ;
    private static final EELFLogger logger = EELFManager.getInstance().getLogger(CommandExecutorImpl.class);

    private ExecutionQueueService executionQueueService;
    private ExpiredMessageHandler expiredMessageHandler;

    public CommandExecutorImpl(){

    }

    public void setExecutionQueueService(ExecutionQueueService executionQueueService) {
        this.executionQueueService = executionQueueService;
    }

    public void setExpiredMessageHandler(ExpiredMessageHandler expiredMessageHandler) {
        this.expiredMessageHandler = expiredMessageHandler;
    }

    public void initialize() {
        logger.info("initialization started of CommandExecutorImpl");
        executionQueueService = ExecutionQueueServiceFactory.getExecutionQueueService();
        executionQueueService.registerMessageExpirationListener(expiredMessageHandler);
    }

    public void setExecutionTaskFactory(CommandTaskFactory executionTaskFactory) {
        this.executionTaskFactory = executionTaskFactory;
    }

    /**
     * Execute given command
     * Create command request and enqueue it for execution.
     * @param commandExecutorInput Contains CommandHeader,  command , target Id , payload and conf ID (optional)
     * @throws APPCException in case of error.
     */
    @Override
    public void executeCommand (RuntimeContext commandExecutorInput) throws APPCException{
        if (logger.isTraceEnabled()) {
            logger.trace("Entering to executeCommand with CommandExecutorInput = "+ ObjectUtils.toString(commandExecutorInput));
        }
        enqueRequest(commandExecutorInput);
        if (logger.isTraceEnabled()) {
            logger.trace("Exiting from executeCommand");
        }
    }

    @SuppressWarnings("unchecked")
    private void enqueRequest(RuntimeContext request) throws APPCException{
        if (logger.isTraceEnabled()) {
            logger.trace("Entering to enqueRequest with CommandRequest = "+ ObjectUtils.toString(request));
        }
        try {
            CommandTask commandTask = getMessageExecutor(request.getRequestContext().getAction().name());
            commandTask.setCommandRequest(request);
            long remainingTTL = getRemainingTTL(request);
            executionQueueService.putMessage(commandTask,remainingTTL, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            logger.error("Exception: "+e.getMessage());
            throw new APPCException(e);
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Exiting from enqueRequest");
        }
    }

    private long getRemainingTTL(RuntimeContext request) {
        Instant requestTimestamp = request.getRequestContext().getCommonHeader().getTimeStamp();
        int ttl = request.getRequestContext().getCommonHeader().getFlags().getTtl();
        return ChronoUnit.MILLIS.between(Instant.now(), requestTimestamp.plusSeconds(ttl));
    }

    private CommandTask getMessageExecutor(String action){
        if (logger.isTraceEnabled()) {
            logger.trace("Entering to getMessageExecutor with command = "+ action);
        }
        CommandTask executionTask = executionTaskFactory.getExecutionTask(action);
        if (logger.isTraceEnabled()) {
            logger.trace("Exiting from getMessageExecutor");
        }
        return executionTask;
    }


}
