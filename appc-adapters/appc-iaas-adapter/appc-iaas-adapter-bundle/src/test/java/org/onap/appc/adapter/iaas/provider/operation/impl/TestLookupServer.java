package org.onap.appc.adapter.iaas.provider.operation.impl;

import static org.junit.Assert.assertTrue;
import org.junit.Assert;
import org.junit.Test;
import org.onap.appc.exceptions.APPCException;
import com.att.cdp.zones.model.ModelObject;
import com.att.cdp.zones.model.Server;
import com.att.cdp.zones.model.Server.Status;

public class TestLookupServer {

    @Test
    public void lookupServer() {
        MockGenerator mg = new MockGenerator(Status.SUSPENDED);
        Server server = mg.getServer();
        LookupServer rbs = new LookupServer();
        rbs.setProviderCache(mg.getProviderCacheMap());
        ModelObject mo = null;
        try {
            mo = rbs.executeProviderOperation(mg.getParams(), mg.getSvcLogicContext());
        } catch (APPCException e) {
            Assert.fail("Exception during LookupServer.executeProviderOperation");
        }
        boolean correctServerReturned = false;
        try {
            Server returnedServer = (Server) mo;
            correctServerReturned = returnedServer == server;
        } catch (Exception e) {
            Assert.fail();
        }
        assertTrue(correctServerReturned);
    }
}
