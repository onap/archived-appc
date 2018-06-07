/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2018 Nokia Solutions and Networks
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
package org.onap.appc.listener.LCM.model;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.onap.appc.listener.TestUtil.buildCommonHeader;

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
}
