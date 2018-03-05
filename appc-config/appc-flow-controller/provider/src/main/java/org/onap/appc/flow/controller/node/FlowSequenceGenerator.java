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

import static org.onap.appc.flow.controller.utils.FlowControllerConstants.DESINGTIME;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.EXTERNAL;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.FLOW_SEQUENCE;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.GENERATION_NODE;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.RUNTIME;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.SEQUENCE_TYPE;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.VNFC_TYPE;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.VNF_TYPE;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.json.JSONObject;
import org.onap.appc.flow.controller.data.Transaction;
import org.onap.appc.flow.controller.data.Transactions;
import org.onap.appc.flow.controller.dbervices.FlowControlDBService;
import org.onap.appc.flow.controller.executorImpl.GraphExecutor;
import org.onap.appc.flow.controller.executorImpl.RestExecutor;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

/**
 * Helper class for FlowControlNode
 */
class FlowSequenceGenerator {

  private static final EELFLogger log = EELFManager.getInstance().getLogger(FlowSequenceGenerator.class);

  static final String MODULE = "APPC_COMMOM";

  private final FlowControlDBService dbService;
  private final FlowGenerator flowGenerator;
  private final GraphExecutor transactionExecutor;
  private final RestExecutor restExecutor;
  private final InputParamsCollector inputParamsCollector;

  FlowSequenceGenerator() {
    this.dbService = FlowControlDBService.initialise();
    this.flowGenerator = new FlowGenerator();
    this.transactionExecutor = new GraphExecutor();
    this.restExecutor = new RestExecutor();
    this.inputParamsCollector = new InputParamsCollector();
  }

  /**
   * Constructor for tests, prefer to use default one
   */
  FlowSequenceGenerator(FlowControlDBService dbService, FlowGenerator flowGenerator,
      GraphExecutor graphExecutor, RestExecutor restExecutor, EnvVariables envVariables) {
    this.dbService = dbService;
    this.flowGenerator = flowGenerator;
    this.transactionExecutor = graphExecutor;
    this.restExecutor = restExecutor;
    this.inputParamsCollector = new InputParamsCollector(envVariables, dbService);
  }

  String getFlowSequence(Map<String, String> inParams, SvcLogicContext ctx, SvcLogicContext localContext)
      throws Exception {

    String flowSequence;
    if (localContext.getAttribute(SEQUENCE_TYPE) == null) {
      Transactions trans = flowGenerator.createSingleStepModel(inParams, ctx);
      ObjectMapper mapper = new ObjectMapper();
      flowSequence = mapper.writeValueAsString(trans);
      log.debug("Single step Flow Sequence : " + flowSequence);

      return flowSequence;
    }

    if (localContext.getAttribute(GENERATION_NODE) != null) {
      flowSequence = generationNode(localContext.getAttribute(GENERATION_NODE));
    } else {
      flowSequence = getFlowSequenceFromType(ctx, localContext, localContext.getAttribute(SEQUENCE_TYPE));
    }
    return flowSequence;
  }

  private String generationNode(String generationNode) throws Exception {
    if (!transactionExecutor.hasGraph(MODULE, generationNode, null, "sync")) {
      throw new Exception("Can not find Custom defined Flow Generator for " + generationNode);
    }
    return transactionExecutor
        .executeGraph(MODULE, generationNode, null, "sync", null)
        .getProperty(FLOW_SEQUENCE);
  }

  private String getFlowSequenceFromType(SvcLogicContext ctx, SvcLogicContext localContext,
      String sequenceType) throws Exception {
    String flowSequence;
    String vnfType = ctx.getAttribute(VNF_TYPE);
    if (sequenceType.equalsIgnoreCase(DESINGTIME)) {

      localContext.setAttribute(VNFC_TYPE, ctx.getAttribute(VNFC_TYPE));
      flowSequence = dbService.getDesignTimeFlowModel(localContext);

      if (flowSequence == null) {
        throw new Exception("Flow Sequence is not found User Designed VNF " + vnfType);
      }
    } else if (sequenceType.equalsIgnoreCase(RUNTIME)) {

      Transaction transaction = inputParamsCollector.collectInputParams(ctx);
      log.info("CollectInputParamsData-Input: " + transaction.getPayload());

      Map<String, String> flowSeq = restExecutor.execute(transaction, localContext);

      JSONObject output = new JSONObject(flowSeq.get("restResponse")).optJSONObject("output");
      if (output == null) {
        throw new Exception("Failed to get the Flow Sequence runtime for VNF type " + vnfType);
      }
      flowSequence = output.toString();
      log.info("MultistepSequenceGenerator-Output: " + flowSequence);

    } else if (sequenceType.equalsIgnoreCase(EXTERNAL)) {
      //String input = collectInputParams(localContext);
      //    flowSequnce = ""; //get it from the External interface calling the Rest End point - TBD
      //if(flowSequnce == null)

      throw new Exception("Flow Sequence not found for " + vnfType);

    } else {
      //No other type of model supported...
      //in Future can get flowModel from other generators which will be included here
      throw new Exception("No information found for sequence Owner Design-Time Vs Run-Time");
    }
    return flowSequence;
  }

}
