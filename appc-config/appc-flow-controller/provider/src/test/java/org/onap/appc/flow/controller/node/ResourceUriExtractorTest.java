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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.INPUT_CONTEXT;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.INPUT_HOST_IP_ADDRESS;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.INPUT_PORT_NUMBER;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.INPUT_REQUEST_ACTION;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.INPUT_SUB_CONTEXT;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.INPUT_URL;

import java.util.Properties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

public class ResourceUriExtractorTest {

  private Properties prop;
  private SvcLogicContext ctx;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  private ResourceUriExtractor resourceUriExtractor;

  @Before
  public void setUp() {
    ctx = mock(SvcLogicContext.class);
    prop = mock(Properties.class);
    resourceUriExtractor = new ResourceUriExtractor();
  }

  @Test
  public void should_return_input_url_if_exist() throws Exception {
    ctx = mock(SvcLogicContext.class);
    when(ctx.getAttribute(INPUT_URL)).thenReturn("http://localhost:8080");

    resourceUriExtractor = new ResourceUriExtractor();
    String resourceUri = resourceUriExtractor.extractResourceUri(ctx, prop);

    Assert.assertEquals("http://localhost:8080", resourceUri);
  }

  @Test
  public void should_extract_url_input_if_context_input_provided() throws Exception {
    when(ctx.getAttribute(INPUT_URL)).thenReturn("");
    when(ctx.getAttribute(INPUT_HOST_IP_ADDRESS)).thenReturn("localhost");
    when(ctx.getAttribute(INPUT_PORT_NUMBER)).thenReturn("8080");

    when(ctx.getAttribute(INPUT_CONTEXT)).thenReturn("input-context");
    when(ctx.getAttribute(INPUT_SUB_CONTEXT)).thenReturn("input-sub-context");

    String resourceUri = resourceUriExtractor.extractResourceUri(ctx, prop);

    Assert.assertEquals("http://localhost:8080/input-context/input-sub-context", resourceUri);
  }

  @Test
  public void should_extract_url_input_if_request_action_provided() throws Exception {

    when(ctx.getAttribute(INPUT_URL)).thenReturn("");
    when(ctx.getAttribute(INPUT_HOST_IP_ADDRESS)).thenReturn("localhost");
    when(ctx.getAttribute(INPUT_PORT_NUMBER)).thenReturn("8080");

    when(ctx.getAttribute(INPUT_REQUEST_ACTION)).thenReturn("request-action");
    when(ctx.getAttribute(INPUT_REQUEST_ACTION)).thenReturn("request-action");

    when(prop.getProperty("request-action.context")).thenReturn("ra-context");
    when(prop.getProperty("request-action.sub-context")).thenReturn("ra-sub-context");

    String resourceUri = resourceUriExtractor.extractResourceUri(ctx, prop);

    Assert.assertEquals("http://localhost:8080/ra-context/ra-sub-context", resourceUri);
  }

  @Test
  public void should_throw_exception_if_missing_context() throws Exception {
    when(ctx.getAttribute(INPUT_URL)).thenReturn("");
    when(ctx.getAttribute(INPUT_HOST_IP_ADDRESS)).thenReturn("localhost");
    when(ctx.getAttribute(INPUT_PORT_NUMBER)).thenReturn("8080");

    expectedException.expect(Exception.class);
    expectedException.expectMessage("Could Not found the context for operation null");

    resourceUriExtractor.extractResourceUri(ctx, prop);
  }

}