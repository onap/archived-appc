/*
 * ============LICENSE_START=======================================================
 * Copyright (C) 2018-2019 Ericsson. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
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

package org.onap.appc.requesthandler.impl;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.onap.appc.domainmodel.lcm.ActionIdentifiers;
import org.onap.appc.domainmodel.lcm.CommonHeader;
import org.onap.appc.domainmodel.lcm.RequestContext;
import org.onap.appc.domainmodel.lcm.RequestStatus;
import org.onap.appc.domainmodel.lcm.ResponseContext;
import org.onap.appc.domainmodel.lcm.RuntimeContext;
import org.onap.appc.domainmodel.lcm.Status;
import org.onap.appc.domainmodel.lcm.TransactionRecord;
import org.onap.appc.domainmodel.lcm.VNFOperation;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.exceptions.InvalidInputException;
import org.onap.appc.executor.objects.LCMCommandStatus;
import org.onap.appc.lockmanager.api.LockException;
import org.onap.appc.messageadapter.MessageAdapter;
import org.onap.appc.metricservice.MetricRegistry;
import org.onap.appc.metricservice.MetricService;
import org.onap.appc.metricservice.impl.MetricRegistryImpl;
import org.onap.appc.metricservice.metric.DispatchingFunctionCounterBuilder;
import org.onap.appc.metricservice.metric.DispatchingFuntionMetric;
import org.onap.appc.metricservice.metric.MetricBuilderFactory;
import org.onap.appc.metricservice.metric.MetricType;
import org.onap.appc.metricservice.metric.impl.DispatchingFuntionMetricImpl;
import org.onap.appc.metricservice.policy.PolicyBuilderFactory;
import org.onap.appc.metricservice.policy.PublishingPolicy;
import org.onap.appc.metricservice.policy.ScheduledPolicyBuilder;
import org.onap.appc.requesthandler.exceptions.RequestValidationException;
import org.onap.appc.requesthandler.helper.RequestValidator;
import org.onap.appc.requesthandler.objects.RequestHandlerInput;
import org.onap.appc.requesthandler.objects.RequestHandlerOutput;
import org.onap.appc.transactionrecorder.TransactionRecorder;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFLogger.Level;
import com.att.eelf.configuration.EELFManager;

@RunWith(PowerMockRunner.class)
@PrepareForTest({FrameworkUtil.class})
public class AbstractRequestHandlerImplTest implements LocalRequestHanlderTestHelper {

    private AbstractRequestHandlerImpl requestHandler;
    private TransactionRecorder recorder;
    private final BundleContext bundleContext = Mockito.mock(BundleContext.class);
    private final Bundle bundleService = Mockito.mock(Bundle.class);
    private final ServiceReference sref = Mockito.mock(ServiceReference.class);
    private final MetricService metricService = Mockito.mock(MetricService.class);

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        setupForHandlerImplTests();
        requestHandler = spy(new LocalRequestHandlerImpl());
        recorder = mock(TransactionRecorder.class);
        requestHandler.setTransactionRecorder(recorder);
        List<RequestStatus> result = Arrays.asList(RequestStatus.ACCEPTED);
        PowerMockito.when(recorder.getRecords(anyString(), anyString(), anyString(), anyString())).thenReturn(result);
        final EELFLogger logger = EELFManager.getInstance().getLogger(AbstractRequestHandlerImpl.class);
        logger.setLevel(Level.TRACE);
        Whitebox.setInternalState(requestHandler, "logger", logger);
    }

    @Test
    public void testHandleRequestAbstractRequestHandler() {
        RequestHandlerInput rhi = setupTestForHandleRequest();
        RequestValidator rv = mock(RequestValidator.class);
        doNothing().when(requestHandler).handleRequest(Mockito.any(RuntimeContext.class));
        requestHandler.setRequestValidator(rv);
        Assert.assertTrue(requestHandler.handleRequest(rhi) instanceof RequestHandlerOutput);
    }

    @Test
    public void testHandleRequestAbstractRequestHandlerRequestValidationException() throws Exception {
        RequestHandlerInput rhi = setupTestForHandleRequest();
        RequestValidator rv = mock(RequestValidator.class);
        RequestValidationException rve = new RequestValidationException("TEST");
        rve.setTargetEntity("TEST_TARGET_ENTITY");
        rve.setTargetService("TEST_TARGET_SERVICE");
        rve.setLogMessage("TEST_LOG_MESSAGE");
        rve.setLcmCommandStatus(LCMCommandStatus.SUCCESS);
        rve.setParams(null);
        RequestHandlerOutput output = null;
        doThrow(rve).when(rv).validateRequest(Mockito.any(RuntimeContext.class));
        doNothing().when(requestHandler).handleRequest(Mockito.any(RuntimeContext.class));
        requestHandler.setRequestValidator(rv);
        output = requestHandler.handleRequest(rhi);
        Assert.assertEquals(LCMCommandStatus.SUCCESS.getResponseCode(),
                output.getResponseContext().getStatus().getCode());
    }

    @Test
    public void testHandleRequestAbstractRequestHandlerInvalidInputException() throws Exception {
        RequestHandlerInput rhi = setupTestForHandleRequest();
        RequestValidator rv = mock(RequestValidator.class);
        InvalidInputException iie = new InvalidInputException("TEST");
        RequestHandlerOutput output = null;
        doThrow(iie).when(rv).validateRequest(Mockito.any(RuntimeContext.class));
        doNothing().when(requestHandler).handleRequest(Mockito.any(RuntimeContext.class));
        requestHandler.setRequestValidator(rv);
        output = requestHandler.handleRequest(rhi);
        Assert.assertEquals(LCMCommandStatus.INVALID_INPUT_PARAMETER.getResponseCode(),
                output.getResponseContext().getStatus().getCode());
    }

    @Test
    public void testHandleRequestAbstractRequestHandlerLockException() throws Exception {
        RequestHandlerInput rhi = setupTestNoMetric();
        RequestValidator rv = mock(RequestValidator.class);
        LockException le = new LockException("TEST");
        RequestHandlerOutput output = null;
        doThrow(le).when(rv).validateRequest(Mockito.any(RuntimeContext.class));
        doNothing().when(requestHandler).handleRequest(Mockito.any(RuntimeContext.class));
        requestHandler.setRequestValidator(rv);
        output = requestHandler.handleRequest(rhi);
        Assert.assertEquals(LCMCommandStatus.LOCKED_VNF_ID.getResponseCode(),
                output.getResponseContext().getStatus().getCode());
    }

    @Test
    public void testHandleRequestAbstractRequestHandlerException() throws Exception {
        RequestHandlerInput rhi = setupTestNoMetric();
        RequestValidator rv = mock(RequestValidator.class);
        Exception exception = new Exception("TEST");
        RequestHandlerOutput output = null;
        doThrow(exception).when(rv).validateRequest(Mockito.any(RuntimeContext.class));
        doNothing().when(requestHandler).handleRequest(Mockito.any(RuntimeContext.class));
        requestHandler.setRequestValidator(rv);
        output = requestHandler.handleRequest(rhi);
        Assert.assertEquals(LCMCommandStatus.UNEXPECTED_ERROR.getResponseCode(),
                output.getResponseContext().getStatus().getCode());
    }

    @Test
    public void testOnRequestExecutionEnd() {
        RuntimeContext runtimeContext = spy(new RuntimeContext());
        TransactionRecord record = new TransactionRecord();
        record.setRequestState(RequestStatus.ACCEPTED);
        runtimeContext.setTransactionRecord(record);
        MessageAdapter messageAdapter = mock(MessageAdapter.class);
        requestHandler.setMessageAdapter(messageAdapter);
        RequestContext rc = mock(RequestContext.class);
        Status status = new Status();
        status.setCode(100);
        ResponseContext responseContext = spy(new ResponseContext());
        Mockito.doReturn(status).when(responseContext).getStatus();
        Mockito.doReturn(VNFOperation.ActionStatus).when(rc).getAction();
        Mockito.doReturn(rc).when(runtimeContext).getRequestContext();
        Mockito.doReturn(responseContext).when(runtimeContext).getResponseContext();
        requestHandler.onRequestExecutionEnd(runtimeContext);
        Mockito.verify(runtimeContext, times(3)).getTransactionRecord();
    }

    @Test
    public void testGetInprogressRequestCount() throws APPCException {
        int i = 0;
        Mockito.doReturn(19).when(recorder).getInProgressRequestsCount();
        i = requestHandler.getInprogressRequestCount();
        Assert.assertEquals(19, i);
    }

    @Test
    public void testMetric() throws Exception {
        PowerMockito.mockStatic(FrameworkUtil.class);
        PowerMockito.when(FrameworkUtil.getBundle(MetricService.class)).thenReturn(bundleService);
        PowerMockito.when(bundleService.getBundleContext()).thenReturn(bundleContext);
        PowerMockito.when(bundleContext.getServiceReference(MetricService.class.getName())).thenReturn(sref);
        PowerMockito.when(bundleContext.getService(sref)).thenReturn(metricService);
        MetricRegistry metricRegistry = Mockito.mock(MetricRegistry.class);
        DispatchingFuntionMetric dispatchingFunctionMetric = Mockito.mock(DispatchingFuntionMetric.class);
        DispatchingFunctionCounterBuilder dispatchingFunctionCounterBuilder = Mockito.mock(DispatchingFunctionCounterBuilder.class);
        MetricBuilderFactory metricBuilderFactory = Mockito.mock(MetricBuilderFactory.class);
        Mockito.when(dispatchingFunctionCounterBuilder.withName("DISPATCH_FUNCTION")).thenReturn(dispatchingFunctionCounterBuilder);
        Mockito.when(dispatchingFunctionCounterBuilder.withType(MetricType.COUNTER)).thenReturn(dispatchingFunctionCounterBuilder);
        Mockito.when(dispatchingFunctionCounterBuilder.withAcceptRequestValue(0)).thenReturn(dispatchingFunctionCounterBuilder);
        Mockito.when(dispatchingFunctionCounterBuilder.withRejectRequestValue(0)).thenReturn(dispatchingFunctionCounterBuilder);
        Mockito.when(dispatchingFunctionCounterBuilder.build()).thenReturn(dispatchingFunctionMetric);
        Mockito.when(metricBuilderFactory.dispatchingFunctionCounterBuilder()).thenReturn(dispatchingFunctionCounterBuilder);
        Mockito.when(metricRegistry.metricBuilderFactory()).thenReturn(metricBuilderFactory);
        Mockito.when(metricService.createRegistry("APPC")).thenReturn(metricRegistry);
        Mockito.when(metricRegistry.register(dispatchingFunctionMetric)).thenReturn(true);

        PublishingPolicy publishingPolicy = Mockito.mock(PublishingPolicy.class);
        PolicyBuilderFactory policyBuilderFactory = Mockito.mock(PolicyBuilderFactory.class);
        ScheduledPolicyBuilder scheduledPolicyBuilder = Mockito.mock(ScheduledPolicyBuilder.class);
        Mockito.when(policyBuilderFactory.scheduledPolicyBuilder()).thenReturn(scheduledPolicyBuilder);
        Mockito.when(scheduledPolicyBuilder.withPublishers(Mockito.any())).thenReturn(scheduledPolicyBuilder);
        Mockito.when(scheduledPolicyBuilder.withMetrics(Mockito.any())).thenReturn(scheduledPolicyBuilder);
        Mockito.when(scheduledPolicyBuilder.build()).thenReturn(publishingPolicy);
        Mockito.when(metricRegistry.policyBuilderFactory()).thenReturn(policyBuilderFactory);
        Whitebox.invokeMethod(requestHandler, "initMetric");
        Mockito.verify(publishingPolicy).init();
    }

    @Test
    public void testMetricNullMetricService() throws Exception {
        expectedEx.expect(NullPointerException.class);
        expectedEx.expectMessage("org.onap.appc.metricservice.MetricService is null. " +
                    "Failed to init Metric");
        Whitebox.invokeMethod(requestHandler, "initMetric");
    }

    private RequestHandlerInput setupTestForHandleRequest() {
        Whitebox.setInternalState(requestHandler, "isMetricEnabled", true);
        MetricRegistry metricRegistry = spy(new MetricRegistryImpl("TEST_METRIC_REGISTRY"));
        DispatchingFuntionMetricImpl metric =
                spy(new DispatchingFuntionMetricImpl("DISPATCH_FUNCTION", MetricType.COUNTER, 0, 0));
        metricRegistry.register(metric);
        doNothing().when(metric).incrementAcceptedRequest();
        Whitebox.setInternalState(RequestHandlerImpl.class, "metricRegistry", metricRegistry);
        RequestHandlerInput rhi = new RequestHandlerInput();
        RequestContext rc = new RequestContext();
        CommonHeader ch = new CommonHeader();
        rc.setCommonHeader(ch);
        ch.setRequestId("TEST");
        ch.setTimestamp(new Date(System.currentTimeMillis()));
        VNFOperation vo = VNFOperation.findByString("ActionStatus");
        rc.setAction(vo);
        ActionIdentifiers ai = new ActionIdentifiers();
        rc.setActionIdentifiers(ai);
        rhi.setRequestContext(rc);
        return rhi;
    }

    private RequestHandlerInput setupTestNoMetric() {
        RequestHandlerInput rhi = new RequestHandlerInput();
        RequestContext rc = new RequestContext();
        CommonHeader ch = new CommonHeader();
        rc.setCommonHeader(ch);
        ch.setRequestId("TEST");
        ch.setTimestamp(new Date(System.currentTimeMillis()));
        VNFOperation vo = VNFOperation.findByString("ActionStatus");
        rc.setAction(vo);
        ActionIdentifiers ai = new ActionIdentifiers();
        rc.setActionIdentifiers(ai);
        rhi.setRequestContext(rc);
        return rhi;
    }
}
