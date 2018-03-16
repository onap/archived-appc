package org.onap.appc.listener.LCM.model;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Before;
import org.junit.Test;
import org.onap.appc.listener.util.Mapper;

public class DmaapMessageTest {

    private static final String jsonInputBodyStr =
        "{\"input\":{ \"common-header\": { \"timestamp\": \"2016-08-03T08:50:18.97Z\", "
            + "\"api-ver\": \"1\", \"originator-id\": \"1\", \"request-id\": \"123\", \"sub-request-id\": \"1\", "
            + "\"flags\": { \"force\":\"TRUE\", \"ttl\":\"9900\" } }, \"action\": \"Stop\", "
            + "\"action-identifiers\": { \"vnf-id\": \"TEST\" } }}";

    private DmaapMessage dmaapMessage;

    @Before
    public void setup() {
        dmaapMessage = new DmaapMessage();
    }

    @Test
    public void should_set_properties() {

        JsonNode jsonNode = Mapper.toJsonNodeFromJsonString(jsonInputBodyStr);

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
        JsonNode jsonNode = Mapper.toJsonNodeFromJsonString(jsonInputBodyStr);

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

