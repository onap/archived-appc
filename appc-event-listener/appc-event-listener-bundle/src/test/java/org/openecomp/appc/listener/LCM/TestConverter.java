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

package org.openecomp.appc.listener.LCM;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Assert;
import org.junit.Test;
import org.openecomp.appc.listener.LCM.conv.Converter;
import org.openecomp.appc.listener.LCM.model.DmaapIncomingMessage;
import org.openecomp.appc.listener.LCM.model.DmaapOutgoingMessage;
import org.openecomp.appc.listener.util.Mapper;

public class TestConverter {

    private String jsonInputBodyStr ="{\"input\":{ \"common-header\": { \"timestamp\": \"2016-08-03T08:50:18.97Z\", \"api-ver\": \"1\", \"originator-id\": \"1\", \"request-id\": \"123\", \"sub-request-id\": \"1\", \"flags\": { \"force\":\"TRUE\", \"ttl\":\"9900\" } }, \"action\": \"Stop\", \"action-identifiers\": { \"vnf-id\": \"TEST\" } }}";
    private String jsonOutputBodyStr ="{\"output\":{\"common-header\":{\"timestamp\":\"2016-08-03T08:50:18.97Z\",\"api-ver\":\"1\",\"flags\":{\"force\":\"TRUE\",\"ttl\":\"9900\"},\"sub-request-id\":\"1\",\"request-id\":\"123\",\"originator-id\":\"1\"},\"status\":{\"value\":\"TestException\",\"code\":200}}}";

    @Test
    public void buildDmaapOutgoingMessageWithUnexpectedErrorTest() throws JsonProcessingException {
        DmaapIncomingMessage dmaapIncomingMessage = buildDmaapIncomingMessage();
        String errMsg = "TestException";
        DmaapOutgoingMessage dmaapOutgoingMessage = Converter.buildDmaapOutgoingMessageWithUnexpectedError(dmaapIncomingMessage, new Exception(errMsg));
        int code = dmaapOutgoingMessage.getBody().get("output").get("status").get("code").asInt();
        String value = dmaapOutgoingMessage.getBody().get("output").get("status").get("value").asText();
        Assert.assertEquals(200,code);
        Assert.assertEquals(errMsg,value);
    }

    private  static String expectedDmaapOutgoingMessageAsJsonString = "{\"body\":{\"output\":{\"common-header\":{\"timestamp\":\"2016-08-03T08:50:18.97Z\",\"api-ver\":\"1\",\"flags\":{\"force\":\"TRUE\",\"ttl\":\"9900\"},\"sub-request-id\":\"1\",\"request-id\":\"123\",\"originator-id\":\"1\"},\"status\":{\"value\":\"TestException\",\"code\":200}}},\"cambria.partition\":\"MSO\",\"rpc-name\":\"test\"}";
    @Test
    public void convDmaapOutgoingMessageToJsonStringTest() throws JsonProcessingException {
        DmaapOutgoingMessage dmaapOutgoingMessage = buildDmaapOutgoingMessage();
        String dmaapOutgoingMessageAsJsonString = Converter.convDmaapOutgoingMessageToJsonString(dmaapOutgoingMessage);
//        Assert.assertEquals(dmaapOutgoingMessageAsJsonString,dmaapOutgoingMessageAsJsonString);
        Assert.assertEquals(expectedDmaapOutgoingMessageAsJsonString,dmaapOutgoingMessageAsJsonString);
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


    @Test
    public void extractRequestIdWithSubIdTest() {
        DmaapIncomingMessage dmaapIncomingMessage = buildDmaapIncomingMessage();
        String equestIdWithSubId = Converter.extractRequestIdWithSubId(dmaapIncomingMessage.getBody());
        Assert.assertEquals("123-1",equestIdWithSubId);
    }

    @Test
    public void extractStatusCodeTest() {
        DmaapOutgoingMessage dmaapOutgoingMessage = buildDmaapOutgoingMessage();
        Integer statusCode = Converter.extractStatusCode(dmaapOutgoingMessage.getBody());
        Assert.assertEquals(200L,statusCode.longValue());
    }

}
