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

package org.onap.sdnc.config.generator.merge;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import java.nio.charset.Charset;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;
import org.onap.sdnc.config.generator.ConfigGeneratorConstant;
import org.onap.sdnc.config.generator.tool.EscapeUtils;
import org.onap.sdnc.config.generator.tool.JSONTool;
import org.onap.sdnc.config.generator.tool.MergeTool;

public class MergeNode implements SvcLogicJavaPlugin {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(MergeNode.class);

    public void mergeDataOnTemplate(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {
        /* TODO implement this method */
    }

    public void mergeJsonDataOnTemplate(Map<String, String> inParams, SvcLogicContext ctx)
        throws SvcLogicException {
        log.info("Received mergeJsonDataOnTemplate call with params : " + inParams);
        String responsePrefix = inParams.get(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX);
        try {
            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix + ".") : "";
            String jsonData = inParams.get(ConfigGeneratorConstant.INPUT_PARAM_JSON_DATA);
            if (StringUtils.isBlank(jsonData)) {
                throw new ParameterMissingException("JSON Data is missing");
            }

            String templateData = inParams.get(ConfigGeneratorConstant.INPUT_PARAM_TEMPLATE_DATA);
            String templateFile = inParams.get(ConfigGeneratorConstant.INPUT_PARAM_TEMPLATE_FILE);

            if (StringUtils.isBlank(templateData) && StringUtils.isBlank(templateFile)) {
                throw new ParameterMissingException("Template data or Template file is missing");
            }
            if (StringUtils.isBlank(templateData)) {
                templateData = IOUtils.toString(
                    MergeNode.class.getClassLoader().getResourceAsStream(templateFile), "UTF-8");
            }

            Map<String, String> dataMap = JSONTool.convertToProperties(jsonData);
            log.info("Data Maps created :" + dataMap);
            trySetContextAttribute(ctx, responsePrefix, templateData, dataMap);
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS,
                ConfigGeneratorConstant.OUTPUT_STATUS_SUCCESS);
            log.info("Data Merge Successful :" + ctx);
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS,
                ConfigGeneratorConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_ERROR_MESSAGE,
                e.getMessage());
            log.error("Failed in merging data to template", e);
            throw new SvcLogicException(e.getMessage());
        }
    }

    private void trySetContextAttribute(SvcLogicContext ctx, String responsePrefix, String templateData,
        Map<String, String> dataMap) {
        if (dataMap != null) {
            String mergedData = MergeTool.mergeMap2TemplateData(templateData, dataMap);
            if (mergedData != null) {
                // Changed for E2E defect 266908 Quote issue
                ctx.setAttribute(
                    responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_MERGED_DATA,
                    EscapeUtils.unescapeSql(mergedData));
            }
        }
    }

    public void mergeComplexJsonDataOnTemplate(Map<String, String> inParams, SvcLogicContext ctx)
        throws SvcLogicException {
        String responsePrefix = inParams.get(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX);
        try {
            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix + ".") : "";
            String jsonData = inParams.get(ConfigGeneratorConstant.INPUT_PARAM_JSON_DATA);
            if (StringUtils.isBlank(jsonData)) {
                throw new ParameterMissingException("JSON Data is missing");
            }

            String templateData = inParams.get(ConfigGeneratorConstant.INPUT_PARAM_TEMPLATE_DATA);
            String templateFile = inParams.get(ConfigGeneratorConstant.INPUT_PARAM_TEMPLATE_FILE);

            if (StringUtils.isBlank(templateData) && StringUtils.isBlank(templateFile)) {
                throw new ParameterMissingException("Template data or Template file is missing");
            }
            if (StringUtils.isBlank(templateData)) {
                templateData = IOUtils.toString(
                    MergeNode.class.getClassLoader().getResourceAsStream(templateFile),
                    Charset.defaultCharset());
            }

            String templateType = inParams.get(ConfigGeneratorConstant.INPUT_PARAM_TEMPLATE_TYPE);
            String doPrettyOutput =
                inParams.get(ConfigGeneratorConstant.INPUT_PARAM_DO_PRETTY_OUTPUT);

            String mergedData = MergeTool.mergeJson2TemplateData(templateData, jsonData,
                templateType, doPrettyOutput);
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_MERGED_DATA,
                mergedData);

            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS,
                ConfigGeneratorConstant.OUTPUT_STATUS_SUCCESS);
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS,
                ConfigGeneratorConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_ERROR_MESSAGE,
                e.getMessage());
            log.error("Failed in merging data to template", e);
            throw new SvcLogicException(e.getMessage());
        }
    }

    public void mergeYamlDataOnTemplate(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {
        /* TODO implement this method */
    }
}
