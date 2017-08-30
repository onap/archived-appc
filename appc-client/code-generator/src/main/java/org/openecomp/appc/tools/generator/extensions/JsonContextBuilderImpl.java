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

package org.openecomp.appc.tools.generator.extensions;

import org.openecomp.appc.tools.generator.api.ContextBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class JsonContextBuilderImpl implements ContextBuilder {

    @Override
    public Map<String, Object> buildContext(String sourceFile, String contextConf) throws IOException {
        //read json file
        ObjectMapper mapper = new ObjectMapper();
        JsonNode model = mapper.readTree(new File(sourceFile));

        //get context config file
        Properties properties = new Properties();
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classloader.getResourceAsStream(contextConf);
        properties.load(inputStream);

        //get context related properties
        ObjectNode metadata = mapper.createObjectNode();
        for (String key : properties.stringPropertyNames()) {
            if (key.startsWith("ctx")) {
                metadata.put(key.replaceFirst("ctx.", ""), properties.getProperty(key));
            }
        }

        //create context and populate it
        Map<String, Object> map = new HashMap<>();
        map.put("model", model);
        map.put("metadata", metadata);
        return map;
    }
}
