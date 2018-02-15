package org.onap.appc.flow.controller.node;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class PropertiesLoaderTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void should_load_property_file() throws IOException {
    Properties properties = PropertiesLoader.load("src/test/resources/properties_loader.properties");

    Assert.assertEquals("OK", properties.getProperty("test"));
  }

  @Test
  public void should_throw_if_file_does_not_exists() throws IOException {
    expectedException.expect(FileNotFoundException.class);
    PropertiesLoader.load("src/test/resources/non_existent.properties");
  }

}