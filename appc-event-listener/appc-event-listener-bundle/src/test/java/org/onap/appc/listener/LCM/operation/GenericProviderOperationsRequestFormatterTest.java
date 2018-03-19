/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */
package org.onap.appc.listener.LCM.operation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.onap.appc.listener.TestUtil.JSON_OUTPUT_BODY_STR;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.MalformedURLException;
import java.net.URL;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.listener.LCM.model.InputBody;
import org.onap.appc.listener.LCM.model.ResponseStatus;
import org.onap.appc.listener.util.Mapper;

public class GenericProviderOperationsRequestFormatterTest {

    private static final String INVALID_JSON_OUTPUT_BODY_STR =
        "{\"output\":{\"common-header\":{\"timestamp\":\"2016-08-03T08:50:18.97Z\","
            + "\"api-ver\":\"1\",\"flags\":{\"force\":\"TRUE\",\"ttl\":\"9900\"},\"sub-request-id\":\"1\","
            + "\"request-id\":\"123\",\"originator-id\":\"1\"}}}";

    private GenericProviderOperationRequestFormatter requestFormatter;


    @Before
    public void setup() {
        requestFormatter = new GenericProviderOperationRequestFormatter();
    }

    @Test
    public void should_build_path() throws MalformedURLException {
        String result = requestFormatter.buildPath(new URL("http://127.0.0.1/abc/def"), "test");
        assertEquals("/abc/def:test", result);
    }

    @Test
    public void should_build_request_json() {
        InputBody inputBody = new InputBody();
        inputBody.setPayload("\"key1\": \"value1\", \"key2\": \"value2\"");

        assertEquals("{\"input\": {\"payload\":\"\\\"key1\\\": \\\"value1\\\", \\\"key2\\\": \\\"value2\\\"\"}}",
            requestFormatter.buildRequest(inputBody));
    }

    @Test(expected = APPCException.class)
    public void should_throw_when_invalid_json() throws APPCException {

        JsonNode jsonNode = Mapper.toJsonNodeFromJsonString(INVALID_JSON_OUTPUT_BODY_STR);
        requestFormatter.getResponseStatus(jsonNode);
    }

    @Test
    public void should_extract_response_status() throws APPCException {

        JsonNode jsonNode = Mapper.toJsonNodeFromJsonString(JSON_OUTPUT_BODY_STR);
        ResponseStatus status = requestFormatter.getResponseStatus(jsonNode);

        assertEquals("test message", status.getValue());
        assertEquals(Integer.valueOf(200), status.getCode());
    }

    @Test
    public void should_return_extract_locked_field() throws APPCException {

        assertNull(requestFormatter.getLocked(new JSONObject(INVALID_JSON_OUTPUT_BODY_STR)));
        assertEquals("test-locked", requestFormatter.getLocked(new JSONObject(JSON_OUTPUT_BODY_STR)));
    }
}
