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

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.openecomp.sdnc.config.generator.ConfigGeneratorConstant;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class CheckDataTool {

    public static String checkData(String data){
        boolean isJSON = isJSON(data);
        if(isJSON){
            return ConfigGeneratorConstant.DATA_TYPE_JSON;
        }

        boolean isXML = isXML(data);
        if(isXML){
            return ConfigGeneratorConstant.DATA_TYPE_XML;
        }

        return ConfigGeneratorConstant.DATA_TYPE_TEXT;
    }

    public static boolean isJSON(String data) {
        try {
            new JSONObject(data);
        } catch (JSONException ex) {
            try {
                new JSONArray(data);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;

        //        try {
        //            final ObjectMapper mapper = new ObjectMapper();
        //            mapper.readTree(data);
        //            return true;
        //         } catch (IOException e) {
        //            return false;
        //         }
    }

    public static boolean isXML(String data) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new StringReader(data)));
            return true;
        } catch (Exception ex) {
            return false;
        }

    }

}
