/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.appc.oam;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.oam.rev170303.AppcState;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.oam.rev170303.GetAppcStateOutput;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.oam.rev170303.MaintenanceModeInput;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.oam.rev170303.MaintenanceModeOutput;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.oam.rev170303.RestartInput;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.oam.rev170303.RestartOutput;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.oam.rev170303.StartInput;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.oam.rev170303.StartOutput;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.oam.rev170303.StopInput;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.oam.rev170303.StopOutput;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.oam.rev170303.common.header.CommonHeader;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.oam.rev170303.status.Status;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.openecomp.appc.oam.processor.OamMmodeProcessor;
import org.openecomp.appc.oam.processor.OamRestartProcessor;
import org.openecomp.appc.oam.processor.OamStartProcessor;
import org.openecomp.appc.oam.processor.OamStopProcessor;
import org.openecomp.appc.oam.util.OperationHelper;
import org.openecomp.appc.oam.util.StateHelper;
import org.osgi.framework.FrameworkUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;

@RunWith(PowerMockRunner.class)
@PrepareForTest({AppcOam.class, FrameworkUtil.class, Executors.class})
public class AppcOamTest {

    private AppcOam appcOam;
    private CommonHeader mockCommonHeader = mock(CommonHeader.class);
    private Status mockStatus = mock(Status.class);
    private OperationHelper mockOperationHelper = mock(OperationHelper.class);
    private StateHelper mockStateHelper = mock(StateHelper.class);

    @Before
    public void setUp() throws Exception {
        appcOam = spy(new AppcOam(null, null, null));

        Whitebox.setInternalState(appcOam, "stateHelper", mockStateHelper);
        Whitebox.setInternalState(appcOam, "operationHelper", mockOperationHelper);
    }

    @Test
    public void testMaintenanceMode() throws Exception {
        // mock processor creation
        OamMmodeProcessor mockProcessor = mock(OamMmodeProcessor.class);
        PowerMockito.mockStatic(OamMmodeProcessor.class);
        PowerMockito.whenNew(OamMmodeProcessor.class).withAnyArguments().thenReturn(mockProcessor);
        // mock input
        MaintenanceModeInput mockInput = mock(MaintenanceModeInput.class);
        Mockito.doReturn(mockCommonHeader).when(mockInput).getCommonHeader();
        // mock processor result
        Mockito.doReturn(mockStatus).when(mockProcessor).processRequest(mockInput);

        Future<RpcResult<MaintenanceModeOutput>> response = appcOam.maintenanceMode(mockInput);

        Assert.assertEquals("Should have common header", mockCommonHeader,
                response.get().getResult().getCommonHeader());
        Assert.assertEquals("Should have status", mockStatus, response.get().getResult().getStatus());
    }

    @Test
    public void testStart()  throws Exception {
        // mock processor creation
        OamStartProcessor mockProcessor = mock(OamStartProcessor.class);
        PowerMockito.mockStatic(OamStartProcessor.class);
        PowerMockito.whenNew(OamStartProcessor.class).withAnyArguments().thenReturn(mockProcessor);
        // mock input
        StartInput mockInput = mock(StartInput.class);
        Mockito.doReturn(mockCommonHeader).when(mockInput).getCommonHeader();
        // mock processor result
        Mockito.doReturn(mockStatus).when(mockProcessor).processRequest(mockInput);

        Future<RpcResult<StartOutput>> response = appcOam.start(mockInput);

        Assert.assertEquals("Should have common header", mockCommonHeader,
                response.get().getResult().getCommonHeader());
        Assert.assertEquals("Should have status", mockStatus, response.get().getResult().getStatus());
    }

    @Test
    public void testStop()  throws Exception {
        // mock processor creation
        OamStopProcessor mockProcessor = mock(OamStopProcessor.class);
        PowerMockito.mockStatic(OamStopProcessor.class);
        PowerMockito.whenNew(OamStopProcessor.class).withAnyArguments().thenReturn(mockProcessor);
        // mock input
        StopInput mockInput = mock(StopInput.class);
        Mockito.doReturn(mockCommonHeader).when(mockInput).getCommonHeader();
        // mock processor result
        Mockito.doReturn(mockStatus).when(mockProcessor).processRequest(mockInput);

        Future<RpcResult<StopOutput>> response = appcOam.stop(mockInput);

        Assert.assertEquals("Should have common header", mockCommonHeader,
                response.get().getResult().getCommonHeader());
        Assert.assertEquals("Should have status", mockStatus, response.get().getResult().getStatus());
    }

    @Test
    public void testRestart()  throws Exception {
        // mock processor creation
        OamRestartProcessor mockProcessor = mock(OamRestartProcessor.class);
        PowerMockito.mockStatic(OamRestartProcessor.class);
        PowerMockito.whenNew(OamRestartProcessor.class).withAnyArguments().thenReturn(mockProcessor);
        // mock input
        RestartInput mockInput = mock(RestartInput.class);
        Mockito.doReturn(mockCommonHeader).when(mockInput).getCommonHeader();
        // mock processor result
        Mockito.doReturn(mockStatus).when(mockProcessor).processRequest(mockInput);

        Future<RpcResult<RestartOutput>> response = appcOam.restart(mockInput);

        Assert.assertEquals("Should have common header", mockCommonHeader,
                response.get().getResult().getCommonHeader());
        Assert.assertEquals("Should have status", mockStatus, response.get().getResult().getStatus());
    }

    @Test
    public void testGetAppcState() throws Exception {
        AppcState appcState = AppcState.Started;
        Mockito.doReturn(appcState).when(mockStateHelper).getCurrentOamYangState();

        Future<RpcResult<GetAppcStateOutput>> state = appcOam.getAppcState();
        Assert.assertEquals("Should return the same state",
                appcState, state.get().getResult().getState());
    }
}
