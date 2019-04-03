/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 IBM.
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

package org.onap.appc.requesthandler.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class DmaapOutgoingMessageTest {

    private DmaapOutgoingMessage dmaapOutgoingMessage;
    private DmaapOutgoingMessage.Body body;

    @Before
    public void setUp() {
        dmaapOutgoingMessage = new DmaapOutgoingMessage();
        body = new DmaapOutgoingMessage.Body();
    }

    @Test
    public void testVersion() {
        dmaapOutgoingMessage.setVersion("version");
        assertEquals("version", dmaapOutgoingMessage.getVersion());
    }

    @Test
    public void testCambriaPartition() {
        dmaapOutgoingMessage.setCambriaPartition("CambriaPartition");
        assertEquals("CambriaPartition", dmaapOutgoingMessage.getCambriaPartition());
    }

    @Test
    public void testToString() {
        dmaapOutgoingMessage.setVersion("version");
        dmaapOutgoingMessage.setCambriaPartition("CambriaPartition");
        dmaapOutgoingMessage.setRpcName("rpcName");
        String expected = "DmaapOutgoingMessage{cambriaPartition='CambriaPartition', rpcName='rpcName', body=null}";
        assertEquals(expected, dmaapOutgoingMessage.toString());
    }
    
    @Test
    public void testOutput() {
        body.setOutput("Output");
        assertEquals("Output", body.getOutput());
    }

}
