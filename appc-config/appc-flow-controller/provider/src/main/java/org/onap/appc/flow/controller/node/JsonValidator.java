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
