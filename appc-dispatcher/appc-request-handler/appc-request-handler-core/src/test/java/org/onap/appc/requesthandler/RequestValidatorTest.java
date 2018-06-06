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

package org.onap.appc.requesthandler;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseFactory;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.message.BasicStatusLine;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.onap.appc.domainmodel.lcm.ActionIdentifiers;
import org.onap.appc.domainmodel.lcm.CommonHeader;
import org.onap.appc.domainmodel.lcm.Flags;
import org.onap.appc.domainmodel.lcm.RequestContext;
import org.onap.appc.domainmodel.lcm.ResponseContext;
import org.onap.appc.domainmodel.lcm.RuntimeContext;
import org.onap.appc.domainmodel.lcm.Status;
import org.onap.appc.domainmodel.lcm.TransactionRecord;
import org.onap.appc.domainmodel.lcm.VNFContext;
import org.onap.appc.domainmodel.lcm.VNFOperation;
import org.onap.appc.exceptions.InvalidInputException;
import org.onap.appc.lockmanager.api.LockManager;
import org.onap.appc.requesthandler.exceptions.DuplicateRequestException;
import org.onap.appc.requesthandler.exceptions.LCMOperationsDisabledException;
import org.onap.appc.requesthandler.exceptions.RequestValidationException;
import org.onap.appc.requesthandler.impl.RequestHandlerImpl;
import org.onap.appc.requesthandler.impl.RequestValidatorImpl;
import org.onap.appc.requesthandler.objects.RequestHandlerInput;
import org.onap.appc.rest.client.RestClientInvoker;
import org.onap.appc.transactionrecorder.TransactionRecorder;
import org.onap.appc.validationpolicy.RequestValidationPolicy;
import org.onap.appc.validationpolicy.executors.ActionInProgressRuleExecutor;
import org.onap.appc.validationpolicy.objects.RuleResult;
import org.onap.appc.workflow.WorkFlowManager;
import org.onap.appc.workflow.objects.WorkflowExistsOutput;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;
import org.onap.ccsdk.sli.adaptors.aai.AAIService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.UUID;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;

@RunWith(PowerMockRunner.class)
@PrepareForTest({FrameworkUtil.class, TransactionRecorder.class,
    RequestHandlerImpl.class, RequestValidatorImpl.class, TransactionRecorder.class})
public class RequestValidatorTest {

    private final EELFLogger logger = EELFManager.getInstance().getLogger(RequestHandlerTest.class);

    private RequestValidatorImpl requestValidator;

    private AAIService aaiAdapter;
    private WorkFlowManager workflowManager;
    private LCMStateManager lcmStateManager;
    private TransactionRecorder transactionRecorder;
    private LockManager lockManager;
    private RestClientInvoker client;
    private RequestValidationPolicy requestValidationPolicy;

    private final BundleContext bundleContext = Mockito.mock(BundleContext.class);
    private final Bundle bundleService = Mockito.mock(Bundle.class);
    private final ServiceReference sref = Mockito.mock(ServiceReference.class);

    @Before
    public void init() throws Exception {
        AAIService aaiService = Mockito.mock(AAIService.class);
        PowerMockito.mockStatic(FrameworkUtil.class);
        PowerMockito.when(FrameworkUtil.getBundle(AAIService.class)).thenReturn(bundleService);
        PowerMockito.when(bundleService.getBundleContext()).thenReturn(bundleContext);
        PowerMockito.when(bundleContext.getServiceReference(AAIService.class.getName())).thenReturn(sref);
        PowerMockito.when(bundleContext.getService(sref)).thenReturn(aaiService);
        PowerMockito.when(aaiService.query(
            anyString(), anyBoolean(), anyString(), anyString(), anyString(), anyString(), anyObject()))
            .thenAnswer(new Answer<SvcLogicResource.QueryStatus>() {
                @Override
                public SvcLogicResource.QueryStatus answer(InvocationOnMock invocation) throws Exception {
                    Object[] args = invocation.getArguments();
                    SvcLogicContext ctx = (SvcLogicContext) args[6];
                    String prefix = (String) args[4];
                    String key = (String) args[3];
                    if (key.contains("'28'")) {
                        return SvcLogicResource.QueryStatus.FAILURE;
                    } else if (key.contains("'8'")) {
                        return SvcLogicResource.QueryStatus.NOT_FOUND;
                    } else {
                        ctx.setAttribute(prefix + ".vnf-type", "FIREWALL");
                        ctx.setAttribute(prefix + ".orchestration-status", "Instantiated");
                    }
                    return SvcLogicResource.QueryStatus.SUCCESS;
                }
            });
        PowerMockito.when(aaiService.update(anyString(), anyString(), anyObject(), anyString(), anyObject()))
            .thenReturn(SvcLogicResource.QueryStatus.SUCCESS);

        aaiAdapter = Mockito.mock(AAIService.class);
        workflowManager = Mockito.mock(WorkFlowManager.class);
        lcmStateManager = Mockito.mock(LCMStateManager.class);
        transactionRecorder=Mockito.mock(TransactionRecorder.class);
        lockManager=Mockito.mock(LockManager.class);
        client=Mockito.mock(RestClientInvoker.class);
        requestValidationPolicy=Mockito.mock(RequestValidationPolicy.class);

        requestValidator = new RequestValidatorImpl();
        requestValidator.setWorkflowManager(workflowManager);
        requestValidator.setLcmStateManager(lcmStateManager);
        requestValidator.setTransactionRecorder(transactionRecorder);
        requestValidator.setLockManager(lockManager);
        requestValidator.setClient(client);
        requestValidator.setRequestValidationPolicy(requestValidationPolicy);

        Mockito.when(lcmStateManager.isLCMOperationEnabled()).thenReturn(true);

    }

    public AAIService getAaiadapter() {
        return this.aaiAdapter;
    }

    private RequestHandlerInput getRequestHandlerInput(String vnfID,
                                                       VNFOperation action,
                                                       int ttl,
                                                       boolean force,
                                                       String originatorId,
                                                       String requestId,
                                                       String subRequestId,
                                                       Date timeStamp) {
        String API_VERSION = "2.0.0";
        RequestHandlerInput input = new RequestHandlerInput();
        RuntimeContext runtimeContext = createRuntimeContextWithSubObjects();
        RequestContext requestContext = runtimeContext.getRequestContext();
        input.setRequestContext(requestContext);
        requestContext.getActionIdentifiers().setVnfId(vnfID);
        requestContext.setAction(action);
        if (action != null) {
            input.setRpcName(convertActionNameToUrl(action.name()));
        } else {
            input.setRpcName(null);
        }
        requestContext.getCommonHeader().setRequestId(requestId);
        requestContext.getCommonHeader().setSubRequestId(subRequestId);
        requestContext.getCommonHeader().setOriginatorId(originatorId);
        requestContext.getCommonHeader().getFlags().setTtl(ttl);
        requestContext.getCommonHeader().getFlags().setForce(force);
        requestContext.getCommonHeader().getTimeStamp();
        requestContext.getCommonHeader().setApiVer(API_VERSION);
        requestContext.getCommonHeader().setTimestamp(timeStamp);
        return input;
    }

    //@Test
    public void testNullVnfID() throws Exception {
        logger.debug("=====================testNullVnfID=============================");
        Mockito.when(workflowManager.workflowExists(anyObject()))
            .thenReturn(new WorkflowExistsOutput(true, true));
        RequestHandlerInput input = this.getRequestHandlerInput(null, VNFOperation.Configure, 30,
            false, UUID.randomUUID().toString(), UUID.randomUUID().toString(),
            UUID.randomUUID().toString(), new Date());
        Exception ex = null;
        RuntimeContext runtimeContext = putInputToRuntimeContext(input);
        try {
            requestValidator.validateRequest(runtimeContext);
        } catch (InvalidInputException e) {
            ex = e;
        }
        assertNotNull(ex);
        logger.debug("=====================testNullVnfID=============================");
    }
    //@Test
    public void testPositiveFlowWithConfigure() throws Exception {
        logger.debug("=====================testPositiveFlowWithConfigure=============================");
        Mockito.when(workflowManager.workflowExists(anyObject()))
            .thenReturn(new WorkflowExistsOutput(true, true));
        Mockito.when(transactionRecorder.isTransactionDuplicate(anyObject())).thenReturn(false);
        Mockito.when(lockManager.getLockOwner(anyString())).thenReturn(null);
        ActionInProgressRuleExecutor action=Mockito.mock(ActionInProgressRuleExecutor.class);

        Mockito.when(requestValidationPolicy.getInProgressRuleExecutor()).thenReturn(action);
        Mockito.when(action.executeRule(anyString(),anyList())).thenReturn(RuleResult.ACCEPT);
        HttpResponseFactory factory = new DefaultHttpResponseFactory();
        HttpResponse response=factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, null),new HttpClientContext());
         /*String jsonResponse="{\n" +
         "\t\"status\" : \"success\",\n" +
         "\t\"scope-overlap\" : true\n" +
          "}";*/
        
         String jsonResponse= "{\"output\":{\"status\":{\"message\":\"success\",\"code\":\"400\"},\"response-info\":{\"requestId\":\"AnynonRepetitiveNumber/String\",\"block\":{\"requestOverlap\":\"true\"}}}}";
        
        InputStream stream= new ByteArrayInputStream(jsonResponse.getBytes());
        BasicHttpEntity a=new BasicHttpEntity();
        a.setContent(stream);
        response.setEntity(a);
        Mockito.when(client.doPost(anyString(),anyString())).thenReturn(response);
        RequestHandlerInput input = this.getRequestHandlerInput("1", VNFOperation.Configure, 30,
            false, UUID.randomUUID().toString(), "123",
            UUID.randomUUID().toString(), new Date());
        Exception ex = null;
        RuntimeContext runtimeContext = putInputToRuntimeContext(input);
        try {
            requestValidator.validateRequest(runtimeContext);
        } catch (Exception e) {
            ex = e;
        }
        assertNull(ex);
        logger.debug("testPositiveFlowWithConfigure");
        logger.debug("=====================testPositiveFlowWithConfigure=============================");
    }

    //@Test(expected= RequestValidationException.class)
    public void testWithRuleResultAsReject() throws Exception {
        logger.debug("=====================testWithRuleResultAsReject=============================");
        Mockito.when(workflowManager.workflowExists(anyObject()))
                .thenReturn(new WorkflowExistsOutput(true, true));
        Mockito.when(transactionRecorder.isTransactionDuplicate(anyObject())).thenReturn(false);
        Mockito.when(lockManager.getLockOwner(anyString())).thenReturn(null);
        ActionInProgressRuleExecutor action=Mockito.mock(ActionInProgressRuleExecutor.class);

        Mockito.when(requestValidationPolicy.getInProgressRuleExecutor()).thenReturn(action);
        Mockito.when(action.executeRule(anyString(),anyList())).thenReturn(RuleResult.REJECT);
        HttpResponseFactory factory = new DefaultHttpResponseFactory();
        HttpResponse response=factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, null),new HttpClientContext());
        /*String jsonResponse="{\n" +
                "\t\"status\" : \"success\",\n" +
                "\t\"scope-overlap\" : true\n" +
                "}";*/
        String jsonResponse= "{\"output\":{\"status\":{\"message\":\"success\",\"code\":\"400\"},\"response-info\":{\"requestId\":\"AnynonRepetitiveNumber/String\",\"block\":{\"requestOverlap\":\"true\"}}}}";
        InputStream stream= new ByteArrayInputStream(jsonResponse.getBytes());
        BasicHttpEntity a=new BasicHttpEntity();
        a.setContent(stream);
        response.setEntity(a);
        Mockito.when(client.doPost(anyString(),anyString())).thenReturn(response);
        RequestHandlerInput input = this.getRequestHandlerInput("1", VNFOperation.Configure, 30,
                false, UUID.randomUUID().toString(), "200",
                UUID.randomUUID().toString(), new Date());
        Exception ex = null;
        RuntimeContext runtimeContext = putInputToRuntimeContext(input);

        requestValidator.validateRequest(runtimeContext);
        logger.debug("testWithRuleResultAsReject");
        logger.debug("=====================testWithRuleResultAsReject=============================");
    }

    //@Test
    public void testVnfNotFound() throws Exception {
        logger.debug("=====================testVnfNotFound=============================");
        Mockito.when(workflowManager.workflowExists(anyObject()))
            .thenReturn(new WorkflowExistsOutput(true, true));
        RequestHandlerInput input = this.getRequestHandlerInput("8", VNFOperation.Configure, 30,
            false, UUID.randomUUID().toString(), UUID.randomUUID().toString(),
            UUID.randomUUID().toString(), new Date());
        Exception ex = null;
        RuntimeContext runtimeContext = putInputToRuntimeContext(input);
        try {
            requestValidator.validateRequest(runtimeContext);
        } catch (Exception e) {
            ex = e;
        }
        assertNotNull(ex);
        logger.debug("=====================testVnfNotFound=============================");
    }


    //@Test
    public void testNullCommand() throws Exception {
        logger.debug("=====================testNullCommand=============================");
        Mockito.when(workflowManager.workflowExists(anyObject()))
            .thenReturn(new WorkflowExistsOutput(true, true));
        RequestHandlerInput input = this.getRequestHandlerInput("7", null, 30,
            false, UUID.randomUUID().toString(), UUID.randomUUID().toString(),
            UUID.randomUUID().toString(), new Date());
        Exception ex = null;
        RuntimeContext runtimeContext = putInputToRuntimeContext(input);
        try {
            requestValidator.validateRequest(runtimeContext);
        } catch (InvalidInputException e) {
            ex = e;
        }
        assertNotNull(ex);
        logger.debug("=====================testNullCommand=============================");
    }

    //@Test
    public void testNullVnfIDAndCommand() throws Exception {
        logger.debug("=====================testNullVnfIDAndCommand=============================");
        Mockito.when(workflowManager.workflowExists(anyObject()))
            .thenReturn(new WorkflowExistsOutput(true, true));
        RequestHandlerInput input = this.getRequestHandlerInput(null, null, 30,
            false, UUID.randomUUID().toString(), UUID.randomUUID().toString(),
            UUID.randomUUID().toString(), new Date());
        Exception ex = null;
        RuntimeContext runtimeContext = putInputToRuntimeContext(input);
        try {
            requestValidator.validateRequest(runtimeContext);
        } catch (InvalidInputException e) {
            ex = e;
        }
        assertNotNull(ex);
        logger.debug("=====================testNullVnfIDAndCommand=============================");
    }

    //@Test
    public void testWorkflowNotFound() throws Exception {
        logger.debug("=====================testWorkflowNotFound=============================");
        Mockito.when(workflowManager.workflowExists(anyObject()))
            .thenReturn(new WorkflowExistsOutput(false, false));
        RequestHandlerInput input = this.getRequestHandlerInput("10", VNFOperation.Configure, 30,
            false, UUID.randomUUID().toString(), UUID.randomUUID().toString(),
            UUID.randomUUID().toString(), new Date());
        Exception ex = null;
        RuntimeContext runtimeContext = putInputToRuntimeContext(input);
        try {
            requestValidator.validateRequest(runtimeContext);
        } catch (Exception e) {
            ex = e;
        }
        assertNotNull(ex);
        logger.debug("=====================testWorkflowNotFound=============================");
    }

    //@Test
    public void testAAIDown() throws Exception {
        logger.debug("=====================testAAIDown=============================");
        RequestHandlerInput input = this.getRequestHandlerInput("28", VNFOperation.Configure, 30,
            false, UUID.randomUUID().toString(), UUID.randomUUID().toString(),
            UUID.randomUUID().toString(), new Date());
        Exception ex = null;
        RuntimeContext runtimeContext = putInputToRuntimeContext(input);
        try {
            requestValidator.validateRequest(runtimeContext);

        } catch (Exception e) {
            ex = e;
        }
        assertNotNull(ex);
        logger.debug("=====================testAAIDown=============================");
    }

    //@Test
    public void testNegativeFlowWithTimeStamp() throws Exception {
        logger.debug("=====================testNegativeFlowWithTimeStamp=============================");
        Date now = new Date();
        Date past = new Date();
        past.setTime(now.getTime() - 1000000);
        RequestHandlerInput input = this.getRequestHandlerInput("35", VNFOperation.Configure, 30,
            false, UUID.randomUUID().toString(), UUID.randomUUID().toString(),
            UUID.randomUUID().toString(), past);
        Exception ex = null;
        RuntimeContext runtimeContext = putInputToRuntimeContext(input);

        try {
            requestValidator.validateRequest(runtimeContext);
        } catch (Exception e) {
            ex = e;
        }
        assertNotNull(ex);
        logger.debug("testNegativeFlowWithTimeStamp");
        logger.debug("=====================testNegativeFlowWithTimeStamp=============================");
    }

    //@Test(expected= DuplicateRequestException.class)
    public void rejectDuplicateRequest() throws Exception {
        String originatorID = UUID.randomUUID().toString();
        String requestID = UUID.randomUUID().toString();
        String subRequestID = UUID.randomUUID().toString();
        Mockito.when(transactionRecorder.isTransactionDuplicate(anyObject())).thenReturn(true);
        Mockito.when(workflowManager.workflowExists(anyObject()))
            .thenReturn(new WorkflowExistsOutput(true, true));
        RequestHandlerInput input = this.getRequestHandlerInput("301", VNFOperation.Configure, 0,
            false, originatorID, requestID, subRequestID, new Date());
        Exception ex = null;
        RuntimeContext runtimeContext = putInputToRuntimeContext(input);
        requestValidator.validateRequest(runtimeContext);

    }

    //@Test
    public void testLockOperation() throws Exception {

        testOperation("no-matter", VNFOperation.Lock);
    }

    //TODO needs to be fixed
    //@Test
    public void testUnlockOperation() throws Exception {
        testOperation("no-matter", VNFOperation.Unlock);
    }

    //TODO needs to be fixed
    //@Test
    public void testCheckLockOperation() throws Exception {
        testOperation("no-matter", VNFOperation.CheckLock);
    }


    //@Test(expected = LCMOperationsDisabledException.class)
    public void testLCMOperationsDisabled() throws Exception {
        Mockito.when(lcmStateManager.isLCMOperationEnabled()).thenReturn(false);
        testOperation("no-matter", VNFOperation.Configure);
    }

    private void testOperation(String resource, VNFOperation operation) throws Exception {
        String originatorID = UUID.randomUUID().toString();
        String requestID = UUID.randomUUID().toString();
        String subRequestID = UUID.randomUUID().toString();
        RequestHandlerInput input = this.getRequestHandlerInput(resource, operation, 0,
            false, originatorID, requestID, subRequestID, new Date());
        RuntimeContext runtimeContext = putInputToRuntimeContext(input);
        requestValidator.validateRequest(runtimeContext);
    }

    private RuntimeContext createRuntimeContextWithSubObjects() {
        RuntimeContext runtimeContext = new RuntimeContext();
        RequestContext requestContext = new RequestContext();
        runtimeContext.setRequestContext(requestContext);
        ResponseContext responseContext = createResponseContextWithSuObjects();
        runtimeContext.setResponseContext(responseContext);
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

    private String convertActionNameToUrl(String action) {
        String regex = "([a-z])([A-Z]+)";
        String replacement = "$1-$2";
        return action.replaceAll(regex, replacement)
            .toLowerCase();
    }

    private RuntimeContext putInputToRuntimeContext(RequestHandlerInput input) {
        RuntimeContext runtimeContext = createRuntimeContextWithSubObjects();
        runtimeContext.setRequestContext(input.getRequestContext());
        runtimeContext.setRpcName(input.getRpcName());
        runtimeContext.getVnfContext().setId(input.getRequestContext().getActionIdentifiers().getVnfId());
        runtimeContext.getRequestContext().getActionIdentifiers().setServiceInstanceId(UUID.randomUUID().toString());
        TransactionRecord record= new TransactionRecord();
        record.setTargetId(input.getRequestContext().getActionIdentifiers().getVnfId());
        record.setOriginatorId(input.getRequestContext().getCommonHeader().getOriginatorId());
        record.setRequestId(input.getRequestContext().getCommonHeader().getRequestId());
        record.setSubRequestId(input.getRequestContext().getCommonHeader().getSubRequestId());
        record.setOriginTimestamp(input.getRequestContext().getCommonHeader().getTimeStamp().toInstant());
        record.setServiceInstanceId(UUID.randomUUID().toString());
        record.setOperation(input.getRequestContext().getAction());
        runtimeContext.setTransactionRecord(record);

        return runtimeContext;
    }
}
