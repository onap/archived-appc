/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications Copyright (C) 2019 Ericsson
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

package org.onap.appc.executor.impl;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.mockito.Matchers.anyObject;
import static org.junit.Assert.assertNotNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.onap.appc.domainmodel.lcm.*;
import org.onap.appc.executor.impl.CommandTask;
import org.onap.appc.executor.impl.objects.CommandRequest;
import org.onap.appc.executor.objects.CommandExecutorInput;
import org.onap.appc.requesthandler.RequestHandler;
import org.onap.appc.workflow.WorkFlowManager;
import org.onap.appc.workflow.objects.WorkflowResponse;
import org.onap.ccsdk.sli.adaptors.aai.AAIRequest;
import org.onap.ccsdk.sli.adaptors.aai.AAIService;
import org.onap.ccsdk.sli.adaptors.aai.AAIServiceException;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * @author sushilma
 * @since September 04, 2017
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({FrameworkUtil.class, InetAddress.class})
public class TestCommandTask {
    CommandTask task;
    private RequestHandler requestHandler;
    private WorkFlowManager workflowManager;
    private AAIService aaiService;

    private BundleContext bundleContext = Mockito.mock(BundleContext.class);
    private Bundle bundleService = Mockito.mock(Bundle.class);
    private ServiceReference sref = Mockito.mock(ServiceReference.class);

    private static final String TTL_FLAG = "TTL";
    private static final String API_VERSION = "2.0.0";
    private static final String ORIGINATOR_ID = "1";

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void init() throws UnknownHostException {
        aaiService = Mockito.mock(AAIService.class);
        PowerMockito.mockStatic(FrameworkUtil.class);
        PowerMockito.when(FrameworkUtil.getBundle(AAIService.class)).thenReturn(bundleService);
        PowerMockito.when(bundleService.getBundleContext()).thenReturn(bundleContext);
        PowerMockito.when(bundleContext.getServiceReference(AAIService.class.getName())).thenReturn(sref);
        PowerMockito.when(bundleContext.getService(sref)).thenReturn(aaiService);
        requestHandler = Mockito.mock(RequestHandler.class);
        workflowManager = Mockito.mock(WorkFlowManager.class);
        task = Mockito.spy(new CommandTask(requestHandler, workflowManager));
    }

    @Test
    public void testRunPositive() {
        Mockito.when(workflowManager.executeWorkflow(anyObject())).thenReturn(getWorkflowResponse());
        task.setWorkflowManager(workflowManager);
        task.setRequestHandler(requestHandler);
        task.setCommandRequest(getCommandRequest("FIREWALL", 30, new Date(), "11", setTTLInFlags("30"),
                VNFOperation.Sync, "1", "1.0"));
        task.run();
        Assert.assertNotNull(
                task.getCommandRequest().getCommandExecutorInput().getRuntimeContext().getResponseContext());
    }

    @Test
    public void testRunPositiveTerminateFailed() {
        Mockito.when(workflowManager.executeWorkflow(anyObject())).thenReturn(getWorkflowResponse());
        task.setWorkflowManager(workflowManager);
        task.setRequestHandler(requestHandler);
        task.setCommandRequest(getCommandRequest("FIREWALL", 30, new Date(), "12", setTTLInFlags("30"),
                VNFOperation.Terminate, "2", "1.0"));
        setResponseContext(300, task.getCommandRequest().getCommandExecutorInput().getRuntimeContext());
        task.run();
        Assert.assertNotNull(
                task.getCommandRequest().getCommandExecutorInput().getRuntimeContext().getResponseContext());
    }

    @Test
    public void testRunPositiveTerminateSucceeded() throws SvcLogicException, AAIServiceException {
        AAIService mockAai = Mockito.mock(AAIService.class);

        Mockito.when(mockAai.query(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(SvcLogicContext.class)))
                .thenReturn(SvcLogicResource.QueryStatus.SUCCESS);
        Mockito.when(mockAai.getRequestFromResource("generic-vnf")).thenReturn(Mockito.mock(AAIRequest.class));
        Mockito.when(mockAai.delete(Mockito.any(AAIRequest.class), Mockito.anyString())).thenReturn(true);
        Whitebox.setInternalState(task, "aaiService", mockAai);
        Mockito.when(workflowManager.executeWorkflow(anyObject())).thenReturn(getWorkflowResponse());
        task.setWorkflowManager(workflowManager);
        task.setRequestHandler(requestHandler);
        task.setCommandRequest(getCommandRequest("FIREWALL", 30, new Date(), "12", setTTLInFlags("30"),
                VNFOperation.Terminate, "2", "1.0"));
        setResponseContext(100, task.getCommandRequest().getCommandExecutorInput().getRuntimeContext());
        task.run();
        assertNotNull(mockAai);
    }

    @Test
    public void testRunPositiveTerminateNotFound() throws SvcLogicException, AAIServiceException {
        AAIService mockAai = Mockito.mock(AAIService.class);

        Mockito.when(mockAai.query(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(SvcLogicContext.class)))
                .thenReturn(SvcLogicResource.QueryStatus.NOT_FOUND);
        Mockito.when(mockAai.getRequestFromResource("generic-vnf")).thenReturn(Mockito.mock(AAIRequest.class));
        Mockito.when(mockAai.delete(Mockito.any(AAIRequest.class), Mockito.anyString())).thenReturn(true);
        Whitebox.setInternalState(task, "aaiService", mockAai);
        Mockito.when(workflowManager.executeWorkflow(anyObject())).thenReturn(getWorkflowResponse());
        task.setWorkflowManager(workflowManager);
        task.setRequestHandler(requestHandler);
        task.setCommandRequest(getCommandRequest("FIREWALL", 30, new Date(), "12", setTTLInFlags("30"),
                VNFOperation.Terminate, "2", "1.0"));
        setResponseContext(100, task.getCommandRequest().getCommandExecutorInput().getRuntimeContext());
        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("VNF not found for vnf_id = ");
        task.run();
    }

    @Test
    public void testRunPositiveTerminateFailure() throws SvcLogicException, AAIServiceException {
        AAIService mockAai = Mockito.mock(AAIService.class);

        Mockito.when(mockAai.query(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(SvcLogicContext.class)))
                .thenReturn(SvcLogicResource.QueryStatus.FAILURE);
        Mockito.when(mockAai.getRequestFromResource("generic-vnf")).thenReturn(Mockito.mock(AAIRequest.class));
        Mockito.when(mockAai.delete(Mockito.any(AAIRequest.class), Mockito.anyString())).thenReturn(true);
        Whitebox.setInternalState(task, "aaiService", mockAai);
        Mockito.when(workflowManager.executeWorkflow(anyObject())).thenReturn(getWorkflowResponse());
        task.setWorkflowManager(workflowManager);
        task.setRequestHandler(requestHandler);
        task.setCommandRequest(getCommandRequest("FIREWALL", 30, new Date(), "12", setTTLInFlags("30"),
                VNFOperation.Terminate, "2", "1.0"));
        setResponseContext(100, task.getCommandRequest().getCommandExecutorInput().getRuntimeContext());
        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("Error Querying AAI with vnfID = ");
        task.run();
    }

    @Test
    public void testRunPositiveAaiServiceException() throws SvcLogicException, AAIServiceException {
        AAIService mockAai = Mockito.mock(AAIService.class);

        Mockito.when(mockAai.query(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(SvcLogicContext.class)))
                .thenReturn(SvcLogicResource.QueryStatus.SUCCESS);
        Mockito.when(mockAai.getRequestFromResource("generic-vnf")).thenReturn(Mockito.mock(AAIRequest.class));
        Mockito.when(mockAai.delete(Mockito.any(AAIRequest.class), Mockito.anyString()))
                .thenThrow(new AAIServiceException("ERROR IN DELETE"));
        Whitebox.setInternalState(task, "aaiService", mockAai);
        Mockito.when(workflowManager.executeWorkflow(anyObject())).thenReturn(getWorkflowResponse());
        task.setWorkflowManager(workflowManager);
        task.setRequestHandler(requestHandler);
        task.setCommandRequest(getCommandRequest("FIREWALL", 30, new Date(), "12", setTTLInFlags("30"),
                VNFOperation.Terminate, "2", "1.0"));
        setResponseContext(100, task.getCommandRequest().getCommandExecutorInput().getRuntimeContext());
        expectedEx.expect(RuntimeException.class);
        expectedEx.expectCause(allOf(isA(AAIServiceException.class),
                hasProperty("message", is("ERROR IN DELETE"))));
        task.run();
    }

    @Test
    public void testRunSvcLogicException() throws SvcLogicException, AAIServiceException {
        AAIService mockAai = Mockito.mock(AAIService.class);

        Mockito.when(mockAai.query(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.any(SvcLogicContext.class)))
                .thenThrow(new SvcLogicException());
        Mockito.when(mockAai.getRequestFromResource("generic-vnf")).thenReturn(Mockito.mock(AAIRequest.class));
        Mockito.when(mockAai.delete(Mockito.any(AAIRequest.class), Mockito.anyString())).thenReturn(true);
        Whitebox.setInternalState(task, "aaiService", mockAai);
        Mockito.when(workflowManager.executeWorkflow(anyObject())).thenReturn(getWorkflowResponse());
        task.setWorkflowManager(workflowManager);
        task.setRequestHandler(requestHandler);
        task.setCommandRequest(getCommandRequest("FIREWALL", 30, new Date(), "12", setTTLInFlags("30"),
                VNFOperation.Terminate, "2", "1.0"));
        setResponseContext(100, task.getCommandRequest().getCommandExecutorInput().getRuntimeContext());
        expectedEx.expect(RuntimeException.class);
        expectedEx.expectCause(isA(SvcLogicException.class));
        task.run();
    }

    private WorkflowResponse getWorkflowResponse () {
        WorkflowResponse wfResponse = new WorkflowResponse();
        ResponseContext responseContext = createResponseContextWithObjects();
        wfResponse.setResponseContext(responseContext);
        responseContext.setPayload("");
        wfResponse.getResponseContext().getStatus().setCode(100);
        return wfResponse;
    }

    private ResponseContext createResponseContextWithObjects() {
        ResponseContext responseContext = new ResponseContext();
        CommonHeader commonHeader = new CommonHeader();
        Flags flags = new Flags();
        Status status = new Status();
        responseContext.setCommonHeader(commonHeader);
        responseContext.setStatus(status);
        commonHeader.setFlags(flags);
        return responseContext;
    }

    private void setResponseContext(int statusCode, RuntimeContext runtimeContext) {
        ResponseContext responseContext = createResponseContextWithObjects();
        responseContext.getStatus().setCode(statusCode);
        runtimeContext.setResponseContext(responseContext);
    }

    private CommandRequest getCommandRequest(String vnfType, Integer ttl, Date timeStamp, String requestId,
            Map<String, Object> flags, VNFOperation command, String vnfId, String vnfVersion) {

        CommandExecutorInput commandExecutorInput = populateCommandExecutorInput(vnfType, ttl, vnfVersion, timeStamp,
                API_VERSION, requestId, ORIGINATOR_ID, "", command, vnfId, "");
        CommandRequest request = new CommandRequest(commandExecutorInput);
        request.setCommandExecutorInput(commandExecutorInput);
        request.setCommandInTimeStamp(new Date());
        return request;
    }

    private CommandExecutorInput populateCommandExecutorInput(String vnfType, int ttl, String vnfVersion,
            Date timeStamp, String apiVersion, String requestId, String originatorID, String subRequestID,
            VNFOperation action, String vnfId, String payload) {
        CommandExecutorInput commandExecutorInput = createCommandExecutorInputWithSubObjects();
        RuntimeContext runtimeContext = commandExecutorInput.getRuntimeContext();
        RequestContext requestContext = runtimeContext.getRequestContext();
        CommonHeader commonHeader = requestContext.getCommonHeader();
        ResponseContext responseContext = createResponseContextWithSuObjects();
        runtimeContext.setResponseContext(responseContext);

        commonHeader.getFlags().setTtl(ttl);
        commonHeader.setApiVer(apiVersion);
        commonHeader.setTimestamp(timeStamp);
        commonHeader.setRequestId(requestId);
        commonHeader.setSubRequestId(subRequestID);
        commonHeader.setOriginatorId(originatorID);
        requestContext.setAction(action);
        requestContext.setPayload(payload);
        ActionIdentifiers actionIdentifiers = requestContext.getActionIdentifiers();
        actionIdentifiers.setVnfId(vnfId);
        actionIdentifiers.setServiceInstanceId("test");
        VNFContext vnfContext = runtimeContext.getVnfContext();
        vnfContext.setType(vnfType);
        vnfContext.setId(vnfId);
        vnfContext.setVersion(vnfVersion);
        return commandExecutorInput;
    }

    private CommandExecutorInput createCommandExecutorInputWithSubObjects() {
        CommandExecutorInput commandExecutorInput = new CommandExecutorInput();
        RuntimeContext runtimeContext = createRuntimeContextWithSubObjects();
        commandExecutorInput.setRuntimeContext(runtimeContext);
        return commandExecutorInput;
    }

    private ResponseContext createResponseContextWithSuObjects() {
        ResponseContext responseContext = new ResponseContext();
        CommonHeader commonHeader = new CommonHeader();
        Flags flags = new Flags();
        Status status = new Status();
        responseContext.setCommonHeader(commonHeader);
        responseContext.setStatus(status);
        commonHeader.setFlags(flags);
        return responseContext;
    }

    private RuntimeContext createRuntimeContextWithSubObjects() {
        RuntimeContext runtimeContext = new RuntimeContext();
        RequestContext requestContext = new RequestContext();
        runtimeContext.setRequestContext(requestContext);
        CommonHeader commonHeader = new CommonHeader();
        requestContext.setCommonHeader(commonHeader);
        Flags flags = new Flags();
        commonHeader.setFlags(flags);
        ActionIdentifiers actionIdentifiers = new ActionIdentifiers();
        requestContext.setActionIdentifiers(actionIdentifiers);
        VNFContext vnfContext = new VNFContext();
        runtimeContext.setVnfContext(vnfContext);
        return runtimeContext;
    }

    private Map<String, Object> setTTLInFlags(String value) {
        Map<String, Object> flags = new HashMap<String, Object>();
        if (value != null || !("".equalsIgnoreCase(value))) {
            flags.put(TTL_FLAG, value);
        }
        return flags;
    }
}
