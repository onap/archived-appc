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

package org.openecomp.appc.flow.controller.node;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.openecomp.appc.flow.controller.data.Transaction;
import org.openecomp.appc.flow.controller.executorImpl.RestExecutor;
import org.openecomp.appc.flow.controller.utils.FlowControllerConstants;
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
    private static final String SDNC_CONFIG_DIR_VAR = "SDNC_CONFIG_DIR";

    public void parse(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {
        String fn = "RestServiceNode.sendRequest";
        log.info("Received processParamKeys call with params : " + inParams);
        String responsePrefix = inParams.get(FlowControllerConstants.INPUT_PARAM_RESPONSE_PRIFIX);
        try
        {
            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix+".") : "";        
            //Remove below for Block
            if(isValidJSON(inParams.get("data")) !=null){
                JsonNode jnode = isValidJSON(inParams.get("data"));
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> map = new HashMap<String, Object>();
                map = mapper.readValue(jnode.toString(), new TypeReference<Map<String, String>>(){});                
                for (Entry<String, Object> entry : map.entrySet())
                {
                    ctx.setAttribute(responsePrefix + entry.getKey(),(String) entry.getValue());
                }
            
            }
            ctx.setAttribute(responsePrefix + FlowControllerConstants.OUTPUT_PARAM_STATUS, FlowControllerConstants.OUTPUT_STATUS_SUCCESS);
            
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + FlowControllerConstants.OUTPUT_PARAM_STATUS, FlowControllerConstants.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + FlowControllerConstants.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
            e.printStackTrace();
            log.error("Error Message : "  + e.getMessage());
            throw new SvcLogicException(e.getMessage());
        }
    }

    public JsonNode isValidJSON(String json) throws IOException {
        JsonNode output = null;     
        log.info("Received response from Interface " + json);
        if(json ==null  || json.isEmpty())
            return null;
        try{ 
            ObjectMapper objectMapper = new ObjectMapper();
            output = objectMapper.readTree(json);
        } catch(JsonProcessingException e){
            log.warn("Response received from interface is not a valid JSON block" + json);
            return null;
        }    
        
        return output;
    }
}
