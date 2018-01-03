package org.onap.appc.adapter.iaas.provider.operation.impl;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.anyInt;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.onap.appc.exceptions.APPCException;
import com.att.cdp.exceptions.ZoneException;
import com.att.cdp.zones.model.Server;
import com.att.cdp.zones.model.Server.Status;

public class TestMigrateServer {

    @Test
    public void migrateServerSuspended() throws ZoneException {
        MockGenerator mg = new MockGenerator(Status.SUSPENDED);
        Server server = mg.getServer();
        MigrateServer rbs = new MigrateServer();
        rbs.setProviderCache(mg.getProviderCacheMap());
        try {
            rbs.executeProviderOperation(mg.getParams(), mg.getSvcLogicContext());
        } catch (APPCException e) {
            Assert.fail("Exception during MigrateServer.executeProviderOperation");
        }
        verify(mg.getComputeService()).migrateServer(mg.SERVER_ID);
        verify(server, atLeastOnce()).waitForStateChange(anyInt(), anyInt(), Matchers.anyVararg());
    }
}
