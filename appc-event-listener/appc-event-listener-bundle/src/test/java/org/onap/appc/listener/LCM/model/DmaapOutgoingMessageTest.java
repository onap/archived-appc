/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2018 Nokia Solutions and Networks
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
package org.onap.appc.listener.LCM.model;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import org.onap.appc.listener.util.Mapper;

public class DmaapOutgoingMessageTest {

    private static final String jsonInputBodyStr =
        "{\"input\":{ \"common-header\": { \"timestamp\": \"2016-08-03T08:50:18.97Z\", "
            + "\"api-ver\": \"1\", \"originator-id\": \"1\", \"request-id\": \"123\", \"sub-request-id\": \"1\", "
            + "\"flags\": { \"force\":\"TRUE\", \"ttl\":\"9900\" } }, \"action\": \"Stop\", "
            + "\"action-identifiers\": { \"vnf-id\": \"TEST\" } }}";

    private DmaapOutgoingMessage dmaapOutgoingMessage;

    @Before
    public void setup() {
        dmaapOutgoingMessage = new DmaapOutgoingMessage();
    }

    @Test
    public void should_set_default_cambria_partition_when_initialized() {

        assertEquals("MSO", dmaapOutgoingMessage.getCambriaPartition());
    }

    @Test
    public void toString_should_return_valid_string_representation() {
        JsonNode jsonNode = Mapper.toJsonNodeFromJsonString(jsonInputBodyStr);

        dmaapOutgoingMessage.setVersion("test-version");
        dmaapOutgoingMessage.setType("test-type");
        dmaapOutgoingMessage.setCorrelationID("test-correlation-id");
        dmaapOutgoingMessage.setCambriaPartition("test-cambria-partition");
        dmaapOutgoingMessage.setRpcName("test-rpc-name");
        dmaapOutgoingMessage.setBody(jsonNode);

        assertEquals("DmaapOutgoingMessage{DmaapMessage{" +
            "version='" + dmaapOutgoingMessage.getVersion() + '\'' +
            ", type='" + dmaapOutgoingMessage.getType() + '\'' +
            ", correlationId='" + dmaapOutgoingMessage.getCorrelationID() + '\'' +
            ", cambriaPartition='" + dmaapOutgoingMessage.getCambriaPartition() + '\'' +
            ", rpcName='" + dmaapOutgoingMessage.getRpcName() + '\'' +
            ", body=" + dmaapOutgoingMessage.getBody() +
            "}}", dmaapOutgoingMessage.toString());
    }
}
