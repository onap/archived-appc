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

package org.onap.appc.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import java.io.IOException;
import java.util.Properties;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.onap.appc.configuration.Configuration;
import org.onap.appc.configuration.ConfigurationFactory;
import org.onap.appc.exceptions.APPCException;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


@RunWith(PowerMockRunner.class)
@PrepareForTest({ConfigurationFactory.class, HttpClients.class})
public class HttpClientTest {

    private CloseableHttpClient client;
    private CloseableHttpResponse httpResponse;
    private StatusLine statusLine;
    private HttpEntity httpEntity;


    @Before
    public void setup() throws ClientProtocolException, IOException {
        Configuration configuration = Mockito.mock(Configuration.class);
        Properties properties = new Properties();
        properties.put("username", "username");
        properties.put("password", "password");
        Mockito.when(configuration.getProperty("username")).thenReturn("username");
        Mockito.when(configuration.getProperty("password")).thenReturn("password");
        PowerMockito.mockStatic(ConfigurationFactory.class);
        PowerMockito.when(ConfigurationFactory.getConfiguration()).thenReturn(configuration);
        client = Mockito.mock(CloseableHttpClient.class);
        PowerMockito.mockStatic(HttpClients.class);
        HttpClientBuilder httpClientBuilder = Mockito.mock(HttpClientBuilder.class);
        PowerMockito.when(HttpClients.custom()).thenReturn(httpClientBuilder);
        PowerMockito.when(httpClientBuilder.build()).thenReturn(client);
        httpResponse = Mockito.mock(CloseableHttpResponse.class);
        statusLine = Mockito.mock(StatusLine.class);
        Mockito.when(statusLine.getStatusCode()).thenReturn(300);
        Mockito.when(httpResponse.getStatusLine()).thenReturn(statusLine);
        httpEntity = Mockito.mock(HttpEntity.class);
        Mockito.when(httpResponse.getEntity()).thenReturn(httpEntity);
        Mockito.when(client.execute(Mockito.any())).thenReturn(httpResponse);
    }

    @Test
    public void testPostMethod() throws APPCException {
        assertEquals(300, httpClient.postMethod("http", "127.0.0.1", 22, "/path", "{}", "application/json"));
    }

    @Test
    public void testPutMethod() throws APPCException {
        assertEquals(300, httpClient.putMethod("http", "127.0.0.1", 22, "/path", "{}", "application/json"));
    }

    @Test
    public void testGetMethod() throws APPCException {
        assertNull(httpClient.getMethod("http", "127.0.0.1", 22, "/path", "application/json"));
    }

    @Test
    public void testDeleteMethod() throws APPCException {
        assertEquals(300, httpClient.deleteMethod("http", "127.0.0.1", 22, "/path", "application/json"));
    }
}
