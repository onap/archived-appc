/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications Copyright (C) 2018 Ericsson
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

package org.onap.sdnc.config.generator.convert;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;
import org.onap.sdnc.config.generator.ConfigGeneratorConstant;
import org.onap.sdnc.config.generator.tool.EscapeUtils;
import org.onap.sdnc.config.generator.tool.JSONTool;

public class ConvertNode implements SvcLogicJavaPlugin {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(ConvertNode.class);
    private static final String DATA_TYPE_STR = " Datatype (";

    public void convertJson2DGContext(Map<String, String> inParams, SvcLogicContext ctx)
        throws SvcLogicException {
        log.trace("Received convertJson2DGContext call with params : " + inParams);
        String responsePrefix = inParams.get(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX);
        try {
            String jsonData = inParams.get(ConfigGeneratorConstant.INPUT_PARAM_JSON_DATA);
            String isEscaped = inParams.get(ConfigGeneratorConstant.INPUT_PARAM_IS_ESCAPED);
            String blockKey = inParams.get(ConfigGeneratorConstant.INPUT_PARAM_BLOCK_KEYS);
            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix + ".") : "";

            if (StringUtils.isNotBlank(jsonData)) {
                if (StringUtils.isNotBlank(isEscaped) && "Y".equalsIgnoreCase(isEscaped)) {
                    jsonData = StringEscapeUtils.unescapeJavaScript(jsonData);
                }

                List<String> blockKeys = new ArrayList<>();
                if (blockKey != null) {
                    blockKeys = Arrays.asList(blockKey.split(","));
                }

                Map<String, String> dgContext = JSONTool.convertToProperties(jsonData, blockKeys);
                log.trace("DG Context Populated:" + dgContext);

                for (Map.Entry<String, String> entry : dgContext.entrySet()) {
                    trySetContextAttribute(ctx, entry);
                }
            }
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS,
                ConfigGeneratorConstant.OUTPUT_STATUS_SUCCESS);

        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS,
                ConfigGeneratorConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_ERROR_MESSAGE,
                e.getMessage());
            log.error("Failed in JSON to DGContext Conversion", e);
            throw new SvcLogicException(e.getMessage());
        }
    }

    private void trySetContextAttribute(SvcLogicContext ctx, Entry<String, String> entry) {
        if (entry != null && entry.getKey() != null) {
            ctx.setAttribute(entry.getKey(), entry.getValue());
        }
    }


    public void escapeData(Map<String, String> inParams, SvcLogicContext ctx)
        throws SvcLogicException {
        log.trace("Received escapeData call with params : " + inParams);
        String responsePrefix = inParams.get(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX);
        try {
            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix + ".") : "";
            String unEscapeData = inParams.get(ConfigGeneratorConstant.INPUT_PARAM_UNESCAPE_DATA);
            String dataType = inParams.get(ConfigGeneratorConstant.INPUT_PARAM_DATA_TYPE);

            String escapedData = tryFetchEscapedData(unEscapeData, dataType);
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_ESCAPE_DATA,
                escapedData);
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS,
                ConfigGeneratorConstant.OUTPUT_STATUS_SUCCESS);
            log.trace("Data escapeData Successfully :" + ctx.getAttributeKeySet());
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS,
                ConfigGeneratorConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_ERROR_MESSAGE,
                e.getMessage());
            log.error("Failed in escapeData Conversion", e);
            throw new SvcLogicException(e.getMessage());
        }
    }

    private String tryFetchEscapedData(String unEscapeData, String dataType) throws InvalidParameterException {
        validateInput(unEscapeData, dataType);
        if (ConfigGeneratorConstant.DATA_TYPE_JSON.equalsIgnoreCase(dataType)) {
            return StringEscapeUtils.escapeJavaScript(unEscapeData);
        } else if (ConfigGeneratorConstant.DATA_TYPE_XML.equalsIgnoreCase(dataType)) {
            return StringEscapeUtils.escapeXml(unEscapeData);
        } else if (ConfigGeneratorConstant.DATA_TYPE_SQL.equalsIgnoreCase(dataType)) {
            return EscapeUtils.escapeSql(unEscapeData);
        } else {
            throw new InvalidParameterException(DATA_TYPE_STR + ConfigGeneratorConstant.INPUT_PARAM_DATA_TYPE
                + ") param  value (" + dataType
                + ")is not supported  for escapeData conversion.");
        }
    }

    private void validateInput(String unEscapeData, String dataType) throws InvalidParameterException {
        if (StringUtils.isBlank(unEscapeData)) {
            throw new InvalidParameterException("Unescape (" + ConfigGeneratorConstant.INPUT_PARAM_UNESCAPE_DATA
                + ") param is missing for escapeData conversion." + unEscapeData);
        }
        if (StringUtils.isBlank(dataType)) {
            throw new InvalidParameterException(DATA_TYPE_STR + ConfigGeneratorConstant.INPUT_PARAM_DATA_TYPE
                + ")param is missing for escapeData conversion.");
        }
    }

    public void unEscapeData(Map<String, String> inParams, SvcLogicContext ctx)
        throws SvcLogicException {
        log.trace("Received unEscapeData call with params : " + inParams);
        String responsePrefix = inParams.get(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX);
        try {
            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix + ".") : "";
            String escapeData = inParams.get(ConfigGeneratorConstant.INPUT_PARAM_ESCAPE_DATA);
            String dataType = inParams.get(ConfigGeneratorConstant.INPUT_PARAM_DATA_TYPE);
            String unEscapedData = tryFetchUnescapedData(escapeData, dataType);

            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_UNESCAPE_DATA,
                unEscapedData);
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS,
                ConfigGeneratorConstant.OUTPUT_STATUS_SUCCESS);
            log.trace("Converted unEscapeData Successfully :" + ctx.getAttributeKeySet());
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS,
                ConfigGeneratorConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_ERROR_MESSAGE,
                e.getMessage());
            log.error("Failed in unEscapeData Conversion", e);
            throw new SvcLogicException(e.getMessage());
        }
    }

    private String tryFetchUnescapedData(String escapeData, String dataType) throws InvalidParameterException {
        if (StringUtils.isBlank(escapeData)) {
            throw new InvalidParameterException("Escape (" + ConfigGeneratorConstant.INPUT_PARAM_ESCAPE_DATA
                + ") param is missing for escapeData conversion.");
        }

        if (StringUtils.isBlank(dataType)) {
            throw new InvalidParameterException(DATA_TYPE_STR + ConfigGeneratorConstant.INPUT_PARAM_DATA_TYPE
                + ")param is missing for escapeData conversion.");
        }
        if (ConfigGeneratorConstant.DATA_TYPE_JSON.equalsIgnoreCase(dataType)) {
            return StringEscapeUtils.unescapeJavaScript(escapeData);
        } else if (ConfigGeneratorConstant.DATA_TYPE_XML.equalsIgnoreCase(dataType)) {
            return StringEscapeUtils.unescapeXml(escapeData);
        } else {
            throw new InvalidParameterException(DATA_TYPE_STR + ConfigGeneratorConstant.INPUT_PARAM_DATA_TYPE
                + ") param  value (" + dataType
                + ")is not supported  for unEscapeData conversion.");
        }
    }


    public void convertContextToJson(Map<String, String> inParams, SvcLogicContext ctx)
        throws SvcLogicException {
        log.trace("Received convertContextToJson call with params : " + inParams);
        String responsePrefix = inParams.get(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX);
        String contextKey = inParams.get(ConfigGeneratorConstant.INPUT_PARAM_CONTEXT_KEY);
        try {
            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix + ".") : "";

            ObjectMapper mapper = new ObjectMapper();
            ObjectNode objectNode = mapper.createObjectNode();

            Set<String> keys = ctx.getAttributeKeySet();
            for (String key : keys) {
                if (key.startsWith(contextKey + ".")) {
                    String objkey = key.replaceFirst(contextKey + ".", "");
                    objectNode.put(objkey, ctx.getAttribute(key));

                }
            }
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.INPUT_PARAM_JSON_CONTENT,
                objectNode.toString());
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS,
                ConfigGeneratorConstant.OUTPUT_STATUS_SUCCESS);
            log.trace("convertContextToJson Successful");
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS,
                ConfigGeneratorConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_ERROR_MESSAGE,
                e.getMessage());
            log.error("Failed in convertContextToJson", e);
            throw new SvcLogicException(e.getMessage());
        }
    }

}
