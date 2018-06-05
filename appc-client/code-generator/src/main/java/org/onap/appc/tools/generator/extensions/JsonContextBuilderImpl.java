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

package org.onap.appc.tools.generator.extensions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.onap.appc.tools.generator.api.ContextBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class JsonContextBuilderImpl implements ContextBuilder {

    @Override
    public Map<String, Object> buildContext(URL sourceURL, String contextConf) throws IOException {
        //read json file
        ObjectMapper mapper = new ObjectMapper();
        JsonNode model = mapper.readTree(sourceURL);

        //get context config file
        Properties properties = new Properties();
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classloader.getResourceAsStream(contextConf);
        if(inputStream == null){
            throw new IOException(String.format("The file [%s] cannot be found in the class path",contextConf));
        }
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
