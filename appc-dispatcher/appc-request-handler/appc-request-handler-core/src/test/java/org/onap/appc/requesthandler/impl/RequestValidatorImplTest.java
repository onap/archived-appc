/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Copyright (C) 2018 Ericsson. All rights reserved.
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

package org.onap.appc.requesthandler.impl;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFLogger.Level;
import com.att.eelf.configuration.EELFManager;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHttpResponse;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.onap.appc.configuration.Configuration;
import org.onap.appc.configuration.ConfigurationFactory;
import org.onap.appc.domainmodel.lcm.ActionIdentifiers;
import org.onap.appc.domainmodel.lcm.CommonHeader;
import org.onap.appc.domainmodel.lcm.Flags;
import org.onap.appc.domainmodel.lcm.Flags.Mode;
import org.onap.appc.domainmodel.lcm.RequestContext;
import org.onap.appc.domainmodel.lcm.RequestStatus;
import org.onap.appc.domainmodel.lcm.ResponseContext;
import org.onap.appc.domainmodel.lcm.RuntimeContext;
import org.onap.appc.domainmodel.lcm.TransactionRecord;
import org.onap.appc.domainmodel.lcm.VNFOperation;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.lockmanager.api.LockException;
import org.onap.appc.lockmanager.api.LockManager;
import org.onap.appc.requesthandler.LCMStateManager;
import org.onap.appc.requesthandler.exceptions.DGWorkflowNotFoundException;
import org.onap.appc.requesthandler.exceptions.LCMOperationsDisabledException;
import org.onap.appc.requesthandler.exceptions.RequestValidationException;
import org.onap.appc.requesthandler.exceptions.WorkflowNotFoundException;
import org.onap.appc.rest.client.RestClientInvoker;
import org.onap.appc.transactionrecorder.TransactionRecorder;
import org.onap.appc.validationpolicy.RequestValidationPolicy;
import org.onap.appc.validationpolicy.executors.RuleExecutor;
import org.onap.appc.validationpolicy.objects.RuleResult;
import org.onap.appc.workflow.WorkFlowManager;
import org.onap.appc.workflow.impl.WorkFlowManagerImpl;
import org.onap.appc.workflow.objects.WorkflowExistsOutput;
import org.onap.appc.workflow.objects.WorkflowRequest;
import org.onap.ccsdk.sli.adaptors.aai.AAIService;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({FrameworkUtil.class})
public class RequestValidatorImplTest implements LocalRequestHanlderTestHelper {
    private Configuration mockConfig = ConfigurationFactory.getConfiguration();
    private RequestValidatorImpl impl;
    private final BundleContext bundleContext = Mockito.mock(BundleContext.class);
    private final Bundle bundleService = Mockito.mock(Bundle.class);
    private final ServiceReference sref = Mockito.mock(ServiceReference.class);
    private EELFLogger logger;
    private TransactionRecorder transactionRecorder;
    private LockManager lockManager;
    private LCMStateManager lcmStateManager;


    @Before
    public void setUp() throws Exception {
        impl = PowerMockito.spy(new RequestValidatorImpl());
        Whitebox.setInternalState(impl, "configuration", mockConfig);
        logger = Mockito.spy(EELFManager.getInstance().getLogger(RequestHandlerImpl.class));
        logger.setLevel(Level.TRACE);
        Whitebox.setInternalState(impl, "logger", logger);
        lcmStateManager = new LCMStateManagerImpl();
        impl.setLcmStateManager(lcmStateManager);
        AAIService aaiService = Mockito.mock(AAIService.class);
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
        PowerMockito.mockStatic(FrameworkUtil.class);
        PowerMockito.when(FrameworkUtil.getBundle(AAIService.class)).thenReturn(bundleService);
        PowerMockito.when(bundleService.getBundleContext()).thenReturn(bundleContext);
        PowerMockito.when(bundleContext.getServiceReference(AAIService.class.getName())).thenReturn(sref);
        PowerMockito.when(bundleContext.getService(sref)).thenReturn(aaiService);
        lockManager = mock(LockManager.class);
        impl.setLockManager(lockManager);
    }

    // TODO: remove Ignore when initialize method actually throws APPCException
    @Ignore
    @Test(expected = APPCException.class)
    public void testInitializeWithNullConfigProps() throws Exception {
        Mockito.doReturn(null).when(mockConfig).getProperties();
        impl.initialize();
    }

    // TODO: remove Ignore when initialize method actually throws APPCException
    @Ignore
    @Test(expected = APPCException.class)
    public void testInitializeWithoutEndpointProp() throws Exception {
        Properties mockProp = mock(Properties.class);
        Mockito.doReturn(null).when(mockProp).getProperty(RequestValidatorImpl.SCOPE_OVERLAP_ENDPOINT);
        Mockito.doReturn(mockProp).when(mockConfig).getProperties();
        impl.initialize();
    }

    // TODO: remove Ignore when initialize method actually throws APPCException
    @Ignore
    @Test(expected = APPCException.class)
    public void testInitializeWithMalFormatEndpoint() throws Exception {
        Properties mockProp = mock(Properties.class);
        Mockito.doReturn("a/b/c").when(mockProp).getProperty(RequestValidatorImpl.SCOPE_OVERLAP_ENDPOINT);
        Mockito.doReturn(mockProp).when(mockConfig).getProperties();
        impl.initialize();
    }

    @Test(expected = LCMOperationsDisabledException.class)
    public void testValidateRequestLCMException() throws Exception {
        RuntimeContext runtimeContext = createRequestValidatorInput();
        logger = Mockito.spy(EELFManager.getInstance().getLogger(LCMStateManager.class));
        logger.setLevel(Level.TRACE);
        Whitebox.setInternalState(lcmStateManager, "logger", logger);
        lcmStateManager.disableLCMOperations();
        impl.validateRequest(runtimeContext);
    }


    @Test(expected = RequestValidationException.class)
    public void testValidateRequest() throws Exception {
        RuntimeContext runtimeContext = createRequestValidatorInput();
        logger = Mockito.spy(EELFManager.getInstance().getLogger(LCMStateManager.class));
        logger.setLevel(Level.TRACE);
        Whitebox.setInternalState(lcmStateManager, "logger", logger);
        lcmStateManager.enableLCMOperations();
        transactionRecorder = Mockito.mock(TransactionRecorder.class);
        Mockito.when(transactionRecorder.isTransactionDuplicate(anyObject())).thenReturn(false);
        TransactionRecord transactionRecord = new TransactionRecord();
        transactionRecord.setMode(Mode.EXCLUSIVE);
        runtimeContext.setTransactionRecord(transactionRecord);
        impl.setTransactionRecorder(transactionRecorder);
        WorkflowExistsOutput workflowExistsOutput = new WorkflowExistsOutput(true, true);
        WorkFlowManager workflowManager = Mockito.mock(WorkFlowManagerImpl.class);
        Mockito.when(workflowManager.workflowExists(Mockito.any(WorkflowRequest.class))).thenReturn(workflowExistsOutput);
        impl.setWorkflowManager(workflowManager);
        ResponseContext responseContext = runtimeContext.getResponseContext();
        CommonHeader commonHeader = returnResponseContextCommonHeader(responseContext);
        RestClientInvoker client = mock(RestClientInvoker.class);
        HttpResponse httpResponse = new BasicHttpResponse(new ProtocolVersion("HTTP",1,0), 200, "ACCEPTED");
        httpResponse.setEntity(getHttpEntity());
        Mockito.when(client.doPost(Mockito.anyString(), Mockito.anyString())).thenReturn(httpResponse);
        impl.setClient(client);
        RequestValidationPolicy requestValidationPolicy = Mockito.mock(RequestValidationPolicy.class);
        RuleExecutor ruleExecutor = Mockito.mock(RuleExecutor.class);
        RuleResult ruleResult = RuleResult.REJECT;
        Mockito.when(requestValidationPolicy.getInProgressRuleExecutor()).thenReturn(ruleExecutor);
        Mockito.when(ruleExecutor.executeRule(Mockito.anyString(), Mockito.anyListOf(VNFOperation.class))).thenReturn(ruleResult);
        impl.setRequestValidationPolicy(requestValidationPolicy);
        impl.validateRequest(runtimeContext);
    }

    @Test
    public void testInitializeMalformed() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("appc.LCM.scopeOverlap.endpoint", "/not/a/URL");
        mockConfig.setProperties(properties);
        Whitebox.setInternalState(impl, "configuration", mockConfig);
        impl.initialize();
        Mockito.verify(logger).error(Mockito.anyString(), Mockito.any(MalformedURLException.class));
    }

    @Test
    public void testInitialize() throws Exception {
        Configuration mockConfigForInitTest = ConfigurationFactory.getConfiguration(new Properties());
        Whitebox.setInternalState(impl, "configuration", mockConfigForInitTest);
        impl.initialize();
        Mockito.verify(logger).error("End point is not defined for scope over lap service in appc.properties.");
    }

    @Test(expected = RequestValidationException.class)
    public void testValidateRequestExclusiveRequestCount() throws Exception {
        RuntimeContext runtimeContext = createRequestValidatorInput();
        lcmStateManager.enableLCMOperations();
        transactionRecorder = Mockito.mock(TransactionRecorder.class);
        Mockito.when(transactionRecorder.isTransactionDuplicate(anyObject())).thenReturn(false);
        List<TransactionRecord> transactionRecordList = new ArrayList<TransactionRecord>(1);
        TransactionRecord inProgressTransaction = new TransactionRecord();
        inProgressTransaction.setMode(Mode.EXCLUSIVE);
        transactionRecordList.add(inProgressTransaction);
        Mockito.when(transactionRecorder.getInProgressRequests(Mockito.any(TransactionRecord.class),Mockito.any(int.class)))
            .thenReturn(transactionRecordList);
        runtimeContext.setTransactionRecord(inProgressTransaction);
        impl.setTransactionRecorder(transactionRecorder);
        WorkflowExistsOutput workflowExistsOutput = new WorkflowExistsOutput(true, true);
        WorkFlowManager workflowManager = Mockito.mock(WorkFlowManagerImpl.class);
        Mockito.when(workflowManager.workflowExists(Mockito.any(WorkflowRequest.class)))
            .thenReturn(workflowExistsOutput);
        impl.setWorkflowManager(workflowManager);
        ResponseContext responseContext = runtimeContext.getResponseContext();
        CommonHeader commonHeader = returnResponseContextCommonHeader(responseContext);
        impl.validateRequest(runtimeContext);
    }

    @Test(expected = LockException.class)
    public void testValidateLockException() throws Exception {
        RuntimeContext runtimeContext = createRequestValidatorInput();
        lcmStateManager.enableLCMOperations();
        Mockito.when(lockManager.getLockOwner(Mockito.anyString())).thenReturn("TEST_LOCK_OWNER");
        transactionRecorder = Mockito.mock(TransactionRecorder.class);
        Mockito.when(transactionRecorder.isTransactionDuplicate(anyObject())).thenReturn(false);
        impl.setTransactionRecorder(transactionRecorder);
        TransactionRecord inProgressTransaction = new TransactionRecord();
        runtimeContext.setTransactionRecord(inProgressTransaction);
        impl.validateRequest(runtimeContext);
    }

    @Test
    public void testValidateRequestInProgressTransactionLoop() throws Exception {
        RuntimeContext runtimeContext = createRequestValidatorInput();
        lcmStateManager.enableLCMOperations();
        transactionRecorder = Mockito.mock(TransactionRecorder.class);
        Mockito.when(transactionRecorder.isTransactionDuplicate(anyObject())).thenReturn(false);
        List<TransactionRecord> transactionRecordList = new ArrayList<TransactionRecord>(1);
        TransactionRecord inProgressTransaction = new TransactionRecord();
        inProgressTransaction.setMode(Mode.NORMAL);
        inProgressTransaction.setOperation(VNFOperation.ActionStatus);
        transactionRecordList.add(inProgressTransaction);
        runtimeContext.setTransactionRecord(inProgressTransaction);
        Mockito.when(transactionRecorder.getInProgressRequests(Mockito.any(TransactionRecord.class),Mockito.any(int.class)))
            .thenReturn(transactionRecordList);
        impl.setTransactionRecorder(transactionRecorder);
        WorkflowExistsOutput workflowExistsOutput = new WorkflowExistsOutput(true, true);
        WorkFlowManager workflowManager = Mockito.mock(WorkFlowManagerImpl.class);
        Mockito.when(workflowManager.workflowExists(Mockito.any(WorkflowRequest.class)))
            .thenReturn(workflowExistsOutput);
        impl.setWorkflowManager(workflowManager);
        ResponseContext responseContext = runtimeContext.getResponseContext();
        CommonHeader commonHeader = returnResponseContextCommonHeader(responseContext);
        RestClientInvoker client = mock(RestClientInvoker.class);
        HttpResponse httpResponse = new BasicHttpResponse(new ProtocolVersion("HTTP",1,0), 200, "ACCEPTED");
        httpResponse.setEntity(getHttpEntity());
        Mockito.when(client.doPost(Mockito.anyString(), Mockito.anyString())).thenReturn(httpResponse);
        impl.setClient(client);
        RequestValidationPolicy requestValidationPolicy = Mockito.mock(RequestValidationPolicy.class);
        RuleExecutor ruleExecutor = Mockito.mock(RuleExecutor.class);
        RuleResult ruleResult = RuleResult.ACCEPT;
        Mockito.when(requestValidationPolicy.getInProgressRuleExecutor()).thenReturn(ruleExecutor);
        Mockito.when(ruleExecutor.executeRule(Mockito.anyString(), Mockito.anyListOf(VNFOperation.class)))
            .thenReturn(ruleResult);
        impl.setRequestValidationPolicy(requestValidationPolicy);
        RequestContext requestContext = new RequestContext();
        ActionIdentifiers actionIdentifiers = new ActionIdentifiers();
        actionIdentifiers.setServiceInstanceId("TEST_SERVICE_INSTANCE_ID");
        actionIdentifiers.setVfModuleId("TEST_VNF_MODULE_ID");
        actionIdentifiers.setVnfcName("TEST_VNFC_NAME");
        actionIdentifiers.setVnfId("TEST_VNF_ID");
        actionIdentifiers.setvServerId("TEST_SERVER_ID");
        requestContext.setActionIdentifiers(actionIdentifiers);
        impl.validateRequest(runtimeContext);
        Mockito.verify(logger).debug("Policy validation result ACCEPT");
    }

    @Test(expected = WorkflowNotFoundException.class)
    public void testValidateRequestWorkflowNotFoundException() throws Exception {
        RuntimeContext runtimeContext = createRequestValidatorInput();
        lcmStateManager.enableLCMOperations();
        transactionRecorder = Mockito.mock(TransactionRecorder.class);
        Mockito.when(transactionRecorder.isTransactionDuplicate(anyObject())).thenReturn(false);
        TransactionRecord transactionRecord = new TransactionRecord();
        transactionRecord.setMode(Mode.EXCLUSIVE);
        runtimeContext.setTransactionRecord(transactionRecord);
        impl.setTransactionRecorder(transactionRecorder);
        WorkflowExistsOutput workflowExistsOutput = Mockito.spy(new WorkflowExistsOutput(true, true));
        WorkFlowManager workflowManager = Mockito.mock(WorkFlowManagerImpl.class);
        Mockito.when(workflowManager.workflowExists(Mockito.any(WorkflowRequest.class)))
            .thenReturn(workflowExistsOutput);
        Mockito.when(workflowExistsOutput.isMappingExist()).thenReturn(false);
        impl.setWorkflowManager(workflowManager);
        ResponseContext responseContext = runtimeContext.getResponseContext();
        CommonHeader commonHeader = returnResponseContextCommonHeader(responseContext);
        RestClientInvoker client = mock(RestClientInvoker.class);
        HttpResponse httpResponse = new BasicHttpResponse(new ProtocolVersion("HTTP",1,0), 200, "ACCEPTED");;
        httpResponse.setEntity(getHttpEntity());
        Mockito.when(client.doPost(Mockito.anyString(), Mockito.anyString())).thenReturn(httpResponse);
        impl.setClient(client);
        RequestValidationPolicy requestValidationPolicy = Mockito.mock(RequestValidationPolicy.class);
        RuleExecutor ruleExecutor = Mockito.mock(RuleExecutor.class);
        RuleResult ruleResult = RuleResult.REJECT;
        Mockito.when(requestValidationPolicy.getInProgressRuleExecutor()).thenReturn(ruleExecutor);
        Mockito.when(ruleExecutor.executeRule(Mockito.anyString(), Mockito.anyListOf(VNFOperation.class)))
            .thenReturn(ruleResult);
        impl.setRequestValidationPolicy(requestValidationPolicy);
        impl.validateRequest(runtimeContext);
    }

    @Test(expected = DGWorkflowNotFoundException.class)
    public void testValidateRequestDGWorkflowNotFoundException() throws Exception {
        RuntimeContext runtimeContext = createRequestValidatorInput();
        lcmStateManager.enableLCMOperations();
        transactionRecorder = Mockito.mock(TransactionRecorder.class);
        Mockito.when(transactionRecorder.isTransactionDuplicate(anyObject())).thenReturn(false);
        TransactionRecord transactionRecord = new TransactionRecord();
        transactionRecord.setMode(Mode.EXCLUSIVE);
        runtimeContext.setTransactionRecord(transactionRecord);
        impl.setTransactionRecorder(transactionRecorder);
        WorkflowExistsOutput workflowExistsOutput = Mockito.spy(new WorkflowExistsOutput(true, true));
        WorkFlowManager workflowManager = Mockito.mock(WorkFlowManagerImpl.class);
        Mockito.when(workflowManager.workflowExists(Mockito.any(WorkflowRequest.class)))
            .thenReturn(workflowExistsOutput);
        Mockito.when(workflowExistsOutput.isDgExist()).thenReturn(false);
        impl.setWorkflowManager(workflowManager);
        ResponseContext responseContext = runtimeContext.getResponseContext();
        CommonHeader commonHeader = returnResponseContextCommonHeader(responseContext);
        RestClientInvoker client = mock(RestClientInvoker.class);
        HttpResponse httpResponse = new BasicHttpResponse(new ProtocolVersion("HTTP",1,0), 200, "ACCEPTED");
        httpResponse.setEntity(getHttpEntity());
        Mockito.when(client.doPost(Mockito.anyString(), Mockito.anyString())).thenReturn(httpResponse);
        impl.setClient(client);
        RequestValidationPolicy requestValidationPolicy = Mockito.mock(RequestValidationPolicy.class);
        RuleExecutor ruleExecutor = Mockito.mock(RuleExecutor.class);
        RuleResult ruleResult = RuleResult.REJECT;
        Mockito.when(requestValidationPolicy.getInProgressRuleExecutor()).thenReturn(ruleExecutor);
        Mockito.when(ruleExecutor.executeRule(Mockito.anyString(), Mockito.anyListOf(VNFOperation.class)))
            .thenReturn(ruleResult);
        impl.setRequestValidationPolicy(requestValidationPolicy);
        impl.validateRequest(runtimeContext);
    }

    @Test
    public void testLogInProgressTransactions() {
        ArrayList<TransactionRecord> trArray = new ArrayList();
        TransactionRecord tr = new TransactionRecord();
        tr.setRequestState(RequestStatus.ACCEPTED);
        tr.setStartTime(Instant.now().minus(48, ChronoUnit.HOURS));
        tr.setTargetId("Vnf001");
        trArray.add(tr);
        String loggedMessage = impl.logInProgressTransactions(trArray, 1, 1);
        String partMessage = "In Progress transaction for Target ID - Vnf001 in state ACCEPTED";
        assertTrue(StringUtils.contains(loggedMessage, partMessage));
    }


/*        logger.info("In Progress transaction for Target ID - "+ tr.getTargetId()
        + " in state " + tr.getRequestState()
        + " with Start time " + tr.getStartTime().toString()
        + " for more than configurable time period " + transactionWindowInterval
        + " hours [transaction details - Request ID - " + tr.getTransactionId()
        + ", Service Instance Id -" + tr.getServiceInstanceId()
        + ", Vserver_id - " + tr.getVserverId()
        + ", VNFC_name - "+ tr.getVnfcName()
        + ", VF module Id - " + tr.getVfModuleId()
        + " Start time " + tr.getStartTime().toString()
        + "]" );*/


    private RuntimeContext createRequestValidatorInput() {
        return createRequestHandlerRuntimeContext("VSCP", "{\"request-id\":\"request-id\"}");
    }

    private CommonHeader returnResponseContextCommonHeader(ResponseContext responseContext) {
        CommonHeader commonHeader = responseContext.getCommonHeader();
        Flags flags = new Flags();
        flags.setForce(false);
        commonHeader.setFlags(flags);
        commonHeader.setRequestId("TEST_REQUEST_ID");
        return commonHeader;
    }

    private BasicHttpEntity getHttpEntity() {
        BasicHttpEntity httpEntity = new BasicHttpEntity();
        InputStream inputStream = new ByteArrayInputStream(
                "{\"output\": {\"status\": {\"message\": \"test_messge\",\"code\": \"400\",\"status\":\"test_status\"},\"response-info\": { \"block\": \"true\"}}}".getBytes());
        httpEntity.setContent(inputStream);
        return httpEntity;
    }
}
