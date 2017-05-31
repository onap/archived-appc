/*-
 * ============LICENSE_START=======================================================
 * APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Amdocs
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */

package org.openecomp.appc.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;


public class JsonUtil {
    /**
     * @param valueAsString a valid json Map represented as String
     * @return a flat map that each entry key derived from hierarchy path in the json object and flatted to a dotted separated string.
     *   e.g. "{\"A\":\"A-value\",\"B\":{\"C\":\"B.C-value\",\"D\":\"B.D-value\"}}"; will be represented as {A=A-value, B.C=B.C-value, B.D=B.D-value}
     *   when it required that the input will not be flatted the json string should be formatted as below example:
     *   e.g. "{\"A\":\"A-value\",\"B\":\"{\\\"C\\\":\\\"C-value\\\",\\\"D\\\":\\\"D-value\\\"}\"}" will be represented as {A=A-value, B={"C":"C-value","D":"D-value"}}
     * @throws IOException when the object is not valid json Map
     */
    public static Map<String, String> convertJsonStringToFlatMap(String valueAsString) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Map readValueMap = objectMapper.readValue(valueAsString,Map.class);
        return org.openecomp.appc.util.ObjectMapper.map(readValueMap);
    }
}
