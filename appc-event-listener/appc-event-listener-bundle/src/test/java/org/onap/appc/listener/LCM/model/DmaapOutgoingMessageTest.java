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
