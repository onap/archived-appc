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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.onap.appc.flow.controller.node.FlowSequenceGenerator.MODULE;
import static org.onap.appc.flow.controller.node.InputParamsCollector.SDNC_CONFIG_DIR_VAR;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.DESINGTIME;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.EXTERNAL;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.FLOW_SEQUENCE;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.GENERATION_NODE;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.RUNTIME;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.SEQUENCE_TYPE;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.VNFC_TYPE;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.VNF_ID;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.VNF_TYPE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.appc.flow.controller.data.Transaction;
import org.onap.appc.flow.controller.data.Transactions;
import org.onap.appc.flow.controller.dbervices.FlowControlDBService;
import org.onap.appc.flow.controller.executorImpl.GraphExecutor;
import org.onap.appc.flow.controller.executorImpl.RestExecutor;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

public class FlowSequenceGeneratorTest {

  private FlowControlDBService dbService;
  private Map<String, String> inParams;
  private SvcLogicContext localCtx;
  private SvcLogicContext ctx;
  private FlowGenerator flowGenerator;
  private GraphExecutor graphExecutor;
  private FlowSequenceGenerator flowSequenceGenerator;
  private RestExecutor restExecutor;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setUp() {
    inParams = new HashMap<>();
    ctx = mock(SvcLogicContext.class);
    localCtx = mock(SvcLogicContext.class);
    dbService = mock(FlowControlDBService.class);
    flowGenerator = mock(FlowGenerator.class);
    graphExecutor = mock(GraphExecutor.class);
    restExecutor = mock(RestExecutor.class);

    EnvVariables envVariables = mock(EnvVariables.class);

    when(envVariables.getenv(SDNC_CONFIG_DIR_VAR)).thenReturn("./src/test/resources");

    flowSequenceGenerator = new FlowSequenceGenerator(
        dbService,
        flowGenerator,
        graphExecutor,
        restExecutor,
        envVariables
    );
  }

  @Test
  public void sequence_type_is_null() throws Exception {

    Transaction transaction = new Transaction();
    transaction.setExecutionType("mock-flow-generator");
    ArrayList<Transaction> transactionList = new ArrayList<>();
    transactionList.add(transaction);
    Transactions transactions = new Transactions();
    transactions.setTransactions(transactionList);

    when(flowGenerator.createSingleStepModel(inParams, ctx)).thenReturn(transactions);

    String seq = flowSequenceGenerator.getFlowSequence(inParams, ctx, localCtx);

    Assert.assertEquals("{\"transactions\":[{\"executionType\":\"mock-flow-generator\",\"uId\":null,\"statusCode\":null,\"pswd\":null,\"executionEndPoint\":null,\"executionModule\":null,\"executionRPC\":null,\"status\":\"PENDING\",\"transaction-id\":0,\"action\":null,\"action-level\":null,\"action-identifier\":null,\"parameters\":null,\"state\":null,\"precheck\":null,\"payload\":null,\"responses\":null}]}", seq);
  }

  @Test
  public void sequence_type_is_not_null_generator_and_generator_node_exists() throws Exception {

    when(localCtx.getAttribute(SEQUENCE_TYPE)).thenReturn("some-seq-type");
    when(localCtx.getAttribute(GENERATION_NODE)).thenReturn("some-gen-node");

    when(graphExecutor.hasGraph(MODULE, "some-gen-node", null,"sync")).thenReturn(true);

    Properties properties = new Properties();
    properties.setProperty(FLOW_SEQUENCE, "flow-sequence");
    when(graphExecutor.executeGraph(MODULE, "some-gen-node", null, "sync", null)).thenReturn(properties);

    String seq = flowSequenceGenerator.getFlowSequence(inParams, ctx, localCtx);

    Assert.assertEquals("flow-sequence", seq);
  }

  @Test
  public void sequence_type_is_not_null_generator_exists_but_generator_node_not_exists() throws Exception {

    when(localCtx.getAttribute(SEQUENCE_TYPE)).thenReturn("some-seq-type");
    when(localCtx.getAttribute(GENERATION_NODE)).thenReturn("some-gen-node");

    when(graphExecutor.hasGraph(MODULE, "some-gen-node", null,"sync")).thenReturn(false);

    expectedException.expectMessage("Can not find Custom defined Flow Generator for some-gen-node");
    flowSequenceGenerator.getFlowSequence(inParams, ctx, localCtx);
  }

  @Test
  public void sequence_type_is_design_time() throws Exception {

    when(localCtx.getAttribute(SEQUENCE_TYPE)).thenReturn(DESINGTIME);
    when(dbService.getDesignTimeFlowModel(localCtx)).thenReturn("flow-sequence");

    String flowSequence = flowSequenceGenerator.getFlowSequence(inParams, ctx, localCtx);
    verify(localCtx).setAttribute(VNFC_TYPE, ctx.getAttribute(VNFC_TYPE));

    Assert.assertEquals("flow-sequence", flowSequence);
  }

  @Test
  public void sequence_type_is_design_time_but_got_null() throws Exception {

    when(localCtx.getAttribute(SEQUENCE_TYPE)).thenReturn(DESINGTIME);
    when(ctx.getAttribute(VNF_TYPE)).thenReturn("some-vnf-type");
    when(dbService.getDesignTimeFlowModel(localCtx)).thenReturn(null);

    expectedException.expectMessage("Flow Sequence is not found User Designed VNF some-vnf-type");
    flowSequenceGenerator.getFlowSequence(inParams, ctx, localCtx);
    verify(localCtx).setAttribute(VNFC_TYPE, ctx.getAttribute(VNFC_TYPE));
  }

  @Test
  public void sequence_type_is_runtime() throws Exception {
    when(localCtx.getAttribute(SEQUENCE_TYPE)).thenReturn(RUNTIME);
    when(ctx.getAttribute(VNF_TYPE)).thenReturn("some-vnf-type");
    when(ctx.getAttribute(VNF_ID)).thenReturn("some-vnf-id");

    Map<String, String> map = new HashMap<>();
    map.put("restResponse", "{'output':{'dummy-json-object':'some-param'}}".replaceAll("'", "\""));
    when(restExecutor.execute(any(Transaction.class), eq(localCtx))).thenReturn(map);

    String flowSequence = flowSequenceGenerator.getFlowSequence(inParams, ctx, localCtx);

    Assert.assertEquals("{'dummy-json-object':'some-param'}".replaceAll("'", "\""), flowSequence);
  }

  @Test
  public void sequence_type_is_runtime_but_got_null() throws Exception {
    when(localCtx.getAttribute(SEQUENCE_TYPE)).thenReturn(RUNTIME);
    when(ctx.getAttribute(VNF_TYPE)).thenReturn("some-vnf-type");
    when(ctx.getAttribute(VNF_ID)).thenReturn("some-vnf-id");

    Map<String, String> map = new HashMap<>();
    map.put("restResponse", "{}".replaceAll("'", "\""));
    when(restExecutor.execute(any(Transaction.class), eq(localCtx))).thenReturn(map);

    expectedException.expectMessage("Failed to get the Flow Sequence runtime for VNF type some-vnf-type");
    flowSequenceGenerator.getFlowSequence(inParams, ctx, localCtx);
  }

  @Test
  public void sequence_type_is_external() throws Exception {
    when(localCtx.getAttribute(SEQUENCE_TYPE)).thenReturn(EXTERNAL);
    when(ctx.getAttribute(VNF_TYPE)).thenReturn("some-vnf-type");

    expectedException.expectMessage("Flow Sequence not found for some-vnf-type");
    flowSequenceGenerator.getFlowSequence(inParams, ctx, localCtx);
  }

  @Test
  public void unknown_sequence() throws Exception {
    when(localCtx.getAttribute(SEQUENCE_TYPE)).thenReturn("some-unknown-type");

    expectedException.expectMessage("No information found for sequence Owner Design-Time Vs Run-Time");
    flowSequenceGenerator.getFlowSequence(inParams, ctx, localCtx);
  }

}