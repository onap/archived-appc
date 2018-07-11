/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 Nokia.
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
package org.onap.appc.flow.controller.ResponseHandlerImpl;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import java.util.Collections;
import org.junit.Test;
import org.onap.appc.flow.controller.data.Response;
import org.onap.appc.flow.controller.data.ResponseAction;
import org.onap.appc.flow.controller.data.Transaction;

public class DefaultResponseHandlerTest {

    @Test
    public void handlerResponse_shouldReturnEmptyResponseAction_whenTransactionResponsesAreNull() {
        Transaction transaction = new Transaction();
        assertExpectedResponseAction(transaction, new ResponseAction());
    }

    @Test
    public void handlerResponse_shouldReturnEmptyResponseAction_whenTransactionResponsesAreEmpty() {
        Transaction transaction = new Transaction();
        transaction.setResponses(Collections.emptyList());
        assertExpectedResponseAction(transaction, new ResponseAction());
    }

    @Test
    public void handlerResponse_shouldReturnExpectedResponseAction_forMatchingStatusCode() {
        // GIVEN
        ResponseAction expectedResponseAction = createExpectedResponseAction();
        String responseCode = "404";

        Transaction transaction = new Transaction();
        transaction.setStatusCode(responseCode);
        transaction.setResponses(Lists.newArrayList(
            createResponse(null, null),
            createResponse(null, "500"),
            createResponse(expectedResponseAction, responseCode)));

        assertExpectedResponseAction(transaction, expectedResponseAction);
    }

    private ResponseAction createExpectedResponseAction() {
        ResponseAction expectedResponseAction = new ResponseAction();
        expectedResponseAction.setIntermediateMessage(true);
        expectedResponseAction.setJump("value1");
        expectedResponseAction.setRetry("value2");
        expectedResponseAction.setWait("value3");
        expectedResponseAction.setIgnore(true);
        expectedResponseAction.setStop(true);
        return expectedResponseAction;
    }

    private Response createResponse(ResponseAction expectedResponseAction, String responseCode) {
        Response response = new Response();
        response.setResponseCode(responseCode);
        response.setResponseAction(expectedResponseAction);
        return response;
    }

    private void assertExpectedResponseAction(Transaction transaction, ResponseAction expectedResponseAction) {
        // WHEN
        ResponseAction responseAction = new DefaultResponseHandler().handlerResponse(transaction);

        // THEN
        assertEquals(expectedResponseAction.isIntermediateMessage(), responseAction.isIntermediateMessage());
        assertEquals(expectedResponseAction.getJump(), responseAction.getJump());
        assertEquals(expectedResponseAction.getRetry(), responseAction.getRetry());
        assertEquals(expectedResponseAction.getWait(), responseAction.getWait());
        assertEquals(expectedResponseAction.isIgnore(), responseAction.isIgnore());
        assertEquals(expectedResponseAction.isStop(), responseAction.isStop());
        assertEquals(expectedResponseAction.toString(), responseAction.toString());
    }
}
