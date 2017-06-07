/*-
 * ============LICENSE_START=======================================================
 * APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Amdocs
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
 * ============LICENSE_END=========================================================
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */

package org.openecomp.appc.dg.common.impl;

import org.openecomp.appc.dg.common.VNFConfigurator;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.mdsal.MDSALStore;
import org.openecomp.appc.mdsal.impl.MDSALStoreFactory;
import org.openecomp.appc.mdsal.impl.MDSALStoreImpl;
import org.openecomp.appc.mdsal.objects.BundleInfo;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest({MDSALStoreImpl.class,MDSALStoreFactory.class})
public class TestVNFConfiguratorImpl {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(TestVNFConfiguratorImpl.class);

    @Before
    public void setUp() {
        logger.setLevel(EELFLogger.Level.DEBUG);
    }

    @Test(expected = APPCException.class)
    public void testValidations() throws APPCException {
        VNFConfigurator configurator = new VNFConfiguratorImpl();
        Map<String,String> params = new HashMap<String, String>();
        params.put("uniqueId","uniqueId");
        params.put("yang","yang");
        params.put("configJSON","configJSON");
        configurator.storeConfig(params,new SvcLogicContext());
    }

    @Test
    public void testYangPresentScenario() throws APPCException {

        VNFConfigurator configurator = new VNFConfiguratorImpl();
        PowerMockito.mockStatic(MDSALStoreFactory.class);
        MDSALStore mdsalStore = PowerMockito.mock(MDSALStoreImpl.class);
        PowerMockito.when(MDSALStoreFactory.createMDSALStore()).thenReturn(mdsalStore);
        PowerMockito.when(mdsalStore.isModulePresent(Matchers.anyString(),(Date) Matchers.anyObject())).thenReturn(true);

        Map<String,String> params = new HashMap<String, String>();
        params.put("uniqueId","uniqueId");
        params.put("yang","yang");
        params.put("configJSON","configJSON");
        params.put("requestId","requestId");
        configurator.storeConfig(params,new SvcLogicContext());
    }

    @Test
    public void testYangAbsentScenario() throws Exception {

        VNFConfigurator configurator = new VNFConfiguratorImpl();
        PowerMockito.mockStatic(MDSALStoreFactory.class);

        MDSALStore mdsalStore = PowerMockito.mock(MDSALStoreImpl.class);

        PowerMockito.when(MDSALStoreFactory.createMDSALStore()).thenReturn(mdsalStore);

        PowerMockito.when(mdsalStore.isModulePresent(Matchers.anyString(),(Date) Matchers.anyObject())).thenReturn(false);

        PowerMockito.doNothing().when(mdsalStore).storeYangModule(Matchers.anyString(),(BundleInfo)Matchers.anyObject());

        Map<String,String> params = new HashMap<>();
        params.put("uniqueId","uniqueId");
        params.put("yang","yang");
        params.put("configJSON","configJSON");
        params.put("requestId","requestId");
        configurator.storeConfig(params,new SvcLogicContext());

    }
}
