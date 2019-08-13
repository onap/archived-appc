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
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
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
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.ResponseBuilder;

import org.powermock.reflect.Whitebox;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.ClientBuilder;

@RunWith(PowerMockRunner.class)
@PrepareForTest({InstarClientConstant.class, SSLContext.class, Client.class,ClientBuilder.class})
public class TestDme2Client {
  
  private static final String ANY = "notNullString";
  private Dme2Client dme2;
  private InputStream inputStream;
  private SSLContext sslContext;
  private Properties properties;
  private Client client;
  private WebTarget webResource;
  private Response clientResponse;
  private ClientBuilder clientBuilder;
  
  private Invocation.Builder builder;
  
  
  @Before
  public void setUp() throws Exception {
    inputStream = Mockito.mock(InputStream.class);
    
    sslContext = PowerMockito.mock(SSLContext.class);
    
    client = Mockito.mock(Client.class);
    builder = Mockito.mock(Invocation.Builder.class);
    clientResponse = Mockito.mock(Response.class);
    webResource = Mockito.mock(WebTarget.class);
	clientBuilder = Mockito.mock(ClientBuilder.class);
    
    HashMap<String, String> data = new HashMap<String, String>();
    data.put("subtext", "value");
    
    
    mockStatic(InstarClientConstant.class);
    PowerMockito.when(InstarClientConstant.getEnvironmentVariable("SDNC_CONFIG_DIR"))
        .thenReturn("test");
    
    
    PowerMockito.when(InstarClientConstant.getInputStream("test/outbound.properties"))
        .thenReturn(inputStream);
        
    mockStatic(SSLContext.class);
    PowerMockito.when(SSLContext.getInstance("SSL")).thenReturn(sslContext);
    
    mockStatic(ClientBuilder.class);
    
    PowerMockito.when(ClientBuilder.newBuilder()).thenReturn(clientBuilder);
    doReturn(clientBuilder).when(clientBuilder).sslContext(any());
    doReturn(clientBuilder).when(clientBuilder).hostnameVerifier(any());
    
    PowerMockito.when(clientBuilder.build()).thenReturn(client);
    
    PowerMockito.when(client.target(any(URI.class))).thenReturn(webResource);

    
    PowerMockito.when(webResource.request(eq("Content-Type"),anyString())).thenReturn(builder);
    PowerMockito.when(webResource.request(anyString())).thenReturn(builder);
    
    PowerMockito.when(builder.get(eq(Response.class))).thenReturn(clientResponse);
        
    properties = Mockito.mock(Properties.class);
    dme2 = new Dme2Client("opt", "subtext", data);
    
    Whitebox.setInternalState(dme2, "properties", properties);
    when(properties.getProperty("MechID")).thenReturn("123");
    when(properties.getProperty("MechPass")).thenReturn("password");
  }

  @Test
  public void testSendtoInstarGet() throws Exception {
    PowerMockito.when(webResource.request("application/json")).thenReturn(builder);
    PowerMockito.when(clientResponse.readEntity(String.class)).thenReturn("Get Success");
    when(properties.getProperty(eq("getIpAddressByVnf_method"))).thenReturn("GET");
    
    assertEquals("Get Success", dme2.send());
  }

  @Test
  public void testSendtoInstarPut() throws Exception {
	
    PowerMockito.when(builder.put(any(Entity.class),eq(Response.class))).thenReturn(clientResponse);
    
    PowerMockito.when(clientResponse.readEntity(String.class)).thenReturn("Put Success");

    when(properties.getProperty("getIpAddressByVnf_method")).thenReturn("PUT");
    assertEquals("Put Success", dme2.send());
  }

  @Test
  public void testSendtoInstarPost() throws Exception {
    ResponseBuilder responseBuilder = clientResponse.ok();
    responseBuilder.encoding("Post Success").build();
    PowerMockito.when(builder.post(any(Entity.class),eq(Response.class))).thenReturn(clientResponse);
    PowerMockito.when(clientResponse.readEntity(String.class)).thenReturn("Post Success");
    when(properties.getProperty("getIpAddressByVnf_method")).thenReturn("POST");
    assertEquals("Post Success", dme2.send());
  }

  @Test
  public void testSendtoInstarDelete() throws Exception {
	ResponseBuilder responseBuilder = Response.ok();
    PowerMockito.when(webResource.request(anyString()).delete(eq(Response.class))).thenReturn(clientResponse);
    PowerMockito.when(clientResponse.readEntity(String.class)).thenReturn("Delete Success");
    when(properties.getProperty("getIpAddressByVnf_method")).thenReturn("DELETE");
    assertNull(dme2.send());
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
    PowerMockito.when(webResource.request("application/json")).thenReturn(builder);
    PowerMockito.when(clientResponse.readEntity(String.class)).thenReturn(null);
    when(properties.getProperty("getIpAddressByVnf_method")).thenReturn("GET");
    assertNull(dme2.send());
  }

  @Test
  public void testSendtoInstarIpNotNull() throws Exception {
    Whitebox.setInternalState(dme2, "ipAddress", "0.0.0.0");
    PowerMockito.when(webResource.request("application/json")).thenReturn(builder);
    PowerMockito.when(clientResponse.readEntity(String.class)).thenReturn(null);
    when(properties.getProperty("getIpAddressByVnf_method")).thenReturn("GET");
    assertNull(dme2.send());
  }
}
