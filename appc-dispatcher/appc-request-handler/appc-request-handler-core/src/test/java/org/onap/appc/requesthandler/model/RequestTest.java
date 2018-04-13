/*-
* ============LICENSE_START=======================================================
* ONAP : APPC
* ================================================================================
* Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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
* ============LICENSE_END=========================================================
*/
package org.onap.appc.requesthandler.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

public class RequestTest extends Request {
	private Request request;

    @Before
    public void setUp() throws Exception {
    	request = new Request();
    }

    @Test
    public void testGetRequestData() {
        assertNull(request.getRequestData());
        request.setRequestData("myRequestData");
        assertEquals(request.getRequestData(), "myRequestData");
    }
    
    @Test
    public void testSetRequestData() {
        assertNull(request.getRequestData());
        request.setRequestData("testRequestData");
        assertNotNull(request.getRequestData());
        assertEquals(request.getRequestData(), "testRequestData");
    }
    @Test
    public void testGetRequestID() {
        assertNull(request.getRequestID());
        request.setRequestID("myRequestID");
        assertEquals(request.getRequestID(), "myRequestID");
    }
    
    @Test
    public void testSetRequestID() {
        assertNull(request.getRequestID());
        request.setRequestID("testRequestID");
        assertEquals(request.getRequestID(), "testRequestID");
    }
    @Test
    public void testGetAction() {
        assertNull(request.getAction());
        request.setAction("myAction");
        assertEquals(request.getAction(), "myAction");
    }
    
    @Test
    public void testSetAction() {
        assertNull(request.getAction());
        request.setAction("testAction");
        assertEquals(request.getAction(), "testAction");
    }

}
