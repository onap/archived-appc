/*
* ============LICENSE_START=======================================================
* ONAP : APPC
* ================================================================================
* Copyright 2018 TechMahindra
*=================================================================================
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* ============LICENSE_END=========================================================
*/
package org.onap.appc.oam.messageadapter;

import org.junit.Assert;
import org.junit.Test;
import org.onap.appc.oam.messageadapter.DmaapOutgoingMessage.Body;

public class DmaapOutgoingMessageTest {
    private DmaapOutgoingMessage dmaapOutgoingMessage = new DmaapOutgoingMessage();
    private Body body = new Body();

    @Test
    public void testGetServiceInstanceId() {
        dmaapOutgoingMessage.setType("String");
        Assert.assertEquals("String", dmaapOutgoingMessage.getType());
    }

    @Test
    public void testGetCorrelationId() {
        dmaapOutgoingMessage.setCorrelationID("ABC");
        Assert.assertEquals("ABC", dmaapOutgoingMessage.getCorrelationID());
    }

    @Test
    public void testGetCambriaPartition() {
        dmaapOutgoingMessage.setCambriaPartition("cambriaPartition");
        Assert.assertEquals("cambriaPartition", dmaapOutgoingMessage.getCambriaPartition());
    }

    @Test
    public void testGetRpcName() {
        dmaapOutgoingMessage.setRpcName("rpcName");
        Assert.assertEquals("rpcName", dmaapOutgoingMessage.getRpcName());
    }

    @Test
    public void testToString_ReturnNonEmptyString() {
        Assert.assertNotEquals(dmaapOutgoingMessage.toString(), "");
        Assert.assertNotEquals(dmaapOutgoingMessage.toString(), null);
    }

    @Test
    public void testToString_ContainsString() {
        Assert.assertTrue(dmaapOutgoingMessage.toString().contains("cambriaPartition"));
    }

    @Test
    public void testGetBody() {
        dmaapOutgoingMessage.setBody(new Body("This is test"));
        Assert.assertEquals("This is test", dmaapOutgoingMessage.getBody().getOutput());
    }

    @Test
    public void testGetOutput() {
        dmaapOutgoingMessage.setBody(new Body());
        dmaapOutgoingMessage.getBody().setOutput("Test Object");
        Assert.assertEquals("Test Object", dmaapOutgoingMessage.getBody().getOutput());
    }

    @Test
    public void testToStringBody_ReturnNonEmptyString() {
        Assert.assertNotEquals(body.toString(), "");
        Assert.assertNotEquals(body.toString(), null);
    }

    @Test
    public void testToStringBody_ContainsString() {
        Assert.assertTrue(body.toString().contains("Body"));
    }
}
