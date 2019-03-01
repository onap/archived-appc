/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 Ericsson
 * ================================================================================
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

package org.onap.appc.rest.client;

import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import java.io.IOException;
import java.net.URL;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.util.HttpClientUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

@RunWith(PowerMockRunner.class)
@PrepareForTest(HttpClientUtil.class)
public class RestClientInvokerTest {

    private CloseableHttpClient client;
    private CloseableHttpResponse httpResponse;
    private HttpEntity httpEntity;
    private RestClientInvoker invoker;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void setup() throws ClientProtocolException, IOException, APPCException {
        client = Mockito.mock(CloseableHttpClient.class);
        PowerMockito.mockStatic(HttpClientUtil.class);
        PowerMockito.when(HttpClientUtil.getHttpClient("http")).thenReturn(client);
        httpResponse = Mockito.mock(CloseableHttpResponse.class);
        httpEntity = Mockito.mock(HttpEntity.class);
        Mockito.when(httpResponse.getEntity()).thenReturn(httpEntity);
        Mockito.when(client.execute(Mockito.any())).thenReturn(httpResponse);
        invoker = new RestClientInvoker(new URL("http://www.example.com:1080/docs/resource1.html"));
    }

    @Test
    public void testSetAuthentication() {
        invoker.setAuthentication("username", "password");
        assertNotNull(Whitebox.getInternalState(invoker, "basicAuth"));
    }

    @Test
    public void testDoPost() throws APPCException {
        Whitebox.setInternalState(invoker, "basicAuth", "username:password");
        assertSame(httpResponse, invoker.doPost("/path", "<body/>"));
    }

    @Test
    public void testDoPostException() throws APPCException, ClientProtocolException, IOException {
        Whitebox.setInternalState(invoker, "basicAuth", "username:password");
        Mockito.when(client.execute(Mockito.any())).thenThrow(new IOException());
        expectedEx.expect(APPCException.class);
        expectedEx.expectCause(isA(IOException.class));
        invoker.doPost("/path", "<body/>");
    }

    @Test
    public void testDoPut() throws APPCException {
        Whitebox.setInternalState(invoker, "basicAuth", "username:password");
        assertSame(httpResponse, invoker.doPut("/path", "<body/>"));
    }

    @Test
    public void testDoPutException() throws APPCException, ClientProtocolException, IOException {
        Whitebox.setInternalState(invoker, "basicAuth", "username:password");
        Mockito.when(client.execute(Mockito.any())).thenThrow(new IOException());
        expectedEx.expect(APPCException.class);
        expectedEx.expectCause(isA(IOException.class));
        invoker.doPut("/path", "<body/>");
    }

    @Test
    public void testDoGet() throws APPCException {
        Whitebox.setInternalState(invoker, "basicAuth", "username:password");
        assertSame(httpResponse, invoker.doGet("/path"));
    }

    @Test
    public void testDoGetException() throws APPCException, ClientProtocolException, IOException {
        Whitebox.setInternalState(invoker, "basicAuth", "username:password");
        Mockito.when(client.execute(Mockito.any())).thenThrow(new IOException());
        expectedEx.expect(APPCException.class);
        expectedEx.expectCause(isA(IOException.class));
        invoker.doGet("/path");
    }
}
