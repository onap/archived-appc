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

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.omg.CORBA.portable.ApplicationException;
import org.onap.appc.flow.controller.data.Transaction;
import org.onap.appc.flow.controller.dbervices.FlowControlDBService;
import org.onap.appc.flow.controller.executorImpl.GraphExecutor;
import org.onap.appc.flow.controller.interfaces.FlowExecutorInterface;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({GraphExecutor.class,FlowExecutorInterface.class})
public class FlowControlNodeTest {

  private FlowControlDBService dbService;
  private SvcLogicContext ctx;
  private FlowControlNode flowControlNode;
  private FlowSequenceGenerator flowSequenceGenerator;
  private Map<String, String> inParams;
  private Transaction transaction;
  private GraphExecutor graphExecutor;

  @Before
  public void setUp() throws Exception {
    graphExecutor = mock(GraphExecutor.class);
    ctx = mock(SvcLogicContext.class);
    dbService = mock(FlowControlDBService.class);
    flowSequenceGenerator = mock(FlowSequenceGenerator.class);
    flowControlNode = new FlowControlNode(dbService, flowSequenceGenerator);
    inParams = new HashMap<>();
    PowerMockito.whenNew(GraphExecutor.class).withNoArguments().thenReturn(graphExecutor);
  }

  //@Test
  public void testProcessFlow() throws Exception {
    String transactionJson = "{\"transaction-id\": \"1\","
        + "  \"action\": \"HealthCheck\", \"action-level\": \"vnf\","
        + "  \"executionModule\": \"APPC\", \"executionRPC\": \"healthcheck\", \"executionType\": \"graph\","
        + "\"precheck\":{\"precheck-operator\":\"any\",\"precheck-options\": ["
        + "{\"pre-transaction-id\":\"1\",\"param-name\":\"executionType\",\"param-value\":\"graph\"}]} }";
    when(flowSequenceGenerator.getFlowSequence(eq(inParams), eq(ctx), anyObject()))
        .thenReturn("{\"transactions\" :["+transactionJson+"] }");
    when(ctx.getAttribute("prefix.status")).thenReturn("success");
    flowControlNode.processFlow(inParams, ctx);
  }

  @Test(expected = SvcLogicException.class)
  public void testProcessFlowWithFailure() throws Exception {
    when(flowSequenceGenerator.getFlowSequence(eq(inParams), eq(ctx), anyObject()))
        .thenReturn("{\"transactions\" :[] }");
    when(ctx.getAttribute("prefix.status")).thenReturn("fail");
    flowControlNode.processFlow(inParams, ctx);
  }
}
