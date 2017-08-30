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

package org.openecomp.appc.client.impl.protocol;

import org.openecomp.appc.client.impl.core.MessageContext;
import org.openecomp.appc.client.impl.protocol.APPCMessageReaderWriter;
import org.openecomp.appc.client.impl.protocol.ProtocolException;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

public class APPCMessageReaderWriterTest {

    private APPCMessageReaderWriter messageReaderWriter;
    private ObjectMapper mapper;

    private static final String VERSION = "2.0";
    private static final String TYPE = "typeTest";
    private static final String CORRELATION_ID = "correlationIdTest";
    private static final String PARTITION = "partitionTest";
    private static final String RPC = "rpcTest";
    private static final String PAYLOAD = "{\"key1\":\"val1\",\"key2\":\"val2\",\"key3\":{\"key3.1\":\"val3.1\"}}";

    @Before
    public void init() throws IOException {
        mapper = new ObjectMapper();
        messageReaderWriter = new APPCMessageReaderWriter();
    }

    @Test
    public void writeTest() throws IOException, ProtocolException {
        MessageContext context = new MessageContext();
        context.setType(TYPE);
        context.setCorrelationID(CORRELATION_ID);
        context.setPartiton(PARTITION);
        context.setRpc(RPC);
        String payload = PAYLOAD;
        String message = messageReaderWriter.write(payload, context);

        JsonNode messageJson = mapper.readTree(message);
        Assert.assertEquals(VERSION, messageJson.get("version").asText());
        Assert.assertEquals(context.getType(), messageJson.get("type").asText());
        Assert.assertEquals(context.getCorrelationID(), messageJson.get("correlation-id").asText());
        Assert.assertEquals(context.getPartiton(), messageJson.get("cambria.partition").asText());
        Assert.assertEquals(context.getRpc(), messageJson.get("rpc-name").asText());
        Assert.assertEquals(payload, messageJson.get("body").toString());
    }

    @Test
    public void readTest() throws IOException, ProtocolException {
        ObjectNode node = mapper.createObjectNode();
        node.put("version", VERSION);
        node.put("type", TYPE);
        node.put("correlation-id", CORRELATION_ID);
        node.put("cambria.partition", PARTITION);
        node.put("rpc-name", RPC);
        JsonNode payload = mapper.valueToTree(PAYLOAD);
        node.set("body", payload);
        String message = node.toString();

        MessageContext returnContext = new MessageContext();
        String returnPayload = messageReaderWriter.read(message, returnContext);

        Assert.assertEquals(TYPE, returnContext.getType());
        Assert.assertEquals(CORRELATION_ID, returnContext.getCorrelationID());
        Assert.assertEquals(PARTITION, returnContext.getPartiton());
        Assert.assertEquals(RPC, returnContext.getRpc());
        Assert.assertEquals(payload.toString(), returnPayload);
    }

}
