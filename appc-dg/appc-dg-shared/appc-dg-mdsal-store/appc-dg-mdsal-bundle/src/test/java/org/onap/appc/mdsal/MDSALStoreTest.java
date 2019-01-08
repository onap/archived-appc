/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications (C) 2019 Ericsson
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

package org.onap.appc.mdsal;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.onap.appc.mdsal.exception.MDSALStoreException;
import org.onap.appc.mdsal.impl.MDSALStoreFactory;
import org.onap.appc.mdsal.impl.MDSALStoreImpl;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.onap.appc.mdsal.operation.ConfigOperationRequestFormatter;
import org.onap.appc.rest.client.RestClientInvoker;
import org.osgi.framework.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.net.URL;

/**
 * MDSALStore Tests
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({FrameworkUtil.class,BundleContext.class,ServiceReference.class,
        BundleReference.class,Bundle.class,Filter.class,BundleListener.class,InvalidSyntaxException.class,
        BundleException.class,FrameworkListener.class,ServiceRegistration.class,ServiceListener.class,
        Version.class})
public class MDSALStoreTest {

    private final EELFLogger logger = EELFManager.getInstance().getLogger(MDSALStoreTest.class);
    private RestClientInvoker client = Mockito.mock(RestClientInvoker.class);
    private ConfigOperationRequestFormatter requestFormatter = new ConfigOperationRequestFormatter();
    private ObjectMapper mapper = new ObjectMapper();
    MDSALStoreImpl store;

    @Before
    public void init() throws Exception{
        PowerMockito.whenNew(RestClientInvoker.class).withArguments(Mockito.any(URL.class)).thenReturn(client);
        store = (MDSALStoreImpl) MDSALStoreFactory.createMDSALStore();
    }

    @Ignore
    public void testYangInput() throws MDSALStoreException {
        store.storeYangModuleOnLeader("module test { }", "Name");
    }

}
