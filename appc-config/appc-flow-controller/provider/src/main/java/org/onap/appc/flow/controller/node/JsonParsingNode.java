/*-
 * ============LICENSE_START=======================================================
 * ONAP : APP-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property.  All rights reserved.
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
 */

package org.onap.appc.flow.controller.node;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.onap.appc.flow.controller.utils.FlowControllerConstants;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonParsingNode implements SvcLogicJavaPlugin{

    private static final  EELFLogger log = EELFManager.getInstance().getLogger(JsonParsingNode.class);

    public void parse(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {
        String fn = "RestServiceNode.sendRequest";
        log.info("Received processParamKeys call with params : " + inParams);
        String responsePrefix = inParams.get(FlowControllerConstants.INPUT_PARAM_RESPONSE_PRIFIX);
        responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix+".") : "";
        try {
            //Remove below for Block
            JsonNode jnode = isValidJSON(inParams.get("data"));
            if(jnode != null) {
                Map<String, Object> map = new ObjectMapper().readValue(jnode.toString(), new TypeReference<Map<String, String>>(){});
                for (Entry<String, Object> entry : map.entrySet())
                {
                    ctx.setAttribute(responsePrefix + entry.getKey(),(String) entry.getValue());
                }
            }
            ctx.setAttribute(responsePrefix + FlowControllerConstants.OUTPUT_PARAM_STATUS, FlowControllerConstants.OUTPUT_STATUS_SUCCESS);
            
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + FlowControllerConstants.OUTPUT_PARAM_STATUS, FlowControllerConstants.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + FlowControllerConstants.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
            log.error(fn + " Error Message : " + e.getMessage(), e);
            throw new SvcLogicException(e.getMessage());
        }
    }

    private JsonNode isValidJSON(String json) throws IOException {
        JsonNode output;
        log.info("Received response from Interface " + json);
        if(json == null  || json.isEmpty())
            return null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            output = objectMapper.readTree(json);
        } catch(JsonProcessingException e) {
            log.warn("Response received from interface is not a valid JSON block" + json, e);
            return null;
        }
        return output;
    }
}
