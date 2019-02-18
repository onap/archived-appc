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

package org.onap.appc.design.services;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.RpcRegistration;
import org.powermock.reflect.Whitebox;

public class DesignServiceProviderTest {

    private DataBroker dataBroker;
    private RpcProviderRegistry rpcProviderRegistry;
    private RpcRegistration serviceRegistration;

    @Before
    public void setup() {
        dataBroker = Mockito.mock(DataBroker.class);
        rpcProviderRegistry = Mockito.mock(RpcProviderRegistry.class);
        serviceRegistration = Mockito.mock(RpcRegistration.class);
    }

    @Test
    public void test() {
        DesignServiceProvider provider = new DesignServiceProvider(dataBroker, rpcProviderRegistry);
        provider.init();
        Whitebox.setInternalState(provider, "serviceRegistration", serviceRegistration);
        provider.close();
        Mockito.verify(serviceRegistration).close();
    }

}
