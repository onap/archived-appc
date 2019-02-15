/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
 * Modifications Copyright (C) 2019 Ericsson
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

package org.onap.appc.rest.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.util.HttpClientUtil;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

@SuppressWarnings("deprecation")
public class RestClientInvoker {

    private static final EELFLogger LOG = EELFManager.getInstance().getLogger(RestClientInvoker.class);
    private static final String OPERATION_APPLICATION_JSON = " application/json";
    private static final String BASIC = "Basic ";

    private URL url = null;
    private String basicAuth = null;

    public RestClientInvoker(URL url) {
        this.url = url;
    }

    /**
     * Sets the basic authentication header for the given user and password. If either entry is null
     * then does not set basic auth
     *
     * @param user The user with optional domain name (for AAF)
     * @param password The password for the user
     */
    public void setAuthentication(String user, String password) {
        if (user != null && password != null) {
            String authStr = user + ":" + password;
            basicAuth = new String(Base64.encodeBase64(authStr.getBytes()));
        }
    }

    public HttpResponse doPost(String path, String body) throws APPCException {
        HttpPost post;

        try {

            URL postUrl = new URL(url.getProtocol(), url.getHost(), url.getPort(), path);
            post = new HttpPost(postUrl.toExternalForm());
            post.setHeader(HttpHeaders.CONTENT_TYPE, OPERATION_APPLICATION_JSON);
            post.setHeader(HttpHeaders.ACCEPT, OPERATION_APPLICATION_JSON);

            if (basicAuth != null) {
                post.setHeader(HttpHeaders.AUTHORIZATION, BASIC + basicAuth);
            }

            StringEntity entity = new StringEntity(body);
            entity.setContentType(OPERATION_APPLICATION_JSON);
            post.setEntity(new StringEntity(body));
        } catch (MalformedURLException | UnsupportedEncodingException e) {
            throw new APPCException(e);
        }
        HttpClient client = HttpClientUtil.getHttpClient(url.getProtocol());

        try {
            return client.execute(post);
        } catch (IOException e) {
            throw new APPCException(e);
        }
    }

    /**
     * This is Generic method that can be used to perform REST Put operation
     *
     * @param path - path for put
     * @param body - payload for put action which will be sent as request body.
     * @return - HttpResponse object which is returned from put REST call.
     * @throws APPCException when error occurs
     */
    public HttpResponse doPut(String path, String body) throws APPCException {
        HttpPut put;
        try {
            URL putUrl = new URL(url.getProtocol(), url.getHost(), url.getPort(), path);
            put = new HttpPut(putUrl.toExternalForm());
            put.setHeader(HttpHeaders.CONTENT_TYPE, OPERATION_APPLICATION_JSON);
            put.setHeader(HttpHeaders.ACCEPT, OPERATION_APPLICATION_JSON);

            if (basicAuth != null) {
                put.setHeader(HttpHeaders.AUTHORIZATION, BASIC + basicAuth);
            }

            StringEntity entity = new StringEntity(body);
            entity.setContentType(OPERATION_APPLICATION_JSON);
            put.setEntity(new StringEntity(body));
        } catch (UnsupportedEncodingException | MalformedURLException e) {
            throw new APPCException(e);
        }

        HttpClient client = HttpClientUtil.getHttpClient(url.getProtocol());

        try {
            return client.execute(put);
        } catch (IOException e) {
            throw new APPCException(e);
        }
    }

    public HttpResponse doGet(String path) throws APPCException {
        HttpGet get;
        try {
            URL getUrl = new URL(url.getProtocol(), url.getHost(), url.getPort(), path);
            get = new HttpGet(getUrl.toExternalForm());
            get.setHeader(HttpHeaders.CONTENT_TYPE, OPERATION_APPLICATION_JSON);
            get.setHeader(HttpHeaders.ACCEPT, OPERATION_APPLICATION_JSON);

            if (basicAuth != null) {
                get.setHeader(HttpHeaders.AUTHORIZATION, BASIC + basicAuth);
            }

        } catch (Exception e) {
            throw new APPCException(e);
        }

        try (CloseableHttpClient client = HttpClientUtil.getHttpClient(url.getProtocol())) {
            return client.execute(get);
        } catch (IOException e) {
            throw new APPCException(e);
        }
    }

   
}
