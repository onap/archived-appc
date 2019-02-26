/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
 * Modifications Copyright (C) 2019 Ericsson
 * Modifications Copyright (C) 2019 IBM
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

package org.onap.appc.listener.demo.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONObject;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.listener.demo.model.IncomingMessage;
import org.onap.appc.listener.util.HttpClientUtil;
import org.onap.appc.listener.util.Mapper;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class ProviderOperations {

    private static final EELFLogger LOG = EELFManager.getInstance().getLogger(ProviderOperations.class);

    private static URL url;

    private static String basic_auth;

    //@formatter:off
    @SuppressWarnings("nls")
    private final static String TEMPLATE = "{\"input\": {\"common-request-header\": {\"service-request-id\": \"%s\"},\"config-payload\": {\"config-url\": \"%s\",\"config-json\":\"%s\"}}}";
    //@formatter:on 

    /**
     * Calls the AppcProvider to run a topology directed graph
     * 
     * @param msg
     *            The incoming message to be run
     * @return True if the result is success. Never returns false and throws an exception instead.
     * @throws UnsupportedEncodingException
     * @throws Exception
     *             if there was a failure processing the request. The exception message is the failure reason.
     */
    @SuppressWarnings("nls")
    public static boolean topologyDG(IncomingMessage msg) throws APPCException {
        if (msg == null) {
            throw new APPCException("Provided message was null");
        }

        HttpPost post = null;
        try {
            // Concatenate the "action" on the end of the URL
            String path = url.getPath() + ":" + msg.getAction().getValue();
            URL serviceUrl = new URL(url.getProtocol(), url.getHost(), url.getPort(), path);

            post = new HttpPost(serviceUrl.toExternalForm());
            post.setHeader("Content-Type", "application/json");
            post.setHeader("Accept", "application/json");

            // Set Auth
            if (basic_auth != null) {
                post.setHeader("Authorization", "Basic " + basic_auth);
            }

            //String body = buildReqest(msg.getId(), msg.getUrl(), msg.getIdentityUrl());
            String body = buildReqest(msg.getHeader().getRequestID(), msg.getPayload().getGenericVnfId(), msg.getPayload().getPgStreams());
            StringEntity entity = new StringEntity(body);
            entity.setContentType("application/json");
            post.setEntity(new StringEntity(body));
        } catch (UnsupportedEncodingException | MalformedURLException e) {
            throw new APPCException(e);
        }

        HttpClient client = HttpClientUtil.getHttpClient(url.getProtocol());

        int httpCode = 0;
        String respBody = null;
        try {
            HttpResponse response = client.execute(post);
            httpCode = response.getStatusLine().getStatusCode();
            respBody = IOUtils.toString(response.getEntity().getContent());
        } catch (IOException e) {
            throw new APPCException(e);
        }

        if (httpCode == 200 && respBody != null) {
            JSONObject json;
            try {
                json = Mapper.toJsonObject(respBody);
            } catch (Exception e) {
                LOG.error("Error prcoessing response from provider. Could not map response to json", e);
                throw new APPCException("APPC has an unknown RPC error");
            }
            boolean success;
            String reason;
            try {
                JSONObject header = json.getJSONObject("output").getJSONObject("common-response-header");
                success = header.getBoolean("success");
                reason = header.getString("reason");
            } catch (Exception e) {
                LOG.error("Unknown error prcoessing failed response from provider. Json not in expected format", e);
                throw new APPCException("APPC has an unknown RPC error");
            }
            if (success) {
                return true;
            }
            String reasonStr = reason == null ? "Unknown" : reason;
            LOG.warn(String.format("Topology Operation [%s] failed. Reason: %s", msg.getHeader().getRequestID(), reasonStr));
            throw new APPCException(reasonStr);

        }
        throw new APPCException(String.format("Unexpected response from endpoint: [%d] - %s ", httpCode, respBody));
    }

    /**
     * Updates the static var URL and returns the value;
     * 
     * @return The new value of URL
     */
    public static String getUrl() {
        return url.toExternalForm();
    }

    public static void setUrl(String newUrl) {
        try {
            url = new URL(newUrl);
        } catch (MalformedURLException e) {
          LOG.error("Malformed URL", e);
        }
    }

    /**
     * Sets the basic authentication header for the given user and password. If either entry is null then set basic auth
     * to null
     *
     * @param user
     *            The user with optional domain name
     * @param password
     *            The password for the user
     * @return The new value of the basic auth string that will be used in the request headers
     */
    public static String setAuthentication(String user, String password) {
        if (user != null && password != null) {
            String authStr = user + ":" + password;
            basic_auth = new String(Base64.encodeBase64(authStr.getBytes()));
        } else {
            basic_auth = null;
        }
        return basic_auth;
    }

    /**
     * Builds the request body for a topology operation
     * 
     * @param id
     *            The request id
     * @param url
     *            The vm's url
     *            
     * @param pgstreams 
     *           The streams to send to the traffic generator
     *
     * @return A String containing the request body
     */
    private static String buildReqest(String id, String url, String pgstreams) {

        return String.format(TEMPLATE, id, url, pgstreams);
    }
}
