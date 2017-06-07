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

package org.openecomp.appc.requesthandler.impl;

import java.time.Instant;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.openecomp.appc.common.constant.Constants;
import org.openecomp.appc.configuration.Configuration;
import org.openecomp.appc.configuration.ConfigurationFactory;
import org.openecomp.appc.domainmodel.lcm.CommonHeader;
import org.openecomp.appc.domainmodel.lcm.RequestContext;
import org.openecomp.appc.domainmodel.lcm.RuntimeContext;
import org.openecomp.appc.domainmodel.lcm.VNFContext;
import org.openecomp.appc.domainmodel.lcm.VNFOperation;
import org.openecomp.appc.executor.UnstableVNFException;
import org.openecomp.appc.executor.objects.UniqueRequestIdentifier;
import org.openecomp.appc.i18n.Msg;
import org.openecomp.appc.lifecyclemanager.LifecycleManager;
import org.openecomp.appc.lifecyclemanager.objects.LifecycleException;
import org.openecomp.appc.lifecyclemanager.objects.NoTransitionDefinedException;
import org.openecomp.appc.logging.LoggingConstants;
import org.openecomp.appc.logging.LoggingUtils;
import org.openecomp.appc.requesthandler.LCMStateManager;
import org.openecomp.appc.requesthandler.exceptions.*;
import org.openecomp.appc.requesthandler.exceptions.DGWorkflowNotFoundException;
import org.openecomp.appc.requesthandler.exceptions.DuplicateRequestException;
import org.openecomp.appc.requesthandler.exceptions.InvalidInputException;
import org.openecomp.appc.requesthandler.exceptions.RequestExpiredException;
import org.openecomp.appc.requesthandler.exceptions.VNFNotFoundException;
import org.openecomp.appc.requesthandler.exceptions.WorkflowNotFoundException;
import org.openecomp.appc.requesthandler.helper.RequestRegistry;
import org.openecomp.appc.requesthandler.helper.RequestValidator;
import org.openecomp.appc.workflow.WorkFlowManager;
import org.openecomp.appc.workflow.objects.WorkflowExistsOutput;
import org.openecomp.appc.workflow.objects.WorkflowRequest;
import org.openecomp.appc.workingstatemanager.WorkingStateManager;
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.openecomp.sdnc.sli.SvcLogicException;
import org.openecomp.sdnc.sli.SvcLogicResource;
import org.openecomp.sdnc.sli.aai.AAIService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;


public class RequestValidatorImpl implements RequestValidator {

    private AAIService aaiService;

    private LifecycleManager lifecyclemanager;

    private WorkFlowManager workflowManager;

    private WorkingStateManager workingStateManager;
    private LCMStateManager lcmStateManager;

    private final RequestRegistry requestRegistry = new RequestRegistry();

    private final Configuration configuration = ConfigurationFactory.getConfiguration();
    private final EELFLogger logger = EELFManager.getInstance().getLogger(RequestValidatorImpl.class);
    private final EELFLogger metricsLogger = EELFManager.getInstance().getMetricsLogger();

    public void setLifecyclemanager(LifecycleManager lifecyclemanager) {
        this.lifecyclemanager = lifecyclemanager;
    }

    public void setWorkflowManager(WorkFlowManager workflowManager) {
        this.workflowManager = workflowManager;
    }

    public void setWorkingStateManager(WorkingStateManager workingStateManager) {
        this.workingStateManager = workingStateManager;
    }

    public void setLcmStateManager(LCMStateManager lcmStateManager) {
        this.lcmStateManager = lcmStateManager;
    }

    public RequestValidatorImpl() {
    }

    @Override
    public void validateRequest(RuntimeContext runtimeContext) throws VNFNotFoundException, RequestExpiredException, UnstableVNFException, InvalidInputException, DuplicateRequestException, NoTransitionDefinedException, LifecycleException, WorkflowNotFoundException, DGWorkflowNotFoundException, MissingVNFDataInAAIException, LCMOperationsDisabledException {
        if (logger.isTraceEnabled()){
            logger.trace("Entering to validateRequest with RequestHandlerInput = "+ ObjectUtils.toString(runtimeContext));
        }
        if(!lcmStateManager.isLCMOperationEnabled()) {
            LoggingUtils.logErrorMessage(
                    LoggingConstants.TargetNames.REQUEST_VALIDATOR,
                    EELFResourceManager.format(Msg.LCM_OPERATIONS_DISABLED),
                    this.getClass().getCanonicalName());
            throw new LCMOperationsDisabledException("APPC LCM operations have been administratively disabled");
        }

        getAAIservice();
        validateInput(runtimeContext.getRequestContext());
        checkVNFWorkingState(runtimeContext);
        String vnfId = runtimeContext.getRequestContext().getActionIdentifiers().getVnfId();
        VNFContext vnfContext = queryAAI(vnfId);
        runtimeContext.setVnfContext(vnfContext);

        queryLCM(runtimeContext.getVnfContext().getStatus(), runtimeContext.getRequestContext().getAction());
        VNFOperation operation = runtimeContext.getRequestContext().getAction();
        if(!operation.isBuiltIn()) {
            // for built-in operations skip WF presence check
            queryWFM(vnfContext, runtimeContext.getRequestContext());
        }
    }

    private boolean isValidTTL(String ttl) {
        if (logger.isTraceEnabled()){
            logger.trace("Entering to isValidTTL where ttl = "+ ObjectUtils.toString(ttl));
        }
        if (ttl == null || ttl.length() == 0) {
            if (logger.isTraceEnabled()) {
                logger.trace("Exiting from isValidTT with (result = "+ ObjectUtils.toString(false)+")");
            }
            return false;
        }
        try {
            Integer i = Integer.parseInt(ttl);
            if (logger.isTraceEnabled()) {
                logger.trace("Exiting from isValidTTL with (result = "+ ObjectUtils.toString(i > 0)+")");
            }
            return (i > 0);
        } catch (NumberFormatException e) {
            if (logger.isTraceEnabled()) {
                logger.trace("Exiting from isValidTTL with (result = "+ ObjectUtils.toString(false)+")");
            }
            return false;
        }
    }

    private void validateInput(RequestContext requestContext) throws RequestExpiredException, InvalidInputException, DuplicateRequestException {
        if (logger.isTraceEnabled()){
            logger.trace("Entering to validateInput with RequestHandlerInput = "+ ObjectUtils.toString(requestContext));
        }
        if (requestContext.getActionIdentifiers().getVnfId() == null || requestContext.getAction() == null
                || requestContext.getActionIdentifiers().getVnfId().length() == 0 || requestContext.getAction().name().length() == 0 ||
                null == requestContext.getCommonHeader().getApiVer()) {
            if (logger.isDebugEnabled()) {
                logger.debug("vnfID = " + requestContext.getActionIdentifiers().getVnfId() + ", action = " + requestContext.getAction().name());
            }

            LoggingUtils.logErrorMessage(
                    LoggingConstants.TargetNames.REQUEST_VALIDATOR,
                    EELFResourceManager.format(Msg.APPC_INVALID_INPUT),
                    this.getClass().getCanonicalName());

            throw new InvalidInputException("vnfID or command is null");
        }
        CommonHeader commonHeader = requestContext.getCommonHeader();

        checkForDuplicateRequest(commonHeader);

        Instant inputTimeStamp = commonHeader.getTimeStamp();
        Instant currentTime = Instant.now();

        // If input timestamp is of future, we reject the request
        if (inputTimeStamp.isAfter(currentTime)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Input Timestamp is of future = " + inputTimeStamp);
            }
            throw new InvalidInputException("Input Timestamp is of future = " + inputTimeStamp);
        }
        int ttl = readTTL(commonHeader);
        logger.debug("TTL value set to (seconds) : " + ttl);
        Instant expirationTime = inputTimeStamp.plusSeconds(ttl);
        if (currentTime.isAfter(expirationTime)) {

            LoggingUtils.logErrorMessage(
                    LoggingConstants.TargetNames.REQUEST_VALIDATOR,
                    "TTL Expired: Current time - " + currentTime + " Request time: " + expirationTime + " with TTL value: " + ttl,
                    this.getClass().getCanonicalName());

            throw new RequestExpiredException("TTL Expired");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Validation of the request is successful");
        }
    }


    // TODO: Get reference once via Blueprint and get rid of this method
    private void getAAIservice() {
        BundleContext bctx = FrameworkUtil.getBundle(AAIService.class).getBundleContext();
        // Get AAIadapter reference
        ServiceReference sref = bctx.getServiceReference(AAIService.class.getName());
        if (sref != null) {
            logger.info("AAIService from bundlecontext");
            aaiService = (AAIService) bctx.getService(sref);

        } else {
            logger.info("AAIService error from bundlecontext");
            logger.warn("Cannot find service reference for org.openecomp.sdnc.sli.aai.AAIService");

        }
    }

    private VNFContext queryAAI(String vnfId) throws VNFNotFoundException, MissingVNFDataInAAIException {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx = getVnfdata(vnfId, "vnf", ctx);

        VNFContext vnfContext = new VNFContext();
        populateVnfContext(vnfContext, ctx);

        return vnfContext;
    }

    private String queryLCM(String orchestrationStatus, VNFOperation action) throws LifecycleException, NoTransitionDefinedException {
        if (logger.isTraceEnabled()) {
            logger.trace("Entering to queryLCM  with Orchestration Status = "+ ObjectUtils.toString(orchestrationStatus)+
                    ", command = "+ ObjectUtils.toString(action));
        }

        String nextState = lifecyclemanager.getNextState(null, orchestrationStatus, action.name());
        if (logger.isDebugEnabled()) {
            logger.trace("Exiting from queryLCM with (LCMResponse = "+ ObjectUtils.toString(nextState)+")");
        }
        return nextState;
    }

    private void queryWFM(VNFContext vnfContext, RequestContext requestContext) throws WorkflowNotFoundException,DGWorkflowNotFoundException {

        checkWorkflowExists(vnfContext, requestContext);
    }

    private void checkWorkflowExists(VNFContext vnfContext, RequestContext requestContext) throws WorkflowNotFoundException,DGWorkflowNotFoundException {

        WorkflowExistsOutput workflowExistsOutput = workflowManager.workflowExists(getWorkflowQueryParams(vnfContext, requestContext));
        if (!workflowExistsOutput.isMappingExist()) {
            if (logger.isDebugEnabled()) {
                logger.debug("WorkflowManager : Workflow not found for vnfType = " + vnfContext.getType() + ", version = " + vnfContext.getVersion() + ", command = " + requestContext.getAction().name());
            }

            LoggingUtils.logErrorMessage(
                    LoggingConstants.TargetNames.WORKFLOW_MANAGER,
                    EELFResourceManager.format(Msg.APPC_WORKFLOW_NOT_FOUND, vnfContext.getType(), requestContext.getAction().name()),
                    this.getClass().getCanonicalName());


            throw new WorkflowNotFoundException("Workflow not found for vnfType = " + vnfContext.getType() + ", command = " + requestContext.getAction().name(),vnfContext.getType(),requestContext.getAction().name());
        }
        if (!workflowExistsOutput.isDgExist()) {
            if (logger.isDebugEnabled()) {
                logger.debug("WorkflowManager : DG Workflow not found for vnfType = " + vnfContext.getType() + ", version = " + vnfContext.getVersion() + ", command = " + requestContext.getAction().name()+" "+workflowExistsOutput);
            }


            LoggingUtils.logErrorMessage(
                    LoggingConstants.TargetNames.WORKFLOW_MANAGER,
                    EELFResourceManager.format(Msg.APPC_WORKFLOW_NOT_FOUND, vnfContext.getType(), requestContext.getAction().name()),
                    this.getClass().getCanonicalName());


            throw new DGWorkflowNotFoundException("Workflow not found for vnfType = " + vnfContext.getType() + ", command = " + requestContext.getAction().name(),
                    workflowExistsOutput.getWorkflowModule(),workflowExistsOutput.getWorkflowName(),workflowExistsOutput.getWorkflowVersion());
        }
    }

    private void populateVnfContext(VNFContext vnfContext, SvcLogicContext ctx) throws MissingVNFDataInAAIException {
        String vnfType = ctx.getAttribute("vnf.vnf-type");
        String orchestrationStatus = ctx.getAttribute("vnf.orchestration-status");
        if(StringUtils.isEmpty(vnfType)){
            throw new MissingVNFDataInAAIException("vnf-type");
        }
        else if(StringUtils.isEmpty(orchestrationStatus)){
            throw new MissingVNFDataInAAIException("orchestration-status");
        }
        vnfContext.setType(vnfType);
        vnfContext.setStatus(orchestrationStatus);
        vnfContext.setId(ctx.getAttribute("vnf.vnf-id"));
        // TODO: Uncomment once A&AI supports VNF version
        //vnfContext.setVersion(ctx.getAttribute("vnf.vnf-version"));
        }

    private WorkflowRequest getWorkflowQueryParams(VNFContext vnfContext, RequestContext requestContext) {

        WorkflowRequest workflowRequest = new WorkflowRequest();
        workflowRequest.setVnfContext(vnfContext);
        workflowRequest.setRequestContext(requestContext);
        if (logger.isTraceEnabled()) {
            logger.trace("Exiting from etWorkflowQueryParams with (WorkflowRequest = "+ ObjectUtils.toString(workflowRequest)+")");
        }
        return workflowRequest;
    }


    private void checkForDuplicateRequest(CommonHeader header) throws DuplicateRequestException {
        if (logger.isTraceEnabled()) {
            logger.trace("Entering to checkForDuplicateRequest with RequestHeader = "+ ObjectUtils.toString(header));
        }

        UniqueRequestIdentifier requestIdentifier = new UniqueRequestIdentifier(header.getOriginatorId(), header.getRequestId(), header.getSubRequestId());
        boolean requestAccepted = requestRegistry.registerRequest(requestIdentifier);
        if (!requestAccepted) {
            if (logger.isDebugEnabled()) {
                logger.debug("Duplicate Request with " + requestIdentifier);
            }
            throw new DuplicateRequestException("Duplicate Request with " + requestIdentifier);
        }
    }

    private void checkVNFWorkingState(RuntimeContext runtimeContext) throws UnstableVNFException {

        if (logger.isTraceEnabled()) {
            logger.trace("Entering to checkVNFWorkingState with RequestHandlerInput = "+ ObjectUtils.toString(runtimeContext.getRequestContext()));
        }
        boolean forceFlag = runtimeContext.getRequestContext().getCommonHeader().getFlags() != null ? runtimeContext.getRequestContext().getCommonHeader().getFlags().isForce() : false;
        String vnfId = runtimeContext.getRequestContext().getActionIdentifiers().getVnfId();

            if (logger.isDebugEnabled()) {
                logger.debug("forceFlag = " + forceFlag);
            }
        boolean isVNFStable = workingStateManager.isVNFStable(vnfId);
            if (!isVNFStable && !forceFlag) {
                if (logger.isDebugEnabled()) {
                logger.debug("VNF is not stable for VNF ID = " + vnfId);
                }
            throw new UnstableVNFException("VNF is not stable for vnfID = " + vnfId);
            }

        }


    private int readTTL(CommonHeader header) {
        if (logger.isTraceEnabled()) {
            logger.trace("Entering to readTTL with RequestHandlerInput = "+ ObjectUtils.toString(header));
        }
        if (header.getFlags()== null || !isValidTTL(String.valueOf(header.getFlags().getTtl()))) {
            String defaultTTLStr = configuration.getProperty("org.openecomp.appc.workflow.default.ttl", String.valueOf(Constants.DEFAULT_TTL));
            return Integer.parseInt(defaultTTLStr);
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Exiting from readTTL with (TTL = "+  ObjectUtils.toString(header.getFlags().getTtl())+")");
        }
        return header.getFlags().getTtl();
    }


    private SvcLogicContext getVnfdata(String vnf_id, String prefix, SvcLogicContext ctx) throws VNFNotFoundException {
        if (logger.isTraceEnabled()) {
            logger.trace("Entering to getVnfdata with vnfid = "+ ObjectUtils.toString(vnf_id) + ", prefix = "+ ObjectUtils.toString(prefix)+ ", SvcLogicContext"+ ObjectUtils.toString(ctx));
        }

        String key = "vnf-id = '" + vnf_id + "'";
        logger.debug("inside getVnfdata=== " + key);
        try {
            Instant beginTimestamp = Instant.now();
            SvcLogicResource.QueryStatus response = aaiService.query("generic-vnf", false, null, key, prefix, null, ctx);
            Instant endTimestamp = Instant.now();
            String status = SvcLogicResource.QueryStatus.SUCCESS.equals(response) ? LoggingConstants.StatusCodes.COMPLETE : LoggingConstants.StatusCodes.ERROR;
            LoggingUtils.logMetricsMessage(
                    beginTimestamp,
                    endTimestamp,
                    LoggingConstants.TargetNames.AAI,
                    LoggingConstants.TargetServiceNames.AAIServiceNames.QUERY,
                    status,
                    "",
                    response.name(),
                    this.getClass().getCanonicalName());
            if (SvcLogicResource.QueryStatus.NOT_FOUND.equals(response)) {
                throw new VNFNotFoundException("VNF not found for vnf_id = " + vnf_id);
            } else if (SvcLogicResource.QueryStatus.FAILURE.equals(response)) {
                throw new RuntimeException("Error Querying AAI with vnfID = " + vnf_id);
            }
            logger.info("AAIResponse: " + response.toString());
        } catch (SvcLogicException e) {

            LoggingUtils.logErrorMessage(
                    LoggingConstants.TargetServiceNames.AAIServiceNames.GET_VNF_DATA,
                    "Error in getVnfdata" + e,
                    this.getClass().getCanonicalName());

            throw new RuntimeException(e);
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Exiting from getVnfdata with (SvcLogicContext = "+ ObjectUtils.toString(ctx)+")");
        }
        return ctx;
    }

}
