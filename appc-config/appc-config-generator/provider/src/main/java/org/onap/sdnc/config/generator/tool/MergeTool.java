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

package org.onap.sdnc.config.generator.tool;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;
import org.onap.sdnc.config.generator.ConfigGeneratorConstant;

public class MergeTool {

    private MergeTool() {}

    private static final EELFLogger log = EELFManager.getInstance().getLogger(MergeTool.class);

    public static String mergeMap2TemplateData(String template, Map<String, String> dataMap) {
        log.info("MergeMap2TemplateData Template :" + template + " Maps :" + dataMap);
        StringWriter writer = new StringWriter();
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "string");
        ve.addProperty("string.resource.loader.class", StringResourceLoader.class.getName());
        ve.addProperty("string.resource.loader.repository.static", "false");
        ve.init();

        StringResourceRepository repo = (StringResourceRepository) ve
            .getApplicationAttribute(StringResourceLoader.REPOSITORY_NAME_DEFAULT);
        repo.putStringResource("TemplateResource", template);

        Template t = ve.getTemplate("TemplateResource");
        VelocityContext context = new VelocityContext();
        for (Entry<String, String> entry : dataMap.entrySet()) {
            context.put(entry.getKey(), entry.getValue());
        }
        t.merge(context, writer);
        return writer.toString();
    }

    public static String mergeJson2TemplateData(String template, String jsonData,
        String templateType, String doPrettyOutput) throws IOException {

        String mergedData = template;
        if (StringUtils.isNotBlank(template) && StringUtils.isNotBlank(jsonData)) {
            Velocity.init();

            ObjectMapper mapper = new ObjectMapper();
            CustomJsonNodeFactory f = new CustomJsonNodeFactory();
            mapper.setNodeFactory(f);

            JsonNode jsonObj = mapper.readValue(jsonData, JsonNode.class);

            VelocityContext context = new VelocityContext();
            Iterator<String> ii = jsonObj.fieldNames();
            while (ii.hasNext()) {
                String key = ii.next();
                context.put(key, jsonObj.get(key));
            }

            StringWriter writer = new StringWriter();
            Velocity.evaluate(context, writer, "TemplateData", template);
            writer.flush();
            mergedData = writer.toString();

            if (prettyPrint(templateType, doPrettyOutput)) {
                // Perform Prettying
            }
        }
        return mergedData;
    }

    private static boolean isJsonOrXml(String templateType) {
        return ConfigGeneratorConstant.DATA_TYPE_JSON.equalsIgnoreCase(templateType)
            || ConfigGeneratorConstant.DATA_TYPE_XML.equalsIgnoreCase(templateType);
    }

    private static boolean prettyPrint(String templateType, String doPrettyOutput) {
        return StringUtils.isNotBlank(templateType) && StringUtils.isNotBlank(doPrettyOutput)
            && ConfigGeneratorConstant.Y.equalsIgnoreCase(doPrettyOutput) && isJsonOrXml(templateType);
    }
}
