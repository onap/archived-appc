/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 AT&T. All rights reserved.
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

package org.onap.appc.adapter.iaas;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.only;

import java.util.Dictionary;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.appc.adapter.iaas.impl.ProviderAdapterImpl;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

@RunWith(MockitoJUnitRunner.class)
public class AppcProviderAdapterActivatorTest {

    @Mock
    private ServiceRegistration serviceRegistration;
    @Mock
    private BundleContext bundleContext;

    private AppcProviderAdapterActivator appcProviderAdapterActivator = new AppcProviderAdapterActivator();

    @Before
    public void setUp() {
        given(bundleContext.registerService(eq(ProviderAdapter.class), isA(ProviderAdapterImpl.class), isNull(
            Dictionary.class))).willReturn(serviceRegistration);
    }

    @Test
    public void start_shouldRegisterService_whenRegistrationOccursForTheFirstTime() throws Exception {
        registerService();

        then(bundleContext).should(only())
            .registerService(eq(ProviderAdapter.class), isA(ProviderAdapterImpl.class), isNull(
                Dictionary.class));
    }

    @Test
    public void start_shouldRegisterServiceOnlyOnce_whenServiceRegistrationIsNotNull() throws Exception {
        // GIVEN
        registerService();

        // WHEN
        registerService();

        // THEN
        then(bundleContext).should(only()).registerService(eq(ProviderAdapter.class), isA(ProviderAdapterImpl.class), isNull(
            Dictionary.class));
    }

    @Test
    public void stop_shouldUnregisterService_whenServiceRegistrationObjectIsNotNull() throws Exception {
        // GIVEN
        registerService();

        // WHEN
        unregisterService();

        // THEN
        then(serviceRegistration).should().unregister();
    }

    @Test
    public void stop_shouldNotAttemptToUnregisterService_whenServiceHasAlreadyBeenUnregistered()
        throws Exception {
        // GIVEN
        registerService();
        unregisterService();

        // WHEN
        unregisterService();

        // THEN
        then(serviceRegistration).should(only()).unregister();
    }

    private void registerService() throws Exception {
        appcProviderAdapterActivator.start(bundleContext);
    }

    private void unregisterService() throws Exception {
        appcProviderAdapterActivator.stop(bundleContext);
    }
}