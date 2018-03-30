/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
package org.onap.appc.flow.controller.node;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.onap.appc.flow.controller.node.RestServiceNode.APPC_CONFIG_DIR_VAR;
import static org.onap.appc.flow.controller.node.RestServiceNode.REST_RESPONSE;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.INPUT_PARAM_RESPONSE_PREFIX;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.appc.flow.controller.data.Transaction;
import org.onap.appc.flow.controller.executorImpl.RestExecutor;
import org.onap.appc.flow.controller.utils.FlowControllerConstants;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

public class TestRestServiceNode {

  private static final String RESOURCE_URI = "resource-uri";
  private static final String REST_BODY_RESPONSE = "{ 'state' : 'TEST' }";

  private RestServiceNode restServiceNode;

  private ResourceUriExtractor uriExtractorMock;
  private TransactionHandler transactionHandlerMock;
  private RestExecutor restExecutorMock;

  private SvcLogicContext ctxMock;
  private Transaction transaction;
  private Map<String, String> params;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setUp() throws Exception {

    uriExtractorMock = mock(ResourceUriExtractor.class);
    transactionHandlerMock = mock(TransactionHandler.class);
    restExecutorMock = mock(RestExecutor.class);
    ctxMock = mock(SvcLogicContext.class);
    transaction = mock(Transaction.class);

    // given
    params = new HashMap<>();

    HashMap<String, String> restResponseMap = new HashMap<>();
    restResponseMap.put(REST_RESPONSE, REST_BODY_RESPONSE.replaceAll("'", "\""));

    when(uriExtractorMock
        .extractResourceUri(any(SvcLogicContext.class), any(Properties.class)))
        .thenReturn(RESOURCE_URI);
    when(transactionHandlerMock
        .buildTransaction(any(SvcLogicContext.class), any(Properties.class), eq(RESOURCE_URI)))
        .thenReturn(transaction);
    when(restExecutorMock.execute(eq(transaction), any(SvcLogicContext.class)))
        .thenReturn(restResponseMap);

    EnvVariables envVariables = mock(EnvVariables.class);
    when(envVariables.getenv(APPC_CONFIG_DIR_VAR)).thenReturn("src/test/resources");
    restServiceNode = new RestServiceNode(transactionHandlerMock, restExecutorMock, uriExtractorMock, envVariables);
  }

  @Test
  public void should_send_request() throws Exception {
    // given
    params.put(INPUT_PARAM_RESPONSE_PREFIX, "some-prefix");

    // when
    restServiceNode.sendRequest(params, ctxMock);

    // then
    verify(uriExtractorMock)
        .extractResourceUri(eq(ctxMock), any(Properties.class));
    verify(transactionHandlerMock)
        .buildTransaction(eq(ctxMock), any(Properties.class), eq(RESOURCE_URI));
    verify(restExecutorMock)
        .execute(transaction, ctxMock);
    verifyNoMoreInteractions(uriExtractorMock, transactionHandlerMock, restExecutorMock);
  }

  @Test
  public void should_rethrow_exception_from_uri_extractor() throws Exception {
    when(uriExtractorMock
        .extractResourceUri(eq(ctxMock), any(Properties.class)))
        .thenThrow(new Exception("resource uri exception"));

    expectedException.expect(SvcLogicException.class);
    expectedException.expectMessage("resource uri exception");

    restServiceNode.sendRequest(params, ctxMock);
  }

  @Test
  public void should_rethrow_exception_from_transaction_handler() throws Exception {
    when(transactionHandlerMock
        .buildTransaction(eq(ctxMock), any(Properties.class), eq(RESOURCE_URI)))
        .thenThrow(new Exception("transaction exception"));

    expectedException.expect(SvcLogicException.class);
    expectedException.expectMessage("transaction exception");

    restServiceNode.sendRequest(params, ctxMock);
  }

  @Test
  public void should_rethrow_exception_from_rest_executor() throws Exception {
    when(restExecutorMock
        .execute(transaction, ctxMock))
        .thenThrow(new Exception("rest executor exception"));

    expectedException.expect(SvcLogicException.class);
    expectedException.expectMessage("rest executor exception");

    restServiceNode.sendRequest(params, ctxMock);
  }

  @Ignore("missing asserts")
  @Test(expected = Exception.class)
  public void testRestServiceNode() throws Exception {

    SvcLogicContext ctx = new SvcLogicContext();
    ctx.setAttribute(FlowControllerConstants.VNF_TYPE, "vUSP - vDBE-IPX HUB");
    ctx.setAttribute(FlowControllerConstants.REQUEST_ACTION, "healthcheck");
    ctx.setAttribute(FlowControllerConstants.VNFC_TYPE, "TESTVNFC-CF");
    ctx.setAttribute(FlowControllerConstants.REQUEST_ID, "TESTCOMMONFRMWK");
    ctx.setAttribute("host-ip-address", "127.0.0.1");
    ctx.setAttribute("port-number", "8888");
    ctx.setAttribute("request-action-type", "GET");
    ctx.setAttribute("context", "loader/restconf/operations/appc-provider-lcm:health-check");

    HashMap<String, String> inParams = new HashMap<String, String>();
    RestServiceNode rsn = new RestServiceNode();
    inParams.put("output-state", "state");
    inParams.put("responsePrefix", "healthcheck");
    rsn.sendRequest(inParams, ctx);

    for (Object key : ctx.getAttributeKeySet()) {
      String parmName = (String) key;
      String parmValue = ctx.getAttribute(parmName);
    }
  }

  @Ignore("missing asserts")
  @Test(expected = Exception.class)
  public void testInputParamsRestServiceNode() throws Exception {
    SvcLogicContext ctx = new SvcLogicContext();
    ctx.setAttribute("vnf-id", "test");
    ctx.setAttribute("tmp.vnfInfo.vm-count", "1");
    ctx.setAttribute("tmp.vnfInfo.vm[0].vnfc-count", "1");
    RestExecutor restExe = new RestExecutor();
    Transaction transaction = new Transaction();

    FlowControlNode node = new FlowControlNode();
    Map<String, String> flowSeq = restExe.execute(transaction, ctx);
    String flowSequnce = flowSeq.get("restResponse");

  }
}
