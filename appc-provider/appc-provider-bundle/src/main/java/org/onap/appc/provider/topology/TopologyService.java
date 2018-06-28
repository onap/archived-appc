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

package org.onap.appc.provider.topology;

import static com.att.eelf.configuration.Configuration.MDC_ALERT_SEVERITY;
import static com.att.eelf.configuration.Configuration.MDC_INSTANCE_UUID;
import static com.att.eelf.configuration.Configuration.MDC_KEY_REQUEST_ID;
import static com.att.eelf.configuration.Configuration.MDC_REMOTE_HOST;
import static com.att.eelf.configuration.Configuration.MDC_SERVER_FQDN;
import static com.att.eelf.configuration.Configuration.MDC_SERVER_IP_ADDRESS;
import static com.att.eelf.configuration.Configuration.MDC_SERVICE_INSTANCE_ID;
import static com.att.eelf.configuration.Configuration.MDC_SERVICE_NAME;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;
import org.onap.appc.Constants;
import org.onap.appc.configuration.Configuration;
import org.onap.appc.configuration.ConfigurationFactory;
import org.onap.appc.i18n.Msg;
import org.onap.appc.logging.LoggingConstants;
import org.onap.appc.logging.LoggingUtils;
import org.onap.appc.provider.AppcProvider;
import org.onap.appc.provider.AppcProviderClient;
import org.onap.appc.provider.ResponseHeaderBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.rev160104.MigrateOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.rev160104.MigrateOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.rev160104.ModifyConfigOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.rev160104.ModifyConfigOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.rev160104.RebuildOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.rev160104.RebuildOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.rev160104.RestartOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.rev160104.RestartOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.rev160104.SnapshotOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.rev160104.SnapshotOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.rev160104.UUID;
import org.opendaylight.yang.gen.v1.org.onap.appc.rev160104.VmstatuscheckOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.rev160104.VmstatuscheckOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.rev160104.common.request.header.CommonRequestHeader;
import org.opendaylight.yang.gen.v1.org.onap.appc.rev160104.config.payload.ConfigPayload;
import org.opendaylight.yang.gen.v1.org.onap.appc.rev160104.vnf.resource.VnfResource;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.MDC;

/**
 * This class is used to implement the topology services API and invoke the appropriate directed graphs based on the
 * service being requested.
 */
public class TopologyService {

    /**
     * The loggers we are using
     */
    private final EELFLogger logger = EELFManager.getInstance().getApplicationLogger();
    private final EELFLogger auditLogger = EELFManager.getInstance().getAuditLogger();
    private final EELFLogger metricsLogger = EELFManager.getInstance().getMetricsLogger();
    private final EELFLogger performanceLogger = EELFManager.getInstance().getPerformanceLogger();
    private final static String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssX";
    private final static String START_TIME_PARAM = "startTime";
    private final static String RESTART_INITIATED_STR = "Metrics Logger: App-C Restart initiated. Start Time: [%s]. Request ID: [%s]";
    private final static String TARGET_PARAM = "target";
    private final static String SUCCESS_PARAM = "SUCCESS";
    private final static String FAILURE_PARAM = "FAILURE";
    private final static String END_TIME_PARAM = "endTime";
    private final static String DURATION_PARAM = "duration";
    private final static String ERROR_STR = "An error occurred";

    /**
     * The provider we are servicing
     */
    private AppcProvider provider;

    /**
     * The reason associated with the last DG call
     */
    private String reason;

    /**
     * The APPC configuration properties
     */
    private Configuration configuration = ConfigurationFactory.getConfiguration();

    /**
     * Create the topology services implementation for the specific appc provider (api) implementation
     *
     * @param provider The provider we are servicing
     */
    public TopologyService(AppcProvider provider) {
        this.provider = provider;
    }

    /**
     * Set MDC metric and audit logging configuration
     *
     * @param operation The operation being run
     * @param hdr The common request header
     * @param reqId RequestIf from common header
     * @return void
     */
    public void setMetricAndAuditConfig(String operation, String reqId) {

        try {
            MDC.put(MDC_KEY_REQUEST_ID, java.util.UUID.fromString(reqId).toString());
            //reaching here without exception means existing RequestId is
            //valid UUID as per ECOMP logging standards
        } catch (Exception e) {
            String reqIdUUID = java.util.UUID.randomUUID().toString();
            MDC.put(MDC_KEY_REQUEST_ID, reqIdUUID);
            logger.info("Replaced invalid requestID of " + reqId + ".  New value is " + reqIdUUID + ".");
        }

        String op = "App-C Provider:" + operation;
        MDC.put(MDC_REMOTE_HOST, "");
        MDC.put(MDC_SERVICE_NAME, op);
        MDC.put(MDC_SERVICE_INSTANCE_ID, "");
        try {
            MDC.put(MDC_SERVER_FQDN, InetAddress.getLocalHost().getHostName());
            MDC.put(MDC_SERVER_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress());
        } catch (Exception e) {
            logger.error(ERROR_STR, e);
        }
        MDC.put(MDC_INSTANCE_UUID, java.util.UUID.randomUUID().toString());
        MDC.put(MDC_ALERT_SEVERITY, "0");
        MDC.put(TARGET_PARAM, "appc");
        MDC.put(LoggingConstants.MDCKeys.PARTNER_NAME, "appc");
        MDC.put(LoggingConstants.MDCKeys.TARGET_ENTITY, "appc");
        MDC.put(LoggingConstants.MDCKeys.TARGET_SERVICE_NAME, op);
        MDC.put(LoggingConstants.MDCKeys.STATUS_CODE, "COMPLETE");
    }


    /**
     * Modify configuration
     *
     * @param hdr The common request header
     * @param data The payload of the configuration
     * @return The rpc result of the operation
     */
    public RpcResult<ModifyConfigOutput> modifyConfig(CommonRequestHeader hdr, ConfigPayload data) {
        String requestId = hdr.getServiceRequestId();
        logger.info(String.format("Starting RESTART for request with id [%s]", requestId));
        setMetricAndAuditConfig("Restart", requestId);
        Date startTimestamp = new Date();
        Instant startTimestampInstant = startTimestamp.toInstant();
        String startTimeStr = LoggingUtils.generateTimestampStr(startTimestampInstant);
        //For complete logging compliance, an initial end time and elapsed time are required
        MDC.put(LoggingConstants.MDCKeys.BEGIN_TIMESTAMP, startTimeStr);
        MDC.put(LoggingConstants.MDCKeys.END_TIMESTAMP, startTimeStr);
        MDC.put(LoggingConstants.MDCKeys.ELAPSED_TIME, "0");
        metricsLogger.info(String.format(RESTART_INITIATED_STR, startTimeStr, requestId));

        /*
         * Copy any needed inputs or other values into the properties to be passed to the DG model
         */
        Properties properties = new Properties();
        properties.put(Constants.CONTEXT_ACTION, "modifyConfig");
        properties.put(Constants.CONTEXT_REQID, requestId);
        String url = configuration.getProperty("appc.provider.vfodl.url");
        try {
            if (url.contains("NODE_NAME")) {
                url = url.replace("NODE_NAME", data.getConfigUrl());
            }
        } catch (Exception e) {
            logger.error("An error occurred when replacing node name", e);
            url = configuration.getProperty("appc.provider.vfodl.url");
        }
        logger.trace("Final URL to VF ODL: " + url);
        properties.put("org.onap.appc.configURL", url);
        properties.put("org.onap.appc.configJson", data.getConfigJson());

        /*
         * Attempt to call the DG with the appropriate properties
         */
        boolean success = callGraph(properties);

        String statusStr = success ? SUCCESS_PARAM : FAILURE_PARAM;
        String infomsg =
                String.format("APPC0119I ModifyConfig '%s' finished with status %s. Reason: %s", requestId, statusStr,
                        reason);
        logger.info(infomsg);

        ModifyConfigOutputBuilder rob = new ModifyConfigOutputBuilder();

        Date endTimestamp = new Date();
        Instant endTimestampInstant = endTimestamp.toInstant();
        String endTimeStr = LoggingUtils.generateTimestampStr(endTimestampInstant);
        long duration = ChronoUnit.MILLIS.between(startTimestampInstant, endTimestampInstant);
        String durationStr = String.valueOf(duration);

        MDC.put(LoggingConstants.MDCKeys.END_TIMESTAMP, endTimeStr);
        MDC.put(LoggingConstants.MDCKeys.ELAPSED_TIME, durationStr);
        rob.setCommonResponseHeader(ResponseHeaderBuilder.buildHeader(success, requestId, reason, duration));

        auditLogger.info(String.format(
                "Audit Logger: APPC0119I Restart '%s' finished with status %s. Start Time: [%s]. End Time: [%s]. Duration: [%s]. Request ID: [%s]. Reason:%s",
                requestId, statusStr, startTimeStr, endTimeStr, duration, requestId, reason));
        metricsLogger.info(String.format(
                "Metrics Logger: APPC0119I Restart '%s' finished with status %s. Start Time: [%s]. End Time: [%s]. Duration: [%s]. Request ID: [%s]. Reason:%s",
                requestId, statusStr, startTimeStr, endTimeStr, duration, requestId, reason));

        // Status must be set to true to indicate that our return is expected
        return RpcResultBuilder.<ModifyConfigOutput>status(true).withResult(rob.build()).build();
    }


    /**
     * Restart a VM
     *
     * @param hdr The common request header
     * @param vnf The identification of the VNF resource to be operated upon
     * @return The rpc result of the restart operation
     */
    public RpcResult<MigrateOutput> migrate(CommonRequestHeader hdr, VnfResource vnf) {
        String requestId = hdr.getServiceRequestId();
        logger.info(String.format("Starting MIGRATE for request with id [%s]", requestId));
        setMetricAndAuditConfig("Migrate", requestId);
        Date startTimestamp = new Date();
        Instant startTimestampInstant = startTimestamp.toInstant();
        String startTimeStr = LoggingUtils.generateTimestampStr(startTimestampInstant);
        //For complete logging compliance, an initial end time and elapsed time are required
        MDC.put(LoggingConstants.MDCKeys.BEGIN_TIMESTAMP, startTimeStr);
        MDC.put(LoggingConstants.MDCKeys.END_TIMESTAMP, startTimeStr);
        MDC.put(LoggingConstants.MDCKeys.ELAPSED_TIME, "0");

        metricsLogger.info(String.format(RESTART_INITIATED_STR,
                startTimeStr, requestId));

        /*
         * Copy any needed inputs or other values into the properties to be passed to the DG model
         */
        UUID vmId = vnf.getVmId();
        Properties properties = new Properties();
        properties.put(Constants.CONTEXT_ACTION, "migrate");
        properties.put(Constants.CONTEXT_REQID, requestId);
        properties.put(Constants.CONTEXT_VMID, vmId.getValue());

        UUID identityUrl = vnf.getIdentityUrl();
        if (identityUrl != null) {
            properties.put(Constants.CONTEXT_IDENTITY_URL, identityUrl.getValue());
        }

        /*
         * Generate the appropriate response
         */
        boolean success = callGraph(properties);

        String statusStr = success ? SUCCESS_PARAM : FAILURE_PARAM;
        String infomsg =
                String.format("APPC0118I Migrate '%s' finished with status %s. Reason: %s", requestId, statusStr, reason);
        logger.info(infomsg);

        MigrateOutputBuilder mob = new MigrateOutputBuilder();
        Date endTimestamp = new Date();
        Instant endTimestampInstant = endTimestamp.toInstant();
        String endTimeStr = LoggingUtils.generateTimestampStr(endTimestampInstant);
        long duration = ChronoUnit.MILLIS.between(startTimestampInstant, endTimestampInstant);
        String durationStr = String.valueOf(duration);

        MDC.put(LoggingConstants.MDCKeys.END_TIMESTAMP, endTimeStr);
        MDC.put(LoggingConstants.MDCKeys.ELAPSED_TIME, durationStr);
        mob.setCommonResponseHeader(ResponseHeaderBuilder.buildHeader(success, requestId, reason, duration));
        mob.setVmId(new UUID(vmId));

        auditLogger.info(String.format(
                "Audit Logger: APPC0118I Migrate '%s' finished with status %s. Start Time: [%s]. End Time: [%s]. Duration: [%s]. Request ID: [%s]. Reason:%s",
                requestId, statusStr, startTimeStr, endTimeStr, duration, requestId, reason));
        metricsLogger.info(String.format(
                "Metrics Logger: APPC0118I Migrate '%s' finished with status %s. Start Time: [%s]. End Time: [%s]. Duration: [%s]. Request ID: [%s]. Reason:%s",
                requestId, statusStr, startTimeStr, endTimeStr, duration, requestId, reason));

        // Status must be set to true to indicate that our return is expected
        return RpcResultBuilder.<MigrateOutput>status(true).withResult(mob.build()).build();
    }

    /**
     * Restart a VM
     *
     * @param hdr The common request header
     * @param vnf The identification of the VNF resource to be operated upon
     * @return The rpc result of the restart operation
     */
    public RpcResult<RestartOutput> restart(CommonRequestHeader hdr, VnfResource vnf) {
        String requestId = hdr.getServiceRequestId();
        logger.info(String.format("Starting RESTART for request with id [%s]", requestId));
        setMetricAndAuditConfig("Restart", requestId);
        Date startTimestamp = new Date();
        Instant startTimestampInstant = startTimestamp.toInstant();
        String startTimeStr = LoggingUtils.generateTimestampStr(startTimestampInstant);
        //For complete logging compliance, an initial end time and elapsed time are required
        MDC.put(LoggingConstants.MDCKeys.BEGIN_TIMESTAMP, startTimeStr);
        MDC.put(LoggingConstants.MDCKeys.END_TIMESTAMP, startTimeStr);
        MDC.put(LoggingConstants.MDCKeys.ELAPSED_TIME, "0");

        metricsLogger.info(String.format(RESTART_INITIATED_STR,
                startTimeStr, requestId));

        /*
         * Copy any needed inputs or other values into the properties to be passed to the DG model
         */
        UUID vmId = vnf.getVmId();
        Properties properties = new Properties();
        properties.put(Constants.CONTEXT_ACTION, "restart");
        properties.put(Constants.CONTEXT_REQID, requestId);
        properties.put(Constants.CONTEXT_VMID, vmId.getValue());

        UUID identityUrl = vnf.getIdentityUrl();
        if (identityUrl != null) {
            properties.put(Constants.CONTEXT_IDENTITY_URL, identityUrl.getValue());
        }
        /*
         * Attempt to call the DG with the appropriate properties
         */
        boolean success = callGraph(properties);

        /*
         * Generate the appropriate response
         */

        String statusStr = success ? SUCCESS_PARAM : FAILURE_PARAM;
        String infomsg =
                String.format("APPC0119I Restart '%s' finished with status %s. Reason: %s", requestId, statusStr, reason);
        logger.info(infomsg);

        RestartOutputBuilder rob = new RestartOutputBuilder();
        Date endTimestamp = new Date();
        Instant endTimestampInstant = endTimestamp.toInstant();
        String endTimeStr = LoggingUtils.generateTimestampStr(endTimestampInstant);
        long duration = ChronoUnit.MILLIS.between(startTimestampInstant, endTimestampInstant);
        String durationStr = String.valueOf(duration);

        MDC.put(LoggingConstants.MDCKeys.END_TIMESTAMP, endTimeStr);
        MDC.put(LoggingConstants.MDCKeys.ELAPSED_TIME, durationStr);
        rob.setCommonResponseHeader(ResponseHeaderBuilder.buildHeader(success, requestId, reason, duration));
        rob.setVmId(new UUID(vmId));

        auditLogger.info(String.format(
                "Audit Logger: APPC0119I Restart '%s' finished with status %s. Start Time: [%s]. End Time: [%s]. Duration: [%s]. Request ID: [%s]. Reason:%s",
                requestId, statusStr, startTimeStr, endTimeStr, duration, requestId, reason));
        metricsLogger.info(String.format(
                "Metrics Logger: APPC0119I Restart '%s' finished with status %s. Start Time: [%s]. End Time: [%s]. Duration: [%s]. Request ID: [%s]. Reason:%s",
                requestId, statusStr, startTimeStr, endTimeStr, duration, requestId, reason));

        // Status must be set to true to indicate that our return is expected
        return RpcResultBuilder.<RestartOutput>status(true).withResult(rob.build()).build();
    }

    /**
     * Rebuild a VM
     *
     * @param hdr The common request header
     * @param vnf The identification of the VNF resource to be operated upon
     * @return The rpc result of the rebuild operation
     */
    public RpcResult<RebuildOutput> rebuild(CommonRequestHeader hdr, VnfResource vnf) {
        String requestId = hdr.getServiceRequestId();

        logger.info(String.format("Starting REBUILD for request with id [%s]", requestId));
        setMetricAndAuditConfig("Rebuild", requestId);
        Date startTimestamp = new Date();
        Instant startTimestampInstant = startTimestamp.toInstant();
        String startTimeStr = LoggingUtils.generateTimestampStr(startTimestampInstant);
        //For complete logging compliance, an initial end time and elapsed time are required
        MDC.put(LoggingConstants.MDCKeys.BEGIN_TIMESTAMP, startTimeStr);
        MDC.put(LoggingConstants.MDCKeys.END_TIMESTAMP, startTimeStr);
        MDC.put(LoggingConstants.MDCKeys.ELAPSED_TIME, "0");

        metricsLogger.info(String.format(RESTART_INITIATED_STR,
                startTimeStr, requestId));

        /*
         * Copy any needed inputs or other values into the properties to be passed to the DG model
         */
        UUID vmId = vnf.getVmId();
        Properties properties = new Properties();
        properties.put(Constants.CONTEXT_ACTION, "rebuild");
        properties.put(Constants.CONTEXT_REQID, requestId);
        properties.put(Constants.CONTEXT_VMID, vmId.getValue());

        UUID identityUrl = vnf.getIdentityUrl();
        if (identityUrl != null) {
            properties.put(Constants.CONTEXT_IDENTITY_URL, identityUrl.getValue());
        }

        /*
         * Attempt to call the DG with the appropriate properties
         */
        boolean success = callGraph(properties);

        /*
         * Generate the appropriate response
         */
        MDC.put(TARGET_PARAM, "appc");
        String statusStr = success ? SUCCESS_PARAM : FAILURE_PARAM;
        String infomsg =
                String.format("APPC0120I Rebuild '%s' finished with status %s. Reason: %s", requestId, statusStr, reason);
        logger.info(infomsg);

        RebuildOutputBuilder rob = new RebuildOutputBuilder();
        Date endTimestamp = new Date();
        Instant endTimestampInstant = endTimestamp.toInstant();
        String endTimeStr = LoggingUtils.generateTimestampStr(endTimestampInstant);
        long duration = ChronoUnit.MILLIS.between(startTimestampInstant, endTimestampInstant);
        String durationStr = String.valueOf(duration);

        MDC.put(LoggingConstants.MDCKeys.END_TIMESTAMP, endTimeStr);
        MDC.put(LoggingConstants.MDCKeys.ELAPSED_TIME, durationStr);
        rob.setCommonResponseHeader(ResponseHeaderBuilder.buildHeader(success, requestId, reason, duration));
        rob.setOriginalVmId(new UUID(vmId));
        rob.setNewVmId(new UUID(vmId));

        auditLogger.info(String.format(
                "Audit Logger: APPC0120I Rebuild '%s' finished with status %s. Start Time: [%s]. End Time: [%s]. Duration: [%s]. Request ID: [%s]. Reason:%s",
                requestId, statusStr, startTimeStr, endTimeStr, duration, requestId, reason));
        metricsLogger.info(String.format(
                "Metrics Logger: APPC0120I Rebuild '%s' finished with status %s. Start Time: [%s]. End Time: [%s].  Duration: [%s]. Request ID: [%s]. Reason:%s",
                requestId, statusStr, startTimeStr, endTimeStr, duration, requestId, reason));

        // Status must be set to true to indicate that our return is expected
        return RpcResultBuilder.<RebuildOutput>status(true).withResult(rob.build()).build();
    }

    /**
     * Snapshot a VM
     *
     * @param hdr The common request header
     * @param vnf The identification of the VNF resource to be operated upon
     * @return The rpc result of the restart operation
     */
    public RpcResult<SnapshotOutput> snapshot(CommonRequestHeader hdr, VnfResource vnf) {
        String requestId = hdr.getServiceRequestId();
        logger.info(String.format("Starting SNAPSHOT for request with id [%s]", requestId));
        setMetricAndAuditConfig("Snapshot", requestId);
        Date startTimestamp = new Date();
        Instant startTimestampInstant = startTimestamp.toInstant();
        String startTimeStr = LoggingUtils.generateTimestampStr(startTimestampInstant);
        //For complete logging compliance, an initial end time and elapsed time are required
        MDC.put(LoggingConstants.MDCKeys.BEGIN_TIMESTAMP, startTimeStr);
        MDC.put(LoggingConstants.MDCKeys.END_TIMESTAMP, startTimeStr);
        MDC.put(LoggingConstants.MDCKeys.ELAPSED_TIME, "0");

        metricsLogger.info(String.format("Metrics Logger: App-C Snapshot initiated. Start Time: [%s]. Request ID: [%s]",
                startTimeStr, requestId));

        /*
         * Copy any needed inputs or other values into the properties to be passed to the DG model
         */
        UUID vmId = vnf.getVmId();
        Properties properties = new Properties();
        properties.put(Constants.CONTEXT_ACTION, "snapshot");
        properties.put(Constants.CONTEXT_REQID, requestId);
        properties.put(Constants.CONTEXT_VMID, vmId.getValue());

        UUID identityUrl = vnf.getIdentityUrl();
        if (identityUrl != null) {
            properties.put(Constants.CONTEXT_IDENTITY_URL, identityUrl.getValue());
        }
        /*
         * Attempt to call the DG with the appropriate properties
         */
        boolean success = callGraph(properties);

        /*
         * Generate the appropriate response
         */
        String statusStr = success ? SUCCESS_PARAM : FAILURE_PARAM;
        String infomsg =
                String.format("APPC0119I Snapshot '%s' finished with status %s. Reason: %s", requestId, statusStr, reason);
        logger.info(infomsg);

        SnapshotOutputBuilder sob = new SnapshotOutputBuilder();
        Date endTimestamp = new Date();
        Instant endTimestampInstant = endTimestamp.toInstant();
        String endTimeStr = LoggingUtils.generateTimestampStr(endTimestampInstant);
        long duration = ChronoUnit.MILLIS.between(startTimestampInstant, endTimestampInstant);
        String durationStr = String.valueOf(duration);

        MDC.put(LoggingConstants.MDCKeys.END_TIMESTAMP, endTimeStr);
        MDC.put(LoggingConstants.MDCKeys.ELAPSED_TIME, durationStr);
        sob.setCommonResponseHeader(ResponseHeaderBuilder.buildHeader(success, requestId, reason, duration));
        sob.setVmId(new UUID(vmId));

        auditLogger.info(String.format(
                "Audit Logger: APPC0119I Snapshot '%s' finished with status %s. Start Time: [%s]. End Time: [%s]. Duration: [%s]. Request ID: [%s]. Reason:%s",
                requestId, statusStr, startTimeStr, endTimeStr, duration, requestId, reason));
        metricsLogger.info(String.format(
                "Metrics Logger: APPC0119I Snapshot '%s' finished with status %s. Start Time: [%s]. End Time: [%s]. Duration: [%s]. Request ID: [%s]. Reason:%s",
                requestId, statusStr, startTimeStr, endTimeStr, duration, requestId, reason));

        // Status must be set to true to indicate that our return is expected
        return RpcResultBuilder.<SnapshotOutput>status(true).withResult(sob.build()).build();
    }

    /**************************************************/

    public RpcResult<VmstatuscheckOutput> vmstatuscheck(CommonRequestHeader hdr, VnfResource vnf) {        long startTime = System.currentTimeMillis();
        String requestId = hdr.getServiceRequestId();
        logger.info(String.format("Starting VMSTATUSCHECK for request with id [%s]", requestId));
        MDC.clear();
        setMetricAndAuditConfig("vmstatuscheck", requestId);
        Date startTimestamp = new Date();
        Instant startTimestampInstant = startTimestamp.toInstant();
        String startTimeStr = LoggingUtils.generateTimestampStr(startTimestampInstant);
        //For complete logging compliance, an initial end time and elapsed time are required
        MDC.put(LoggingConstants.MDCKeys.BEGIN_TIMESTAMP, startTimeStr);
        MDC.put(LoggingConstants.MDCKeys.END_TIMESTAMP, startTimeStr);
        MDC.put(LoggingConstants.MDCKeys.ELAPSED_TIME, "0");

        performanceLogger.info(String
                .format("Performance Logger: App-C vmstatuscheck initiated. Start Time: [%s]. Request ID: [%s]", startTime,
                        requestId));
        auditLogger.info(String
                .format("Audit Logger: App-C vmstatuscheck initiated. Start Time: [%s]. Request ID: [%s]", startTime,
                        requestId));
        metricsLogger.info(String
                .format("Metrics Logger: App-C vmstatuscheck initiated. Start Time: [%s]. Request ID: [%s]", startTime,
                        requestId));

        /*
         * Copy any needed inputs or other values into the properties to be passed to the DG model
         */
        UUID vmId = vnf.getVmId();
        Properties properties = new Properties();
        properties.put(Constants.CONTEXT_ACTION, "vmstatuschecking");
        properties.put(Constants.CONTEXT_REQID, requestId);
        properties.put(Constants.CONTEXT_VMID, vmId.getValue());
        properties.put(Constants.STATUS_GETTER, "checking");

        UUID identityUrl = vnf.getIdentityUrl();
        if (identityUrl != null) {
            properties.put(Constants.CONTEXT_IDENTITY_URL, identityUrl.getValue());
        }
        /*
         * Attempt to call the DG with the appropriate properties
         */
        boolean success = callGraph(properties);

        /*
         * Generate the appropriate response
         */
        String statusStr = success ? SUCCESS_PARAM : FAILURE_PARAM;
        String infomsg =
                String.format("VMSTATUSCHECK '%s' finished with status %s. Reason: %s", requestId, statusStr, reason);
        logger.info(infomsg);

        Date endTimestamp = new Date();
        Instant endTimestampInstant = endTimestamp.toInstant();
        String endTimeStr = LoggingUtils.generateTimestampStr(endTimestampInstant);
        long duration = ChronoUnit.MILLIS.between(startTimestampInstant, endTimestampInstant);
        String durationStr = String.valueOf(duration);

        MDC.put(LoggingConstants.MDCKeys.END_TIMESTAMP, endTimeStr);
        MDC.put(LoggingConstants.MDCKeys.ELAPSED_TIME, durationStr);

        auditLogger.info(String.format(
                "Audit Logger: VMSTATUSCHECK '%s' finished with status %s. Start Time: [%s]. End Time: [%s]. Request ID: [%s]. Reason:%s",
                requestId, statusStr, startTime, endTimeStr, requestId, reason));
        metricsLogger.info(String.format(
                "Metrics Logger: VMSTATUSCHECK '%s' finished with status %s. Start Time: [%s]. End Time: [%s]. Request ID: [%s]. Reason:%s",
                requestId, statusStr, startTime, endTimeStr, requestId, reason));

        String tempstring2 = properties.getProperty(Constants.STATUS_GETTER).trim();

        VmstatuscheckOutputBuilder vob = new VmstatuscheckOutputBuilder();
        vob.setCommonResponseHeader(ResponseHeaderBuilder.buildHeader(success, requestId, reason, duration));
        vob.setStatMsg(tempstring2);

        // Status must be set to true to indicate that our return is expected
        return RpcResultBuilder.<VmstatuscheckOutput>status(true).withResult(vob.build()).build();
    }

    /*************************************************/


    private boolean callGraph(Properties props) {
        String moduleName = configuration.getProperty(Constants.PROPERTY_MODULE_NAME);
        String methodName = configuration.getProperty(Constants.PROPERTY_TOPOLOGY_METHOD);
        String version = configuration.getProperty(Constants.PROPERTY_TOPOLOGY_VERSION);
        String mode = Constants.SYNC_MODE;
        return callGraph(moduleName, methodName, version, mode, props);
    }

    /**
     * Calls a specified directed graph with the specified properties and returns the response
     *
     * @param module The module name to be used to locate the graph
     * @param method The method name to be executed (rpc)
     * @param version The version of the graph to be used, or null for the latest
     * @param mode the execution mode of the graph, sync or async
     * @param props A set of name-value properties to be passed to the graph for context variables.
     */
    private boolean callGraph(String module, String method, String version, String mode, Properties props) {
        String graphName = String.format(("%s:%s:%s"), module, method, version);
        logger.debug(String.format("Calling Graph %s", graphName));
        metricsLogger.info(String.format("Calling Graph %s", graphName));

        boolean success;
        String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);
        AppcProviderClient svcLogicClient = new AppcProviderClient();
        try {
            if (svcLogicClient.hasGraph(module, method, version, mode)) {
                try {
                    Properties respProps = svcLogicClient.execute(module, method, version, mode, props);
                    reason = "Failed";      // Assume it failed unless proven otherwise
                    logger.debug(EELFResourceManager.format(Msg.DEBUG_GRAPH_RESPONSE_HEADER, appName, graphName,
                            Integer.toString(respProps.size())));
                    logKeys(graphName, appName, respProps);
                    success = resolveSuccess(graphName, appName, respProps);
                } catch (Exception e) {
                    success = false;
                    reason = EELFResourceManager.format(Msg.EXCEPTION_CALLING_DG, e, appName,
                            e.getClass().getSimpleName(), graphName, e.getMessage());
                    logger.error(reason);
                }
            } else {
                success = false;
                reason = EELFResourceManager.format(Msg.GRAPH_NOT_FOUND, appName, graphName);
                logger.error(reason);
            }
        } catch (Exception e) {
            success = false;
            reason = EELFResourceManager.format(Msg.EXCEPTION_CALLING_DG, e, appName, e.getClass().getSimpleName(),
                    graphName, e.getMessage());
            logger.error(reason);
        }

        return success;
    }

    private boolean resolveSuccess(String graphName, String appName, Properties respProps) {
        // TODO - Find docs and see if there is a better way to handle this
        // Bad requests have errors
        if (respProps.containsKey(Constants.ATTRIBUTE_ERROR_CODE)) {
            String errorCodeProperty = respProps.getProperty(Constants.ATTRIBUTE_ERROR_CODE).trim();
            return doResolveSuccess(graphName, appName, respProps, errorCodeProperty);
        } else {
            /*
             * Added code that requires error code to now be defined in ALL cases. If not, it is an error
             * and the response will be set to failed regardless if the DG worked or not.
             */
            reason = EELFResourceManager.format(Msg.PARAMETER_IS_MISSING, appName, graphName,
                    Constants.ATTRIBUTE_ERROR_CODE);
            logger.error(reason);
            return false;
        }
    }

    private boolean doResolveSuccess(String graphName, String appName, Properties respProps, String errorCodeProperty) {

        try {
            int errorCode = Integer.parseInt(errorCodeProperty);
            if (errorCode >= 300) {
                reason = EELFResourceManager.format(Msg.DG_FAILED_RESPONSE, appName, graphName,
                        errorCodeProperty, respProps.getProperty(Constants.ATTRIBUTE_ERROR_MESSAGE));
                logger.error(reason);
                return false;
            } else {

                reason = "Success";
                return true;
            }
        } catch (NumberFormatException e) {
            reason = EELFResourceManager.format(Msg.PARAMETER_NOT_NUMERIC, appName, graphName,
                    Constants.ATTRIBUTE_ERROR_CODE, errorCodeProperty);
            logger.error(reason);
            return false;
        }
    }

    private void logKeys(String graphName, String appName, Properties respProps) {
        for (String key : respProps.stringPropertyNames()) {
            logger.debug(EELFResourceManager.format(
                    Msg.DEBUG_GRAPH_RESPONSE_DETAIL, appName, graphName, key, (String) respProps.get(key)));
        }
    }

}

