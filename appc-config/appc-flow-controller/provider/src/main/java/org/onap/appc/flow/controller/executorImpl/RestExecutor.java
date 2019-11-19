/*
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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
 * * ============LICENSE_END=========================================================
 */
package org.onap.appc.flow.controller.executorImpl;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.client.ClientProperties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Feature;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.onap.appc.flow.controller.data.Response;
import org.onap.appc.flow.controller.data.Transaction;
import org.onap.appc.flow.controller.interfaces.FlowExecutorInterface;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;


public class RestExecutor implements FlowExecutorInterface {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(RestExecutor.class);

    @Override
    public Map<String, String> execute(Transaction transaction, SvcLogicContext ctx) throws Exception {
        String woPswd = transaction.toString().replaceAll("pswd=(.*?), ", "pswd=XXXX, ");
        log.info("Configuring Rest Operation....." + woPswd);
        Map<String, String> outputMessage = new HashMap<>();
        Client client = null;

        try {
            System.setProperty("jsse.enableSNIExtension", "false");
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new javax.net.ssl.TrustManager[] {new SecureRestClientTrustManager()}, null);
            client = createClient(sslContext);
            if ((transaction.getuId() != null) && (transaction.getPswd() != null)) {
                client.register(HttpAuthenticationFeature.basic(transaction.getuId(), transaction.getPswd()));
            }
            WebTarget webResource = client.target(new URI(transaction.getExecutionEndPoint()));
            webResource.property("Content-Type", "application/json;charset=UTF-8");

            javax.ws.rs.core.Response clientResponse = getClientResponse(transaction, webResource).orElseThrow(() -> new Exception(
                    "Cannot determine the state of : " + transaction.getActionLevel() + " HTTP response is null"));

            processClientResponse(clientResponse, transaction, outputMessage);

            log.info("Completed Rest Operation.....");

        } catch (Exception e) {
            log.debug("failed in RESTCONT Action (" + transaction.getExecutionRPC() + ") for the resource "
                    + transaction.getExecutionEndPoint() + ", fault message :" + e.getMessage());
            throw new Exception("Error While Sending Rest Request", e);

        } finally {
            if (client != null) {
                client.close();
            }
        }
        return outputMessage;
    }

    private HostnameVerifier getHostnameVerifier() {
        return (hostname, sslSession) -> true;
    }

    Client createClient(SSLContext ctx) {
        return ClientBuilder.newBuilder().sslContext(ctx).hostnameVerifier(getHostnameVerifier()).build();
    }

    private Optional<javax.ws.rs.core.Response> getClientResponse(Transaction transaction, WebTarget webResource) {
        String responseDataType = MediaType.APPLICATION_JSON;
        String requestDataType = MediaType.APPLICATION_JSON;
        javax.ws.rs.core.Response clientResponse = null;

        log.info("Starting Rest Operation.....");
        if (HttpMethod.GET.equalsIgnoreCase(transaction.getExecutionRPC())) {
            clientResponse = webResource.request(responseDataType).get(javax.ws.rs.core.Response.class);
        } else if (HttpMethod.POST.equalsIgnoreCase(transaction.getExecutionRPC())) {
            clientResponse = webResource.request(requestDataType).post(Entity.json(transaction.getPayload()),
         javax.ws.rs.core.Response.class);
        } else if (HttpMethod.PUT.equalsIgnoreCase(transaction.getExecutionRPC())) {
            clientResponse = webResource.request(requestDataType).put(Entity.json(transaction.getPayload()),
                javax.ws.rs.core.Response.class);
        } else if (HttpMethod.DELETE.equalsIgnoreCase(transaction.getExecutionRPC())) {
            clientResponse = webResource.request(requestDataType).delete(javax.ws.rs.core.Response.class);
        }
        return Optional.ofNullable(clientResponse);
    }

    private void processClientResponse(javax.ws.rs.core.Response clientResponse, Transaction transaction,
            Map<String, String> outputMessage) throws Exception {

        if (clientResponse.getStatus() == Status.OK.getStatusCode()) {
            Response response = new Response();
            response.setResponseCode(String.valueOf(Status.OK.getStatusCode()));
            transaction.setResponses(Collections.singletonList(response));
            outputMessage.put("restResponse", clientResponse.readEntity(String.class));
        } else {
            String errorMsg = clientResponse.readEntity(String.class);
            if (StringUtils.isNotBlank(errorMsg)) {
                log.debug("Error Message from Client Response" + errorMsg);
            }
            throw new Exception("Cannot determine the state of : " + transaction.getActionLevel()
                    + " HTTP error code : " + clientResponse.getStatus());
        }
    }
}
