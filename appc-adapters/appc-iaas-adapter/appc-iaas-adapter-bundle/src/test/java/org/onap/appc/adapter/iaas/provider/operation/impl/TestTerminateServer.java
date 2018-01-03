package org.onap.appc.adapter.iaas.provider.operation.impl;

import static org.mockito.Mockito.inOrder;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InOrder;
import org.onap.appc.exceptions.APPCException;
import com.att.cdp.exceptions.ZoneException;
import com.att.cdp.zones.model.Server;
import com.att.cdp.zones.model.Server.Status;

public class TestTerminateServer {

    @Test
    public void terminateServerSuspended() throws ZoneException {
        MockGenerator mg = new MockGenerator(Status.SUSPENDED);
        Server server = mg.getServer();
        TerminateServer rbs = new TerminateServer();
        rbs.setProviderCache(mg.getProviderCacheMap());
        try {
            rbs.executeProviderOperation(mg.getParams(), mg.getSvcLogicContext());
        } catch (APPCException e) {
            Assert.fail("Exception during TerminateServer.executeProviderOperation");
        }
        InOrder inOrderTest = inOrder(server);
        inOrderTest.verify(server).delete();
    }

    @Test
    public void terminateServerRunning() throws ZoneException {
        MockGenerator mg = new MockGenerator(Status.RUNNING);
        Server server = mg.getServer();
        TerminateServer rbs = new TerminateServer();
        rbs.setProviderCache(mg.getProviderCacheMap());
        try {
            rbs.executeProviderOperation(mg.getParams(), mg.getSvcLogicContext());
            int i = 5;
        } catch (APPCException e) {
            Assert.fail("Exception during TerminateServer.executeProviderOperation");
        }
        InOrder inOrderTest = inOrder(server);
        inOrderTest.verify(server).stop();
        inOrderTest.verify(server).delete();
    }
}
