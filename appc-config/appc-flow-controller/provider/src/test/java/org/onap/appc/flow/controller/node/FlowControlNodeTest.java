/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
 * Modifications Copyright (C) 2019 Ericsson
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

package org.onap.appc.flow.controller.node;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.onap.appc.flow.controller.dbervices.FlowControlDBService;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

public class FlowControlNodeTest {

  private FlowControlDBService dbService;
  private SvcLogicContext ctx;
  private FlowControlNode flowControlNode;
  private FlowSequenceGenerator flowSequenceGenerator;
  private Map<String, String> inParams;

  @Before
  public void setUp() throws Exception {
    ctx = new SvcLogicContext();
    ctx.setAttribute("response.status", "success");
    dbService = mock(FlowControlDBService.class);
    flowSequenceGenerator = mock(FlowSequenceGenerator.class);
    flowControlNode = new FlowControlNode(dbService, flowSequenceGenerator);
    inParams = new HashMap<>();
    inParams.put("responsePrefix", "response");
  }

  @Test
  public void testProcessFlow() throws Exception {
    String transactionJson = "{\"transaction-id\": \"1\","
        + "  \"action\": \"HealthCheck\", \"action-level\": \"vnf\","
        + "  \"executionModule\": \"APPC\", \"executionRPC\": \"healthcheck\", \"executionType\": \"node\","
        + "\"precheck\":{\"precheck-operator\":\"any\",\"precheck-options\": ["
        + "{\"pre-transaction-id\":\"1\",\"param-name\":\"executionType\",\"param-value\":\"node\"}]} }";
    when(flowSequenceGenerator.getFlowSequence(eq(inParams), eq(ctx), anyObject()))
        .thenReturn("{\"transactions\" :[" + transactionJson + "] }");
    flowControlNode.processFlow(inParams, ctx);
    assertEquals("response.", ctx.getAttribute("response-prefix"));
  }

  @Test
  public void testProcessFlowWithoutPrecheck() throws Exception {
    String transactionJson = "{\"transaction-id\": \"1\","
        + "  \"action\": \"HealthCheck\", \"action-level\": \"vnf\","
        + "  \"executionModule\": \"APPC\", \"executionRPC\": \"healthcheck\", \"executionType\": \"node\","
        + "\"precheck\":{\"precheck-operator\":\"any\",\"precheck-options\": ["
        + "{\"pre-transaction-id\":\"1\",\"param-name\":\"executionType\",\"param-value\":\"node1\"}]} }";
    when(flowSequenceGenerator.getFlowSequence(eq(inParams), eq(ctx), anyObject()))
        .thenReturn("{\"transactions\" :[" + transactionJson + "] }");
    flowControlNode.processFlow(inParams, ctx);
    assertEquals("response.", ctx.getAttribute("response-prefix"));
  }

  @Test(expected = SvcLogicException.class)
  public void testProcessFlowWithFailure() throws Exception {
    when(flowSequenceGenerator.getFlowSequence(eq(inParams), eq(ctx), anyObject()))
        .thenReturn("{\"transactions\" :[] }");
    ctx.setAttribute("response.status", "fail");
    flowControlNode.processFlow(inParams, ctx);
  }

  @Test(expected = SvcLogicException.class)
  public void testProcessFlowWithNoExecutionType() throws Exception {
    String transactionJson = "{\"transaction-id\": \"1\","
        + "  \"action\": \"HealthCheck\", \"action-level\": \"vnf\","
        + "  \"executionModule\": \"APPC\", \"executionRPC\": \"healthcheck\", \"executionType\": \"other\","
        + "\"precheck\":{\"precheck-operator\":\"any\",\"precheck-options\": ["
        + "{\"pre-transaction-id\":\"1\",\"param-name\":\"executionType\",\"param-value\":\"other\"}]} }";
    when(flowSequenceGenerator.getFlowSequence(eq(inParams), eq(ctx), anyObject()))
        .thenReturn("{\"transactions\" :[" + transactionJson + "] }");
    ctx.setAttribute("response.status", "fail");
    flowControlNode.processFlow(inParams, ctx);
  }
}
