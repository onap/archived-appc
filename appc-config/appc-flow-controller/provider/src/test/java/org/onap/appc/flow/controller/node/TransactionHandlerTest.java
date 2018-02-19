package org.onap.appc.flow.controller.node;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.INPUT_REQUEST_ACTION;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.INPUT_REQUEST_ACTION_TYPE;

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

  private SvcLogicContext ctx;
  private Properties prop;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setUp() {
    ctx = mock(SvcLogicContext.class);
    prop = mock(Properties.class);
  }

  @Test
  public void should_throw_exception_when_input_request_action_type_missing() throws Exception {

    when(ctx.getAttribute(INPUT_REQUEST_ACTION_TYPE)).thenReturn("");

    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("Don't know REST operation for Action");
    TransactionHandler.buildTransaction(ctx, prop, RESOURCE_URI);
  }

  @Test
  public void should_throw_exception_when_input_request_action_missing() throws Exception {

    when(ctx.getAttribute(INPUT_REQUEST_ACTION_TYPE)).thenReturn("foo");

    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("Don't know request-action request-action");
    TransactionHandler.buildTransaction(ctx, prop, "some uri");
  }

  @Test
  public void should_return_proper_transaction() throws Exception {

    when(ctx.getAttribute(INPUT_REQUEST_ACTION_TYPE)).thenReturn("input-ra-type");
    when(ctx.getAttribute(INPUT_REQUEST_ACTION)).thenReturn("input-ra");

    when(prop.getProperty(ctx.getAttribute(INPUT_REQUEST_ACTION).concat(".default-rest-user")))
        .thenReturn("rest-user");
    when(prop.getProperty(ctx.getAttribute(INPUT_REQUEST_ACTION).concat(".default-rest-pass")))
        .thenReturn("rest-pass");

    Transaction transaction = TransactionHandler.buildTransaction(ctx, prop, "some uri");

    Assert.assertEquals(INPUT_REQUEST_ACTION, transaction.getAction());
    Assert.assertEquals("input-ra-type", transaction.getExecutionRPC());
    Assert.assertEquals("some uri", transaction.getExecutionEndPoint());
    Assert.assertEquals("rest-user", transaction.getId());
    Assert.assertEquals("rest-pass", transaction.getPswd());
  }

}