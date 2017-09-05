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

package org.openecomp.sdnc.config.generator.transform;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.sdnc.config.generator.ConfigGeneratorConstant;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;


public class XSLTTransformerNode implements SvcLogicJavaPlugin {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(XSLTTransformerNode.class);

    public void transformData(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {
        log.trace("Received convertJson2DGContext call with params : " + inParams);
        String responsePrefix = inParams.get(ConfigGeneratorConstant.INPUT_PARAM_RESPONSE_PRIFIX);
        try {
            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix + ".") : "";

            String templateData = inParams.get(ConfigGeneratorConstant.INPUT_PARAM_TEMPLATE_DATA);

            if (StringUtils.isNotBlank(templateData)) {
                String templateFile = inParams.get(ConfigGeneratorConstant.INPUT_PARAM_TEMPLATE_FILE);
                if (StringUtils.isNotBlank(templateFile)) {
                    templateData = FileUtils.readFileToString(new File(templateFile), Charset.defaultCharset());
                }
            }
            if (StringUtils.isBlank(templateData)) {
                throw new Exception("In-param templateFile/templateData value is missing");
            }

            String requestData = inParams.get(ConfigGeneratorConstant.INPUT_PARAM_REQUEST_DATA);
            if (StringUtils.isBlank(requestData)) {
                throw new Exception("In-param requestData value is missing");
            }

            String transformedData = transform(requestData, templateData);
            log.trace("Transformed Data : "+ transformedData);
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_TRANSFORMED_DATA, transformedData);
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS, ConfigGeneratorConstant.OUTPUT_STATUS_SUCCESS);    
        } catch (Exception e) {
            e.printStackTrace();
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_STATUS,
                    ConfigGeneratorConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + ConfigGeneratorConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
            log.error("Failed in XSLTTransformerNode : " + e.getMessage());
            throw new SvcLogicException(e.getMessage());
        }
    }

    public String transform(String requestData, String templateData)
            throws TransformerConfigurationException, TransformerException {
        StringWriter xmlResultResource = new StringWriter();
        Transformer xmlTransformer = TransformerFactory.newInstance().newTransformer(new StreamSource(new StringReader(templateData)));
        xmlTransformer.transform(new StreamSource(new StringReader(requestData)), new StreamResult(xmlResultResource));
        return xmlResultResource.getBuffer().toString();
    }

}
