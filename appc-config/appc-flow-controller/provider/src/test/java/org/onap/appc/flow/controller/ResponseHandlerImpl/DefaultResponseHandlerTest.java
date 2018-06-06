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

import java.util.Collections;
import org.junit.Test;
import org.onap.appc.flow.controller.data.ResponseAction;
import org.onap.appc.flow.controller.data.Transaction;

public class DefaultResponseHandlerTest {

    @Test
    public void handlerResponse_shouldReturnEmptyResponseAction_whenTransactionResponsesAreNull() {
        Transaction transaction = new Transaction();
        assertDefaultResponseAction(transaction);
    }

    @Test
    public void handlerResponse_shouldReturnEmptyResponseAction_whenTransactionResponsesAreEmpty() {
        Transaction transaction = new Transaction();
        transaction.setResponses(Collections.emptyList());
        assertDefaultResponseAction(transaction);
    }

    private void assertDefaultResponseAction(Transaction transaction) {
        // GIVEN
        ResponseAction expectedResponseAction = new ResponseAction();

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
