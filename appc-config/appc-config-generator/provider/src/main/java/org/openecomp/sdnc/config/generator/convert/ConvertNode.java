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

package org.openecomp.sdnc.config.generator.convert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdnc.config.generator.ConfigGeneratorConstant;
import org.openecomp.sdnc.config.generator.tool.EscapeUtils;
import org.openecomp.sdnc.config.generator.tool.JSONTool;
import org.openecomp.sdnc.sli.SvcLogicJavaPlugin;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.openecomp.sdnc.sli.SvcLogicContext;
import org.openecomp.sdnc.sli.SvcLogicException;

public class ConvertNode implements SvcLogicJavaPlugin {

    private static final  EELFLogger log = EELFManager.getInstance().getLogger(ConvertNode.class);

    public void convertJson2DGContext( Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {
        log.trace("Received convertJson2DGContext call with params : " + inParams);
        String responsePrefix = inParams.get(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX);
        try {
            String jsonData =  inParams.get(ConfigGeneratorConstant.INPUT_PARAM_JSON_DATA);
            String isEscaped =  inParams.get(ConfigGeneratorConstant.INPUT_PARAM_IS_ESCAPED);
            String blockKey =  inParams.get(ConfigGeneratorConstant.INPUT_PARAM_BLOCK_KEYS);
            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix+".") : "";

            if(StringUtils.isNotBlank(jsonData)){
                if(StringUtils.isNotBlank(isEscaped) && isEscaped.equalsIgnoreCase("Y")){
                    jsonData = StringEscapeUtils.unescapeJavaScript(jsonData);
                }

                List<String> blockKeys = new ArrayList<String>();
                if(blockKey != null){
                    blockKeys = Arrays.asList(blockKey.split(","));
                }

                Map<String, String> dgContext = JSONTool.convertToProperties(jsonData, blockKeys);
                log.trace("DG Context Populated:"+dgContext);

                for (Map.Entry<String, String> entry : dgContext.entrySet()) {
                    if(entry != null && entry.getKey() != null){
                        ctx.setAttribute(entry.getKey(), entry.getValue());
                    }
                }
            }
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS, ConfigGeneratorConstant.OUTPUT_STATUS_SUCCESS);

        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS, ConfigGeneratorConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_ERROR_MESSAGE,e.getMessage());
            log.error("Failed in JSON to DGContext Conversion" + e.getMessage());
            throw new SvcLogicException(e.getMessage());
        }
    }


    public void escapeData( Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {
        log.trace("Received escapeData call with params : " + inParams);
        String responsePrefix = inParams.get(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX);
        try {
            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix+".") : "";
            String unEscapeData =  inParams.get(ConfigGeneratorConstant.INPUT_PARAM_UNESCAPE_DATA);
            String dataType =  inParams.get(ConfigGeneratorConstant.INPUT_PARAM_DATA_TYPE);

            if(StringUtils.isBlank(unEscapeData)){
                throw new Exception("Unescape ("+ConfigGeneratorConstant.INPUT_PARAM_UNESCAPE_DATA +") param is missing for escapeData conversion." + unEscapeData);
            }

            if(StringUtils.isBlank(dataType)){
                throw new Exception(" Datatype ("+ConfigGeneratorConstant.INPUT_PARAM_DATA_TYPE+")param is missing for escapeData conversion.");
            }

            String escapedData = null;
            if(ConfigGeneratorConstant.DATA_TYPE_JSON.equalsIgnoreCase(dataType)){
                escapedData = StringEscapeUtils.escapeJavaScript(unEscapeData);
            }else if(ConfigGeneratorConstant.DATA_TYPE_XML.equalsIgnoreCase(dataType)){
                escapedData = StringEscapeUtils.escapeXml(unEscapeData);
            }else if(ConfigGeneratorConstant.DATA_TYPE_SQL.equalsIgnoreCase(dataType)){
                escapedData = EscapeUtils.escapeSql(unEscapeData);
            }else{
                throw new Exception(" Datatype ("+ConfigGeneratorConstant.INPUT_PARAM_DATA_TYPE+") param  value ("+dataType+")is not supported  for escapeData conversion.");
            }
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_ESCAPE_DATA, escapedData);
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS, ConfigGeneratorConstant.OUTPUT_STATUS_SUCCESS);
            log.trace("Data escapeData Successfully :" + ctx.getAttributeKeySet());
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS, ConfigGeneratorConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_ERROR_MESSAGE,e.getMessage());
            log.error("Failed in escapeData Conversion" + e.getMessage());
            throw new SvcLogicException(e.getMessage());
        }
    }

    public void unEscapeData( Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {
        log.trace("Received unEscapeData call with params : " + inParams);
        String responsePrefix = inParams.get(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX);
        try {
            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix+".") : "";
            String escapeData =  inParams.get(ConfigGeneratorConstant.INPUT_PARAM_ESCAPE_DATA);
            String dataType =  inParams.get(ConfigGeneratorConstant.INPUT_PARAM_DATA_TYPE);

            if(StringUtils.isBlank(escapeData)){
                throw new Exception("Escape ("+ConfigGeneratorConstant.INPUT_PARAM_ESCAPE_DATA +") param is missing for escapeData conversion.");
            }

            if(StringUtils.isBlank(dataType)){
                throw new Exception(" Datatype ("+ConfigGeneratorConstant.INPUT_PARAM_DATA_TYPE+")param is missing for escapeData conversion.");
            }

            String unEscapedData = null;
            if(ConfigGeneratorConstant.DATA_TYPE_JSON.equalsIgnoreCase(dataType)){
                unEscapedData = StringEscapeUtils.unescapeJavaScript(escapeData);
            }else if(ConfigGeneratorConstant.DATA_TYPE_XML.equalsIgnoreCase(dataType)){
                unEscapedData = StringEscapeUtils.unescapeXml(escapeData);
            }else{
                throw new Exception(" Datatype ("+ConfigGeneratorConstant.INPUT_PARAM_DATA_TYPE+") param  value ("+dataType+")is not supported  for unEscapeData conversion.");
            }
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_UNESCAPE_DATA, unEscapedData);
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS, ConfigGeneratorConstant.OUTPUT_STATUS_SUCCESS);
            log.trace("Converted unEscapeData Successfully :" + ctx.getAttributeKeySet());
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS, ConfigGeneratorConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_ERROR_MESSAGE,e.getMessage());
            log.error("Failed in unEscapeData Conversion" + e.getMessage());
            throw new SvcLogicException(e.getMessage());
        }
    }
    
    
     public void convertContextToJson(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException{
        log.trace("Received convertContextToJson call with params : " + inParams);
        String responsePrefix = inParams.get(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX);
        String contextKey = inParams.get(ConfigGeneratorConstant.INPUT_PARAM_CONTEXT_KEY);
        try {
            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix+".") : "";
            
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode objectNode = mapper.createObjectNode();
            
            Set<String> keys = ctx.getAttributeKeySet();    
            for (String key : keys) {
                if(key.startsWith(contextKey +".")){
                     String objkey=  key.replaceFirst(contextKey + ".", "");
                     objectNode.put(objkey, ctx.getAttribute(key));
                     
                }                
            }
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.INPUT_PARAM_JSON_CONTENT, objectNode.toString());
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS, ConfigGeneratorConstant.OUTPUT_STATUS_SUCCESS);
            log.trace("convertContextToJson Successful" );
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS, ConfigGeneratorConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_ERROR_MESSAGE,e.getMessage());
            log.error("Failed in convertContextToJson" + e.getMessage());
            throw new SvcLogicException(e.getMessage());
        }
     }

}
