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

package org.openecomp.sdnc.config.generator.merge;

import java.nio.charset.Charset;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdnc.config.generator.ConfigGeneratorConstant;
import org.openecomp.sdnc.config.generator.tool.JSONTool;
import org.openecomp.sdnc.config.generator.tool.MergeTool;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;

public class MergeNode implements SvcLogicJavaPlugin {

    private static final  EELFLogger log = EELFManager.getInstance().getLogger(MergeNode.class);

    public void mergeDataOnTemplate(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {

    }

    public void mergeJsonDataOnTemplate(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {
        log.info("Received mergeJsonDataOnTemplate call with params : " + inParams);
        String responsePrefix = inParams.get(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX);
        try{
            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix+".") : "";
            String jsonData =  inParams.get(ConfigGeneratorConstant.INPUT_PARAM_JSON_DATA);
            if(StringUtils.isBlank(jsonData)){
                throw new Exception("JSON Data is missing");
            }

            String templateData =  inParams.get(ConfigGeneratorConstant.INPUT_PARAM_TEMPLATE_DATA);
            String templateFile =  inParams.get(ConfigGeneratorConstant.INPUT_PARAM_TEMPLATE_FILE);

            if(StringUtils.isBlank(templateData) && StringUtils.isBlank(templateFile)){
                throw new Exception("Template data or Template file is missing");
            }
            if(StringUtils.isBlank(templateData)){
                String path = MergeNode.class.getClassLoader().getResource(".").toString();
                templateData = IOUtils.toString(MergeNode.class.getClassLoader().getResourceAsStream(templateFile));
            }

            String templateType =  inParams.get(ConfigGeneratorConstant.INPUT_PARAM_TEMPLATE_TYPE);

            Map<String, String> dataMap  = JSONTool.convertToProperties(jsonData);
            log.info("Data Maps created :" + dataMap);
            if(dataMap != null){
                String mergedData = MergeTool.mergeMap2TemplateData(templateData, dataMap);
                if(mergedData != null){
                    ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_MERGED_DATA,mergedData);
                }
            }
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS, ConfigGeneratorConstant.OUTPUT_STATUS_SUCCESS);
            log.info("Data Merge Successful :" + ctx);
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS, ConfigGeneratorConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_ERROR_MESSAGE,e.getMessage());
            log.error("Failed in merging data to template " + e.getMessage());
            throw new SvcLogicException(e.getMessage());
        }
    }
    
    public void mergeComplexJsonDataOnTemplate(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {
        //log.info("Received mergeJsonComplexDataOnTemplate call with params : " + inParams);
        String responsePrefix = inParams.get(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX);
        try{
            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix+".") : "";
            String jsonData =  inParams.get(ConfigGeneratorConstant.INPUT_PARAM_JSON_DATA);
            if(StringUtils.isBlank(jsonData)){
                throw new Exception("JSON Data is missing");
            }

            String templateData =  inParams.get(ConfigGeneratorConstant.INPUT_PARAM_TEMPLATE_DATA);
            String templateFile =  inParams.get(ConfigGeneratorConstant.INPUT_PARAM_TEMPLATE_FILE);

            if(StringUtils.isBlank(templateData) && StringUtils.isBlank(templateFile)){
                throw new Exception("Template data or Template file is missing");
            }
            if(StringUtils.isBlank(templateData)){
                //String path = MergeNode.class.getClassLoader().getResource(".").toString();
                templateData = IOUtils.toString(MergeNode.class.getClassLoader().getResourceAsStream(templateFile), Charset.defaultCharset());
            }

            String templateType =  inParams.get(ConfigGeneratorConstant.INPUT_PARAM_TEMPLATE_TYPE);
            String doPrettyOutput =  inParams.get(ConfigGeneratorConstant.INPUT_PARAM_DO_PRETTY_OUTPUT);
            
            String mergedData = MergeTool.mergeJson2TemplateData(templateData, jsonData, templateType, doPrettyOutput);
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_MERGED_DATA,mergedData);
            
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS, ConfigGeneratorConstant.OUTPUT_STATUS_SUCCESS);
            //log.info("Data Merge Successful :" + ctx);
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS, ConfigGeneratorConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_ERROR_MESSAGE,e.getMessage());
            log.error("Failed in merging data to template " + e.getMessage());
            throw new SvcLogicException(e.getMessage());
        }
    }

    public void mergeYamlDataOnTemplate(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {

    }

}
