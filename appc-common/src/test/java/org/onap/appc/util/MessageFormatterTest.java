package org.onap.appc.util;

import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class MessageFormatterTest {

    @Test
    public void testEscapeDollarInMessgeFormatter() {
        String msg = "${SYNC_NEW201}";
        Map<String, Object> respMsg = new HashMap<>();
        respMsg.put("vnfid", msg);
        String formattedMsg = MessageFormatter.format(msg, respMsg);
        Assert.assertEquals(msg, formattedMsg);
    }
}
