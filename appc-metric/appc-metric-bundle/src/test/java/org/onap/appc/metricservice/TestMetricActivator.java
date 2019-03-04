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

package org.onap.appc.metricservice;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.appc.MetricActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class TestMetricActivator {

    private MetricActivator metricActivator;
    private BundleContext bundleContext;
    private ServiceRegistration registration;

    @Test
    public void testStart() throws Exception {
        metricActivator = new MetricActivator();
        registration = Mockito.mock(ServiceRegistration.class);
        bundleContext = Mockito.mock(BundleContext.class);
        when(bundleContext.registerService(eq(MetricService.class.getName()), anyObject(), eq(null)))
                .thenReturn(registration);
        metricActivator.start(bundleContext);
        verify(bundleContext, times(1)).registerService(eq(MetricService.class.getName()), anyObject(), eq(null));
    }
}
