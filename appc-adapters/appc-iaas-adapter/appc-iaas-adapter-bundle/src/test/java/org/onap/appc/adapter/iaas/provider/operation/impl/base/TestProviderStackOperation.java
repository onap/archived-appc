/*
 * ============LICENSE_START======================================================= 
 * Copyright (C) 2018 Ericsson. All rights reserved.
 * ================================================================================ 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except 
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.adapter.iaas.provider.operation.impl.base;

import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.appc.adapter.iaas.ProviderAdapter;
import org.onap.appc.adapter.iaas.impl.RequestContext;
import org.onap.appc.adapter.iaas.impl.RequestFailedException;
import org.onap.appc.adapter.iaas.provider.operation.impl.MockGenerator;
import org.onap.appc.adapter.iaas.provider.operation.impl.RestoreStack;
import org.onap.appc.adapter.openstack.heat.StackResource;
import org.onap.appc.adapter.openstack.heat.StackResource.ShowStack;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import com.att.cdp.exceptions.ContextConnectionException;
import com.att.cdp.exceptions.TimeoutException;
import com.att.cdp.exceptions.ZoneException;
import com.att.cdp.openstack.v1.OpenStackStackService;
import com.att.cdp.zones.Context;
import com.att.cdp.zones.StackService;
import com.att.cdp.zones.model.Server;
import com.att.cdp.zones.model.Stack;
import com.att.cdp.zones.model.Tenant;
import com.att.cdp.zones.model.Server.Status;
import com.att.cdp.zones.spi.AbstractService;

public class TestProviderStackOperation {

    ProviderStackOperation underTest = spy(RestoreStack.class);

    @Test
    public void testTrackRequest() {
        MockGenerator mg = new MockGenerator(Server.Status.RUNNING);
        Context context = mg.getContext();
        AbstractService.State state = new OpenStackStackService(context).new State("STATE_NAME", "STATE_VALUE");
        underTest.trackRequest(context, state);
        verify(context).getPrincipal();
    }

    @Test
    public void testWaitForStack() {
        MockGenerator mg = new MockGenerator(Status.SUSPENDED);
        StackService stackService = mock(StackService.class);
        StackResource stackResource = mock(StackResource.class);
        ShowStack showStack = mock(ShowStack.class);
        Stack stack1 = mock(Stack.class);
        com.woorea.openstack.heat.model.Stack openstackHeatModelStack =
                mock(com.woorea.openstack.heat.model.Stack.class);
        doReturn("stack1").when(stack1).getId();
        doReturn("stack1").when(stack1).getName();
        com.att.cdp.zones.model.Stack.Status stackStatus = com.att.cdp.zones.model.Stack.Status.DELETED;
        doReturn(stackStatus).when(stack1).getStatus();
        doReturn(mg.getContext()).when(stack1).getContext();
        List<Stack> stackList = new LinkedList<Stack>();
        stackList.add(stack1);
        try {
            doReturn(stackList).when(stackService).getStacks();
            doReturn(stack1).when(stackService).getStack("stack1", "stack1");
            doReturn(showStack).when(stackResource).show(Mockito.anyString(), Mockito.anyString());
            doReturn(openstackHeatModelStack).when(showStack).execute();
            doReturn(stackService).when(mg.getContext()).getStackService();
            doReturn("ONLINE").when(openstackHeatModelStack).getStackStatus();
            mg.getParams().put(ProviderAdapter.PROPERTY_STACK_ID, "stack1");
            underTest.setProviderCache(mg.getProviderCacheMap());
            assertTrue(underTest.waitForStack(stack1, stackResource, "ONLINE"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testWaitForStackUnexpectedStatus() {
        MockGenerator mg = new MockGenerator(Status.SUSPENDED);
        StackService stackService = mock(StackService.class);
        StackResource stackResource = mock(StackResource.class);
        ShowStack showStack = mock(ShowStack.class);
        Stack stack1 = mock(Stack.class);
        com.woorea.openstack.heat.model.Stack openstackHeatModelStack =
                mock(com.woorea.openstack.heat.model.Stack.class);
        doReturn("stack1").when(stack1).getId();
        doReturn("stack1").when(stack1).getName();
        com.att.cdp.zones.model.Stack.Status stackStatus = com.att.cdp.zones.model.Stack.Status.DELETED;
        doReturn(stackStatus).when(stack1).getStatus();
        doReturn(mg.getContext()).when(stack1).getContext();
        List<Stack> stackList = new LinkedList<Stack>();
        stackList.add(stack1);
        try {
            doReturn(stackList).when(stackService).getStacks();
            doReturn(stack1).when(stackService).getStack("stack1", "stack1");
            doReturn(showStack).when(stackResource).show(Mockito.anyString(), Mockito.anyString());
            doReturn(openstackHeatModelStack).when(showStack).execute();
            doReturn(stackService).when(mg.getContext()).getStackService();
            doReturn("RUNNING").when(openstackHeatModelStack).getStackStatus();
            mg.getParams().put(ProviderAdapter.PROPERTY_STACK_ID, "stack1");
            underTest.setProviderCache(mg.getProviderCacheMap());
            assertFalse(underTest.waitForStack(stack1, stackResource, "ONLINE"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testWaitForStackFailed() {
        MockGenerator mg = new MockGenerator(Status.SUSPENDED);
        StackService stackService = mock(StackService.class);
        StackResource stackResource = mock(StackResource.class);
        ShowStack showStack = mock(ShowStack.class);
        Stack stack1 = mock(Stack.class);
        com.woorea.openstack.heat.model.Stack openstackHeatModelStack =
                mock(com.woorea.openstack.heat.model.Stack.class);
        doReturn("stack1").when(stack1).getId();
        doReturn("stack1").when(stack1).getName();
        com.att.cdp.zones.model.Stack.Status stackStatus = com.att.cdp.zones.model.Stack.Status.DELETED;
        doReturn(stackStatus).when(stack1).getStatus();
        doReturn(mg.getContext()).when(stack1).getContext();
        List<Stack> stackList = new LinkedList<Stack>();
        stackList.add(stack1);
        try {
            doReturn(stackList).when(stackService).getStacks();
            doReturn(stack1).when(stackService).getStack("stack1", "stack1");
            doReturn(showStack).when(stackResource).show(Mockito.anyString(), Mockito.anyString());
            doReturn(openstackHeatModelStack).when(showStack).execute();
            doReturn(stackService).when(mg.getContext()).getStackService();
            doReturn("FAILED").when(openstackHeatModelStack).getStackStatus();
            mg.getParams().put(ProviderAdapter.PROPERTY_STACK_ID, "stack1");
            underTest.setProviderCache(mg.getProviderCacheMap());
            assertFalse(underTest.waitForStack(stack1, stackResource, "ONLINE"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testLookupStack() {
        MockGenerator mg = new MockGenerator(Status.RUNNING);
        Server server = spy(new Server());
        RequestContext rc = mock(RequestContext.class);
        Context context = mg.getContext();
        when(server.getContext()).thenReturn(context);
        Tenant tenant = spy(new Tenant());
        when(tenant.getName()).thenReturn("TEST_TENANT_NAME");
        when(tenant.getId()).thenReturn("TEST_TENANT_ID");
        when(rc.attempt()).thenReturn(true).thenReturn(false);
        Stack stack1 = mock(Stack.class);
        String id = mg.SERVER_ID;
        when(stack1.getId()).thenReturn(id);
        StackService stackService = mock(StackService.class);
        List<Stack> stackList = new LinkedList<Stack>();
        stackList.add(stack1);
        when(tenant.getName()).thenReturn("TEST_TENANT_NAME");
        when(tenant.getId()).thenReturn("TEST_TENANT_ID");
        try {
            when(context.getTenant()).thenReturn(tenant);
            doReturn(stackService).when(context).getStackService();
            doThrow(new ContextConnectionException("TEST")).when(stackService).getStacks();
            underTest.lookupStack(rc, context, id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        verify(rc, times(1)).delay();
    }

    @Test(expected = RequestFailedException.class)
    public void testLookupStackRcFailed() throws RequestFailedException {
        MockGenerator mg = new MockGenerator(Status.RUNNING);
        Server server = spy(new Server());
        RequestContext rc = mock(RequestContext.class);
        Context context = mg.getContext();
        SvcLogicContext svcLogicContext = mg.getSvcLogicContext();
        when(rc.getSvcLogicContext()).thenReturn(svcLogicContext);
        when(server.getContext()).thenReturn(context);
        Tenant tenant = spy(new Tenant());
        when(tenant.getName()).thenReturn("TEST_TENANT_NAME");
        when(tenant.getId()).thenReturn("TEST_TENANT_ID");
        when(rc.attempt()).thenReturn(true).thenReturn(false);
        Stack stack1 = mock(Stack.class);
        String id = mg.SERVER_ID;
        when(stack1.getId()).thenReturn(id);
        StackService stackService = mock(StackService.class);
        List<Stack> stackList = new LinkedList<Stack>();
        stackList.add(stack1);
        when(tenant.getName()).thenReturn("TEST_TENANT_NAME");
        when(tenant.getId()).thenReturn("TEST_TENANT_ID");
        when(rc.isFailed()).thenReturn(true);
        try {
            when(context.getTenant()).thenReturn(tenant);
            doReturn(stackService).when(context).getStackService();
            doThrow(new ContextConnectionException("TEST")).when(stackService).getStacks();
            underTest.lookupStack(rc, context, id);
        } catch (ZoneException ze) {
            ze.printStackTrace();
        } catch (RequestFailedException rfe) {
            throw rfe;
        }
    }

    @Test
    public void testWaitForStackStatus() {
        MockGenerator mg = new MockGenerator(Server.Status.ERROR);
        RequestContext rc = mock(RequestContext.class);
        Context context = mg.getContext();
        Stack stack1 = mock(Stack.class);
        String id = mg.SERVER_ID;
        when(stack1.getId()).thenReturn(id);
        StackService stackService = mock(StackService.class);
        List<Stack> stackList = new LinkedList<Stack>();
        stackList.add(stack1);
        when(stack1.getContext()).thenReturn(context);
        com.att.cdp.zones.model.Stack.Status stackStatus = com.att.cdp.zones.model.Stack.Status.FAILED;
        doReturn(stackStatus).when(stack1).getStatus();
        SvcLogicContext svcLogicContext = mg.getSvcLogicContext();
        when(rc.getSvcLogicContext()).thenReturn(svcLogicContext);
        doReturn(stackService).when(context).getStackService();
        try {
            doReturn(stack1).when(stackService).getStack(Mockito.anyString(), Mockito.anyString());
            assertFalse(underTest.waitForStackStatus(rc, stack1, Stack.Status.DELETED));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test(expected = TimeoutException.class)
    public void testWaitForStackStatusTimeoutException() throws TimeoutException {
        MockGenerator mg = new MockGenerator(Server.Status.ERROR);
        RequestContext rc = mock(RequestContext.class);
        Context context = mg.getContext();
        Stack stack1 = mock(Stack.class);
        String id = mg.SERVER_ID;
        when(stack1.getId()).thenReturn(id);
        StackService stackService = mock(StackService.class);
        List<Stack> stackList = new LinkedList<Stack>();
        stackList.add(stack1);
        when(stack1.getContext()).thenReturn(context);
        com.att.cdp.zones.model.Stack.Status stackStatus = com.att.cdp.zones.model.Stack.Status.DELETED;
        doReturn(stackStatus).when(stack1).getStatus();
        SvcLogicContext svcLogicContext = mg.getSvcLogicContext();
        when(rc.getSvcLogicContext()).thenReturn(svcLogicContext);
        doReturn(stackService).when(context).getStackService();
        try {
            doReturn(stack1).when(stackService).getStack(Mockito.anyString(), Mockito.anyString());
            assertFalse(underTest.waitForStackStatus(rc, stack1, Stack.Status.FAILED));
        } catch (TimeoutException te) {
            throw (te);
        } catch (RequestFailedException | ZoneException e) {
            e.printStackTrace();
        }
    }
}
