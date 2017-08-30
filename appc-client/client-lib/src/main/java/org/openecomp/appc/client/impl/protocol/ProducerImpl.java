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

import com.att.nsa.cambria.client.CambriaBatchingPublisher;
import com.att.nsa.cambria.client.CambriaClientBuilders.PublisherBuilder;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.GeneralSecurityException;
import java.util.Collection;

class ProducerImpl implements Producer {

    private Collection<String> hosts;
    private String topic;
    private CambriaBatchingPublisher producer;

    private String authKey;
    private String authSecret;

    public ProducerImpl(Collection<String> urls, String topicName, String apiKey, String apiSecret) throws MalformedURLException, GeneralSecurityException {

        topic = topicName;
        hosts = urls;
        authKey = apiKey;
        authSecret = apiSecret;
        producer = getProducer();
    }

    public void post(String partition, String data) throws IOException {

        producer.send(partition, data);
    }

    /**
     * get cambria producer
     * @return
     */
    private CambriaBatchingPublisher getProducer() throws MalformedURLException, GeneralSecurityException {

        PublisherBuilder builder = new PublisherBuilder().usingHosts(hosts);

        // Add credentials if provided
        if (authKey != null && authSecret != null) {
            builder.authenticatedBy(authKey, authSecret);
        }

        CambriaBatchingPublisher client = null;

        client = builder.onTopic(topic).build();

        return client;
    }

    @Override
    public void close() {
        producer.close();
    }
}
