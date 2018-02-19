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

import static org.onap.appc.flow.controller.utils.FlowControllerConstants.APPC_FLOW_CONTROLLER;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.INPUT_PARAM_RESPONSE_PREFIX;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.OUTPUT_PARAM_ERROR_MESSAGE;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.OUTPUT_PARAM_STATUS;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.OUTPUT_STATUS_FAILURE;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.OUTPUT_STATUS_MESSAGE;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.OUTPUT_STATUS_SUCCESS;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.onap.appc.flow.controller.data.Transaction;
import org.onap.appc.flow.controller.executorImpl.RestExecutor;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;

public class RestServiceNode implements SvcLogicJavaPlugin {

  private static final EELFLogger log = EELFManager.getInstance().getLogger(RestServiceNode.class);
  private static final String SDNC_CONFIG_DIR_VAR = "SDNC_CONFIG_DIR";

  public void sendRequest(Map<String, String> inParams, SvcLogicContext ctx)
      throws SvcLogicException {
    String fn = "RestServiceNode.sendRequest";
    log.info("Received processParamKeys call with params : " + inParams);
    String responsePrefix = inParams.get(INPUT_PARAM_RESPONSE_PREFIX);
    try {
      responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix + ".") : "";
      //Remove below for Block
      for (Object key : ctx.getAttributeKeySet()) {
        String parmName = (String) key;
        String parmValue = ctx.getAttribute(parmName);
        log.info(fn + "Getting Key = " + parmName + "and Value = " + parmValue);
      }

      send(ctx, inParams);
      ctx.setAttribute(responsePrefix + OUTPUT_PARAM_STATUS, OUTPUT_STATUS_SUCCESS);

    } catch (Exception e) {
      ctx.setAttribute(responsePrefix + OUTPUT_PARAM_STATUS, OUTPUT_STATUS_FAILURE);
      ctx.setAttribute(responsePrefix + OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
      log.error("Error Message : " + e.getMessage(), e);
      throw new SvcLogicException(e.getMessage());
    }
  }

  private void send(SvcLogicContext ctx, Map<String, String> inParams) throws Exception {
    try {
      Properties prop = loadProperties();
      log.info("Loaded Properties " + prop.toString());
      String responsePrefix = inParams.get(INPUT_PARAM_RESPONSE_PREFIX);
      String resourceUri = ResourceUriExtractor.extractResourceUri(ctx, prop);
      log.info("Rest Constructed URL : " + resourceUri);

      Transaction transaction = TransactionHandler.buildTransaction(ctx, prop, resourceUri);

      RestExecutor restRequestExecutor = new RestExecutor();
      Map<String, String> output = restRequestExecutor.execute(transaction, ctx);

      if (isValidJson(output.get("restResponse")) != null) {
        ctx.setAttribute(responsePrefix + "." + OUTPUT_STATUS_MESSAGE,
            output.get("restResponse"));
//                JsonNode restResponse = isValidJson(output.get("restResponse"));
//                for (String key : inParams.keySet()) {
//                    if(key !=null &&  key.startsWith("output-")){
//                            log.info("Found Key = " + key);
//                            log.info("Found Key in Params " + inParams.get(key) + ".");
//                            JsonNode setValue =  restResponse.findValue(inParams.get(key));                            
//                             log.info("Found value = " + setValue);
//                             if(setValue !=null && setValue.textValue() !=null && !setValue.textValue().isEmpty())
//                                 ctx.setAttribute(responsePrefix + "." + key, setValue.textValue());
//                             else
//                                 ctx.setAttribute(responsePrefix + "." + key, null);
//                    }
//                }                
      }
      log.info("Response from Rest :");

    } catch (Exception e) {
      log.error("Error Message: " + e.getMessage(), e);
      throw e;
    }
  }

  private Properties loadProperties() throws Exception {
    String directory = System.getenv(SDNC_CONFIG_DIR_VAR);
    if (directory == null) {
      throw new Exception("Cannot find Property file: " + SDNC_CONFIG_DIR_VAR);
    }
    String path = directory + APPC_FLOW_CONTROLLER;
    return PropertiesLoader.load(path);
  }

  private JsonNode isValidJson(String json) throws IOException {
    JsonNode output;
    log.info("Received response from Interface " + json);
    if (json == null || json.isEmpty()) {
      return null;
    }
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      output = objectMapper.readTree(json);
    } catch (JsonProcessingException e) {
      log.warn("Response received from interface is not a valid JSON block" + json, e);
      return null;
    }
    log.info("state is " + output.findValue("state"));
    return output;
  }
}
