/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.design.services.util;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;


public class ArtifactHandlerClient {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(ArtifactHandlerClient.class);
    private static final String SDNC_CONFIG_DIR_VAR = "SDNC_CONFIG_DIR";
    private Properties props = new Properties();

    public ArtifactHandlerClient() throws IOException {
        String propDir = System.getenv(SDNC_CONFIG_DIR_VAR);
        if (propDir == null) {
            throw new IOException(" Cannot find Property file -" + SDNC_CONFIG_DIR_VAR);
        }
        String propFile = propDir + "/" + DesignServiceConstants.DESIGN_SERVICE_PROPERTIES;
        InputStream propStream = new FileInputStream(propFile);
        try {
            props.load(propStream);
        } catch (Exception e) {
            throw new IOException("Could not load properties file " + propFile, e);
        } finally {
            try {
                propStream.close();
            } catch (Exception e) {
                log.warn("Could not close FileInputStream", e);
            }
        }
    }

    public String createArtifactData(String payload, String requestID) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode payloadObject = objectMapper.readTree(payload);

        ObjectNode json = objectMapper.createObjectNode();
        String artifactName = payloadObject.get(DesignServiceConstants.ARTIFACT_NAME).textValue();
        String artifactVersion = payloadObject.get(DesignServiceConstants.ARTIFACT_VERSOIN).textValue();
        String artifactContents = payloadObject.get(DesignServiceConstants.ARTIFACT_CONTENTS).textValue();

        ObjectNode requestInfo = objectMapper.createObjectNode();
        requestInfo.put(DesignServiceConstants.REQUETS_ID, requestID);
        requestInfo.put(DesignServiceConstants.REQUEST_ACTION, "StoreSdcDocumentRequest");
        requestInfo.put(DesignServiceConstants.SOURCE, DesignServiceConstants.DESIGN_TOOL);

        ObjectNode docParams = objectMapper.createObjectNode();
        docParams.put(DesignServiceConstants.ARTIFACT_VERSOIN, artifactVersion);
        docParams.put(DesignServiceConstants.ARTIFACT_NAME, artifactName);
        docParams.put(DesignServiceConstants.ARTIFACT_CONTENTS, artifactContents);

        json.put(DesignServiceConstants.REQUEST_INFORMATION, requestInfo);
        json.put(DesignServiceConstants.DOCUMENT_PARAMETERS, docParams);
        log.info("Final data =" + json.toString());
        return String.format("{\"input\": %s}", json.toString());
    }

    public Map<String, String> execute(String payload, String rpc) throws IOException {
        log.info("Configuring Rest Operation for Payload " + payload + " RPC : " + rpc);
        Map<String, String> outputMessage = new HashMap<>();
        Client client = null;
        WebResource webResource;
        ClientResponse clientResponse = null;
        EncryptionTool et = EncryptionTool.getInstance();
        String responseDataType = MediaType.APPLICATION_JSON;
        String requestDataType = MediaType.APPLICATION_JSON;

        try {
            DefaultClientConfig defaultClientConfig = new DefaultClientConfig();
            System.setProperty("jsse.enableSNIExtension", "false");
            SSLContext sslContext;
            SecureRestClientTrustManager secureRestClientTrustManager = new SecureRestClientTrustManager();
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new javax.net.ssl.TrustManager[]{secureRestClientTrustManager}, null);

            defaultClientConfig
                .getProperties()
                .put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, new HTTPSProperties(getHostnameVerifier(), sslContext));

            client = Client.create(defaultClientConfig);
            String password = et.decrypt(props.getProperty("appc.upload.pass"));
            client.addFilter(new HTTPBasicAuthFilter(props.getProperty("appc.upload.user"), password));
            webResource = client.resource(new URI(props.getProperty("appc.upload.provider.url")));
            webResource.setProperty("Content-Type", "application/json;charset=UTF-8");
            log.info("Starting Rest Operation.....");
            if (HttpMethod.GET.equalsIgnoreCase(rpc)) {
                clientResponse = webResource.accept(responseDataType).get(ClientResponse.class);
            } else if (HttpMethod.POST.equalsIgnoreCase(rpc)) {
                clientResponse = webResource.type(requestDataType).post(ClientResponse.class, payload);
            } else if (HttpMethod.PUT.equalsIgnoreCase(rpc)) {
                clientResponse = webResource.type(requestDataType).put(ClientResponse.class, payload);
            } else if (HttpMethod.DELETE.equalsIgnoreCase(rpc)) {
                clientResponse = webResource.delete(ClientResponse.class);
            }
            validateClientResponse(clientResponse);
            log.info("Completed Rest Operation.....");

        } catch (Exception e) {
            log.debug("failed in RESTCONT Action", e);
            throw new IOException("Error While Sending Rest Request" + e.getMessage());
        } finally {
            // clean up.
            if (client != null) {
                client.destroy();
            }
        }
        return outputMessage;
    }

    private void validateClientResponse(ClientResponse clientResponse) throws ArtifactHandlerInternalException {
        if (clientResponse == null) {
            throw new ArtifactHandlerInternalException("Failed to create client response");
        }
        if (clientResponse.getStatus() != 200) {
            throw new ArtifactHandlerInternalException("HTTP error code : " + clientResponse.getStatus());
        }
    }

    private HostnameVerifier getHostnameVerifier() {
        return (hostname, sslSession) -> true;
    }
}
