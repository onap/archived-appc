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

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

class UEBMessagingService implements MessagingService {

    private Consumer consumer;
    private Producer producer;

    private final String DEFAULT_READ_TIMEOUT_MS = "60000";
    private final String DEFAULT_READ_LIMIT = "1000";

    private int readLimit;

    private final EELFLogger LOG = EELFManager.getInstance().getLogger(UEBMessagingService.class);

    @SuppressWarnings("Since15")
    public void init(Properties props) throws IOException, GeneralSecurityException, NoSuchFieldException, IllegalAccessException {

        if (props != null) {
            String readTopic = props.getProperty(UEBPropertiesKeys.TOPIC_READ);
            String writeTopic = props.getProperty(UEBPropertiesKeys.TOPIC_WRITE);
            String apiKey = props.getProperty(UEBPropertiesKeys.AUTH_USER);
            String apiSecret = props.getProperty(UEBPropertiesKeys.AUTH_SECRET);
            String readTimeoutString = props.getProperty(UEBPropertiesKeys.TOPIC_READ_TIMEOUT, DEFAULT_READ_TIMEOUT_MS);
            Integer readTimeout = Integer.parseInt(readTimeoutString);
            String readLimitString = props.getProperty(UEBPropertiesKeys.READ_LIMIT, DEFAULT_READ_LIMIT);
            readLimit = Integer.parseInt(readLimitString);
            //get hosts pool
            Collection<String> pool = new HashSet<String>();
            String hostNames = props.getProperty(UEBPropertiesKeys.HOSTS);
            if (hostNames != null && !hostNames.isEmpty()) {
                for (String name : hostNames.split(",")) {
                    pool.add(name);
                }
            }

            //generate consumer id and group - same value for both
            String consumerName = UUID.randomUUID().toString();
            String consumerID = consumerName;

            //create consumer and producer
            consumer = new ConsumerImpl(pool, readTopic, consumerName, consumerID, readTimeout, apiKey, apiSecret);
            producer = new ProducerImpl(pool, writeTopic, apiKey, apiSecret);

            //initial consumer registration
            try {
                consumer.registerForRead();
            }catch(Exception e){
                LOG.error("Message consumer failed to register client "+consumerID);
            }
        }
    }

    public void send(String partition, String body) throws IOException {
        producer.post(partition, body);
    }

    public List<String> fetch() throws IOException {
        return consumer.fetch(readLimit);
    }

    public List<String> fetch(int limit) throws IOException {
        return consumer.fetch(limit);
    }

    @Override
    public void close() {
        consumer.close();
        producer.close();
    }

}
