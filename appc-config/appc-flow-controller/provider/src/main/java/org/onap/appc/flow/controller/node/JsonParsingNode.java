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

import static org.onap.appc.flow.controller.utils.FlowControllerConstants.INPUT_PARAM_RESPONSE_PREFIX;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.OUTPUT_PARAM_ERROR_MESSAGE;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.OUTPUT_PARAM_STATUS;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.OUTPUT_STATUS_FAILURE;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.OUTPUT_STATUS_SUCCESS;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;

public class JsonParsingNode implements SvcLogicJavaPlugin {

  private static final EELFLogger log = EELFManager.getInstance().getLogger(JsonParsingNode.class);

  public void parse(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {
    String fn = "RestServiceNode.sendRequest";
    log.info("Received processParamKeys call with params : " + inParams);
    String responsePrefix = inParams.get(INPUT_PARAM_RESPONSE_PREFIX);
    responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix + ".") : "";
    try {
      String json = inParams.get("data");
      log.info("Received response from Interface " + json);
      JsonNode node = JsonValidator.validate(json);

      if (node != null) {
        Map<String, String> map = convertToMap(node);
        for (Entry<String, String> entry : map.entrySet()) {
          ctx.setAttribute(responsePrefix + entry.getKey(), entry.getValue());
        }
      }
      ctx.setAttribute(responsePrefix + OUTPUT_PARAM_STATUS, OUTPUT_STATUS_SUCCESS);

    } catch (Exception e) {
      ctx.setAttribute(responsePrefix + OUTPUT_PARAM_STATUS, OUTPUT_STATUS_FAILURE);
      ctx.setAttribute(responsePrefix + OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
      log.error(fn + " Error Message : " + e.getMessage(), e);
      throw new SvcLogicException(e.getMessage());
    }
  }

  private Map<String, String> convertToMap(JsonNode node) throws IOException {
    return new ObjectMapper().readValue(node.toString(), new TypeReference<Map<String, String>>() {
    });
  }

}
