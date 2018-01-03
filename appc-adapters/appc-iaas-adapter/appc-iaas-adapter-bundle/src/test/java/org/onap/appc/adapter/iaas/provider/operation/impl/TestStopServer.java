package org.onap.appc.adapter.iaas.provider.operation.impl;

import static org.mockito.Mockito.inOrder;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InOrder;
import org.onap.appc.exceptions.APPCException;
import com.att.cdp.exceptions.ZoneException;
import com.att.cdp.zones.model.Server;
import com.att.cdp.zones.model.Server.Status;

public class TestStopServer {

    @Test
    public void stopServerSuspended() throws ZoneException {
        MockGenerator mg = new MockGenerator(Status.SUSPENDED);
        Server server = mg.getServer();
        StopServer rbs = new StopServer();
        rbs.setProviderCache(mg.getProviderCacheMap());
        try {
            rbs.executeProviderOperation(mg.getParams(), mg.getSvcLogicContext());
        } catch (APPCException e) {
            Assert.fail("Exception during StopServer.executeProviderOperation");
        }
        InOrder inOrderTest = inOrder(server);
        inOrderTest.verify(server).resume();
    }

    @Test
    public void stopServerRunning() throws ZoneException {
        MockGenerator mg = new MockGenerator(Status.PAUSED);
        Server server = mg.getServer();
        StopServer rbs = new StopServer();
        rbs.setProviderCache(mg.getProviderCacheMap());
        try {
            rbs.executeProviderOperation(mg.getParams(), mg.getSvcLogicContext());
        } catch (APPCException e) {
            Assert.fail("Exception during StopServer.executeProviderOperation");
        }
        InOrder inOrderTest = inOrder(server);
        inOrderTest.verify(server).unpause();
    }
}
