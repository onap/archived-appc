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

public class DmaapMessageTest {

    private DmaapMessage dmaapMessage;

    @Before
    public void setup() {
        dmaapMessage = new DmaapMessage();
    }

    @Test
    public void should_set_properties() {

        JsonNode jsonNode = Mapper.toJsonNodeFromJsonString(JSON_INPUT_BODY_STR);

        dmaapMessage.setVersion("test-version");
        dmaapMessage.setType("test-type");
        dmaapMessage.setCorrelationID("test-correlation-id");
        dmaapMessage.setCambriaPartition("test-cambria-partition");
        dmaapMessage.setRpcName("test-rpc-name");
        dmaapMessage.setBody(jsonNode);

        assertEquals("test-version", dmaapMessage.getVersion());
        assertEquals("test-type", dmaapMessage.getType());
        assertEquals("test-correlation-id", dmaapMessage.getCorrelationID());
        assertEquals("test-cambria-partition", dmaapMessage.getCambriaPartition());
        assertEquals("test-rpc-name", dmaapMessage.getRpcName());
        assertEquals(jsonNode, dmaapMessage.getBody());
    }

    @Test
    public void toString_should_return_valid_string_representation() {
        JsonNode jsonNode = Mapper.toJsonNodeFromJsonString(JSON_INPUT_BODY_STR);

        dmaapMessage.setVersion("test-version");
        dmaapMessage.setType("test-type");
        dmaapMessage.setCorrelationID("test-correlation-id");
        dmaapMessage.setCambriaPartition("test-cambria-partition");
        dmaapMessage.setRpcName("test-rpc-name");
        dmaapMessage.setBody(jsonNode);

        assertEquals("DmaapMessage{" +
            "version='" + dmaapMessage.getVersion() + '\'' +
            ", type='" + dmaapMessage.getType() + '\'' +
            ", correlationId='" + dmaapMessage.getCorrelationID() + '\'' +
            ", cambriaPartition='" + dmaapMessage.getCambriaPartition() + '\'' +
            ", rpcName='" + dmaapMessage.getRpcName() + '\'' +
            ", body=" + dmaapMessage.getBody() +
            '}', dmaapMessage.toString());
    }
}

