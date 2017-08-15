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

package org.openecomp.sdnc.config.generator.tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;



public class JSONTool {

    private static final  EELFLogger log = EELFManager.getInstance().getLogger(JSONTool.class);

    public static Map<String, String> convertToProperties(String s) throws JSONException {
        return convertToProperties(s, null);
    }

    public static Map<String, String> convertToProperties(String s,List<String> blockKeys) throws JSONException {
        JSONObject json = new JSONObject(s);
        Map<String, String> mm = new HashMap<String, String>();

        Map<String, Object> wm = new HashMap<String, Object>();
        Iterator<String> ii = json.keys();
        while (ii.hasNext()) {
            String key1 = ii.next();
            wm.put(key1, json.get(key1));


        }

        while (!wm.isEmpty())
            for (String key : new ArrayList<>(wm.keySet())) {
                Object o = wm.get(key);
                wm.remove(key);


                if(blockKeys != null && blockKeys.contains(key) && o != null){
                    //log.info("Adding JSON Block Keys : " + key + "=" + o.toString());
                    mm.put("block_" +key,o.toString());
                }

                if (o instanceof Boolean || o instanceof Number || o instanceof String) {
                    mm.put(key, o.toString());
                    //log.info("Added property: " + key + ": " + o.toString());
                }

                else if (o instanceof JSONObject) {
                    JSONObject jo = (JSONObject) o;
                    Iterator<String> i = jo.keys();
                    while (i.hasNext()) {
                        String key1 = i.next();
                        wm.put(key + "." + key1, jo.get(key1));
                    }
                }

                else if (o instanceof JSONArray) {
                    JSONArray ja = (JSONArray) o;
                    mm.put("size_"+key, String.valueOf(ja.length()));

                    //log.info("Added property: " + key + "_length" + ": " + String.valueOf(ja.length()));

                    for (int i = 0; i < ja.length(); i++)
                        wm.put(key + '[' + i + ']', ja.get(i));
                }
            }

        return mm;
    }
    
    /*
    public static Map<String, String> convertToProperties1(String s,List<String> blockKeys) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        
        JsonNode rootNode = objectMapper.readTree(s);
        
        Map<String, String> mm = new HashMap<String, String>();

        Map<String, Object> wm = new HashMap<String, Object>();
        Iterator<String> ii = rootNode.fieldNames();
        while (ii.hasNext()) {
            String key1 = ii.next();
            wm.put(key1, rootNode.get(key1));


        }

        while (!wm.isEmpty())
            for (String key : new ArrayList<>(wm.keySet())) {
                Object o = wm.get(key);
                wm.remove(key);


                if(blockKeys != null && blockKeys.contains(key) && o != null){
                    //log.info("Adding JSON Block Keys : " + key + "=" + o.toString());
                    mm.put("block_" +key,o.toString());
                }

                if (o instanceof Boolean || o instanceof Number || o instanceof String) {
                    mm.put(key, o.toString());
                    //log.info("Added property: " + key + ": " + o.toString());
                }

                else if (o instanceof JSONObject) {
                    JSONObject jo = (JSONObject) o;
                    Iterator<String> i = jo.keys();
                    while (i.hasNext()) {
                        String key1 = i.next();
                        wm.put(key + "." + key1, jo.get(key1));
                    }
                }

                else if (o instanceof JSONArray) {
                    JSONArray ja = (JSONArray) o;
                    mm.put("size_"+key, String.valueOf(ja.length()));

                    //log.info("Added property: " + key + "_length" + ": " + String.valueOf(ja.length()));

                    for (int i = 0; i < ja.length(); i++)
                        wm.put(key + '[' + i + ']', ja.get(i));
                }
            }

        return mm;
    }
*/

}
