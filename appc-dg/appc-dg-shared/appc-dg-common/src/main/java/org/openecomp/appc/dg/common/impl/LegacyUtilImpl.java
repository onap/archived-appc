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

import java.util.Map;

import org.openecomp.appc.dg.common.LegacyUtil;
import org.openecomp.appc.dg.common.utils.JSONUtil;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.sdnc.sli.SvcLogicContext;



public class LegacyUtilImpl implements LegacyUtil {

    @Override public void prepareRequest(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        ctx.setAttribute(Constants.LegacyAttributes.Action.getValue(), ctx.getAttribute(Constants.LCMAttributes.Action.getValue()).toLowerCase());

        String payloadStr = ctx.getAttribute(Constants.LCMAttributes.Payload.getValue());
        Map<String,String> payloads = JSONUtil.extractPlainValues(payloadStr,
                        Constants.LCMAttributes.VMID.getValue(), Constants.LCMAttributes.IdentityURL.getValue(), Constants.LCMAttributes.TenantID.getValue(), 
                        Constants.LCMAttributes.SkipHypervisorCheck.getValue());

        ctx.setAttribute(Constants.LegacyAttributes.VMID.getValue(), payloads.get(Constants.LCMAttributes.VMID.getValue()));
        ctx.setAttribute(Constants.LegacyAttributes.IdentityURL.getValue(), payloads.get(Constants.LCMAttributes.IdentityURL.getValue()));
        ctx.setAttribute(Constants.LegacyAttributes.TenantID.getValue(), payloads.get(Constants.LCMAttributes.TenantID.getValue()));
        ctx.setAttribute(Constants.LegacyAttributes.SkipHypervisorCheck.getValue(), payloads.get(Constants.LCMAttributes.SkipHypervisorCheck.getValue()));

    }

    @Override
    public void convertPositiveResponse(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
    }

    @Override
    public void convertNegativeResponse(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
    }
}
