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

package org.openecomp.appc.executor.impl;

import org.openecomp.appc.domainmodel.lcm.Status;
import org.openecomp.appc.executor.objects.CommandResponse;
import org.openecomp.appc.executor.objects.LCMCommandStatus;
import org.openecomp.appc.executor.objects.Params;
import org.openecomp.appc.requesthandler.RequestHandler;
import org.openecomp.appc.domainmodel.lcm.RuntimeContext;
import org.openecomp.appc.workflow.WorkFlowManager;
import org.openecomp.appc.workflow.objects.WorkflowRequest;
import org.openecomp.appc.workflow.objects.WorkflowResponse;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

/**
 * This abstract class is base class for all Command tasks. All command task must inherit this class.
 */

public abstract class CommandTask implements Runnable {

    protected final RequestHandler requestHandler;
    protected final WorkFlowManager workflowManager;
    protected final RuntimeContext commandRequest;

    protected CommandTask(RuntimeContext commandRequest, RequestHandler requestHandler,
            WorkFlowManager workflowManager) {
        super();
        this.commandRequest = commandRequest;
        this.requestHandler = requestHandler;
        this.workflowManager = workflowManager;
    }

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(CommandTask.class);

    public void onRequestCompletion(CommandResponse response, boolean isAAIUpdated) {
        logger.debug("Entry: onRequestCompletion()");
        requestHandler.onRequestExecutionEnd(commandRequest, isAAIUpdated);
    }

    public abstract void onRequestCompletion(CommandResponse response);

    protected CommandResponse buildCommandResponse(WorkflowResponse response) {

        return new CommandResponse(commandRequest);
    }


    public void execute() {
        final RuntimeContext runtimeContext = commandRequest;
        WorkflowRequest workflowRequest = new WorkflowRequest();
        workflowRequest.setRequestContext(runtimeContext.getRequestContext());
        workflowRequest.setResponseContext(runtimeContext.getResponseContext());
        workflowRequest.setVnfContext(runtimeContext.getVnfContext());

        WorkflowResponse response = workflowManager.executeWorkflow(workflowRequest);

        CommandResponse commandResponse = buildCommandResponse(response);
        this.onRequestCompletion(commandResponse);
    }

}
