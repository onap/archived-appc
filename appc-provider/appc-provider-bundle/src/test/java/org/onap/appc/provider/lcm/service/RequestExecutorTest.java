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

package org.onap.appc.provider.lcm.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.Payload;
import org.onap.appc.domainmodel.lcm.ActionLevel;
import org.onap.appc.domainmodel.lcm.CommonHeader;
import org.onap.appc.domainmodel.lcm.RequestContext;
import org.onap.appc.domainmodel.lcm.ResponseContext;
import org.onap.appc.domainmodel.lcm.Status;
import org.onap.appc.domainmodel.lcm.VNFOperation;
import org.onap.appc.executor.objects.LCMCommandStatus;
import org.onap.appc.executor.objects.Params;
import org.onap.appc.i18n.Msg;
import org.onap.appc.requesthandler.RequestHandler;
import org.onap.appc.requesthandler.objects.RequestHandlerInput;
import org.onap.appc.requesthandler.objects.RequestHandlerOutput;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collection;
import java.util.Iterator;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({FrameworkUtil.class})
public class RequestExecutorTest {
    private final VNFOperation vnfOperation = VNFOperation.ActionStatus;
    private final ActionLevel actionLevel = ActionLevel.MGMT;

    private Bundle mockBundle = mock(Bundle.class);
    private BundleContext mockBundleContext = mock(BundleContext.class);

    private RequestHandlerInput mockInput = mock(RequestHandlerInput.class);
    private RequestContext mockRequestContext = mock(RequestContext.class);
    private CommonHeader mockCommonHeader = mock(CommonHeader.class);
    private RequestHandler mockHandler = mock(RequestHandler.class);

    private RequestExecutor requestExecutor;

    @Before
    public void setUp() throws Exception {
        mockStatic(FrameworkUtil.class);
        PowerMockito.when(FrameworkUtil.getBundle(any())).thenReturn(mockBundle);

        Mockito.doReturn(mockRequestContext).when(mockInput).getRequestContext();
        Mockito.doReturn(mockCommonHeader).when(mockRequestContext).getCommonHeader();

        requestExecutor = spy(new RequestExecutor());
    }

    @Test
    public void testExecuteRequest() throws Exception {
        // test RequestHandler is null
        Mockito.doReturn(null).when(mockBundle).getBundleContext();
        Mockito.doReturn(actionLevel).when(mockRequestContext).getActionLevel();
        Mockito.doReturn(vnfOperation).when(mockRequestContext).getAction();

        Params params = new Params().addParam("errorMsg", requestExecutor.CANNOT_PROCESS);
        LCMCommandStatus lcmCommandStatus = LCMCommandStatus.REJECTED;
        Msg msg = Msg.REQUEST_HANDLER_UNAVAILABLE;

        RequestHandlerOutput output = requestExecutor.executeRequest(mockInput);
        ResponseContext responseContext = output.getResponseContext();
        Status status = responseContext.getStatus();
        Assert.assertEquals("Should have rejected status code",
            lcmCommandStatus.getResponseCode(), status.getCode());
        Assert.assertEquals("Should have rejected CANNOT_PROCESS status message",
            lcmCommandStatus.getFormattedMessage(params), status.getMessage());
        Assert.assertEquals("Should have the same commonHeader",
            mockCommonHeader, responseContext.getCommonHeader());
        Mockito.verify(requestExecutor, times(1)).getRequestHandler(any());
        Mockito.verify(requestExecutor, times(1))
            .createRequestHandlerOutput(any(), any(), any(), any());

        // to get RequestHandler
        ServiceReference<RequestHandler> mockSvcRefs = mock(ServiceReference.class);
        Iterator mockIterator = mock(Iterator.class);
        Mockito.doReturn(mockSvcRefs).when(mockIterator).next();

        Collection<ServiceReference<RequestHandler>> mockSvcRefCollection = mock(Collection.class);
        Mockito.doReturn(1).when(mockSvcRefCollection).size();
        Mockito.doReturn(mockIterator).when(mockSvcRefCollection).iterator();

        Mockito.doReturn(mockBundleContext).when(mockBundle).getBundleContext();
        Mockito.doReturn(mockSvcRefCollection).when(mockBundleContext)
            .getServiceReferences(eq(RequestHandler.class), anyString());

        Mockito.doReturn(mockHandler).when(mockBundleContext).getService(mockSvcRefs);

        // Skip test RequestHandler.quiesce throws exception as it does not throw exception

        // test normal return
        RequestHandlerOutput mockOutput = mock(RequestHandlerOutput.class);
        Mockito.doReturn(mockOutput).when(mockHandler).handleRequest(mockInput);
        output = requestExecutor.executeRequest(mockInput);
        Assert.assertEquals("Should return mockOuput", mockOutput, output);
    }

    @Test
    public void testGetRequestHandler() throws Exception {
        // test null BundleContext
        Mockito.doReturn(null).when(mockBundle).getBundleContext();
        RequestHandler requestHandler = requestExecutor.getRequestHandler(actionLevel);
        Assert.assertTrue("Should return null", requestHandler == null);

        // test successful returning RequestHandler
        ServiceReference<RequestHandler> mockSvcRefs = mock(ServiceReference.class);
        Iterator mockIterator = mock(Iterator.class);
        Mockito.doReturn(mockSvcRefs).when(mockIterator).next();

        Collection<ServiceReference<RequestHandler>> mockSvcRefCollection = mock(Collection.class);
        Mockito.doReturn(1).when(mockSvcRefCollection).size();
        Mockito.doReturn(mockIterator).when(mockSvcRefCollection).iterator();

        Mockito.doReturn(mockBundleContext).when(mockBundle).getBundleContext();
        Mockito.doReturn(mockSvcRefCollection).when(mockBundleContext)
            .getServiceReferences(eq(RequestHandler.class), anyString());

        Mockito.doReturn(mockHandler).when(mockBundleContext).getService(mockSvcRefs);

        requestHandler = requestExecutor.getRequestHandler(actionLevel);
        Assert.assertEquals("Should return RequestHandler", mockHandler, requestHandler);
    }

    @Test(expected = RuntimeException.class)
    public void testGetRequesetHandlerWithInvalidSyntaxException() throws Exception {
        Mockito.doReturn(mockBundleContext).when(mockBundle).getBundleContext();
        Mockito.doThrow(new InvalidSyntaxException("testing message", "testing filter"))
            .when(mockBundleContext).getServiceReferences(eq(RequestHandler.class), anyString());

        requestExecutor.getRequestHandler(actionLevel);
    }


    @Test(expected = RuntimeException.class)
    public void testGetRequesetHandlerWithCannotFoundSvc() throws Exception {
        Collection<ServiceReference<RequestHandler>> mockSvcRefCollection = mock(Collection.class);
        Mockito.doReturn(2).when(mockSvcRefCollection).size();

        Mockito.doReturn(mockBundleContext).when(mockBundle).getBundleContext();
        Mockito.doReturn(mockSvcRefCollection).when(mockBundleContext)
            .getServiceReferences(eq(RequestHandler.class), anyString());

        requestExecutor.getRequestHandler(actionLevel);
    }

    @Test
    public void testCreateRequestHandlerOutput() throws Exception {
        // test exception without message
        Exception testException = new Exception();
        Params params = new Params().addParam("errorMsg", testException.toString());
        LCMCommandStatus lcmCommandStatus = LCMCommandStatus.REJECTED;
        Msg msg = Msg.REQUEST_HANDLER_UNAVAILABLE;

        RequestHandlerOutput output =
            requestExecutor.createRequestHandlerOutput(mockInput, lcmCommandStatus, msg, testException);
        ResponseContext responseContext = output.getResponseContext();
        Status status = responseContext.getStatus();
        Assert.assertEquals("Should have the same status code",
            lcmCommandStatus.getResponseCode(), status.getCode());
        Assert.assertEquals("Should have the proper exception to String status message",
            lcmCommandStatus.getFormattedMessage(params), status.getMessage());
        Assert.assertEquals("Should have the same commonHeader",
            mockCommonHeader, responseContext.getCommonHeader());

        // test exception with message
        testException = new Exception("testing exception");
        params = new Params().addParam("errorMsg", testException.getMessage());
        lcmCommandStatus = LCMCommandStatus.UNEXPECTED_ERROR;
        msg = Msg.EXCEPTION_CALLING_DG;

        output =
            requestExecutor.createRequestHandlerOutput(mockInput, lcmCommandStatus, msg, testException);
        responseContext = output.getResponseContext();
        status = responseContext.getStatus();
        Assert.assertEquals("Should have the same status code",
            lcmCommandStatus.getResponseCode(), status.getCode());
        Assert.assertEquals("Should have the proper exception to String status message",
            lcmCommandStatus.getFormattedMessage(params), status.getMessage());
        Assert.assertEquals("Should have the same commonHeader",
            mockCommonHeader, responseContext.getCommonHeader());
    }

    @Test
    public void testGetPayload() throws Exception {
        RequestHandlerOutput mockOutput = mock(RequestHandlerOutput.class);
        ResponseContext mockResponseContext = mock(ResponseContext.class);
        // test null response context
        Mockito.doReturn(null).when(mockOutput).getResponseContext();
        Assert.assertTrue("Should return null with null requestContext",
            requestExecutor.getPayload(mockOutput) == null);

        // test null payload
        Mockito.doReturn(mockResponseContext).when(mockOutput).getResponseContext();
        Mockito.doReturn(null).when(mockResponseContext).getPayload();
        Assert.assertTrue("Should return null with null payload",
            requestExecutor.getPayload(mockOutput) == null);

        // test empty payload
        Mockito.doReturn("").when(mockResponseContext).getPayload();
        Assert.assertTrue("Should return null with empty payload",
            requestExecutor.getPayload(mockOutput) == null);

        // test proper payload
        String testingPayload = "testing payload";
        Mockito.doReturn(testingPayload).when(mockResponseContext).getPayload();
        Payload payload = requestExecutor.getPayload(mockOutput);
        Assert.assertEquals("Should return null with empty payload", testingPayload, payload.getValue());
    }

}
