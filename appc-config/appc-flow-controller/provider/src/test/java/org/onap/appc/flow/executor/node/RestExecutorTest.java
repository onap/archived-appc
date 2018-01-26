/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.flow.executor.node;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.OK;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.onap.appc.flow.controller.data.Transaction;
import org.onap.appc.flow.controller.executorImpl.RestExecutor;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.ws.rs.HttpMethod;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;


@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.net.ssl.*")
@PrepareForTest(Client.class)
public class RestExecutorTest {

    private static final String ANY = "notNullString";
    private static final String REST_RESPONSE = "restResponse";

    private RestExecutor restExecutor = new RestExecutor();
    private Transaction transaction;
    private Map<String, String> outputMessage = new HashMap<>();

    @Mock
    private Client client;
    @Mock
    private WebResource webResource;
    @Mock
    private WebResource.Builder webResourceBuilder;
    @Mock
    private ClientResponse clientResponse;


    @Before
    public void setUp() {
        transaction = new Transaction();
        transaction.setuId(ANY);
        transaction.setPswd(ANY);
        transaction.setExecutionEndPoint(ANY);
        transaction.setPayload(ANY);

        MockitoAnnotations.initMocks(this);
        mockStatic(Client.class);
        when(Client.create(any())).thenReturn(client);
        when(client.resource(any(URI.class))).thenReturn(webResource);

        when(webResource.accept(anyString())).thenReturn(webResourceBuilder);
        when(webResource.type(anyString())).thenReturn(webResourceBuilder);

        when(webResourceBuilder.get(ClientResponse.class)).thenReturn(clientResponse);
        when(webResourceBuilder.post(ClientResponse.class, ANY)).thenReturn(clientResponse);
        when(webResourceBuilder.put(ClientResponse.class, ANY)).thenReturn(clientResponse);
        when(webResource.delete(ClientResponse.class)).thenReturn(clientResponse);

        when(clientResponse.getStatus()).thenReturn(OK.getStatusCode());
        when(clientResponse.getEntity(String.class)).thenReturn(OK.getReasonPhrase());
    }

    @Test
    public void checkClientResponse_whenHTTPMethodIsGET() throws Exception {

        transaction.setExecutionRPC(HttpMethod.GET);

        outputMessage = restExecutor.execute(transaction, null);

        assertResponseOK();
    }

    @Test
    public void checkClientResponse_whenHTTPMethodIsPOST() throws Exception {

        transaction.setExecutionRPC(HttpMethod.POST);

        outputMessage = restExecutor.execute(transaction, null);

        assertResponseOK();
    }

    @Test
    public void checkClientResponse_whenHTTPMethodIsPUT() throws Exception {

        transaction.setExecutionRPC(HttpMethod.PUT);

        outputMessage = restExecutor.execute(transaction, null);

        assertResponseOK();
    }

    @Test
    public void checkClientResponse_whenHTTPMethodIsDELETE() throws Exception {

        transaction.setExecutionRPC(HttpMethod.DELETE);

        outputMessage = restExecutor.execute(transaction, null);

        assertResponseOK();
    }

    @Test(expected=Exception.class)
    public void checkClienResponse_whenStatusNOK() throws Exception {
        try {
            when(clientResponse.getStatus()).thenReturn(FORBIDDEN.getStatusCode());
            when(clientResponse.getEntity(String.class)).thenReturn(FORBIDDEN.getReasonPhrase());
            transaction.setExecutionRPC(HttpMethod.GET);

            outputMessage = restExecutor.execute(transaction, null);

        } catch(Exception e) {
            assertResponseNOK(e);
            throw e;
        }
    }

    @Test(expected=Exception.class)
    public void checkIfExceptionIsThrown_whenHTTPMethodIsNotSupported() throws Exception {
        try {
            transaction.setExecutionRPC(HttpMethod.HEAD);

            outputMessage = restExecutor.execute(transaction, null);

        } finally {
            assertNotSupportedHTTPMethod();
        }
    }

    private void assertResponseOK() {
        assertFalse("Output Message is empty", outputMessage.isEmpty());
        assertTrue("Output Message does not contain " + REST_RESPONSE, outputMessage.containsKey(REST_RESPONSE));
        assertTrue("restResponse is not " + OK.getReasonPhrase(),
                (OK.getReasonPhrase()).equals(outputMessage.get(REST_RESPONSE)));
        assertTrue("HTTP_Response in NOK", transaction.getResponses().stream()
                .anyMatch(response -> Integer.toString(OK.getStatusCode()).equals(response.getResponseCode())));
    }

    private void assertResponseNOK(Exception e) {
        assertTrue("Output Message is not empty as it should", outputMessage.isEmpty());
        assertTrue("Expected HTTP error code: " + FORBIDDEN.getStatusCode() + " is not present",
                e.getCause().getMessage().contains(Integer.toString(FORBIDDEN.getStatusCode())));
    }

    private void assertNotSupportedHTTPMethod() {
        assertTrue("Output Message is not empty as it should", outputMessage.isEmpty());
        assertTrue("HTTP Method: " + transaction.getExecutionRPC() + " is supported but was not handled",
                Stream.of(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)
                        .noneMatch(httpMethod -> httpMethod.equals(transaction.getExecutionRPC())));
    }
}
