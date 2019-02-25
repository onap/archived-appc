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

package org.onap.appc.metricservice.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.onap.appc.metricservice.metric.Counter;
import org.onap.appc.metricservice.metric.MetricType;
import org.onap.appc.metricservice.metric.impl.DefaultPrimitiveCounter;
import org.onap.appc.metricservice.metric.impl.DispatchingFuntionMetricImpl;

public class MetricRegistryImplTest {

    private static final String METRIC_NAME = "METRIC_NAME";
    private static final String COUNTER_NAME = "COUNTER_NAME";
    private MetricRegistryImpl registry;
    private DispatchingFuntionMetricImpl metric;
    private DefaultPrimitiveCounter counter;

    @Before
    public void setup() {
        registry = new MetricRegistryImpl(null);
        counter = new DefaultPrimitiveCounter(COUNTER_NAME, null);
        metric = new DispatchingFuntionMetricImpl(METRIC_NAME, MetricType.COUNTER, 0, 0);

    }

    @Test
    public void testRegister() {
        assertEquals(0, registry.counters().length);
        assertEquals(0, registry.metrics().length);
        assertNull(registry.counter(COUNTER_NAME));
        assertTrue(registry.register(counter));
        assertFalse(registry.register(counter));
        assertTrue(registry.counter(COUNTER_NAME) instanceof Counter);
        assertSame(counter, registry.counter(COUNTER_NAME));
        assertTrue(registry.register(metric));
        assertSame(metric, registry.metric(METRIC_NAME));
        assertEquals(1, registry.counters().length);
        assertEquals(2, registry.metrics().length);
    }

    @Test
    public void testDispose() {
        registry.register(metric);
        registry.register(counter);
        assertEquals(2, registry.metrics().length);
        registry.dispose();
        assertEquals(0, registry.metrics().length);
    }

}
