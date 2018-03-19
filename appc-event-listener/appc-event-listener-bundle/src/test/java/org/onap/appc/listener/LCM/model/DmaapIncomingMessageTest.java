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
import static org.onap.appc.listener.TestUtil.JSON_INPUT_BODY_STR;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import org.onap.appc.listener.util.Mapper;

public class DmaapIncomingMessageTest {

    private DmaapIncomingMessage dmaapIncomingMessage;

    @Before
    public void setup() {
        dmaapIncomingMessage = new DmaapIncomingMessage();
    }

    @Test
    public void should_set_default_cambria_partition_when_initialized() {

        assertEquals("APP-C", dmaapIncomingMessage.getCambriaPartition());
    }

    @Test
    public void toString_should_return_valid_string_representation() {
        JsonNode jsonNode = Mapper.toJsonNodeFromJsonString(JSON_INPUT_BODY_STR);

        dmaapIncomingMessage.setVersion("test-version");
        dmaapIncomingMessage.setType("test-type");
        dmaapIncomingMessage.setCorrelationID("test-correlation-id");
        dmaapIncomingMessage.setCambriaPartition("test-cambria-partition");
        dmaapIncomingMessage.setRpcName("test-rpc-name");
        dmaapIncomingMessage.setBody(jsonNode);

        assertEquals("DmaapIncomingMessage{DmaapMessage{" +
            "version='" + dmaapIncomingMessage.getVersion() + '\'' +
            ", type='" + dmaapIncomingMessage.getType() + '\'' +
            ", correlationId='" + dmaapIncomingMessage.getCorrelationID() + '\'' +
            ", cambriaPartition='" + dmaapIncomingMessage.getCambriaPartition() + '\'' +
            ", rpcName='" + dmaapIncomingMessage.getRpcName() + '\'' +
            ", body=" + dmaapIncomingMessage.getBody() +
            "}}", dmaapIncomingMessage.toString());
    }


}
