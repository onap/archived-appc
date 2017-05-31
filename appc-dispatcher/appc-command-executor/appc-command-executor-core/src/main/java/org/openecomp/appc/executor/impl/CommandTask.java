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

import static com.att.eelf.configuration.Configuration.MDC_ALERT_SEVERITY;
import static com.att.eelf.configuration.Configuration.MDC_INSTANCE_UUID;
import static com.att.eelf.configuration.Configuration.MDC_KEY_REQUEST_ID;
import static com.att.eelf.configuration.Configuration.MDC_SERVER_FQDN;
import static com.att.eelf.configuration.Configuration.MDC_SERVER_IP_ADDRESS;
import static com.att.eelf.configuration.Configuration.MDC_SERVICE_INSTANCE_ID;
import static com.att.eelf.configuration.Configuration.MDC_SERVICE_NAME;

import java.net.InetAddress;

import org.openecomp.appc.domainmodel.lcm.RuntimeContext;
import org.openecomp.appc.domainmodel.lcm.Status;
import org.openecomp.appc.executor.objects.CommandResponse;
import org.openecomp.appc.executor.objects.LCMCommandStatus;
import org.openecomp.appc.executor.objects.Params;
import org.openecomp.appc.logging.LoggingConstants;
import org.openecomp.appc.requesthandler.RequestHandler;
import org.openecomp.appc.workflow.WorkFlowManager;
import org.openecomp.appc.workflow.objects.WorkflowRequest;
import org.openecomp.appc.workflow.objects.WorkflowResponse;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.slf4j.MDC;

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
        MDC.put(MDC_KEY_REQUEST_ID, runtimeContext.getRequestContext().getCommonHeader().getRequestId());
        if (runtimeContext.getRequestContext().getActionIdentifiers().getServiceInstanceId() != null)
            MDC.put(MDC_SERVICE_INSTANCE_ID, runtimeContext.getRequestContext().getActionIdentifiers().getServiceInstanceId());
        MDC.put(LoggingConstants.MDCKeys.PARTNER_NAME, runtimeContext.getRequestContext().getCommonHeader().getOriginatorId());
        MDC.put(MDC_SERVICE_NAME, runtimeContext.getRequestContext().getAction().name());
        try {
            MDC.put(MDC_SERVER_FQDN, InetAddress.getLocalHost().getCanonicalHostName()); //Don't change it to a .getLocalHostName() again please. It's wrong!
            MDC.put(MDC_SERVER_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress());
        }catch(Exception e){
            logger.debug(e.getMessage());
        }
        MDC.put(MDC_INSTANCE_UUID, ""); //TODO make instanse_UUID generation once during APPC-instanse deploying

        WorkflowRequest workflowRequest = new WorkflowRequest();
        workflowRequest.setRequestContext(runtimeContext.getRequestContext());
        workflowRequest.setResponseContext(runtimeContext.getResponseContext());
        workflowRequest.setVnfContext(runtimeContext.getVnfContext());

        WorkflowResponse response = workflowManager.executeWorkflow(workflowRequest);

        CommandResponse commandResponse = buildCommandResponse(response);
        this.onRequestCompletion(commandResponse);
    }

}
