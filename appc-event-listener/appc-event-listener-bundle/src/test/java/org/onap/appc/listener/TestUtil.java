package org.onap.appc.listener;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.Map;
import org.onap.appc.listener.LCM.model.ActionIdentifiers;
import org.onap.appc.listener.LCM.model.CommonHeader;
import org.onap.appc.listener.LCM.model.DmaapIncomingMessage;
import org.onap.appc.listener.LCM.model.DmaapMessage;
import org.onap.appc.listener.LCM.model.DmaapOutgoingMessage;
import org.onap.appc.listener.util.Mapper;

public class TestUtil {

    public static final String JSON_INPUT_BODY_STR =
        "{\"input\":{ \"common-header\": { \"timestamp\": \"2016-08-03T08:50:18.97Z\", "
            + "\"api-ver\": \"1\", \"originator-id\": \"1\", \"request-id\": \"123\", \"sub-request-id\": \"1\", "
            + "\"flags\": { \"force\":\"TRUE\", \"ttl\":\"9900\" } }, \"action\": \"Stop\", "
            + "\"action-identifiers\": { \"vnf-id\": \"TEST\" } }}";

    public static final String JSON_OUTPUT_BODY_STR =
        "{\"output\":{\"common-header\":{\"timestamp\":\"2016-08-03T08:50:18.97Z\","
            + "\"api-ver\":\"1\",\"flags\":{\"force\":\"TRUE\",\"ttl\":\"9900\"},\"sub-request-id\":\"1\","
            + "\"request-id\":\"123\",\"originator-id\":\"1\"},\"locked\": \"test-locked\", "
            + "\"status\":{\"message\":\"test message\",\"code\":200}}}";

    public static DmaapMessage buildDmaapMessage() {

        DmaapMessage dmaapMessage = new DmaapMessage();
        dmaapMessage.setRpcName("test");
        JsonNode jsonNode = Mapper.toJsonNodeFromJsonString(JSON_INPUT_BODY_STR);
        dmaapMessage.setBody(jsonNode);
        return dmaapMessage;
    }

    public static DmaapIncomingMessage buildDmaapIncomingMessage() {
        DmaapIncomingMessage dmaapIncomingMessage = new DmaapIncomingMessage();
        dmaapIncomingMessage.setRpcName("test");
        JsonNode jsonNode = Mapper.toJsonNodeFromJsonString(JSON_INPUT_BODY_STR);
        dmaapIncomingMessage.setBody(jsonNode);
        return dmaapIncomingMessage;

    }

    public static DmaapOutgoingMessage buildDmaapOutgoingMessage() {
        DmaapOutgoingMessage dmaapOutgoingMessage = new DmaapOutgoingMessage();
        dmaapOutgoingMessage.setRpcName("test");
        JsonNode jsonNode = Mapper.toJsonNodeFromJsonString(JSON_OUTPUT_BODY_STR);
        dmaapOutgoingMessage.setBody(jsonNode);
        return dmaapOutgoingMessage;

    }

    public static CommonHeader buildCommonHeader() {

        CommonHeader commonHeader = new CommonHeader();
        commonHeader.setTimeStamp("test-timestamp");
        commonHeader.setApiVer("test-api-version");
        commonHeader.setOriginatorId("test-originator-id");
        commonHeader.setRequestID("test-request-id");
        commonHeader.setSubRequestId("test-subrequest-id");

        Map<String, String> flags = new HashMap<>();
        flags.put("key1", "flag1");
        flags.put("key2", "flag2");
        flags.put("key3", "flag3");

        commonHeader.setFlags(flags);
        return commonHeader;
    }

    public static ActionIdentifiers buildActionIdentifiers() {

        ActionIdentifiers actionIdentifiers = new ActionIdentifiers();
        actionIdentifiers.setServiceInstanceId("test-instance-id");
        actionIdentifiers.setVnfID("test-vnf-id");
        actionIdentifiers.setVnfcName("test-name");
        actionIdentifiers.setVserverId("test-vserver-id");

        return actionIdentifiers;
    }
}
