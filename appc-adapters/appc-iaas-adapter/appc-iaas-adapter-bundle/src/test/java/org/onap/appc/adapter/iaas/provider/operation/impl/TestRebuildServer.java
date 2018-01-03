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
