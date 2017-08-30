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

package org.openecomp.appc.client.impl.protocol;

import com.att.nsa.cambria.client.CambriaClientBuilders.ConsumerBuilder;
import com.att.nsa.cambria.client.CambriaConsumer;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class ConsumerImpl implements Consumer {

    private static final int DEFAULT_LIMIT = 1000;

    private Collection<String> hosts;
    private String topic;
    private String group;
    private String groupId;
    private int timeout;

    private String authKey;
    private String authSecret;

    private CambriaConsumer consumer = null;

    /**
     * constructor
     * @param urls
     * @param topicName
     * @param consumerName
     * @param consumerId
     * @param timeout
     */
    public ConsumerImpl(Collection<String> urls, String topicName, String consumerName, String consumerId, Integer timeout, String apiKey, String apiSecret) throws MalformedURLException, GeneralSecurityException, NoSuchFieldException, IllegalAccessException {
        this.hosts = urls;
        this.topic = topicName;
        this.group = consumerName;
        this.groupId = consumerId;
        this.authKey = apiKey;
        this.authSecret = apiSecret;
        this.timeout = timeout;
        consumer = getConsumer();
    }


    public List<String> fetch() throws IOException {

        return fetch(DEFAULT_LIMIT);
    }

    public List<String> fetch(int limit) throws IOException {

        List<String> out = new ArrayList<String>();
        try {
            for(String msg : consumer.fetch(timeout,limit)){
                out.add(msg);
            }
        } catch (IOException e) {
            throw e;
        }
        return out;
    }

    public void registerForRead() throws IOException {

        int waitForRegisteration = 1; //return from fetch after 1ms, no need to read any messages
        consumer.fetch(waitForRegisteration, 1);
    }

    /**
     * init cambria consumer
     * @return CambriaConsumer
     */
    private CambriaConsumer getConsumer() throws MalformedURLException, GeneralSecurityException, NoSuchFieldException, IllegalAccessException {

        ConsumerBuilder builder = new ConsumerBuilder();

        builder.usingHosts(hosts).onTopic(topic).knownAs(group, groupId);
        builder.withSocketTimeout(timeout + 5000).waitAtServer(timeout);
        builder.receivingAtMost(DEFAULT_LIMIT);

        // Add credentials if provided
        if (authKey != null && authSecret != null) {

            Field apiKeyField = ConsumerBuilder.class.getDeclaredField("fApiKey");
            apiKeyField.setAccessible(true);
            apiKeyField.set(builder, "");
            builder.authenticatedBy(authKey, authSecret);
        }

        return builder.build();
    }

    @Override
    public void close() {
        consumer.close();
    }
}
