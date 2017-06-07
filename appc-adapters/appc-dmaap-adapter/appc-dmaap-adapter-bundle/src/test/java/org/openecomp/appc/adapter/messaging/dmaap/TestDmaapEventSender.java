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

package org.openecomp.appc.adapter.messaging.dmaap;

import org.openecomp.sdnc.sli.SvcLogicContext;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.openecomp.appc.adapter.message.MessageDestination;
import org.openecomp.appc.adapter.message.Producer;
import org.openecomp.appc.adapter.message.event.EventHeader;
import org.openecomp.appc.adapter.message.event.EventMessage;
import org.openecomp.appc.adapter.message.event.EventStatus;
import org.openecomp.appc.adapter.messaging.dmaap.impl.DmaapProducerImpl;
import org.openecomp.appc.adapter.messaging.dmaap.impl.EventSenderDmaapImpl;
import org.openecomp.appc.configuration.Configuration;
import org.openecomp.appc.configuration.ConfigurationFactory;
import org.openecomp.appc.exceptions.APPCException;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


public class TestDmaapEventSender {

    private static Properties props;
    private static Map<String,Producer> producerMap = new HashMap<>();
    private static EventMessage eventMessage;

    @BeforeClass
    public static void setUp() {

        Configuration configuration = ConfigurationFactory.getConfiguration(); // test.properties file placed in home dir.

        props = new Properties();
        props.setProperty(EventSenderDmaapImpl.EVENT_POOL_MEMBERS,
                configuration.getProperty(EventSenderDmaapImpl.EVENT_POOL_MEMBERS) != null ?
                configuration.getProperty(EventSenderDmaapImpl.EVENT_POOL_MEMBERS) : "member1,member2,member3");
        props.setProperty(EventSenderDmaapImpl.EVENT_TOPIC_WRITE,
                configuration.getProperty(EventSenderDmaapImpl.EVENT_TOPIC_WRITE) != null ?
                configuration.getProperty(EventSenderDmaapImpl.EVENT_TOPIC_WRITE) : "topic1");

        String eventClientKey = configuration.getProperty(EventSenderDmaapImpl.DMAAP_USERNAME);
        if (eventClientKey != null) {
            props.setProperty(EventSenderDmaapImpl.DMAAP_USERNAME,eventClientKey);
        }
        String eventClientSecret = configuration.getProperty(EventSenderDmaapImpl.DMAAP_PASSWORD);
        if (eventClientSecret != null) {
            props.setProperty(EventSenderDmaapImpl.DMAAP_PASSWORD, eventClientSecret);
        }

        Producer producer = Mockito.mock(DmaapProducerImpl.class);
        producerMap.put(MessageDestination.DCAE.toString(),producer);
        Mockito.when(producer.post(Matchers.anyString(), Matchers.anyString())).thenReturn(true);

        eventMessage = new EventMessage(
                new EventHeader("2016-03-15T10:59:33.79Z", "1.01", "17"),
                new EventStatus(404, "No krokodil found"));
    }

    @Test
    @Ignore // requires connection to a live DMaaP server
    public void testDmaapEventSenderWithProperties() {
        EventSenderDmaapImpl eventSender = new EventSenderDmaapImpl();
        eventSender.initialize();
        eventSender.setProducerMap(producerMap);
        Assert.assertTrue(eventSender.sendEvent(MessageDestination.DCAE, eventMessage));
    }

    @Test
    public void testDmaapEventSenderWithNullProperties() {
        EventSenderDmaapImpl eventSender = new EventSenderDmaapImpl();
//        eventSender.initialize();
        eventSender.setProducerMap(producerMap);
        Assert.assertTrue(eventSender.sendEvent(MessageDestination.DCAE, eventMessage));
    }

    /*
     * This test runs agains a real Dmaap (or a simulator) that should be cofigured in test.properties file.
     */
    @Test
    @Ignore // requires connection to a live DMaaP server
    public void testDmaapEventSenderWithDmaapSim() {
        EventSenderDmaapImpl eventSender = new EventSenderDmaapImpl();
        eventSender.initialize();
        Assert.assertTrue(eventSender.sendEvent(MessageDestination.DCAE, eventMessage));
    }


    @Test
    @Ignore // requires connection to a live DMaaP server
    public void testDmaapEventSenderDG() throws APPCException {
        EventSenderDmaapImpl eventSender = new EventSenderDmaapImpl();
        eventSender.initialize();
        eventSender.setProducerMap(producerMap);
        Map<String,String> params = new HashMap<>();

        params.put("eventTime", eventMessage.getEventHeader().getEventTime());
        params.put("apiVer", eventMessage.getEventHeader().getApiVer());
        params.put("eventId", eventMessage.getEventHeader().getEventId());
        params.put("reason", eventMessage.getEventStatus().getReason());
        params.put("code", "200");

        Assert.assertTrue(eventSender.sendEvent(MessageDestination.DCAE,params, new SvcLogicContext()));
    }

    @Test(expected = APPCException.class)
    @Ignore // requires connection to a live DMaaP server
    public void testDmaapEventSenderDGNoParams() throws APPCException {
        EventSenderDmaapImpl eventSender = new EventSenderDmaapImpl();
        eventSender.initialize();
        eventSender.setProducerMap(producerMap);
        Map<String,String> params = new HashMap<>();

        Assert.assertFalse(eventSender.sendEvent(MessageDestination.DCAE,params, new SvcLogicContext()));
    }


    @Test(expected = APPCException.class)
    @Ignore // requires connection to a live DMaaP server
    public void testDmaapEventSenderDGNullParam() throws APPCException {
        EventSenderDmaapImpl eventSender = new EventSenderDmaapImpl();
        eventSender.initialize();
        eventSender.setProducerMap(producerMap);
        Map<String,String> params = null;

        Assert.assertFalse(eventSender.sendEvent(MessageDestination.DCAE,params, new SvcLogicContext()));
    }

    @Test(expected = APPCException.class)
    @Ignore // requires connection to a live DMaaP server
    public void testDmaapEventSenderDGNoParam() throws APPCException {
        EventSenderDmaapImpl eventSender = new EventSenderDmaapImpl();
        eventSender.initialize();
        eventSender.setProducerMap(producerMap);
        Map<String,String> params = new HashMap<>();

//        params.put("apiVer", eventMessage.getEventHeader().getApiVer());
        params.put("eventId", eventMessage.getEventHeader().getEventId());
        params.put("reason", eventMessage.getEventStatus().getReason());
        params.put("code", "200");

        eventSender.sendEvent(MessageDestination.DCAE,params, new SvcLogicContext());
    }

}
