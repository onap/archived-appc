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