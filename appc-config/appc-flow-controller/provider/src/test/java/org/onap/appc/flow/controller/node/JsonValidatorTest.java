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

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;

public class JsonValidatorTest {

  @Test
  public void should_return_json_node_on_valid_json() throws IOException {
    String json = "{'test': 'OK'}".replaceAll("'", "\"");
    JsonNode result = JsonValidator.validate(json);

    Assert.assertNotNull(result);
    Assert.assertTrue(result.has("test"));
    Assert.assertEquals("OK", result.get("test").asText());
  }

  @Test
  public void should_return_null_on_empty_input() throws IOException {
    String json = "";
    JsonNode result = JsonValidator.validate(json);

    Assert.assertNull(result);
  }

  @Test
  public void should_return_null_on_invalid_input() throws IOException {
    String json = "{'test': 'OK'".replaceAll("'", "\"");
    JsonNode result = JsonValidator.validate(json);

    Assert.assertNull(result);
  }

}