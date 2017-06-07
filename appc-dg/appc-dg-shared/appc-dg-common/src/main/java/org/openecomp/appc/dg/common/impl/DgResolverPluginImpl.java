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

package org.openecomp.appc.dg.common.impl;

import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.i18n.Msg;
import com.att.eelf.i18n.EELFResourceManager;
import org.openecomp.sdnc.sli.SvcLogicContext;

import java.util.Map;

import org.openecomp.appc.dg.common.DgResolverPlugin;

public class DgResolverPluginImpl implements DgResolverPlugin {

    @Override
    public void resolveDg(Map<String, String> params, SvcLogicContext ctx) throws APPCException {

        String DGName, DGVersion, DGModule = null;
        String prefix = params.containsKey("prefix") ? params.get("prefix") + "." : "";
        AbstractResolver resolver = ResolverFactory.createResolver(params.get("DGResolutionType"));
        FlowKey flowKey = null;
        try {

            if(params.get("DGResolutionType").equalsIgnoreCase("VNFC")) {
                 flowKey = resolver.resolve(params.get(Constants.IN_PARAM_ACTION), params.get(Constants.IN_PARAM_VNF_TYPE), params.get(Constants.IN_PARAM_VNFC_TYPE), params.get(Constants.IN_PARAM_API_VERSION));
            }else if(params.get("DGResolutionType").equalsIgnoreCase("VNF")){
                 flowKey = resolver.resolve(params.get(Constants.IN_PARAM_ACTION), params.get(Constants.IN_PARAM_VNF_TYPE), params.get("vnfVersion"), params.get(Constants.IN_PARAM_API_VERSION));
            }
            if (flowKey != null) {
                DGName = flowKey.name();
                ctx.setAttribute(prefix + Constants.OUT_PARAM_DG_NAME, DGName);
                DGVersion = flowKey.version();
                ctx.setAttribute(prefix + Constants.OUT_PARAM_DG_VERSION, DGVersion);
                DGModule = flowKey.module();
                ctx.setAttribute(prefix + Constants.OUT_PARAM_DG_MODULE, DGModule);
            } else {
                throw new RuntimeException(params.get("DGResolutionType")+ " DG not found for vnf type :" + params.get(Constants.IN_PARAM_VNF_TYPE)
                        + " vnfc type : " + params.get(Constants.IN_PARAM_VNFC_TYPE)
                        + " action : " + params.get(Constants.IN_PARAM_ACTION)
                        + " api version : " + params.get(Constants.IN_PARAM_API_VERSION));
            }
        } catch (Exception e) {
            String msg = EELFResourceManager.format(Msg.FAILURE_RETRIEVE_VNFC_DG,params.get(Constants.IN_PARAM_VNFC_TYPE), e.getMessage());
            ctx.setAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE,msg);
            throw new RuntimeException(e);
        }
    }
}
