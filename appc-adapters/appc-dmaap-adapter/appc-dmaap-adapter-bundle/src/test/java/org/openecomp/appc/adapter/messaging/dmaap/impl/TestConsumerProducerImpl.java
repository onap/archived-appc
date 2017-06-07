/*-
 * ============LICENSE_START=======================================================
 * APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Amdocs
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */

package org.openecomp.appc.adapter.messaging.dmaap.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.*;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.appc.adapter.message.Consumer;
import org.openecomp.appc.adapter.message.Producer;
import org.openecomp.appc.adapter.messaging.dmaap.impl.DmaapConsumerImpl;
import org.openecomp.appc.adapter.messaging.dmaap.impl.DmaapProducerImpl;
import org.openecomp.appc.configuration.Configuration;
import org.openecomp.appc.configuration.ConfigurationFactory;

public class TestConsumerProducerImpl {

    private Collection<String> urls;
    private String topicRead;
    private String topicWrite;
    private String group;
    private String groupId;
    private String user;
    private String password;

    @Before
    public void setup() {
    	System.out.println("setup entry...");
//        urls = new HashSet<String>();
//        urls.add("dmaaphost1");
//        urls.add("dmaaphost2");
//        //remove unavailable dmaap instance for build
//        //urls.add("dmaaphost3");
//
//        topicRead = "APPC-UNIT-TEST";
//        topicWrite = "APPC-UNIT-TEST";
//        group = "APPC-CLIENT";
//        groupId = "0";
        Configuration configuration = ConfigurationFactory.getConfiguration();
        List<String> hosts = Arrays.asList(configuration.getProperty("poolMembers").split(","));
        urls = new HashSet<String>(hosts);
        topicRead = configuration.getProperty("topic.read");
        topicWrite = configuration.getProperty("topic.write");
        user = configuration.getProperty("dmaap.appc.username");
        password = configuration.getProperty("dmaap.appc.password");
        group = "APPC-CLIENT";
        groupId = "0";


        runoff();
    }

    /**
     * Test that we can read and write and that the messages come back in order
     */
    @Ignore
    @Test
    public void testWriteRead() {
    	System.out.println("testWriteRead entry...");
        Producer p = new DmaapProducerImpl(urls, topicWrite,user,password);

        String s1 = UUID.randomUUID().toString();
        String s2 = UUID.randomUUID().toString();
        if (p.post("TEST", s1) == false) {
        	// try again - 2nd attempt may succeed if cambria client failed over
        	p.post("TEST", s1);
        }
        if (p.post("TEST", s2) == false) {
        	// try again - 2nd attempt may succeed if cambria client failed over
        	p.post("TEST", s2);
        }

        Consumer c = new DmaapConsumerImpl(urls, topicRead, group, groupId,user,password);
        List<String> out = c.fetch();
        // if fetch is empty, try again - a 2nd attempt may succeed if
        // cambria client has failed over
        if ((out == null) || out.isEmpty()) {
        	out = c.fetch();
        }

        assertNotNull(out);
        assertEquals(2, out.size());
        assertEquals(s1, out.get(0));
        assertEquals(s2, out.get(1));

    }

    /**
     * Test that we can read and write and that the messages come back in order
     */
    @Test
    @Ignore // Https Not support on jenkins server
    public void testWriteReadHttps() {
    	System.out.println("testWriteReadHttps entry...");
        Producer p = new DmaapProducerImpl(urls, topicWrite,user,password);
        p.useHttps(true);

        String s1 = UUID.randomUUID().toString();
        String s2 = UUID.randomUUID().toString();
        if (p.post("TEST", s1) == false) {
        	// try again - 2nd attempt may succeed if cambria client failed over
        	p.post("TEST", s1);
        }
        if (p.post("TEST", s2) == false) {
        	// try again - 2nd attempt may succeed if cambria client failed over
        	p.post("TEST", s2);
        }

        Consumer c = new DmaapConsumerImpl(urls, topicRead, group, groupId,user,password);
        c.useHttps(true);

        List<String> out = c.fetch();
        // if fetch is empty, try again - a 2nd attempt may succeed if
        // cambria client has failed over
        if ((out == null) || out.isEmpty()) {
        	out = c.fetch();
        }

        assertNotNull(out);
        assertEquals(2, out.size());
        assertEquals(s1, out.get(0));
        assertEquals(s2, out.get(1));

    }

    @Test
    @Ignore // requires connection to a live DMaaP server
    public void testBadUrl() {
    	System.out.println("testBadUrl entry...");
        urls.clear();
        urls.add("something.local");

        // Producer p = new DmaapProducerImpl(urls, topicWrite);
        Consumer c = new DmaapConsumerImpl(urls, topicRead, group, groupId,user,password);
        List<String> result = c.fetch(1000, 1000);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @Ignore // requires connection to a live DMaaP server
    public void testAuth() {
    	System.out.println("testAuth entry...");
        Producer p = new DmaapProducerImpl(urls, topicWrite,user,password);
        Consumer c = new DmaapConsumerImpl(urls, topicRead, group, groupId,user,password);

        p.updateCredentials("key", "secret");
        c.updateCredentials("key", "secret");

        // TODO - Do some protected dmaap queries when the apis are updated
    }

    /**
     * Test DMaaP client failover to another server when a bad url is encountered

     */
    @Ignore
    @Test
    public void testFailover() {
    	System.out.println("testFailover entry...");
    	urls.clear();
        urls.add("openecomp2.org");  // bad url
        urls.add("dmaaphost2");
        Producer p = new DmaapProducerImpl(urls, topicWrite,user,password);

        String s1 = UUID.randomUUID().toString();
        if (p.post("TEST", s1) == false) {
        	// try again - cambria client should have failed over
        	p.post("TEST", s1);
        }

        urls.clear();
        urls.add("openecomp3.org");  // bad url
        urls.add("dmaaphost3");
        
        Consumer c = new DmaapConsumerImpl(urls, topicRead, group, groupId,user,password);
        List<String> out = c.fetch(1000, 1000);
        // if fetch is empty, try again - cambria client should have failed over
        if ((out == null) || out.isEmpty()) {
        	out = c.fetch();
        }

        assertNotNull(out);
        assertEquals(1, out.size());
        assertEquals(s1, out.get(0));
    }
    
    /**
     * Reads through the entire topic so it is clean for testing. WARNING - ONLY USE ON TOPICS WHERE YOU ARE THE ONLY
     * WRITER. Could end in an infinite loop otherwise.
     */
    private void runoff() {
        Consumer c = new DmaapConsumerImpl(urls, topicRead, group, groupId,user,password);
        List<String> data;
        do {
            data = c.fetch(1000, 10000);
        } while (!data.isEmpty()  && data.size()!=1);
    }

    @Test
    @Ignore
    public void testFilter() {
    	System.out.println("testFilter entry...");
        List<String> res;
        String filter = "{\"class\":\"Assigned\",\"field\":\"request\"}";
        Consumer c = new DmaapConsumerImpl(urls, "DCAE-CLOSED-LOOP-EVENTS-DEV1510SIM", group, groupId,user,password,filter);
        res = c.fetch(2000, 10);
        assertFalse(res.isEmpty());

        res.clear();
        filter = "{\"class\":\"Assigned\",\"field\":\"response\"}";
        c = new DmaapConsumerImpl(urls, "DCAE-CLOSED-LOOP-EVENTS-DEV1510SIM", group, groupId,user,password, filter);
        res = c.fetch(2000, 10);
        assertTrue(res.isEmpty());
    }
}
