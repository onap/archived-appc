package org.onap.appc.adapter.iaas.provider.operation.impl;

import org.junit.Assert;
import org.junit.Test;
import org.onap.appc.adapter.iaas.ProviderAdapter;
import org.onap.appc.exceptions.APPCException;
import com.att.cdp.exceptions.ZoneException;
import com.att.cdp.zones.model.Server;
import com.att.cdp.zones.model.Server.Status;
import static org.mockito.Mockito.verify;

public class TestEvacuateServer {

    @Test
    public void evacuateServerRunning() throws ZoneException {
        MockGenerator mg = new MockGenerator(Status.RUNNING);
        Server server = mg.getServer();
        EvacuateServer rbs = new EvacuateServer();
        rbs.setProviderCache(mg.getProviderCacheMap());
        mg.getParams().put(ProviderAdapter.PROPERTY_TARGETHOST_ID, "newServer1");
        try {
            rbs.executeProviderOperation(mg.getParams(), mg.getSvcLogicContext());
        } catch (APPCException e) {
            Assert.fail("Exception during EvacuateServer.executeProviderOperation");
        }
        verify(mg.getComputeService()).moveServer(server.getId(), "newServer1");
    }

}
