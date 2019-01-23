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

package org.onap.appc.interfaces.service;

import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.opendaylight.yang.gen.v1.org.onap.appc.interfaces.service.rev170818.InterfacesServiceService;
import org.powermock.reflect.Whitebox;

public class InterfacesServiceProviderTest {

    private DataBroker dataBroker = Mockito.mock(DataBroker.class);
    private RpcProviderRegistry rpcRegistry = Mockito.mock(RpcProviderRegistry.class);
    private RpcRegistration <InterfacesServiceService> serviceRegistration = (RpcRegistration <InterfacesServiceService>) Mockito.mock(RpcRegistration.class);
    private InterfacesServiceProvider provider;

    @Before
    public void setup() {
        provider = new InterfacesServiceProvider(dataBroker, rpcRegistry);
        Mockito.doReturn(serviceRegistration).when(rpcRegistry).addRpcImplementation(Mockito.any(), Mockito.any());
    }

    @Test
    public void testInit() {
        provider.init();
        RpcRegistration <InterfacesServiceService> serviceRegistration = Whitebox.getInternalState(provider, "serviceRegistration");
        assertNotNull(serviceRegistration);
    }

    @Test
    public void testClose() {
        provider.init();
        provider.close();
        Mockito.verify(serviceRegistration).close();
    }

}
