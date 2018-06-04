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

import java.util.Map;
import org.onap.appc.dg.common.LegacyUtil;
import org.onap.appc.dg.common.utils.JSONUtil;
import org.onap.appc.exceptions.APPCException;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;


public class LegacyUtilImpl implements LegacyUtil {

    @Override
    public void prepareRequest(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        ctx.setAttribute(Constants.LegacyAttributes.ACTION.getValue(),
            ctx.getAttribute(Constants.LCMAttributes.ACTION.getValue()).toLowerCase());

        String payloadStr = ctx.getAttribute(Constants.LCMAttributes.PAYLOAD.getValue());
        Map<String, String> payloads = JSONUtil.extractPlainValues(payloadStr,
            Constants.LCMAttributes.VMID.getValue(), Constants.LCMAttributes.IDENTITY_URL.getValue(),
            Constants.LCMAttributes.TENANT_ID.getValue(),
            Constants.LCMAttributes.SKIP_HYPERVISOR_CHECK.getValue());

        ctx.setAttribute(Constants.LegacyAttributes.VMID.getValue(),
            payloads.get(Constants.LCMAttributes.VMID.getValue()));
        ctx.setAttribute(Constants.LegacyAttributes.IDENTITY_URL.getValue(),
            payloads.get(Constants.LCMAttributes.IDENTITY_URL.getValue()));
        ctx.setAttribute(Constants.LegacyAttributes.TENANT_ID.getValue(),
            payloads.get(Constants.LCMAttributes.TENANT_ID.getValue()));
        ctx.setAttribute(Constants.LegacyAttributes.SKIP_HYPERVISOR_CHECK.getValue(),
            payloads.get(Constants.LCMAttributes.SKIP_HYPERVISOR_CHECK.getValue()));

    }

    @Override
    public void convertPositiveResponse(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        /*TODO implement this method*/
    }

    @Override
    public void convertNegativeResponse(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        /*TODO implement this method*/
    }
}
