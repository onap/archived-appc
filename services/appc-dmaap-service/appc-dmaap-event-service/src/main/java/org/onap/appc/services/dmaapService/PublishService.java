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

package org.onap.appc.services.dmaapService;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

import org.onap.appc.adapter.factory.DmaapMessageAdapterFactoryImpl;
import org.onap.appc.configuration.Configuration;
import org.onap.appc.configuration.ConfigurationFactory;
import org.springframework.stereotype.Service;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

import org.onap.appc.adapter.message.MessageAdapterFactory;
import org.onap.appc.adapter.message.Producer;

@Service
public class PublishService {
    
    private static final EELFLogger LOG = EELFManager.getInstance().getLogger(PublishService.class);
    
    private Map<String,Producer> producers;
    private MessageAdapterFactory factory;
    Configuration configuration;
    
    public PublishService() {
        this.factory = new DmaapMessageAdapterFactoryImpl();
        producers = new HashMap<>();
    }
    
    public PublishService(MessageAdapterFactory factory) {
        this.factory = factory;
        producers = new HashMap<>();
    }
    
    public String publishMessage(String key, String partition, String topic, String message) {
        Producer producer = getProducer(key, topic);
        if(producer == null) {
            return "Could not find producer with property prefix: " + key;
        }
        boolean success = producer.post(partition, message);
        if(success) {
            return "Success";
        }
        return "Failed. See dmaap service jar log.";
    }
    
    private Producer getProducer(String key, String topic) {
        String searchKey = key;
        if(topic != null) {
            searchKey += topic;
        }
        Producer producer = producers.get(searchKey);
        if(producer != null) {
            return producer;
        }
        producer =  newProducer(key, topic);
        producers.put(searchKey,producer);
        return producer;
    }
    
    private Producer newProducer(String key, String topic) {
        Configuration configuration;
        if(this.configuration != null) {
            configuration = this.configuration;
        } else {
            configuration = ConfigurationFactory.getConfiguration();
        }
        Properties props = configuration.getProperties();
        HashSet<String> pool = new HashSet<>();
        if (props != null) {
            String writeTopic;
            if(topic == null) {
                writeTopic = props.getProperty(key + ".topic.write");
            } else {
                writeTopic = topic;
            }
            String apiKey = props.getProperty(key + ".client.key");
            String apiSecret = props.getProperty(key + ".client.secret");
            String hostnames = props.getProperty(key + ".poolMembers");
            if (hostnames != null && !hostnames.isEmpty()) {
                for (String name : hostnames.split(",")) {
                    pool.add(name);
                }
            }
            if(pool.isEmpty()) {
                LOG.error("There are no dmaap server pools. Check the property " + key + ".poolMembers");
                return null;
            }
            if(writeTopic == null || writeTopic.isEmpty()) {
                LOG.error("There is no write topic defined in the message request or in the properties file");
                return null;
            }
            return factory.createProducer(pool, writeTopic, apiKey, apiSecret);
        }
        return null;
    }
    
}
