/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 Ericsson. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.listener.demo.impl;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.listener.demo.model.Action;
import org.onap.appc.listener.demo.model.CommonMessage.CommonHeader;
import org.onap.appc.listener.demo.model.CommonMessage.Payload;
import org.onap.appc.listener.demo.model.IncomingMessage;
import org.onap.appc.listener.util.HttpClientUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(HttpClientUtil.class)
public class TestProviderOperations {

  private ProviderOperations providerOperations;
  private IncomingMessage message;
  private URL url;
  private CommonHeader commonHeader;
  private Payload payload;
  private HttpClient httpClient;
  private HttpResponse httpResponse;
  private StatusLine statusLine;
  private HttpEntity httpEntity;
  private InputStream inputStream;
  private String reponseMessage;

  @Before
  public void setUp() throws Exception {
    reponseMessage = "{\"output\":{\"common-response-header\":{\"success\":true,\"reason\":\"\"}}}";
    providerOperations = new ProviderOperations();
    PowerMockito.mockStatic(HttpClientUtil.class);
    httpClient = PowerMockito.mock(HttpClient.class);
    httpResponse = PowerMockito.mock(HttpResponse.class);
    statusLine = PowerMockito.mock(StatusLine.class);
    httpEntity = PowerMockito.mock(HttpEntity.class);
    inputStream = new ByteArrayInputStream(reponseMessage.getBytes(StandardCharsets.UTF_8));
    message = Mockito.mock(IncomingMessage.class);
    url = PowerMockito.mock(URL.class);
    commonHeader = Mockito.mock(CommonHeader.class);
    payload = Mockito.mock(Payload.class);
    when(message.getAction()).thenReturn(Action.Evacuate);
    when(message.getHeader()).thenReturn(commonHeader);
    when(message.getPayload()).thenReturn(payload);
    when(HttpClientUtil.getHttpClient("http")).thenReturn(httpClient);
    when(httpClient.execute(anyObject())).thenReturn(httpResponse);
    when(httpResponse.getStatusLine()).thenReturn(statusLine);
    when(statusLine.getStatusCode()).thenReturn(200);
    when(httpResponse.getEntity()).thenReturn(httpEntity);
    when(httpEntity.getContent()).thenReturn(inputStream);
    Whitebox.setInternalState(url, "protocol", "http");
    Whitebox.setInternalState(providerOperations, "url", url);
  }

  @Test
  public void testTopologyDG() throws APPCException, ClientProtocolException, IOException {
    assertTrue(ProviderOperations.topologyDG(message));
  }

  @Test
  public void testTopologyDGWithBaseAuth()
      throws APPCException, ClientProtocolException, IOException {
    ProviderOperations.setAuthentication("user", "password");
    ProviderOperations.setUrl("http://localhost:8080");
    assertTrue(ProviderOperations.topologyDG(message));
  }

  @Test(expected = APPCException.class)
  public void testTopologyDGFail() throws APPCException, ClientProtocolException, IOException {
    reponseMessage =
        "{\"output\":{\"common-response-header\":{\"success\":false,\"reason\":\"\"}}}";
    inputStream = new ByteArrayInputStream(reponseMessage.getBytes(StandardCharsets.UTF_8));
    when(httpEntity.getContent()).thenReturn(inputStream);
    ProviderOperations.topologyDG(message);
  }

  @Test(expected = APPCException.class)
  public void testTopologyDGInvalidResponse()
      throws APPCException, ClientProtocolException, IOException {
    reponseMessage = "{\"output\":{\"common-response-header\":{\"succss\":false,\"reason\":\"\"}}}";
    inputStream = new ByteArrayInputStream(reponseMessage.getBytes(StandardCharsets.UTF_8));
    when(httpEntity.getContent()).thenReturn(inputStream);
    ProviderOperations.topologyDG(message);
  }

  @Test(expected = APPCException.class)
  public void testTopologyDGWithInvalidHttp()
      throws APPCException, ClientProtocolException, IOException {
    when(statusLine.getStatusCode()).thenReturn(500);
    ProviderOperations.topologyDG(message);
  }
}
