/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.requesthandler.impl;

import com.att.eelf.i18n.EELFResourceManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jline.internal.Log;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.json.JSONObject;
import org.onap.appc.domainmodel.lcm.Flags;
import org.onap.appc.domainmodel.lcm.RequestContext;
import org.onap.appc.domainmodel.lcm.RuntimeContext;
import org.onap.appc.domainmodel.lcm.TransactionRecord;
import org.onap.appc.domainmodel.lcm.VNFContext;
import org.onap.appc.domainmodel.lcm.VNFOperation;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.executor.objects.LCMCommandStatus;
import org.onap.appc.executor.objects.Params;
import org.onap.appc.i18n.Msg;
import org.onap.appc.lockmanager.api.LockException;
import org.onap.appc.lockmanager.api.LockManager;
import org.onap.appc.logging.LoggingConstants;
import org.onap.appc.logging.LoggingUtils;
import org.onap.appc.requesthandler.exceptions.DGWorkflowNotFoundException;
import org.onap.appc.requesthandler.exceptions.LCMOperationsDisabledException;
import org.onap.appc.requesthandler.exceptions.MissingVNFDataInAAIException;
import org.onap.appc.requesthandler.exceptions.RequestValidationException;
import org.onap.appc.requesthandler.exceptions.VNFNotFoundException;
import org.onap.appc.requesthandler.exceptions.WorkflowNotFoundException;
import org.onap.appc.requesthandler.model.ActionIdentifierModel;
import org.onap.appc.requesthandler.model.Input;
import org.onap.appc.requesthandler.model.Request;
import org.onap.appc.requesthandler.model.RequestData;
import org.onap.appc.requesthandler.model.RequestModel;
import org.onap.appc.requesthandler.model.ScopeOverlapModel;
import org.onap.appc.rest.client.RestClientInvoker;
import org.onap.appc.validationpolicy.RequestValidationPolicy;
import org.onap.appc.validationpolicy.executors.RuleExecutor;
import org.onap.appc.validationpolicy.objects.RuleResult;
import org.onap.appc.workflow.WorkFlowManager;
import org.onap.appc.workflow.objects.WorkflowExistsOutput;
import org.onap.appc.workflow.objects.WorkflowRequest;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;
import org.onap.ccsdk.sli.adaptors.aai.AAIService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class RequestValidatorImpl extends AbstractRequestValidatorImpl {

    private WorkFlowManager workflowManager;
    private LockManager lockManager;
    private AAIService aaiService;
    private RequestValidationPolicy requestValidationPolicy;
    private RestClientInvoker client;
    private String path;
    private int transactionWindowInterval=0;

    static final String SCOPE_OVERLAP_ENDPOINT = "appc.LCM.scopeOverlap.endpoint";
    static final String ODL_USER = "appc.LCM.provider.user";
    static final String ODL_PASS = "appc.LCM.provider.pass";
    static final String TRANSACTION_WINDOW_HOURS = "appc.inProgressTransactionWindow.hours";

    public void initialize() throws APPCException {
        logger.info("Initializing RequestValidatorImpl.");
        String endpoint = null;
        String user = null;
        String pass =null;
        String transactionWindow = null;

        Properties properties = configuration.getProperties();
        if (properties != null) {
            endpoint = properties.getProperty(SCOPE_OVERLAP_ENDPOINT);
            user = properties.getProperty(ODL_USER);
            pass = properties.getProperty(ODL_PASS);
            transactionWindow = properties.getProperty(TRANSACTION_WINDOW_HOURS);
        }
        if (endpoint == null) {
            String message = "End point is not defined for scope over lap service in appc.properties.";
            logger.error(message);
            // TODO throw following exception (and remove the "return") when
            // entry in appc.properties file is made for scope overlap service
            // endpoint
            // and remove @Ignore in unit tests:
            // testInitializeWithNullConfigProps,
            // testInitializeWithoutEndpointProp
            // throw new APPCException(message);
            return;
        }

        if (StringUtils.isNotBlank(transactionWindow)) {
            logger.info("RequestValidatorImpl::TransactionWindow defined !!!");
            try {
                transactionWindowInterval = Integer.parseInt(transactionWindow);
            }
            catch (NumberFormatException e) {
                String message = "RequestValidatorImpl:::Error parsing transaction window interval!";
                logger.error(message, e);
                throw new APPCException(message);
            }
        }

        try {
            URL url = new URL(endpoint);
            client = new RestClientInvoker(url);
            client.setAuthentication(user, pass);
            path = url.getPath();

        } catch (MalformedURLException e) {
            String message = "Invalid endpoint " + endpoint;
            logger.error(message, e);
            // TODO throw following exception when entry in appc.properties file
            // is made for scope overlap service endpoint
            // and remove @Ignore in unit test:
            // testInitializeWithMalFormatEndpoint
            // throw new APPCException(message);
        }
    }

    private void getAAIservice() {
        BundleContext bctx = FrameworkUtil.getBundle(AAIService.class).getBundleContext();
        // Get AAIadapter reference
        ServiceReference sref = bctx.getServiceReference(AAIService.class.getName());
        if (sref != null) {
            logger.info("AAIService from bundlecontext");
            aaiService = (AAIService) bctx.getService(sref);

        } else {
            logger.error("Cannot find service reference for org.onap.ccsdk.sli.adaptors.aai.AAIService");

        }
    }

    public void setLockManager(LockManager lockManager) {
        this.lockManager = lockManager;
    }

    public void setClient(RestClientInvoker client) {
        this.client = client;
    }

    public void setWorkflowManager(WorkFlowManager workflowManager) {
        this.workflowManager = workflowManager;
    }

    public void setRequestValidationPolicy(RequestValidationPolicy requestValidationPolicy) {
        this.requestValidationPolicy = requestValidationPolicy;
    }

    public void validateRequest(RuntimeContext runtimeContext) throws Exception {
        if (logger.isTraceEnabled()) {
            logger.trace(
                    "Entering to validateRequest with RequestHandlerInput = " + ObjectUtils.toString(runtimeContext));
        }
        if (!lcmStateManager.isLCMOperationEnabled()) {
            LoggingUtils.logErrorMessage(LoggingConstants.TargetNames.REQUEST_VALIDATOR,
                    EELFResourceManager.format(Msg.LCM_OPERATIONS_DISABLED), this.getClass().getCanonicalName());
            throw new LCMOperationsDisabledException("APPC LCM operations have been administratively disabled");
        }

        getAAIservice();
        validateInput(runtimeContext);
        String vnfId = runtimeContext.getRequestContext().getActionIdentifiers().getVnfId();
        VNFContext vnfContext = queryAAI(vnfId);
        runtimeContext.setVnfContext(vnfContext);
        runtimeContext.getTransactionRecord().setTargetType(vnfContext.getType());

        VNFOperation operation = runtimeContext.getRequestContext().getAction();
        if (operation.isBuiltIn()) {
            return;
        }

        validateVNFLock(runtimeContext);
        checkWorkflowExists(vnfContext, runtimeContext.getRequestContext());

        if (runtimeContext.getRequestContext().getCommonHeader().getFlags().isForce()) {
            return;
        }

        List<TransactionRecord> inProgressTransactionsAll = transactionRecorder
                .getInProgressRequests(runtimeContext.getTransactionRecord(),0);
        List<TransactionRecord> inProgressTransactions = transactionRecorder
                .getInProgressRequests(runtimeContext.getTransactionRecord(),transactionWindowInterval);

        long inProgressTransactionsAllCount = inProgressTransactionsAll.size();
        long inProgressTransactionsRelevant = inProgressTransactions.size();
        logger.debug("In progress requests " + inProgressTransactions.toString());

        if ( inProgressTransactions.isEmpty()) //No need to check for scope overlap
            return;

        logInProgressTransactions(inProgressTransactions,inProgressTransactionsAllCount,
            inProgressTransactionsRelevant );

        Long exclusiveRequestCount = inProgressTransactions.stream()
                .filter(record -> record.getMode().equals(Flags.Mode.EXCLUSIVE.name())).count();
        if (exclusiveRequestCount > 0) {
            String message = "Request rejected - Existing request in progress with exclusive mode for VNF: " + vnfId;
            throw new RequestValidationException(message, LCMCommandStatus.EXLCUSIVE_REQUEST_IN_PROGRESS,
                    (new Params()).addParam("errorMsg", message));
        }

        Boolean scopeOverLap = checkScopeOverLap(runtimeContext.getRequestContext(), inProgressTransactions);
        logger.debug("Scope overlap " + scopeOverLap);
        if (scopeOverLap) {
            List<VNFOperation> inProgressActions = inProgressTransactions.stream().map(TransactionRecord::getOperation)
                    .collect(Collectors.toList());

            RuleExecutor ruleExecutor = requestValidationPolicy.getInProgressRuleExecutor();
            RuleResult result = ruleExecutor.executeRule(operation.name(), inProgressActions);
            logger.debug("Policy validation result " + result);
            if (RuleResult.REJECT == result) {
                String message = "Request rejected as per the request validation policy";
                throw new RequestValidationException(message, LCMCommandStatus.POLICY_VALIDATION_FAILURE,
                        (new Params()).addParam("errorMsg", message));
            }
        }
    }

    private void validateVNFLock(RuntimeContext runtimeContext) throws LockException {
        String vnfId = runtimeContext.getRequestContext().getActionIdentifiers().getVnfId();
        String lockOwner = lockManager.getLockOwner(vnfId);
        logger.debug("Current lock owner is " + lockOwner + " for vnf " + vnfId);
        if (lockOwner != null
                && !lockOwner.equals(runtimeContext.getRequestContext().getCommonHeader().getRequestId())) {
            String message = new StringBuilder("VNF : ").append(vnfId).append(" is locked by request id :")
                    .append(lockOwner).toString();
            throw new LockException(message);
        }
    }

    /*
     * Do not remove this method, this is actual method for invoking scope
     * overlap service When the service becomes available, its dummy
     * implementation should be removed and this implementation should be used.
     */
    private Boolean checkScopeOverLap(RequestContext requestContext, List<TransactionRecord> inProgressTransactions)
            throws APPCException {
        Boolean scopeOverlap = null;
        try {
            JsonNode inputJSON = convertToJsonInput(requestContext, inProgressTransactions);
            logger.debug("Input to scope overlap service " + inputJSON.toString());
            HttpResponse response = client.doPost(path, inputJSON.toString());
            int httpCode = response.getStatusLine().getStatusCode();
            if (httpCode < 200 || httpCode >= 300) {
                logger.debug("http error code " + httpCode);
                throw new APPCException("Exception occurred on invoking check scope overlap api");
            }
            String respBody = IOUtils.toString(response.getEntity().getContent());
            logger.debug("response body " + respBody);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode outputJSON = mapper.readTree(respBody);
            scopeOverlap = readScopeOverlap(outputJSON);
        } catch (IOException e) {
            String message = "Error accessing check scope overlap service";
            logger.error(message, e);
            throw new APPCException(message);
        }
        return scopeOverlap;
    }

    private Boolean readScopeOverlap(JsonNode outputJSON) throws APPCException {
        logger.debug("Response JSON " + outputJSON.toString());
        String message = "Error reading response JSON from scope overlap service ";
        JsonNode outputNode = outputJSON.get("output");
        JsonNode statusNode = outputNode.get("status");
        if (statusNode == null) {
            throw new APPCException(message);
        }

        if (null == statusNode.get("message"))
            throw new APPCException(message + "Status message is null.");
        String responseStatusMessage = statusNode.get("message").textValue();

        if (null == statusNode.get("code"))
            throw new APPCException(message + "Status code is null.");
        String code = statusNode.get("code").textValue();

        JsonNode responseInfoNode = outputNode.get("response-info");
        JsonNode blockNode = responseInfoNode.get("block");
        String requestOverlapValue = null;

        if (null != blockNode)
            requestOverlapValue = blockNode.textValue();

        logger.debug("Response JSON " + requestOverlapValue);

        if (code.equals("400")) {
            if(null==requestOverlapValue)
                throw new APPCException("Response code is 400 but requestOverlapValue is null ");
            if (requestOverlapValue.contains("true")) {
                return Boolean.TRUE;
            } else if (requestOverlapValue.contains("false")) {
                return Boolean.FALSE;
            } else {
                throw new APPCException(
                        message + "requestOverlap value is other than True and False, it is " + requestOverlapValue);
            }
        } else if (code.equals("401")) {
            throw new APPCException(message + responseStatusMessage);
        } else {
            throw new APPCException(message + "Status code is neither 400 nor 401, it is " + code);
        }

    }

    private JsonNode convertToJsonInput(RequestContext requestContext, List<TransactionRecord> inProgressTransactions) {
        ObjectMapper objectMapper = new ObjectMapper();
        ScopeOverlapModel scopeOverlapModel = getScopeOverlapModel(requestContext, inProgressTransactions);
        // Added for change in interface for action level

        JsonNode jsonObject = objectMapper.valueToTree(scopeOverlapModel);

        return jsonObject;
    }

    public ScopeOverlapModel getScopeOverlapModel(RequestContext requestContext,
            List<TransactionRecord> inProgressTransactions) {
        ScopeOverlapModel scopeOverlapModel = new ScopeOverlapModel();
        RequestData requestData = new RequestData();

        List<RequestModel> inProgressRequests = new ArrayList<>();
        RequestModel requestModel = new RequestModel();
        ActionIdentifierModel actionIdentifierModel = extractActionIdentifierModel(requestContext);
        requestModel.setAction(requestContext.getAction().toString());
        requestModel.setActionIdentifier(actionIdentifierModel);

        if (requestModel.getActionIdentifier().getVnfId() != null) {
            requestData.setVnfID(requestModel.getActionIdentifier().getVnfId());
        }

        if (requestModel.getActionIdentifier().getVnfcName() != null
                || requestModel.getActionIdentifier().getVfModuleId() != null
                || requestModel.getActionIdentifier().getVserverId() != null) {

            requestModel.getActionIdentifier().setVnfId(null);
        }

        requestData.setCurrentRequest(requestModel);

        for (TransactionRecord record : inProgressTransactions) {
            RequestModel request = new RequestModel();
            ActionIdentifierModel actionIdentifier = new ActionIdentifierModel();

            actionIdentifier.setServiceInstanceId(record.getServiceInstanceId());
            actionIdentifier.setVnfId(record.getTargetId());
            actionIdentifier.setVnfcName(record.getVnfcName());
            actionIdentifier.setVfModuleId(record.getVfModuleId());
            actionIdentifier.setVserverId(record.getVserverId());

            request.setAction(record.getOperation().name());
            request.setActionIdentifier(actionIdentifier);
            if (request.getActionIdentifier().getVnfcName() != null
                        || request.getActionIdentifier().getVfModuleId() != null
                        || request.getActionIdentifier().getVserverId() != null) {

                    request.getActionIdentifier().setVnfId(null);
            }
            inProgressRequests.add(request);
        }

        requestData.setInProgressRequests(inProgressRequests);

        Request request = new Request();

        Date date = new Date();
        request.setRequestID("RequestId-ScopeOverlap " + date.toString());
        request.setAction("isScopeOverlap");
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.valueToTree(requestData);
        request.setRequestData(json.toString());
        Input input = new Input();
        input.setRequest(request);
        scopeOverlapModel.setInput(input);

        return scopeOverlapModel;
    }

    private ActionIdentifierModel extractActionIdentifierModel(RequestContext requestContext) {
        ActionIdentifierModel actionIdentifierModel = new ActionIdentifierModel();
        actionIdentifierModel.setServiceInstanceId(requestContext.getActionIdentifiers().getServiceInstanceId());
        actionIdentifierModel.setVnfId(requestContext.getActionIdentifiers().getVnfId());
        actionIdentifierModel.setVnfcName(requestContext.getActionIdentifiers().getVnfcName());
        actionIdentifierModel.setVfModuleId(requestContext.getActionIdentifiers().getVfModuleId());
        actionIdentifierModel.setVserverId(requestContext.getActionIdentifiers().getVserverId());
        return actionIdentifierModel;
    }

    private VNFContext queryAAI(String vnfId) throws VNFNotFoundException, MissingVNFDataInAAIException {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx = getVnfdata(vnfId, "vnf", ctx);

        VNFContext vnfContext = new VNFContext();
        populateVnfContext(vnfContext, ctx);

        return vnfContext;
    }

    private SvcLogicContext getVnfdata(String vnf_id, String prefix, SvcLogicContext ctx) throws VNFNotFoundException {
        if (logger.isTraceEnabled()) {
            logger.trace("Entering to getVnfdata with vnfid = " + ObjectUtils.toString(vnf_id) + ", prefix = "
                    + ObjectUtils.toString(prefix) + ", SvcLogicContext" + ObjectUtils.toString(ctx));
        }
        String key = "vnf-id = '" + vnf_id + "'";
        logger.debug("inside getVnfdata=== " + key);
        try {
            Date beginTimestamp = new Date();
            SvcLogicResource.QueryStatus response = aaiService.query("generic-vnf", false, null, key, prefix, null,
                    ctx);
            Date endTimestamp = new Date();
            String status = SvcLogicResource.QueryStatus.SUCCESS.equals(response)
                    ? LoggingConstants.StatusCodes.COMPLETE : LoggingConstants.StatusCodes.ERROR;
            LoggingUtils.logMetricsMessage(beginTimestamp.toInstant(), endTimestamp.toInstant(),
                    LoggingConstants.TargetNames.AAI, LoggingConstants.TargetServiceNames.AAIServiceNames.QUERY, status,
                    "", response.name(), this.getClass().getCanonicalName());
            if (SvcLogicResource.QueryStatus.NOT_FOUND.equals(response)) {
                throw new VNFNotFoundException("VNF not found for vnf_id = " + vnf_id, vnf_id);
            } else if (SvcLogicResource.QueryStatus.FAILURE.equals(response)) {
                throw new RuntimeException("Error Querying AAI with vnfID = " + vnf_id);
            }
            logger.info("AAIResponse: " + response.toString());
        } catch (SvcLogicException e) {

            LoggingUtils.logErrorMessage(LoggingConstants.TargetServiceNames.AAIServiceNames.GET_VNF_DATA,
                    "Error in getVnfdata" + e, this.getClass().getCanonicalName());

            throw new RuntimeException(e);
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Exiting from getVnfdata with (SvcLogicContext = " + ObjectUtils.toString(ctx) + ")");
        }
        return ctx;
    }

    private void populateVnfContext(VNFContext vnfContext, SvcLogicContext ctx) throws MissingVNFDataInAAIException {
        String vnfType = ctx.getAttribute("vnf.vnf-type");
        if (StringUtils.isEmpty(vnfType)) {
            throw new MissingVNFDataInAAIException("vnf-type", ctx.getAttribute("vnf.vnf-id"));
        }
        vnfContext.setType(vnfType);
        vnfContext.setId(ctx.getAttribute("vnf.vnf-id"));
    }

    private void checkWorkflowExists(VNFContext vnfContext, RequestContext requestContext)
            throws WorkflowNotFoundException, DGWorkflowNotFoundException {
        WorkflowExistsOutput workflowExistsOutput = workflowManager
                .workflowExists(getWorkflowQueryParams(vnfContext, requestContext));
        if (!workflowExistsOutput.isMappingExist()) {
            if (logger.isDebugEnabled()) {
                logger.debug("WorkflowManager : Workflow mapping not found for vnfType = " + vnfContext.getType()
                        + ", version = " + vnfContext.getVersion() + ", command = "
                        + requestContext.getAction().name());
            }
            LoggingUtils.logErrorMessage(LoggingConstants.TargetNames.WORKFLOW_MANAGER, EELFResourceManager
                    .format(Msg.APPC_WORKFLOW_NOT_FOUND, vnfContext.getType(), requestContext.getAction().name()),
                    this.getClass().getCanonicalName());
            throw new WorkflowNotFoundException(
                    "Workflow mapping not found for vnfType = " + vnfContext.getType() + ", command = "
                            + requestContext.getAction().name(),
                    vnfContext.getType(), requestContext.getAction().name());
        }
        if (!workflowExistsOutput.isDgExist()) {
            if (logger.isDebugEnabled()) {
                logger.debug("WorkflowManager : DG Workflow not found for vnfType = " + vnfContext.getType()
                        + ", version = " + vnfContext.getVersion() + ", command = " + requestContext.getAction().name()
                        + " " + workflowExistsOutput);
            }
            LoggingUtils.logErrorMessage(LoggingConstants.TargetNames.WORKFLOW_MANAGER, EELFResourceManager
                    .format(Msg.APPC_WORKFLOW_NOT_FOUND, vnfContext.getType(), requestContext.getAction().name()),
                    this.getClass().getCanonicalName());
            throw new DGWorkflowNotFoundException(
                    "Workflow not found for vnfType = " + vnfContext.getType() + ", command = "
                            + requestContext.getAction().name(),
                    workflowExistsOutput.getWorkflowModule(), workflowExistsOutput.getWorkflowName(),
                    workflowExistsOutput.getWorkflowVersion(), vnfContext.getType(), requestContext.getAction().name());
        }
    }

    private WorkflowRequest getWorkflowQueryParams(VNFContext vnfContext, RequestContext requestContext) {
        WorkflowRequest workflowRequest = new WorkflowRequest();
        workflowRequest.setVnfContext(vnfContext);
        workflowRequest.setRequestContext(requestContext);
        if (logger.isTraceEnabled()) {
            logger.trace("Exiting from getWorkflowQueryParams with (WorkflowRequest = "
                    + ObjectUtils.toString(workflowRequest) + ")");
        }
        return workflowRequest;
    }

    public String logInProgressTransactions(List<TransactionRecord> inProgressTransactions,
            long inProgressTransactionsAllCount, long inProgressTransactionsRelevant) {
            if (inProgressTransactionsAllCount > inProgressTransactionsRelevant) {
                logger.info("Found Stale Transactions! Ignoring Stale Transactions for target, only considering "
                    + "transactions within the last " + transactionWindowInterval + " hours as transactions in-progress");
            }
            String logMsg="";
            for (TransactionRecord tr: inProgressTransactions) {
                logMsg = ("In Progress transaction for Target ID - "+ tr.getTargetId()
                        + " in state " + tr.getRequestState()
                        + " with Start time " + tr.getStartTime().toString()
                        + " for more than configurable time period " + transactionWindowInterval
                        + " hours [transaction details - Request ID - " + tr.getTransactionId()
                        + ", Service Instance Id -" + tr.getServiceInstanceId()
                        + ", Vserver_id - " + tr.getVserverId()
                        + ", VNFC_name - "+ tr.getVnfcName()
                        + ", VF module Id - " + tr.getVfModuleId()
                        + " Start time " + tr.getStartTime().toString()
                        + "]" );
            }
            return logMsg;

    }
}
