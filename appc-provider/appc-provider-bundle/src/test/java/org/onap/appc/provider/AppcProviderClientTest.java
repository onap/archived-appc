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
 *
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.provider;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.Properties;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AppcProviderClient.class, FrameworkUtil.class})
public class AppcProviderClientTest {
    @Mock
    private SvcLogicService svcLogicService;
    @Mock
    private AppcProviderClient appcProviderClient;
/*
    @Before
    public void setUp() throws Exception {
        // Prepare all mocks
        mockStatic(FrameworkUtil.class);
        Bundle mockedBundle = mock(Bundle.class);
        PowerMockito.when(FrameworkUtil.getBundle(SvcLogicService.class)).thenReturn(mockedBundle);

        BundleContext mockedBundleContext = mock(BundleContext.class);
        Mockito.when(mockedBundle.getBundleContext()).thenReturn(mockedBundleContext);

        ServiceReference svcRef = mock(ServiceReference.class);
        Mockito.when(mockedBundleContext.getServiceReference(SvcLogicService.NAME)).thenReturn(svcRef);

        Mockito.when(mockedBundleContext.getService(svcRef)).thenReturn(svcLogicService);

        appcProviderClient = new AppcProviderClient();
    }

    @Test
    public void testNonArgumentConstructor() {
        AppcProviderClient appcProviderClient = new AppcProviderClient();
        Assert.assertEquals(Whitebox.getInternalState(appcProviderClient, "svcLogic"), svcLogicService);
    }

    @Test
    public void hasGraph() throws Exception {
        doReturn(true).when(svcLogicService).hasGraph(any(), any(), any(), any());
        boolean hasGraph = appcProviderClient.hasGraph("test-module", "test-rpc", "test-version", "test-mode");
        Assert.assertTrue(hasGraph);
    }

    @Test
    public void execute() throws Exception {
        Properties properties = new Properties();
        appcProviderClient.execute("test-module", "test-rpc", "test-version", "test-mode", properties);
        verify(svcLogicService, times(1)).execute("test-module", "test-rpc", "test-version", "test-mode",
            properties);
    }
*/
}