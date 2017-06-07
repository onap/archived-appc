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

package org.openecomp.appc.listener.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import org.json.JSONObject;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Mapper {

    private static final EELFLogger LOG = EELFManager.getInstance().getLogger(Mapper.class);

    private static ObjectMapper mapper = new ObjectMapper();

    /**
     * @return The object mapper that we are using.
     */
    public static ObjectMapper getMapper() {
        return mapper;
    }

    /**
     * Convert a String to a DcaeMessage
     * 
     * @param data
     *            The json string to try and parse
     * @return A DcaeMessage from the json string or null if it could not
     */
    public static <T> T mapOne(String data, Class<T> cls) {
        try {
            return mapper.readValue(data, cls);
        } catch (Exception e) {
            LOG.warn(String.format("Could not map [ %s ] to %s", data, cls.getName()), e);
            return null;
        }
    }

    public static <T> List<T> mapList(List<String> data, Class<T> cls) {
        List<T> out = new ArrayList<T>();
        for (String s : data) {
            T tmp = Mapper.mapOne(s, cls);
            if (tmp != null) {
                out.add(tmp);
            }
        }
        return out;
    }

    /**
     * Convenience method to try and convert objects to json String
     * 
     * @param obj
     *            The object to try and convert
     * @return A json string representing the object or null if it could not be converted
     */
    public static String toJsonString(Object obj) {
        String jsonStr;
        try {
            if (obj instanceof JSONObject) {
                jsonStr = obj.toString();
            }else {
                jsonStr = mapper.writeValueAsString(obj);
            }
            return jsonStr;
        } catch (Exception e) {
            LOG.warn(String.format("Could not map %s to JSONObject.", obj), e);
            return null;
        }
    }

    public static JSONObject toJsonObject(Object obj) {
        String jsonStr;
        try {
            if (obj.getClass().equals(String.class)) {
                jsonStr = (String) obj;
            } else {
                jsonStr = mapper.writeValueAsString(obj);
            }
            return new JSONObject(jsonStr);
        } catch (Exception e) {
            LOG.warn(String.format("Could not map %s to JSONObject.", obj), e);
            return null;
        }
    }
    public static JsonNode toJsonNodeFromJsonString(String jsonStr) {
        JsonNode jsonNode = null;
        if(jsonStr != null) {
            try {
                jsonNode = mapper.readTree(jsonStr);
            } catch (IOException e) {
                LOG.warn(String.format("Could not map %s to jsonNode.", jsonStr), e);
            }
        }
        return jsonNode;
    }
    public static JsonNode toJsonNode(Object obj) {
        JsonNode jsonNode = null;
        String jsonStr = toJsonString(obj);
        if(jsonStr != null) {
            try {
                jsonNode = mapper.readTree(jsonStr);
            } catch (IOException e) {
                LOG.warn(String.format("Could not map %s to JSONObject.", obj), e);
            }
        }
        return jsonNode;
    }
}
