/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 Ericsson. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.flow.controller;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(FrameworkUtil.class)
public class TestFlowControllerActivator {

    private FlowControllerActivator flowControllerActivator;
    private Bundle bundle;
    private BundleContext ctx;
    private ServiceRegistration registration = null;

    @Before
    public void setUp() throws Exception {
        flowControllerActivator = new FlowControllerActivator();
        ctx = Mockito.mock(BundleContext.class);
        PowerMockito.mockStatic(FrameworkUtil.class);
        bundle = Mockito.mock(Bundle.class);
        PowerMockito.when(FrameworkUtil.getBundle(SvcLogicService.class)).thenReturn(bundle);
        PowerMockito.when(bundle.getBundleContext()).thenReturn(ctx);
        registration = Mockito.mock(ServiceRegistration.class);
        when(ctx.registerService(anyString(), anyObject(), eq(null))).thenReturn(registration);
    }

    @Test
    public void testStart() {
        flowControllerActivator.start(ctx);
        List<ServiceRegistration> registrations =
                (List<ServiceRegistration>) Whitebox.getInternalState(flowControllerActivator, "registrations");
        assertNotNull(registrations.get(0));
    }

    @Test
    public void testStartException() {
        flowControllerActivator.start(null);
        List<ServiceRegistration> registrations =
                (List<ServiceRegistration>) Whitebox.getInternalState(flowControllerActivator, "registrations");
        assertTrue(registrations.isEmpty());
    }

    @Test
    public void testStop() {
        flowControllerActivator.stop(ctx);
        List<ServiceRegistration> registrations =
                (List<ServiceRegistration>) Whitebox.getInternalState(flowControllerActivator, "registrations");
        assertTrue(registrations.isEmpty());
    }

}
