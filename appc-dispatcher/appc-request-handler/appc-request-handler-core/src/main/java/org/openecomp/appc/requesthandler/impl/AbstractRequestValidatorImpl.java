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

package org.openecomp.appc.requesthandler.impl;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.openecomp.appc.requesthandler.constant.Constants;
import org.openecomp.appc.configuration.Configuration;
import org.openecomp.appc.configuration.ConfigurationFactory;
import org.openecomp.appc.domainmodel.lcm.CommonHeader;
import org.openecomp.appc.domainmodel.lcm.RequestContext;
import org.openecomp.appc.domainmodel.lcm.RuntimeContext;
import org.openecomp.appc.domainmodel.lcm.VNFContext;
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
import org.openecomp.appc.requesthandler.helper.RequestRegistry;
import org.openecomp.appc.requesthandler.helper.RequestValidator;
import org.openecomp.appc.workflow.WorkFlowManager;
import org.openecomp.appc.workflow.objects.WorkflowExistsOutput;
import org.openecomp.appc.workflow.objects.WorkflowRequest;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;
import org.onap.ccsdk.sli.adaptors.aai.AAIService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;

public abstract class AbstractRequestValidatorImpl implements RequestValidator {

    protected final EELFLogger logger = EELFManager.getInstance().getLogger(RequestValidatorImpl.class);
    private final Configuration configuration = ConfigurationFactory.getConfiguration();
    protected LifecycleManager lifecyclemanager;
    protected LCMStateManager lcmStateManager;
    private AAIService aaiService;
    private WorkFlowManager workflowManager;
    private RequestRegistry requestRegistry = new RequestRegistry();

    protected static Calendar DateToCalendar(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    public void setWorkflowManager(WorkFlowManager workflowManager) {
        this.workflowManager = workflowManager;
    }

    public void setLcmStateManager(LCMStateManager lcmStateManager) {
        this.lcmStateManager = lcmStateManager;
    }

    public void setRequestRegistry(RequestRegistry requestRegistry) {
        this.requestRegistry = requestRegistry;
    }

    public abstract void validateRequest(RuntimeContext runtimeContext) throws VNFNotFoundException, RequestExpiredException, UnstableVNFException, InvalidInputException, DuplicateRequestException, NoTransitionDefinedException, LifecycleException, WorkflowNotFoundException, DGWorkflowNotFoundException, MissingVNFDataInAAIException, LCMOperationsDisabledException;

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

    protected void getAAIservice() {
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

    protected VNFContext queryAAI(String vnfId) throws VNFNotFoundException, MissingVNFDataInAAIException {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx = getVnfdata(vnfId, "vnf", ctx);

        VNFContext vnfContext = new VNFContext();
        populateVnfContext(vnfContext, ctx);

        return vnfContext;
    }

    protected void queryWFM(VNFContext vnfContext, RequestContext requestContext) throws WorkflowNotFoundException,DGWorkflowNotFoundException {

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

    protected void checkForDuplicateRequest(CommonHeader header) throws DuplicateRequestException {
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

    protected Integer readTTL(CommonHeader header) {
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

    protected void validateInput(RequestContext requestContext)
            throws RequestExpiredException, InvalidInputException, DuplicateRequestException {
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

        Calendar inputTimeStamp = DateToCalendar(Date.from(commonHeader.getTimeStamp()));
        Calendar currentTime = Calendar.getInstance();

        // If input timestamp is of future, we reject the request
        if (inputTimeStamp.getTime().getTime() > currentTime.getTime().getTime()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Input Timestamp is of future = " + inputTimeStamp.getTime());
            }
            throw new InvalidInputException("Input Timestamp is of future = " + inputTimeStamp.getTime());
        }
        Integer ttl = readTTL(commonHeader);
        logger.debug("TTL value set to (seconds) : " + ttl);
        inputTimeStamp.add(Calendar.SECOND, ttl);
        if (currentTime.getTime().getTime() >= inputTimeStamp.getTime().getTime()) {

            LoggingUtils.logErrorMessage(
                    LoggingConstants.TargetNames.REQUEST_VALIDATOR,
                    "TTL Expired: Current time - " + currentTime.getTime().getTime() + " Request time: " + inputTimeStamp.getTime().getTime() + " with TTL value: " + ttl,
                    this.getClass().getCanonicalName());

            throw new RequestExpiredException("TTL Expired");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Validation of the request is successful");
        }
    }
}
