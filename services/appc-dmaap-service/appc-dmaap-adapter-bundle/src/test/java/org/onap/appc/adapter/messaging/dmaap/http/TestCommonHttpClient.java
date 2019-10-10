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

package org.onap.appc.adapter.messaging.dmaap.http;

import static org.junit.Assert.*;

import java.net.URI;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.junit.Before;
import org.junit.Test;

public class TestCommonHttpClient {

    private static final String HTTP = "http://";
    private static final String HTTPS = "https://";
    private static final String URL = "example.org/location";
    private static final URI URI = java.net.URI.create(HTTP + URL);
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final int TIMEOUT = 15000;
    private static final int TIMEOUT_OFFSET = 5000;
    private static final int HTTP_PORT = 3904;
    private static final int HTTPS_PORT = 3905;

    private CommonHttpClient commonHttpClient;

    @Before
    public void setUp() {
        commonHttpClient = new CommonHttpClient() {};
    }

    private void setBasicAuth() {
        commonHttpClient.setBasicAuth(USERNAME, PASSWORD);
    }

    private void noBasicAuth() {
        commonHttpClient.setBasicAuth(null, null);
    }

    @Test
    public void shouldGetHttpRequest_whenSetBasicAuth() throws AuthenticationException {

        setBasicAuth();

        HttpGet httpGet = commonHttpClient.getReq(URI, TIMEOUT);

        assertNotNull(httpGet);
        assertNotNull(httpGet.getFirstHeader("Authorization"));
        assertNotNull(httpGet.getConfig());
        assertEquals(httpGet.getConfig().getSocketTimeout(), TIMEOUT + TIMEOUT_OFFSET);
    }

    @Test
    public void shouldPostHttpRequest_whenSetBasicAuth() throws AuthenticationException {

        setBasicAuth();

        HttpPost httpPost = commonHttpClient.postReq(URL);

        assertNotNull(httpPost);
        assertNotNull(httpPost.getFirstHeader("Authorization"));
        assertNotNull(httpPost.getConfig());
        assertEquals(httpPost.getConfig().getSocketTimeout(), TIMEOUT_OFFSET);
    }

    @Test
    public void shouldGetClient() {
        assertNotNull(commonHttpClient.getClient());
    }

    @Test
    public void shouldFormatHostString() {
        String httpUrl = HTTP + URL + ":" + HTTP_PORT;
        String httpsUrl = HTTPS + URL + ":" + HTTPS_PORT;
        String outputUrl;

        outputUrl = commonHttpClient.formatHostString(httpUrl);
        assertTrue(assertMessage(httpUrl, outputUrl), httpUrl.equals(outputUrl));

        outputUrl = commonHttpClient.formatHostString(httpsUrl);
        assertTrue(assertMessage(httpsUrl, outputUrl), httpsUrl.equals(outputUrl));

        outputUrl = commonHttpClient.formatHostString(httpsUrl + "/");
        assertTrue(assertMessage(httpsUrl, outputUrl), httpsUrl.equals(outputUrl));

        outputUrl = commonHttpClient.formatHostString(URL + ":" + HTTP_PORT);
        assertTrue(assertMessage(httpUrl, outputUrl), httpUrl.equals(outputUrl));

        outputUrl = commonHttpClient.formatHostString(URL + ":" + HTTPS_PORT);
        assertTrue(assertMessage(httpsUrl, outputUrl), httpsUrl.equals(outputUrl));

        outputUrl = commonHttpClient.formatHostString(URL);
        assertTrue(assertMessage(httpUrl, outputUrl), httpUrl.equals(outputUrl));
    }

    private String assertMessage(String expected, String actual) {
        return "Expected: " + expected + " Actual: " + actual;
    }
}