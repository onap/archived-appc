/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
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
package org.onap.appc.adapter.restHealthcheck.impl;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.onap.appc.exceptions.APPCException;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.att.cdp.exceptions.ZoneException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;



@RunWith(PowerMockRunner.class)
@PrepareForTest({HttpClients.class, EntityUtils.class})
public class TestRestHealthcheckAdapterImpl {

    private RestHealthcheckAdapterImpl adapter;
    private CloseableHttpClient client;
    
    @Before
    public void setup() throws IllegalArgumentException, IllegalAccessException, ParseException, IOException {
        PowerMockito.mockStatic(HttpClients.class);
        PowerMockito.mockStatic(EntityUtils.class);
        client = Mockito.mock(CloseableHttpClient.class);
        PowerMockito.when(HttpClients.createDefault()).thenReturn(client);
        PowerMockito.when(EntityUtils.toString(Mockito.any(HttpEntity.class))).thenReturn("TEST");
        adapter = new RestHealthcheckAdapterImpl();
    }

    @Test
    public void testCheckHealth() throws IOException, IllegalStateException, IllegalArgumentException,
        ZoneException, APPCException {
            Map<String, String> params = new HashMap<>();
            params.put("VNF.URI", "http://restHalthCheck.test");
            params.put("VNF.endpoint", "health");
            HttpResponse response = Mockito.mock(CloseableHttpResponse.class);
            HttpEntity entity = Mockito.mock(HttpEntity.class);
            Mockito.doReturn(entity).when(response).getEntity();
            StatusLine statusLine = Mockito.mock(StatusLine.class);
            Mockito.doReturn(200).when(statusLine).getStatusCode();
            Mockito.doReturn(statusLine).when(response).getStatusLine();
            Mockito.doReturn(response).when(client).execute(Mockito.any(HttpGet.class));
            SvcLogicContext svcContext = new SvcLogicContext();
            adapter.checkHealth(params, svcContext);
            String statusCode = svcContext.getAttribute("healthcheck.result.code");
            assertEquals("400", statusCode);
    }

    @Test
    public void testCheckHealthFailure() throws IOException, IllegalStateException, IllegalArgumentException,
        ZoneException, APPCException {
            Map<String, String> params = new HashMap<>();
            params.put("VNF.URI", "http://restHalthCheck.test");
            params.put("VNF.endpoint", "health");
            HttpResponse response = Mockito.mock(CloseableHttpResponse.class);
            HttpEntity entity = Mockito.mock(HttpEntity.class);
            Mockito.doReturn(entity).when(response).getEntity();
            StatusLine statusLine = Mockito.mock(StatusLine.class);
            Mockito.doReturn(400).when(statusLine).getStatusCode();
            Mockito.doReturn(statusLine).when(response).getStatusLine();
            Mockito.doReturn(response).when(client).execute(Mockito.any(HttpGet.class));
            SvcLogicContext svcContext = new SvcLogicContext();
            adapter.checkHealth(params, svcContext);
            String statusCode = svcContext.getAttribute("healthcheck.result.code");
            assertEquals("200", statusCode);
    }

    @Test
    public void testCheckHealthException() throws IOException, IllegalStateException, IllegalArgumentException,
        ZoneException, APPCException {
            Map<String, String> params = new HashMap<>();
            params.put("VNF.URI", "http://restHalthCheck.test");
            params.put("VNF.endpoint", "health");
            HttpResponse response = Mockito.mock(CloseableHttpResponse.class);
            HttpEntity entity = Mockito.mock(HttpEntity.class);
            Mockito.doReturn(entity).when(response).getEntity();
            StatusLine statusLine = Mockito.mock(StatusLine.class);
            Mockito.doReturn(400).when(statusLine).getStatusCode();
            Mockito.doReturn(statusLine).when(response).getStatusLine();
            Mockito.doThrow(new IOException()).when(client).execute(Mockito.any(HttpGet.class));
            SvcLogicContext svcContext = new SvcLogicContext();
            adapter.checkHealth(params, svcContext);
            String statusCode = svcContext.getAttribute("healthcheck.result.code");
            assertEquals(RestHealthcheckAdapterImpl.OUTCOME_FAILURE, svcContext.getStatus());
    }



}
