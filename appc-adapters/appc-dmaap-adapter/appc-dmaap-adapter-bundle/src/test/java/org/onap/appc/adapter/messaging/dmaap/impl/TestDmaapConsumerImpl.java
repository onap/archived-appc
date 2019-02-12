/* 
 * ============LICENSE_START======================================================= 
 * ONAP : APPC 
 * ================================================================================ 
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved. 
 * ================================================================================
 * Modifications Copyright (C) 2018 IBM
 * ================================================================================
 * Modifications Copyright (C) 2019 Ericsson
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import org.junit.Ignore;
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
import com.att.nsa.mr.client.MRClientFactory;
import com.att.nsa.mr.client.MRConsumer;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ConfigurationFactory.class, FrameworkUtil.class, MRClientFactory.class})
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

    @Ignore("test is taking 130 sec")
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
    public void testGetClient() throws FileNotFoundException, IOException 
    {
        DmaapConsumerImpl consumer = new DmaapConsumerImpl(hosts, topic, group, id, key, secret);
        assertNotNull(consumer);
        PowerMockito.mockStatic(MRClientFactory.class);
        PowerMockito.when(MRClientFactory.createConsumer(Mockito.anyString())).thenReturn(Mockito.mock(MRConsumer.class));
        assertTrue(consumer.getClient(1000,5) instanceof MRConsumer);
        Properties props= consumer.getProperties();
        assertEquals("1000", props.getProperty("timeout"));
        assertEquals("5", props.getProperty("limit"));
    }

    @Test
    public void testGetClientExceptionFlow() throws FileNotFoundException, IOException 
    {
        DmaapConsumerImpl consumer = new DmaapConsumerImpl(hosts, topic, group, id, key, secret);
        assertNotNull(consumer);
        PowerMockito.mockStatic(MRClientFactory.class);
        PowerMockito.when(MRClientFactory.createConsumer(Mockito.anyString())).thenThrow(new IOException());
        assertFalse(consumer.getClient(1000,5) instanceof MRConsumer);
        Properties props= consumer.getProperties();
        assertEquals("1000", props.getProperty("timeout"));
        assertEquals("5", props.getProperty("limit"));
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
        DmaapConsumerImpl consumer = Mockito.spy(new DmaapConsumerImpl(hosts, topic, group, id, key, secret));
        Whitebox.setInternalState(consumer, "configuration", configuration);
        MetricService metricService = Mockito.mock(MetricService.class);
        MetricRegistry metricRegistry = Mockito.mock(MetricRegistry.class);
        MetricBuilderFactory metricBuilderFactory = Mockito.spy(new MetricBuilderFactoryImpl());
        DmaapRequestCounterBuilder builder = Mockito.mock(DmaapRequestCounterBuilder.class);
        DmaapRequestCounterMetric metric = Mockito.mock(DmaapRequestCounterMetric.class);
        Mockito.when(builder.withName(Mockito.anyString())).thenReturn(builder);
        Mockito.when(builder.withType(Mockito.any())).thenReturn(builder);
        Mockito.when(builder.withPublishedMessage(Mockito.anyLong())).thenReturn(builder);
        Mockito.when(builder.withRecievedMessage(Mockito.anyLong())).thenReturn(builder);
        Mockito.when(builder.build()).thenReturn(metric);
        Mockito.when(metricBuilderFactory.dmaapRequestCounterBuilder()).thenReturn(builder);
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
        Mockito.doReturn(metricService).when(consumer).getMetricService();
        consumer.fetch(1, 1);
        Mockito.verify(policy).init();
    }

}
