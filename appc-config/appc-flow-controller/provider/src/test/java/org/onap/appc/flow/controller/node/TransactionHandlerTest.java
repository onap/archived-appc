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
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.INPUT_REQUEST_ACTION;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.INPUT_REQUEST_ACTION_TYPE;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.REST_PROTOCOL;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.VNF_TYPE;

import java.util.Properties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.appc.flow.controller.data.Transaction;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

public class TransactionHandlerTest {

  private static final String RESOURCE_URI = "some uri";

  private TransactionHandler transactionHandler;
  private SvcLogicContext ctx;
  private Properties prop;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setUp() {
    transactionHandler = new TransactionHandler();
    ctx = mock(SvcLogicContext.class);
    prop = mock(Properties.class);
  }

  @Test
  public void should_throw_exception_when_input_request_action_type_missing() throws Exception {

    when(ctx.getAttribute(INPUT_REQUEST_ACTION_TYPE)).thenReturn("");

    expectedException.expect(Exception.class);
    expectedException.expectMessage("Don't know vnf type to send REST request for  request-action - null");
    transactionHandler = new TransactionHandler();
    transactionHandler.buildTransaction(ctx, prop, RESOURCE_URI);
  }

  @Test
  public void should_throw_exception_when_input_request_action_missing() throws Exception {

    when(ctx.getAttribute(INPUT_REQUEST_ACTION_TYPE)).thenReturn("foo");

    expectedException.expect(Exception.class);
    expectedException.expectMessage("Don't know vnf type to send REST request for  request-action - null");
    transactionHandler.buildTransaction(ctx, prop, "some uri");
  }

  @Test
  public void should_return_proper_transaction() throws Exception {

    when(ctx.getAttribute(INPUT_REQUEST_ACTION_TYPE)).thenReturn("input-ra-type");
    when(ctx.getAttribute(INPUT_REQUEST_ACTION)).thenReturn("input-ra");
    when(ctx.getAttribute(VNF_TYPE)).thenReturn("vnf");
    
    String userKey = ctx.getAttribute(VNF_TYPE) + "." + REST_PROTOCOL + "." + ctx.getAttribute(INPUT_REQUEST_ACTION) 
                     + ".user";
    String passwordKey = ctx.getAttribute(VNF_TYPE) + "." + REST_PROTOCOL + "." + ctx.getAttribute(INPUT_REQUEST_ACTION) 
                     + ".password";

    when(prop.getProperty(userKey))
        .thenReturn("rest-user");
    when(prop.getProperty(passwordKey))
        .thenReturn("rest-pass");

    Transaction transaction = transactionHandler.buildTransaction(ctx, prop, "some uri");

    Assert.assertEquals(INPUT_REQUEST_ACTION, transaction.getAction());
    Assert.assertEquals("input-ra-type", transaction.getExecutionRPC());
    Assert.assertEquals("some uri", transaction.getExecutionEndPoint());
    Assert.assertEquals("rest-user", transaction.getuId());
    Assert.assertEquals("rest-pass", transaction.getPswd());
  }

}
