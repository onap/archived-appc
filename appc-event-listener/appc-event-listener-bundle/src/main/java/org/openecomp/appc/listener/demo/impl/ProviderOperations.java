/*-
 * ============LICENSE_START=======================================================
 * APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Amdocs
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
 * ============LICENSE_END=========================================================
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */

package org.openecomp.appc.listener.demo.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.listener.demo.model.IncomingMessage;
import org.openecomp.appc.listener.util.Mapper;

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

        HttpClient client = getHttpClient();

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
            e.printStackTrace();
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

    @SuppressWarnings("deprecation")
    private static HttpClient getHttpClient() throws APPCException {
        HttpClient client;
        if (url.getProtocol().equals("https")) {
            try {
                KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustStore.load(null, null);
                MySSLSocketFactory sf = new MySSLSocketFactory(trustStore);
                sf.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

                HttpParams params = new BasicHttpParams();
                HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
                HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

                SchemeRegistry registry = new SchemeRegistry();
                registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
                registry.register(new Scheme("https", sf, 443));
                registry.register(new Scheme("https", sf, 8443));
                registry.register(new Scheme("http", sf, 8181));

                ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
                client = new DefaultHttpClient(ccm, params);
            } catch (Exception e) {
                client = new DefaultHttpClient();
            }
        } else if (url.getProtocol().equals("http")) {
            client = new DefaultHttpClient();
        } else {
            throw new APPCException(
                "The provider.topology.url property is invalid. The url did not start with http[s]");
        }
        return client;
    }

    @SuppressWarnings("deprecation")
    public static class MySSLSocketFactory extends SSLSocketFactory {
        private SSLContext sslContext = SSLContext.getInstance("TLS");

        public MySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException,
                        KeyStoreException, UnrecoverableKeyException {
            super(truststore);

            TrustManager tm = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            sslContext.init(null, new TrustManager[] {
                tm
            }, null);
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
            throws IOException, UnknownHostException {
            return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return sslContext.getSocketFactory().createSocket();
        }
    }

}
