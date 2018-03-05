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