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

import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class TestHttpDmaapConsumerImpl {

    private static final Collection<String> URLS = Arrays.asList("test.com", "test.org");
    private static final Collection<String> OUTPUT_MSG = Arrays.asList("FirstMessage", "SecondMessage");
    private static final String TOPIC_NAME = "Topic";
    private static final String CONSUMER_NAME = "Consumer";
    private static final String CONSUMER_ID = "Id";
    private static final String FILTER = "filter";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String MESSAGE_BODY = "[FirstMessage, SecondMessage]";
    private static final int TIMEOUT_MS = 1000;
    private static final int LIMIT = 1000;

    @Spy
    private HttpDmaapConsumerImpl httpDmaapConsumer;

    @Mock
    private CloseableHttpClient httpClient;

    @Mock
    private CloseableHttpResponse httpResponse;

    @Mock
    private StatusLine statusLine;

    @Mock
    private HttpEntity entity;

    @Before
    public void setUp() throws Exception {
        httpDmaapConsumer = new HttpDmaapConsumerImpl(URLS, TOPIC_NAME, CONSUMER_NAME, CONSUMER_ID, FILTER);
        httpDmaapConsumer.updateCredentials(USERNAME, PASSWORD);

        MockitoAnnotations.initMocks(this);
        doReturn(httpClient).when(httpDmaapConsumer).getClient();
        when(httpClient.execute(any(HttpGet.class))).thenReturn(httpResponse);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(httpResponse.getEntity()).thenReturn(entity);
        doReturn(MESSAGE_BODY).when(httpDmaapConsumer).entityToString(same(entity));
    }

    @Test
    public void shouldGetHttpRequest() throws Exception {

        when(statusLine.getStatusCode()).thenReturn(OK.getStatusCode());

        List<String> output =  httpDmaapConsumer.fetch();

        assertFalse(output.isEmpty());
        assertTrue(output.containsAll(OUTPUT_MSG));
        verify(httpClient).execute(any(HttpGet.class));
        verify(httpResponse).getStatusLine();
        verify(httpResponse).getEntity();
        verify(httpResponse).close();
        verify(statusLine).getStatusCode();
        verifyNoMoreInteractions(httpClient, httpResponse, statusLine, entity);
    }

    @Test
    public void shouldNotBeSuccessful_whenHttpResponseIsOtherThanOk() throws Exception {

        when(statusLine.getStatusCode()).thenReturn(FORBIDDEN.getStatusCode());

        List<String> output =  httpDmaapConsumer.fetch(TIMEOUT_MS, LIMIT);

        assertTrue(output.isEmpty());
        verify(httpClient).execute(any(HttpGet.class));
        verify(httpResponse).getStatusLine();
        verify(httpResponse).getEntity();
        verify(httpResponse).close();
        verify(statusLine).getStatusCode();
        verifyNoMoreInteractions(httpClient, httpResponse, statusLine, entity);
    }

    @Test
    public void shouldNotBeSuccessful_whenRequestToOneOfUrlsCannotBeSent() throws Exception {

        when(httpClient.execute(any(HttpGet.class))).thenThrow(new ClientProtocolException());

        List<String> output =  httpDmaapConsumer.fetch(TIMEOUT_MS, LIMIT);

        assertTrue(output.isEmpty());
        verify(httpClient).execute(any(HttpGet.class));
        verifyNoMoreInteractions(httpClient, httpResponse, statusLine, entity);


        reset(httpClient);
        when(httpClient.execute(any(HttpGet.class))).thenReturn(httpResponse);
        when(statusLine.getStatusCode()).thenReturn(OK.getStatusCode());

        output =  httpDmaapConsumer.fetch(TIMEOUT_MS, LIMIT);

        assertFalse(output.isEmpty());
        assertTrue(output.containsAll(OUTPUT_MSG));
        verify(httpClient).execute(any(HttpGet.class));
        verify(httpResponse).getStatusLine();
        verify(httpResponse).getEntity();
        verify(httpResponse).close();
        verify(statusLine).getStatusCode();
        verifyNoMoreInteractions(httpClient, httpResponse, statusLine, entity);
    }

}