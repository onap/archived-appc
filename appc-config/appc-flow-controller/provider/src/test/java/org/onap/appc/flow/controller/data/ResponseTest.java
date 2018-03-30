/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 Nokia Intellectual Property. All rights reserved.
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
package org.onap.appc.flow.controller.data;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ResponseTest {

  private Response response;

  @Before
  public void setUp() {
    response = new Response();
  }

  @Test
  public void get_set_response_action() {
    ResponseAction ra = mock(ResponseAction.class);
    response.setResponseAction(ra);
    Assert.assertEquals(ra, response.getResponseAction());
  }

  @Test
  public void get_set_response_action_handler() {
    String rah = "response_action_handler";
    response.setResponseActionHanlder(rah);
    Assert.assertEquals(rah, response.getResponseActionHanlder());
  }

  @Test
  public void get_set_response_code() {
    String responseCode = "response_code";
    response.setResponseCode(responseCode);
    Assert.assertEquals(responseCode, response.getResponseCode());
  }

  @Test
  public void get_set_response_message() {
    String responseMessage = "response_message";
    response.setResponseMessage(responseMessage);
    Assert.assertEquals(responseMessage, response.getResponseMessage());
  }

  @Test
  public void to_string() {
    response.setResponseCode("code");
    response.setResponseMessage("msg");
    response.setResponseActionHanlder("rah");

    ResponseAction ra = mock(ResponseAction.class);
    when(ra.toString()).thenReturn("ra-toString");

    response.setResponseAction(ra);

    Assert.assertEquals("Response [responseCode=code, responseMessage=msg, responseAction=ra-toString, responseActionHanlder=rah]", response.toString());
  }

}