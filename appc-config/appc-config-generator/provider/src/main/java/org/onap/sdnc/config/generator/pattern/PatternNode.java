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

package org.openecomp.sdnc.config.generator.pattern;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdnc.config.generator.ConfigGeneratorConstant;
import org.openecomp.sdnc.config.generator.tool.CheckDataTool;
import org.openecomp.sdnc.config.generator.tool.LogParserTool;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;

public class PatternNode implements SvcLogicJavaPlugin {

    private static final  EELFLogger log = EELFManager.getInstance().getLogger(PatternNode.class);

    public void parseErrorLog(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {
        log.info("Received parseErroLog call with params : " + inParams);
        String responsePrefix = inParams.get(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX);
        try{
            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix+".") : "";
            String logData =  inParams.get(ConfigGeneratorConstant.INPUT_PARAM_LOG_DATA);
            if(StringUtils.isBlank(logData)){
                throw new Exception("Log Data is missing");
            }
            LogParserTool logParserTool = new  LogParserTool();
            String parsedError = logParserTool.parseErrorLog(logData);
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_PARSED_ERROR, parsedError);
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS, ConfigGeneratorConstant.OUTPUT_STATUS_SUCCESS);
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS, ConfigGeneratorConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_ERROR_MESSAGE,e.getMessage());
            log.error("Failed in parsing error log " + e.getMessage());
            throw new SvcLogicException(e.getMessage());
        }
    }

    public void checkDataType(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {
        log.info("Received checkDataType call with params : " + inParams);
        String responsePrefix = inParams.get(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX);
        try{
            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix+".") : "";
            String checkData =  inParams.get(ConfigGeneratorConstant.INPUT_PARAM_CHECK_DATA);
            if(StringUtils.isBlank(checkData)){
                throw new Exception("Check Data is missing");
            }
            String dataType = CheckDataTool.checkData(checkData);
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_DATA_TYPE, dataType);
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS, ConfigGeneratorConstant.OUTPUT_STATUS_SUCCESS);
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS, ConfigGeneratorConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_ERROR_MESSAGE,e.getMessage());
            log.error("Failed in checkDataType " + e.getMessage());
            throw new SvcLogicException(e.getMessage());
        }
    }

}
