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
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.appc.flow.controller.dbervices.FlowControlDBService;
import org.onap.appc.flow.controller.executorImpl.GraphExecutor;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.BundleReference;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.Version;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({FrameworkUtil.class,BundleContext.class,ServiceReference.class,
  BundleReference.class,Bundle.class,Filter.class,BundleListener.class,InvalidSyntaxException.class,
  BundleException.class,FrameworkListener.class,ServiceRegistration.class,ServiceListener.class,Version.class})
public class FlowControlNodeTest {

  private FlowControlDBService dbService;
  private SvcLogicContext ctx;
  private FlowControlNode flowControlNode;
  private FlowSequenceGenerator flowSequenceGenerator;
  private Map<String, String> inParams;
  private GraphExecutor graphExecutor;

  @Before
  public void setUp() throws Exception {
    graphExecutor = mock(GraphExecutor.class);
    ctx = mock(SvcLogicContext.class);
    dbService = mock(FlowControlDBService.class);
    flowSequenceGenerator = mock(FlowSequenceGenerator.class);
    //flowControlNode = new FlowControlNode();
    flowControlNode = new FlowControlNode(dbService, flowSequenceGenerator);
    inParams = new HashMap<>();
    PowerMockito.whenNew(GraphExecutor.class).withNoArguments().thenReturn(graphExecutor);
  }

  @Test
  public void testProcessFlow() throws Exception {
    String transactionJson = "{\"transaction-id\": \"1\","
        + "  \"action\": \"HealthCheck\", \"action-level\": \"vnf\","
        + "  \"executionModule\": \"APPC\", \"executionRPC\": \"healthcheck\", \"executionType\": \"node\","
        + "\"precheck\":{\"precheck-operator\":\"any\",\"precheck-options\": ["
        + "{\"pre-transaction-id\":\"1\",\"param-name\":\"executionType\",\"param-value\":\"node\"}]} }";
    when(flowSequenceGenerator.getFlowSequence(eq(inParams), eq(ctx), anyObject()))
        .thenReturn("{\"transactions\" :["+transactionJson+"] }");
    when(ctx.getAttribute(anyString())).thenReturn("success");
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
