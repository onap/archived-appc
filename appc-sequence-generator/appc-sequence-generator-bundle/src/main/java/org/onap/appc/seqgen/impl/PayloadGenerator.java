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

package org.onap.appc.seqgen.impl;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import org.onap.appc.seqgen.dbservices.SequenceGeneratorDBServices;
import org.onap.appc.seqgen.objects.SequenceGeneratorInput;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

import org.apache.velocity.*;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.apache.commons.io.IOUtils;


public class PayloadGenerator {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(PayloadGenerator.class);
    private SequenceGeneratorDBServices dbService;

    public String getPayload(SequenceGeneratorInput input, String vmId, String url)throws Exception {
        SequenceGeneratorDBServices services = new SequenceGeneratorDBServices();
        SvcLogicContext ctx = new SvcLogicContext();
        String payload = null;
        String payloadTemplate = null;
        InputStream data = getClass().getClassLoader().getResourceAsStream("payload.json");
        String payloadTemplatefromRes = IOUtils.toString(data);
        System.out.println("Input template data: " + payloadTemplatefromRes);
        logger.debug("Input template data: " + payloadTemplatefromRes);
        payloadTemplate=payloadTemplatefromRes;

        if (payloadTemplate != null) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

            JsonNode payloadModel = mapper.readTree(payloadTemplate).get("output_payload-model");
            logger.debug("Payload template:"+payloadModel.toString());

            JsonNode modelName = payloadModel.get("sequence-generator");
            logger.debug("Payload Model Name:" + modelName);

            JsonNode action = payloadModel.path("action-list");
            if (action.isArray()) {
                for (JsonNode nodes : action) {
                    String actionName = nodes.path("action").asText();
                    JsonNode payloadValues = nodes.path("payload-fields");
                    logger.debug("Action Name: " + actionName + "-" + "Payload values:  " + payloadValues);
                    String actionValue = input.getRequestInfo().getAction();
                    if (actionName.equalsIgnoreCase(actionValue)) {
                        JsonNode template = nodes.path("payload-fields");
                        logger.debug("Payload template:" + template);
                        payload = generatePayloadFromTemplate(template.toString(),input,url);
                        break;

                    }

                }
            }

        }

        return payload;
    }

    public String generatePayloadFromTemplate(String template,SequenceGeneratorInput input,String vmId) throws ParseException {

        String payload = null;

        RuntimeServices runtimeServices = RuntimeSingleton.getRuntimeServices();
        StringReader reader = new StringReader(template);
        SimpleNode node = runtimeServices.parse(reader,template);

        Template templateData = new Template();
        templateData.setRuntimeServices(runtimeServices);
        templateData.setData(node);
        templateData.initDocument();

        VelocityContext vc = new VelocityContext();
        String identityUrl = input.getInventoryModel().getVnf().getIdentityUrl();
        logger.debug("IdentityUrl:" + identityUrl);
        logger.debug("Vm-Id:" + vmId);
        vc.put("identity-url", identityUrl);
        vc.put("vm-id", vmId);

        StringWriter stringWriter = new StringWriter();
        templateData.merge(vc, stringWriter);

        payload= stringWriter.toString();
        logger.debug("Payload Data:" +stringWriter.toString());

        return payload;

    }
}
