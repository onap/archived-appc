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

package org.openecomp.appc.provider.topology;

import static com.att.eelf.configuration.Configuration.MDC_ALERT_SEVERITY;
import static com.att.eelf.configuration.Configuration.MDC_INSTANCE_UUID;
import static com.att.eelf.configuration.Configuration.MDC_KEY_REQUEST_ID;
import static com.att.eelf.configuration.Configuration.MDC_REMOTE_HOST;
import static com.att.eelf.configuration.Configuration.MDC_SERVER_FQDN;
import static com.att.eelf.configuration.Configuration.MDC_SERVER_IP_ADDRESS;
import static com.att.eelf.configuration.Configuration.MDC_SERVICE_INSTANCE_ID;
import static com.att.eelf.configuration.Configuration.MDC_SERVICE_NAME;

import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;

import org.opendaylight.yang.gen.v1.org.openecomp.appc.rev160104.MigrateOutput;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.rev160104.ModifyConfigOutput;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.rev160104.ModifyConfigOutputBuilder;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.rev160104.MigrateOutputBuilder;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.rev160104.RebuildOutput;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.rev160104.RebuildOutputBuilder;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.rev160104.RestartOutput;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.rev160104.RestartOutputBuilder;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.rev160104.SnapshotOutput;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.rev160104.SnapshotOutputBuilder;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.rev160104.UUID;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.rev160104.common.request.header.CommonRequestHeader;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.rev160104.vnf.resource.VnfResource;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.rev160104.config.payload.ConfigPayload;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.rev160104.VmstatuscheckOutput;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.rev160104.VmstatuscheckOutputBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.openecomp.appc.Constants;
import org.openecomp.appc.configuration.Configuration;
import org.openecomp.appc.configuration.ConfigurationFactory;
import org.openecomp.appc.i18n.Msg;
import org.openecomp.appc.provider.AppcProvider;
import org.openecomp.appc.provider.AppcProviderClient;
import org.openecomp.appc.provider.ResponseHeaderBuilder;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import org.slf4j.MDC;

/**
 * This class is used to implement the topology services API and invoke the appropriate directed graphs based on the
 * service being requested.
 * 
 */
public class TopologyService {

    /**
     * The loggers we are using
     */
    // private static EELFLogger logger = LoggerFactory.getLogger(TopologyService.class);
    private static EELFLogger logger = EELFManager.getInstance().getApplicationLogger();
    private static EELFLogger securityLogger = EELFManager.getInstance().getSecurityLogger();
    private static EELFLogger auditLogger = EELFManager.getInstance().getAuditLogger();
    private static EELFLogger metricsLogger = EELFManager.getInstance().getMetricsLogger();
    private static EELFLogger performanceLogger = EELFManager.getInstance().getPerformanceLogger();

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
     * @param provider
     *            The provider we are servicing
     */
    public TopologyService(AppcProvider provider) {
        this.provider = provider;
    }

    // /**
    // * Processes the topology request
    // *
    // * @param input
    // * The request to be processed
    // * @return The result of processing
    // */
    // public RpcResult<TopologyOperationOutput> process(TopologyOperationInput input) {
    // RpcResult<TopologyOperationOutput> response;
    //
    // String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);
    // logger.info(String.format("%s:topology operations called...", appName));
    //
    // /*
    // * Properties used to pass information to the DG
    // */
    // Properties properties = new Properties();
    //
    // if (input == null || input.getTopologyRequest().getVmId() == null) {
    // String msg =
    // String.format("%s: topology operation failed, invalid input. Null or empty argument '%s'", appName,
    // "vm_id");
    // logger.debug(msg);
    // response = generateTopologyOperationResponse(Boolean.FALSE, "UNKNOWN", msg, "UNDEFINED");
    // } else {
    // // CommonRequestHeader crh = input.getCommonRequestHeader();
    // TopologyHeader hdr = input.getTopologyHeader();
    // TopologyRequest req = input.getTopologyRequest();
    //
    // // String requestId = crh.getServiceRequestId();
    // String requestId = hdr.getSvcRequestId();
    // properties.put(Constants.CONTEXT_REQID, requestId);
    //
    // String infomsg = String.format("Topology request '%s' (%s) received.", requestId, hdr.getSvcAction());
    //
    // // switch (req.getSvcAction()) {
    // switch (hdr.getSvcAction()) {
    // case Restart:
    // properties.put(Constants.CONTEXT_SERVICE, Constants.SERVICE_RESTART);
    // response = restart(input, properties);
    // logger.info(infomsg);
    // break;
    //
    // case Rebuild:
    // properties.put(Constants.CONTEXT_SERVICE, Constants.SERVICE_REBUILD);
    // response = rebuild(input, properties);
    // logger.info(infomsg);
    // break;
    //
    // default:
    // String msg = String.format("Invalid request type [%s] for request id [%s]", req, requestId);
    // response = generateTopologyOperationResponse(Boolean.FALSE, requestId, msg, "N/A");
    // }
    // }
    //
    // return response;
    // }

    /**
     * Restart a VM
     * 
     * @param hdr
     *            The common request header
     * @param vnf
     *            The identification of the VNF resource to be operated upon
     * @return The rpc result of the restart operation
     */
    public RpcResult<ModifyConfigOutput> modifyConfig(CommonRequestHeader hdr, ConfigPayload data) {
        long startTime = System.currentTimeMillis();
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
        df.setTimeZone(tz);
        // String startTimeStr = String.valueOf(startTime);
        String startTimeStr = df.format(new Date());
        String requestId = hdr.getServiceRequestId();

        //MDC.clear();
        MDC.put(MDC_REMOTE_HOST, "");
        MDC.put(MDC_KEY_REQUEST_ID, requestId);
        MDC.put(MDC_SERVICE_NAME, "App-C Provider:Restart");
        MDC.put(MDC_SERVICE_INSTANCE_ID, "");
        try {
            MDC.put(MDC_SERVER_FQDN, InetAddress.getLocalHost().getHostName());
            MDC.put(MDC_SERVER_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress());
        } catch (Exception e) {
            e.printStackTrace();
        }
        MDC.put(MDC_INSTANCE_UUID, java.util.UUID.randomUUID().toString());
        MDC.put(MDC_ALERT_SEVERITY, "0");
        MDC.put("startTime", Long.toString(startTime));
        MDC.put("target", "appc");
        logger.info(String.format("Starting RESTART for request with id [%s]", requestId));
        metricsLogger.info(String.format("Metrics Logger: App-C Restart initiated. Start Time: [%s]. Request ID: [%s]",
            startTime, requestId));

        /*
         * Copy any needed inputs or other values into the properties to be passed to the DG model
         */
        //UUID vmId = vnf.getVmId();
        Properties properties = new Properties();
        properties.put(Constants.CONTEXT_ACTION, "modifyConfig");
        properties.put(Constants.CONTEXT_REQID, requestId);
        //properties.put(Constants.CONTEXT_VMID, vmId.getValue());
        String url = configuration.getProperty("appc.provider.vfodl.url");
        try{
        if(url.contains("NODE_NAME")){
        	url = url.replace("NODE_NAME", data.getConfigUrl());
        }
        }catch(Exception e){
        	url = configuration.getProperty("appc.provider.vfodl.url");
        }
        logger.trace("Final URL to VF ODL: "+url);
        properties.put("org.openecomp.appc.configURL", url);
        properties.put("org.openecomp.appc.configJson", data.getConfigJson());

        //UUID identityUrl = vnf.getIdentityUrl();
        //if (identityUrl != null) {
        //    properties.put(Constants.CONTEXT_IDENTITY_URL, identityUrl.getValue());
        //}
        /*
         * Attempt to call the DG with the appropriate properties
         */
        boolean success = callGraph(properties);

 
        MDC.put("target", "appc");
        String statusStr = success ? "SUCCESS" : "FAILURE";
        String infomsg =
            String.format("APPC0119I ModifyConfig '%s' finished with status %s. Reason: %s", requestId, statusStr, reason);
        logger.info(infomsg);

        ModifyConfigOutputBuilder rob = new ModifyConfigOutputBuilder();
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        String endTimeStr = String.valueOf(endTime);
        String durationStr = String.valueOf(duration);
        MDC.put("endTime", endTimeStr);
        MDC.put("duration", durationStr);
        rob.setCommonResponseHeader(ResponseHeaderBuilder.buildHeader(success, requestId, reason, duration));
        //rob.setVmId(new UUID(vmId));

        auditLogger.info(String.format(
            "Audit Logger: APPC0119I Restart '%s' finished with status %s. Start Time: [%s]. End Time: [%s]. Duration: [%s]. Request ID: [%s]. Reason:%s",
            requestId, statusStr, startTime, endTime, duration, requestId, reason));
        metricsLogger.info(String.format(
            "Metrics Logger: APPC0119I Restart '%s' finished with status %s. Start Time: [%s]. End Time: [%s]. Duration: [%s]. Request ID: [%s]. Reason:%s",
            requestId, statusStr, startTime, endTime, duration, requestId, reason));

        // Status must be set to true to indicate that our return is expected
        RpcResult<ModifyConfigOutput> rpcResult =
            RpcResultBuilder.<ModifyConfigOutput> status(true).withResult(rob.build()).build();
        return rpcResult;
        
    }
    
    
    
    
    /**
     * Restart a VM
     * 
     * @param hdr
     *            The common request header
     * @param vnf
     *            The identification of the VNF resource to be operated upon
     * @return The rpc result of the restart operation
     */
    public RpcResult<MigrateOutput> migrate(CommonRequestHeader hdr, VnfResource vnf) {
        long startTime = System.currentTimeMillis();
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
        df.setTimeZone(tz);
        // String startTimeStr = String.valueOf(startTime);
        String startTimeStr = df.format(new Date());
        String requestId = hdr.getServiceRequestId();

        //MDC.clear();
        MDC.put(MDC_REMOTE_HOST, "");
        MDC.put(MDC_KEY_REQUEST_ID, requestId);
        MDC.put(MDC_SERVICE_NAME, "App-C Provider:Migrate");
        MDC.put(MDC_SERVICE_INSTANCE_ID, "");
        try {
            MDC.put(MDC_SERVER_FQDN, InetAddress.getLocalHost().getHostName());
            MDC.put(MDC_SERVER_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress());
        } catch (Exception e) {
            e.printStackTrace();
        }
        MDC.put(MDC_INSTANCE_UUID, java.util.UUID.randomUUID().toString());
        MDC.put(MDC_ALERT_SEVERITY, "0");
        MDC.put("startTime", startTimeStr);
        MDC.put("target", "appc");
        logger.info(String.format("Starting ANY for request with id [%s]", requestId));
        metricsLogger.info(String.format("Metrics Logger: App-C Restart initiated. Start Time: [%s]. Request ID: [%s]",
            startTime, requestId));

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
         * Attempt to call the DG with the appropriate properties
         */
        boolean success = callGraph(properties);

        /*
         * Generate the appropriate response
         */
        MDC.put("target", "appc");
        String statusStr = success ? "SUCCESS" : "FAILURE";
        String infomsg =
            String.format("APPC0118I Migrate '%s' finished with status %s. Reason: %s", requestId, statusStr, reason);
        logger.info(infomsg);

        MigrateOutputBuilder mob = new MigrateOutputBuilder();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        String endTimeStr = String.valueOf(endTime);
        String durationStr = String.valueOf(duration);
        MDC.put("endTime", endTimeStr);
        MDC.put("duration", durationStr);
        mob.setCommonResponseHeader(ResponseHeaderBuilder.buildHeader(success, requestId, reason, duration));
        mob.setVmId(new UUID(vmId));

        auditLogger.info(String.format(
            "Audit Logger: APPC0118I Migrate '%s' finished with status %s. Start Time: [%s]. End Time: [%s]. Duration: [%s]. Request ID: [%s]. Reason:%s",
            requestId, statusStr, startTime, endTime, duration, requestId, reason));
        metricsLogger.info(String.format(
            "Metrics Logger: APPC0118I Migrate '%s' finished with status %s. Start Time: [%s]. End Time: [%s]. Duration: [%s]. Request ID: [%s]. Reason:%s",
            requestId, statusStr, startTime, endTime, duration, requestId, reason));

        // Status must be set to true to indicate that our return is expected
        RpcResult<MigrateOutput> rpcResult =
            RpcResultBuilder.<MigrateOutput> status(true).withResult(mob.build()).build();
        return rpcResult;
    }

    /**
     * Restart a VM
     * 
     * @param hdr
     *            The common request header
     * @param vnf
     *            The identification of the VNF resource to be operated upon
     * @return The rpc result of the restart operation
     */
    public RpcResult<RestartOutput> restart(CommonRequestHeader hdr, VnfResource vnf) {
        long startTime = System.currentTimeMillis();
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
        df.setTimeZone(tz);
        // String startTimeStr = String.valueOf(startTime);
        String startTimeStr = df.format(new Date());
        String requestId = hdr.getServiceRequestId();

        //MDC.clear();
        MDC.put(MDC_REMOTE_HOST, "");
        MDC.put(MDC_KEY_REQUEST_ID, requestId);
        MDC.put(MDC_SERVICE_NAME, "App-C Provider:Restart");
        MDC.put(MDC_SERVICE_INSTANCE_ID, "");
        try {
            MDC.put(MDC_SERVER_FQDN, InetAddress.getLocalHost().getHostName());
            MDC.put(MDC_SERVER_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress());
        } catch (Exception e) {
            e.printStackTrace();
        }
        MDC.put(MDC_INSTANCE_UUID, java.util.UUID.randomUUID().toString());
        MDC.put(MDC_ALERT_SEVERITY, "0");
        MDC.put("startTime", Long.toString(startTime));
        MDC.put("target", "appc");
        logger.info(String.format("Starting RESTART for request with id [%s]", requestId));
        metricsLogger.info(String.format("Metrics Logger: App-C Restart initiated. Start Time: [%s]. Request ID: [%s]",
            startTime, requestId));

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
        MDC.put("target", "appc");
        String statusStr = success ? "SUCCESS" : "FAILURE";
        String infomsg =
            String.format("APPC0119I Restart '%s' finished with status %s. Reason: %s", requestId, statusStr, reason);
        logger.info(infomsg);

        RestartOutputBuilder rob = new RestartOutputBuilder();
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        String endTimeStr = String.valueOf(endTime);
        String durationStr = String.valueOf(duration);
        MDC.put("endTime", endTimeStr);
        MDC.put("duration", durationStr);
        rob.setCommonResponseHeader(ResponseHeaderBuilder.buildHeader(success, requestId, reason, duration));
        rob.setVmId(new UUID(vmId));

        auditLogger.info(String.format(
            "Audit Logger: APPC0119I Restart '%s' finished with status %s. Start Time: [%s]. End Time: [%s]. Duration: [%s]. Request ID: [%s]. Reason:%s",
            requestId, statusStr, startTime, endTime, duration, requestId, reason));
        metricsLogger.info(String.format(
            "Metrics Logger: APPC0119I Restart '%s' finished with status %s. Start Time: [%s]. End Time: [%s]. Duration: [%s]. Request ID: [%s]. Reason:%s",
            requestId, statusStr, startTime, endTime, duration, requestId, reason));

        // Status must be set to true to indicate that our return is expected
        RpcResult<RestartOutput> rpcResult =
            RpcResultBuilder.<RestartOutput> status(true).withResult(rob.build()).build();
        return rpcResult;
    }

    /**
     * Rebuild a VM
     * 
     * @param hdr
     *            The common request header
     * @param vnf
     *            The identification of the VNF resource to be operated upon
     * @return The rpc result of the rebuild operation
     */
    public RpcResult<RebuildOutput> rebuild(CommonRequestHeader hdr, VnfResource vnf) {
        long startTime = System.currentTimeMillis();
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
        df.setTimeZone(tz);
        // String startTimeStr = String.valueOf(startTime);
        String startTimeStr = df.format(new Date());
        String requestId = hdr.getServiceRequestId();

        //MDC.clear();
        MDC.put(MDC_REMOTE_HOST, "");
        MDC.put(MDC_KEY_REQUEST_ID, requestId);
        MDC.put(MDC_SERVICE_NAME, "App-C Provider:Rebuild");
        MDC.put(MDC_SERVICE_INSTANCE_ID, "");
        try {
            MDC.put(MDC_SERVER_FQDN, InetAddress.getLocalHost().getHostName());
            MDC.put(MDC_SERVER_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress());
        } catch (Exception e) {
            e.printStackTrace();
        }
        MDC.put(MDC_INSTANCE_UUID, java.util.UUID.randomUUID().toString());
        MDC.put(MDC_ALERT_SEVERITY, "0");
        MDC.put("startTime", startTimeStr);
        MDC.put("target", "appc");
        logger.info(String.format("Starting REBUILD for request with id [%s]", requestId));
        metricsLogger.info(String.format("Metrics Logger: App-C Restart initiated. Start Time: [%s]. Request ID: [%s]",
            startTime, requestId));

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
        MDC.put("target", "appc");
        String statusStr = success ? "SUCCESS" : "FAILURE";
        String infomsg =
            String.format("APPC0120I Rebuild '%s' finished with status %s. Reason: %s", requestId, statusStr, reason);
        logger.info(infomsg);

        RebuildOutputBuilder rob = new RebuildOutputBuilder();
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        String endTimeStr = String.valueOf(endTime);
        String durationStr = String.valueOf(duration);
        MDC.put("endTime", endTimeStr);
        MDC.put("duration", durationStr);
        rob.setCommonResponseHeader(ResponseHeaderBuilder.buildHeader(success, requestId, reason, duration));
        rob.setOriginalVmId(new UUID(vmId));
        rob.setNewVmId(new UUID(vmId));

        auditLogger.info(String.format(
            "Audit Logger: APPC0120I Rebuild '%s' finished with status %s. Start Time: [%s]. End Time: [%s]. Duration: [%s]. Request ID: [%s]. Reason:%s",
            requestId, statusStr, startTime, endTime, duration, requestId, reason));
        metricsLogger.info(String.format(
            "Metrics Logger: APPC0120I Rebuild '%s' finished with status %s. Start Time: [%s]. End Time: [%s].  Duration: [%s]. Request ID: [%s]. Reason:%s",
            requestId, statusStr, startTime, endTime, duration, requestId, reason));

        // Status must be set to true to indicate that our return is expected
        RpcResult<RebuildOutput> rpcResult =
            RpcResultBuilder.<RebuildOutput> status(true).withResult(rob.build()).build();
        return rpcResult;
    }

    /**
     * Snapshot a VM
     * 
     * @param hdr
     *            The common request header
     * @param vnf
     *            The identification of the VNF resource to be operated upon
     * @return The rpc result of the restart operation
     */
    public RpcResult<SnapshotOutput> snapshot(CommonRequestHeader hdr, VnfResource vnf) {
        long startTime = System.currentTimeMillis();
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
        df.setTimeZone(tz);
        // String startTimeStr = String.valueOf(startTime);
        String startTimeStr = df.format(new Date());
        String requestId = hdr.getServiceRequestId();

        //MDC.clear();
        MDC.put(MDC_REMOTE_HOST, "");
        MDC.put(MDC_KEY_REQUEST_ID, requestId);
        MDC.put(MDC_SERVICE_NAME, "App-C Provider:Snapshot");
        MDC.put(MDC_SERVICE_INSTANCE_ID, "");
        try {
            MDC.put(MDC_SERVER_FQDN, InetAddress.getLocalHost().getHostName());
            MDC.put(MDC_SERVER_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress());
        } catch (Exception e) {
            e.printStackTrace();
        }
        MDC.put(MDC_INSTANCE_UUID, java.util.UUID.randomUUID().toString());
        MDC.put(MDC_ALERT_SEVERITY, "0");
        MDC.put("startTime", Long.toString(startTime));
        MDC.put("target", "appc");
        logger.info(String.format("Starting SNAPSHOT for request with id [%s]", requestId));
        metricsLogger.info(String.format("Metrics Logger: App-C Snapshot initiated. Start Time: [%s]. Request ID: [%s]",
            startTime, requestId));

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
        MDC.put("target", "appc");
        String statusStr = success ? "SUCCESS" : "FAILURE";
        String infomsg =
            String.format("APPC0119I Snapshot '%s' finished with status %s. Reason: %s", requestId, statusStr, reason);
        logger.info(infomsg);

        SnapshotOutputBuilder sob = new SnapshotOutputBuilder();
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        String endTimeStr = String.valueOf(endTime);
        String durationStr = String.valueOf(duration);
        MDC.put("endTime", endTimeStr);
        MDC.put("duration", durationStr);
        sob.setCommonResponseHeader(ResponseHeaderBuilder.buildHeader(success, requestId, reason, duration));
        sob.setVmId(new UUID(vmId));

        auditLogger.info(String.format(
            "Audit Logger: APPC0119I Snapshot '%s' finished with status %s. Start Time: [%s]. End Time: [%s]. Duration: [%s]. Request ID: [%s]. Reason:%s",
            requestId, statusStr, startTime, endTime, duration, requestId, reason));
        metricsLogger.info(String.format(
            "Metrics Logger: APPC0119I Snapshot '%s' finished with status %s. Start Time: [%s]. End Time: [%s]. Duration: [%s]. Request ID: [%s]. Reason:%s",
            requestId, statusStr, startTime, endTime, duration, requestId, reason));

        // Status must be set to true to indicate that our return is expected
        RpcResult<SnapshotOutput> rpcResult =
            RpcResultBuilder.<SnapshotOutput> status(true).withResult(sob.build()).build();
        return rpcResult;
    }
    
/**************************************************/
    
    public RpcResult<VmstatuscheckOutput> vmstatuscheck(CommonRequestHeader hdr, VnfResource vnf) {
        long startTime = System.currentTimeMillis();
        String requestId = hdr.getServiceRequestId();

        MDC.clear();
        MDC.put(MDC_REMOTE_HOST, "");
        MDC.put(MDC_KEY_REQUEST_ID, requestId);
        MDC.put(MDC_SERVICE_NAME, "App-C Provider:vmstatuscheck");
        MDC.put(MDC_SERVICE_INSTANCE_ID, "");
        try {
            MDC.put(MDC_SERVER_FQDN, InetAddress.getLocalHost().getHostName());
            MDC.put(MDC_SERVER_IP_ADDRESS, InetAddress.getLocalHost().getHostAddress());
        } catch (Exception e) {
            e.printStackTrace();
        }
        MDC.put(MDC_INSTANCE_UUID, java.util.UUID.randomUUID().toString());
        MDC.put(MDC_ALERT_SEVERITY, "0");
        logger.info(String.format("Starting VMSTATUSCHECK for request with id [%s]", requestId));
        
        performanceLogger.info(String.format("Performance Logger: App-C vmstatuscheck initiated. Start Time: [%s]. Request ID: [%s]", startTime, requestId));
        auditLogger.info(String.format("Audit Logger: App-C vmstatuscheck initiated. Start Time: [%s]. Request ID: [%s]", startTime, requestId));
        metricsLogger.info(String.format("Metrics Logger: App-C vmstatuscheck initiated. Start Time: [%s]. Request ID: [%s]", startTime, requestId));

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
        String statusStr = success ? "SUCCESS" : "FAILURE";
        String infomsg =
            String.format("VMSTATUSCHECK '%s' finished with status %s. Reason: %s", requestId, statusStr, reason);
        logger.info(infomsg);
        long endTime = System.currentTimeMillis();
        auditLogger.info(String.format("Audit Logger: VMSTATUSCHECK '%s' finished with status %s. Start Time: [%s]. End Time: [%s]. Request ID: [%s]. Reason:%s", requestId, statusStr, startTime, endTime, requestId, reason));
        metricsLogger.info(String.format("Metrics Logger: VMSTATUSCHECK '%s' finished with status %s. Start Time: [%s]. End Time: [%s]. Request ID: [%s]. Reason:%s", requestId, statusStr, startTime, endTime, requestId, reason));
        //logger.info(String.format("Step1 [%s]", Constants.STATUS_GETTER));
        String tempstring2 = properties.getProperty(Constants.STATUS_GETTER).trim();
        //logger.info(String.format("Step2 [%s]", tempstring2));
        
        
        VmstatuscheckOutputBuilder vob = new VmstatuscheckOutputBuilder();
        long duration = System.currentTimeMillis() - startTime;
        vob.setCommonResponseHeader(ResponseHeaderBuilder.buildHeader(success, requestId, reason, duration));
        vob.setStatMsg(tempstring2);

        // Status must be set to true to indicate that our return is expected
        RpcResult<VmstatuscheckOutput> rpcResult =
            RpcResultBuilder.<VmstatuscheckOutput> status(true).withResult(vob.build()).build();
        return rpcResult;
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
     * @param module
     *            The module name to be used to locate the graph
     * @param method
     *            The method name to be executed (rpc)
     * @param version
     *            The version of the graph to be used, or null for the latest
     * @param mode
     *            the execution mode of the graph, sync or async
     * @param props
     *            A set of name-value properties to be passed to the graph for context variables.
     */
    private boolean callGraph(String module, String method, String version, String mode, Properties props) {
        String graphName = String.format(("%s:%s:%s"), module, method, version);
        logger.debug(String.format("Calling Graph %s", graphName));
        metricsLogger.info(String.format("Calling Graph %s", graphName));

        boolean success = false;
        String appName = configuration.getProperty(Constants.PROPERTY_APPLICATION_NAME);
        AppcProviderClient svcLogicClient = new AppcProviderClient();
        try {
            if (svcLogicClient.hasGraph(module, method, version, mode)) {
                try {
                    Properties respProps = svcLogicClient.execute(module, method, version, mode, props);
                    success = false;        // Assume it failed unless proven otherwise
                    reason = "Failed";      // Assume it failed unless proven otherwise

                    logger.debug(EELFResourceManager.format(Msg.DEBUG_GRAPH_RESPONSE_HEADER, appName, graphName,
                        Integer.toString(respProps.size())));
                    for (String key : respProps.stringPropertyNames()) {
                        logger.debug(EELFResourceManager.format(Msg.DEBUG_GRAPH_RESPONSE_DETAIL, appName, graphName,
                            key, (String) respProps.get(key)));
                    }

                    // TODO - Find docs and see if there is a better way to handle this
                    // Bad requests have errors
                    if (respProps.containsKey(Constants.ATTRIBUTE_ERROR_CODE)) {
                        // || respProps.containsKey(Constants.ATTRIBUTE_ERROR_MESSAGE)) {
                        String errorCodeProperty = respProps.getProperty(Constants.ATTRIBUTE_ERROR_CODE).trim();
                        int errorCode = 200;
                        try {
                            errorCode = Integer.parseInt(errorCodeProperty);
                            if (errorCode >= 300) {
                                reason = EELFResourceManager.format(Msg.DG_FAILED_RESPONSE, appName, graphName,
                                    errorCodeProperty, respProps.getProperty(Constants.ATTRIBUTE_ERROR_MESSAGE));
                                logger.error(reason);
                                success = false;
                            } else {
                                success = true;
                                reason = "Success";
                            }
                        } catch (NumberFormatException e) {
                            reason = EELFResourceManager.format(Msg.PARAMETER_NOT_NUMERIC, appName, graphName,
                                Constants.ATTRIBUTE_ERROR_CODE, errorCodeProperty);
                            logger.error(reason);
                            success = false;
                        }
                    } else {
                        /*
                         * Added code that requires error code to now be defined in ALL cases. If not, it is an error
                         * and the response will be set to failed regardless if the DG worked or not.
                         */
                        reason = EELFResourceManager.format(Msg.PARAMETER_IS_MISSING, appName, graphName,
                            Constants.ATTRIBUTE_ERROR_CODE);
                        logger.error(reason);
                        success = false;
                    }
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

}
