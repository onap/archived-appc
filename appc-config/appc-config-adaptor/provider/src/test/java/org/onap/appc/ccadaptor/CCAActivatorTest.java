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

package org.onap.appc.ccadaptor;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.onap.ccsdk.sli.core.sli.ConfigurationException;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CcAdaptorConstants.class})
public class CCAActivatorTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void testStartNoConfigFile() throws Exception {
        CCAActivator activator = new CCAActivator();
        BundleContext context = Mockito.mock(BundleContext.class);
        expectedEx.expect(ConfigurationException.class);
        expectedEx.expectMessage( "Cannot find config file - " + CcAdaptorConstants.CCA_PROP_FILE_VAR + " and " +
                CcAdaptorConstants.APPC_CONFIG_DIR_VAR + " unset");
        activator.start(context);
    }

    @Test
    public void testStart() throws Exception {
        PowerMockito.mockStatic(CcAdaptorConstants.class);
        PowerMockito.when(CcAdaptorConstants.getEnvironmentVariable(CcAdaptorConstants.APPC_CONFIG_DIR_VAR))
            .thenReturn("src/test/resources");
        CCAActivator activator = new CCAActivator();
        BundleContext context = Mockito.mock(BundleContext.class);
        activator.start(context);
        Mockito.verify(context).registerService(Mockito.anyString(), Mockito.any(), Mockito.any());
    }

    @Test
    public void testStartInvalidFile() throws Exception {
        PowerMockito.mockStatic(CcAdaptorConstants.class);
        PowerMockito.when(CcAdaptorConstants.getEnvironmentVariable(CcAdaptorConstants.APPC_CONFIG_DIR_VAR))
            .thenReturn("INVALID_DIRECTORY");
        CCAActivator activator = new CCAActivator();
        BundleContext context = Mockito.mock(BundleContext.class);
        expectedEx.expect(ConfigurationException.class);
        expectedEx.expectMessage("Missing configuration properties file: ");
        activator.start(context);
    }

    @Test
    public void testStop() throws Exception {
        CCAActivator activator = new CCAActivator();
        ServiceRegistration registration = Mockito.mock(ServiceRegistration.class);
        Whitebox.setInternalState(activator, "registration", registration);
        activator.stop(Mockito.mock(BundleContext.class));
        Mockito.verify(registration).unregister();
    }

}
