package org.onap.appc.services.dmaapService;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

import org.onap.appc.adapter.factory.DmaapMessageAdapterFactoryImpl;
import org.onap.appc.configuration.Configuration;
import org.onap.appc.configuration.ConfigurationFactory;
import org.springframework.stereotype.Service;
import org.onap.appc.adapter.message.MessageAdapterFactory;
import org.onap.appc.adapter.message.Producer;
import org.onap.appc.adapter.messaging.dmaap.http.HttpDmaapProducerImpl;

@Service
public class PublishService {
    
    private Map<String,Producer> producers;
    private MessageAdapterFactory factory;
    
    public PublishService() {
        factory = new DmaapMessageAdapterFactoryImpl();
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
        Configuration configuration = ConfigurationFactory.getConfiguration();
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
            return factory.createProducer(pool, writeTopic, apiKey, apiSecret);
        }
        return null;
    }
    
}
