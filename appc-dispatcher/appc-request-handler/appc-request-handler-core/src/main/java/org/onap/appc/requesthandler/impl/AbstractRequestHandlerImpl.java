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

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.onap.appc.configuration.Configuration;
import org.onap.appc.configuration.ConfigurationFactory;
import org.onap.appc.domainmodel.lcm.Flags;
import org.onap.appc.domainmodel.lcm.RequestContext;
import org.onap.appc.domainmodel.lcm.RequestStatus;
import org.onap.appc.domainmodel.lcm.ResponseContext;
import org.onap.appc.domainmodel.lcm.RuntimeContext;
import org.onap.appc.domainmodel.lcm.Status;
import org.onap.appc.domainmodel.lcm.TransactionRecord;
import org.onap.appc.domainmodel.lcm.VNFOperation;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.exceptions.InvalidInputException;
import org.onap.appc.executor.objects.LCMCommandStatus;
import org.onap.appc.executor.objects.Params;
import org.onap.appc.lockmanager.api.LockException;
import org.onap.appc.logging.LoggingConstants;
import org.onap.appc.logging.LoggingUtils;
import org.onap.appc.messageadapter.MessageAdapter;
import org.onap.appc.messageadapter.impl.MessageAdapterImpl;
import org.onap.appc.metricservice.MetricRegistry;
import org.onap.appc.metricservice.MetricService;
import org.onap.appc.metricservice.metric.DispatchingFuntionMetric;
import org.onap.appc.metricservice.metric.Metric;
import org.onap.appc.metricservice.metric.MetricType;
import org.onap.appc.metricservice.policy.PublishingPolicy;
import org.onap.appc.metricservice.publisher.LogPublisher;
import org.onap.appc.requesthandler.RequestHandler;
import org.onap.appc.requesthandler.exceptions.RequestValidationException;
import org.onap.appc.requesthandler.helper.RequestValidator;
import org.onap.appc.requesthandler.objects.RequestHandlerInput;
import org.onap.appc.requesthandler.objects.RequestHandlerOutput;
import org.onap.appc.transactionrecorder.TransactionRecorder;
import org.onap.appc.transactionrecorder.objects.TransactionConstants;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.MDC;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.TimeZone;
import static com.att.eelf.configuration.Configuration.MDC_INSTANCE_UUID;
import static com.att.eelf.configuration.Configuration.MDC_KEY_REQUEST_ID;
import static com.att.eelf.configuration.Configuration.MDC_SERVER_FQDN;
import static com.att.eelf.configuration.Configuration.MDC_SERVER_IP_ADDRESS;
import static com.att.eelf.configuration.Configuration.MDC_SERVICE_INSTANCE_ID;
import static com.att.eelf.configuration.Configuration.MDC_SERVICE_NAME;

/**
 * This class provides application logic for the Request/Response Handler Component.
 */
public abstract class AbstractRequestHandlerImpl implements RequestHandler {

    private RequestValidator requestValidator;

    protected TransactionRecorder transactionRecorder;

    private MessageAdapter messageAdapter;

    static MetricRegistry metricRegistry;

    private boolean isMetricEnabled = false;

    protected final Configuration configuration = ConfigurationFactory.getConfiguration();

    private final EELFLogger logger = EELFManager.getInstance().getLogger(AbstractRequestHandlerImpl.class);

    AbstractRequestHandlerImpl() {
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

    public void setTransactionRecorder(TransactionRecorder transactionRecorder) {
        this.transactionRecorder = transactionRecorder;
    }

    public void setRequestValidator(RequestValidator requestValidator) {
        this.requestValidator = requestValidator;
    }

    public void setMessageAdapter(MessageAdapter messageAdapter) {
        this.messageAdapter = messageAdapter;
    }

    /**
     * It receives requests from the north-bound REST API (Communication) Layer and
     * performs following validations.
     * 1. VNF exists in A&AI for the given targetID (VnfID)
     * 2. For the current VNF  Orchestration Status, the command can be executed
     * 3. For the given VNF type and Operation, there exists work-flow definition in the APPC database
     * If any of the validation fails, it returns appropriate response     *
     *
     * @param input RequestHandlerInput object which contains request header and  other request parameters like
     *              command , target Id , payload etc.
     * @return response for request as enum with Return code and message.
     */
    @Override
    public RequestHandlerOutput handleRequest(RequestHandlerInput input) {
        if (logger.isTraceEnabled())
            logger.trace("Entering to handleRequest with RequestHandlerInput = " + ObjectUtils.toString(input) + ")");
        String errorMessage = null;
        RequestStatus requestStatus;
        TransactionRecord transactionRecord = createTransactionRecord(input);
        RuntimeContext runtimeContext = createRuntimeContext(input, transactionRecord);
        ResponseContext responseContext = createResponseContext(input);
        runtimeContext.setResponseContext(responseContext);
        RequestHandlerOutput output = new RequestHandlerOutput();
        try {
            transactionRecorder.store(transactionRecord);
            requestValidator.validateRequest(runtimeContext);

            setInitialLogProperties(input.getRequestContext());

            handleRequest(runtimeContext);
            output.setResponseContext(runtimeContext.getResponseContext());
        } catch (RequestValidationException e) {
            errorMessage = e.getMessage();
            logger.error(errorMessage, e);
            if (!StringUtils.isEmpty(e.getLogMessage()))
                storeErrorMessageToLog(runtimeContext, e.getTargetEntity(), e.getTargetService(), e.getLogMessage());
            output = buildRequestHandlerOutput(e.getLcmCommandStatus(), e.getParams());
        } catch (InvalidInputException e) {
            logger.error("InvalidInputException : " + e.getMessage(), e);
            errorMessage = e.getMessage() != null ? e.getMessage() : e.toString();
            output = buildRequestHandlerOutput(LCMCommandStatus.INVALID_INPUT_PARAMETER, new Params().addParam
                ("errorMsg", errorMessage));
        } catch (LockException e) {
            logger.error("LockException : " + e.getMessage(), e);
            Params params = new Params().addParam("errorMsg", e.getMessage());
            fillStatus(runtimeContext, LCMCommandStatus.LOCKED_VNF_ID, params);
            output = buildRequestHandlerOutput(LCMCommandStatus.LOCKED_VNF_ID, params);
        } catch (Exception e) {
            logger.error("Exception : " + e.getMessage(), e);
            storeErrorMessageToLog(runtimeContext, "", "", "Exception = " + e.getMessage());
            errorMessage = e.getMessage() != null ? e.getMessage() : e.toString();
            Params params = new Params().addParam("errorMsg", errorMessage);
            output = buildRequestHandlerOutput(LCMCommandStatus.UNEXPECTED_ERROR, params);
        } finally {
            final int statusCode = output.getResponseContext().getStatus().getCode();
            if (statusCode == LCMCommandStatus.ACCEPTED.getResponseCode()) {
                requestStatus = RequestStatus.ACCEPTED;
            } else if (statusCode == LCMCommandStatus.SUCCESS.getResponseCode()) {
                requestStatus = RequestStatus.SUCCESSFUL;
                if (isMetricEnabled)
                    ((DispatchingFuntionMetric) metricRegistry.metric("DISPATCH_FUNCTION")).incrementAcceptedRequest();
            } else {
                requestStatus = (statusCode == LCMCommandStatus.EXPIRED_REQUEST.getResponseCode()) ? RequestStatus
                    .TIMEOUT : RequestStatus.REJECTED;
                if (isMetricEnabled)
                    ((DispatchingFuntionMetric) metricRegistry.metric("DISPATCH_FUNCTION")).incrementRejectedRequest();
            }
            try {
                if (errorMessage != null && logger.isDebugEnabled())
                    logger.debug("error occurred in handleRequest " + errorMessage);
                logger.debug("output.getResponseContext().getStatus().getCode():  " + statusCode);
                runtimeContext.setResponseContext(output.getResponseContext());
            } finally {
                runtimeContext.getTransactionRecord().setRequestState(requestStatus);
                runtimeContext.getTransactionRecord().setResultCode(output.getResponseContext().getStatus().getCode());
                updateTransactionStatus(runtimeContext.getTransactionRecord());
                recordAndClearLogProperties(runtimeContext);
            }
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Exiting from handleRequest with (RequestHandlerOutput = " +
                ObjectUtils.toString(output.getResponseContext()) + ")");
        }
        return output;
    }

    private ResponseContext createResponseContext(RequestHandlerInput input) {
        final ResponseContext responseContext = new ResponseContext();
        responseContext.setStatus(new Status());
        responseContext.setAdditionalContext(new HashMap<>(4));
        responseContext.setCommonHeader(input.getRequestContext().getCommonHeader());
        return responseContext;
    }

    private void updateTransactionStatus(TransactionRecord record) {
        Map<TransactionConstants.TRANSACTION_ATTRIBUTES, String> updateColumns = new HashMap<>();
        if (!StringUtils.isBlank(record.getTargetType())) {
            updateColumns.put(TransactionConstants.TRANSACTION_ATTRIBUTES.TARGET_TYPE, record.getTargetType());
        }
        updateColumns.put(TransactionConstants.TRANSACTION_ATTRIBUTES.STATE, record.getRequestState());
        updateColumns.put(TransactionConstants.TRANSACTION_ATTRIBUTES.RESULT_CODE,
                String.valueOf(record.getResultCode()));
        if (RequestStatus.valueOf(record.getRequestState()).isTerminal()) {
            Date endTime = new Date();
            updateColumns.put(TransactionConstants.TRANSACTION_ATTRIBUTES.END_TIME,
                    dateToStringConverterMillis(endTime));
        }
        try {
            transactionRecorder.update(record.getTransactionId(), updateColumns);
        } catch (APPCException e) {
            logger.error("Error accessing database", e);
        }
    }

    private RuntimeContext createRuntimeContext(RequestHandlerInput input, TransactionRecord transactionRecord) {
        RuntimeContext runtimeContext;
        runtimeContext = new RuntimeContext();
        runtimeContext.setRequestContext(input.getRequestContext());
        runtimeContext.setTimeStart(transactionRecord.getStartTime());
        runtimeContext.setRpcName(input.getRpcName());
        runtimeContext.setTransactionRecord(transactionRecord);
        return runtimeContext;
    }

    private TransactionRecord createTransactionRecord(RequestHandlerInput input) {
        Instant startTime = Instant.now();
        TransactionRecord record = new TransactionRecord();
        record.setTransactionId(UUID.randomUUID().toString());
        record.setRequestState(RequestStatus.RECEIVED);
        record.setRequestId(input.getRequestContext().getCommonHeader().getRequestId());
        record.setSubRequestId(input.getRequestContext().getCommonHeader().getSubRequestId());
        record.setOriginatorId(input.getRequestContext().getCommonHeader().getOriginatorId());
        record.setOriginTimestamp(input.getRequestContext().getCommonHeader().getTimeStamp().toInstant());
        record.setStartTime(startTime);
        record.setOperation(VNFOperation.valueOf(input.getRequestContext().getAction().name()));
        record.setTargetId(input.getRequestContext().getActionIdentifiers().getVnfId());
        record.setVnfcName(input.getRequestContext().getActionIdentifiers().getVnfcName());
        record.setVserverId(input.getRequestContext().getActionIdentifiers().getVserverId());
        record.setVfModuleId(input.getRequestContext().getActionIdentifiers().getVfModuleId());
        record.setServiceInstanceId(input.getRequestContext().getActionIdentifiers().getServiceInstanceId());
        Flags.Mode mode;
        if (input.getRequestContext().getCommonHeader().getFlags() != null &&
            input.getRequestContext().getCommonHeader().getFlags().getMode() != null) {
            mode = input.getRequestContext().getCommonHeader().getFlags().getMode();
        } else {
            mode = Flags.Mode.NORMAL;
        }
        record.setMode(mode);
        return record;
    }

    private void recordAndClearLogProperties(RuntimeContext runtimeContext) {
        storeAuditLogRecord(runtimeContext);
        storeMetricLogRecord(runtimeContext);
        clearRequestLogProperties();
    }

    void storeErrorMessageToLog(RuntimeContext runtimeContext, String targetEntity, String targetServiceName,
                                String additionalMessage) {
        LoggingUtils.logErrorMessage(runtimeContext.getResponseContext().getStatus() != null ?
                String.valueOf(runtimeContext.getResponseContext().getStatus().getCode()) : "",
            runtimeContext.getResponseContext().getStatus() != null ?
                String.valueOf(runtimeContext.getResponseContext().getStatus().getMessage()) : "",
            targetEntity,
            targetServiceName,
            additionalMessage,
            this.getClass().getCanonicalName());
    }

    protected abstract void handleRequest(RuntimeContext runtimeContext);

    void fillStatus(RuntimeContext runtimeContext, LCMCommandStatus lcmCommandStatus, Params params) {
        runtimeContext.getResponseContext().getStatus().setCode(lcmCommandStatus.getResponseCode());
        runtimeContext.getResponseContext().getStatus().setMessage(lcmCommandStatus.getFormattedMessage(params));
    }

    private void clearRequestLogProperties() {
        try {
            MDC.remove(MDC_KEY_REQUEST_ID);
            MDC.remove(MDC_SERVICE_INSTANCE_ID);
            MDC.remove(MDC_SERVICE_NAME);
            MDC.remove(LoggingConstants.MDCKeys.PARTNER_NAME);
            MDC.remove(LoggingConstants.MDCKeys.TARGET_VIRTUAL_ENTITY);
        } catch (Exception e) {
            logger.error("Error clearing MDC log properties. " + e.getMessage(), e);
        }
    }

    private void setInitialLogProperties(RequestContext requestContext) {

        try {
            MDC.put(MDC_KEY_REQUEST_ID, requestContext.getCommonHeader().getRequestId());
            if (requestContext.getActionIdentifiers().getServiceInstanceId() != null) {
                MDC.put(MDC_SERVICE_INSTANCE_ID, requestContext.getActionIdentifiers().getServiceInstanceId());
            }
            MDC.put(LoggingConstants.MDCKeys.PARTNER_NAME, requestContext.getCommonHeader().getOriginatorId());
            MDC.put(MDC_INSTANCE_UUID, ""); // value should be created in the future
            //Don't change it to a.getHostName() again please. It's wrong!
            MDC.put(MDC_SERVER_FQDN, InetAddress.getLocalHost().getCanonicalHostName());
            MDC.put(MDC_SERVER_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress());
            MDC.put(LoggingConstants.MDCKeys.SERVER_NAME, InetAddress.getLocalHost().getHostName());
            MDC.put(MDC_SERVICE_NAME, requestContext.getAction().name());
            MDC.put(LoggingConstants.MDCKeys.TARGET_VIRTUAL_ENTITY, requestContext.getActionIdentifiers().getVnfId());
        } catch (UnknownHostException e) {
            logger.error("Error occured while setting initial log properties", e);
        }
    }

    private static RequestHandlerOutput buildRequestHandlerOutput(LCMCommandStatus response, Params params) {
        RequestHandlerOutput output = new RequestHandlerOutput();
        ResponseContext responseContext = new ResponseContext();
        org.onap.appc.domainmodel.lcm.Status status = new org.onap.appc.domainmodel.lcm.Status();
        status.setCode(response.getResponseCode());
        status.setMessage(response.getFormattedMessage(params));
        responseContext.setStatus(status);
        output.setResponseContext(responseContext);
        return output;
    }

    /**
     * This method perform following operations required after execution of workflow.
     * It posts asynchronous response to message bus (DMaaP).
     * Unlock VNF Id
     * Removes request from request registry.
     * Generate audit logs.
     * Adds transaction record to database id if transaction logging is enabled.
     */
    @Override
    public void onRequestExecutionEnd(RuntimeContext runtimeContext) {
        if (logger.isTraceEnabled()) {
            logger.trace("Entering to onRequestExecutionEnd with runtimeContext = " +
                ObjectUtils.toString(runtimeContext));
        }
        postMessageToDMaaP(runtimeContext.getRequestContext().getAction(), runtimeContext.getRpcName(),
            runtimeContext.getResponseContext());
        final int statusCode = runtimeContext.getResponseContext().getStatus().getCode();
        RequestStatus requestStatus =
            (statusCode == LCMCommandStatus.SUCCESS.getResponseCode()) ?
                RequestStatus.SUCCESSFUL : RequestStatus.FAILED;
        runtimeContext.getTransactionRecord().setRequestState(requestStatus);
        runtimeContext.getTransactionRecord().setResultCode(runtimeContext.getResponseContext().getStatus().getCode());
        updateTransactionStatus(runtimeContext.getTransactionRecord());
        storeAuditLogRecord(runtimeContext);
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
            runtimeContext.getResponseContext().getStatus().getCode() == LCMCommandStatus.ACCEPTED.getResponseCode()
                ? LoggingConstants.StatusCodes.COMPLETE : LoggingConstants.StatusCodes.ERROR,
            String.valueOf(runtimeContext.getResponseContext().getStatus().getCode()),
            runtimeContext.getResponseContext().getStatus().getMessage(),
            this.getClass().getCanonicalName());
    }

    private void postMessageToDMaaP(VNFOperation operation, String rpcName, ResponseContext responseContext) {
        if (logger.isTraceEnabled()) {
            logger.trace("Entering to postMessageToDMaaP with AsyncResponse = " +
                ObjectUtils.toString(responseContext));
        }
        boolean callbackResponse = messageAdapter.post(operation, rpcName, responseContext);
        if (!callbackResponse) {
            logger.error("DMaaP posting status: false", "dmaapMessage: " + responseContext);
        }
        if (logger.isTraceEnabled())
            logger.trace("Exiting from postMessageToDMaaP with (callbackResponse = " +
                ObjectUtils.toString(callbackResponse) + ")");
    }

    private void initMetric() {
        if (logger.isDebugEnabled())
            logger.debug("Metric getting initialized");
        MetricService metricService = getMetricservice();
        // Check for the metric service created before trying to create registry using
        // the metricService object
        if (metricService == null) {
            // Cannot find service reference for org.onap.appc.metricservice.MetricService
            throw new NullPointerException("org.onap.appc.metricservice.MetricService is null. " +
                    "Failed to init Metric");
        }
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
            PublishingPolicy manuallyScheduledPublishingPolicy = metricRegistry.policyBuilderFactory()
                .scheduledPolicyBuilder()
                .withPublishers(logPublishers)
                .withMetrics(metrics)
                .build();

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
            logger.warn("Cannot find service reference for org.onap.appc.metricservice.MetricService");
            return null;
        }
    }

    /**
     * This method returns the count of in progress requests
     * * @return in progress requests count
     */
    @Override
    public int getInprogressRequestCount() throws APPCException {
        if (logger.isTraceEnabled()) {
            logger.trace("Entering to getInprogressRequestCount");
        }
        return transactionRecorder.getInProgressRequestsCount();
    }

    public static String dateToStringConverterMillis(Date date) {
        SimpleDateFormat customDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        customDate.setTimeZone(TimeZone.getTimeZone("UTC"));
        if (date != null) {
            return customDate.format(date);
        }
        return null;
    }
}
