/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
 * Copyright (C) 2017 Intel Corp.
 * =============================================================================
 * Modifications Copyright (C) 2018 Samsung
 * ================================================================================
 * Modifications Copyright (C) 2019 Ericsson
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

package org.onap.appc.adapter.rest.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.onap.appc.exceptions.APPCException;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.att.cdp.exceptions.ZoneException;


/**
 * Test the ProviderAdapter implementation.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({HttpClients.class, SSLContexts.class})
public class TestRestAdapterImpl {
    private RestAdapterImpl adapter;
    private CloseableHttpClient client;
    private CloseableHttpResponse httpResponse;
    private HttpEntity httpEntity;
    private StatusLine statusLine;

    @SuppressWarnings("nls")
    @BeforeClass
    public static void once() throws NoSuchFieldException, SecurityException, NoSuchMethodException {

    }

    @Before
    public void setup() throws IllegalArgumentException, IllegalAccessException, ClientProtocolException, IOException {
        client = Mockito.mock(CloseableHttpClient.class);
        PowerMockito.mockStatic(HttpClients.class);
        PowerMockito.when(HttpClients.createDefault()).thenReturn(client);
        httpResponse = Mockito.mock(CloseableHttpResponse.class);
        statusLine = Mockito.mock(StatusLine.class);
        Mockito.when(statusLine.getStatusCode()).thenReturn(200);
        Mockito.when(httpResponse.getStatusLine()).thenReturn(statusLine);
        httpEntity = Mockito.mock(HttpEntity.class);
        Mockito.when(httpResponse.getEntity()).thenReturn(httpEntity);
        Mockito.when(client.execute(Mockito.any())).thenReturn(httpResponse);
        adapter = new RestAdapterImpl();
    }

    @Test
    public void testCreateHttpRequestGet() throws IOException, IllegalStateException, IllegalArgumentException,
        ZoneException, APPCException {

        Map<String, String> params = new HashMap<>();
        params.put("org.onap.appc.instance.URI", "http://example.com:8080/about/health");
        params.put("org.onap.appc.instance.haveHeader","false");

        HttpGet httpGet = ((HttpGet) givenParams(params, "GET"));

        assertEquals("GET", httpGet.getMethod());
        assertEquals("http://example.com:8080/about/health", httpGet.getURI().toURL().toString());
    }

    @Test
    public void testCreateHttpRequestPost() throws IOException, IllegalStateException, IllegalArgumentException,
        ZoneException, APPCException {

        Map<String, String> params = new HashMap<>();
        params.put("org.onap.appc.instance.URI", "http://example.com:8081/posttest");
        params.put("org.onap.appc.instance.haveHeader","false");
        params.put("org.onap.appc.instance.requestBody", "{\"name\":\"MyNode\", \"width\":200, \"height\":100}");

        HttpPost httpPost = ((HttpPost) givenParams(params, "POST"));

        assertEquals("POST", httpPost.getMethod());
        assertEquals("http://example.com:8081/posttest", httpPost.getURI().toURL().toString());
        assertEquals("{\"name\":\"MyNode\", \"width\":200, \"height\":100}", EntityUtils.toString(httpPost.getEntity()));
    }

    @Test
    public void testCreateRequestInvalidParamPost() throws IOException, IllegalStateException, IllegalArgumentException,
            ZoneException, APPCException {
        Mockito.when(statusLine.getStatusCode()).thenReturn(500);
        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> params = new HashMap<>();
        params.put("org.onap.appc.instance.URI", "boo");
        params.put("org.onap.appc.instance.haveHeader","false");
        params.put("org.onap.appc.instance.requestBody", "{\"name\":\"MyNode2\", \"width\":300, \"height\":300}");

        adapter.commonPost(params, ctx);

        assertEquals("failure", ctx.getStatus());
        assertEquals("500", ctx.getAttribute("org.onap.rest.result.code"));
        assertEquals("Internal Server Error",
                     ctx.getAttribute("org.onap.rest.result.message"));
    }

    @Test
    public void testCreateHttpRequestPut() throws IOException, IllegalStateException, IllegalArgumentException,
        ZoneException, APPCException {    

        Map<String, String> params = new HashMap<>();
        params.put("org.onap.appc.instance.URI", "http://example.com:8081/puttest");
        params.put("org.onap.appc.instance.haveHeader","false");
        params.put("org.onap.appc.instance.requestBody", "{\"name\":\"MyNode2\", \"width\":300, \"height\":300}");

        HttpPut httpPut = ((HttpPut) givenParams(params, "PUT"));

        assertEquals("PUT", httpPut.getMethod());
        assertEquals("http://example.com:8081/puttest", httpPut.getURI().toURL().toString());
        assertEquals("{\"name\":\"MyNode2\", \"width\":300, \"height\":300}", EntityUtils.toString(httpPut.getEntity()));
    }

    @Test
    public void testCreateRequestNoParamPut() throws IOException, IllegalStateException, IllegalArgumentException,
            ZoneException, APPCException {
        Mockito.when(statusLine.getStatusCode()).thenReturn(200);
        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> params = new HashMap<>();

        adapter.commonPut(params, ctx);

        assertEquals("success", ctx.getStatus());
        assertEquals("200", ctx.getAttribute("org.onap.rest.result.code"));
        assertEquals("java.lang.NullPointerException",
                     ctx.getAttribute("org.onap.rest.result.message"));
    }

    @Test
    public void testCreateHttpRequestDelete() throws IOException, IllegalStateException, IllegalArgumentException,
        ZoneException, APPCException {    

        Map<String, String> params = new HashMap<>();
        params.put("org.onap.appc.instance.URI", "http://example.com:8081/deletetest");
        params.put("org.onap.appc.instance.haveHeader","false");

        HttpDelete httpDelete = ((HttpDelete) givenParams(params, "DELETE"));

        assertEquals("DELETE", httpDelete.getMethod());
        assertEquals("http://example.com:8081/deletetest", httpDelete.getURI().toURL().toString());
    }

    @Test
    public void testCreateRequestNoParamDelete() throws IOException, IllegalStateException, IllegalArgumentException,
            ZoneException, APPCException {
        Mockito.when(statusLine.getStatusCode()).thenReturn(400);
        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> params = new HashMap<>();

        adapter.commonDelete(params, ctx);

        assertEquals("failure", ctx.getStatus());
        assertEquals("400", ctx.getAttribute("org.onap.rest.result.code"));
        assertEquals("Bad Request",
                     ctx.getAttribute("org.onap.rest.result.message"));
    }

    @Test
    public void testDoFailureMultiLineErrorMessage() {
        Map<String, String> mockParams = Mockito.mock(Map.class);
        Mockito.when(mockParams.get("org.onap.appc.instance.URI")).thenThrow(new RuntimeException("\n\n"));
        adapter.createHttpRequest("test_method", mockParams, new RequestContext(new SvcLogicContext()));
    }

    @Test
    public void testCreateHttpRequestWithHeader() throws MalformedURLException {
        Map<String, String> params = new HashMap<>();
        params.put("org.onap.appc.instance.URI", "http://example.com:8080/about/health");
        params.put("org.onap.appc.instance.headers", "{\"header1\":\"header1-value\"}");
        params.put("org.onap.appc.instance.haveHeader","true");

        HttpGet httpGet = ((HttpGet) givenParams(params, "GET"));

        assertEquals("GET", httpGet.getMethod());
        assertNotNull(httpGet.getHeaders("header1"));
    }

    @Test
    public void testExecuteHttpRequest() {
        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> params = new HashMap<>();
        adapter.commonGet(params, ctx);
    }

    @Test
    public void testExecuteRequestException() throws IOException, IllegalStateException, IllegalArgumentException,
            ZoneException, APPCException {
        Mockito.when(client.execute(Mockito.any())).thenThrow(new IOException());
        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> params = new HashMap<>();

        adapter.commonDelete(params, ctx);

        assertEquals("failure", ctx.getStatus());
        assertEquals("500", ctx.getAttribute("org.onap.rest.result.code"));
        assertEquals("java.io.IOException",
                     ctx.getAttribute("org.onap.rest.result.message"));
    }

    private HttpRequestBase givenParams(Map<String, String> params, String method){
        SvcLogicContext ctx = new SvcLogicContext();
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();

        adapter = new RestAdapterImpl();
        return adapter.createHttpRequest(method, params, rc);
    }
}
