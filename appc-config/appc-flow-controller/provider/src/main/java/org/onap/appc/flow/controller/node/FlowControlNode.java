/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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

import static org.onap.appc.flow.controller.utils.FlowControllerConstants.ACTION_LEVEL;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.APPC_FLOW_CONTROLLER;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.DESINGTIME;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.EXTERNAL;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.FLOW_SEQUENCE;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.GENERATION_NODE;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.GRAPH;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.INPUT_PARAM_RESPONSE_PREFIX;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.NODE;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.OUTPUT_PARAM_ERROR_MESSAGE;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.OUTPUT_PARAM_STATUS;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.OUTPUT_STATUS_FAILURE;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.OUTPUT_STATUS_MESSAGE;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.OUTPUT_STATUS_SUCCESS;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.PAYLOAD;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.REQUEST_ACTION;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.REQUEST_ID;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.RESPONSE_PREFIX;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.REST;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.RUNTIME;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.SEQUENCE_TYPE;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.SEQ_GENERATOR_PWD;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.SEQ_GENERATOR_UID;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.SEQ_GENERATOR_URL;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.VF_MODULE;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.VM;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.VNF;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.VNFC;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.VNFC_NAME;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.VNFC_TYPE;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.VNF_ID;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.VNF_TYPE;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.VSERVER_ID;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.onap.appc.flow.controller.ResponseHandlerImpl.DefaultResponseHandler;
import org.onap.appc.flow.controller.data.PrecheckOption;
import org.onap.appc.flow.controller.data.ResponseAction;
import org.onap.appc.flow.controller.data.Transaction;
import org.onap.appc.flow.controller.data.Transactions;
import org.onap.appc.flow.controller.dbervices.FlowControlDBService;
import org.onap.appc.flow.controller.executorImpl.GraphExecutor;
import org.onap.appc.flow.controller.executorImpl.NodeExecutor;
import org.onap.appc.flow.controller.executorImpl.RestExecutor;
import org.onap.appc.flow.controller.interfaceData.ActionIdentifier;
import org.onap.appc.flow.controller.interfaceData.Capabilities;
import org.onap.appc.flow.controller.interfaceData.DependencyInfo;
import org.onap.appc.flow.controller.interfaceData.Input;
import org.onap.appc.flow.controller.interfaceData.InventoryInfo;
import org.onap.appc.flow.controller.interfaceData.RequestInfo;
import org.onap.appc.flow.controller.interfaceData.Vnfcs;
import org.onap.appc.flow.controller.interfaces.FlowExecutorInterface;
import org.onap.appc.flow.controller.utils.EncryptionTool;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;

public class FlowControlNode implements SvcLogicJavaPlugin {

  private static final EELFLogger log = EELFManager.getInstance().getLogger(FlowControlNode.class);

  private final FlowControlDBService dbService;
  private final FlowSequenceGenerator flowSequenceGenerator;

  public FlowControlNode() {
    this.dbService = FlowControlDBService.initialise();
    this.flowSequenceGenerator = new FlowSequenceGenerator();
  }

  FlowControlNode(FlowControlDBService dbService, FlowSequenceGenerator flowSequenceGenerator) {
    this.dbService = dbService;
    this.flowSequenceGenerator = flowSequenceGenerator;
  }

  public void processFlow(Map<String, String> inParams, SvcLogicContext ctx)
      throws SvcLogicException {
    log.debug("Received processParamKeys call with params : " + inParams);
    String responsePrefix = inParams.get(INPUT_PARAM_RESPONSE_PREFIX);
    try {
      responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix + ".") : "";
      SvcLogicContext localContext = new SvcLogicContext();

      localContext.setAttribute(REQUEST_ID, ctx.getAttribute(REQUEST_ID));
      localContext.setAttribute(VNF_TYPE, ctx.getAttribute(VNF_TYPE));
      localContext.setAttribute(REQUEST_ACTION, ctx.getAttribute(REQUEST_ACTION));
      localContext.setAttribute(ACTION_LEVEL, ctx.getAttribute(ACTION_LEVEL));
      localContext.setAttribute(RESPONSE_PREFIX, responsePrefix);
      ctx.setAttribute(RESPONSE_PREFIX, responsePrefix);

      dbService.getFlowReferenceData(ctx, inParams, localContext);

      for (String key : localContext.getAttributeKeySet()) {
        log.debug("processFlow " + key + "=" + ctx.getAttribute(key));
      }
      processFlowSequence(inParams, ctx, localContext);
      if (!ctx.getAttribute(responsePrefix + OUTPUT_PARAM_STATUS).equals(OUTPUT_STATUS_SUCCESS)) {
        throw new SvcLogicException(ctx.getAttribute(responsePrefix + OUTPUT_STATUS_MESSAGE));
      }
    } catch (Exception e) {
      ctx.setAttribute(responsePrefix + OUTPUT_PARAM_STATUS, OUTPUT_STATUS_FAILURE);
      ctx.setAttribute(responsePrefix + OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
      log.error("Error occurred in processFlow ", e);
      throw new SvcLogicException(e.getMessage());
    }
  }

  private void processFlowSequence(Map<String, String> inParams, SvcLogicContext ctx, SvcLogicContext localContext)
      throws Exception {

    String fn = "FlowExecutorNode.processflowSequence";
    log.debug(fn + "Received model for flow : " + localContext.toString());

    localContext
        .getAttributeKeySet()
        .forEach(key -> log.debug(key + "=" + ctx.getAttribute(key)));

    String flowSequence = flowSequenceGenerator.getFlowSequence(inParams, ctx, localContext);

    log.debug("Received Flow Sequence : " + flowSequence);
    HashMap<Integer, Transaction> transactionMap = createTransactionMap(flowSequence, localContext);
    executeAllTransaction(transactionMap, ctx);
    log.info("Executed all the transaction successfully");
  }

  private void executeAllTransaction(HashMap<Integer, Transaction> transactionMap, SvcLogicContext ctx)
      throws Exception {

    String fn = "FlowExecutorNode.executeAllTransaction ";
    int retry = 0;
    FlowExecutorInterface flowExecutor;
    for (int key = 1; key <= transactionMap.size(); key++) {
      log.debug(fn + "Starting transactions ID " + key + " :)=" + retry);
      Transaction transaction = transactionMap.get(key);
      if (!preProcessor(transactionMap, transaction)) {
        log.info("Skipping Transaction ID " + transaction.getTransactionId());
        continue;
      }
      if (transaction.getExecutionType() != null) {
        switch (transaction.getExecutionType()) {
          case GRAPH:
            flowExecutor = new GraphExecutor();
            break;
          case NODE:
            flowExecutor = new NodeExecutor();
            break;
          case REST:
            flowExecutor = new RestExecutor();
            break;
          default:
            throw new Exception("No Executor found for transaction ID" + transaction.getTransactionId());
        }
        flowExecutor.execute(transaction, ctx);
        ResponseAction responseAction = handleResponse(transaction);

        if (responseAction.getWait() != null && Integer.parseInt(responseAction.getWait()) > 0) {
          log.debug(fn + "Going to Sleep .... " + responseAction.getWait());
          Thread.sleep(Integer.parseInt(responseAction.getWait()) * 1000L);
        }
        if (responseAction.isIntermediateMessage()) {
          log.debug(fn + "Sending Intermediate Message back  .... ");
          sendIntermediateMessage();
        }
        if (responseAction.getRetry() != null && Integer.parseInt(responseAction.getRetry()) > retry) {
          log.debug(fn + "Ooppss!!! We will retry again ....... ");
          key--;
          retry++;
          log.debug(fn + "key =" + key + "retry =" + retry);
        }
        if (responseAction.isIgnore()) {
          log.debug(fn + "Ignoring this Error and moving ahead  ....... ");
          continue;
        }
        if (responseAction.isStop()) {
          log.debug(fn + "Need to Stop  ....... ");
          break;
        }
        if (responseAction.getJump() != null && Integer.parseInt(responseAction.getJump()) > 0) {
          key = Integer.parseInt(responseAction.getJump());
          key--;
        }
        log.debug(fn + "key =" + key + "retry =" + retry);

      } else {
        throw new Exception("Don't know how to execute transaction ID " + transaction.getTransactionId());
      }
    }
  }

  private void sendIntermediateMessage() {
    // TODO Auto-generated method stub
  }

  private ResponseAction handleResponse(Transaction transaction) {
    log.info("Handling Response for transaction Id " + transaction.getTransactionId());
    DefaultResponseHandler defaultHandler = new DefaultResponseHandler();
    return defaultHandler.handlerResponse(transaction);
  }

  private boolean preProcessor(HashMap<Integer, Transaction> transactionMap, Transaction transaction)
      throws IOException {

    log.debug("Starting Preprocessing Logic ");
    boolean runThisStep = false;
    try {
      if (transaction.getPrecheck() != null
          && transaction.getPrecheck().getPrecheckOptions() != null
          && !transaction.getPrecheck().getPrecheckOptions().isEmpty()) {

        List<PrecheckOption> precheckOptions = transaction.getPrecheck().getPrecheckOptions();
        for (PrecheckOption precheck : precheckOptions) {
          Transaction trans = transactionMap.get(precheck.getpTransactionID());
          ObjectMapper mapper = new ObjectMapper();
          log.info("Mapper= " + mapper.writeValueAsString(trans));
          HashMap trmap = mapper.readValue(mapper.writeValueAsString(trans), HashMap.class);
          runThisStep = trmap.get(precheck.getParamName()) != null
              && ((String) trmap.get(precheck.getParamName()))
              .equalsIgnoreCase(precheck.getParamValue());

          if (("any").equalsIgnoreCase(transaction.getPrecheck().getPrecheckOperator()) && runThisStep) {
            break;
          }
        }
      } else {
        log.debug("No Pre check defined for transaction ID " + transaction.getTransactionId());
        runThisStep = true;
      }
    } catch (Exception e) {
      log.error("Error occured when Preprocessing Logic ", e);
      throw e;
    }
    log.debug("Returing process current Transaction = " + runThisStep);
    return runThisStep;
  }

  private HashMap<Integer, Transaction> createTransactionMap(String flowSequence, SvcLogicContext localContext)
      throws Exception {

    ObjectMapper mapper = new ObjectMapper();
    Transactions transactions = mapper.readValue(flowSequence, Transactions.class);
    HashMap<Integer, Transaction> transMap = new HashMap<>();
    for (Transaction transaction : transactions.getTransactions()) {
      compileFlowDependencies(transaction, localContext);
      //parse the Transactions Object and create records in process_flow_status table
      //loadTransactionIntoStatus(transactions, ctx);
      transMap.put(transaction.getTransactionId(), transaction);
    }
    return transMap;
  }

  private void compileFlowDependencies(Transaction transaction, SvcLogicContext localContext)
      throws Exception {

    dbService.populateModuleAndRPC(transaction, localContext.getAttribute(VNF_TYPE));
    ObjectMapper mapper = new ObjectMapper();
    log.debug("Individual Transaction Details :" + transaction.toString());

    if ((localContext.getAttribute(SEQUENCE_TYPE) == null)
        || (localContext.getAttribute(SEQUENCE_TYPE) != null
        && !localContext.getAttribute(SEQUENCE_TYPE)
        .equalsIgnoreCase(DESINGTIME))) {

      localContext.setAttribute("artifact-content", mapper.writeValueAsString(transaction));
      dbService.loadSequenceIntoDB(localContext);
    }
    //get a field in transction class as transactionhandle interface and register the Handler here for each trnactions
  }

}
