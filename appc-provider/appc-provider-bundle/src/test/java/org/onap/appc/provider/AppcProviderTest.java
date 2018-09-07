/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.test.AbstractDataBrokerTest;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.EvacuateInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.EvacuateOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.MigrateInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.MigrateOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.ModifyConfigInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.ModifyConfigOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.RebuildInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.RebuildOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.RestartInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.RestartOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.SnapshotInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.SnapshotOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.VmstatuscheckInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.VmstatuscheckOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.common.request.header.CommonRequestHeader;
import org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.config.payload.ConfigPayload;
import org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.vnf.resource.VnfResource;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.onap.appc.provider.topology.TopologyService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.lang.reflect.Field;
import org.onap.appc.provider.Whitebox;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class AppcProviderTest extends AbstractDataBrokerTest {

    @Mock
    private CommonRequestHeader commonRequestHeader;
    @Mock
    private ConfigPayload configPayload;
    @Mock
    private VnfResource vnfResource;
    @Mock
    private TopologyService topologyService;

    private AppcProvider provider;
    private DataBroker dataBroker;


    /**
     * The @Before annotation is defined in the AbstractDataBrokerTest class. The method setupWithDataBroker is invoked
     * from inside the @Before method and is used to initialize the databroker with objects for a test runs. In our case
     * we use this oportunity to create an instance of our provider and initialize it (which registers it as a listener
     * etc). This method runs before every @Test method below.
     */
    @Override
    protected void setupWithDataBroker(DataBroker dataBroker) {
        super.setupWithDataBroker(dataBroker);

        this.dataBroker = dataBroker;
    }
/*
    @Before
    public void setUp() throws Exception {
        NotificationProviderService nps = mock(NotificationProviderService.class);
        RpcProviderRegistry registry = mock(RpcProviderRegistry.class);
        BindingAwareBroker.RpcRegistration rpcRegistration = mock(BindingAwareBroker.RpcRegistration.class);
        
        doReturn(rpcRegistration).when(registry).addRpcImplementation(any(), any());

        provider = spy(new AppcProvider(dataBroker, nps, registry));

        doReturn(topologyService).when(provider).getTopologyService();
    }

    @Test
    public void testConstructor() throws Exception {
        Object executorService = Whitebox.getInternalState(provider, "executor");
        Assert.assertNotNull(executorService);
        Object internalRpcRegistration = Whitebox.getInternalState(provider,"rpcRegistration");
        Assert.assertNotNull(internalRpcRegistration);
    }

    @Test
    public void testClose() throws Exception {
        ExecutorService executorService = spy(Executors.newFixedThreadPool(1));
        Whitebox.setInternalState(provider, "executor", executorService);
        BindingAwareBroker.RpcRegistration rpcRegistration = mock(BindingAwareBroker.RpcRegistration.class);
        Whitebox.setInternalState(provider, "rpcRegistration", rpcRegistration);
        provider.close();

        verify(executorService, times(1)).shutdown();
        verify(rpcRegistration, times(1)).close();
    }

    @Test
    public void testModifyConfig() throws Exception {
        ModifyConfigInput modifyConfigInput = mock(ModifyConfigInput.class);
        doReturn(commonRequestHeader).when(modifyConfigInput).getCommonRequestHeader();
        doReturn(configPayload).when(modifyConfigInput).getConfigPayload();
        // mock output
        RpcResult<ModifyConfigOutput> modifyConfigOutput = mock(RpcResult.class);
        doReturn(modifyConfigOutput).when(topologyService).modifyConfig(any(), any());

        Future<RpcResult<ModifyConfigOutput>> rpcResultFuture = provider.modifyConfig(modifyConfigInput);

        Assert.assertNotNull(rpcResultFuture);
    }

    @Test
    public void testRebuild() throws Exception {
        RebuildInput input = mock(RebuildInput.class);
        RpcResult<RebuildOutput> output = mock(RpcResult.class);
        doReturn(vnfResource).when(input).getVnfResource();
        doReturn(output).when(topologyService).rebuild(any(), any());

        Future<RpcResult<RebuildOutput>> rpcResultFuture = provider.rebuild(input);

        Assert.assertNotNull(rpcResultFuture);
    }

    @Test
    public void testRestart() throws Exception {
        RestartInput input = mock(RestartInput.class);
        RpcResult<RestartOutput> output = mock(RpcResult.class);
        doReturn(vnfResource).when(input).getVnfResource();
        doReturn(output).when(topologyService).restart(any(), any());

        Future<RpcResult<RestartOutput>> rpcResultFuture = provider.restart(input);

        Assert.assertNotNull(rpcResultFuture);
    }

    @Test
    public void testMigrate() throws Exception {
        MigrateInput input = mock(MigrateInput.class);
        RpcResult<MigrateOutput> output = mock(RpcResult.class);
        doReturn(vnfResource).when(input).getVnfResource();
        doReturn(output).when(topologyService).migrate(any(), any());

        Future<RpcResult<MigrateOutput>> rpcResultFuture = provider.migrate(input);

        Assert.assertNotNull(rpcResultFuture);
    }

    @Test
    public void testEvacuate() throws Exception {
        EvacuateInput input = mock(EvacuateInput.class);
        doReturn(vnfResource).when(input).getVnfResource();

        Future<RpcResult<EvacuateOutput>> rpcResultFuture = provider.evacuate(input);

        Assert.assertNull(rpcResultFuture);
    }

    @Test
    public void testSnapshot() throws Exception {
        SnapshotInput input = mock(SnapshotInput.class);
        RpcResult<SnapshotOutput> output = mock(RpcResult.class);
        doReturn(vnfResource).when(input).getVnfResource();
        doReturn(output).when(topologyService).snapshot(any(), any());

        Future<RpcResult<SnapshotOutput>> rpcResultFuture = provider.snapshot(input);

        Assert.assertNotNull(rpcResultFuture);
    }

    @Test
    public void testVmstatuscheck() throws Exception {
        VmstatuscheckInput input = mock(VmstatuscheckInput.class);
        RpcResult<VmstatuscheckOutput> output = mock(RpcResult.class);
        doReturn(vnfResource).when(input).getVnfResource();
        doReturn(output).when(topologyService).vmstatuscheck(any(), any());

        Future<RpcResult<VmstatuscheckOutput>> rpcResultFuture = provider.vmstatuscheck(input);

        Assert.assertNotNull(rpcResultFuture);
    }

    @After
    public void tearDown() throws Exception {
        if (provider != null) {
            provider.close();
        }
    }
    */
}