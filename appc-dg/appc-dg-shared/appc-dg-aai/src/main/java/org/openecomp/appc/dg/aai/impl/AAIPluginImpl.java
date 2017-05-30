/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 						reserved.
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

package org.openecomp.appc.dg.aai.impl;

import org.openecomp.appc.dg.aai.AAIPlugin;
import org.openecomp.appc.dg.aai.impl.Constants;
import org.openecomp.appc.exceptions.APPCException;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.openecomp.sdnc.sli.SvcLogicException;
import org.openecomp.sdnc.sli.SvcLogicResource;
import org.openecomp.sdnc.sli.aai.AAIClient;
import org.openecomp.sdnc.sli.aai.AAIService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import java.util.HashMap;
import java.util.Map;


public class AAIPluginImpl implements AAIPlugin {
    private AAIClient aaiClient;
    private static final EELFLogger logger = EELFManager.getInstance().getLogger(AAIPluginImpl.class);

    public AAIPluginImpl() {
        BundleContext bctx = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        ServiceReference sref = bctx.getServiceReference(AAIService.class);
        aaiClient = (AAIClient) bctx.getService(sref);
    }

    @Override
    public void postGenericVnfData(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        String vnf_id = ctx.getAttribute(Constants.VNF_ID_PARAM_NAME);
        String prefix = ctx.getAttribute(Constants.AAI_PREFIX_PARAM_NAME);

        String key = "vnf-id = '" + vnf_id + "'";

        Map<String, String> data = new HashMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String paramKey = entry.getKey();
            int pos = paramKey.indexOf(Constants.AAI_INPUT_DATA);
            if (pos == 0) {
                data.put(paramKey.substring(Constants.AAI_INPUT_DATA.length()+1), entry.getValue());
            }
        }

        try {
            SvcLogicResource.QueryStatus response = aaiClient.update("generic-vnf", key, data, prefix, ctx);
            if (SvcLogicResource.QueryStatus.NOT_FOUND.equals(response)) {
                String errorMessage = String.format("VNF not found for vnf_id = %s", vnf_id);
                ctx.setAttribute(Constants.DG_OUTPUT_STATUS_MESSAGE, errorMessage);
                throw new APPCException(errorMessage);
            }
            logger.info("AAIResponse: " + response.toString());
            if (SvcLogicResource.QueryStatus.FAILURE.equals(response)) {
                String errorMessage = String.format("Error Querying AAI with vnfID = %s", vnf_id);
                ctx.setAttribute(Constants.DG_OUTPUT_STATUS_MESSAGE, errorMessage);
                throw new APPCException(errorMessage);
            }
        } catch (SvcLogicException e) {
            String errorMessage = String.format("Error in postVnfdata %s", e);
            ctx.setAttribute(Constants.DG_OUTPUT_STATUS_MESSAGE, errorMessage);
            logger.error(errorMessage);
            throw new APPCException(e);
        }
    }

    @Override
    public void getGenericVnfData(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        String vnf_id = ctx.getAttribute(Constants.VNF_ID_PARAM_NAME);
        String prefix = ctx.getAttribute(Constants.AAI_PREFIX_PARAM_NAME);

        String key = "vnf-id = '" + vnf_id + "'";
        try {
            SvcLogicResource.QueryStatus response = aaiClient.query("generic-vnf", false, null, key, prefix, null, ctx);
            if (SvcLogicResource.QueryStatus.NOT_FOUND.equals(response)) {
                String errorMessage = String.format("VNF not found for vnf_id = %s", vnf_id);
                ctx.setAttribute(Constants.DG_OUTPUT_STATUS_MESSAGE, errorMessage);
                throw new APPCException(errorMessage);
            } else if (SvcLogicResource.QueryStatus.FAILURE.equals(response)) {
                String errorMessage = String.format("Error Querying AAI with vnfID = %s", vnf_id);
                ctx.setAttribute(Constants.DG_OUTPUT_STATUS_MESSAGE, errorMessage);
                throw new APPCException(errorMessage);
            }
            String aaiEntitlementPoolUuid = ctx.getAttribute(Constants.AAI_ENTITLMENT_POOL_UUID_NAME);
            if (null == aaiEntitlementPoolUuid) aaiEntitlementPoolUuid = "";
            String aaiLicenseKeyGroupUuid = ctx.getAttribute(Constants.AAI_LICENSE_KEY_UUID_NAME);
            if (null == aaiLicenseKeyGroupUuid) aaiLicenseKeyGroupUuid = "";

            ctx.setAttribute(Constants.IS_RELEASE_ENTITLEMENT_REQUIRE, Boolean.toString(!aaiEntitlementPoolUuid.isEmpty()));
            ctx.setAttribute(Constants.IS_RELEASE_LICENSE_REQUIRE, Boolean.toString(!aaiLicenseKeyGroupUuid.isEmpty()));

            logger.info("AAIResponse: " + response.toString());
        } catch (SvcLogicException e) {
            String errorMessage = String.format("Error in getVnfdata %s", e);
            ctx.setAttribute(Constants.DG_OUTPUT_STATUS_MESSAGE, errorMessage);
            logger.error(errorMessage);
            throw new APPCException(e);
        }
    }
}
