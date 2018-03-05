/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 Nokia. All rights reserved.
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
package org.onap.appc.flow.controller.node;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.ACTION_LEVEL;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.PAYLOAD;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.REQUEST_ACTION;

import java.util.HashMap;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.onap.appc.flow.controller.data.Response;
import org.onap.appc.flow.controller.data.ResponseAction;
import org.onap.appc.flow.controller.data.Transaction;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

public class FlowGeneratorTest {

  private HashMap<String, String> inParams;
  private SvcLogicContext ctx;

  @Before
  public void setUp() {
    inParams = new HashMap<>();
    ctx = mock(SvcLogicContext.class);
  }

  @Test
  public void should_get_proper_transactions() {
    when(ctx.getAttribute(REQUEST_ACTION)).thenReturn("some-request-action");
    when(ctx.getAttribute(ACTION_LEVEL)).thenReturn("some-action-level");
    when(ctx.getAttribute(PAYLOAD)).thenReturn("some-payload");

    FlowGenerator flowGenerator = new FlowGenerator();
    List<Transaction> transactionsList = flowGenerator.createSingleStepModel(inParams, ctx).getTransactions();

    Assert.assertEquals(1, transactionsList.size());

    Transaction transaction = transactionsList.get(0);
    Assert.assertEquals(1, transaction.getTransactionId());
    Assert.assertEquals("some-request-action", transaction.getAction());
    Assert.assertEquals("some-payload", transaction.getPayload());
    Assert.assertEquals("some-action-level", transaction.getActionLevel());

    List<Response> responses = transaction.getResponses();
    Assert.assertEquals(1, responses.size());

    ResponseAction responseAction = responses.get(0).getResponseAction();
    Assert.assertTrue(responseAction.isStop());
  }

}