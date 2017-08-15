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

package org.openecomp.sdnc.config.generator.writer;

import java.io.File;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdnc.config.generator.ConfigGeneratorConstant;


import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.openecomp.sdnc.sli.SvcLogicException;
import org.openecomp.sdnc.sli.SvcLogicJavaPlugin;

public class FileWriterNode implements SvcLogicJavaPlugin {

    private static final  EELFLogger log = EELFManager.getInstance().getLogger(FileWriterNode.class);
    
    
    
    public void writeFile(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {
        log.info("Received writeFile call with params : " + inParams);
        String responsePrefix = inParams.get(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX);
        try{
               responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix+".") : "";
               String fileName =  inParams.get(ConfigGeneratorConstant.INPUT_PARAM_FILE_NAME);
               String fileContents =  inParams.get(ConfigGeneratorConstant.INPUT_PARAM_REQUEST_DATA);
               
               
               File file = new File(fileName);
               File filePath = file.getParentFile();
               FileUtils.forceMkdir(filePath);
               
               FileUtils.writeStringToFile(file, fileContents, ConfigGeneratorConstant.STRING_ENCODING);
               
               
               ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS, ConfigGeneratorConstant.OUTPUT_STATUS_SUCCESS);
        } catch (Exception e) {
               ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS, ConfigGeneratorConstant.OUTPUT_STATUS_FAILURE);
               ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_ERROR_MESSAGE,e.getMessage());
               log.error("Failed in writeFile " + e.getMessage());
               throw new SvcLogicException(e.getMessage());
        }
    }


    
}
