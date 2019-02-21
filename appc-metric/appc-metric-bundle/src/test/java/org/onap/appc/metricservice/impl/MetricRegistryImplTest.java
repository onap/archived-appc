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
import org.onap.appc.metricservice.metric.Metric;
import org.onap.appc.metricservice.metric.impl.DefaultPrimitiveCounter;

public class MetricRegistryImplTest {

    private static final String NAME = "NAME";
    private MetricRegistryImpl registry;
    @Before
    public void setup() {
        registry = new MetricRegistryImpl(null);
    }

    @Test
    public void testRegister() {
        Metric metric = new DefaultPrimitiveCounter(NAME, null);
        assertNull(registry.counter(NAME));
        assertTrue(registry.register(metric));
        assertFalse(registry.register(metric));
        assertTrue(registry.counter(NAME) instanceof Counter);
        assertSame(metric, registry.metric(NAME));
    }

    @Test
    public void testCounter() {
        registry.dispose();
        assertEquals(0, registry.metrics().length);
    }

}
