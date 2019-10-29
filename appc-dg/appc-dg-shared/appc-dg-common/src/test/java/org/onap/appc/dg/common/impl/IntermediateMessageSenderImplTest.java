/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 Ericsson
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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.dg.common.impl;

import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.appc.srvcomm.messaging.MessagingConnector;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

public class IntermediateMessageSenderImplTest {

    private SvcLogicContext ctx;
    private Map<String, String> params;


    private IntermediateMessageSenderImpl intermediateMessageSenderImpl;



    @Test
    public void testSendEmptyMessage() {
        intermediateMessageSenderImpl = new IntermediateMessageSenderImpl();
        MessagingConnector msgConn = Mockito.mock(MessagingConnector.class);
        Mockito.when(msgConn.publishMessage(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(false);
        intermediateMessageSenderImpl.init(msgConn);
        ctx = new SvcLogicContext();
        params = new HashMap<>();
        intermediateMessageSenderImpl.sendMessage(params, ctx);
        Assert.assertEquals("FAILURE", ctx.getAttribute("STATUS"));
    }

    @Test
    public void testSendMessage() {
        intermediateMessageSenderImpl = new IntermediateMessageSenderImpl();
        MessagingConnector msgConn = Mockito.mock(MessagingConnector.class);
        Mockito.when(msgConn.publishMessage(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(true);
        intermediateMessageSenderImpl.init(msgConn);
        ctx = new SvcLogicContext();
        ctx.setAttribute("input.common-header.request-id", "REQUEST-ID");
        params = new HashMap<>();
        params.put("message", "TEST MESSAGE");
        params.put("code", "TEST CODE");
        intermediateMessageSenderImpl.sendMessage(params, ctx);
        Assert.assertEquals("SUCCESS", ctx.getAttribute("STATUS"));
    }
}
