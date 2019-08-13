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

package org.onap.appc.design.services.util;

import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import javax.net.ssl.SSLContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.ClientBuilder;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DesignServiceConstants.class, SSLContext.class, Client.class,ClientBuilder.class,HttpAuthenticationFeature.class})
public class ArtifactHandlerClientTest {
    private SSLContext sslContext = PowerMockito.mock(SSLContext.class);
    private Client client;
    private WebTarget webResource;
    private Invocation.Builder builder;
    private ClientBuilder clientBuilder;


    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void setup() throws URISyntaxException {
        PowerMockito.mockStatic(DesignServiceConstants.class);
        PowerMockito.mockStatic(SSLContext.class);
        PowerMockito.mockStatic(Client.class);
        PowerMockito.mockStatic(ClientBuilder.class);
        PowerMockito.mockStatic(HttpAuthenticationFeature.class);

        sslContext = PowerMockito.mock(SSLContext.class);
        client = Mockito.mock(Client.class);
        builder = PowerMockito.mock(Invocation.Builder.class);
        clientBuilder = Mockito.mock(ClientBuilder.class);
        webResource = PowerMockito.mock(WebTarget.class);

        PowerMockito.when(client.target(Mockito.any(URI.class))).thenReturn(webResource);
        PowerMockito.when(ClientBuilder.newBuilder()).thenReturn(clientBuilder);
        doReturn(clientBuilder).when(clientBuilder).sslContext(any());
        doReturn(clientBuilder).when(clientBuilder).hostnameVerifier(any());
        PowerMockito.when(clientBuilder.build()).thenReturn(client);


    }

    @Test
    public void testConstructorException() throws IOException {
        expectedEx.expect(IOException.class);
        new ArtifactHandlerClient();
    }

    @Test
    public void testConstructor() throws IOException {
        PowerMockito.when(DesignServiceConstants.getEnvironmentVariable(ArtifactHandlerClient.SDNC_CONFIG_DIR_VAR))
            .thenReturn("src/test/resources");
        ArtifactHandlerClient ahClient = new ArtifactHandlerClient();
        assertTrue(ahClient instanceof ArtifactHandlerClient);
        Properties props = Whitebox.getInternalState(ahClient, "props");
        assertTrue(props.containsKey("appc.upload.provider.url"));
    }

    @Test
    public void testCreateArtifactData() throws IOException {
        PowerMockito.when(DesignServiceConstants.getEnvironmentVariable(ArtifactHandlerClient.SDNC_CONFIG_DIR_VAR))
            .thenReturn("src/test/resources");
        ArtifactHandlerClient ahClient = new ArtifactHandlerClient();
        assertEquals("{\"input\": {\"request-information\":{\"request-id\":\"\",\"request-action\":"
                + "\"StoreSdcDocumentRequest\",\"source\":\"Design-tool\"},\"document-parameters\":"
                + "{\"artifact-version\":\"TEST\",\"artifact-name\":\"TEST\",\"artifact-contents\":"
                + "\"TEST\"}}}", ahClient.createArtifactData("{\"" + DesignServiceConstants.ARTIFACT_NAME
                + "\":\"TEST\", \"" + DesignServiceConstants.ARTIFACT_VERSOIN + "\":\"TEST\", \"" +
                DesignServiceConstants.ARTIFACT_CONTENTS + "\":\"TEST\"}", ""));
    }

    @Test
    public void testExecuteArtifactHandlerInternalException() throws IOException, NoSuchAlgorithmException {
        PowerMockito.when(SSLContext.getInstance("SSL")).thenReturn(sslContext);
        PowerMockito.when(DesignServiceConstants.getEnvironmentVariable(ArtifactHandlerClient.SDNC_CONFIG_DIR_VAR))
            .thenReturn("src/test/resources");
        builder = Mockito.mock(Invocation.Builder.class);
        PowerMockito.when(webResource.request("application/json")).thenReturn(builder);
        ArtifactHandlerClient ahClient = new ArtifactHandlerClient();
        Properties properties = new Properties();
        properties.put("appc.upload.user", "TEST_USER");
        properties.put("appc.upload.provider.url", "http://127.0.0.1:8080/path");
        properties.put("appc.upload.pass", "enc:password");
        Whitebox.setInternalState(ahClient, "props", properties);
        expectedEx.expectCause(isA(ArtifactHandlerInternalException.class));
        ahClient.execute("", "GET");
    }

    @Test
    public void testExecuteIOException() throws IOException, NoSuchAlgorithmException {
        PowerMockito.when(SSLContext.getInstance("SSL")).thenThrow(new RuntimeException());
        PowerMockito.when(DesignServiceConstants.getEnvironmentVariable(ArtifactHandlerClient.SDNC_CONFIG_DIR_VAR))
            .thenReturn("src/test/resources");
        ArtifactHandlerClient ahClient = new ArtifactHandlerClient();
        expectedEx.expect(IOException.class);
        ahClient.execute("", "");
    }
}
