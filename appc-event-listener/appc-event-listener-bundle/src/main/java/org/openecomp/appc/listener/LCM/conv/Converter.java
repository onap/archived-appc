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

package org.openecomp.appc.listener.LCM.conv;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.openecomp.appc.listener.LCM.model.DmaapMessage;
import org.openecomp.appc.listener.LCM.model.DmaapOutgoingMessage;
import org.openecomp.appc.listener.util.Mapper;

public class Converter {
   
    public static DmaapOutgoingMessage convJsonNodeToDmaapOutgoingMessage(DmaapMessage event, JsonNode inObj) {
    	DmaapOutgoingMessage outObj = new DmaapOutgoingMessage();
        outObj.setBody(inObj);
        outObj.setRpcName(event.getRpcName());
        outObj.setVersion(event.getVersion());
        outObj.setType("response");
        outObj.setCorrelationID(event.getCorrelationID());
        return outObj;
    }

    public static String convDmaapOutgoingMessageToJsonString(DmaapMessage inObj) throws JsonProcessingException {
//        return Mapper.toJsonString(inObj);
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectWriter writer = objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL).configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY,true)
                .writer().withFeatures(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
        return writer.writeValueAsString(inObj);

    }

    public static DmaapOutgoingMessage buildDmaapOutgoingMessageWithUnexpectedError(DmaapMessage event,Exception inputException) {
    	DmaapOutgoingMessage dmaapOutgoingMessage = null;
        String errMsg = StringUtils.isEmpty(inputException.getMessage())? inputException.toString() : inputException.getMessage();
        JSONObject commonHeaderJsonObject = Mapper.toJsonObject(event.getBody().get("input").get("common-header"));
        JSONObject jsonObjectOutput = new JSONObject().accumulate("common-header", commonHeaderJsonObject).accumulate("status", new JSONObject().accumulate("code",200).accumulate("value",errMsg));
        dmaapOutgoingMessage = new DmaapOutgoingMessage();
        dmaapOutgoingMessage.setRpcName(event.getRpcName());
        dmaapOutgoingMessage.setCorrelationID(event.getCorrelationID());
        dmaapOutgoingMessage.setType("error");
        dmaapOutgoingMessage.setVersion(event.getVersion());
        JSONObject jsonObjectBody = new JSONObject().accumulate("output",jsonObjectOutput);
        JsonNode jsonNodeBody = Mapper.toJsonNodeFromJsonString(jsonObjectBody.toString());
        dmaapOutgoingMessage.setBody(jsonNodeBody);
        return dmaapOutgoingMessage;
    }

    public static String extractRequestIdWithSubId(JsonNode dmaapBody) {
        //TODO: null pointer exception if dmaapBody is null, check if null or ensure is not null before calling
        JsonNode commonHeaderJsonNode = dmaapBody.get("input").get("common-header");
        String requestId = getValue(commonHeaderJsonNode,"request-id","");
        String subRequestId = getValue(commonHeaderJsonNode,"sub-request-id","");
        if(!StringUtils.isEmpty(subRequestId)){
            requestId = requestId +"-"+subRequestId;
        }
        return requestId;
    }

    public static Integer extractStatusCode(JsonNode event) {
        Integer statusCode;
        statusCode = event.get("output").get("status").get("code").asInt();
        return statusCode;
    }

    private static String getValue(JsonNode jsonNode,String name,String defaultValue){
        if(jsonNode == null){
            return defaultValue;
        }
        JsonNode childJsonNode = jsonNode.get(name);
        if(childJsonNode == null){
            return defaultValue;
        }
        String value = childJsonNode.asText();
        if(value == null){
            return defaultValue;
        }
        return value;
    }

}
