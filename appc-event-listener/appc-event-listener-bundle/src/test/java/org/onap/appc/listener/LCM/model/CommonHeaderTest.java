package org.onap.appc.listener.LCM.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class CommonHeaderTest {

    private CommonHeader commonHeader;

    @Before
    public void setup() {
        commonHeader = new CommonHeader();
    }

    @Test
    public void should_set_properties() {

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

        assertEquals("test-timestamp", commonHeader.getTimeStamp());
        assertEquals("test-api-version", commonHeader.getApiVer());
        assertEquals("test-originator-id", commonHeader.getOriginatorId());
        assertEquals("test-request-id", commonHeader.getRequestID());
        assertEquals("test-subrequest-id", commonHeader.getSubRequestId());
        assertEquals(flags, commonHeader.getFlags());
    }

    @Test
    public void should_initialize_parameters_from_constructor() {

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

        CommonHeader testObject = new CommonHeader(commonHeader);

        assertNotEquals(commonHeader.getTimeStamp(), testObject.getTimeStamp());
        assertEquals(commonHeader.getApiVer(), testObject.getApiVer());
        assertEquals(commonHeader.getOriginatorId(), testObject.getOriginatorId());
        assertEquals(commonHeader.getRequestID(), testObject.getRequestID());
        assertEquals(commonHeader.getSubRequestId(), testObject.getSubRequestId());
        assertEquals(commonHeader.getFlags(), testObject.getFlags());
    }
}
