/* 
 * ============LICENSE_START======================================================= 
 * ONAP : APPC 
 * ================================================================================ 
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved. 
 * ============================================================================= 
 * Modifications Copyright (C) 2018 IBM. 
 * =================================================================================
 * Modifications Copyright (C) 2019 Ericsson
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.onap.appc.configuration.Configuration;
import org.onap.appc.configuration.ConfigurationFactory;
import org.onap.appc.metricservice.MetricRegistry;
import org.onap.appc.metricservice.MetricService;
import org.onap.appc.metricservice.metric.DmaapRequestCounterBuilder;
import org.onap.appc.metricservice.metric.DmaapRequestCounterMetric;
import org.onap.appc.metricservice.metric.MetricBuilderFactory;
import org.onap.appc.metricservice.metric.impl.MetricBuilderFactoryImpl;
import org.onap.appc.metricservice.policy.PolicyBuilderFactory;
import org.onap.appc.metricservice.policy.PublishingPolicy;
import org.onap.appc.metricservice.policy.ScheduledPolicyBuilder;
import org.osgi.framework.FrameworkUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import com.att.nsa.mr.client.MRBatchingPublisher;
import com.att.nsa.mr.client.MRClientFactory;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ConfigurationFactory.class, FrameworkUtil.class, MRClientFactory.class})
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
        assertNotNull(producer);

        Properties props = producer.getProperties();

        assertNotNull(props);

        assertNull(props.getProperty("username"));
        assertNull(props.getProperty("password"));
    }

    @Test
    public void testUpdateCredentials() {
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
        producer = new DmaapProducerImpl(hosts, topic, key, secret);
        boolean successful = producer.post("partition", "data");
        assertEquals(true, successful);
    }

    @Test
    public void testCloseNoClient() {
        producer = new DmaapProducerImpl(hosts, topic, key, secret);

        assertNotNull(producer);

        producer.close();
    }

    @Test
    public void testCloseWithClient() {
        producer = new DmaapProducerImpl(hosts, topic, key, secret);
        producer.post("partition", "data");
        assertNotNull(producer);
        producer.close();
    }

    @Test
    public void testUseHttps() {
        producer = new DmaapProducerImpl(hosts, topic, key, secret);

        assertNotNull(producer);

        assertEquals(false, producer.isHttps());

        producer.useHttps(true);

        assertEquals(true, producer.isHttps());

    }

    @Test
    public void testInitMetric() throws FileNotFoundException, IOException 
    {
        Configuration configuration = Mockito.mock(Configuration.class);
        Properties properties = new Properties();
        properties.put("metric.enabled", "true");
        Mockito.when(configuration.getProperties()).thenReturn(properties);
        PowerMockito.mockStatic(MRClientFactory.class);
        PowerMockito.when(MRClientFactory.createConsumer(Mockito.anyString())).thenThrow(new IOException());        
        DmaapProducerImpl producer = Mockito.spy(new DmaapProducerImpl(hosts, topic, key, secret));
        Whitebox.setInternalState(DmaapProducerImpl.class, "configuration", configuration);
        MetricService metricService = Mockito.mock(MetricService.class);
        MetricRegistry metricRegistry = Mockito.mock(MetricRegistry.class);
        MetricBuilderFactory metricBuilderFactory = Mockito.spy(new MetricBuilderFactoryImpl());
        DmaapRequestCounterBuilder builder = Mockito.mock(DmaapRequestCounterBuilder.class);
        DmaapRequestCounterMetric metric = Mockito.mock(DmaapRequestCounterMetric.class);
        Mockito.doNothing().when(metric).incrementPublishedMessage();
        Mockito.when(builder.withName(Mockito.anyString())).thenReturn(builder);
        Mockito.when(builder.withType(Mockito.any())).thenReturn(builder);
        Mockito.when(builder.withPublishedMessage(Mockito.anyLong())).thenReturn(builder);
        Mockito.when(builder.withRecievedMessage(Mockito.anyLong())).thenReturn(builder);
        Mockito.when(builder.build()).thenReturn(metric);
        Mockito.when(metricBuilderFactory.dmaapRequestCounterBuilder()).thenReturn(builder);
        Mockito.when(metricRegistry.metric("DMAAP_KPI")).thenReturn(metric);
        Mockito.when(metricRegistry.register(Mockito.any())).thenReturn(true);
        PublishingPolicy policy = Mockito.mock(PublishingPolicy.class);
        PolicyBuilderFactory policyFactory = Mockito.mock(PolicyBuilderFactory.class);
        Mockito.when(metricRegistry.policyBuilderFactory()).thenReturn(policyFactory);
        ScheduledPolicyBuilder policyBuilder = Mockito.mock(ScheduledPolicyBuilder.class);
        Mockito.when(policyBuilder.withPublishers(Mockito.any())).thenReturn(policyBuilder);
        Mockito.when(policyBuilder.withMetrics(Mockito.any())).thenReturn(policyBuilder);
        Mockito.when(policyBuilder.build()).thenReturn(policy);
        Mockito.when(policyFactory.scheduledPolicyBuilder()).thenReturn(policyBuilder);
        Mockito.when(metricRegistry.metricBuilderFactory()).thenReturn(metricBuilderFactory);
        Mockito.when(metricService.createRegistry("APPC")).thenReturn(metricRegistry);
        Mockito.doReturn(metricService).when(producer).getMetricservice();
        Set<MRBatchingPublisher> clients = new HashSet<>();
        MRBatchingPublisher client = Mockito.mock(MRBatchingPublisher.class);
        clients.add(client);
        Mockito.doReturn(0).when(client).send(Mockito.anyString(), Mockito.anyString());
        Whitebox.setInternalState(producer, "clients", clients);
        producer.post(null, null);
        Mockito.verify(policy).init();
    }

}
