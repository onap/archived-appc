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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import org.onap.appc.exceptions.APPCException;
import com.att.cdp.exceptions.ZoneException;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;


/**
 * Test the ProviderAdapter implementation.
 */
public class TestRestAdapterImpl {
    private RestAdapterImpl adapter;


    @SuppressWarnings("nls")
    @BeforeClass
    public static void once() throws NoSuchFieldException, SecurityException, NoSuchMethodException {

    }

    @Before
    public void setup() throws IllegalArgumentException, IllegalAccessException {

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
    public void testCreateRequestNoParamGet() throws IOException, IllegalStateException, IllegalArgumentException,
            ZoneException, APPCException {

        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> params = new HashMap<>();

        adapter.commonGet(params, ctx);

        assertEquals("failure", ctx.getStatus());
        assertEquals("500", ctx.getAttribute("org.openecomp.rest.result.code"));
        assertEquals("java.lang.IllegalArgumentException: HTTP request may not be null",
                     ctx.getAttribute("org.openecomp.rest.result.message"));
    }

    @Test
    public void testCreateRequestInvalidParamGet() throws IOException, IllegalStateException, IllegalArgumentException,
            ZoneException, APPCException {

        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> params = new HashMap<>();
        params.put("org.onap.appc.instance.URI", "boo");
        params.put("org.onap.appc.instance.haveHeader","false");
        params.put("org.onap.appc.instance.requestBody", "{\"name\":\"MyNode2\", \"width\":300, \"height\":300}");

        adapter.commonGet(params, ctx);

        assertEquals("failure", ctx.getStatus());
        assertEquals("500", ctx.getAttribute("org.openecomp.rest.result.code"));
        assertEquals("org.apache.http.client.ClientProtocolException",
                     ctx.getAttribute("org.openecomp.rest.result.message"));
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
    public void testCreateRequestNoParamPost() throws IOException, IllegalStateException, IllegalArgumentException,
            ZoneException, APPCException {

        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> params = new HashMap<>();

        adapter.commonPost(params, ctx);

        assertEquals("failure", ctx.getStatus());
        assertEquals("500", ctx.getAttribute("org.openecomp.rest.result.code"));
        assertEquals("java.lang.IllegalArgumentException: HTTP request may not be null",
                     ctx.getAttribute("org.openecomp.rest.result.message"));
    }

    @Test
    public void testCreateRequestInvalidParamPost() throws IOException, IllegalStateException, IllegalArgumentException,
            ZoneException, APPCException {

        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> params = new HashMap<>();
        params.put("org.onap.appc.instance.URI", "boo");
        params.put("org.onap.appc.instance.haveHeader","false");
        params.put("org.onap.appc.instance.requestBody", "{\"name\":\"MyNode2\", \"width\":300, \"height\":300}");

        adapter.commonPost(params, ctx);

        assertEquals("failure", ctx.getStatus());
        assertEquals("500", ctx.getAttribute("org.openecomp.rest.result.code"));
        assertEquals("org.apache.http.client.ClientProtocolException",
                     ctx.getAttribute("org.openecomp.rest.result.message"));
    }
    
    @Test
    public void testCreateHttpRequestPut() throws IOException, IllegalStateException, IllegalArgumentException,
        ZoneException, APPCException {    

        Map<String, String> params = new HashMap<>();
        params.put("org.onap.appc.instance.URI", "http://example.com:8081/puttest");
        params.put("org.onap.appc.instance.haveHeader","false");
        params.put("org.onap.appc.instance.requestBody", "{\"name\":\"MyNode2\", \"width\":300, \"height\":300}");

        HttpPut httpPut = ((HttpPut) givenParams(params, "PUT"));
        //Header headers[] = httpPut.getAllHeaders();

        assertEquals("PUT", httpPut.getMethod());
        assertEquals("http://example.com:8081/puttest", httpPut.getURI().toURL().toString());
        assertEquals("{\"name\":\"MyNode2\", \"width\":300, \"height\":300}", EntityUtils.toString(httpPut.getEntity()));
    }

    @Test
    public void testCreateRequestNoParamPut() throws IOException, IllegalStateException, IllegalArgumentException,
            ZoneException, APPCException {

        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> params = new HashMap<>();

        adapter.commonPut(params, ctx);

        assertEquals("failure", ctx.getStatus());
        assertEquals("500", ctx.getAttribute("org.openecomp.rest.result.code"));
        assertEquals("java.lang.IllegalArgumentException: HTTP request may not be null",
                     ctx.getAttribute("org.openecomp.rest.result.message"));
    }

    @Test
    public void testCreateRequestInvalidParamPut() throws IOException, IllegalStateException, IllegalArgumentException,
            ZoneException, APPCException {

        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> params = new HashMap<>();
        params.put("org.onap.appc.instance.URI", "boo");
        params.put("org.onap.appc.instance.haveHeader","false");
        params.put("org.onap.appc.instance.requestBody", "{\"name\":\"MyNode2\", \"width\":300, \"height\":300}");

        adapter.commonPut(params, ctx);

        assertEquals("failure", ctx.getStatus());
        assertEquals("500", ctx.getAttribute("org.openecomp.rest.result.code"));
        assertEquals("org.apache.http.client.ClientProtocolException",
                     ctx.getAttribute("org.openecomp.rest.result.message"));
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

        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> params = new HashMap<>();

        adapter.commonDelete(params, ctx);

        assertEquals("failure", ctx.getStatus());
        assertEquals("500", ctx.getAttribute("org.openecomp.rest.result.code"));
        assertEquals("java.lang.IllegalArgumentException: HTTP request may not be null",
                     ctx.getAttribute("org.openecomp.rest.result.message"));
    }

    @Test
    public void testCreateRequestInvalidParamDelete() throws IOException, IllegalStateException, IllegalArgumentException,
            ZoneException, APPCException {

        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> params = new HashMap<>();
        params.put("org.onap.appc.instance.URI", "boo");
        params.put("org.onap.appc.instance.haveHeader","false");
        params.put("org.onap.appc.instance.requestBody", "{\"name\":\"MyNode2\", \"width\":300, \"height\":300}");

        adapter.commonDelete(params, ctx);

        assertEquals("failure", ctx.getStatus());
        assertEquals("500", ctx.getAttribute("org.openecomp.rest.result.code"));
        assertEquals("org.apache.http.client.ClientProtocolException",
                     ctx.getAttribute("org.openecomp.rest.result.message"));
    }
    
    private HttpRequestBase givenParams(Map<String, String> params, String method){
        SvcLogicContext ctx = new SvcLogicContext();
        RequestContext rc = new RequestContext(ctx);
        rc.isAlive();

        adapter = new RestAdapterImpl();
        return adapter.createHttpRequest(method, params, rc);
    }


}
