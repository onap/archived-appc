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

package org.onap.appc.listener.LCM.conv;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Assert;
import org.junit.Test;
import org.onap.appc.listener.LCM.model.DmaapIncomingMessage;
import org.onap.appc.listener.LCM.model.DmaapMessage;
import org.onap.appc.listener.LCM.model.DmaapOutgoingMessage;
import org.onap.appc.listener.demo.model.OutgoingMessage;
import org.onap.appc.listener.util.Mapper;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

public class ConverterTest {

    private static final String jsonInputBodyStr = "{\"input\":{ \"common-header\": { \"timestamp\": \"2016-08-03T08:50:18.97Z\", "
        + "\"api-ver\": \"1\", \"originator-id\": \"1\", \"request-id\": \"123\", \"sub-request-id\": \"1\", "
        + "\"flags\": { \"force\":\"TRUE\", \"ttl\":\"9900\" } }, \"action\": \"Stop\", "
        + "\"action-identifiers\": { \"vnf-id\": \"TEST\" } }}";

    private static final String jsonOutputBodyStr = "{\"output\":{\"common-header\":{\"timestamp\":\"2016-08-03T08:50:18.97Z\","
        + "\"api-ver\":\"1\",\"flags\":{\"force\":\"TRUE\",\"ttl\":\"9900\"},\"sub-request-id\":\"1\","
        + "\"request-id\":\"123\",\"originator-id\":\"1\"},\"status\":{\"value\":\"TestException\",\"code\":200}}}";

    private static final String expectedDmaapOutgoingMessageAsJsonString = "{\"body\":{\"output\":{\"common-header\":"
        + "{\"timestamp\":\"2016-08-03T08:50:18.97Z\",\"api-ver\":\"1\",\"flags\":{\"force\":\"TRUE\",\"ttl\":\"9900\"},"
        + "\"sub-request-id\":\"1\",\"request-id\":\"123\",\"originator-id\":\"1\"},\"status\":"
        + "{\"value\":\"TestException\",\"code\":200}}},\"cambria.partition\":\"MSO\",\"rpc-name\":\"test\"}";


    @Test(expected = IllegalArgumentException.class)
    public void convertJsonNodeToDmaapOutgoingMessage_should_throw_when_given_null_arguments() {

        Converter.convertJsonNodeToDmaapOutgoingMessage(null, null);
    }

    @Test
    public void convertJsonNodeToDmaapOutgoingMessage_should_convert_to_outgoing_message() {

        DmaapIncomingMessage message = new DmaapIncomingMessage();
        message.setRpcName("test");
        message.setCorrelationID("test-1");
        message.setVersion("v1");
        JsonNode jsonNode = Mapper.toJsonNodeFromJsonString(jsonInputBodyStr);
        message.setBody(jsonNode);

        DmaapOutgoingMessage result = Converter.convertJsonNodeToDmaapOutgoingMessage(message, jsonNode);

        assertEquals("test", result.getRpcName());
        assertEquals("test-1", result.getCorrelationID());
        assertEquals("v1", result.getVersion());
        assertEquals(jsonNode, result.getBody());
    }

    @Test(expected = IllegalArgumentException.class)
    public void convertDmaapOutgoingMessageToJsonString_should_throw_when_given_null_arguments()
        throws JsonProcessingException {

        Converter.convertDmaapOutgoingMessageToJsonString(null);
    }

    @Test
    public void convertDmaapOutgoingMessageToJsonString_should_return_converted_json_string()
        throws JsonProcessingException {

        DmaapOutgoingMessage message = new DmaapOutgoingMessage();
        message.setRpcName("test");
        JsonNode jsonNode = Mapper.toJsonNodeFromJsonString(jsonOutputBodyStr);
        message.setBody(jsonNode);

        assertEquals(expectedDmaapOutgoingMessageAsJsonString,
            Converter.convertDmaapOutgoingMessageToJsonString(message));
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildDmaapOutgoingMessageWithUnexpectedErrorTest_should_throw_given_null_arguments()
        throws JsonProcessingException {

        Converter.buildDmaapOutgoingMessageWithUnexpectedError(null, null);
    }

    @Test
    public void buildDmaapOutgoingMessageWithUnexpectedErrorTest_should_build_valid_outgoing_message()
        throws JsonProcessingException {

        DmaapIncomingMessage dmaapIncomingMessage = buildDmaapIncomingMessage();
        String errMsg = "TestException";
        DmaapOutgoingMessage dmaapOutgoingMessage = Converter
            .buildDmaapOutgoingMessageWithUnexpectedError(dmaapIncomingMessage, new Exception(errMsg));
        int code = dmaapOutgoingMessage.getBody().get("output").get("status").get("code").asInt();
        String value = dmaapOutgoingMessage.getBody().get("output").get("status").get("value").asText();
        assertEquals(200, code);
        assertEquals(errMsg, value);
    }


    @Test(expected = IllegalArgumentException.class)
    public void extractRequestIdWithSubId_should_throw_given_null_argument() throws SvcLogicException {

        Converter.extractRequestIdWithSubId(null);
    }

    @Test
    public void extractRequestIdWithSubIdTest_should_extract_id_with_subDd() throws SvcLogicException {
        DmaapIncomingMessage dmaapIncomingMessage = buildDmaapIncomingMessage();

        String requestIdWithSubId = Converter.extractRequestIdWithSubId(dmaapIncomingMessage.getBody());
        assertEquals("123-1", requestIdWithSubId);
    }



    @Test(expected = IllegalArgumentException.class)
    public void extractStatusCode_should_throw_given_null_argument() {
        Converter.extractStatusCode(null);
    }


    @Test
    public void extractStatusCode_should_extract_valid_status_code() {
        DmaapOutgoingMessage dmaapOutgoingMessage = buildDmaapOutgoingMessage();
        Integer statusCode = Converter.extractStatusCode(dmaapOutgoingMessage.getBody());
        assertEquals(200L, statusCode.longValue());
    }

    private DmaapIncomingMessage buildDmaapIncomingMessage() {
        DmaapIncomingMessage dmaapIncomingMessage = new DmaapIncomingMessage();
        dmaapIncomingMessage.setRpcName("test");
        JsonNode jsonNode = Mapper.toJsonNodeFromJsonString(jsonInputBodyStr);
        dmaapIncomingMessage.setBody(jsonNode);
        return dmaapIncomingMessage;

    }

    private DmaapOutgoingMessage buildDmaapOutgoingMessage() {
        DmaapOutgoingMessage dmaapOutgoingMessage = new DmaapOutgoingMessage();
        dmaapOutgoingMessage.setRpcName("test");
        JsonNode jsonNode = Mapper.toJsonNodeFromJsonString(jsonOutputBodyStr);
        dmaapOutgoingMessage.setBody(jsonNode);
        return dmaapOutgoingMessage;

    }

}
