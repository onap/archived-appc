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

package org.onap.appc.adapter.messaging.dmaap.http;

import java.net.URI;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

abstract class CommonHttpClient {

    private static final int HTTP_PORT = 3904;
    private static final int HTTPS_PORT = 3905;
    private static final int TIMEOUT_OFFSET = 5000;

    private String authStr;

    protected void setBasicAuth(String username, String password) {
        if (username != null && password != null) {
            String plain = String.format("%s:%s", username, password);
            authStr = Base64.encodeBase64String(plain.getBytes());
        } else {
            authStr = null;
        }
    }

    protected HttpGet getReq(URI uri, int timeoutMs) throws AuthenticationException {

        HttpGet out = (uri == null) ? new HttpGet() : new HttpGet(uri);
        if (authStr != null) {
            out.setHeader("Authorization", String.format("Basic %s", authStr));
        }
      
        out.setConfig(getConfig(timeoutMs));
        return out;
    }

    protected HttpPost postReq(String url) throws AuthenticationException {

        HttpPost out = (url == null) ? new HttpPost() : new HttpPost(url);
        if (authStr != null) {
            out.setHeader("Authorization", String.format("Basic %s", authStr));
        }
        out.setConfig(getConfig(0));
        return out;
    }

    private RequestConfig getConfig(int timeoutMs) {
        Builder builder = RequestConfig.custom();
        builder.setSocketTimeout(timeoutMs + TIMEOUT_OFFSET);
        return builder.build();
    }

    protected CloseableHttpClient getClient() {
        return HttpClientBuilder.create().build();
    }

    protected String formatHostString(String host) {
        return formatHostString(host, host.contains(String.valueOf(HTTPS_PORT)));
    }

    private String formatHostString(String host, boolean useHttps) {
        // Trim trailing slash
        String out = host.endsWith("/") ? host.substring(0, host.length() - 1) : host;

        boolean hasProtocol = out.startsWith("http");
        boolean hasPort = out.contains(":");

        // Add protocol
        if (!hasProtocol) {
            out = String.format("%s%s", (useHttps) ? "https://" : "http://", out);
        }
        // Add port
        if (!hasPort) {
            out = String.format("%s:%d", out, (useHttps) ? HTTPS_PORT : HTTP_PORT);
        }
        return out;
    }
}
