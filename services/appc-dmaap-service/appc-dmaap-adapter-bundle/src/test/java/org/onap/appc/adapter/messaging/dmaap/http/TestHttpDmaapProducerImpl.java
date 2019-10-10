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
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class TestHttpDmaapProducerImpl {

    private static final Collection<String> URLS = Arrays.asList("test.com", "test.org");
    private static final String TOPIC_NAME = "Topic";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String PARTITION = "partition";
    private static final String DATA = "data";


    @Spy
    private HttpDmaapProducerImpl httpDmaapProducer = new HttpDmaapProducerImpl();

    @Mock
    private CloseableHttpClient httpClient;

    @Mock
    private CloseableHttpResponse httpResponse;

    @Mock
    private StatusLine statusLine;

    @Before
    public void setUp() throws Exception {
        httpDmaapProducer = new HttpDmaapProducerImpl(URLS, TOPIC_NAME);
        httpDmaapProducer.updateCredentials(USERNAME, PASSWORD);

        MockitoAnnotations.initMocks(this);
        doReturn(httpClient).when(httpDmaapProducer).getClient();
        when(httpClient.execute(any(HttpPost.class))).thenReturn(httpResponse);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
    }

    @Test
    public void shouldPostHttpRequest() throws Exception {

        when(statusLine.getStatusCode()).thenReturn(OK.getStatusCode());

        boolean successful = httpDmaapProducer.post(PARTITION, DATA);

        assertTrue(successful);
        verify(httpClient).execute(any(HttpPost.class));
        verify(httpResponse).getStatusLine();
        verify(httpResponse).close();
        verify(statusLine).getStatusCode();
        verifyNoMoreInteractions(httpClient, httpResponse, statusLine);
    }

    @Test
    public void shouldNotBeSuccessful_whenHttpResponseIsOtherThanOk() throws Exception {

        when(statusLine.getStatusCode()).thenReturn(FORBIDDEN.getStatusCode());

        boolean successful = httpDmaapProducer.post(PARTITION, DATA);

        assertFalse(successful);
        verify(httpClient).execute(any(HttpPost.class));
        verify(httpResponse).getStatusLine();
        verify(httpResponse).close();
        verify(statusLine).getStatusCode();
        verifyNoMoreInteractions(httpClient, httpResponse, statusLine);
    }

    @Test
    public void shouldNotBeSuccessful_whenRequestToOneOfUrlsCannotBeSent() throws Exception {

        when(httpClient.execute(any(HttpPost.class))).thenThrow(new ClientProtocolException());

        boolean successful = httpDmaapProducer.post(PARTITION, DATA);

        assertFalse(successful);
        verify(httpClient).execute(any(HttpPost.class));
        verifyNoMoreInteractions(httpClient, httpResponse, statusLine);


        reset(httpClient);
        when(httpClient.execute(any(HttpPost.class))).thenReturn(httpResponse);
        when(statusLine.getStatusCode()).thenReturn(OK.getStatusCode());

        successful = httpDmaapProducer.post(PARTITION, DATA);

        assertTrue(successful);
        verify(httpClient).execute(any(HttpPost.class));
        verify(httpResponse).getStatusLine();
        verify(httpResponse).close();
        verify(statusLine).getStatusCode();
        verifyNoMoreInteractions(httpClient, httpResponse, statusLine);
    }
}