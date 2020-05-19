/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.dg.common.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.onap.appc.dg.common.VNFConfigurator;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.mdsal.MDSALStore;
import org.onap.appc.mdsal.impl.MDSALStoreFactory;
import org.onap.appc.mdsal.impl.MDSALStoreImpl;
import org.onap.appc.mdsal.objects.BundleInfo;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import static org.junit.Assert.assertNotNull;

@RunWith(PowerMockRunner.class)
@PrepareForTest({MDSALStoreImpl.class, MDSALStoreFactory.class})
public class TestVNFConfiguratorImpl {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(TestVNFConfiguratorImpl.class);

    @Before
    public void setUp() {
        logger.setLevel(EELFLogger.Level.DEBUG);
    }

    @Test(expected = APPCException.class)
    public void testValidations() throws APPCException {
        VNFConfigurator configurator = new VNFConfiguratorImpl();
        Map<String, String> params = new HashMap<String, String>();
        params.put("uniqueId", "uniqueId");
        params.put("yang", "yang");
        params.put("configJSON", "configJSON");
        configurator.storeConfig(params, new SvcLogicContext());
    }

    @Test
    public void testYangPresentScenario() throws APPCException {

        VNFConfigurator configurator = new VNFConfiguratorImpl();
        PowerMockito.mockStatic(MDSALStoreFactory.class);
        MDSALStore mdsalStore = PowerMockito.mock(MDSALStoreImpl.class);
        PowerMockito.when(MDSALStoreFactory.createMDSALStore()).thenReturn(mdsalStore);
        PowerMockito.when(mdsalStore.isModulePresent(Matchers.anyString(), (Date) Matchers.anyObject()))
                .thenReturn(true);

        Map<String, String> params = new HashMap<String, String>();
        params.put("uniqueId", "uniqueId");
        params.put("yang", "yang");
        params.put("configJSON", "configJSON");
        params.put("requestId", "requestId");
        configurator.storeConfig(params, new SvcLogicContext());
        assertNotNull(configurator);
    }

    @Test
    public void testYangAbsentScenario() throws Exception {

        VNFConfigurator configurator = new VNFConfiguratorImpl();
        PowerMockito.mockStatic(MDSALStoreFactory.class);

        MDSALStore mdsalStore = PowerMockito.mock(MDSALStoreImpl.class);

        PowerMockito.when(MDSALStoreFactory.createMDSALStore()).thenReturn(mdsalStore);

        PowerMockito.when(mdsalStore.isModulePresent(Matchers.anyString(), (Date) Matchers.anyObject()))
                .thenReturn(false);

        PowerMockito.doNothing()
                .when(mdsalStore).storeYangModule(Matchers.anyString(), (BundleInfo) Matchers.anyObject());

        Map<String, String> params = new HashMap<String, String>();
        params.put("uniqueId", "uniqueId");
        params.put("yang", "yang");
        params.put("configJSON", "configJSON");
        params.put("requestId", "requestId");
        configurator.storeConfig(params, new SvcLogicContext());
        assertNotNull(configurator);

    }
}
