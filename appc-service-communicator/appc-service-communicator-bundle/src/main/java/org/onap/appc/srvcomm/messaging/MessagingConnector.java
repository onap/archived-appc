/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.appc.srvcomm.messaging;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.onap.appc.configuration.ConfigurationFactory;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class MessagingConnector {

    private static final EELFLogger LOG = EELFManager.getInstance().getLogger(MessagingConnector.class);

    private final String URL;
    private final String USER;
    private final String PASSWORD;
    private final String PROPERTIES_PREFIX = "appc.srvcomm.messaging";
    public MessagingConnector() {

        Properties props = ConfigurationFactory.getConfiguration().getProperties();
        String url = props.getProperty(PROPERTIES_PREFIX + ".url");
        if(!isNullOrEmpty(url)) {
            this.URL = url;
        } else {
            this.URL = "localhost:8080";
        }
        String username = props.getProperty(PROPERTIES_PREFIX + ".username");
        String password = props.getProperty(PROPERTIES_PREFIX + ".password");
        //Both username and password properties need to be set. One or the other
        //can't be set.
        if(isNullOrEmpty(username, password)) {
            USER = null;
            PASSWORD = null;
        } else {
            USER = username;
            PASSWORD = password;
        }
    }
    public boolean isNullOrEmpty(String... strings) {
        for(String s : strings) {
            if(s == null || s.isEmpty()){
                return true;
            }
        }
        return false;
    }
    

    public boolean publishMessage(String propertySet, String partition, String data) {
        return publishMessage(propertySet, partition, null, data);
    }

    public boolean publishMessage(String propertySet, String partition, String topic, String data) {
        HttpPost post = new HttpPost(URL);
        //check if we need to enable authentication
        if(USER != null) {
            String authStr = getBasicAuth(USER, PASSWORD);
            post.setHeader("Authorization", String.format("Basic %s", authStr));
        }
        //encode the message so it can be sent to the dmaap client jar 
        String body = bodyLine(propertySet, partition, topic, data);
        try {
            post.setEntity(new StringEntity(body));
        } catch (UnsupportedEncodingException e) {
            LOG.error("Error during publishMessage",e);
        }
        try {
            CloseableHttpResponse response = getClient().execute(post);
            if (response.getStatusLine().getStatusCode() == 200) {
                return true;
            } else {
                LOG.error(response.getStatusLine().getStatusCode() +
                        " Error during publishMessage: " + 
                        response.getStatusLine().getReasonPhrase() +
                        " See messaging service jar logs.");
                return false;
            }
        } catch (ClientProtocolException e) {
            LOG.error("Error during publishMessage",e);
        } catch (IOException e) {
            LOG.error("Error during publishMessage",e);
        }
        return false;

    }

    /**
     * Format the body for the application/cambria content type with no partitioning. See
     *
     * @param propertySet
     *            The prefix of the properties that the dmaap service should look up in order to
     *            get the correct properties for the message being sent.
     * @param partition
     *            The dmaap partition that the message should be published to
     * @param topic
     *            The dmaap topic that the message should be published to. Leave this unset in order
     *            to use the topic that is in the property files.
     * @param message
     *            The message to publish
     * @return A string in the application/cambria content type
     */
    private String bodyLine(String propertySet, String partition, String topic, String message) {
        String prop = (propertySet == null) ? "" : propertySet;
        String prt = (partition == null) ? "" : partition;
        String msg = (message == null) ? "" : message;
        String top = (topic == null) ? "" : topic;
        return String.format("%d.%d.%d.%d.%s%s%s%s", prop.length(),prt.length(), top.length(),
                msg.length(), prop, prt, top, msg);
    }

    protected CloseableHttpClient getClient() {
        return HttpClientBuilder.create().build();
    }
    
    protected String getBasicAuth(String username, String password) {
        if (username != null && password != null) {
            String plain = String.format("%s:%s", username, password);
            return Base64.encodeBase64String(plain.getBytes());
        } else {
            return null;
        }
    }

}
