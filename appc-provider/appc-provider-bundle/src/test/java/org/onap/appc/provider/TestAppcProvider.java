/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 Ericsson
 * ================================================================================
 * Modifications Copyright (C) 2019 IBM.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.onap.appc.provider.topology.TopologyService;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.MigrateInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.ModifyConfigInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.RebuildInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.RestartInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.SnapshotInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.VmstatuscheckInput;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.EvacuateInput;

public class TestAppcProvider {

    private AppcProvider appcProvider;
    private DataBroker dataBroker2;
    private NotificationPublishService notificationProviderService;
    private RpcProviderRegistry rpcProviderRegistry;
    private AppcProviderClient appcProviderClient;
    private TopologyService topologyService;
    private RpcResult result;

    @Before
    public void setUp() {

        topologyService = Mockito.mock(TopologyService.class);
        result = Mockito.mock(RpcResult.class);
        appcProvider = new AppcProvider(dataBroker2, notificationProviderService, rpcProviderRegistry,
                appcProviderClient);
    }

    @Test
    public void testModifyConfig() throws InterruptedException, ExecutionException {
        ModifyConfigInput input = Mockito.mock(ModifyConfigInput.class);
        AppcProvider appcProviderspy = Mockito.spy(appcProvider);
        when(appcProviderspy.getTopologyService()).thenReturn(topologyService);
        when(topologyService.modifyConfig(anyObject(), anyObject())).thenReturn(result);
        assertTrue(appcProviderspy.modifyConfig(input).isDone());
    }

    @Test
    public void testRebuild() throws InterruptedException, ExecutionException {
        RebuildInput input = Mockito.mock(RebuildInput.class);
        AppcProvider appcProviderspy = Mockito.spy(appcProvider);
        when(appcProviderspy.getTopologyService()).thenReturn(topologyService);
        when(topologyService.rebuild(anyObject(), anyObject())).thenReturn(result);
        assertTrue(appcProviderspy.rebuild(input).isDone());
    }

    @Test
    public void testRestart() throws InterruptedException, ExecutionException {
        RestartInput input = Mockito.mock(RestartInput.class);
        AppcProvider appcProviderspy = Mockito.spy(appcProvider);
        when(appcProviderspy.getTopologyService()).thenReturn(topologyService);
        when(topologyService.restart(anyObject(), anyObject())).thenReturn(result);
        assertTrue(appcProviderspy.restart(input).isDone());
    }

    @Test
    public void testMigrate() throws InterruptedException, ExecutionException {
        MigrateInput input = Mockito.mock(MigrateInput.class);
        AppcProvider appcProviderspy = Mockito.spy(appcProvider);
        when(appcProviderspy.getTopologyService()).thenReturn(topologyService);
        when(topologyService.migrate(anyObject(), anyObject())).thenReturn(result);
        assertTrue(appcProviderspy.migrate(input).isDone());
    }

    @Test
    public void testSnapshot() throws InterruptedException, ExecutionException {
        SnapshotInput input = Mockito.mock(SnapshotInput.class);
        AppcProvider appcProviderspy = Mockito.spy(appcProvider);
        when(appcProviderspy.getTopologyService()).thenReturn(topologyService);
        when(topologyService.snapshot(anyObject(), anyObject())).thenReturn(result);
        assertTrue(appcProviderspy.snapshot(input).isDone());
    }

    @Test
    public void testVmstatuscheck() throws InterruptedException, ExecutionException {
        VmstatuscheckInput input = Mockito.mock(VmstatuscheckInput.class);
        AppcProvider appcProviderspy = Mockito.spy(appcProvider);
        when(appcProviderspy.getTopologyService()).thenReturn(topologyService);
        when(topologyService.vmstatuscheck(anyObject(), anyObject())).thenReturn(result);
        assertTrue(appcProviderspy.vmstatuscheck(input).isDone());
    }

    @Test
    public void testClose() throws Exception {
        appcProvider.close();
        ExecutorService executor = (ExecutorService) Whitebox.getInternalState(appcProvider, "executor");
        assertTrue(executor.isShutdown());
    }

    @Test
    public void testGetClient() {
        assertEquals(appcProviderClient, appcProvider.getClient());
    }

    @Test
    public void testEvacuate() {
        EvacuateInput input = null;
        assertNull(appcProvider.evacuate(input));
    }
}
