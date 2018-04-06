package org.onap.appc.adapter.messaging.dmaap.impl;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;

public class TestDmaapProducerImpl {
    String[] hostList = {"192.168.1.1"};    
    Collection<String> hosts = new HashSet<String>(Arrays.asList(hostList));
    
    String topic = "JunitTopicOne";
    String group = "junit-client";
    String id = "junit-consumer-one";
    String key = "key";
    String secret = "secret";
    String filter = null;
    
    @Test
    public void testDmaapProducerImplSingleTopic() {
        DmaapProducerImpl producer = new DmaapProducerImpl(hosts, topic, key, secret);
        
        assertNotNull(producer);
        
        Properties props = producer.getProperties();
        
        assertNotNull(props);
        
        assertEquals("key",props.getProperty("username"));
        assertEquals("secret",props.getProperty("password"));
    }

    @Test
    public void testDmaapProducerImplMultipleTopic() {
        String[] topicList = {"topic1","topic2"};
        Set<String> topicNames = new HashSet<String>(Arrays.asList(topicList));
        
        DmaapProducerImpl producer = new DmaapProducerImpl(hosts, topicNames, key, secret);
        
        assertNotNull(producer);
        
        
        Properties props = producer.getProperties();
        
        assertNotNull(props);
        
        assertEquals("key",props.getProperty("username"));
        assertEquals("secret",props.getProperty("password"));
        
    }
    
    @Test
    public void testDmaapProducerImplNoUserPass() {
        DmaapProducerImpl producer = new DmaapProducerImpl(hosts, topic, null, null);
        
        assertNotNull(producer);
        
        Properties props = producer.getProperties();
        
        assertNotNull(props);
        
        assertNull(props.getProperty("username"));
        assertNull(props.getProperty("password"));
    }

    @Test
    public void testUpdateCredentials() {
        DmaapProducerImpl producer = new DmaapProducerImpl(hosts, topic, null, null);
        
        assertNotNull(producer);
        
        Properties props = producer.getProperties();
        
        assertNotNull(props);
        
        assertNull(props.getProperty("username"));
        assertNull(props.getProperty("password"));
        
        producer.updateCredentials(key, secret);
        
        props = producer.getProperties();
        
        assertNotNull(props);
        
        assertEquals("key",props.getProperty("username"));
        assertEquals("secret",props.getProperty("password"));
        
    }

    @Ignore
    @Test
    public void testPost() {
        fail("Not yet implemented");
    }

    @Test
    public void testCloseNoClient() {
        DmaapProducerImpl producer = new DmaapProducerImpl(hosts, topic, key, secret);
        
        assertNotNull(producer);
        
        producer.close();
    }
    
    @Ignore
    @Test
    public void testCloseWithClient() {
        fail("Not yet implemented");
    }
    
    @Test
    public void testUseHttps() {
        DmaapProducerImpl producer = new DmaapProducerImpl(hosts, topic, key, secret);
        
        assertNotNull(producer);
        
        assertEquals(false,producer.isHttps());
        
        producer.useHttps(true);
        
        assertEquals(true,producer.isHttps());
        
    }

}
