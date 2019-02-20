/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 Ericsson
 * ================================================================================
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

package org.onap.appc.listener.demo.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.commons.io.IOUtils;
import org.hamcrest.CoreMatchers;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.onap.appc.listener.util.Mapper;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Mapper.class)
public class OutgoingMessageTest {

    private IncomingMessage incomingMessage;

    @Before
    public void setup() throws IOException {
        String incomingStr = IOUtils.toString(getClass().getResourceAsStream("/IncomingMessagedemo.txt"), "UTF-8");
        incomingMessage = Mapper.mapOne(incomingStr, IncomingMessage.class);
    }

    @Test
    public void testOutgoingMessage() throws UnknownHostException {
        InetAddress mockInetAddress = Mockito.mock(InetAddress.class);
        Mockito.when(mockInetAddress.getCanonicalHostName()).thenReturn("TEST_CANONICAL_HOSTNAME");
        OutgoingMessage outgoingMessage = Mockito.spy(new OutgoingMessage(incomingMessage));
        PowerMockito.when(outgoingMessage.getLocalHost()).thenReturn(mockInetAddress);
        outgoingMessage.updateResponseTime();
        assertEquals("appc@TEST_CANONICAL_HOSTNAME", outgoingMessage.generateFrom());
    }

    @Test
    public void testOutgoingMessageUnknowHost() throws UnknownHostException {
        OutgoingMessage outgoingMessage = Mockito.spy(new OutgoingMessage(incomingMessage));
        PowerMockito.when(outgoingMessage.getLocalHost()).thenThrow(new UnknownHostException());
        assertEquals("appc@UnknownHost", outgoingMessage.generateFrom());
    }

    @Test
    public void testJson() {
        PowerMockito.mockStatic(Mapper.class);
        JSONObject mockObject = Mockito.mock(JSONObject.class);
        PowerMockito.when(Mapper.toJsonObject(Mockito.any())).thenReturn(mockObject);
        OutgoingMessage outgoingMessage = Mockito.spy(new OutgoingMessage(incomingMessage));
        assertEquals(mockObject, outgoingMessage.toResponse());
    }

    @Test
    public void testSetResponse() {
        OutgoingMessage outgoingMessage = new OutgoingMessage(incomingMessage);
        outgoingMessage.setResponse(null);
        assertEquals(new OutgoingMessage.OutStatus().getValue(), outgoingMessage.getStatus().getValue());
        outgoingMessage.setResponse(Status.ACCEPTED);
        assertEquals("100", outgoingMessage.getStatus().getCode());
        outgoingMessage.setResponse(Status.FAILURE);
        assertEquals("500", outgoingMessage.getStatus().getCode());
        outgoingMessage.setResponse(Status.SUCCESS);
        assertEquals("400", outgoingMessage.getStatus().getCode());
    }
}
