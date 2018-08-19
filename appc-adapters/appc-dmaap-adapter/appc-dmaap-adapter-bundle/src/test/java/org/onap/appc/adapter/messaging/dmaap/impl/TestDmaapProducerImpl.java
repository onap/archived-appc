/* 
 * ============LICENSE_START======================================================= 
 * ONAP : APPC 
 * ================================================================================ 
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved. 
 * ============================================================================= 
 * Modifications Copyright (C) 2018 IBM. 
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
import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class TestDmaapProducerImpl {
    String[] hostList = { "192.168.1.1" };
    Collection<String> hosts = new HashSet<String>(Arrays.asList(hostList));

    String topic = "JunitTopicOne";
    String group = "junit-client";
    String id = "junit-consumer-one";
    String key = "key";
    String secret = "secret";
    String filter = null;

    private DmaapProducerImpl producer;

    @Before
    public void setUp() {
        producer = new DmaapProducerImpl(hosts, topic, null, null);
    }

    @Test
    public void testDmaapProducerImplSingleTopic() {
        producer = new DmaapProducerImpl(hosts, topic, key, secret);

        assertNotNull(producer);

        Properties props = producer.getProperties();

        assertNotNull(props);

        assertEquals("key", props.getProperty("username"));
        assertEquals("secret", props.getProperty("password"));
    }

    @Test
    public void testDmaapProducerImplMultipleTopic() {
        String[] topicList = { "topic1", "topic2" };
        Set<String> topicNames = new HashSet<String>(Arrays.asList(topicList));

        producer = new DmaapProducerImpl(hosts, topicNames, key, secret);

        assertNotNull(producer);

        Properties props = producer.getProperties();

        assertNotNull(props);

        assertEquals("key", props.getProperty("username"));
        assertEquals("secret", props.getProperty("password"));

    }

    @Test
    public void testDmaapProducerImplNoUserPass() {
        producer = new DmaapProducerImpl(hosts, topic, null, null);

        assertNotNull(producer);

        Properties props = producer.getProperties();

        assertNotNull(props);

        assertNull(props.getProperty("username"));
        assertNull(props.getProperty("password"));
    }

    @Test
    public void testUpdateCredentials() {
        producer = new DmaapProducerImpl(hosts, topic, null, null);

        assertNotNull(producer);

        Properties props = producer.getProperties();

        assertNotNull(props);

        assertNull(props.getProperty("username"));
        assertNull(props.getProperty("password"));

        producer.updateCredentials(key, secret);

        props = producer.getProperties();

        assertNotNull(props);

        assertEquals("key", props.getProperty("username"));
        assertEquals("secret", props.getProperty("password"));

    }

    @Test
    public void testPost() {
        boolean successful = producer.post("partition", "data");
        assertEquals(true, successful);
    }

    @Test
    public void testCloseNoClient() {
        producer = new DmaapProducerImpl(hosts, topic, key, secret);

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
        producer = new DmaapProducerImpl(hosts, topic, key, secret);

        assertNotNull(producer);

        assertEquals(false, producer.isHttps());

        producer.useHttps(true);

        assertEquals(true, producer.isHttps());

    }

}
