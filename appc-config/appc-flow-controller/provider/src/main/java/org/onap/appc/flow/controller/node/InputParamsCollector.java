/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 Nokia. All rights reserved.
 * Copyright (C) 2018 AT&T intellectual property. All rights reserved.
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

import static org.onap.appc.flow.controller.utils.FlowControllerConstants.ACTION_LEVEL;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.APPC_FLOW_CONTROLLER;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.PAYLOAD;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.REQUEST_ACTION;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.SEQ_GENERATOR_PWD;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.SEQ_GENERATOR_UID;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.SEQ_GENERATOR_URL;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.VNFC_NAME;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.VNF_ID;
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
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;
import org.apache.commons.lang3.StringUtils;
import org.onap.appc.flow.controller.data.Transaction;
import org.onap.appc.flow.controller.dbervices.FlowControlDBService;
import org.onap.appc.flow.controller.interfaceData.ActionIdentifier;
import org.onap.appc.flow.controller.interfaceData.Capabilities;
import org.onap.appc.flow.controller.interfaceData.DependencyInfo;
import org.onap.appc.flow.controller.interfaceData.Input;
import org.onap.appc.flow.controller.interfaceData.InventoryInfo;
import org.onap.appc.flow.controller.interfaceData.RequestInfo;
import org.onap.appc.flow.controller.interfaceData.Vnfcs;
import org.onap.appc.flow.controller.utils.EncryptionTool;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

class InputParamsCollector {

  private static final EELFLogger log = EELFManager.getInstance().getLogger(InputParamsCollector.class);

  private final EnvVariables envVariables;
  private final FlowControlDBService dbService;

  static final String SDNC_CONFIG_DIR_VAR = "SDNC_CONFIG_DIR";

  InputParamsCollector() {
    this.envVariables = new EnvVariables();
    this.dbService = FlowControlDBService.initialise();
  }

  InputParamsCollector(EnvVariables envVariables, FlowControlDBService dbService) {
    this.envVariables = envVariables;
    this.dbService = dbService;
  }

  Transaction collectInputParams(SvcLogicContext ctx) throws Exception {

    String fn = "FlowExecuteNode.collectInputParams";
    Properties prop = loadProperties();
    log.info("Loaded Properties " + prop.toString());

    String vnfId = ctx.getAttribute(VNF_ID);
    log.debug(fn + "vnfId :" + vnfId);

    if (StringUtils.isBlank(vnfId)) {
      throw new Exception("VnfId is missing");
    }

    String resourceUri = prop.getProperty(SEQ_GENERATOR_URL);
    log.info(fn + "resourceUri= " + resourceUri);

    String pass = EncryptionTool.getInstance().decrypt(prop.getProperty(SEQ_GENERATOR_PWD));

    Transaction transaction = new Transaction();
    transaction.setPayload(getInputData(ctx, fn, vnfId));
    transaction.setExecutionRPC("POST");
    transaction.setuId(prop.getProperty(SEQ_GENERATOR_UID));
    transaction.setPswd(pass);
    transaction.setExecutionEndPoint(resourceUri);

    return transaction;
  }

  private String getInputData(SvcLogicContext ctx, String fn, String vnfId) throws IOException, SvcLogicException {
    ActionIdentifier actionIdentifier = new ActionIdentifier();
    log.debug("Enter ActionIdentifier");

    applyIfNotBlank(vnfId, actionIdentifier::setVnfId);
    applyIfNotBlank(ctx.getAttribute(VSERVER_ID), actionIdentifier::setVserverId);
    applyIfNotBlank(ctx.getAttribute(VNFC_NAME), actionIdentifier::setVnfcName);

    log.info("ActionIdentifierData" + actionIdentifier.toString());

    log.info("Enter RequestInfo");
    RequestInfo requestInfo = getRequestInfo(ctx, actionIdentifier);
    log.debug("RequestInfo: " + requestInfo.toString());

    InventoryInfo inventoryInfo = new InventoryInfoExtractor().getInventoryInfo(ctx, vnfId);
    Capabilities capabilities = new CapabilitiesDataExtractor(dbService).getCapabilitiesData(ctx);
    ctx.setAttribute("artifact-content", null);

    log.info("Enter InputData");
    Input input = getInput(requestInfo, inventoryInfo, null, capabilities);
    log.info(fn + "Input parameters:" + input.toString());

    ObjectMapper mapper = new ObjectMapper();
    mapper.setSerializationInclusion(Include.NON_NULL);
    mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
    String inputData = mapper.writeValueAsString(input);
    log.info("InputDataJson:" + inputData);

    return inputData;
  }

  private Input getInput(RequestInfo requestInfo, InventoryInfo inventoryInfo,
      DependencyInfo dependencyInfo, Capabilities capabilities) {
    Input input = new Input();
    input.setRequestInfo(requestInfo);
    input.setInventoryInfo(inventoryInfo);
    input.setDependencyInfo(dependencyInfo);
    input.setCapabilities(capabilities);
    return input;
  }

  private RequestInfo getRequestInfo(SvcLogicContext ctx, ActionIdentifier actionIdentifier) {
    RequestInfo requestInfo = new RequestInfo();
    requestInfo.setAction(ctx.getAttribute(REQUEST_ACTION));
    requestInfo.setActionLevel(ctx.getAttribute(ACTION_LEVEL));
    requestInfo.setPayload(ctx.getAttribute(PAYLOAD));
    requestInfo.setActionIdentifier(actionIdentifier);
    return requestInfo;
  }

  DependencyInfo getDependencyInfo(SvcLogicContext ctx) throws SvcLogicException, IOException {

    String fn = "FlowExecutorNode.getDependencyInfo";
    String dependencyData = dbService.getDependencyInfo(ctx);
    log.info(fn + "dependencyDataInput:" + dependencyData);

    DependencyInfo dependencyInfo = new DependencyInfo();
    if (dependencyData == null) {
      return dependencyInfo;
    }

    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
    //JsonNode dependencyInfoData = mapper.readTree(dependencyData).get("dependencyInfo");
    JsonNode vnfcData = mapper.readTree(dependencyData).get("vnfcs");
    dependencyInfo.getVnfcs().addAll(mapper.readValue(vnfcData.toString(), new TypeReference<List<Vnfcs>>(){}));

    log.info("Dependency Output:" + dependencyInfo.toString());
    return dependencyInfo;
  }

  private Properties loadProperties() throws Exception {
    String directory = envVariables.getenv(SDNC_CONFIG_DIR_VAR);
    if (directory == null) {
      throw new Exception("Cannot find Property file -" + SDNC_CONFIG_DIR_VAR);
    }
    String path = directory + APPC_FLOW_CONTROLLER;
    return PropertiesLoader.load(path);
  }

  private void applyIfNotBlank(String parameter, Consumer<String> consumer) {
    if (StringUtils.isNotBlank(parameter)) {
      consumer.accept(parameter);
    }
  }

}
