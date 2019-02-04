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


package org.onap.appc.aai.client.node;

import static junit.framework.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.onap.appc.aai.client.AppcAaiClientConstant;
//import org.onap.appc.aai.client.aai.AAIClientMock;
import org.onap.appc.aai.client.aai.AaiService;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.adaptors.aai.AAIClient;

public class MockAaiService extends AaiService {

    // ONAP merging
    private static final EELFLogger log = EELFManager.getInstance().getLogger(MockAaiService.class);
    private AAIClient aaiClient;

         /*public MockAaiService() {
              super(new AAIClientMock());
         }*/


    public MockAaiService(AAIClient aaic) {
        super(aaic);
    }

    public void getVMInfo(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        log.info("Received Mock getVmInfo call with params : " + params);
        String vserverId = params.get("vserverId");
        String prefix = params.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX);
        if (vserverId.equals("ibcm0001id")) {
            ctx.setAttribute(prefix + ".vm.vserver-name", "vserverName1");
            ctx.setAttribute(prefix + ".vm.vf-module-id", "vfModule1");
        } else {
            ctx.setAttribute(prefix + ".vm.vserver-name", "vserverName2");
            ctx.setAttribute(prefix + ".vm.vf-module-id", "vfModule2");
            ctx.setAttribute(prefix + ".vm.vnfc[0].vnfc-name", "vnfcName2");
        }

    }


    public void getVnfcInfo(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        log.info("Received Mock getVmInfo call with params : " + params);
        String prefix = params.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX);

        String vnfcName = params.get("vnfcName");

        if (vnfcName.equals("vnfcName2")) {
            ctx.setAttribute(prefix + ".vnfc.vnfc-type", "vnfcType2");
            ctx.setAttribute(prefix + ".vnfc.vnfc-function-code", "vnfcFuncCode2");
            ctx.setAttribute(prefix + ".vnfc.group-notation", "vnfcGrpNot2");
        }


    }

    @Override
    public SvcLogicContext readResource(String query, String prefix, String resourceType) throws SvcLogicException {
        SvcLogicContext resourceContext = new SvcLogicContext();
        resourceContext.setAttribute("vfModuleInfo.model-invariant-id", "invid01");
        resourceContext.setAttribute("vfModuleInfo.model-version-id", "versid01");
        resourceContext.setAttribute("modelInfo.model-name", "model0001");

        return resourceContext;
    }
}
