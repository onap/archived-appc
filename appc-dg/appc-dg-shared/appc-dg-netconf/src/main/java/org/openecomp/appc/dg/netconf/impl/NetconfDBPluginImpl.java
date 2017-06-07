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

package org.openecomp.appc.dg.netconf.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

import org.openecomp.appc.adapter.netconf.NetconfConnectionDetails;
import org.openecomp.appc.adapter.netconf.NetconfDataAccessService;
import org.openecomp.appc.adapter.netconf.exception.DataAccessException;
import org.openecomp.appc.adapter.netconf.util.Constants;
import org.openecomp.appc.dg.netconf.NetconfDBPlugin;
import org.openecomp.appc.exceptions.APPCException;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.openecomp.sdnc.sli.SvcLogicContext;

public class NetconfDBPluginImpl implements NetconfDBPlugin {

    private static EELFLogger logger = EELFManager.getInstance().getApplicationLogger();
    private static ObjectMapper mapper = new ObjectMapper();

    // populated by blueprint framework
    private NetconfDataAccessService daoService;

    public void setDaoService(NetconfDataAccessService daoService) {
        this.daoService = daoService;
        this.daoService.setSchema(Constants.NETCONF_SCHEMA);
    }

    public NetconfDBPluginImpl() {
    }

    public void retrieveDSConfiguration(Map<String, String> params, SvcLogicContext ctx) throws APPCException {

        try {
            String fileContent = daoService.retrieveConfigFileName(params.get(Constants.CONFIGURATION_FILE_FIELD_NAME));
            ctx.setAttribute(Constants.FILE_CONTENT_FIELD_NAME, fileContent);
        } catch(DataAccessException e) {
            logger.error("Error " + e.getMessage());
            ctx.setAttribute(Constants.DG_OUTPUT_STATUS_MESSAGE, e.getMessage());
            throw e;
        }

        getConnection(params, ctx);
    }

    @Override
    public void retrieveVMDSConfiguration(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        logger.info("Setting entity value :" +params.get(Constants.RESOURCEKEY));
        ctx.setAttribute("entity", params.get(Constants.RESOURCEKEY));
        NetconfConnectionDetails connectionDetails = new NetconfConnectionDetails();
        try {
            if (!daoService.retrieveNetconfConnectionDetails(params.get(Constants.RESOURCEKEY), connectionDetails)) {
                ctx.setAttribute("retrieveVMDSConfiguration_Result","failure");
                logger.error("Missing configuration for " + params.get(Constants.VNF_TYPE_FIELD_NAME));
                throw new APPCException("Missing configuration for " + params.get(Constants.VNF_TYPE_FIELD_NAME) + " in " + Constants.DEVICE_AUTHENTICATION_TABLE_NAME);
            }
            ctx.setAttribute(Constants.CONNECTION_DETAILS_FIELD_NAME, mapper.writeValueAsString(connectionDetails));
            ctx.setAttribute("retrieveVMDSConfiguration_Result","success");
        } catch(APPCException e) {
            ctx.setAttribute(Constants.DG_OUTPUT_STATUS_MESSAGE, e.getMessage());
            throw e;
        } catch(DataAccessException | JsonProcessingException e) {
            logger.error("Error " + e.getMessage());
            ctx.setAttribute(Constants.DG_OUTPUT_STATUS_MESSAGE, e.getMessage());
            throw new APPCException(e);
        }
    }

    @Override
    public void retrieveConfigFile(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        String fileContent = daoService.retrieveConfigFileName(params.get(Constants.CONFIGURATION_FILE_FIELD_NAME));
        ctx.setAttribute(Constants.FILE_CONTENT_FIELD_NAME, fileContent);
    }

    public void retrieveConnectionDetails(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        getConnection(params, ctx);
    }

            private void getConnection(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
                NetconfConnectionDetails connectionDetails = new NetconfConnectionDetails();
                try {
                    if (!daoService.retrieveNetconfConnectionDetails(params.get(Constants.VNF_TYPE_FIELD_NAME), connectionDetails)) {
                        logger.error("Missing configuration for " + params.get(Constants.VNF_TYPE_FIELD_NAME));
                        throw new APPCException("Missing configuration for " + params.get(Constants.VNF_TYPE_FIELD_NAME) + " in " + Constants.DEVICE_AUTHENTICATION_TABLE_NAME);
                    }
                    connectionDetails.setHost(params.get(Constants.VNF_HOST_IP_ADDRESS_FIELD_NAME));
                    ctx.setAttribute(Constants.CONNECTION_DETAILS_FIELD_NAME, mapper.writeValueAsString(connectionDetails));
                } catch(APPCException e) {
                    ctx.setAttribute(Constants.DG_OUTPUT_STATUS_MESSAGE, e.getMessage());
                    throw e;
                } catch(DataAccessException | JsonProcessingException e) {
                    logger.error("Error " + e.getMessage());
            ctx.setAttribute(Constants.DG_OUTPUT_STATUS_MESSAGE, e.getMessage());
            throw new APPCException(e);
        }
    }
}
