/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
 * Modifications Copyright (C) 2019 Ericsson
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

package org.onap.appc.instar.node;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;
import java.io.InputStream;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Properties;
import javax.net.ssl.SSLContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.onap.appc.instar.dme2client.Dme2Client;
import org.onap.appc.instar.utils.InstarClientConstant;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;

@RunWith(PowerMockRunner.class)
@PrepareForTest({InstarClientConstant.class, SSLContext.class, Client.class})
public class TestDme2Client {

  private Dme2Client dme2;
  private InputStream inputStream;
  private SSLContext sslContext;
  private Properties properties;
  private Client client;
  private WebResource webResource;
  private URI uri;
  private Builder builder;
  private ClientResponse clientResponse;

  @Before
  public void setUp() throws Exception {
    inputStream = Mockito.mock(InputStream.class);
    sslContext = PowerMockito.mock(SSLContext.class);
    client = Mockito.mock(Client.class);
    uri = new URI("nullnullnullvalue");
    builder = Mockito.mock(Builder.class);
    clientResponse = Mockito.mock(ClientResponse.class);
    webResource = Mockito.mock(WebResource.class);
    HashMap<String, String> data = new HashMap<String, String>();
    data.put("subtext", "value");
    PowerMockito.mockStatic(InstarClientConstant.class);
    PowerMockito.mockStatic(SSLContext.class);
    PowerMockito.mockStatic(Client.class);
    PowerMockito.when(InstarClientConstant.getEnvironmentVariable("SDNC_CONFIG_DIR"))
        .thenReturn("test");
    PowerMockito.when(InstarClientConstant.getInputStream("test/outbound.properties"))
        .thenReturn(inputStream);
    PowerMockito.when(SSLContext.getInstance("SSL")).thenReturn(sslContext);
    PowerMockito.when(Client.create(anyObject())).thenReturn(client);
    PowerMockito.when(client.resource(uri)).thenReturn(webResource);

    PowerMockito.when(builder.get(ClientResponse.class)).thenReturn(clientResponse);
    properties = Mockito.mock(Properties.class);
    dme2 = new Dme2Client("opt", "subtext", data);
    Whitebox.setInternalState(dme2, "properties", properties);
    when(properties.getProperty("MechID")).thenReturn("123");
    when(properties.getProperty("MechPass")).thenReturn("password");
  }

  @Test
  public void testSendtoInstarGet() throws Exception {
    PowerMockito.when(webResource.accept("application/json")).thenReturn(builder);
    PowerMockito.when(clientResponse.getEntity(String.class)).thenReturn("Get Success");
    when(properties.getProperty("getIpAddressByVnf_method")).thenReturn("GET");
    assertEquals("Get Success", dme2.send());
  }

  @Test
  public void testSendtoInstarPut() throws Exception {
    PowerMockito.when(webResource.type("application/json")).thenReturn(builder);
    PowerMockito.when(builder.put(ClientResponse.class, "")).thenReturn(clientResponse);
    PowerMockito.when(clientResponse.getEntity(String.class)).thenReturn("Put Success");
    when(properties.getProperty("getIpAddressByVnf_method")).thenReturn("PUT");
    assertEquals("Put Success", dme2.send());
  }

  @Test
  public void testSendtoInstarPost() throws Exception {
    PowerMockito.when(webResource.type("application/json")).thenReturn(builder);
    PowerMockito.when(builder.post(ClientResponse.class, "")).thenReturn(clientResponse);
    PowerMockito.when(clientResponse.getEntity(String.class)).thenReturn("Post Success");
    when(properties.getProperty("getIpAddressByVnf_method")).thenReturn("POST");
    assertEquals("Post Success", dme2.send());
  }

  @Test
  public void testSendtoInstarDelete() throws Exception {
    PowerMockito.when(webResource.delete(ClientResponse.class)).thenReturn(clientResponse);
    PowerMockito.when(clientResponse.getEntity(String.class)).thenReturn("Delete Success");
    when(properties.getProperty("getIpAddressByVnf_method")).thenReturn("DELETE");
    assertEquals("Delete Success", dme2.send());
  }

  @Test
  public void testSendtoInstarException() throws Exception {
    PowerMockito.when(SSLContext.getInstance("SSL")).thenThrow(new NoSuchAlgorithmException());
    when(properties.getProperty("getIpAddressByVnf_method")).thenReturn("DELETE");
    assertNull(dme2.send());
  }

  @Test
  public void testSendtoInstarMaskNotNull() throws Exception {
    Whitebox.setInternalState(dme2, "mask", "0.0.0.0/1");
    PowerMockito.when(webResource.accept("application/json")).thenReturn(builder);
    PowerMockito.when(clientResponse.getEntity(String.class)).thenReturn("Get Success");
    when(properties.getProperty("getIpAddressByVnf_method")).thenReturn("GET");
    assertNull(dme2.send());
  }

  @Test
  public void testSendtoInstarIpNotNull() throws Exception {
    Whitebox.setInternalState(dme2, "ipAddress", "0.0.0.0");
    PowerMockito.when(webResource.accept("application/json")).thenReturn(builder);
    PowerMockito.when(clientResponse.getEntity(String.class)).thenReturn("Get Success");
    when(properties.getProperty("getIpAddressByVnf_method")).thenReturn("GET");
    assertNull(dme2.send());
  }
}
