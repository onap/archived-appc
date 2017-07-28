/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.appc.listener.CL.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.openecomp.appc.listener.CL.model.IncomingMessage;
import org.openecomp.appc.listener.CL.model.OutgoingMessage;
import org.openecomp.appc.listener.CL.model.Status;
import org.openecomp.appc.listener.util.Mapper;

public class TestMessages {
    private IncomingMessage in;
    private OutgoingMessage out;

    private String incomingStr;
    private String outgoingStr;

    @Before
    public void setup() {
        try {
            incomingStr = IOUtils.toString(getClass().getResourceAsStream("/IncomingMessage.txt"), "UTF-8");
            outgoingStr = IOUtils.toString(getClass().getResourceAsStream("/OutgoingMessage.txt"), "UTF-8");
            assertNotNull(incomingStr);
            assertNotNull(outgoingStr);

            in = Mapper.mapOne(incomingStr, IncomingMessage.class);

            out = Mapper.mapOne(in.toOutgoing(Status.PENDING), OutgoingMessage.class);
            out.updateResponseTime();

            assertNotNull(in);
            assertNotNull(out);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    // NOTE Test Mapper will be used to test an event from dmaap.
    @Test
    public void testGetterSetter() {
        assertNotNull(in);
        assertNotNull(in.getRequestClient());
        assertNotNull(in.getRequestTime());
        assertNotNull(in.getMessage());
        assertNotNull(in.getTenantId());
        assertNotNull(in.getVmId());
        assertNotNull(in.getVmName());
        assertNotNull(in.getAction());
        assertNotNull(in.getId());
        assertNotNull(in.getPolicyName());
        assertNotNull(in.getPolicyVersion());
        assertNotNull(in.getRequest());

        out = Mapper.mapOne(in.toOutgoing(null), OutgoingMessage.class);
        assertNotNull(out.getRequestClient());
        assertEquals(in.getRequestClient(), out.getRequestClient());
        assertNotNull(out.getRequestTime());
        assertEquals(in.getRequestTime(), out.getRequestTime());
        assertNotNull(out.getMessage());
        assertEquals(in.getMessage(), out.getMessage());
        assertNotNull(out.getVmName());
        assertEquals(in.getVmName(), out.getVmName());
        assertNotNull(out.getPolicyName());
        assertEquals(in.getPolicyName(), out.getPolicyName());
        assertNotNull(out.getPolicyVersion());
        assertEquals(in.getPolicyVersion(), out.getPolicyVersion());
        assertNotNull(out.getOriginalRequest());
        assertNotNull(in.getRequest(), out.getOriginalRequest());
    }

    @Test
    public void testToString() {
        in = new IncomingMessage();
        assertNotNull(in.toString());
        String id = "test";
        in.setId(id);
        assertNotNull(in.toString());
        assertTrue(in.toString().contains(id));
    }

    @Test
    public void testOutgoingUpdateTime() {
        String old = out.getResponseTime();
        out.updateResponseTime();
        assertFalse(old.equals(out.getResponseTime()));
    }

    // Testing for 1510
    @Test
    public void testOutgoingToJson() {
        // Message Set
        String message = "MSG";
        out.setMessage(message);
        JSONObject json = out.toResponse();
        assertNotNull(json);
        String respStr = json.getString("response");
        assertTrue(respStr.contains(out.getResponse().getValue()));

        String msgStr = json.getString("message");
        assertNotNull(msgStr);
        assertFalse(msgStr.contains(out.getOriginalRequest())); // False for 1602
        assertTrue(msgStr.contains(out.getMessage()));

        // Null Message
        out.setMessage(null);
        json = out.toResponse();
        assertNotNull(json);
        msgStr = json.getString("message");
        assertNotNull(msgStr);
        assertFalse(msgStr.contains(out.getOriginalRequest())); // False for 1602
        assertTrue(msgStr.contains(out.getResponse().getValue()));

        // Echoing request
        assertNotNull(out.getOriginalRequest());
    }

    @Test
    public void testOutgoingToString() {
        String s = out.toString();
        assertTrue(s.contains(out.getId()));
    }
}
