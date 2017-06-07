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

package org.openecomp.appc.listener.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.appc.listener.EventHandler;
import org.openecomp.appc.listener.ListenerProperties;
import org.openecomp.appc.listener.impl.EventHandlerImpl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Test the ProviderAdapter implementation.
 *
 */

public class TestEventHandler {

    private ListenerProperties prop;

    private EventHandler adapter;

    private static final String PROP_FILE = "/org/openecomp/appc/default.properties";

    private static final String MESSAGE_FILE = "/DCAEResponse.txt";

    /**
     * Setup the test environment.
     * 
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws NoSuchFieldException
     */
    @Before
    public void setup() {
        Properties allProps = new Properties();
        try {
            allProps.load(getClass().getResourceAsStream(PROP_FILE));
            allProps.remove("appc.ClosedLoop.topic.read.filter");
            prop = new ListenerProperties("appc.ClosedLoop", allProps);
        } catch (IOException e) {
            System.out.println("WARNING: Failed to load properties file: " + PROP_FILE);
        }
        adapter = new EventHandlerImpl(prop);
    }
    
    @Test
    public void testInitialProperties() {
        assertEquals(prop.getProperty("topic.read"), adapter.getReadTopic());
        assertTrue(adapter.getWriteTopics().contains(prop.getProperty("topic.write")));
        assertEquals(prop.getProperty("client.name"), adapter.getClientName());
        assertEquals(prop.getProperty("client.name.id"), adapter.getClientId());

        String hostStr = prop.getProperty("poolMembers");
        int hostCount = hostStr.length()>0 ? hostStr.split(",").length : 0;
        assertEquals(hostCount, adapter.getPool().size());
    }

    @Test
    public void testGettersAndSetters() {
        String readTopic = "read";
        Set<String> writeTopic = new HashSet<String>();
        writeTopic.add("write");
        String clientName = "APPC-TEST";
        String clientId = "00";
        String newHost = "google.com";

        adapter.setReadTopic(readTopic);
        assertEquals(readTopic, adapter.getReadTopic());

        adapter.setWriteTopics(writeTopic);
        assertEquals(writeTopic, adapter.getWriteTopics());

        adapter.setClientName(clientName);
        assertEquals(clientName, adapter.getClientName());

        adapter.setClientId(clientId);
        assertEquals(clientId, adapter.getClientId());

        adapter.setCredentials("fake", "secret");
        adapter.clearCredentials();

        int oldSize = adapter.getPool().size();
        adapter.addToPool(newHost);
        assertEquals(oldSize + 1, adapter.getPool().size());
        assertTrue(adapter.getPool().contains(newHost));

        adapter.removeFromPool(newHost);
        assertEquals(oldSize, adapter.getPool().size());
        assertFalse(adapter.getPool().contains(newHost));

    }

//    @Test
    public void testRun() {
        // Runoff any old data
        List<String> result1 = adapter.getIncomingEvents();
        assertNotNull(result1);

        // Post new data
        DummyObj data = new DummyObj();
        data.key = "value";
        adapter.postStatus(data.toJson());

        // Wait to account for network delay
        sleep(2000);

        // Get data back
        List<DummyObj> result2 = adapter.getIncomingEvents(DummyObj.class);
        assertNotNull(result2);
//        assertEquals(1, result2.size());
        assertEquals(data.toJson(), result2.get(0).toJson());
    }

    @JsonSerialize
    public static class DummyObj implements Serializable {
        @JsonProperty("request") // Call request for default filter
        public String key;

        public DummyObj() {
        }

        public String toJson() {
            return String.format("{\"request\": \"%s\"}", key);
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception e) {
            return;
        }
    }
}
