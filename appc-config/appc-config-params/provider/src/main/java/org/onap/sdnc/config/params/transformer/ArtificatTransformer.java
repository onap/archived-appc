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

package org.onap.sdnc.config.params.transformer;

import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
import org.onap.sdnc.config.params.data.PropertyDefinition;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class ArtificatTransformer {


    public String convertPDToYaml(PropertyDefinition propertyDefinition)
            throws JsonParseException, JsonMappingException, IOException {
        String yamlContent = null;
        if (propertyDefinition != null) {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            yamlContent = mapper.writeValueAsString(propertyDefinition);
        }
        return yamlContent;
    }

    public String transformYamlToJson(String yaml)
            throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
        Object obj = yamlReader.readValue(yaml, Object.class);
        ObjectMapper jsonWriter = new ObjectMapper();
        jsonWriter.enable(SerializationFeature.INDENT_OUTPUT);
        return jsonWriter.writeValueAsString(obj);
    }

    public PropertyDefinition convertYAMLToPD(String pdContent)
            throws JsonParseException, JsonMappingException, IOException {
        PropertyDefinition propertyDefinition = null;
        if (StringUtils.isNotBlank(pdContent)) {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            propertyDefinition = mapper.readValue(pdContent, PropertyDefinition.class);
        }
        return propertyDefinition;
    }
}
