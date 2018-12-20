/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications (C) 2018 Ericsson
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

package org.onap.appc.oam;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.i18n.Msg;
import org.onap.appc.metricservice.MetricRegistry;
import org.onap.appc.metricservice.MetricService;
import org.onap.appc.metricservice.metric.Metric;
import org.onap.appc.metricservice.metric.MetricType;
import org.onap.appc.metricservice.metric.impl.DefaultPrimitiveCounter;
import org.onap.appc.metricservice.metric.impl.DispatchingFuntionMetricImpl;
import org.onap.appc.oam.processor.OamMmodeProcessor;
import org.onap.appc.oam.processor.OamRestartProcessor;
import org.onap.appc.oam.processor.OamStartProcessor;
import org.onap.appc.oam.processor.OamStopProcessor;
import org.onap.appc.oam.util.AsyncTaskHelper;
import org.onap.appc.oam.util.ConfigurationHelper;
import org.onap.appc.oam.util.OperationHelper;
import org.onap.appc.oam.util.StateHelper;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.AppcState;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.GetAppcStateOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.GetMetricsOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.MaintenanceModeInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.MaintenanceModeOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.RestartInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.RestartOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.StartInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.StartOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.StopInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.StopOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.common.header.CommonHeader;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.status.Status;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.powermock.reflect.Whitebox;
import com.att.aft.dme2.internal.google.common.collect.Iterables;
import com.att.eelf.configuration.EELFLogger;
import com.google.common.collect.ImmutableMap;


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
        Mockito.doReturn(mockProcessor).when(appcOam).getOamMmodeProcessor(Mockito.any(EELFLogger.class),
                Mockito.any(ConfigurationHelper.class), Mockito.any(StateHelper.class),
                Mockito.any(AsyncTaskHelper.class), Mockito.any(OperationHelper.class));
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
        Mockito.doReturn(mockProcessor).when(appcOam).getOamStartProcessor(Mockito.any(EELFLogger.class),
                Mockito.any(ConfigurationHelper.class), Mockito.any(StateHelper.class),
                Mockito.any(AsyncTaskHelper.class), Mockito.any(OperationHelper.class));
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
        //Mockito.doNothing().when(mockProcessor).setInitialLogProperties();
        Mockito.doReturn(mockProcessor).when(appcOam).getOamStopProcessor(Mockito.any(EELFLogger.class),
                Mockito.any(ConfigurationHelper.class), Mockito.any(StateHelper.class),
                Mockito.any(AsyncTaskHelper.class), Mockito.any(OperationHelper.class));
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
        Mockito.doReturn(mockProcessor).when(appcOam).getOamRestartProcessor(Mockito.any(EELFLogger.class),
                Mockito.any(ConfigurationHelper.class), Mockito.any(StateHelper.class),
                Mockito.any(AsyncTaskHelper.class), Mockito.any(OperationHelper.class));
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

    @Test
    public void testGetMetricsMetricDisabled() throws InterruptedException, ExecutionException {
        Whitebox.setInternalState(appcOam, "isMetricEnabled", false);
        Future<RpcResult<GetMetricsOutput>> result = appcOam.getMetrics();
        assertEquals("Metric Service not enabled", Iterables.get(result.get().getErrors(), 0).getMessage());
    }

    @Test
    public void testGetMetricsNoMetricsService() throws InterruptedException, ExecutionException, APPCException {
        Whitebox.setInternalState(appcOam, "isMetricEnabled", true);
        Mockito.doThrow(new APPCException()).when(mockOperationHelper).getService(MetricService.class);
        Future<RpcResult<GetMetricsOutput>> result = appcOam.getMetrics();
        assertEquals("Metric Service not found", Iterables.get(result.get().getErrors(), 0).getMessage());
    }

    @Test
    public void testGetMetricsNoMetrics() throws InterruptedException, ExecutionException, APPCException {
        Whitebox.setInternalState(appcOam, "isMetricEnabled", true);
        MetricService mockMetricService = mock(MetricService.class);
        Mockito.doReturn(mockMetricService).when(mockOperationHelper).getService(MetricService.class);
        Future<RpcResult<GetMetricsOutput>> result = appcOam.getMetrics();
        assertEquals("No metrics Registered", Iterables.get(result.get().getErrors(), 0).getMessage());
    }

    @Test
    public void testGetMetricsWithMetricRegistry() throws InterruptedException, ExecutionException, APPCException {
        Whitebox.setInternalState(appcOam, "isMetricEnabled", true);
        MetricService mockMetricService = mock(MetricService.class);
        MetricRegistry mockMetricRegistry = mock(MetricRegistry.class);
        Mockito.doReturn(mockMetricService).when(mockOperationHelper).getService(MetricService.class);
        Mockito.doReturn(ImmutableMap.of("TEST REGISTRY NAME", mockMetricRegistry)).when(mockMetricService).getAllRegistry();
        Metric metric = new DispatchingFuntionMetricImpl("TEST METRIC NAME", MetricType.COUNTER, 0, 0);
        Mockito.doReturn(new Metric[] {metric}).when(mockMetricRegistry).metrics();
        Future<RpcResult<GetMetricsOutput>> result = appcOam.getMetrics();
        assertEquals(1, result.get().getResult().getMetrics().size());
    }

    @Test
    public void testClose() throws Exception {
        ConfigurationHelper mockConfigurationHelper = mock(ConfigurationHelper.class);
        Mockito.doReturn("TEST APP NAME").when(mockConfigurationHelper).getAppcName();
        Whitebox.setInternalState(appcOam, "configurationHelper", mockConfigurationHelper);
        EELFLogger mockLogger = Mockito.mock(EELFLogger.class);
        Whitebox.setInternalState(appcOam, "logger", mockLogger);
        appcOam.close();
        Mockito.verify(mockLogger).info(Msg.COMPONENT_TERMINATED, "TEST APP NAME", "oam");
    }
}
