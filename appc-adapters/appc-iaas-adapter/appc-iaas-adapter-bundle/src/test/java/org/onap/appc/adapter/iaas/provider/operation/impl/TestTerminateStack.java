package org.onap.appc.adapter.iaas.provider.operation.impl;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import java.util.LinkedList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.onap.appc.Constants;
import org.onap.appc.adapter.iaas.ProviderAdapter;
import org.onap.appc.configuration.Configuration;
import org.onap.appc.configuration.ConfigurationFactory;
import org.onap.appc.exceptions.APPCException;
import com.att.cdp.exceptions.ZoneException;
import com.att.cdp.zones.StackService;
import com.att.cdp.zones.model.Server;
import com.att.cdp.zones.model.Stack;
import com.att.cdp.zones.model.Server.Status;

public class TestTerminateStack {

    @Test
    public void terminateStack() throws ZoneException {
        MockGenerator mg = new MockGenerator(Status.SUSPENDED);
        Server server = mg.getServer();
        StackService stackService = mock(StackService.class);
        Stack stack1 = mock(Stack.class);
        doReturn("stack1").when(stack1).getId();
        doReturn("stack1").when(stack1).getName();
        com.att.cdp.zones.model.Stack.Status stackStatus =
                com.att.cdp.zones.model.Stack.Status.DELETED;
        doReturn(stackStatus).when(stack1).getStatus();
        doReturn(mg.getContext()).when(stack1).getContext();
        List<Stack> stackList = new LinkedList<Stack>();
        stackList.add(stack1);
        doReturn(stackList).when(stackService).getStacks();
        doReturn(stack1).when(stackService).getStack("stack1", "stack1");
        doReturn(stackService).when(mg.getContext()).getStackService();
        mg.getParams().put(ProviderAdapter.PROPERTY_STACK_ID, "stack1");

        TerminateStack rbs = new TerminateStack();
        rbs.setProviderCache(mg.getProviderCacheMap());
        try {
            rbs.executeProviderOperation(mg.getParams(), mg.getSvcLogicContext());
        } catch (APPCException e) {
            Assert.fail("Exception during TerminateStack.executeProviderOperation");
        }
        verify(stackService).deleteStack(stack1);
    }
}
