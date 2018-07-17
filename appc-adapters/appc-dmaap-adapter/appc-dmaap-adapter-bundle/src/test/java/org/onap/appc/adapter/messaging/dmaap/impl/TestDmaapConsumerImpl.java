/* 
 * ============LICENSE_START======================================================= 
 * ONAP : APPC 
 * ================================================================================ 
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved. 
 * ================================================================================
 * Modifications Copyright (C) 2018 IBM
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
 */

package org.onap.appc.adapter.messaging.dmaap.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;

import org.junit.Ignore;
import org.junit.Test;


public class TestDmaapConsumerImpl {
    String[]           hostList = { "192.168.1.1" };
    Collection<String> hosts    = new HashSet<String>(Arrays.asList(hostList));

    String             topic    = "JunitTopicOne";
    String             group    = "junit-client";
    String             id       = "junit-consumer-one";
    String             key      = "key";
    String             secret   = "secret";
    String             filter   = null;

    @Test
    public void testDmaapConsumerImplNoFilter() {

        DmaapConsumerImpl consumer = new DmaapConsumerImpl(hosts, topic, group, id, key, secret);

        assertNotNull(consumer);

        Properties props = consumer.getProperties();

        assertEquals("192.168.1.1", props.getProperty("host"));
        assertEquals("key", props.getProperty("username"));
        assertEquals("secret", props.getProperty("password"));
    }

    @Test
    public void testDmaapConsumerImplwithFilter() {

    	filter="";
        DmaapConsumerImpl consumer = new DmaapConsumerImpl(hosts, topic, group, id, key, secret, filter);

        assertNotNull(consumer);

    }

    @Test
    public void testDmaapConsumerImplNoUserPassword() {

        DmaapConsumerImpl consumer = new DmaapConsumerImpl(hosts, topic, group, id, null, null);

        assertNotNull(consumer);

        Properties props = consumer.getProperties();

        assertEquals("192.168.1.1", props.getProperty("host"));
        assertNull(props.getProperty("username"));
        assertNull(props.getProperty("password"));
        assertEquals("HTTPNOAUTH", props.getProperty("TransportType"));
    }

    @Test
    public void testUpdateCredentials() {
        DmaapConsumerImpl consumer = new DmaapConsumerImpl(hosts, topic, group, id, null, null);

        assertNotNull(consumer);

        Properties props = consumer.getProperties();

        assertEquals("192.168.1.1", props.getProperty("host"));
        assertNull(props.getProperty("username"));
        assertNull(props.getProperty("password"));

        consumer.updateCredentials(key, secret);

        props = consumer.getProperties();
        assertEquals("192.168.1.1", props.getProperty("host"));
        assertEquals("key", props.getProperty("username"));
        assertEquals("secret", props.getProperty("password"));
    }

    
    @Test
    public void testFetch() {
    	DmaapConsumerImpl consumer = new DmaapConsumerImpl(hosts, topic, group, id, key, secret);

        assertNotNull(consumer);
        
        consumer.fetch(5000,500);
    }

    @Ignore
    @Test
    public void testFetchIntInt() {
        fail("Not yet implemented");
    }

    @Test
    public void testCloseNoClient() {
        DmaapConsumerImpl consumer = new DmaapConsumerImpl(hosts, topic, group, id, key, secret);

        assertNotNull(consumer);

        consumer.close();
    }

    @Ignore
    @Test
    public void testCloseWithClient() {
        fail("Not yet implemented");
    }

    @Test
    public void testToString() {
        DmaapConsumerImpl consumer = new DmaapConsumerImpl(hosts, topic, group, id, null, null);

        assertNotNull(consumer);

        assertEquals("Consumer junit-client/junit-consumer-one listening to JunitTopicOne on [192.168.1.1]",
                consumer.toString());
    }

    @Test
    public void testUseHttps() {
        DmaapConsumerImpl consumer = new DmaapConsumerImpl(hosts, topic, group, id, key, secret);

        assertNotNull(consumer);

        assertEquals(false, consumer.isHttps());

        consumer.useHttps(true);

        assertEquals(true, consumer.isHttps());

    }
    
    @Test
    public void testGetClient() 
    {
    	DmaapConsumerImpl consumer = new DmaapConsumerImpl(hosts, topic, group, id, key, secret);
    	assertNotNull(consumer);    
    	consumer.getClient(1000,5);
    	Properties props= consumer.getProperties();
    	assertEquals("1000", props.getProperty("timeout"));
    	assertEquals("5", props.getProperty("limit"));
    }
    
    @Test
    public void testInitMetric() 
    {
    	DmaapConsumerImpl consumer = new DmaapConsumerImpl(hosts, topic, group, id, key, secret);
    	assertNotNull(consumer); 
    	
    }
}
