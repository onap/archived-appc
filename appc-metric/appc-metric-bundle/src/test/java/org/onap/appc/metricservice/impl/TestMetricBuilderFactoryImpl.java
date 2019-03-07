/*-
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

import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.onap.appc.metricservice.metric.impl.MetricBuilderFactoryImpl;

public class TestMetricBuilderFactoryImpl {

    private MetricBuilderFactoryImpl metricBuilderFactoryImpl = new MetricBuilderFactoryImpl();

    @Test
    public void testPrimitiveCounterBuilder() {
        assertNotNull(metricBuilderFactoryImpl.primitiveCounterBuilder());
    }

    @Test
    public void testDispatchingFunctionCounterBuilder() {
        assertNotNull(metricBuilderFactoryImpl.dispatchingFunctionCounterBuilder());
    }

    @Test
    public void testDmaapRequestCounterBuilder() {
        assertNotNull(metricBuilderFactoryImpl.dmaapRequestCounterBuilder());
    }
}
