/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 Ericsson
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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.mdsal.provider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.onap.appc.mdsal.MDSALStore;
import org.onap.appc.mdsal.impl.MDSALStoreFactory;
import org.onap.appc.mdsal.impl.MDSALStoreImpl;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.org.onap.appc.mdsal.store.rev170925.MdsalStoreService;
import org.opendaylight.yang.gen.v1.org.onap.appc.mdsal.store.rev170925.StoreYangInput;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import com.att.eelf.configuration.EELFLogger;
import org.junit.Assert;

@RunWith(PowerMockRunner.class)
@PrepareForTest(MDSALStoreFactory.class)
public class MdsalStoreProviderTest {

    private DataBroker dataBroker = Mockito.mock(DataBroker.class);
    private RpcProviderRegistry rpcRegistry = Mockito.mock(RpcProviderRegistry.class);
    private NotificationPublishService notificationService = Mockito.mock(NotificationPublishService.class);

    @Test
    public void testClose() throws Exception {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        MdsalStoreProvider mdsalStoreProvider = new MdsalStoreProvider(dataBroker, notificationService, rpcRegistry);
        Whitebox.setInternalState(mdsalStoreProvider, "log", mockLogger);
        mdsalStoreProvider.close();
        Mockito.verify(mockLogger).info("Closing provider for MdsalStoreProvider");
        Mockito.verify(mockLogger).info("Successfully closed provider for MdsalStoreProvider");
    }

    @Test
    public void testStoreYang() throws Exception {
        PowerMockito.mockStatic(MDSALStoreFactory.class);
        MDSALStore mdsalStore = Mockito.mock(MDSALStore.class);
        PowerMockito.when(MDSALStoreFactory.createMDSALStore()).thenReturn(mdsalStore);
        MdsalStoreProvider mdsalStoreProvider = new MdsalStoreProvider(dataBroker, notificationService, rpcRegistry);
        StoreYangInput mockInput = Mockito.mock(StoreYangInput.class);
        Assert.assertNotNull(mdsalStoreProvider.storeYang(mockInput));
    }

    @Test
    public void testStoreYangExceptionFlow() throws Exception {
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        MdsalStoreProvider mdsalStoreProvider = new MdsalStoreProvider(dataBroker, notificationService, rpcRegistry);
        Whitebox.setInternalState(mdsalStoreProvider, "log", mockLogger);
        StoreYangInput mockInput = Mockito.mock(StoreYangInput.class);
        mdsalStoreProvider.storeYang(mockInput);
        Mockito.verify(mockLogger).error(Mockito.anyString(), Mockito.any(NullPointerException.class));
    }
}
