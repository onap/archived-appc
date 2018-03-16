package org.onap.appc.listener.LCM.model;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class OutputBodyTest {

    private OutputBody outputBody;

    @Before
    public void setup() {
        outputBody = new OutputBody();
    }

    @Test
    public void should_set_properties() {

        CommonHeader testCommonHeader = buildCommonHeader();
        ResponseStatus testResponseStatus = new ResponseStatus(200, "OK");

        outputBody.setHeader(testCommonHeader);
        outputBody.setStatus(testResponseStatus);
        outputBody.setLocked("test-locked");
        outputBody.setPayload("{\"payload\": \"value\"");

        assertEquals(testCommonHeader, outputBody.getHeader());
        assertEquals(testResponseStatus, outputBody.getStatus());
        assertEquals("test-locked", outputBody.getLocked());
        assertEquals("{\"payload\": \"value\"", outputBody.getPayload());
    }


    @Test
    public void should_inherit_input_body_header_when_initialized_from_constructor() {

        InputBody testInputBody = new InputBody();
        CommonHeader testCommonHeader = buildCommonHeader();
        testInputBody.setCommonHeader(testCommonHeader);

        outputBody = new OutputBody(testInputBody);

        assertNotNull(outputBody.getHeader());
        assertEquals(testCommonHeader.getFlags(), outputBody.getHeader().getFlags());
        assertEquals(testCommonHeader.getSubRequestId(), outputBody.getHeader().getSubRequestId());
        assertEquals(testCommonHeader.getRequestID(), outputBody.getHeader().getRequestID());
        assertEquals(testCommonHeader.getOriginatorId(), outputBody.getHeader().getOriginatorId());
        assertEquals(testCommonHeader.getApiVer(), outputBody.getHeader().getApiVer());
    }

    @Test
    public void toResponse_should_convert_to_json_object() {
        CommonHeader testCommonHeader = buildCommonHeader();
        ResponseStatus testResponseStatus = new ResponseStatus(200, "OK");

        outputBody.setHeader(testCommonHeader);
        outputBody.setStatus(testResponseStatus);
        outputBody.setLocked("test-locked");
        outputBody.setPayload("{\"payload\": \"value\"");

        JSONObject response = outputBody.toResponse();
        assertNotNull(response);

        assertEquals("test-locked", response.get("locked"));
        assertEquals("{\"payload\": \"value\"", response.get("payload"));
    }

    private CommonHeader buildCommonHeader() {

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
}
