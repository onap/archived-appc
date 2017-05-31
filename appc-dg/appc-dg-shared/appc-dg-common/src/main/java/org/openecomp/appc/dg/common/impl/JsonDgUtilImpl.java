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

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.appc.dg.common.JsonDgUtil;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.util.JsonUtil;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.openecomp.sdnc.sli.SvcLogicContext;

import java.util.Map;


public class JsonDgUtilImpl implements JsonDgUtil {
    private static final EELFLogger logger = EELFManager.getInstance().getLogger(JsonDgUtilImpl.class);

    @Override
    public void flatAndAddToContext(Map<String, String> params, SvcLogicContext ctx) throws APPCException {

        if (logger.isTraceEnabled()) {
            logger.trace("Entering to flatAndAddToContext with params = "+ ObjectUtils.toString(params)+", SvcLogicContext = "+ObjectUtils.toString(ctx));
        }
        try {
            String paramName = Constants.PAYLOAD;
            String payload = params.get(paramName);
            if (payload == "")
                payload = ctx.getAttribute("input.payload");
            if (!StringUtils.isEmpty(payload)) {
                Map<String, String> flatMap = JsonUtil.convertJsonStringToFlatMap(payload);
                if (flatMap != null && flatMap.size() > 0) {
                    for (Map.Entry<String, String> entry : flatMap.entrySet()) {
                        ctx.setAttribute(entry.getKey(), entry.getValue());
                    }
                }
            } else {
                logger.warn("input payload param value is empty (\"\") or null");
            }
        } catch (Exception e) {
            logger.error(e.toString());
            ctx.setAttribute(Constants.DG_OUTPUT_STATUS_MESSAGE, e.toString());
            throw new APPCException(e);
        }
    }
}
