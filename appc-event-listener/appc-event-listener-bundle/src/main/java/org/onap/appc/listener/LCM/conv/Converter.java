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

package org.onap.appc.listener.LCM.conv;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.onap.appc.listener.LCM.model.DmaapMessage;
import org.onap.appc.listener.LCM.model.DmaapOutgoingMessage;
import org.onap.appc.listener.util.Mapper;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

public class Converter {

    private Converter() {
    }

    public static DmaapOutgoingMessage convertJsonNodeToDmaapOutgoingMessage(DmaapMessage event, JsonNode inObj){

        if (event == null || inObj == null) {
            throw new IllegalArgumentException("One of given arguments is null");
        }

        DmaapOutgoingMessage outObj = new DmaapOutgoingMessage();
        outObj.setBody(inObj);
        outObj.setRpcName(event.getRpcName());
        outObj.setVersion(event.getVersion());
        outObj.setType("response");
        outObj.setCorrelationID(event.getCorrelationID());
        return outObj;
    }

    public static String convertDmaapOutgoingMessageToJsonString(DmaapMessage inObj) throws JsonProcessingException {

        if (inObj == null)
            throw new IllegalArgumentException("Input message is null");


        ObjectMapper objectMapper = new ObjectMapper();
        ObjectWriter writer = objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
            .writer().withFeatures(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
        return writer.writeValueAsString(inObj);

    }

    public static DmaapOutgoingMessage buildDmaapOutgoingMessageWithUnexpectedError(DmaapMessage event,
        Exception inputException) {

        if (event == null || inputException == null) {
            throw new IllegalArgumentException("One of given arguments is null");
        }

        DmaapOutgoingMessage dmaapOutgoingMessage;
        String errMsg =
            StringUtils.isEmpty(inputException.getMessage()) ? inputException.toString() : inputException.getMessage();
        JSONObject commonHeaderJsonObject = Mapper.toJsonObject(event.getBody().get("input").get("common-header"));
        JSONObject jsonObjectOutput = new JSONObject().accumulate("common-header", commonHeaderJsonObject)
            .accumulate("status", new JSONObject().accumulate("code", 200).accumulate("value", errMsg));
        dmaapOutgoingMessage = new DmaapOutgoingMessage();
        dmaapOutgoingMessage.setRpcName(event.getRpcName());
        dmaapOutgoingMessage.setCorrelationID(event.getCorrelationID());
        dmaapOutgoingMessage.setType("error");
        dmaapOutgoingMessage.setVersion(event.getVersion());
        JSONObject jsonObjectBody = new JSONObject().accumulate("output", jsonObjectOutput);
        JsonNode jsonNodeBody = Mapper.toJsonNodeFromJsonString(jsonObjectBody.toString());
        dmaapOutgoingMessage.setBody(jsonNodeBody);
        return dmaapOutgoingMessage;
    }

    public static String extractRequestIdWithSubId(JsonNode dmaapBody) {

        if (dmaapBody == null) {
            throw new IllegalArgumentException("Dmaap body is null");
        }

        JsonNode commonHeaderJsonNode = dmaapBody.get("input").get("common-header");
        String requestId = getValue(commonHeaderJsonNode, "request-id", "");
        String subRequestId = getValue(commonHeaderJsonNode, "sub-request-id", "");
        if (!StringUtils.isEmpty(subRequestId)) {
            requestId = requestId + "-" + subRequestId;
        }
        return requestId;
    }

    public static Integer extractStatusCode(JsonNode event) {

        if (event == null){
            throw new IllegalArgumentException("Input event is null");
        }

        Integer statusCode;
        statusCode = event.get("output").get("status").get("code").asInt();
        return statusCode;
    }

    private static String getValue(JsonNode jsonNode, String name, String defaultValue) {
        if (jsonNode == null) {
            return defaultValue;
        }
        JsonNode childJsonNode = jsonNode.get(name);
        if (childJsonNode == null) {
            return defaultValue;
        }
        String value = childJsonNode.asText();
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

}
