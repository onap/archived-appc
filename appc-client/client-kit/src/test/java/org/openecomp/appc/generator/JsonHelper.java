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

package org.openecomp.appc.generator;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class JsonHelper {

    private enum PopulateType{
        PARAMETER,
        SCHEMA_REF,
        ;
    }

    public static void populateRpcInputOutputParamsFromJson(final JsonNode jsonNode, Map<String, Set<String>> jsonBodyInputParams, Map<String, Set<String>> jsonBodyOutputParams) {
        populateRpcInputOutputParamsFromJson(jsonNode,jsonBodyInputParams, jsonBodyOutputParams,PopulateType.PARAMETER,false);
    }
    public static void populateRpcInputOutputSchemaRefFromJson(final JsonNode jsonNode, Map<String, Set<String>> jsonBodyInputParams, Map<String, Set<String>> jsonBodyOutputParams) {
        populateRpcInputOutputParamsFromJson(jsonNode,jsonBodyInputParams, jsonBodyOutputParams,PopulateType.SCHEMA_REF,true);
    }
    public static void populateRpcInputOutputParamsFromJson(final JsonNode jsonNode, Map<String, Set<String>> jsonBodyInputParams, Map<String, Set<String>> jsonBodyOutputParams,PopulateType populateType,boolean normalizeRpcOperation) {
        //get all yang2json Input and output Params and populate jsonBodyInputParams & jsonBodyOutputParams
        for(Iterator<JsonNode> pathsElements = jsonNode.get("paths").elements(); pathsElements.hasNext(); ){
            JsonNode pathNode = pathsElements.next();
            String rpcOperation = pathNode.get("post").get("operationId").textValue();
            String bodyInputSchemaRef = pathNode.get("post").get("parameters").get(0).get("schema").get("properties").get("input").get("$ref").textValue();
            String bodyInputSchemaRefSuffix = JsonHelper.getStringSuffix(bodyInputSchemaRef,"/");
            rpcOperation = normalizeRpcOperation ? rpcOperation.replaceAll(Pattern.quote("-"),"").toLowerCase() : rpcOperation;

            String bodyOutputSchemaRef = pathNode.get("post").get("responses").get("200").get("schema").get("properties").get("output").get("$ref").textValue();
            String bodyOutputSchemaRefSuffix = JsonHelper.getStringSuffix(bodyOutputSchemaRef,"/");

            Set<String> inputParametersOrSchemaRef;
            Set<String> outputParametersOrSchemaRef;
            if(populateType == PopulateType.SCHEMA_REF){
                inputParametersOrSchemaRef = new HashSet();
                inputParametersOrSchemaRef.add(bodyInputSchemaRefSuffix);

                outputParametersOrSchemaRef = new HashSet();
                outputParametersOrSchemaRef.add(bodyOutputSchemaRefSuffix);
            }else{
                inputParametersOrSchemaRef = extractParams(jsonNode,bodyInputSchemaRefSuffix);
                outputParametersOrSchemaRef = extractParams(jsonNode,bodyOutputSchemaRefSuffix);
            }
            jsonBodyInputParams.put(rpcOperation,inputParametersOrSchemaRef);
            jsonBodyOutputParams.put(rpcOperation,outputParametersOrSchemaRef);
        }
    }

    private static Set<String> extractParams(final JsonNode jsonNode,String schemaRefName) {
        Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.get("definitions").get(schemaRefName).get("properties").fields();
        Set<String> params = new HashSet();
        for( ;fields.hasNext(); ){
            Map.Entry<String, JsonNode> fieldEntry = fields.next();
            params.add(fieldEntry.getKey());
        }
        return params;
    }

    public static String getStringSuffix(String input, String afterString){
        int indexOf = input.lastIndexOf(afterString);
        String stringSuffix = (indexOf > -1) ? input.substring(indexOf+1) : input;
        return stringSuffix;
    }
}
