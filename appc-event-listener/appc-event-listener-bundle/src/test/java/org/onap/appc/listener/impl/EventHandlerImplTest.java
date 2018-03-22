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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.listener.impl;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.appc.adapter.message.Consumer;
import org.onap.appc.adapter.message.Producer;
import org.onap.appc.listener.ListenerProperties;

/**
 * Test the ProviderAdapter implementation.
 */

@RunWith(MockitoJUnitRunner.class)
public class EventHandlerImplTest {

    private TestEventHandlerImpl adapter;
    private ListenerProperties properties;

    @Mock
    private Producer mockProducer;
    @Mock
    private Consumer mockConsumer;

    private static final String PROP_FILE = "/org/onap/appc/default.properties";

    private static final String MESSAGE_FILE = "/DCAEResponse.txt";

    /**
     * Setup the test environment.
     */
    @Before
    public void setup() {
        Properties allProps = new Properties();
        try {
            allProps.load(getClass().getResourceAsStream(PROP_FILE));
            allProps.remove("appc.ClosedLoop.topic.read.filter");
            properties = new ListenerProperties("appc.ClosedLoop", allProps);
        } catch (IOException e) {
            System.out.println("WARNING: Failed to load properties file: " + PROP_FILE);
        }
        adapter = new TestEventHandlerImpl(properties);
        adapter.setConsumer(mockConsumer);
        adapter.setProducer(mockProducer);
    }

    @Test
    public void testInitialProperties() {
        assertEquals(properties.getProperty("topic.read"), adapter.getReadTopic());
        assertTrue(adapter.getWriteTopics().contains(properties.getProperty("topic.write")));
        assertEquals(properties.getProperty("client.name"), adapter.getClientName());
        assertEquals(properties.getProperty("client.name.id"), adapter.getClientId());

        String hostStr = properties.getProperty("poolMembers");
        int hostCount = hostStr.length() > 0 ? hostStr.split(",").length : 0;
        assertEquals(hostCount, adapter.getPool().size());
    }

    @Test
    public void testGettersAndSetters() {
        String readTopic = "read";
        Set<String> writeTopic = new HashSet<>();
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

    @Test
    public void getIncomingEvents_should_success_when_no_errors_encountered() {

        List<String> testResult = newArrayList("test-result1", "test-result2", "test-result3");
        when(mockConsumer.fetch(anyInt(), anyInt())).thenReturn(testResult);

        List<String> result = adapter.getIncomingEvents(5);

        for (int i = 0; i < testResult.size(); i++) {
            assertEquals(testResult.get(i), result.get(i));
        }
    }


    @Test
    public void postStatus_should_success_when_no_errors_encountered() {

        adapter.postStatus("test-partition", "test-event");
        verify(mockProducer).post("test-partition", "test-event");

        adapter.postStatus("test-event");
        verify(mockProducer).post(null, "test-event");
    }


    @Test
    public void closeClients_should_close_producer_and_consumer() {
        adapter.getIncomingEvents(5);
        adapter.postStatus("test-partition", "test-event");

        adapter.closeClients();
        verify(mockConsumer).close();
        verify(mockProducer).close();
    }


//    @Test
    public void testRun() {
        EventHandlerImpl adapter = new EventHandlerImpl(properties);

        // Runoff any old data
        List<String> result = adapter.getIncomingEvents();
        assertNotNull(result);

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

    private class TestEventHandlerImpl extends EventHandlerImpl {

        private Consumer mockConsumer;
        private Producer mockProducer;

        private TestEventHandlerImpl(ListenerProperties props) {
            super(props);
        }

        @Override
        protected Consumer getConsumer() {
            return mockConsumer;
        }

        @Override
        protected Producer getProducer() {
            return mockProducer;
        }

        private void setConsumer(Consumer consumer) {
            mockConsumer = consumer;
        }

        private void setProducer(Producer producer) {
            mockProducer = producer;
        }
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
