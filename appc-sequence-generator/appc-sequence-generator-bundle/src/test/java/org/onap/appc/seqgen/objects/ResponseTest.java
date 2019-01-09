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
package org.onap.appc.seqgen.objects;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.Map;
import java.util.HashMap;

public class ResponseTest {

    private Response response;

    @Before
    public void setUp() {
        response = new Response();
    }
  
    @Test
    public void get_set_response_code() {
        String responseCode = "response_code";
        response.setResponseCode(responseCode);
        assertEquals(responseCode, response.getResponseCode());
        assertNotNull(responseCode, response.getResponseCode());
    }

    @Test
    public void get_set_response_message() {
        String responseMessage = "response_message";
        response.setResponseMessage(responseMessage);
        assertEquals(responseMessage, response.getResponseMessage());
        assertNotNull(responseMessage, response.getResponseMessage());
    }

    @Test
    public void get_set_ResponseAction() {
        Map<String, String> responseAction = new HashMap();
        responseAction.put("hello","world");
        response.setResponseAction(responseAction);
        assertEquals(responseAction, response.getResponseAction());
    }

}
