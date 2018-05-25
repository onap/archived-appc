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
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.onap.appc.configuration.Configuration;
import org.onap.appc.configuration.ConfigurationFactory;
import org.onap.appc.Constants;
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
import com.att.cdp.zones.spi.RequestState;
import com.woorea.openstack.base.client.OpenStackBaseException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RequestState.class, Configuration.class})
@SuppressStaticInitializationFor("org.onap.appc.adapter.iaas.provider.operation.impl.base.ProviderOperation")
public class TestProviderStackOperation {

	ProviderStackOperation underTest = spy(RestoreStack.class);
	
	@Before
	public void setup() {
		Properties props = new Properties();
		props.setProperty(Constants.PROPERTY_OPENSTACK_POLL_INTERVAL, "1");
		props.setProperty(Constants.PROPERTY_STACK_STATE_CHANGE_TIMEOUT, "1");
		Configuration configuration = ConfigurationFactory.getConfiguration(props);
		Whitebox.setInternalState(ProviderStackOperation.class, "configuration", configuration);
	}

	@Test
	public void testTrackRequest() {
		MockGenerator mg = new MockGenerator(Server.Status.RUNNING);
		Context context = mg.getContext();
		AbstractService.State state = new OpenStackStackService(context).new State("STATE_NAME", "STATE_VALUE");
		AbstractService.State state2 = new OpenStackStackService(context).new State("STATE_NAME2", "STATE_VALUE2");
		PowerMockito.mockStatic(RequestState.class);
		underTest.trackRequest(context, state, state2);
		PowerMockito.verifyStatic();
		RequestState.put("STATE_NAME", "STATE_VALUE");
		PowerMockito.verifyStatic();
		RequestState.put("STATE_NAME2", "STATE_VALUE2");
		verify(context).getPrincipal();
	}

	@Test
	public void testWaitForStack() throws Exception{
		MockGenerator mg = new MockGenerator(Status.SUSPENDED);
		StackService stackService = mock(StackService.class);
		StackResource stackResource = mock(StackResource.class);
		ShowStack showStack = mock(ShowStack.class);
		Stack stack1 = mock(Stack.class);
		com.woorea.openstack.heat.model.Stack openstackHeatModelStack = mock(
				com.woorea.openstack.heat.model.Stack.class);
		doReturn("stack1").when(stack1).getId();
		doReturn("stack1").when(stack1).getName();
		com.att.cdp.zones.model.Stack.Status stackStatus = com.att.cdp.zones.model.Stack.Status.DELETED;
		doReturn(stackStatus).when(stack1).getStatus();
		doReturn(mg.getContext()).when(stack1).getContext();
		List<Stack> stackList = new LinkedList<Stack>();
		stackList.add(stack1);
		doReturn(stackList).when(stackService).getStacks();
		doReturn(stack1).when(stackService).getStack("stack1", "stack1");
		doReturn(showStack).when(stackResource).show(Mockito.anyString(), Mockito.anyString());
		doReturn(openstackHeatModelStack).when(showStack).execute();
		doReturn(stackService).when(mg.getContext()).getStackService();
		doReturn("ONLINE").when(openstackHeatModelStack).getStackStatus();
		mg.getParams().put(ProviderAdapter.PROPERTY_STACK_ID, "stack1");
		underTest.setProviderCache(mg.getProviderCacheMap());
		assertTrue(underTest.waitForStack(stack1, stackResource, "ONLINE"));
	}

	@Ignore
	@Test
	public void testWaitForStackUnexpectedStatus() throws ZoneException, OpenStackBaseException {
		MockGenerator mg = new MockGenerator(Status.SUSPENDED);
		StackService stackService = mock(StackService.class);
		StackResource stackResource = mock(StackResource.class);
		ShowStack showStack = mock(ShowStack.class);
		Stack stack1 = mock(Stack.class);
		com.woorea.openstack.heat.model.Stack openstackHeatModelStack = mock(
				com.woorea.openstack.heat.model.Stack.class);
		doReturn("stack1").when(stack1).getId();
		doReturn("stack1").when(stack1).getName();
		com.att.cdp.zones.model.Stack.Status stackStatus = com.att.cdp.zones.model.Stack.Status.DELETED;
		doReturn(stackStatus).when(stack1).getStatus();
		doReturn(mg.getContext()).when(stack1).getContext();
		List<Stack> stackList = new LinkedList<Stack>();
		stackList.add(stack1);
		doReturn(stackList).when(stackService).getStacks();
		doReturn(stack1).when(stackService).getStack("stack1", "stack1");
		doReturn(showStack).when(stackResource).show(Mockito.anyString(), Mockito.anyString());
		doReturn(openstackHeatModelStack).when(showStack).execute();
		doReturn(stackService).when(mg.getContext()).getStackService();
		doReturn("RUNNING").when(openstackHeatModelStack).getStackStatus();
		mg.getParams().put(ProviderAdapter.PROPERTY_STACK_ID, "stack1");
		underTest.setProviderCache(mg.getProviderCacheMap());
		assertFalse(underTest.waitForStack(stack1, stackResource, "ONLINE"));
	}

	@Test
	public void testWaitForStackFailed() throws ZoneException, OpenStackBaseException {
		MockGenerator mg = new MockGenerator(Status.SUSPENDED);
		StackService stackService = mock(StackService.class);
		StackResource stackResource = mock(StackResource.class);
		ShowStack showStack = mock(ShowStack.class);
		Stack stack1 = mock(Stack.class);
		com.woorea.openstack.heat.model.Stack openstackHeatModelStack = mock(
				com.woorea.openstack.heat.model.Stack.class);
		doReturn("stack1").when(stack1).getId();
		doReturn("stack1").when(stack1).getName();
		com.att.cdp.zones.model.Stack.Status stackStatus = com.att.cdp.zones.model.Stack.Status.DELETED;
		doReturn(stackStatus).when(stack1).getStatus();
		doReturn(mg.getContext()).when(stack1).getContext();
		List<Stack> stackList = new LinkedList<Stack>();
		stackList.add(stack1);
		doReturn(stackList).when(stackService).getStacks();
		doReturn(stack1).when(stackService).getStack("stack1", "stack1");
		doReturn(showStack).when(stackResource).show(Mockito.anyString(), Mockito.anyString());
		doReturn(openstackHeatModelStack).when(showStack).execute();
		doReturn(stackService).when(mg.getContext()).getStackService();
		doReturn("FAILED").when(openstackHeatModelStack).getStackStatus();
		mg.getParams().put(ProviderAdapter.PROPERTY_STACK_ID, "stack1");
		underTest.setProviderCache(mg.getProviderCacheMap());
		assertFalse(underTest.waitForStack(stack1, stackResource, "ONLINE"));
	}

	@Test
	public void testLookupStack() throws ZoneException, RequestFailedException {
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
		when(context.getTenant()).thenReturn(tenant);
		doReturn(stackList).when(stackService).getStacks();
		doReturn(stackService).when(context).getStackService();
		underTest.lookupStack(rc, context, id);
		verify(rc, times(1)).isFailed();
	}

	@Test(expected = RequestFailedException.class)
	public void testLookupStackRcFailed() throws RequestFailedException, ZoneException {
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
		when(context.getTenant()).thenReturn(tenant);
		doReturn(stackService).when(context).getStackService();
		doThrow(new ContextConnectionException("TEST")).when(stackService).getStacks();
		underTest.lookupStack(rc, context, id);
	}

	@Test
	public void testWaitForStackStatus() throws ZoneException, RequestFailedException {
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
		doReturn(stack1).when(stackService).getStack(Mockito.anyString(), Mockito.anyString());
		assertFalse(underTest.waitForStackStatus(rc, stack1, Stack.Status.DELETED));
	}

	@Test(expected = TimeoutException.class)
	public void testWaitForStackStatusTimeoutException() throws ZoneException, RequestFailedException {
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
		doReturn(stack1).when(stackService).getStack(Mockito.anyString(), Mockito.anyString());
		PowerMockito.mockStatic(RequestState.class);
		assertFalse(underTest.waitForStackStatus(rc, stack1, Stack.Status.FAILED));
	}
}
