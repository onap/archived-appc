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

package org.onap.appc.requesthandler.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.onap.appc.adapter.message.MessageAdapterFactory;
import org.onap.appc.adapter.message.Producer;
import org.onap.appc.domainmodel.lcm.RequestStatus;
import org.onap.appc.domainmodel.lcm.RuntimeContext;
import org.onap.appc.domainmodel.lcm.Status;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.requesthandler.exceptions.MultipleRecordsRetrievedException;
import org.onap.appc.transactionrecorder.TransactionRecorder;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;


/**
 * Test class for LocalRequestHandlerImpl
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({FrameworkUtil.class})
public class LocalRequestHandlerImplTest implements LocalRequestHanlderTestHelper {
    private final String requestId = "requestId";
    private final String vnfId = "vnfId";
    private final String subRequestId = "subRequestId";
    private final String originatorId = "originatorId";
    private final String ACCEPTED_IN_PROGRESS_Response = "{\"status-reason\":\"ACCEPTED\","
        + "\"status\":\"IN_PROGRESS\"}";
    private final String SUCCESSFUL_SUCCESSFUL_Response = "{\"status-reason\":\"SUCCESSFUL\","
        + "\"status\":\"SUCCESSFUL\"}";
    private final String RECEIVED_IN_PROGRESS_Response = "{\"status-reason\":\"RECEIVED\","
        + "\"status\":\"IN_PROGRESS\"}";
    private final String UNKNOWN_FAILED_Response = "{\"status-reason\":\"UNKNOWN\","
        + "\"status\":\"FAILED\"}";

    private LocalRequestHandlerImpl requestHandler;
    private TransactionRecorder recorder;

    @Before
    public void setUp() throws Exception {
        mockStatic(FrameworkUtil.class);
        Bundle myBundle = mock(Bundle.class);
        PowerMockito.when(FrameworkUtil.getBundle(any())).thenReturn(myBundle);

        BundleContext myBundleContext = mock(BundleContext.class);
        Mockito.when(myBundle.getBundleContext()).thenReturn(myBundleContext);

        ServiceReference svcRef = mock(ServiceReference.class);
        Mockito.when(myBundleContext.getServiceReference(MessageAdapterFactory.class.getName())).thenReturn(svcRef);

        Producer producer = mock(Producer.class);
        MessageAdapterFactory factory = mock(MessageAdapterFactory.class);
        Mockito.when(myBundleContext.getService(svcRef)).thenReturn(factory);
        Mockito.when(factory.createProducer(anyCollectionOf(String.class), anyString(), anyString(), anyString()))
            .thenReturn(producer);

        requestHandler = spy(new LocalRequestHandlerImpl());

        recorder = mock(TransactionRecorder.class);
        requestHandler.setTransactionRecorder(recorder);

        List<RequestStatus> result = Arrays.asList(RequestStatus.ACCEPTED);
        PowerMockito.when(recorder.getRecords(anyString(), anyString(), anyString(), anyString())).thenReturn(result);
    }

    /**
     * Test response which contains status=Successful, status-reason=Successful
     * <p>
     * Search criteria one = vnf-id + request-id + sub-request-id + originator-id
     */
    @Test
    public void testHandleRequestSuccessfulWithSearchCriteriaOne() throws APPCException {
        final String payload = "{\"request-id\":\"requestId\","
            + "\"sub-request-id\":\"subRequestId\","
            + "\"originator-id\":\"originatorId\"}";

        List<RequestStatus> result = Arrays.asList(RequestStatus.SUCCESSFUL);
        PowerMockito.when(recorder.getRecords(requestId, subRequestId, originatorId, vnfId)).thenReturn(result);
        RuntimeContext runtimeContext = createRequestHandlerRuntimeContext(vnfId, payload);

        requestHandler.handleRequest(runtimeContext);
        Assert.assertTrue(SUCCESSFUL_SUCCESSFUL_Response.equals(runtimeContext.getResponseContext().getPayload()));
    }

    /**
     * Test response which contains status=IN_PROGRESS, status-reason=RECEIVED
     * <p>
     * Search criteria one = vnf-id + request-id + sub-request-id + originator-id
     */
    @Test
    public void testHandleRequestReceivedWithSearchCriteriaOne() throws APPCException {
        final String payload = "{\"request-id\":\"requestId\","
            + "\"sub-request-id\":\"subRequestId\","
            + "\"originator-id\":\"originatorId\"}";

        List<RequestStatus> result = Arrays.asList(RequestStatus.RECEIVED);
        PowerMockito.when(recorder.getRecords(requestId, subRequestId, originatorId, vnfId)).thenReturn(result);
        RuntimeContext runtimeContext = createRequestHandlerRuntimeContext(vnfId, payload);

        requestHandler.handleRequest(runtimeContext);
        Assert.assertTrue(RECEIVED_IN_PROGRESS_Response.equals(runtimeContext.getResponseContext().getPayload()));
    }

    /**
     * Test response which contains status=FAILED, status-reason=UNKNOWN
     * <p>
     * Search criteria two = vnf-id + request-id + sub-request-id
     */
    @Test
    public void testHandleRequestFailedWithSearchCriteriaTwo() throws APPCException {
        final String payload = "{\"request-id\":\"requestId\","
            + "\"sub-request-id\":\"subRequestId\"}";

        List<RequestStatus> result = Arrays.asList(RequestStatus.UNKNOWN);
        PowerMockito.when(recorder.getRecords(requestId, subRequestId, null, vnfId)).thenReturn(result);
        RuntimeContext runtimeContext = createRequestHandlerRuntimeContext(vnfId, payload);

        requestHandler.handleRequest(runtimeContext);
        Assert.assertTrue(UNKNOWN_FAILED_Response.equals(runtimeContext.getResponseContext().getPayload()));
    }

    @Test
    public void testSuccessHandleRequest() throws Exception {
        RuntimeContext runtimeContext = createRequestHandlerRuntimeContext("vnfId",
            "{\"request-id\":\"request-id\"}");

        requestHandler.handleRequest(runtimeContext);
        Assert.assertTrue(ACCEPTED_IN_PROGRESS_Response.equals(runtimeContext.getResponseContext().getPayload()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHandleRequestVnfIdFailure() throws Exception {
        RuntimeContext runtimeContext = createRequestHandlerRuntimeContext(null, "{\"request-id\":\"request-id\"}");
        runtimeContext.getRequestContext().getActionIdentifiers().setVnfId(null);
        requestHandler.handleRequest(runtimeContext);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHandleRequestInvalidPayload() throws Exception {
        String incorrectPayload = "{\"RequestId\":\"requestToCheck\"}";
        RuntimeContext runtimeContext = createRequestHandlerRuntimeContext("vnfId", incorrectPayload);
        requestHandler.handleRequest(runtimeContext);
    }

    @Test
    public void testGetStatusOfRequestSuccess() throws Exception {
        List<RequestStatus> result = Arrays.asList(RequestStatus.ACCEPTED);
        PowerMockito.when(recorder.getRecords(requestId, null, null, vnfId)).thenReturn(result);

        RequestStatus status = Whitebox.invokeMethod(requestHandler,
            "getStatusOfRequest", requestId, null, null, vnfId);
        Assert.assertTrue(status.name().equals("ACCEPTED"));
    }

    @Test(expected = MultipleRecordsRetrievedException.class)
    public void testGetStatusOfRequest_MoreThanOne() throws Exception {
        List<RequestStatus> result = Arrays.asList(RequestStatus.ACCEPTED, RequestStatus.FAILED);
        PowerMockito.when(recorder.getRecords(requestId, null, null, vnfId)).thenReturn(result);
        RequestStatus status = Whitebox.invokeMethod(requestHandler,
            "getStatusOfRequest", requestId, null, null, vnfId);
        Assert.assertNull(status);
    }

    @Test
    public void testGetStatusOfRequestNotFound() throws Exception {
        List<RequestStatus> result = new ArrayList<>();
        PowerMockito.when(recorder.getRecords(requestId, null, null, vnfId)).thenReturn(result);

        RequestStatus status = Whitebox.invokeMethod(requestHandler,
            "getStatusOfRequest", requestId, null, null, vnfId);
        Assert.assertTrue(status.name().equals("NOT_FOUND"));
    }

    @Test
    public void testHandleRequestMultipleRecordException() throws Exception {
        final EELFLogger logger = spy(EELFManager.getInstance().getLogger(LocalRequestHandlerImpl.class));
        Whitebox.setInternalState(requestHandler, "logger", logger);
        RuntimeContext runtimeContext = createRequestHandlerRuntimeContext("vnfId",
            "{\"request-id\":\"request-id\"}");
        RequestStatus rs1 = RequestStatus.ABORTED;
        RequestStatus rs2 = RequestStatus.ACCEPTED;
        List<RequestStatus> rqList = new ArrayList<RequestStatus>(2);
        rqList.add(rs1);
        rqList.add(rs2);
        Mockito.when(recorder.getRecords(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString())).thenReturn(rqList);
        requestHandler.handleRequest(runtimeContext);
        Status status = new Status();
        status.setCode(315);
        status.setMessage("MULTIPLE REQUESTS FOUND - using search criteria: request-id=request-id AND vnf-id=vnfId");
        Mockito.verify(logger).debug("RequestStatus is set to MULTIPLE_REQUESTS_FOUND due to "
                + "MultipleRecordsRetrievedException", "MULTIPLE REQUESTS FOUND USING SEARCH CRITERIA: request-id="
                + "request-id AND vnf-id=vnfId");
    }
}