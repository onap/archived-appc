/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 Ericsson
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

package org.onap.appc.adapter.messaging.dmaap.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.onap.appc.adapter.message.MessageDestination;
import org.onap.appc.adapter.message.Producer;
import org.onap.appc.adapter.message.event.EventHeader;
import org.onap.appc.adapter.message.event.EventMessage;
import org.onap.appc.configuration.Configuration;
import org.onap.appc.configuration.ConfigurationFactory;
import org.onap.appc.exceptions.APPCException;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ConfigurationFactory.class)
public class EventSenderDmaapImplTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void setup() {
        Configuration configuration = Mockito.mock(Configuration.class);
        Properties properties = new Properties();
        properties.put(MessageDestination.DCAE + "." + EventSenderDmaapImpl.EVENT_POOL_MEMBERS, "host1,host2");
        Mockito.when(configuration.getProperties()).thenReturn(properties);
        PowerMockito.mockStatic(ConfigurationFactory.class);
        PowerMockito.when(ConfigurationFactory.getConfiguration()).thenReturn(configuration);
    }

    @Test
    public void testInit() {
        EventSenderDmaapImpl sender = new EventSenderDmaapImpl();
        sender.initialize();
        assertEquals(1, sender.getProducerMap().size());
    }

    @Test
    public void testSendEvent() {
        EventSenderDmaapImpl sender = new EventSenderDmaapImpl();
        EventMessage eventMessage = Mockito.mock(EventMessage.class);
        EventHeader eventHeader = Mockito.mock(EventHeader.class);
        Mockito.when(eventHeader.getEventId()).thenReturn("EVENT_ID");
        Mockito.when(eventMessage.getEventHeader()).thenReturn(eventHeader);
        assertTrue(sender.sendEvent(MessageDestination.DCAE, eventMessage, "TOPIC NAME"));
    }

    @Test
    public void testSendEventSvcLogicContext() throws APPCException {
        EventSenderDmaapImpl sender = new EventSenderDmaapImpl();
        expectedEx.expect(APPCException.class);
        expectedEx.expectMessage("Missing input parameters: ");
        sender.sendEvent(MessageDestination.DCAE, new HashMap<String, String>(), new SvcLogicContext());
    }

    @Test
    public void testSendEventSvcLogicContextNullParams() throws APPCException {
        EventSenderDmaapImpl sender = new EventSenderDmaapImpl();
        expectedEx.expect(APPCException.class);
        expectedEx.expectMessage("Parameters map is empty (null)");
        sender.sendEvent(MessageDestination.DCAE, null, new SvcLogicContext());
    }

    @Test
    public void testSendEventSvcLogicContextWithParams() throws APPCException {
        EventSenderDmaapImpl sender = new EventSenderDmaapImpl();
        Map<String, String> params = new HashMap<>();
        params.put("apiVer", "apiVer");
        params.put("eventId", "eventId");
        params.put("reason", "reason");
        params.put("entityId", "entityId");
        Producer producer = Mockito.mock(Producer.class);
        Mockito.when(producer.post(Mockito.anyString(), Mockito.anyString())).thenReturn(false);
        Map<String, Producer> producerMap = new HashMap<>();
        producerMap.put(MessageDestination.DCAE.toString(), producer);
        sender.setProducerMap(producerMap);
        assertFalse(sender.sendEvent(MessageDestination.DCAE, params, new SvcLogicContext()));
    }
}
