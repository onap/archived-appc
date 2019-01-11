/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 Ericsson
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
 *
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.metricservice.policy.impl;

import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.onap.appc.configuration.Configuration;
import org.onap.appc.configuration.ConfigurationFactory;
import org.onap.appc.metricservice.Publisher;
import org.onap.appc.metricservice.metric.Metric;
import org.onap.appc.metricservice.metric.impl.DmaapRequestCounterMetricImpl;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import com.att.eelf.configuration.EELFLogger;
import org.junit.Assert;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ConfigurationFactory.class)
public class ScheduledPublishingPolicyImplTest {

    private Configuration configuration = Mockito.mock(Configuration.class);

    @Before
    public void setup() {
        PowerMockito.mockStatic(ConfigurationFactory.class);
        PowerMockito.when(ConfigurationFactory.getConfiguration()).thenReturn(configuration);
    }

    @Test
    public void testWithPeriodAndStartTime() {
        Properties properties = new Properties();
        properties.setProperty("schedule.policy.metric.period", "1000");
        properties.setProperty("schedule.policy.metric.start.time", "1000");
        Mockito.when(configuration.getProperties()).thenReturn(properties);
        PolicyBuilderFactoryImpl builderFactory = new PolicyBuilderFactoryImpl();
        ScheduledPolicyBuilderImpl policyBuilder = (ScheduledPolicyBuilderImpl) builderFactory.scheduledPolicyBuilder();
        Metric[] metrics = new Metric[0];
        policyBuilder.withMetrics(metrics);
        ScheduledPublishingPolicyImpl publishingPolicy = (ScheduledPublishingPolicyImpl) policyBuilder.build();
        Assert.assertEquals(0, publishingPolicy.metrics().length);
    }

    @Test
    public void testWithPeriod() {
        Properties properties = new Properties();
        properties.setProperty("schedule.policy.metric.period", "1000");
        Mockito.when(configuration.getProperties()).thenReturn(properties);
        PolicyBuilderFactoryImpl builderFactory = new PolicyBuilderFactoryImpl();
        ScheduledPolicyBuilderImpl policyBuilder = (ScheduledPolicyBuilderImpl) builderFactory.scheduledPolicyBuilder();
        Metric[] metrics = new Metric[0];
        policyBuilder.withMetrics(metrics);
        ScheduledPublishingPolicyImpl publishingPolicy = (ScheduledPublishingPolicyImpl) policyBuilder.build();
        Assert.assertEquals(0, publishingPolicy.metrics().length);
    }

    @Test
    public void testWithNeither() {
        Properties properties = new Properties();
        Mockito.when(configuration.getProperties()).thenReturn(properties);
        PolicyBuilderFactoryImpl builderFactory = new PolicyBuilderFactoryImpl();
        ScheduledPolicyBuilderImpl policyBuilder = (ScheduledPolicyBuilderImpl) builderFactory.scheduledPolicyBuilder();
        Metric[] metrics = new Metric[0];
        policyBuilder.withMetrics(metrics);
        policyBuilder.withPublishers(null);
        ScheduledPublishingPolicyImpl publishingPolicy = (ScheduledPublishingPolicyImpl) policyBuilder.build();
        Assert.assertEquals(0, publishingPolicy.metrics().length);

    }

    @Test
    public void testWithNullProperties() {
        Mockito.when(configuration.getProperties()).thenReturn(null);
        PolicyBuilderFactoryImpl builderFactory = new PolicyBuilderFactoryImpl();
        ScheduledPolicyBuilderImpl policyBuilder = (ScheduledPolicyBuilderImpl) builderFactory.scheduledPolicyBuilder();
        Metric[] metrics = new Metric[0];
        policyBuilder.withMetrics(metrics);
        policyBuilder.withPeriod(100000);
        policyBuilder.withStartTime(1);
        ScheduledPublishingPolicyImpl publishingPolicy = (ScheduledPublishingPolicyImpl) policyBuilder.build();
        Assert.assertEquals(0, publishingPolicy.metrics().length);
        Assert.assertEquals(100000, publishingPolicy.getPeriod());
        Assert.assertEquals(1, publishingPolicy.getStartTime());
    }

    @Test
    public void testConstructorAndInit() {
        Properties properties = new Properties();
        properties.setProperty("schedule.policy.metric.period", "1000");
        properties.setProperty("schedule.policy.metric.start.time", "1000");
        properties.setProperty("metric.enabled" , "true");
        Publisher publisherMock = Mockito.mock(Publisher.class);
        Publisher[] publisherArray = new Publisher[1];
        publisherArray[0] = publisherMock;
        Mockito.when(configuration.getProperties()).thenReturn(properties);
        ScheduledPublishingPolicyImpl publishingPolicy = new ScheduledPublishingPolicyImpl(1000, 1000, publisherArray, new Metric[0]);
        Whitebox.setInternalState(publishingPolicy, "configuration", configuration);
        publishingPolicy.init();
        Assert.assertEquals(1, publishingPolicy.getPublishers().length);
    }

}
