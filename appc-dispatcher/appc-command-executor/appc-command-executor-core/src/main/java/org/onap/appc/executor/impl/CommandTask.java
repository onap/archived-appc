/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

import com.att.eelf.configuration.Configuration;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import java.net.InetAddress;
import java.util.UUID;
import org.onap.appc.domainmodel.lcm.Status;
import org.onap.appc.domainmodel.lcm.VNFOperation;
import org.onap.appc.executor.impl.objects.CommandRequest;
import org.onap.appc.logging.LoggingConstants;
import org.onap.appc.requesthandler.RequestHandler;
import org.onap.appc.domainmodel.lcm.RuntimeContext;
import org.onap.appc.workflow.WorkFlowManager;
import org.onap.appc.workflow.objects.WorkflowRequest;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;
import org.onap.ccsdk.sli.adaptors.aai.AAIRequest;
import org.onap.ccsdk.sli.adaptors.aai.AAIService;
import org.onap.ccsdk.sli.adaptors.aai.AAIServiceException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.MDC;



/**
 * This abstract class is base class for all Command tasks. All command task must inherit this class.
 */

public class CommandTask implements Runnable {

    private RequestHandler requestHandler;
    private WorkFlowManager workflowManager;
    private CommandRequest commandRequest;
    private AAIService aaiService;


    public CommandRequest getCommandRequest() {
        return commandRequest;
    }

    public void setCommandRequest(CommandRequest commandRequest) {
        this.commandRequest = commandRequest;
    }

    private final EELFLogger logger = EELFManager.getInstance().getLogger(CommandTask.class);

    public void setWorkflowManager(WorkFlowManager workflowManager) {
        this.workflowManager = workflowManager;
    }

    public void setRequestHandler(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    public CommandTask(RequestHandler requestHandler,
                       WorkFlowManager workflowManager){
        this.requestHandler = requestHandler;
        this.workflowManager = workflowManager;
        getAAIservice();
    }

    private void getAAIservice() {
        BundleContext bctx = FrameworkUtil.getBundle(AAIService.class).getBundleContext();
        // Get AAIadapter reference
        ServiceReference sref = bctx.getServiceReference(AAIService.class.getName());
        if (sref != null) {
            logger.info("AAIService from bundlecontext");
            aaiService = (AAIService) bctx.getService(sref);
        } else {
            logger.info("AAIService error from bundlecontext");
            logger.warn("Cannot find service reference for org.onap.ccsdk.sli.adaptors.aai.AAIService");
        }
    }


    @Override
    public void run() {
        logger.debug("Starting execution of command :" + commandRequest);
        setInitialLogProperties(commandRequest);
        final RuntimeContext runtimeContext = commandRequest.getCommandExecutorInput().getRuntimeContext();

        WorkflowRequest workflowRequest = new WorkflowRequest();
        workflowRequest.setRequestContext(runtimeContext.getRequestContext());
        workflowRequest.setResponseContext(runtimeContext.getResponseContext());
        workflowRequest.setVnfContext(runtimeContext.getVnfContext());
        logger.debug("Executing workflow :" + workflowRequest);
        workflowManager.executeWorkflow(workflowRequest);
        logger.debug("Completed execution workflow with response:"+ commandRequest.getCommandExecutorInput().getRuntimeContext().getResponseContext());
        try {
            if (VNFOperation.Terminate ==  commandRequest.getCommandExecutorInput().getRuntimeContext().getRequestContext().getAction())
                updateAAIForTerminate(commandRequest);
        } catch (AAIServiceException e) {
            logger.error("Exception = " + e);
            // In case of any errors we are updating the response status code and message
            Status updatedStatus = new Status();
            updatedStatus.setCode(401);
            updatedStatus.setMessage("Failed to update VNF status in A&AI");
            commandRequest.getCommandExecutorInput().getRuntimeContext().getResponseContext().setStatus(updatedStatus);
            throw new RuntimeException(e);
        }finally {
            requestHandler.onRequestExecutionEnd(commandRequest.getCommandExecutorInput().getRuntimeContext());
            clearRequestLogProperties();
        }
    }

    private void updateAAIForTerminate(CommandRequest commandRequest) throws AAIServiceException {
        final int statusCode = commandRequest.getCommandExecutorInput().getRuntimeContext().getResponseContext().getStatus().getCode();

        logger.debug("Workflow Execution Status = "+ statusCode);
        if (statusCode == 100 || statusCode == 400) {
            SvcLogicContext ctx = new SvcLogicContext();
            ctx = getVnfdata(commandRequest.getCommandExecutorInput().getRuntimeContext().getVnfContext().getId(), "vnf", ctx);
            deleteGenericVnfData(commandRequest.getCommandExecutorInput().getRuntimeContext().getVnfContext().getId(),
                    ctx.getAttribute("vnf.resource-version"));
        }
    }

    private SvcLogicContext getVnfdata(String vnf_id, String prefix,SvcLogicContext ctx) {
        String key="generic-vnf.vnf-id = '" + vnf_id + "'" + " AND http-header.Real-Time = 'true'";
        logger.debug("inside getVnfdata=== " + key);
        try {
            SvcLogicResource.QueryStatus response = aaiService.query("generic-vnf", false, null, key,prefix, null, ctx);
            if(SvcLogicResource.QueryStatus.NOT_FOUND.equals(response)){
                logger.warn("VNF " + vnf_id + " not found while updating A&AI");
                throw new RuntimeException("VNF not found for vnf_id = " + vnf_id);
            }
            else if(SvcLogicResource.QueryStatus.FAILURE.equals(response)){
                throw new RuntimeException("Error Querying AAI with vnfID = " + vnf_id);
            }
            logger.info("AAIResponse: " + response.toString());
        } catch (SvcLogicException e) {
            logger.error("Error in getVnfdata " + e);
            throw new RuntimeException(e);
        }
        return ctx;
    }


    private void setInitialLogProperties(CommandRequest request) {
        String reqId = request.getCommandExecutorInput().getRuntimeContext().getRequestContext().getCommonHeader().getRequestId();

        try {
            MDC.put(Configuration.MDC_KEY_REQUEST_ID, UUID.fromString(reqId).toString());
            //reaching here without exception means existing RequestId is
            //valid UUID as per ECOMP logging standards
        } catch (Exception e) {
            String reqIdUUID = UUID.randomUUID().toString();
            MDC.put(Configuration.MDC_KEY_REQUEST_ID, reqIdUUID);
            logger.info("Replaced invalid requestID of " + reqId + ".  New value is " + reqIdUUID + ".");
        }
        if (request.getCommandExecutorInput().getRuntimeContext().getRequestContext().getActionIdentifiers().getServiceInstanceId() != null) {
            MDC.put(Configuration.MDC_SERVICE_INSTANCE_ID, 
                    request.getCommandExecutorInput().getRuntimeContext().getRequestContext().getActionIdentifiers().getServiceInstanceId());
            MDC.put(LoggingConstants.MDCKeys.PARTNER_NAME, 
                    request.getCommandExecutorInput().getRuntimeContext().getRequestContext().getCommonHeader().getOriginatorId());
            MDC.put(Configuration.MDC_SERVICE_NAME, 
                    request.getCommandExecutorInput().getRuntimeContext().getRequestContext().getAction().name());
        }
        try {
            MDC.put(Configuration.MDC_SERVER_FQDN, InetAddress.getLocalHost().getCanonicalHostName());
            MDC.put(Configuration.MDC_SERVER_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        MDC.put(Configuration.MDC_INSTANCE_UUID, ""); // make instanse_UUID generation once during APPC-instance deploying
    }

    private void clearRequestLogProperties()
    {
        try {
            MDC.remove(Configuration.MDC_KEY_REQUEST_ID);
            MDC.remove(Configuration.MDC_SERVICE_INSTANCE_ID);
            MDC.remove(Configuration.MDC_SERVICE_NAME);
            MDC.remove(LoggingConstants.MDCKeys.PARTNER_NAME);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public boolean deleteGenericVnfData(String vnf_id, String resourceVersion) throws AAIServiceException {
        boolean response = false;

        try {
            AAIRequest request = aaiService.getRequestFromResource("generic-vnf");
            request.addRequestProperty("generic-vnf.vnf-id", vnf_id);
            response = aaiService.delete(request, resourceVersion);
        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            logger.warn("deleteGenericVnfData", exc);
            throw new AAIServiceException(exc);
        }
        return response;
    }
}
