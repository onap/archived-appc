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

package org.openecomp.appc.sdc.listener;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
import java.util.Map;
import java.util.Map.Entry;

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
import org.openecomp.appc.exceptions.APPCException;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class ProviderOperations {

    private static final EELFLogger LOG = EELFManager.getInstance().getLogger(ProviderOperations.class);

    private static String basic_auth;

    public static ProviderResponse post(URL url, String json, Map<String, String> adtl_headers) throws APPCException {
        if (json == null) {
            throw new APPCException("Provided message was null");
        }

        HttpPost post = null;
        try {
            post = new HttpPost(url.toExternalForm());
            post.setHeader("Content-Type", "application/json");
            post.setHeader("Accept", "application/json");

            // Set Auth
            if (basic_auth != null) {
                post.setHeader("Authorization", "Basic " + basic_auth);
            }

            if (adtl_headers != null) {
                for (Entry<String, String> header : adtl_headers.entrySet()) {
                    post.setHeader(header.getKey(), header.getValue());
                }
            }

            StringEntity entity = new StringEntity(json);
            entity.setContentType("application/json");
            post.setEntity(new StringEntity(json));
        } catch (UnsupportedEncodingException e) {
            throw new APPCException(e);
        }

        HttpClient client = getHttpClient(url);

        int httpCode = 0;
        String respBody = null;
        try {
            HttpResponse response = client.execute(post);
            httpCode = response.getStatusLine().getStatusCode();
            respBody = IOUtils.toString(response.getEntity().getContent());
            return new ProviderResponse(httpCode, respBody);
        } catch (IOException e) {
            throw new APPCException(e);
        }
    }

    /**
     * Sets the basic authentication header for the given user and password. If either entry is null then set basic auth
     * to null
     *
     * @param user
     *            The user with optional domain name (for AAF)
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

    @SuppressWarnings("deprecation")
    private static HttpClient getHttpClient(URL url) throws APPCException {
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
        private SSLContext sslContext = SSLContext.getInstance("TLSv1.2");

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
