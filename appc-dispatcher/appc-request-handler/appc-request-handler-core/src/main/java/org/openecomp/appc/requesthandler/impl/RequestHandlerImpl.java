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

import org.apache.commons.lang.ObjectUtils;
import org.openecomp.appc.common.constant.Constants;
import org.openecomp.appc.configuration.Configuration;
import org.openecomp.appc.configuration.ConfigurationFactory;
import org.openecomp.appc.domainmodel.lcm.*;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.executor.CommandExecutor;
import org.openecomp.appc.executor.UnstableVNFException;
import org.openecomp.appc.executor.objects.LCMCommandStatus;
import org.openecomp.appc.executor.objects.Params;
import org.openecomp.appc.executor.objects.UniqueRequestIdentifier;
import org.openecomp.appc.i18n.Msg;
import org.openecomp.appc.lifecyclemanager.objects.LifecycleException;
import org.openecomp.appc.lifecyclemanager.objects.NoTransitionDefinedException;
import org.openecomp.appc.lockmanager.api.LockException;
import org.openecomp.appc.lockmanager.api.LockManager;
import org.openecomp.appc.logging.LoggingConstants;
import org.openecomp.appc.logging.LoggingUtils;
import org.openecomp.appc.messageadapter.MessageAdapter;
import org.openecomp.appc.messageadapter.impl.MessageAdapterImpl;
import org.openecomp.appc.metricservice.MetricRegistry;
import org.openecomp.appc.metricservice.MetricService;
import org.openecomp.appc.metricservice.metric.DispatchingFuntionMetric;
import org.openecomp.appc.metricservice.metric.Metric;
import org.openecomp.appc.metricservice.metric.MetricType;
import org.openecomp.appc.metricservice.policy.PublishingPolicy;
import org.openecomp.appc.metricservice.publisher.LogPublisher;
import org.openecomp.appc.requesthandler.RequestHandler;
import org.openecomp.appc.requesthandler.exceptions.*;
import org.openecomp.appc.requesthandler.helper.RequestRegistry;
import org.openecomp.appc.requesthandler.helper.RequestValidator;
import org.openecomp.appc.requesthandler.objects.RequestHandlerInput;
import org.openecomp.appc.requesthandler.objects.RequestHandlerOutput;
import org.openecomp.appc.transactionrecorder.TransactionRecorder;
import org.openecomp.appc.transactionrecorder.objects.TransactionRecord;
import org.openecomp.appc.workingstatemanager.WorkingStateManager;
import org.openecomp.appc.workingstatemanager.objects.VNFWorkingState;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.openecomp.sdnc.sli.SvcLogicException;
import org.openecomp.sdnc.sli.SvcLogicResource.QueryStatus;
import org.openecomp.sdnc.sli.aai.AAIService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.MDC;

import static com.att.eelf.configuration.Configuration.*;

import java.net.InetAddress;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * This class provides application logic for the Request/Response Handler Component.
 *
 */
public class RequestHandlerImpl implements RequestHandler {

    /**
     * APP-C VNF lock idle timeout in milliseconds. Applied only when locking VNF using northbound API "lock"
     */
    private static final String PROP_IDLE_TIMEOUT = "org.openecomp.appc.lock.idleTimeout";

    private CommandExecutor commandExecutor;

    private TransactionRecorder transactionRecorder;
    private MessageAdapter messageAdapter;
    private RequestValidator requestValidator;
    private static MetricRegistry metricRegistry;
    private boolean isMetricEnabled = false;
    private RequestRegistry requestRegistry;
    private LockManager lockManager;
    private WorkingStateManager workingStateManager;
    private AAIService aaiService;

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(RequestHandlerImpl.class);

    public void setAaiService(AAIService aaiService) {
        this.aaiService = aaiService;
    }

    public void setTransactionRecorder(TransactionRecorder transactionRecorder) {
        this.transactionRecorder = transactionRecorder;
    }

    public void setLockManager(LockManager lockManager) {
        this.lockManager = lockManager;
    }

    public void setRequestValidator(RequestValidator requestValidator) {
        this.requestValidator = requestValidator;
    }

    public void setMessageAdapter(MessageAdapter messageAdapter) {
        this.messageAdapter = messageAdapter;
    }

    private static final Configuration configuration = ConfigurationFactory.getConfiguration();

    public void setWorkingStateManager(WorkingStateManager workingStateManager) {
        this.workingStateManager = workingStateManager;
    }

    public RequestHandlerImpl() {
        requestRegistry = new RequestRegistry();
        messageAdapter = new MessageAdapterImpl();
        messageAdapter.init();
        Properties properties = configuration.getProperties();
        if (properties != null && properties.getProperty("metric.enabled") != null) {
            isMetricEnabled = Boolean.valueOf(properties.getProperty("metric.enabled"));
        }
        if (isMetricEnabled) {
            initMetric();
        }
    }

    public void setCommandExecutor(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }


    /**
     * It receives requests from the north-bound REST API (Communication) Layer and
     * performs following validations.
     * 1. VNF exists in A&AI for the given targetID (VnfID)
     * 2. For the current VNF  Orchestration Status, the command can be executed
     * 3. For the given VNF type and Operation, there exists work-flow definition in the APPC database
     * If any of the validation fails, it returns appropriate response
     *
     * @param input RequestHandlerInput object which contains request header and  other request parameters like command , target Id , payload etc.
     * @return response for request as enum with Return code and message.
     */
    @Override
    public RequestHandlerOutput handleRequest(RequestHandlerInput input) {
        if (logger.isTraceEnabled())
            logger.trace("Entering to handleRequest with RequestHandlerInput = " + ObjectUtils.toString(input) + ")");
        Params params = null;
        String vnfId = null, vnfType = null, errorMessage = null;
        Instant startTime = Instant.now();
        RequestHandlerOutput output = null;
        setInitialLogProperties(input.getRequestContext());

        RuntimeContext runtimeContext = new RuntimeContext();
        runtimeContext.setRequestContext(input.getRequestContext());
        runtimeContext.setTimeStart(startTime);
        runtimeContext.setRpcName(input.getRpcName());

        final ResponseContext responseContext = new ResponseContext();
        responseContext.setStatus(new Status(0, null));
        responseContext.setAdditionalContext(new HashMap<String, String>(4));
        responseContext.setCommonHeader(input.getRequestContext().getCommonHeader());
        runtimeContext.setResponseContext(responseContext);
        runtimeContext.getResponseContext().setStatus(new Status(0, null));

        vnfId = runtimeContext.getRequestContext().getActionIdentifiers().getVnfId();

        try {

            requestValidator.validateRequest(runtimeContext);

            handleRequest(runtimeContext);

            final int statusCode = runtimeContext.getResponseContext().getStatus().getCode();
            if (statusCode % 100 == 2 || statusCode % 100 == 3) {
                createTransactionRecord(runtimeContext);
            }
            output = new RequestHandlerOutput();
            output.setResponseContext(runtimeContext.getResponseContext());

        } catch (VNFNotFoundException e) {
            errorMessage = e.getMessage();
            String logMessage = EELFResourceManager.format(Msg.APPC_NO_RESOURCE_FOUND, vnfId);
            storeErrorMessageToLog(runtimeContext, LoggingConstants.TargetNames.AAI, "", logMessage);
            params = new Params().addParam("vnfId", vnfId);
            output = buildRequestHandlerOutput(LCMCommandStatus.VNF_NOT_FOUND, params);
        } catch (NoTransitionDefinedException e) {
            errorMessage = e.getMessage();
            String logMessage = EELFResourceManager.format(Msg.VF_UNDEFINED_STATE, input.getRequestContext().getCommonHeader().getOriginatorId(), input.getRequestContext().getAction().name());
            params = new Params().addParam("actionName", input.getRequestContext().getAction()).addParam("currentState", e.currentState);
            output = buildRequestHandlerOutput(LCMCommandStatus.NO_TRANSITION_DEFINE, params);
            storeErrorMessageToLog(runtimeContext,
                    LoggingConstants.TargetNames.APPC,
                    LoggingConstants.TargetNames.STATE_MACHINE,
                    logMessage);
        } catch (LifecycleException e) {
            errorMessage = e.getMessage();
            params = new Params().addParam("actionName", input.getRequestContext().getAction()).addParam("currentState", e.currentState);
            output = buildRequestHandlerOutput(LCMCommandStatus.INVALID_VNF_STATE, params);
        } catch (UnstableVNFException e) {
            errorMessage = e.getMessage();
            params = new Params().addParam("vnfId", vnfId);
            output = buildRequestHandlerOutput(LCMCommandStatus.UNSTABLE_VNF, params);
        } catch (WorkflowNotFoundException e) {
            errorMessage = e.getMessage();
            String vnfTypeVersion = e.vnfTypeVersion;
            params = new Params().addParam("actionName", input.getRequestContext().getAction()).addParam("vnfTypeVersion", vnfTypeVersion);
            output = buildRequestHandlerOutput(LCMCommandStatus.WORKFLOW_NOT_FOUND, params);
        } catch (DGWorkflowNotFoundException e) {
            errorMessage = e.getMessage();
            String logMessage = EELFResourceManager.format(Msg.APPC_WORKFLOW_NOT_FOUND, vnfType, input.getRequestContext().getAction().name());
            storeErrorMessageToLog(runtimeContext,
                    LoggingConstants.TargetNames.APPC,
                    LoggingConstants.TargetNames.WORKFLOW_MANAGER,
                    logMessage);
            params = new Params().addParam("actionName", input.getRequestContext().getAction().name())
                    .addParam("dgModule", e.workflowModule).addParam("dgName", e.workflowName).addParam("dgVersion", e.workflowVersion);
            output = buildRequestHandlerOutput(LCMCommandStatus.DG_WORKFLOW_NOT_FOUND, params);
        } catch (RequestExpiredException e) {
            errorMessage = e.getMessage();
            params = new Params().addParam("actionName", input.getRequestContext().getAction().name());
            output = buildRequestHandlerOutput(LCMCommandStatus.EXPIRED_REQUEST, params);
        } catch (InvalidInputException e) {
            errorMessage = e.getMessage() != null ? e.getMessage() : e.toString();
            params = new Params().addParam("errorMsg", errorMessage);
            output = buildRequestHandlerOutput(LCMCommandStatus.INVALID_INPUT_PARAMETER, params);
        } catch (DuplicateRequestException e) {
            errorMessage = e.getMessage();
            output = buildRequestHandlerOutput(LCMCommandStatus.DUPLICATE_REQUEST, null);
        } catch (MissingVNFDataInAAIException e) {
            params = new Params().addParam("attributeName",e.getMissingAttributeName())
                    .addParam("vnfId",vnfId);
            output = buildRequestHandlerOutput(LCMCommandStatus.MISSING_VNF_DATA_IN_AAI,params);
            errorMessage = output.getResponseContext().getStatus().getMessage();
        } catch (LCMOperationsDisabledException e) {
            errorMessage = e.getMessage();
            params = new Params().addParam("errorMsg", errorMessage);
            output = buildRequestHandlerOutput(LCMCommandStatus.REJECTED, params);
        } catch (Exception e) {
            storeErrorMessageToLog(runtimeContext, "", "", "Exception = " + e.getMessage());
            errorMessage = e.getMessage() != null ? e.getMessage() : e.toString();
            params = new Params().addParam("errorMsg", errorMessage);
            output = buildRequestHandlerOutput(LCMCommandStatus.UNEXPECTED_ERROR, params);
        } finally {
            try {
                if (logger.isDebugEnabled() && errorMessage != null)
                    logger.debug("error occurred in handleRequest " + errorMessage);
                logger.debug("output.getResponse().getResponseCode().equals(LCMCommandStatus.ACCEPTED.getResponseCode():  " + (output.getResponseContext().getStatus().getCode() == LCMCommandStatus.ACCEPTED.getResponseCode()));
                logger.debug("output.getResponse().getResponseCode().equals(LCMCommandStatus.SUCCESS.getResponseCode():  " + (output.getResponseContext().getStatus().getCode() == LCMCommandStatus.SUCCESS.getResponseCode()));

                runtimeContext.setResponseContext(output.getResponseContext());
                if ((null == output) || !(output.getResponseContext().getStatus().getCode() == LCMCommandStatus.ACCEPTED.getResponseCode())) {
                    if (isMetricEnabled) {
                        if((output.getResponseContext().getStatus().getCode() == LCMCommandStatus.SUCCESS.getResponseCode())) {
                            ((DispatchingFuntionMetric) metricRegistry.metric("DISPATCH_FUNCTION")).incrementAcceptedRequest();
                        }else {
                            ((DispatchingFuntionMetric) metricRegistry.metric("DISPATCH_FUNCTION")).incrementRejectedRequest();
                        }
                    }
                    removeRequestFromRegistry(input.getRequestContext().getCommonHeader());
                }
            } finally {
                storeAuditLogRecord(runtimeContext);
                storeMetricLogRecord(runtimeContext);
                clearRequestLogProperties();
            }
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Exiting from handleRequest with (RequestHandlerOutput = " + ObjectUtils.toString(output.getResponseContext()) + ")");
        }
        return output;
    }

    private void storeErrorMessageToLog(RuntimeContext runtimeContext, String targetEntity, String targetServiceName, String additionalMessage) {
        LoggingUtils.logErrorMessage(runtimeContext.getResponseContext().getStatus() != null ?
                        String.valueOf(runtimeContext.getResponseContext().getStatus().getCode()) : "",
                runtimeContext.getResponseContext().getStatus() != null ?
                        String.valueOf(runtimeContext.getResponseContext().getStatus().getMessage()) : "",
                targetEntity,
                targetServiceName,
                additionalMessage,
                this.getClass().getCanonicalName());
    }

    private void createTransactionRecord(RuntimeContext runtimeContext) {
        TransactionRecord transactionRecord = new TransactionRecord();
        transactionRecord.setTimeStamp(runtimeContext.getResponseContext().getCommonHeader().getTimeStamp());
        transactionRecord.setRequestID(runtimeContext.getResponseContext().getCommonHeader().getRequestId());
        transactionRecord.setStartTime(runtimeContext.getTimeStart());
        transactionRecord.setEndTime(Instant.now());
        transactionRecord.setTargetID(runtimeContext.getVnfContext().getId());
        transactionRecord.setTargetType(runtimeContext.getVnfContext().getType());
        transactionRecord.setOperation(runtimeContext.getRequestContext().getAction().name());
        transactionRecord.setResultCode(String.valueOf(runtimeContext.getResponseContext().getStatus().getCode()));
        transactionRecord.setDescription(runtimeContext.getResponseContext().getStatus().getMessage());
        transactionRecorder.store(transactionRecord);
    }

    private void handleRequest(RuntimeContext runtimeContext) {

        switch (runtimeContext.getRequestContext().getAction()) {
            case Lock:
                try {
                    lockWithTimeout(runtimeContext.getVnfContext().getId(), runtimeContext.getRequestContext().getCommonHeader().getRequestId());
                    fillStatus(runtimeContext,LCMCommandStatus.SUCCESS, null);
                } catch (LockException e) {
                    Params params = new Params().addParam("errorMsg", e.getMessage());
                    fillStatus(runtimeContext, LCMCommandStatus.LOCKING_FAILURE, params);
                    storeErrorMessageToLog(runtimeContext,
                            LoggingConstants.TargetNames.APPC,
                            LoggingConstants.TargetNames.LOCK_MANAGER,
                            EELFResourceManager.format(Msg.VF_SERVER_BUSY, runtimeContext.getVnfContext().getId()));
                }

                break;

            case Unlock:
                try {
                    releaseVNFLock(runtimeContext.getVnfContext().getId(), runtimeContext.getRequestContext().getCommonHeader().getRequestId());
                    fillStatus(runtimeContext,LCMCommandStatus.SUCCESS, null);
                } catch (LockException e) {
                    //TODO add proper error code and message
                    //  logger.error(EELFResourceManager.format(Msg.VF_SERVER_BUSY, runtimeContext.getVnfContext().getId()));
                    Params params = new Params().addParam("errorMsg", e.getMessage());
                    fillStatus(runtimeContext, LCMCommandStatus.LOCKING_FAILURE, params);
                }
                break;

            case CheckLock:
                boolean isLocked = lockManager.isLocked(runtimeContext.getVnfContext().getId());
                fillStatus(runtimeContext,LCMCommandStatus.SUCCESS, null);
                runtimeContext.getResponseContext().addKeyValueToAdditionalContext("locked", String.valueOf(isLocked).toUpperCase());
                break;
            default:
                try {
                    boolean lockAcquired = acquireVNFLock(runtimeContext.getVnfContext().getId(), runtimeContext.getRequestContext().getCommonHeader().getRequestId(), 0);
                    runtimeContext.setIsLockAcquired(lockAcquired);
                    callWfOperation(runtimeContext);
                } catch (LockException e) {
                    Params params = new Params().addParam("errorMsg", e.getMessage());
                    fillStatus(runtimeContext, LCMCommandStatus.LOCKING_FAILURE, params);
                } finally {
                    if (runtimeContext.isLockAcquired()) {
                        final int statusCode = runtimeContext.getResponseContext().getStatus().getCode();
                        if (statusCode % 100 == 2 || statusCode % 100 == 3) {
                            try {
                                releaseVNFLock(runtimeContext.getVnfContext().getId(), runtimeContext.getRequestContext().getCommonHeader().getRequestId());
                                //TODO add logger call
                            } catch (LockException e) {
                                //ignore
                            }
                        }
                    }
                }
        }

    }

    private void callWfOperation(RuntimeContext runtimeContext) {
        int remainingTTL = calculateRemainingTTL(runtimeContext.getRequestContext().getCommonHeader());
        if (remainingTTL > 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("Calling command Executor with remaining TTL value: " + remainingTTL);
            }

            RuntimeContext commandExecutorInput = cloneContext(runtimeContext);

            try {
                commandExecutor.executeCommand(commandExecutorInput);
                if(logger.isTraceEnabled()) {
                    logger.trace("Command was added to queue successfully for vnfID = " + ObjectUtils.toString(runtimeContext.getRequestContext().getActionIdentifiers().getVnfId()));
                }
                fillStatus(runtimeContext, LCMCommandStatus.ACCEPTED, null);
                if (isMetricEnabled) {
                    ((DispatchingFuntionMetric) metricRegistry.metric("DISPATCH_FUNCTION")).incrementAcceptedRequest();
                }
            } catch (APPCException e) {
                String errorMessage = e.getMessage() != null ? e.getMessage() : e.toString();
                Params params = new Params().addParam("errorMsg", errorMessage);
                fillStatus(runtimeContext, LCMCommandStatus.UNEXPECTED_ERROR, params);
            }

        } else {
            fillStatus(runtimeContext, LCMCommandStatus.EXPIRED_REQUEST, null);
            storeErrorMessageToLog(runtimeContext,
                    LoggingConstants.TargetNames.APPC,
                    LoggingConstants.TargetNames.REQUEST_HANDLER,
                    EELFResourceManager.format(Msg.APPC_EXPIRED_REQUEST,
                            runtimeContext.getRequestContext().getCommonHeader().getOriginatorId(),
                            runtimeContext.getRequestContext().getActionIdentifiers().getVnfId(),
                            String.valueOf(runtimeContext.getRequestContext().getCommonHeader().getFlags().getTtl())));
        }
    }

    private void fillStatus(RuntimeContext runtimeContext, LCMCommandStatus lcmCommandStatus, Params params) {
        runtimeContext.getResponseContext().setStatus(lcmCommandStatus.toStatus(params));
    }

    /*
     * Workaround to clone context in order to prevent sharing of ResponseContext by two threads (one to set Accepted
     * status code and other - depending on DG status). Other properties should not be a problem
     */
    private RuntimeContext cloneContext(RuntimeContext runtimeContext) {
        RuntimeContext other = new RuntimeContext();
        other.setRequestContext(runtimeContext.getRequestContext());
        other.setResponseContext(new ResponseContext());
        other.getResponseContext().setStatus(new Status(0, null));
        other.getResponseContext().setCommonHeader(runtimeContext.getRequestContext().getCommonHeader());
        other.setVnfContext(runtimeContext.getVnfContext());
        other.setRpcName(runtimeContext.getRpcName());
        other.setTimeStart(runtimeContext.getTimeStart());
        other.setIsLockAcquired(runtimeContext.isLockAcquired());
        return other;
    }


    private void clearRequestLogProperties() {
        try {
            MDC.remove(MDC_KEY_REQUEST_ID);
            MDC.remove(MDC_SERVICE_INSTANCE_ID);
            MDC.remove(MDC_SERVICE_NAME);
            MDC.remove(LoggingConstants.MDCKeys.PARTNER_NAME);
            MDC.remove(LoggingConstants.MDCKeys.TARGET_VIRTUAL_ENTITY);
        } catch (Exception e) {

        }
    }

    private void removeRequestFromRegistry(CommonHeader commonHeader) {
        if (logger.isTraceEnabled())
            logger.trace("Entering to removeRequestFromRegistry with RequestHeader = " + ObjectUtils.toString(commonHeader));
        requestRegistry.removeRequest(
                new UniqueRequestIdentifier(commonHeader.getOriginatorId(),
                        commonHeader.getRequestId(),
                        commonHeader.getSubRequestId()));
    }

    private boolean acquireVNFLock(String vnfID, String requestId, long timeout) throws LockException {
        if (logger.isTraceEnabled())
            logger.trace("Entering to acquireVNFLock with vnfID = " + vnfID);
        boolean lockAcquired = lockManager.acquireLock(vnfID, requestId, timeout);
        if (lockAcquired) {
            logger.info("Lock acquired for vnfID = " + vnfID);
        } else {
            logger.info("vnfID = " + vnfID + " was already locked");
        }
        return lockAcquired;
    }

    private void lockWithTimeout(String vnfId, String requestId) throws LockException {
        long timeout = configuration.getLongProperty(PROP_IDLE_TIMEOUT, Constants.DEFAULT_IDLE_TIMEOUT);
        acquireVNFLock(vnfId, requestId, timeout);
    }

    private void resetLock(String vnfId, String requestId, boolean lockAcquired, boolean resetLockTimeout) {
        if (lockAcquired) {
            try {
                releaseVNFLock(vnfId, requestId);
            } catch (LockException e) {
                logger.error("Unlock VNF [" + vnfId + "] failed. Request id: [" + requestId + "]", e);


            }
        } else if (resetLockTimeout) {
            try {
                // reset timeout to previous value
                lockWithTimeout(vnfId, requestId);
            } catch (LockException e) {
                logger.error("Reset lock idle timeout for VNF [" + vnfId + "] failed. Request id: [" + requestId + "]", e);
            }
        }
    }

    private void releaseVNFLock(String vnfId, String transactionId) throws LockException {
        lockManager.releaseLock(vnfId, transactionId);
        logger.info("Lock released for vnfID = " + vnfId);
    }

    private void setInitialLogProperties(RequestContext requestContext) {

        try {
            MDC.put(MDC_KEY_REQUEST_ID, requestContext.getCommonHeader().getRequestId());
            if (requestContext.getActionIdentifiers().getServiceInstanceId() != null) {
                MDC.put(MDC_SERVICE_INSTANCE_ID, requestContext.getActionIdentifiers().getServiceInstanceId());
            }
            MDC.put(LoggingConstants.MDCKeys.PARTNER_NAME, requestContext.getCommonHeader().getOriginatorId());
            MDC.put(MDC_INSTANCE_UUID, ""); // value should be created in the future
            try {
                MDC.put(MDC_SERVER_FQDN, InetAddress.getLocalHost().getCanonicalHostName()); //Don't change it to a .getHostName() again please. It's wrong!
                MDC.put(MDC_SERVER_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress());
                MDC.put(LoggingConstants.MDCKeys.SERVER_NAME, InetAddress.getLocalHost().getHostName());
                MDC.put(MDC_SERVICE_NAME, requestContext.getAction().name());
                MDC.put(LoggingConstants.MDCKeys.TARGET_VIRTUAL_ENTITY, requestContext.getActionIdentifiers().getVnfId());

            } catch (Exception e) {
                logger.debug(e.getMessage());
            }
        } catch (RuntimeException e) {
            //ignore
        }
    }


    private int calculateRemainingTTL(CommonHeader commonHeader) {
        if (logger.isTraceEnabled()) {
            logger.trace("Entering to calculateRemainingTTL with RequestHeader = " + ObjectUtils.toString(commonHeader));
        }
        long usedTimeInMillis = ChronoUnit.MILLIS.between(commonHeader.getTimeStamp(), Instant.now());
        logger.debug("usedTimeInMillis = " + usedTimeInMillis);
        int usedTimeInSeconds = Math.round(usedTimeInMillis / 1000);
        logger.debug("usedTimeInSeconds = " + usedTimeInSeconds);
        Integer inputTTL = this.getInputTTL(commonHeader);
        logger.debug("inputTTL = " + inputTTL);
        Integer remainingTTL = inputTTL - usedTimeInSeconds;
        logger.debug("Remaining TTL = " + remainingTTL);
        if (logger.isTraceEnabled())
            logger.trace("Exiting from calculateRemainingTTL with (remainingTTL = " + ObjectUtils.toString(remainingTTL) + ")");
        return remainingTTL;
    }

    private Integer getInputTTL(CommonHeader header) {
        if (logger.isTraceEnabled())
            logger.trace("Entering in getInputTTL with RequestHeader = " + ObjectUtils.toString(header));
        if (!isValidTTL(String.valueOf(header.getFlags().getTtl()))) {
            String defaultTTLStr = configuration.getProperty("org.openecomp.appc.workflow.default.ttl", String.valueOf(Constants.DEFAULT_TTL));
            Integer defaultTTL = Integer.parseInt(defaultTTLStr);
            if (logger.isTraceEnabled())
                logger.trace("Exiting from getInputTTL with (defaultTTL = " + ObjectUtils.toString(defaultTTL) + ")");
            return defaultTTL;
        }
        if (logger.isTraceEnabled())
            logger.trace("Exiting from getInputTTL with (inputTTL = " + ObjectUtils.toString(header.getFlags().getTtl()) + ")");

        return header.getFlags().getTtl();
    }

    private boolean isValidTTL(String ttl) {
        if (ttl == null || ttl.length() == 0) {
            if (logger.isTraceEnabled())
                logger.trace("Exiting from getInputTTL with (result = false)");
            return false;
        }
        try {
            Integer i = Integer.parseInt(ttl);
            return (i > 0);
        } catch (NumberFormatException e) {
            if (logger.isTraceEnabled())
                logger.trace("Exiting from getInputTTL with (result = false)");
            return false;
        }
    }

    private Boolean isLoggingEnabled() {
        String defaultFlagStr = configuration.getProperty("org.openecomp.appc.localTransactionRecorder.enable", String.valueOf(Constants.DEFAULT_LOGGING_FLAG));
        Boolean defaultFlag = Boolean.parseBoolean(defaultFlagStr);
        return defaultFlag;
    }

    private static RequestHandlerOutput buildRequestHandlerOutput(LCMCommandStatus response, Params params) {
        RequestHandlerOutput output = new RequestHandlerOutput();
        ResponseContext responseContext = new ResponseContext();
        responseContext.setStatus(response.toStatus(params));
        output.setResponseContext(responseContext);
        return output;
    }

    /**
     * This method perform operations required before execution of workflow starts. It retrieves next state for current operation from Lifecycle manager and update it in AAI.
     *
     * @return true in case AAI updates are successful. false for any error or exception.
     */
    @Override
    public void onRequestExecutionStart(String vnfId, boolean readOnlyActivity, String requestIdentifierString, boolean forceFlag) throws UnstableVNFException {
        if (logger.isTraceEnabled()) {
            logger.trace("Entering to onRequestExecutionStart with vnfId = " + vnfId + "and requestIdentifierString = " + requestIdentifierString);
        }

        if(!readOnlyActivity || !forceFlag || workingStateManager.isVNFStable(vnfId)) {
            boolean updated = false;
            try {
                updated = workingStateManager.setWorkingState(vnfId, VNFWorkingState.UNSTABLE, requestIdentifierString, forceFlag);
            }  catch (Exception e) {
                logger.error("Error updating working state for vnf " + vnfId + e);
                throw new RuntimeException(e);
            }
            if (!updated) {
                throw new UnstableVNFException("VNF is not stable for vnfID = " + vnfId);
            }
        }

        if (logger.isTraceEnabled())
            logger.trace("Exiting from onRequestExecutionStart ");
    }

    /**
     * This method perform following operations required after execution of workflow.
     * It posts asynchronous response to message bus (DMaaP).
     * Unlock VNF Id
     * Removes request from request registry.
     * Generate audit logs.
     * Adds transaction record to database id if transaction logging is enabled.
     *
     * @param isAAIUpdated boolean flag which indicate AAI upodate status after request completion.
     */
    @Override
    public void onRequestExecutionEnd(RuntimeContext runtimeContext, boolean isAAIUpdated) {
        if (logger.isTraceEnabled()) {
            logger.trace("Entering to onRequestExecutionEnd with runtimeContext = " + ObjectUtils.toString(runtimeContext));
        }

        postMessageToDMaaP(runtimeContext.getRequestContext().getAction(), runtimeContext.getRpcName(), runtimeContext.getResponseContext());
        requestRegistry.removeRequest(
                new UniqueRequestIdentifier(runtimeContext.getResponseContext().getCommonHeader().getOriginatorId(),
                        runtimeContext.getResponseContext().getCommonHeader().getRequestId(),
                        runtimeContext.getResponseContext().getCommonHeader().getSubRequestId()));
        resetLock(runtimeContext.getVnfContext().getId(), runtimeContext.getResponseContext().getCommonHeader().getRequestId(), runtimeContext.isLockAcquired(), true);
        Status status = runtimeContext.getResponseContext().getStatus();

        VNFWorkingState workingState;
        if (status.getCode() == LCMCommandStatus.SUCCESS.getResponseCode() || isReadOnlyAction(runtimeContext.getRequestContext().getAction())) {
            workingState = VNFWorkingState.STABLE;
        } else {
            workingState = VNFWorkingState.UNKNOWN;
        }

        UniqueRequestIdentifier requestIdentifier = new UniqueRequestIdentifier(runtimeContext.getResponseContext().getCommonHeader().getOriginatorId(),
                runtimeContext.getResponseContext().getCommonHeader().getRequestId(),
                runtimeContext.getResponseContext().getCommonHeader().getSubRequestId());

        String requestIdentifierString = requestIdentifier.toIdentifierString();
        workingStateManager.setWorkingState(runtimeContext.getVnfContext().getId(), workingState, requestIdentifierString, false);


        storeAuditLogRecord(runtimeContext);

        if (isLoggingEnabled()) {
            createTransactionRecord(runtimeContext);
        }
    }

    private void storeAuditLogRecord(RuntimeContext runtimeContext) {
        LoggingUtils.logAuditMessage(runtimeContext.getTimeStart(),
                Instant.now(),
                String.valueOf(runtimeContext.getResponseContext().getStatus().getCode()),
                runtimeContext.getResponseContext().getStatus().getMessage(),
                this.getClass().getCanonicalName());
    }

    private void storeMetricLogRecord(RuntimeContext runtimeContext) {
        LoggingUtils.logMetricsMessage(runtimeContext.getTimeStart(),
                Instant.now(),
                LoggingConstants.TargetNames.APPC,
                runtimeContext.getRequestContext().getAction().name(),
                runtimeContext.getResponseContext().getStatus().getCode() == LCMCommandStatus.ACCEPTED.getResponseCode() ? LoggingConstants.StatusCodes.COMPLETE : LoggingConstants.StatusCodes.ERROR,
                String.valueOf(runtimeContext.getResponseContext().getStatus().getCode()),
                runtimeContext.getResponseContext().getStatus().getMessage(),
                this.getClass().getCanonicalName());
    }

    private boolean isReadOnlyAction(VNFOperation action) {
        return VNFOperation.Sync == action
                || VNFOperation.Audit == action;
    }


    //returns null if asyncResponse was not modified otherwise reutrns the updated asyncResponse
    private void postMessageToDMaaP(VNFOperation operation, String rpcName, ResponseContext responseContext/*, boolean isTTLEnd, boolean aaiUpdateSuccess*/) {
        if (logger.isTraceEnabled()) {
            logger.trace("Entering to postMessageToDMaaP with AsyncResponse = " + ObjectUtils.toString(responseContext));
        }
        boolean callbackResponse = messageAdapter.post(operation, rpcName, responseContext);
        if (!callbackResponse) {
            logger.error("DMaaP posting status: " + callbackResponse, "dmaapMessage: " + responseContext);
        }
        if (logger.isTraceEnabled())
            logger.trace("Exiting from postMessageToDMaaP with (callbackResponse = " + ObjectUtils.toString(callbackResponse) + ")");
    }

    /**
     * This method perform following operations required if TTL ends when request still waiting in execution queue .
     * It posts asynchronous response to message bus (DMaaP).
     * Unlock VNF Id
     * Removes request from request registry.
     *
     * @param runtimeContext AsyncResponse object which contains VNF Id 	, timestamp , apiVersion, responseId, executionSuccess, payload, isExpired, action, startTime, vnfType, originatorId, subResponseId;
     * @param updateAAI      boolean flag which indicate AAI upodate status after request completion.
     */
    @Override
    public void onRequestTTLEnd(RuntimeContext runtimeContext, boolean updateAAI) {
        if (logger.isTraceEnabled()) {
            logger.trace("Entering to onRequestTTLEnd with " +
                    "AsyncResponse = " + ObjectUtils.toString(runtimeContext) +
                    ", updateAAI = " + ObjectUtils.toString(updateAAI));
        }
        logger.error(LCMCommandStatus.EXPIRED_REQUEST_FAILURE.getResponseMessage());
        fillStatus(runtimeContext, LCMCommandStatus.EXPIRED_REQUEST_FAILURE, null);
        postMessageToDMaaP(runtimeContext.getRequestContext().getAction(), runtimeContext.getRpcName(), runtimeContext.getResponseContext());
        resetLock(runtimeContext.getVnfContext().getId(), runtimeContext.getResponseContext().getCommonHeader().getRequestId(), runtimeContext.isLockAcquired(), true);
        requestRegistry.removeRequest(
                new UniqueRequestIdentifier(runtimeContext.getResponseContext().getCommonHeader().getOriginatorId(),
                        runtimeContext.getResponseContext().getCommonHeader().getRequestId(),
                        runtimeContext.getResponseContext().getCommonHeader().getSubRequestId()));
    }

    private SvcLogicContext getVnfdata(String vnf_id, String prefix, SvcLogicContext ctx) throws VNFNotFoundException {
        String key = "vnf-id = '" + vnf_id + "'";
        if (logger.isTraceEnabled()) {
            logger.trace("Entering to getVnfdata with " +
                    "vnfId = " + vnf_id +
                    ", prefix = " + prefix +
                    ", SvcLogicContex = " + ObjectUtils.toString(ctx));
        }
        try {
            QueryStatus response = aaiService.query("generic-vnf", false, null, key, prefix, null, ctx);
            if (QueryStatus.NOT_FOUND.equals(response)) {
                throw new VNFNotFoundException("VNF not found for vnf_id = " + vnf_id);
            } else if (QueryStatus.FAILURE.equals(response)) {
                throw new RuntimeException("Error Querying AAI with vnfID = " + vnf_id);
            }
            logger.info("AAIResponse: " + response.toString());
        } catch (SvcLogicException e) {
            logger.error("Error in getVnfdata " + e);
            throw new RuntimeException(e);
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Exiting from getVnfdata with (SvcLogicContext = " + ObjectUtils.toString(ctx) + ")");
        }
        return ctx;
    }

    private boolean postVnfdata(String vnf_id, String status, String prefix, SvcLogicContext ctx) throws VNFNotFoundException {
        if (logger.isTraceEnabled()) {
            logger.trace("Entering to postVnfdata with " +
                    "vnfId = " + vnf_id +
                    ", status = " + status +
                    ", prefix = " + prefix +
                    ", SvcLogicContext = " + ObjectUtils.toString(ctx));
        }
        String key = "vnf-id = '" + vnf_id + "'";
        Map<String, String> data = new HashMap<>();
        data.put("orchestration-status", status);
        try {
            QueryStatus response = aaiService.update("generic-vnf", key, data, prefix, ctx);
            if (QueryStatus.NOT_FOUND.equals(response)) {
                throw new VNFNotFoundException("VNF not found for vnf_id = " + vnf_id);
            }
            logger.info("AAIResponse: " + response.toString());
            if (response.toString().equals("SUCCESS")) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Exiting from postVnfdata with (Result = " + ObjectUtils.toString(true) + ")");
                }
                return true;
            }

        } catch (SvcLogicException e) {
            logger.error("Error in postVnfdata " + e);
            throw new RuntimeException(e);
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Exiting from postVnfdata with (Result = " + ObjectUtils.toString(false) + ")");
        }
        return false;
    }

    private void initMetric() {
        if (logger.isDebugEnabled())
            logger.debug("Metric getting initialized");
        MetricService metricService = getMetricservice();
        metricRegistry = metricService.createRegistry("APPC");
        DispatchingFuntionMetric dispatchingFuntionMetric = metricRegistry.metricBuilderFactory().
                dispatchingFunctionCounterBuilder().
                withName("DISPATCH_FUNCTION").withType(MetricType.COUNTER).
                withAcceptRequestValue(0)
                .withRejectRequestValue(0)
                .build();
        if (metricRegistry.register(dispatchingFuntionMetric)) {
            Metric[] metrics = new Metric[]{dispatchingFuntionMetric};
            LogPublisher logPublisher = new LogPublisher(metricRegistry, metrics);
            LogPublisher[] logPublishers = new LogPublisher[1];
            logPublishers[0] = logPublisher;
            PublishingPolicy manuallyScheduledPublishingPolicy = metricRegistry.policyBuilderFactory().
                    scheduledPolicyBuilder().withPublishers(logPublishers).
                    withMetrics(metrics).
                    build();
            if (logger.isDebugEnabled())
                logger.debug("Policy getting initialized");
            manuallyScheduledPublishingPolicy.init();
            if (logger.isDebugEnabled())
                logger.debug("Metric initialized");
        }
    }


    private MetricService getMetricservice() {
        BundleContext bctx = FrameworkUtil.getBundle(MetricService.class).getBundleContext();
        ServiceReference sref = bctx.getServiceReference(MetricService.class.getName());
        if (sref != null) {
            logger.info("Metric Service from bundlecontext");
            return (MetricService) bctx.getService(sref);
        } else {
            logger.info("Metric Service error from bundlecontext");
            logger.warn("Cannot find service reference for org.openecomp.appc.metricservice.MetricService");
            return null;
        }
    }

    /**
     * This method returns the count of in progress requests
     * * @return in progress requests count
     */
    @Override
    public int getInprogressRequestCount() {
        if (logger.isTraceEnabled()) {
            logger.trace("Entering to getInprogressRequestCount");
        }
        return requestRegistry.getRegisteredRequestCount();
    }
}
