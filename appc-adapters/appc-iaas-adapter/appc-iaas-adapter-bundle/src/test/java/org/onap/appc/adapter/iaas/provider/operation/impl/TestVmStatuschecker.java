package org.onap.appc.adapter.iaas.provider.operation.impl;

import org.junit.Assert;
import org.junit.Test;
import org.onap.appc.Constants;
import org.onap.appc.exceptions.APPCException;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.atLeastOnce;
import com.att.cdp.zones.model.ModelObject;
import com.att.cdp.zones.model.Server;
import com.att.cdp.zones.model.Server.Status;

public class TestVmStatuschecker {

    @Test
    public void vmStatuscheckerSuspended() {
        MockGenerator mg = new MockGenerator(Status.SUSPENDED);
        Server server = mg.getServer();
        VmStatuschecker rbs = new VmStatuschecker();
        rbs.setProviderCache(mg.getProviderCacheMap());
        ModelObject mo = null;
        try {
            mo = rbs.executeProviderOperation(mg.getParams(), mg.getSvcLogicContext());
        } catch (APPCException e) {
            Assert.fail("Exception during VmStatuschecker.executeProviderOperation");
        }
        verify(mg.getSvcLogicContext(), atLeastOnce()).setAttribute(Constants.STATUS_OF_VM,
                "suspended");
    }

    @Test
    public void vmStatuscheckerRunning() {
        MockGenerator mg = new MockGenerator(Status.RUNNING);
        Server server = mg.getServer();
        VmStatuschecker rbs = new VmStatuschecker();
        rbs.setProviderCache(mg.getProviderCacheMap());
        ModelObject mo = null;
        try {
            mo = rbs.executeProviderOperation(mg.getParams(), mg.getSvcLogicContext());
        } catch (APPCException e) {
            Assert.fail("Exception during VmStatuschecker.executeProviderOperation");
        }
        verify(mg.getSvcLogicContext(), atLeastOnce()).setAttribute(Constants.STATUS_OF_VM,
                "running");
    }

    @Test
    public void vmStatuscheckerError() {
        MockGenerator mg = new MockGenerator(Status.ERROR);
        Server server = mg.getServer();
        VmStatuschecker rbs = new VmStatuschecker();
        rbs.setProviderCache(mg.getProviderCacheMap());
        ModelObject mo = null;
        try {
            mo = rbs.executeProviderOperation(mg.getParams(), mg.getSvcLogicContext());
        } catch (APPCException e) {
            Assert.fail("Exception during VmStatuschecker.executeProviderOperation");
        }
        verify(mg.getSvcLogicContext(), atLeastOnce()).setAttribute(Constants.STATUS_OF_VM,
                "error");
    }
}
