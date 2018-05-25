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
import static org.mockito.Mockito.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.Mockito;
import org.onap.appc.adapter.iaas.impl.RequestContext;
import org.onap.appc.adapter.iaas.impl.RequestFailedException;
import org.onap.appc.adapter.iaas.provider.operation.impl.MockGenerator;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import com.att.cdp.exceptions.ContextConnectionException;
import com.att.cdp.exceptions.NotLoggedInException;
import com.att.cdp.exceptions.TimeoutException;
import com.att.cdp.exceptions.ZoneException;
import com.att.cdp.openstack.OpenStackContext;
import com.att.cdp.zones.ComputeService;
import com.att.cdp.zones.Context;
import com.att.cdp.zones.ImageService;
import com.att.cdp.zones.NetworkService;
import com.att.cdp.zones.Provider;
import com.att.cdp.zones.model.Hypervisor;
import com.att.cdp.zones.model.Image;
import com.att.cdp.zones.model.Network;
import com.att.cdp.zones.model.Server;
import com.att.cdp.zones.model.Server.Status;
import com.att.cdp.zones.model.Tenant;
import com.att.cdp.zones.model.Port;
import org.onap.appc.adapter.iaas.provider.operation.impl.AttachVolumeServer;

@RunWith(MockitoJUnitRunner.class)
public class TestProviderServerOperation {

    ProviderServerOperation underTest = spy(AttachVolumeServer.class);

    @Test
    public void testHasImageAccess() {
        RequestContext rc = mock(RequestContext.class);
        ImageService imageService = mock(ImageService.class);
        Context context = mock(OpenStackContext.class);
        try {
            when(context.getImageService()).thenReturn(imageService);
            assertTrue(underTest.hasImageAccess(rc, context));
        } catch (NotLoggedInException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testHasImageAccessZoneException() {
        RequestContext rc = mock(RequestContext.class);
        ImageService imageService = mock(ImageService.class);
        Context context = mock(OpenStackContext.class);
        try {
            when(context.getImageService()).thenReturn(imageService);
            when(imageService.getImageByName("CHECK_IMAGE_ACCESS")).thenThrow(new ZoneException("TEST_ZONE_EXCEPTION"));
            assertFalse(underTest.hasImageAccess(rc, context));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testWaitForStateChangeRequestContextImageStatusArray() {
        Image image = mock(Image.class);
        Image.Status imageStatus = Image.Status.ACTIVE;
        image.setStatus(imageStatus);
        RequestContext rc = mock(RequestContext.class);
        ImageService imageService = mock(ImageService.class);
        Context context = mock(OpenStackContext.class);
        Provider provider = mock(Provider.class);
        when(context.getProvider()).thenReturn(provider);
        when(provider.getName()).thenReturn("TEST Provider Name");
        when(image.getContext()).thenReturn(context);
        when(rc.isFailed()).thenReturn(true);
        try {
            when(context.getImageService()).thenReturn(imageService);
        } catch (NotLoggedInException e) {
            e.printStackTrace();
        }
        boolean requestFailedExceptionThrown = false;
        try {
            underTest.waitForStateChange(rc, image, imageStatus);
        } catch (ZoneException e) {
            e.printStackTrace();
        } catch (RequestFailedException requestFailedException) {
            requestFailedExceptionThrown =
                    (requestFailedException.getOperation().equals("Waiting for State Change")) ? true : false;
        }
        assertTrue(requestFailedExceptionThrown);
    }

    @Test
    public void testWaitForStateChangeRequestContextImageStatusArrayTimeoutException() {
        Image image = mock(Image.class);
        Image.Status imageStatus = Image.Status.ACTIVE;
        image.setStatus(imageStatus);
        RequestContext rc = mock(RequestContext.class);
        ImageService imageService = mock(ImageService.class);
        Context context = mock(OpenStackContext.class);
        Provider provider = mock(Provider.class);
        ComputeService computeService = mock(ComputeService.class);
        when(context.getComputeService()).thenReturn(computeService);
        when(computeService.getURL()).thenReturn("TEST URL");
        when(context.getProvider()).thenReturn(provider);
        when(provider.getName()).thenReturn("TEST Provider Name");
        when(image.getContext()).thenReturn(context);
        when(rc.isFailed()).thenReturn(false);
        Tenant tenant = spy(new Tenant());
        when(tenant.getName()).thenReturn("TEST_TENANT_NAME");
        when(tenant.getId()).thenReturn("TEST_TENANT_ID");
        when(rc.attempt()).thenReturn(true).thenReturn(false);
        try {
            when(context.getTenant()).thenReturn(tenant);
            when(context.getImageService()).thenReturn(imageService);
            doThrow(new TimeoutException("TEST")).when(image).waitForStateChange(Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.anyObject());
            underTest.waitForStateChange(rc, image, imageStatus);
        } catch (Exception e) {
            e.printStackTrace();
        }
        verify(rc, times(1)).delay();
    }

    @Test
    public void testWaitForStateChangeRequestContextImageStatusArrayZoneException() {
        Image image = mock(Image.class);
        Image.Status imageStatus = Image.Status.ACTIVE;
        image.setStatus(imageStatus);
        RequestContext rc = mock(RequestContext.class);
        ImageService imageService = mock(ImageService.class);
        Context context = mock(OpenStackContext.class);
        Provider provider = mock(Provider.class);
        ComputeService computeService = mock(ComputeService.class);
        when(context.getComputeService()).thenReturn(computeService);
        when(computeService.getURL()).thenReturn("TEST URL");
        when(context.getProvider()).thenReturn(provider);
        when(provider.getName()).thenReturn("TEST Provider Name");
        when(image.getContext()).thenReturn(context);
        when(image.getName()).thenReturn("TEST_IMAGE_NAME");
        when(image.getId()).thenReturn("TEST_IMAGE_ID");
        when(image.getStatus()).thenReturn(imageStatus);
        when(rc.isFailed()).thenReturn(false);
        Tenant tenant = spy(new Tenant());
        when(rc.attempt()).thenReturn(true).thenReturn(false);
        try {
            when(context.getTenant()).thenReturn(tenant).thenThrow(new ZoneException("TEST_ZONE_EXCEPTION"));
            when(context.getImageService()).thenReturn(imageService);
            doThrow(new TimeoutException("TEST")).when(image).waitForStateChange(Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.anyObject());
            underTest.waitForStateChange(rc, image, imageStatus);
        } catch (Exception e) {
            e.printStackTrace();
        }
        verify(rc, times(1)).delay();
    }

    @Test
    public void testWaitForStateChangeRequestContextServerStatusArray() {
        Server server = mock(Server.class);
        Status serverStatus = Status.RUNNING;
        server.setStatus(serverStatus);
        RequestContext rc = mock(RequestContext.class);
        ImageService imageService = mock(ImageService.class);
        Context context = mock(OpenStackContext.class);
        Provider provider = mock(Provider.class);
        ComputeService computeService = mock(ComputeService.class);
        when(context.getComputeService()).thenReturn(computeService);
        when(computeService.getURL()).thenReturn("TEST URL");
        when(context.getProvider()).thenReturn(provider);
        when(provider.getName()).thenReturn("TEST Provider Name");
        when(server.getContext()).thenReturn(context);
        when(rc.isFailed()).thenReturn(true);
        boolean requestFailedExceptionThrown = false;
        try {
            when(context.getImageService()).thenReturn(imageService);
            underTest.waitForStateChange(rc, server, serverStatus);
        } catch (RequestFailedException requestFailedException) {
            requestFailedExceptionThrown =
                    (requestFailedException.getOperation().equals("Waiting for State Change")) ? true : false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertTrue(requestFailedExceptionThrown);
    }


    @Test
    public void testWaitForStateChangeRequestContextServerStatusArrayTimeoutException() {
        Server server = mock(Server.class);
        Status serverStatus = Status.RUNNING;
        server.setStatus(serverStatus);
        when(server.getStatus()).thenReturn(serverStatus);
        RequestContext rc = mock(RequestContext.class);
        Context context = mock(OpenStackContext.class);
        Provider provider = mock(Provider.class);
        ComputeService computeService = mock(ComputeService.class);
        when(context.getComputeService()).thenReturn(computeService);
        when(computeService.getURL()).thenReturn("TEST URL");
        when(context.getProvider()).thenReturn(provider);
        when(provider.getName()).thenReturn("TEST Provider Name");
        when(server.getContext()).thenReturn(context);
        when(rc.isFailed()).thenReturn(false);
        Tenant tenant = spy(new Tenant());
        when(tenant.getName()).thenReturn("TEST_TENANT_NAME");
        when(tenant.getId()).thenReturn("TEST_TENANT_ID");
        when(rc.attempt()).thenReturn(true).thenReturn(false);
        try {
            when(context.getTenant()).thenReturn(tenant);
            doThrow(new TimeoutException("TEST")).when(server).waitForStateChange(Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.anyObject());
            underTest.waitForStateChange(rc, server, serverStatus);
        } catch (Exception e) {
            e.printStackTrace();
        }
        verify(rc, times(1)).delay();
    }

    @Test
    public void testWaitForStateChangeRequestContextServerStatusArrayZoneException() {
        Server server = mock(Server.class);
        Status serverStatus = Status.RUNNING;
        MockGenerator mg = new MockGenerator(serverStatus);
        server.setStatus(serverStatus);
        when(server.getStatus()).thenReturn(serverStatus);
        RequestContext rc = mock(RequestContext.class);
        ImageService imageService = mock(ImageService.class);
        Context context = mock(OpenStackContext.class);
        Provider provider = mock(Provider.class);
        ComputeService computeService = mock(ComputeService.class);
        when(context.getComputeService()).thenReturn(computeService);
        when(computeService.getURL()).thenReturn("TEST URL");
        when(context.getProvider()).thenReturn(provider);
        when(provider.getName()).thenReturn("TEST Provider Name");
        when(server.getContext()).thenReturn(context);
        when(rc.isFailed()).thenReturn(false);
        Tenant tenant = spy(new Tenant());
        when(rc.attempt()).thenReturn(true).thenReturn(false);
        try {
            when(context.getTenant()).thenReturn(tenant).thenThrow(new ZoneException("TEST_ZONE_EXCEPTION"));
            when(context.getImageService()).thenReturn(imageService);
            doThrow(new TimeoutException("TEST")).when(server).waitForStateChange(Mockito.anyInt(), Mockito.anyInt(),
                    Mockito.anyObject());
            underTest.waitForStateChange(rc, server, serverStatus);
        } catch (Exception e) {
            e.printStackTrace();
        }
        verify(rc, times(1)).delay();
    }

    @Test
    public void testLookupServer() {
        MockGenerator mg = new MockGenerator(Status.RUNNING);
        Server server = mock(Server.class);
        RequestContext rc = mock(RequestContext.class);
        Context context = mock(OpenStackContext.class);
        Provider provider = mock(Provider.class);
        when(context.getProvider()).thenReturn(provider);
        when(server.getContext()).thenReturn(context);
        when(provider.getName()).thenReturn("TEST Provider Name");
        ComputeService computeService = mock(ComputeService.class);
        when(context.getComputeService()).thenReturn(computeService);
        when(computeService.getURL()).thenReturn("TEST URL");
        when(rc.isFailed()).thenReturn(true);
        SvcLogicContext slc = new SvcLogicContext();
        when(rc.getSvcLogicContext()).thenReturn(slc);
        rc.setSvcLogicContext(slc);
        String id = mg.SERVER_ID;
        boolean requestFailedExceptionThrown = false;
        try {
            underTest.lookupServer(rc, context, id);
        } catch (ZoneException zoneException) {
            zoneException.printStackTrace();
        } catch (RequestFailedException requestFailedException) {
            System.out.println(requestFailedException.getOperation());
            requestFailedExceptionThrown =
                    (requestFailedException.getOperation().equals("Lookup Server")) ? true : false;
        }
        assertTrue(requestFailedExceptionThrown);
    }

    @Test
    public void testLookupServerContextConnectionException() {
        MockGenerator mg = new MockGenerator(Status.RUNNING);
        Server server = spy(new Server());
        RequestContext rc = mock(RequestContext.class);
        Context context = mock(OpenStackContext.class);
        Provider provider = mock(Provider.class);
        when(context.getProvider()).thenReturn(provider);
        when(server.getContext()).thenReturn(context);
        when(provider.getName()).thenReturn("TEST Provider Name");
        ComputeService computeService = mock(ComputeService.class);
        when(context.getComputeService()).thenReturn(computeService);
        when(computeService.getURL()).thenReturn("TEST URL");
        Tenant tenant = spy(new Tenant());
        when(tenant.getName()).thenReturn("TEST_TENANT_NAME");
        when(tenant.getId()).thenReturn("TEST_TENANT_ID");
        when(rc.attempt()).thenReturn(true).thenReturn(false);
        try {
            when(context.getTenant()).thenReturn(tenant);
            doThrow(new ContextConnectionException("TEST")).when(computeService).getServer(Mockito.anyString());
            underTest.lookupServer(rc, context, mg.SERVER_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        verify(rc, times(1)).delay();
    }

    @Test
    public void testResumeServer() {
        Server server = spy(new Server());
        RequestContext rc = mock(RequestContext.class);
        Context context = mock(OpenStackContext.class);
        Provider provider = mock(Provider.class);
        when(context.getProvider()).thenReturn(provider);
        when(server.getContext()).thenReturn(context);
        when(provider.getName()).thenReturn("TEST Provider Name");
        ComputeService computeService = mock(ComputeService.class);
        when(context.getComputeService()).thenReturn(computeService);
        when(computeService.getURL()).thenReturn("TEST URL");
        when(rc.isFailed()).thenReturn(true);
        boolean requestFailedExceptionThrown = false;
        try {
            underTest.resumeServer(rc, server);
        } catch (ZoneException zoneException) {
            zoneException.printStackTrace();
        } catch (RequestFailedException requestFailedException) {
            requestFailedExceptionThrown =
                    (requestFailedException.getOperation().equals("Resume Server")) ? true : false;
        }
        assertTrue(requestFailedExceptionThrown);
    }

    @Test
    public void testResumeServerContextConnectionException() {
        Server server = spy(new Server());
        RequestContext rc = mock(RequestContext.class);
        Context context = mock(OpenStackContext.class);
        Provider provider = mock(Provider.class);
        when(context.getProvider()).thenReturn(provider);
        when(server.getContext()).thenReturn(context);
        when(provider.getName()).thenReturn("TEST Provider Name");
        ComputeService computeService = mock(ComputeService.class);
        when(context.getComputeService()).thenReturn(computeService);
        when(computeService.getURL()).thenReturn("TEST URL");
        Tenant tenant = spy(new Tenant());
        when(tenant.getName()).thenReturn("TEST_TENANT_NAME");
        when(tenant.getId()).thenReturn("TEST_TENANT_ID");
        when(rc.attempt()).thenReturn(true).thenReturn(false);
        try {
            when(context.getTenant()).thenReturn(tenant);
            doThrow(new ContextConnectionException("TEST")).when(server).resume();
            underTest.resumeServer(rc, server);
        } catch (Exception e) {
            e.printStackTrace();
        }
        verify(rc, times(1)).delay();
    }



    @Test
    public void testStopServer() {
        Server server = spy(new Server());
        RequestContext rc = mock(RequestContext.class);
        Context context = mock(OpenStackContext.class);
        Provider provider = mock(Provider.class);
        when(context.getProvider()).thenReturn(provider);
        when(server.getContext()).thenReturn(context);
        when(provider.getName()).thenReturn("TEST Provider Name");
        ComputeService computeService = mock(ComputeService.class);
        when(context.getComputeService()).thenReturn(computeService);
        when(computeService.getURL()).thenReturn("TEST URL");
        when(rc.isFailed()).thenReturn(true);
        boolean requestFailedExceptionThrown = false;
        try {
            underTest.stopServer(rc, server);
        } catch (ZoneException zoneException) {
            zoneException.printStackTrace();
        } catch (RequestFailedException requestFailedException) {
            requestFailedExceptionThrown = (requestFailedException.getOperation().equals("Stop Server")) ? true : false;
        }
        assertTrue(requestFailedExceptionThrown);
    }

    @Test
    public void testStopServerContextConnectionException() {
        Server server = spy(new Server());
        RequestContext rc = mock(RequestContext.class);
        Context context = mock(OpenStackContext.class);
        Provider provider = mock(Provider.class);
        when(context.getProvider()).thenReturn(provider);
        when(server.getContext()).thenReturn(context);
        when(provider.getName()).thenReturn("TEST Provider Name");
        ComputeService computeService = mock(ComputeService.class);
        when(context.getComputeService()).thenReturn(computeService);
        when(computeService.getURL()).thenReturn("TEST URL");
        Tenant tenant = spy(new Tenant());
        when(tenant.getName()).thenReturn("TEST_TENANT_NAME");
        when(tenant.getId()).thenReturn("TEST_TENANT_ID");
        when(rc.attempt()).thenReturn(true).thenReturn(false);
        try {
            when(context.getTenant()).thenReturn(tenant);
            doThrow(new ContextConnectionException("TEST")).when(server).stop();
            underTest.stopServer(rc, server);
        } catch (Exception e) {
            e.printStackTrace();
        }
        verify(rc, times(1)).delay();
    }

    @Test
    public void testStartServer() {
        Server server = spy(new Server());
        RequestContext rc = mock(RequestContext.class);
        Context context = mock(OpenStackContext.class);
        Provider provider = mock(Provider.class);
        when(context.getProvider()).thenReturn(provider);
        when(server.getContext()).thenReturn(context);
        when(provider.getName()).thenReturn("TEST Provider Name");
        ComputeService computeService = mock(ComputeService.class);
        when(context.getComputeService()).thenReturn(computeService);
        when(computeService.getURL()).thenReturn("TEST URL");
        when(rc.isFailed()).thenReturn(true);
        boolean requestFailedExceptionThrown = false;
        try {
            underTest.startServer(rc, server);
        } catch (ZoneException zoneException) {
            zoneException.printStackTrace();
        } catch (RequestFailedException requestFailedException) {
            System.out.println(requestFailedException.getOperation());
            requestFailedExceptionThrown =
                    (requestFailedException.getOperation().equals("Start Server")) ? true : false;
        }
        assertTrue(requestFailedExceptionThrown);
    }

    @Test
    public void testStartServerContextConnectionException() {
        Server server = spy(new Server());
        RequestContext rc = mock(RequestContext.class);
        Context context = mock(OpenStackContext.class);
        Provider provider = mock(Provider.class);
        when(context.getProvider()).thenReturn(provider);
        when(server.getContext()).thenReturn(context);
        when(provider.getName()).thenReturn("TEST Provider Name");
        ComputeService computeService = mock(ComputeService.class);
        when(context.getComputeService()).thenReturn(computeService);
        when(computeService.getURL()).thenReturn("TEST URL");
        Tenant tenant = spy(new Tenant());
        when(tenant.getName()).thenReturn("TEST_TENANT_NAME");
        when(tenant.getId()).thenReturn("TEST_TENANT_ID");
        when(rc.attempt()).thenReturn(true).thenReturn(false);
        try {
            when(context.getTenant()).thenReturn(tenant);
            doThrow(new ContextConnectionException("TEST")).when(server).start();
            underTest.startServer(rc, server);
        } catch (Exception e) {
            e.printStackTrace();
        }
        verify(rc, times(1)).delay();
    }

    @Test
    public void testUnpauseServer() {
        Server server = spy(new Server());
        RequestContext rc = mock(RequestContext.class);
        Context context = mock(OpenStackContext.class);
        Provider provider = mock(Provider.class);
        when(context.getProvider()).thenReturn(provider);
        when(server.getContext()).thenReturn(context);
        when(provider.getName()).thenReturn("TEST Provider Name");
        ComputeService computeService = mock(ComputeService.class);
        when(context.getComputeService()).thenReturn(computeService);
        when(computeService.getURL()).thenReturn("TEST URL");
        when(rc.isFailed()).thenReturn(true);

        boolean requestFailedExceptionThrown = false;
        try {
            underTest.unpauseServer(rc, server);
        } catch (ZoneException zoneException) {
            zoneException.printStackTrace();
        } catch (RequestFailedException requestFailedException) {
            System.out.println(requestFailedException.getOperation());
            requestFailedExceptionThrown =
                    (requestFailedException.getOperation().equals("Unpause Server")) ? true : false;
        }
        assertTrue(requestFailedExceptionThrown);
    }

    @Test
    public void testUnpauseServerContextConnectionException() {
        Server server = spy(new Server());
        RequestContext rc = mock(RequestContext.class);
        Context context = mock(OpenStackContext.class);
        Provider provider = mock(Provider.class);
        when(context.getProvider()).thenReturn(provider);
        when(server.getContext()).thenReturn(context);
        when(provider.getName()).thenReturn("TEST Provider Name");
        ComputeService computeService = mock(ComputeService.class);
        when(context.getComputeService()).thenReturn(computeService);
        when(computeService.getURL()).thenReturn("TEST URL");
        Tenant tenant = spy(new Tenant());
        when(tenant.getName()).thenReturn("TEST_TENANT_NAME");
        when(tenant.getId()).thenReturn("TEST_TENANT_ID");
        when(rc.attempt()).thenReturn(true).thenReturn(false);
        try {
            when(context.getTenant()).thenReturn(tenant);
            doThrow(new ContextConnectionException("TEST")).when(server).unpause();
            underTest.unpauseServer(rc, server);
        } catch (Exception e) {
            e.printStackTrace();
        }
        verify(rc, times(1)).delay();
    }

    @Test
    public void testCheckVirtualMachineNetworkStatusOnlinePort() {
        MockGenerator mg = new MockGenerator(Status.RUNNING);
        RequestContext rc = mock(RequestContext.class);
        SvcLogicContext slc = new SvcLogicContext();
        when(rc.getSvcLogicContext()).thenReturn(slc);
        rc.setSvcLogicContext(slc);
        OpenStackContext context = mock(OpenStackContext.class);
        NetworkService netSvc = mock(NetworkService.class);
        when(context.getNetworkService()).thenReturn(netSvc);
        String id = mg.SERVER_ID;
        Network network = mock(Network.class);
        Server server = mock(Server.class);
        List<Port> portList = new ArrayList<Port>(1);
        Port onlinePort = new Port();
        onlinePort.setName("Online Port");
        onlinePort.setPortState(Port.Status.ONLINE);
        onlinePort.setNetwork("ONLINE Port Network");
        portList.add(onlinePort);
        System.out.println(Arrays.toString(portList.toArray()));
        boolean requestFailedExceptionThrown = false;
        try {
            when(netSvc.getNetworkById(Mockito.anyString())).thenReturn(network);
            when(server.getPorts()).thenReturn(portList);
            when(network.getStatus()).thenReturn(Network.Status.OFFLINE.toString());
            underTest.checkVirtualMachineNetworkStatus(rc, server, context);
        } catch (ZoneException zoneException) {
            zoneException.printStackTrace();
        } catch (RequestFailedException requestFailedException) {
            System.out.println(requestFailedException.getOperation());
            requestFailedExceptionThrown =
                    (requestFailedException.getOperation().equals("VM Server Network is DOWN")) ? true : false;
        }
        assertTrue(requestFailedExceptionThrown);

    }

    @Test
    public void testCheckVirtualMachineNetworkStatusOfflinePort() {
        Port offlinePort = new Port();
        offlinePort.setName("Offline Port");
        offlinePort.setId("Offline Port");
        offlinePort.setPortState(Port.Status.OFFLINE);
        testCheckVirtualMachineNetworkStatusBaseTest("OFFLINE", offlinePort);
    }

    @Test
    public void testCheckVirtualMachineNetworkStatusPendingPort() {
        Port pendingPort = new Port();
        pendingPort.setName("Pending Port");
        pendingPort.setId("Pending Port");
        pendingPort.setPortState(Port.Status.PENDING);
        testCheckVirtualMachineNetworkStatusBaseTest("PENDING", pendingPort);
    }

    @Test
    public void testCheckVirtualMachineNetworkStatusUnkownPort() {
        Port unknownPort = new Port();
        unknownPort.setName("Unknown Port");
        unknownPort.setId("Unknown Port");
        unknownPort.setPortState(Port.Status.UNKNOWN);
        testCheckVirtualMachineNetworkStatusBaseTest("UNKNOWN", unknownPort);
    }

    private void testCheckVirtualMachineNetworkStatusBaseTest(String name, Port port) {
        MockGenerator mg = new MockGenerator(Status.RUNNING);
        RequestContext rc = mock(RequestContext.class);
        SvcLogicContext slc = new SvcLogicContext();
        when(rc.getSvcLogicContext()).thenReturn(slc);
        rc.setSvcLogicContext(slc);
        OpenStackContext context = mg.getContext();
        String id = mg.SERVER_ID;
        Server server = mock(Server.class);
        List<Port> portList = new ArrayList<Port>(1);
        portList.add(port);
        boolean requestFailedExceptionThrown = false;
        try {
            when(server.getPorts()).thenReturn(portList);
            underTest.checkVirtualMachineNetworkStatus(rc, server, context);
        } catch (ZoneException zoneException) {
            zoneException.printStackTrace();
        } catch (RequestFailedException requestFailedException) {
            requestFailedExceptionThrown =
                    (requestFailedException.getOperation().equals("VM Server Port status is " + name)) ? true : false;
        }
        assertTrue(requestFailedExceptionThrown);
    }


    @Test
    public void testCheckHypervisorDown() {
        testCheckHypervisorBaseTest(Hypervisor.State.DOWN, "Hypervisor status DOWN or NOT ENABLED");
    }

    @Test
    public void testCheckHypervisorUnknown() {
        testCheckHypervisorBaseTest(null, "Unable to determine Hypervisor status");
    }

    public void testCheckHypervisorBaseTest(Hypervisor.State state, String expectedExceptionOperation) {
        Server server = mock(Server.class);
        Hypervisor hypervisor = new Hypervisor();
        hypervisor.setStatus(Hypervisor.Status.DISABLED);
        hypervisor.setState(state);
        when(server.getHypervisor()).thenReturn(hypervisor);
        boolean requestFailedExceptionThrown = false;
        try {
            underTest.checkHypervisor(server);
        } catch (ZoneException zoneException) {
            zoneException.printStackTrace();
        } catch (RequestFailedException requestFailedException) {
            requestFailedExceptionThrown =
                    (requestFailedException.getOperation().equals(expectedExceptionOperation)) ? true : false;
        }
        assertTrue(requestFailedExceptionThrown);
    }
}
