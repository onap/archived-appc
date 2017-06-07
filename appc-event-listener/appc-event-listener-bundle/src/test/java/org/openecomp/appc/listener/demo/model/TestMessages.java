/*-
 * ============LICENSE_START=======================================================
 * APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Amdocs
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
 * ============LICENSE_END=========================================================
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */

package org.openecomp.appc.listener.demo.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.appc.listener.demo.model.IncomingMessage;
import org.openecomp.appc.listener.demo.model.OutgoingMessage;
import org.openecomp.appc.listener.demo.model.Status;
import org.openecomp.appc.listener.util.Mapper;

public class TestMessages {
    private IncomingMessage in;
    private OutgoingMessage out;

    private String incomingStr;
    private String outgoingStr;

    @Before
    public void setup() {
        try {
            incomingStr = IOUtils.toString(getClass().getResourceAsStream("/IncomingMessagedemo.txt"), "UTF-8");
            outgoingStr = IOUtils.toString(getClass().getResourceAsStream("/OutgoingMessagedemo.txt"), "UTF-8");
            assertNotNull(incomingStr);
            assertNotNull(outgoingStr);

            in = Mapper.mapOne(incomingStr, IncomingMessage.class);

            out = Mapper.mapOne(outgoingStr, OutgoingMessage.class);

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
        assertNotNull(in.getAction());
        assertNotNull(in.getHeader().getApiVer());
        assertNotNull(in.getHeader().getOriginatorId());
        assertNotNull(in.getHeader().getRequestID());
        assertNotNull(in.getHeader().getSubRequestId());
        assertNotNull(in.getHeader().getTimeStamp());
        
        assertNotNull(out);
        assertNotNull(out.getHeader().getApiVer());
        assertNotNull(out.getHeader().getOriginatorId());
        assertNotNull(out.getHeader().getRequestID());
        assertNotNull(out.getHeader().getSubRequestId());
        assertNotNull(out.getHeader().getTimeStamp());
        assertNotNull(out.getStatus().getCode());
        assertNotNull(out.getStatus().getValue());

    }

    @Test
    @Ignore
    public void testIncommingToOutgoing(){
    	OutgoingMessage newOut;
    	newOut = Mapper.mapOne(in.toOutgoing(Status.ACCEPTED), OutgoingMessage.class);
        assertNotNull(newOut);
        assertNotNull(newOut.getHeader().getApiVer());
        assertNotNull(newOut.getHeader().getOriginatorId());
        assertNotNull(newOut.getHeader().getRequestID());
        assertNotNull(newOut.getHeader().getSubRequestId());
        assertNotNull(newOut.getHeader().getTimeStamp());
        assertNotNull(newOut.getStatus().getCode());
        assertNotNull(newOut.getStatus().getValue());
    }
    
    @Test
    @Ignore
    public void testToString() {
        in = new IncomingMessage();
        assertNotNull(in.toString());
        String id = "test";
        //in.setId(id);
        assertNotNull(in.toString());
        assertTrue(in.toString().contains(id));
    }

    
    @Test
    @Ignore
    public void testOutgoingUpdateTime() {
        //String old = out.getResponseTime();
        out.updateResponseTime();
        //assertFalse(old.equals(out.getResponseTime()));
    }

    // Testing for 1510
    @Test
    @Ignore
    public void testOutgoingToJson() {
        // Message Set
        String message = "MSG";
        //out.setMessage(message);
        JSONObject json = out.toResponse();
        assertNotNull(json);
        String respStr = json.getString("response");
        //assertTrue(respStr.contains(out.getResponse().getValue()));

        String msgStr = json.getString("message");
        assertNotNull(msgStr);
        //assertFalse(msgStr.contains(out.getOriginalRequest())); // False for 1602
        //assertTrue(msgStr.contains(out.getMessage()));

        // Null Message
        //out.setMessage(null);
        json = out.toResponse();
        assertNotNull(json);
        msgStr = json.getString("message");
        assertNotNull(msgStr);
        //assertFalse(msgStr.contains(out.getOriginalRequest())); // False for 1602
        //assertTrue(msgStr.contains(out.getResponse().getValue()));

        // Echoing request
        //assertNotNull(out.getOriginalRequest());
    }

    @Test
    @Ignore
    public void testOutgoingToString() {
        String s = out.toString();
        //assertTrue(s.contains(out.getId()));
    }
}
