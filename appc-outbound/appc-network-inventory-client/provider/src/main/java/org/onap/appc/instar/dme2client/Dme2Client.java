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

package org.onap.appc.instar.dme2client;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
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
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Properties;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import org.apache.commons.io.IOUtils;
import org.onap.appc.instar.utils.InstarClientConstant;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

public class Dme2Client {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(Dme2Client.class);
    private static final String SDNC_CONFIG_DIR_VAR = "SDNC_CONFIG_DIR";
    private Properties properties = new Properties();
    private String operationName;
    private String appendContext;
    private String mask;
    private String ipAddress;

    public Dme2Client(String optName, String subCtxt, Map<String, String> data) throws IOException {
        log.info("Setting Properties for DME2 Client for INSTAR connection");
        this.operationName = optName;
        this.appendContext = data.get(subCtxt);
        if ("getVnfbyIpadress".equals(optName)) {
            this.ipAddress = data.get("ipAddress");
            this.mask = data.get("mask");
        }
        String propDir = System.getenv(SDNC_CONFIG_DIR_VAR);
        if (propDir == null) {
            throw new IOException("Cannot find Property file -" + SDNC_CONFIG_DIR_VAR);
        }
        String propFile = propDir + InstarClientConstant.OUTBOUND_PROPERTIES;
        InputStream propStream = new FileInputStream(propFile);
        try {
            properties.load(propStream);
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

    private ClientResponse sendToInstar() throws SvcLogicException {

        log.info("Called Send with operation Name=" + this.operationName + "and = " +
            properties.getProperty(operationName + InstarClientConstant.BASE_URL));

        String resourceUri = buildResourceUri();

        log.info("DME Endpoint URI:" + resourceUri);

        Client client = null;
        WebResource webResource;
        ClientResponse clientResponse = null;
        String authorization = properties.getProperty("authorization");
        String requestDataType = "application/json";
        String responseDataType = MediaType.APPLICATION_JSON;
        String methodType = properties.getProperty("getIpAddressByVnf_method");
        String request = "";
        String userId = properties.getProperty("MechID");
        String password = properties.getProperty("MechPass");

        log.info("authorization = " + authorization + "methodType= " + methodType);

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
            client.addFilter(new HTTPBasicAuthFilter(userId, password));

            webResource = client.resource(new URI(resourceUri));
            webResource.setProperty("Content-Type", "application/json;charset=UTF-8");

            if (HttpMethod.GET.equalsIgnoreCase(methodType)) {
                clientResponse = webResource.accept(responseDataType).get(ClientResponse.class);
            } else if (HttpMethod.POST.equalsIgnoreCase(methodType)) {
                clientResponse = webResource.type(requestDataType).post(ClientResponse.class, request);
            } else if (HttpMethod.PUT.equalsIgnoreCase(methodType)) {
                clientResponse = webResource.type(requestDataType).put(ClientResponse.class, request);
            } else if (HttpMethod.DELETE.equalsIgnoreCase(methodType)) {
                clientResponse = webResource.delete(ClientResponse.class);
            }
            return clientResponse;

        } catch (Exception e) {
            log.info(
                "failed in RESTCONT Action (" + methodType + ") for the resource " + resourceUri + ", falut message :"
                    + e.getMessage());
            throw new SvcLogicException("Error While gettting Data from INSTAR", e);

        } finally {
            // clean up.
            if (client != null) {
                client.destroy();
            }
        }
    }

    private String buildResourceUri() {
        String resourceUri = properties.getProperty(operationName + InstarClientConstant.BASE_URL) +
            properties.getProperty(operationName + InstarClientConstant.URL_SUFFIX);

        if (ipAddress != null && mask == null) {
            resourceUri = resourceUri
                + properties.getProperty(operationName + InstarClientConstant.SUB_CONTEXT_BYIPADDRESS) + ipAddress;
        } else if (mask != null) {
            resourceUri = resourceUri
                + properties.getProperty(operationName + InstarClientConstant.SUB_CONTEXT_BYIPADDRESS)
                + ipAddress + properties.getProperty(operationName + InstarClientConstant.SUB_CONTEXT_BYMASK) + mask;
        } else {
            resourceUri = resourceUri
                + properties.getProperty(operationName + InstarClientConstant.SUB_CONTEXT) + appendContext;
        }
        return resourceUri;
    }

    public String send() {
        String response = null;
        try {
            if (validateProperties()) {
                return IOUtils.toString(Dme2Client.class.getClassLoader().getResourceAsStream("/tmp/sampleResponse"),
                    Charset.defaultCharset());
            }
            ClientResponse clientResponse = sendToInstar();
            if (clientResponse != null) {
                response = clientResponse.getEntity(String.class);
                log.info(clientResponse.getStatus() + " Status, Response :" + response);
            }
        } catch (Exception e) {
            log.error("Failed to send response", e);
        }
        return response;
    }

    private boolean validateProperties() {
        return properties != null
            && properties.getProperty(InstarClientConstant.MOCK_INSTAR) != null
            && "true".equalsIgnoreCase(properties.getProperty(InstarClientConstant.MOCK_INSTAR));
    }

    private HostnameVerifier getHostnameVerifier() {
        return (hostname, sslSession) -> true;
    }
}
