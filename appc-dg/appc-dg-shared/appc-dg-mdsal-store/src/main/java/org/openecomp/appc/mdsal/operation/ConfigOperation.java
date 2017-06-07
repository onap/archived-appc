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

package org.openecomp.appc.mdsal.operation;

import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.mdsal.impl.Constants;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
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

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;

/**
 * Provides method to store configuration to MD-SAL store. It also exposes doPut operation which can be used to invoke REST Put operation.
*/
public class ConfigOperation {
    private static final EELFLogger LOG = EELFManager.getInstance().getLogger(ConfigOperation.class);

    private static URL url;
    private static String basicAuth;

    ConfigOperation(){}

    private static ConfigOperationRequestFormatter requestFormatter = new ConfigOperationRequestFormatter();

    private static ObjectMapper mapper = new ObjectMapper();

    /**
     * This method stores configuration JSON to MD-SAL store. Following input parameters are expected as input
     * @param configJson - configuration JSON as String. This value will be stored in  MD-SAL store
     * @param module - Module name that contains yang Schema
     * @param containerName - yang container name which will be used as base container.
     * @param subModules - Sub modules list if any. Order of sub module is top to bottom.
     * @throws APPCException
     */
    public static void storeConfig(String configJson , String module, String containerName, String... subModules ) throws APPCException {
        if (configJson == null) {
            throw new APPCException("Provided message was null");
        }
        LOG.debug("Config JSON: " + configJson +"\n"
                +"module" + module +"\n"
                +"containerName" + containerName +"\n"
                +"subModules length : " + subModules.length );

        int httpCode;
        String respBody ;
        try {
            String path = requestFormatter.buildPath(url, module, containerName, subModules);
            LOG.debug("Configuration Path : " + path);
            URL serviceUrl = new URL(url.getProtocol(), url.getHost(), url.getPort(), path);
            HttpResponse response = doPut(serviceUrl , configJson);
            httpCode = response.getStatusLine().getStatusCode();
            respBody = IOUtils.toString(response.getEntity().getContent());
        } catch (IOException e) {
            LOG.error("Error while storing configuration json "+e.getMessage(), e);
            throw new APPCException(e);
        }

        if (httpCode != 200 ) {
            try {
                ArrayList<String> errorMessage = new ArrayList<>();
                JsonNode responseJson = toJsonNodeFromJsonString(respBody);
                if(responseJson!=null && responseJson.get("errors")!=null) {
                    JsonNode errors = responseJson.get("errors").get("error");
                for (Iterator<JsonNode> i = errors.elements();i.hasNext();){
                    JsonNode error = i.next();
                    errorMessage.add(error.get("error-message").textValue());
                }
                }
                throw new APPCException("Failed to load config JSON to MD SAL store. Error Message:" + errorMessage.toString());
            } catch (Exception e) {
                LOG.error("Error while loading config JSON to MD SAL store. "+e.getMessage(), e);
                throw new APPCException("Error while loading config JSON to MD SAL store. "+ e.getMessage(),e);
            }
        }
    }

    /**
     * This is Generic method that can be used to perform REST Put operation
     * @param url - Destination URL for put
     * @param body - payload for put action which will be sent as request body.
     * @return - HttpResponse object which is returned from put REST call.
     * @throws APPCException
     */
    public static HttpResponse doPut (URL url, String body) throws APPCException {
        HttpPut put;
        try {
            put = new HttpPut(url.toExternalForm());
            put.setHeader(HttpHeaders.CONTENT_TYPE, Constants.OPERATION_APPLICATION_JSON);
            put.setHeader(HttpHeaders.ACCEPT, Constants.OPERATION_APPLICATION_JSON);

            if (basicAuth != null) {
                put.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + basicAuth);
            }

            StringEntity entity = new StringEntity(body);
            entity.setContentType(Constants.OPERATION_APPLICATION_JSON);
            put.setEntity(new StringEntity(body));
        } catch (UnsupportedEncodingException e) {
            throw new APPCException(e);
        }

        HttpClient client = getHttpClient();

        try {
            return client.execute(put);
        } catch (IOException e) {
            throw new APPCException(e);
        }

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
            LOG.error("Malformed URL " +newUrl + e.getMessage(), e);
        }
    }

    /**
     * Sets the basic authentication header for the given user and password. If either entry is null then set basic auth
     * to null
     *
     * @param user     The user with optional domain name (for AAF)
     * @param password The password for the user
     * @return The new value of the basic auth string that will be used in the request headers
     */
    public static String setAuthentication(String user, String password) {
        if (user != null && password != null) {
            String authStr = user + ":" + password;
            basicAuth = new String(Base64.encodeBase64(authStr.getBytes()));
        } else {
            basicAuth = null;
        }
        return basicAuth;
    }

    @SuppressWarnings("deprecation")
    private static HttpClient getHttpClient() throws APPCException {
        HttpClient client;
        if (url.getProtocol().equals(Constants.OPERATION_HTTPS)) {
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
                registry.register(new Scheme(Constants.OPERATION_HTTPS, sf, 443));
                registry.register(new Scheme(Constants.OPERATION_HTTPS, sf, 8443));
                registry.register(new Scheme("http", sf, 8181));

                ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);
                client = new DefaultHttpClient(ccm, params);
            } catch (Exception e) {
                LOG.error("Error creating HTTP Client. Creating default client." ,  e);
                client = new DefaultHttpClient();
            }
        } else if ("http".equals(url.getProtocol())) {
            client = new DefaultHttpClient();
        } else {
            throw new APPCException(
                    "The provider.topology.url property is invalid. The url did not start with http[s]");
        }
        return client;
    }

    @SuppressWarnings("deprecation")
    private static class MySSLSocketFactory extends SSLSocketFactory {
        private SSLContext sslContext = SSLContext.getInstance("TLS");

        private MySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException,
                KeyStoreException, UnrecoverableKeyException {
            super(truststore);

            TrustManager tm = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    LOG.debug("Inside checkClientTrusted");
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    LOG.debug("Inside checkServerTrusted");
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[1];
                }
            };

            sslContext.init(null, new TrustManager[]{
                    tm
            }, null);
        }

        @Override
        public Socket createSocket(Socket socket, String host, int port, boolean autoClose)
                throws IOException  {
            return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
        }

        @Override
        public Socket createSocket() throws IOException {
            return sslContext.getSocketFactory().createSocket();
        }
    }

    private static JsonNode toJsonNodeFromJsonString(String jsonStr) {
        JsonNode jsonNode = null;
        if(jsonStr != null) {
            try {
                jsonNode = mapper.readTree(jsonStr);
            } catch (IOException e) {
                LOG.warn(String.format("Could not map %s to jsonNode.", jsonStr), e);
            }
        }
        return jsonNode;
    }

}
