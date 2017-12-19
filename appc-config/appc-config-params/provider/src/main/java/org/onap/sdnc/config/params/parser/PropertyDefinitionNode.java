/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.onap.sdnc.config.params.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;
import org.onap.sdnc.config.params.ParamsHandlerConstant;
import org.onap.sdnc.config.params.data.Parameter;
import org.onap.sdnc.config.params.data.PropertyDefinition;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class PropertyDefinitionNode implements SvcLogicJavaPlugin {


    private static final EELFLogger log =
            EELFManager.getInstance().getLogger(PropertyDefinitionNode.class);

    public void processMissingParamKeys(Map<String, String> inParams, SvcLogicContext ctx)
            throws SvcLogicException {
        log.info("Received processParamKeys call with params : " + inParams);
        String responsePrefix = inParams.get(ParamsHandlerConstant.INPUT_PARAM_RESPONSE_PRIFIX);
        try {
            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix + ".") : "";

            String requestParamJson = inParams.get(ParamsHandlerConstant.INPUT_PARAM_JSON_DATA);
            String pdContent = inParams.get(ParamsHandlerConstant.INPUT_PARAM_PD_CONTENT);

            if (StringUtils.isBlank(pdContent)) {
                throw new Exception("Request Param (pdContent) is Missing ..");
            }

            if (StringUtils.isBlank(requestParamJson)) {
                throw new Exception("Request Param (jsonData) is Missing ..");
            }

            PropertyDefinition propertyDefinition = parsePDContent(pdContent);
            if (propertyDefinition != null) {
                requestParamJson =
                        mergeMissingRequestParamFromPD(propertyDefinition, requestParamJson);
                ctx.setAttribute(
                        responsePrefix + ParamsHandlerConstant.OUTPUT_PARAM_CONFIGURATION_PARAMETER,
                        requestParamJson);
            }

            ctx.setAttribute(responsePrefix + ParamsHandlerConstant.OUTPUT_PARAM_STATUS,
                    ParamsHandlerConstant.OUTPUT_STATUS_SUCCESS);
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + ParamsHandlerConstant.OUTPUT_PARAM_STATUS,
                    ParamsHandlerConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + ParamsHandlerConstant.OUTPUT_PARAM_ERROR_MESSAGE,
                    e.getMessage());
            log.error("Failed in merging data to template " + e.getMessage());
            throw new SvcLogicException(e.getMessage());
        }
    }

    public void processExternalSystemParamKeys(Map<String, String> inParams, SvcLogicContext ctx)
            throws SvcLogicException {
        log.info("Received processExternalSystemParamKeys call with params : " + inParams);
        log.debug(
                "Source sytem name passed in inParams will be ignored!!Source will be obtained from PD block!");
        String responsePrefix = inParams.get(ParamsHandlerConstant.INPUT_PARAM_RESPONSE_PRIFIX);
        try {
            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix + ".") : "";

            String requestParamJson = inParams.get(ParamsHandlerConstant.INPUT_PARAM_JSON_DATA);
            String pdContent = inParams.get(ParamsHandlerConstant.INPUT_PARAM_PD_CONTENT);
            String systemName = inParams.get(ParamsHandlerConstant.INPUT_PARAM_SYSTEM_NAME);


            if (StringUtils.isBlank(pdContent)) {
                throw new Exception("Request Param (pdContent) is Missing ..");
            }

            if (StringUtils.isBlank(requestParamJson)) {
                throw new Exception("Request Param (jsonData) is Missing ..");
            }

            if (StringUtils.isBlank(systemName)) {
                throw new Exception("Request Param (systemName) is Missing ..");
            }

            PropertyDefinition propertyDefinition = parsePDContent(pdContent);
            if (propertyDefinition != null) {
                getSystemRequestParamInfoFromPD(propertyDefinition, requestParamJson, ctx);
            }

            ctx.setAttribute(responsePrefix + ParamsHandlerConstant.OUTPUT_PARAM_STATUS,
                    ParamsHandlerConstant.OUTPUT_STATUS_SUCCESS);
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + ParamsHandlerConstant.OUTPUT_PARAM_STATUS,
                    ParamsHandlerConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + ParamsHandlerConstant.OUTPUT_PARAM_ERROR_MESSAGE,
                    e.getMessage());
            log.error("Failed in merging data to template " + e.getMessage());
            throw new SvcLogicException(e.getMessage());
        }
    }


    public void mergeJsonData(Map<String, String> inParams, SvcLogicContext ctx)
            throws SvcLogicException {
        log.info("Received mergeJsonData call with params : " + inParams);
        String responsePrefix = inParams.get(ParamsHandlerConstant.INPUT_PARAM_RESPONSE_PRIFIX);
        try {
            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix + ".") : "";

            String requestParamJson = inParams.get(ParamsHandlerConstant.INPUT_PARAM_JSON_DATA);
            String mergeJsonData = inParams.get(ParamsHandlerConstant.INPUT_PARAM_MERGE__JSON_DATA);

            if (StringUtils.isBlank(requestParamJson)) {
                throw new Exception("Request Param (jsonData) is Missing ..");
            }

            if (StringUtils.isBlank(mergeJsonData)) {
                throw new Exception("Request Param (mergeJsonData) is Missing ..");
            }

            requestParamJson = mergeJson(requestParamJson, mergeJsonData);
            ctx.setAttribute(
                    responsePrefix + ParamsHandlerConstant.OUTPUT_PARAM_CONFIGURATION_PARAMETER,
                    requestParamJson);
            ctx.setAttribute(responsePrefix + ParamsHandlerConstant.OUTPUT_PARAM_STATUS,
                    ParamsHandlerConstant.OUTPUT_STATUS_SUCCESS);
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + ParamsHandlerConstant.OUTPUT_PARAM_STATUS,
                    ParamsHandlerConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + ParamsHandlerConstant.OUTPUT_PARAM_ERROR_MESSAGE,
                    e.getMessage());
            log.error("Failed in merging data to template " + e.getMessage());
            throw new SvcLogicException(e.getMessage());
        }
    }


    /* */

    private PropertyDefinition parsePDContent(String pdContent)
            throws JsonParseException, JsonMappingException, IOException {
        PropertyDefinition propertyDefinition = null;
        if (StringUtils.isNotBlank(pdContent)) {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            propertyDefinition = mapper.readValue(pdContent, PropertyDefinition.class);
        }
        return propertyDefinition;
    }


    private String mergeMissingRequestParamFromPD(PropertyDefinition propertyDefinition,
            String requestParamJson) throws Exception {

        if (propertyDefinition == null) {
            throw new Exception("PropertyDefinition is Missing ..");
        }

        if (StringUtils.isBlank(requestParamJson)) {
            throw new Exception("Request Param is Missing ..");
        }

        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> requestParamMap = mapper.readValue(requestParamJson, HashMap.class);
        if (requestParamMap != null) {
            List<Parameter> parameters = propertyDefinition.getParameters();
            for (Parameter parameter : parameters) {
                if (parameter != null) {

                    log.info("Checking Key " + parameter.getName() + ":: Source :"
                            + parameter.getSource());
                    // Add Only non external system keys,If it is not present in request Params
                    if (!requestParamMap.containsKey(parameter.getName())
                            && StringUtils.isBlank(parameter.getSource())) {
                        log.info("Adding New Key " + parameter.getName());
                        requestParamMap.put(parameter.getName(), parameter.getDefaultValue());
                    }
                }
            }
            requestParamJson = mapper.writeValueAsString(requestParamMap);
            log.info("Processed Request Param " + requestParamJson);
        }

        return requestParamJson;
    }

    private void getSystemRequestParamInfoFromPD(PropertyDefinition propertyDefinition,
            String requestParamJson, SvcLogicContext ctx) throws Exception {

        if (propertyDefinition == null) {
            throw new Exception("PropertyDefinition is Missing ..");
        }

        if (StringUtils.isBlank(requestParamJson)) {
            throw new Exception("Request Param is Missing ..");
        }

        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> requestParamMap = mapper.readValue(requestParamJson, HashMap.class);
        Map<String, List<String>> systemKeysMap = new HashMap<String, List<String>>();
        if (requestParamMap != null) {
            List<Parameter> parameters = propertyDefinition.getParameters();

            List<String> externalSystemKeys = new ArrayList<String>();
            for (Parameter parameter : parameters) {
                if (parameter != null) {
                    if (!requestParamMap.containsKey(parameter.getName())
                            && StringUtils.isNotBlank(parameter.getSource())
                            && !StringUtils.equalsIgnoreCase(parameter.getSource(), "Manual")) {
                        String source = parameter.getSource();
                        if (StringUtils.equalsIgnoreCase(source, "A&AI"))
                            source = "AAI";
                        source = StringUtils.upperCase(source);
                        if (systemKeysMap.containsKey(source)) {
                            log.info("Adding New System Key " + parameter.getName() + ":"
                                    + mapper.writeValueAsString(parameter));
                            List l = systemKeysMap.get(source);
                            if (null != l) {
                                l.add(parameter.getName());
                                systemKeysMap.put(source, l);
                            }
                        } else {
                            log.info("Creating/Adding New System Key " + parameter.getName() + ":"
                                    + mapper.writeValueAsString(parameter));
                            List<String> l = new ArrayList<String>();
                            if (null != l) {
                                l.add(parameter.getName());
                                systemKeysMap.put(source, l);
                            }
                        }

                        externalSystemKeys.add(parameter.getName());
                        ctx.setAttribute(source + "." + parameter.getName(),
                                mapper.writeValueAsString(parameter));
                    }
                }
            }
            for (String sysName : systemKeysMap.keySet()) {
                String systemKeys = sysName + ".keys";
                ctx.setAttribute(systemKeys, mapper.writeValueAsString(systemKeysMap.get(sysName)));
            }
            /*
             * String systemKeys = systemName+".keys"; ctx.setAttribute(systemKeys,
             * mapper.writeValueAsString(externalSystemKeys));
             */

        }
    }


    private String mergeJson(String requestParamJson, String systemParamJson) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> requestParamMap = mapper.readValue(requestParamJson, HashMap.class);
        if (requestParamMap != null) {
            Map<String, String> systemParamMap = mapper.readValue(systemParamJson, HashMap.class);
            if (systemParamMap != null) {
                for (String systemParamKey : systemParamMap.keySet()) {
                    log.trace("Megging System Key Values " + systemParamKey);
                    requestParamMap.put(systemParamKey, systemParamMap.get(systemParamKey));
                }
            }
            requestParamJson = mapper.writeValueAsString(requestParamMap);
            log.info("Processed Request Param " + requestParamJson);
        }

        return requestParamJson;
    }


    public void validateParams(Map<String, String> inParams, SvcLogicContext ctx)
            throws SvcLogicException

    {
        String responsePrefix = inParams.get(ParamsHandlerConstant.INPUT_PARAM_RESPONSE_PRIFIX);
        String pdContent = inParams.get(ParamsHandlerConstant.INPUT_PARAM_PD_CONTENT);
        String configParams =
                inParams.get(ParamsHandlerConstant.OUTPUT_PARAM_CONFIGURATION_PARAMETER);
        responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix + ".") : "";
        log.info("Processed pdContent Param " + pdContent);
        log.info("Processed config Param " + configParams);

        try {
            if (StringUtils.isBlank(pdContent)) {
                throw new Exception("Request Param (pdContent) is Missing ..");
            }

            if (StringUtils.isBlank(configParams)) {
                throw new Exception("Request Param (configParams) is Missing ..");
            }
            PropertyDefinition propertyDefinition = parsePDContent(pdContent);
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> requestParamMap = mapper.readValue(configParams, HashMap.class);
            List<Parameter> parameters = propertyDefinition.getParameters();
            Map<String, String> missingKeys = new HashMap<String, String>();
            for (Parameter parameter : parameters) {
                if (parameter != null) {
                    if (parameter.isRequired()) {
                        if (!requestParamMap.containsKey(parameter.getName())) {
                            missingKeys.put(parameter.getName(), parameter.getSource());
                        } else {
                            if ((requestParamMap.get(parameter.getName()) == null)
                                    || (requestParamMap.get(parameter.getName()).equals("")))

                            {
                                missingKeys.put(parameter.getName(), parameter.getSource());
                            }

                        }

                    }

                }

            }

            if (missingKeys != null && missingKeys.size() > 0) {

                String requiredFields = mapper.writeValueAsString(missingKeys);
                log.info(" Below mentioned keys and respective  source type are mandatory");
                log.info(requiredFields);

                ctx.setAttribute(responsePrefix + ParamsHandlerConstant.OUTPUT_PARAM_STATUS,
                        ParamsHandlerConstant.OUTPUT_STATUS_FAILURE);
                ctx.setAttribute(ParamsHandlerConstant.OUTPUT_PARAM_STATUS,
                        ParamsHandlerConstant.OUTPUT_STATUS_FAILURE);
                ctx.setAttribute(responsePrefix + ParamsHandlerConstant.OUTPUT_PARAM_ERROR_MESSAGE,
                        "Missing Mandatory Keys and source are" + requiredFields);
                throw new SvcLogicException(
                        " Missing  Mandatory Keys and source are" + requiredFields);


            } else {
                log.info("success ");
                ctx.setAttribute(ParamsHandlerConstant.OUTPUT_PARAM_STATUS,
                        ParamsHandlerConstant.OUTPUT_STATUS_SUCCESS);

            }

        } catch (Exception e) {

            ctx.setAttribute(responsePrefix + ParamsHandlerConstant.OUTPUT_PARAM_STATUS,
                    ParamsHandlerConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(ParamsHandlerConstant.OUTPUT_PARAM_STATUS,
                    ParamsHandlerConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + ParamsHandlerConstant.OUTPUT_PARAM_ERROR_MESSAGE,
                    e.getMessage());
            log.error(e.getMessage());
            throw new SvcLogicException(e.getMessage());
        }

    }



}
