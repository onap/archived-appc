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

package org.onap.appc.client.impl.protocol;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

class UEBMessagingService implements MessagingService {

    private final EELFLogger logger = EELFManager.getInstance().getLogger(UEBMessagingService.class);

    private static final String DEFAULT_READ_TIMEOUT_MS = "60000";
    private static final String DEFAULT_READ_LIMIT = "1000";
    private static final String DEFAULT_READ_TOPIC = "client-read";
    private static final String DEFAULT_WRITE_TOPIC = "client-write";

    private Consumer consumer;
    private Producer producer;
    private int readLimit;

    @Override
    @SuppressWarnings("Since15")
    public void init(Properties props)
            throws IOException, GeneralSecurityException, NoSuchFieldException, IllegalAccessException {

        if (props != null) {
            String readTopic = null;
            String writeTopic = null;
            String cType = props.getProperty(UEBPropertiesKeys.CONTROLLER_TYPE); //CONTROLLER_TYPE = "controllerType"
            
            if (cType != null && cType.length()!= 0 && (!cType.equals("APPC")))
            {
                logger.debug("Using controller type " + cType + " for topic properties");
                
                readTopic = props.getProperty(cType + "-" + UEBPropertiesKeys.TOPIC_READ);
                if(readTopic == null) {
                    logger.error("Error reading property '"+ cType + "-" + UEBPropertiesKeys.TOPIC_READ + "' defaulting to " + DEFAULT_READ_TOPIC);
                    readTopic = DEFAULT_READ_TOPIC;
                }
                writeTopic = props.getProperty(cType + "-" + UEBPropertiesKeys.TOPIC_WRITE);
                if(writeTopic == null) {
                    logger.error("Error reading property '"+ cType + "-" + UEBPropertiesKeys.TOPIC_READ + "' defaulting to " + DEFAULT_WRITE_TOPIC);
                    writeTopic = DEFAULT_WRITE_TOPIC;
                }
            }
            else {
                readTopic = props.getProperty(UEBPropertiesKeys.TOPIC_READ); //TOPIC_READ = "topic.read"
                if(readTopic == null) {
                    logger.error("Error reading property '"+ UEBPropertiesKeys.TOPIC_READ + "' defaulting to " + DEFAULT_READ_TOPIC);
                    readTopic = DEFAULT_READ_TOPIC;
                }
                writeTopic = props.getProperty(UEBPropertiesKeys.TOPIC_WRITE); //TOPIC_WRITE = "topic.write"
                if(writeTopic == null) {
                    logger.error("Error reading property '" + UEBPropertiesKeys.TOPIC_READ + "' defaulting to " + DEFAULT_WRITE_TOPIC);
                    writeTopic = DEFAULT_WRITE_TOPIC;
                }
            }
            
            logger.debug("Using topics: Read = '" + readTopic + "' Write = '" + writeTopic + "'");
            
            String apiKey = props.getProperty(UEBPropertiesKeys.AUTH_USER);
            String apiSecret = props.getProperty(UEBPropertiesKeys.AUTH_SECRET);
            String readTimeoutString = props.getProperty(UEBPropertiesKeys.TOPIC_READ_TIMEOUT, DEFAULT_READ_TIMEOUT_MS);
            Integer readTimeout = Integer.parseInt(readTimeoutString);
            String readLimitString = props.getProperty(UEBPropertiesKeys.READ_LIMIT, DEFAULT_READ_LIMIT);
            readLimit = Integer.parseInt(readLimitString);
            //get hosts pool
            Collection<String> pool = new HashSet<>();
            String hostNames = props.getProperty(UEBPropertiesKeys.HOSTS);
            if (hostNames != null && !hostNames.isEmpty()) {
                pool.addAll(Arrays.asList(hostNames.split(",")));
            }
            //generate consumer id and group - same value for both
            String consumerName = UUID.randomUUID().toString();

            //create consumer and producer
            consumer = new ConsumerImpl(pool, readTopic, consumerName, consumerName, readTimeout, apiKey, apiSecret);
            producer = new ProducerImpl(pool, writeTopic, apiKey, apiSecret);

            //initial consumer registration
            try {
                consumer.registerForRead();
            } catch (Exception e) {
                logger.error("Message consumer failed to register client " + consumerName, e);
            }
        }
    }

    @Override
    public void send(String partition, String body) throws IOException {
        producer.post(partition, body);
    }

    @Override
    public List<String> fetch() throws IOException {
        return consumer.fetch(readLimit);
    }

    @Override
    public List<String> fetch(int limit) throws IOException {
        return consumer.fetch(limit);
    }

    @Override
    public void close() {
        consumer.close();
        producer.close();
    }

}