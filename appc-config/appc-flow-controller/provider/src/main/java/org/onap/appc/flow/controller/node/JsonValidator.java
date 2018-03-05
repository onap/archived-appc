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

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;

class JsonValidator {

  private static final EELFLogger log = EELFManager.getInstance().getLogger(JsonValidator.class);

  static JsonNode validate(String json) throws IOException {
    if (StringUtils.isBlank(json)) {
      return null;
    }
    JsonNode output = null;
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      output = objectMapper.readTree(json);
    } catch (JsonProcessingException e) {
      log.warn("Response received from interface is not a valid JSON block" + json, e);
    }
    return output;
  }

}
