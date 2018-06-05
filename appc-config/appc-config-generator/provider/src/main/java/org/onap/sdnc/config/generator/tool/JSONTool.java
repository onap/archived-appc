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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;


public class JSONTool {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(JSONTool.class);

    private JSONTool() {
    }

    public static Map<String, String> convertToProperties(String s) throws JSONException {
        return convertToProperties(s, null);
    }

    public static Map<String, String> convertToProperties(String s, List<String> blockKeys)
        throws JSONException {
        JSONObject json = new JSONObject(s);
        Map<String, String> mm = new HashMap<>();
        Map<String, Object> wm = new HashMap<>();

        Iterator<String> ii = json.keys();
        while (ii.hasNext()) {
            String key1 = ii.next();
            wm.put(key1, json.get(key1));
        }
        while (!wm.isEmpty()) {
            for (String key : new ArrayList<>(wm.keySet())) {
                Object o = wm.get(key);
                wm.remove(key);
                tryAddBlockKeys(blockKeys, mm, key, o);
                if (o instanceof Boolean || o instanceof Number || o instanceof String) {
                    mm.put(key, o.toString());
                    log.info("Added property: " + key + ": " + o.toString());
                } else if (o instanceof JSONObject) {
                    fill(wm, key, (JSONObject) o);
                } else if (o instanceof JSONArray) {
                    fill(mm, wm, key, (JSONArray) o);
                }
            }
        }
        return mm;
    }

    private static void tryAddBlockKeys(List<String> blockKeys, Map<String, String> mm, String key, Object o) {
        if (blockKeys != null && blockKeys.contains(key) && o != null) {
            mm.put("block_" + key, o.toString());
            log.info("Adding JSON Block Keys : " + key + "=" + o.toString());
        }
    }

    private static void fill(Map<String, String> mm, Map<String, Object> wm, String key, JSONArray array)
        throws JSONException {
        mm.put("size_" + key, String.valueOf(array.length()));
        log.info("Added property: " + key + "_length" + ": " + array.length());

        for (int i = 0; i < array.length(); i++) {
            wm.put(key + '[' + i + ']', array.get(i));
        }
    }

    private static void fill(Map<String, Object> wm, String key, JSONObject object) throws JSONException {

        Iterator<String> i = object.keys();
        while (i.hasNext()) {
            String key1 = i.next();
            wm.put(key + "." + key1, object.get(key1));
        }
    }
}
