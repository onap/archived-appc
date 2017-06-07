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

package org.openecomp.appc;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.test.AbstractDataBrokerTest;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.openecomp.appc.provider.AppcProvider;

/**
 * Defines a unit test class which tests the provider. This class leverages the AbstractDataBrokerTest class which
 * starts a real MD-SAL implementation to use inside of your unit tests. This is not an exhaustive test, but rather is
 * used to illustrate how one can leverage the AbstractDataBrokerTest to test MD-SAL providers/listeners.
 */
public class AppcProviderTest extends AbstractDataBrokerTest {

    private ExecutorService threadPool = Executors.newSingleThreadExecutor();
    private AppcProvider provider;
    private DataBroker dataBroker;

    /**
     * The @Before annotation is defined in the AbstractDataBrokerTest class. The method setupWithDataBroker is invoked
     * from inside the @Before method and is used to initialize the databroker with objects for a test runs. In our case
     * we use this oportunity to create an instance of our provider and initialize it (which registers it as a listener
     * etc). This method runs before every @Test method below.
     */
    @Override
    protected void setupWithDataBroker(DataBroker dataBroker) {
        super.setupWithDataBroker(dataBroker);

        this.dataBroker = dataBroker;
        NotificationProviderService nps = null;
        RpcProviderRegistry registry = null;

        provider = new AppcProvider(dataBroker, nps, registry);
    }

    /**
     * Shuts down our provider, testing close code. @After runs after every @Test method below.
     */
    @After
    public void stop() throws Exception {
        if (provider != null) {
            provider.close();
        }
    }

    /**
     * This validates that when a task is created, the run count is initialized to 0
     */
    @Ignore
    @Test
    public void sampleUnitTest() {
        // This is where you add your unit testing. You can access "DataBroker" as
        // needed to create items etc.
        // This a "Real" data broker.
    }
}
