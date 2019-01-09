/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications (C) 2019 Ericsson
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

package org.onap.appc.dg.netconf.impl;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.onap.appc.adapter.netconf.NetconfClient;
import org.onap.appc.adapter.netconf.NetconfClientFactory;
import org.onap.appc.adapter.netconf.NetconfClientType;
import org.onap.appc.adapter.netconf.NetconfConnectionDetails;
import org.onap.appc.adapter.netconf.NetconfDataAccessService;
import org.onap.appc.adapter.netconf.OperationalStateValidator;
import org.onap.appc.adapter.netconf.OperationalStateValidatorFactory;
import org.onap.appc.adapter.netconf.VnfType;
import org.onap.appc.adapter.netconf.util.Constants;
import org.onap.appc.dg.netconf.NetconfClientPlugin;
import org.onap.appc.exceptions.APPCException;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;


public class NetconfClientPluginImpl implements NetconfClientPlugin {

    private static EELFLogger logger = EELFManager.getInstance().getApplicationLogger();
    private static ObjectMapper mapper = new ObjectMapper();
    private static final String NETCONF_CLIENT_FACTORY_NAME = "org.onap.appc.adapter.netconf.NetconfClientFactory";
    private static final String CONNECTION_DETAILS_PARAM = "connection-details";
    private static final String ERROR_STR = "Error ";
    private static final String GET_CONFIG_RESULT_PARAM = "getConfig_Result";
    private static final String FAILURE_PARAM = "failure";
    private static final String GET_RUNNING_CONFIG_RESULT_PARAM = "getRunningConfig_Result";

    private NetconfDataAccessService dao;
    private NetconfClientFactory clientFactory;

    public NetconfClientPluginImpl() {
        BundleContext bctx = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        ServiceReference<NetconfClientFactory> srefNetconfClientFactory = bctx
            .getServiceReference(NetconfClientFactory.class);
        clientFactory = bctx.getService(srefNetconfClientFactory);
    }

    public void setDao(NetconfDataAccessService dao) {
        this.dao = dao;
        this.dao.setSchema(Constants.NETCONF_SCHEMA);
    }

    public void configure(Map<String, String> params, SvcLogicContext ctx) throws APPCException {

        try {
            // by default, it uses the jsch Netconf Adapter implementation by calling getNetconfClient(NetconfClientType.SSH).
            NetconfClient client = clientFactory.getNetconfClient(NetconfClientType.SSH);
            connect(params, client);
        } catch (Exception e) {
            ctx.setAttribute(Constants.DG_OUTPUT_STATUS_MESSAGE, e.getMessage());
            logger.error(ERROR_STR + e.getMessage());
            throw e;
        }
    }

    private void connect(Map<String, String> params, NetconfClient client) throws APPCException {
        try {
            NetconfConnectionDetails connectionDetails = mapper
                .readValue(params.get(CONNECTION_DETAILS_PARAM), NetconfConnectionDetails.class);
            String netconfMessage = params.get("file-content");
            client.connect(connectionDetails);
            client.configure(netconfMessage);
        } catch (IOException e) {
            logger.error(ERROR_STR + e.getMessage());
            throw new APPCException(e);
        } finally {
            client.disconnect();
        }
    }

    @Override
    public void operationStateValidation(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        if (logger.isTraceEnabled()) {
            logger.trace("Entering to operationStateValidation with params = " + ObjectUtils.toString(params)
                + ", SvcLogicContext = " + ObjectUtils.toString(ctx));
        }
        try {
            String paramName = Constants.VNF_TYPE_FIELD_NAME;
            String vfType = params.get(paramName);
            validateMandatoryParam(paramName, vfType);
            VnfType vnfType = VnfType.getVnfType(vfType);

            paramName = Constants.VNF_HOST_IP_ADDRESS_FIELD_NAME;
            String vnfHostIpAddress = params.get(paramName);
            validateMandatoryParam(paramName, vnfHostIpAddress);

            //get connectionDetails
            String connectionDetailsStr = params.get(Constants.CONNECTION_DETAILS_FIELD_NAME);
            NetconfConnectionDetails connectionDetails =
                resolveConnectionDetails(ctx, vnfType, vnfHostIpAddress, connectionDetailsStr);

            if (connectionDetails == null) {
                throw new IllegalStateException("missing connectionDetails for VnfType:" + vnfType.name());
            }

            //get operationsStateNetconfMessage
            OperationalStateValidator operationalStateValidator = OperationalStateValidatorFactory
                .getOperationalStateValidator(vnfType);
            String configurationFileName = operationalStateValidator.getConfigurationFileName();
            String operationsStateNetconfMessage = null;
            if (!StringUtils.isEmpty(configurationFileName)) {
                operationsStateNetconfMessage = retrieveConfigurationFileContent(configurationFileName);
            }

            //connect checK Opertaions state and dissconnect
            NetconfClient client = clientFactory.getNetconfClient(NetconfClientType.SSH);
            try {
                client.connect(connectionDetails);
                String response = null;
                if (!StringUtils.isEmpty(operationsStateNetconfMessage)) {
                    response = client.exchangeMessage(operationsStateNetconfMessage);
                }
                operationalStateValidator.validateResponse(response);
            } finally {
                client.disconnect();
            }
        } catch (APPCException e) {
            logger.error(e.getMessage());
            ctx.setAttribute(Constants.DG_OUTPUT_STATUS_MESSAGE, e.toString());
            throw e;
        } catch (Exception e) {
            logger.error(e.toString());
            ctx.setAttribute(Constants.DG_OUTPUT_STATUS_MESSAGE, e.toString());
            throw new APPCException(e);
        }
    }

    private NetconfConnectionDetails resolveConnectionDetails(SvcLogicContext ctx, VnfType vnfType,
        String vnfHostIpAddress, String connectionDetailsStr) throws APPCException, IOException {

        NetconfConnectionDetails connectionDetails;
        if (StringUtils.isEmpty(connectionDetailsStr)) {
            connectionDetails = retrieveConnectionDetails(vnfType);
            connectionDetails.setHost(vnfHostIpAddress);
            ctx.setAttribute(Constants.CONNECTION_DETAILS_FIELD_NAME, mapper.writeValueAsString(connectionDetails));
        } else {
            connectionDetails = mapper.readValue(connectionDetailsStr, NetconfConnectionDetails.class);
        }
        return connectionDetails;
    }

    @Override
    public void modifyConfiguration(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        this.configure(params, ctx);
    }

    @Override
    public void backupConfiguration(Map<String, String> params, SvcLogicContext ctx) throws APPCException {

        NetconfClient client = null;
        try {
                logger.debug("Entered backup to DEVICE_INTERFACE_LOG");

            client = clientFactory.getNetconfClient(NetconfClientType.SSH);
            //get connection details
            NetconfConnectionDetails connectionDetails = mapper
                .readValue(params.get(CONNECTION_DETAILS_PARAM), NetconfConnectionDetails.class);
            //connect the client and get configuration
            client.connect(connectionDetails);
            String configuration = client.getConfiguration();

            //store configuration in database
            dao.logDeviceInteraction(null, null, getCurrentDateTime(), configuration);

        } catch (Exception e) {
            logger.error(ERROR_STR + e.getMessage());
            ctx.setAttribute(Constants.DG_OUTPUT_STATUS_MESSAGE, e.getMessage());
            throw new APPCException(e);
        } finally {
            //disconnect the client
            if (client != null) {
                client.disconnect();
            }
        }
    }

    @Override
    public void getConfig(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        NetconfClient client = null;
        String confId = params.get("conf-id");
        if ("current".equalsIgnoreCase(confId)) {
            try {
                    logger.debug("Entered getConfig to DEVICE_INTERFACE_LOG");
                //get netconf client to get configuration
                BundleContext bctx = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
                ServiceReference sref = bctx.getServiceReference(NETCONF_CLIENT_FACTORY_NAME);
                NetconfClientFactory clientFact = (NetconfClientFactory) bctx.getService(sref);
                client = clientFact.getNetconfClient(NetconfClientType.SSH);
                //get connection details
                NetconfConnectionDetails connectionDetails = mapper
                    .readValue(params.get(CONNECTION_DETAILS_PARAM), NetconfConnectionDetails.class);
                //connect the client and get configuration
                client.connect(connectionDetails);
                String configuration = client.getConfiguration();
                if (configuration != null) {
                    String fullConfig = ctx.getAttribute("fullConfig");
                    fullConfig = fullConfig == null ? "" : fullConfig;
                    ctx.setAttribute("fullConfig", fullConfig + configuration);

                    ctx.setAttribute(GET_CONFIG_RESULT_PARAM, "Success");
                    String entityName = ctx.getAttribute("entity");//VM name
                    trySetEntityConfiguration(ctx, configuration, entityName);
                } else {
                    ctx.setAttribute(GET_CONFIG_RESULT_PARAM, FAILURE_PARAM);
                }
            } catch (Exception e) {
                ctx.setAttribute(GET_CONFIG_RESULT_PARAM, FAILURE_PARAM);
                logger.error(ERROR_STR + e.getMessage());
                ctx.setAttribute(Constants.DG_OUTPUT_STATUS_MESSAGE, e.getMessage());
                throw new APPCException(e);
            } finally {
                //disconnect the client
                if (client != null) {
                    client.disconnect();
                }
            }
        } else {
            logger.info("Current Conf id value is not supported");
        }

    }

    private void trySetEntityConfiguration(SvcLogicContext ctx, String configuration, String entityName) {
        if (entityName != null) {
            ctx.setAttribute(entityName + ".Configuration", configuration);
        }
    }


    @Override
    public void getRunningConfig(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        NetconfClient client = null;
        try {
            logger.info("Entered getRunningConfig to DEVICE_INTERFACE_LOG");
            //get netconf client to get configuration
            BundleContext bctx = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
            ServiceReference sref = bctx.getServiceReference(NETCONF_CLIENT_FACTORY_NAME);
            NetconfClientFactory clientFact = (NetconfClientFactory) bctx.getService(sref);
            client = clientFact.getNetconfClient(NetconfClientType.SSH);
            //get connection details
            NetconfConnectionDetails connectionDetails = new NetconfConnectionDetails();
            connectionDetails.setHost(params.get("host-ip-address"));
            connectionDetails.setUsername(params.get("user-name"));
            connectionDetails.setPassword(params.get("password"));
            connectionDetails.setPort(
                !("".equalsIgnoreCase(params.get("port-number"))) ? Integer.parseInt(params.get("port-number"))
                    : NetconfConnectionDetails.DEFAULT_PORT);
            //connect the client and get configuration
            client.connect(connectionDetails);
            String configuration = client.getConfiguration();
            if (configuration != null) {
                ctx.setAttribute("running-config", configuration);

                ctx.setAttribute(GET_RUNNING_CONFIG_RESULT_PARAM, "Success");
            } else {
                ctx.setAttribute(GET_RUNNING_CONFIG_RESULT_PARAM, FAILURE_PARAM);
            }
        } catch (Exception e) {
            ctx.setAttribute(GET_RUNNING_CONFIG_RESULT_PARAM, FAILURE_PARAM);
            logger.error(ERROR_STR + e.getMessage());
            ctx.setAttribute(Constants.DG_OUTPUT_STATUS_MESSAGE, e.getMessage());
            throw new APPCException(e);
        } finally {
            //disconnect the client
            if (client != null) {
                client.disconnect();
            }
        }
    }

    private String getCurrentDateTime() {

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    void validateMandatoryParam(String paramName, String paramValue) {
        if (StringUtils.isEmpty(paramValue)) {
            throw new IllegalArgumentException("input " + paramName + " param is empty");
        }
    }

    public NetconfConnectionDetails retrieveConnectionDetails(VnfType vnfType) throws APPCException {

        NetconfConnectionDetails connectionDetails = new NetconfConnectionDetails();
        if (!dao.retrieveNetconfConnectionDetails(vnfType.getFamilyType().name(), connectionDetails)) {
            logger.error("Missing configuration for " + vnfType.getFamilyType().name());
            throw new APPCException("Missing configuration for " + vnfType.getFamilyType().name() + " in "
                + Constants.DEVICE_AUTHENTICATION_TABLE_NAME);
        }
        return connectionDetails;
    }

    public String retrieveConfigurationFileContent(String configFileName) {
        return dao.retrieveConfigFileName(configFileName);
    }
}
