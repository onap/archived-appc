/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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
package org.onap.appc.adapter.iaas.provider.operation.impl;

import org.junit.Assert;
import org.junit.Test;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.inOrder;
import org.mockito.InOrder;
import com.att.cdp.zones.model.Server;
import com.att.cdp.zones.model.Server.Status;
import com.att.cdp.exceptions.ZoneException;
import org.onap.appc.adapter.iaas.provider.operation.impl.RebuildServer;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.configuration.Configuration;
import org.onap.appc.configuration.ConfigurationFactory;


public class TestRebuildServer {
    protected static final Configuration configuration = ConfigurationFactory.getConfiguration();

    @Test
    public void rebuildServerRunning() throws ZoneException {
        MockGenerator mg = new MockGenerator(Status.RUNNING);
        Server server = mg.getServer();
        RebuildServer rbs = new RebuildServer();
        rbs.setProviderCache(mg.getProviderCacheMap());
        rbs.setRebuildSleepTime(0);
        try {
            rbs.executeProviderOperation(mg.getParams(), mg.getSvcLogicContext());
        } catch (APPCException e) {
            Assert.fail("Exception during RebuildServer.executeProviderOperation");
        }
        InOrder inOrderTest = inOrder(server);
        inOrderTest.verify(server).stop();
        inOrderTest.verify(server).rebuild("linuxBase");
        inOrderTest.verify(server).start();

    }

    @Test
    public void rebuildServerReady() throws ZoneException {
        MockGenerator mg = new MockGenerator(Status.READY);
        Server server = mg.getServer();
        RebuildServer rbs = new RebuildServer();
        rbs.setProviderCache(mg.getProviderCacheMap());
        rbs.setRebuildSleepTime(0);
        try {
            rbs.executeProviderOperation(mg.getParams(), mg.getSvcLogicContext());
        } catch (APPCException e) {
            Assert.fail("Exception during RebuildServer.executeProviderOperation");
        }
        InOrder inOrderTest = inOrder(server);
        inOrderTest.verify(server).rebuild("linuxBase");
        inOrderTest.verify(server).start();
    }

    @Test
    public void rebuildServerPause() throws ZoneException {
        MockGenerator mg = new MockGenerator(Status.PAUSED);
        Server server = mg.getServer();
        RebuildServer rbs = new RebuildServer();
        rbs.setProviderCache(mg.getProviderCacheMap());
        rbs.setRebuildSleepTime(0);
        try {
            rbs.executeProviderOperation(mg.getParams(), mg.getSvcLogicContext());
        } catch (APPCException e) {
            Assert.fail("Exception during RebuildServer.executeProviderOperation");
        }
        InOrder inOrderTest = inOrder(server);
        inOrderTest.verify(server).unpause();
        inOrderTest.verify(server).stop();
        inOrderTest.verify(server).rebuild("linuxBase");
        inOrderTest.verify(server).start();
    }

    @Test
    public void rebuildServerError() {
        MockGenerator mg = new MockGenerator(Status.ERROR);
        Server server = mg.getServer();
        RebuildServer rbs = new RebuildServer();
        rbs.setProviderCache(mg.getProviderCacheMap());
        try {
            rbs.executeProviderOperation(mg.getParams(), mg.getSvcLogicContext());
        } catch (APPCException e) {
            Assert.fail("Exception during RebuildServer.executeProviderOperation");
        }
        verify(mg.getSvcLogicContext()).setAttribute(org.onap.appc.Constants.ATTRIBUTE_ERROR_CODE,
                "405");
    }

    @Test
    public void rebuildServerSuspended() throws ZoneException {
        MockGenerator mg = new MockGenerator(Status.SUSPENDED);
        Server server = mg.getServer();
        RebuildServer rbs = new RebuildServer();
        rbs.setProviderCache(mg.getProviderCacheMap());
        rbs.setRebuildSleepTime(0);
        try {
            rbs.executeProviderOperation(mg.getParams(), mg.getSvcLogicContext());
        } catch (APPCException e) {
            Assert.fail("Exception during RebuildServer.executeProviderOperation");
        }
        InOrder inOrderTest = inOrder(server);
        inOrderTest.verify(server).resume();
        inOrderTest.verify(server).stop();
        inOrderTest.verify(server).rebuild("linuxBase");
        inOrderTest.verify(server).start();
    }

}