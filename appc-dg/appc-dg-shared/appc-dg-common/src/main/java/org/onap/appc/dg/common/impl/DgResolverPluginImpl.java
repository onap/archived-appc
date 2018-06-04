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

package org.onap.appc.dg.common.impl;

import com.att.eelf.i18n.EELFResourceManager;
import java.util.Map;
import org.onap.appc.dg.common.DgResolverPlugin;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.i18n.Msg;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

public class DgResolverPluginImpl implements DgResolverPlugin {

    private static final String PARAM_DG_RESOLUTION_TYPE = "DGResolutionType";

    @Override
    public void resolveDg(Map<String, String> params, SvcLogicContext ctx) throws APPCException {

        String dgName;
        String dgVersion;
        String dgModule;

        String prefix = params.containsKey("prefix") ? params.get("prefix") + "." : "";
        AbstractResolver resolver = ResolverFactory.createResolver(params.get(PARAM_DG_RESOLUTION_TYPE));
        FlowKey flowKey = null;
        try {
            if (resolver == null)
                throw new DgResolverException("Couldn't create resolver of type: " + PARAM_DG_RESOLUTION_TYPE);

            if ("VNFC".equalsIgnoreCase(params.get(PARAM_DG_RESOLUTION_TYPE))) {
                flowKey = resolver
                    .resolve(params.get(Constants.IN_PARAM_ACTION), params.get(Constants.IN_PARAM_VNF_TYPE),
                        params.get(Constants.IN_PARAM_VNFC_TYPE), params.get(Constants.IN_PARAM_API_VERSION));
            } else if ("VNF".equalsIgnoreCase(params.get(PARAM_DG_RESOLUTION_TYPE))) {
                flowKey = resolver
                    .resolve(params.get(Constants.IN_PARAM_ACTION), params.get(Constants.IN_PARAM_VNF_TYPE),
                        params.get("vnfVersion"), params.get(Constants.IN_PARAM_API_VERSION));
            }
            if (flowKey != null) {
                dgName = flowKey.name();
                ctx.setAttribute(prefix + Constants.OUT_PARAM_DG_NAME, dgName);
                dgVersion = flowKey.version();
                ctx.setAttribute(prefix + Constants.OUT_PARAM_DG_VERSION, dgVersion);
                dgModule = flowKey.module();
                ctx.setAttribute(prefix + Constants.OUT_PARAM_DG_MODULE, dgModule);
            } else {
                throw new DgResolverException(params.get(PARAM_DG_RESOLUTION_TYPE) + " DG not found for vnf type :" + params
                    .get(Constants.IN_PARAM_VNF_TYPE)
                    + " vnfc type : " + params.get(Constants.IN_PARAM_VNFC_TYPE)
                    + " action : " + params.get(Constants.IN_PARAM_ACTION)
                    + " api version : " + params.get(Constants.IN_PARAM_API_VERSION));
            }
        } catch (Exception e) {
            String msg = EELFResourceManager
                .format(Msg.FAILURE_RETRIEVE_VNFC_DG, params.get(Constants.IN_PARAM_VNFC_TYPE), e.getMessage());
            ctx.setAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE, msg);
            throw new DgResolverException(e);
        }
    }
}
