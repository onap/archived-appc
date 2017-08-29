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

package org.openecomp.appc.requesthandler;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openecomp.appc.domainmodel.lcm.ActionIdentifiers;
import org.openecomp.appc.domainmodel.lcm.CommonHeader;
import org.openecomp.appc.domainmodel.lcm.Flags;
import org.openecomp.appc.domainmodel.lcm.RequestContext;
import org.openecomp.appc.domainmodel.lcm.ResponseContext;
import org.openecomp.appc.domainmodel.lcm.RuntimeContext;
import org.openecomp.appc.domainmodel.lcm.Status;
import org.openecomp.appc.domainmodel.lcm.VNFContext;
import org.openecomp.appc.domainmodel.lcm.VNFOperation;
import org.openecomp.appc.lifecyclemanager.LifecycleManager;
import org.openecomp.appc.lifecyclemanager.objects.NoTransitionDefinedException;
import org.openecomp.appc.requesthandler.exceptions.InvalidInputException;
import org.openecomp.appc.requesthandler.exceptions.LCMOperationsDisabledException;
import org.openecomp.appc.requesthandler.impl.RequestHandlerImpl;
import org.openecomp.appc.requesthandler.impl.RequestValidatorImpl;
import org.openecomp.appc.requesthandler.objects.RequestHandlerInput;
import org.openecomp.appc.transactionrecorder.TransactionRecorder;
import org.openecomp.appc.workflow.WorkFlowManager;
import org.openecomp.appc.workflow.objects.WorkflowExistsOutput;
import org.openecomp.appc.workingstatemanager.WorkingStateManager;
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.openecomp.sdnc.sli.SvcLogicResource;
import org.openecomp.sdnc.sli.aai.AAIService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.time.Instant;
import java.util.UUID;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.*;

@SuppressWarnings("unchecked")
@RunWith(PowerMockRunner.class)
@PrepareForTest( {WorkingStateManager.class,FrameworkUtil.class, TransactionRecorder.class, RequestHandlerImpl.class,
        RequestValidatorImpl.class, TransactionRecorder.class})
public class RequestValidatorTest {
    private final EELFLogger logger = EELFManager.getInstance().getLogger(TestRequestHandler.class);

    private RequestValidatorImpl requestValidator;

    private AAIService aaiAdapter ;
    private LifecycleManager lifecyclemanager;
    private WorkFlowManager workflowManager;
    private WorkingStateManager workingStateManager ;
    private LCMStateManager lcmStateManager;

    private final BundleContext bundleContext= Mockito.mock(BundleContext.class);
    private final Bundle bundleService=Mockito.mock(Bundle.class);
    private final ServiceReference sref=Mockito.mock(ServiceReference.class);

    @Before
    public void init() throws Exception {
        // ***
        AAIService aaiService = Mockito.mock(AAIService.class);
        PowerMockito.mockStatic(FrameworkUtil.class);
        PowerMockito.when(FrameworkUtil.getBundle(AAIService.class)).thenReturn(bundleService);
        PowerMockito.when(bundleService.getBundleContext()).thenReturn(bundleContext);
        PowerMockito.when(bundleContext.getServiceReference(AAIService.class.getName())).thenReturn(sref);
        PowerMockito.when(bundleContext.<AAIService>getService(sref)).thenReturn(aaiService);
        PowerMockito.when(aaiService.query(anyString(),anyBoolean(),anyString(),anyString(),anyString(),anyString(),
                anyObject())).thenAnswer(invocation -> {
                    Object[] args = invocation.getArguments();
                    SvcLogicContext ctx =(SvcLogicContext)args[6];
                    String prefix = (String)args[4];
                    String key = (String)args[3];
                    if(key.contains("'28'")){
                        return  SvcLogicResource.QueryStatus.FAILURE ;
                    }else if ( key.contains("'8'")) {
                        return  SvcLogicResource.QueryStatus.NOT_FOUND ;
                    }else {
                        ctx.setAttribute(prefix + ".vnf-type", "FIREWALL");
                        ctx.setAttribute(prefix + ".orchestration-status", "Instantiated");
                    }
                    return  SvcLogicResource.QueryStatus.SUCCESS ;
                });
        PowerMockito.when(aaiService.update(anyString(),anyString(), anyObject(),anyString(), anyObject()))
                .thenReturn(SvcLogicResource.QueryStatus.SUCCESS);
        //  ***

        aaiAdapter = Mockito.mock(AAIService.class);
        lifecyclemanager= Mockito.mock(LifecycleManager.class);
        workflowManager= Mockito.mock(WorkFlowManager.class);
        workingStateManager = Mockito.mock(WorkingStateManager.class);
        lcmStateManager = Mockito.mock(LCMStateManager.class);

        requestValidator = new RequestValidatorImpl();
        requestValidator.setWorkflowManager(workflowManager);
        requestValidator.setLifecyclemanager(lifecyclemanager);
        requestValidator.setWorkingStateManager(workingStateManager);
        requestValidator.setLcmStateManager(lcmStateManager);

        Mockito.when(lcmStateManager.isLCMOperationEnabled()).thenReturn(true);
    }

    public AAIService getAaiadapter() {
        return this.aaiAdapter;
    }

    private RequestHandlerInput getRequestHandlerInput(String vnfID, VNFOperation action, int ttl,
                                                       boolean force, String originatorId, String requestId,
                                                       String subRequestId, Instant timeStamp){
        String API_VERSION= "2.0.0";
        RequestHandlerInput input = new RequestHandlerInput();
        RuntimeContext runtimeContext = createRuntimeContextWithSubObjects();
        RequestContext requestContext = runtimeContext.getRequestContext();
        input.setRequestContext(requestContext);
        requestContext.getActionIdentifiers().setVnfId(vnfID);
        requestContext.setAction(action);
        if (action != null) {
            input.setRpcName(convertActionNameToUrl(action.name()));
        } else{
            input.setRpcName(null);
        }
        requestContext.getCommonHeader().setRequestId(requestId);
        requestContext.getCommonHeader().setSubRequestId(subRequestId);
        requestContext.getCommonHeader().setOriginatorId(originatorId);
        requestContext.getCommonHeader().setFlags(new Flags(null, force, ttl));
        requestContext.getCommonHeader().getTimeStamp();
        requestContext.getCommonHeader().setApiVer(API_VERSION);
        requestContext.getCommonHeader().setTimestamp(timeStamp);
        return input;
    }

    @Test
    public void testNullVnfID() throws Exception {
        logger.debug("=====================testNullVnfID=============================");
        Mockito.when(workflowManager.workflowExists(anyObject()))
                .thenReturn(new WorkflowExistsOutput(true,true));
        RequestHandlerInput input = this.getRequestHandlerInput(null, VNFOperation.Configure, 30,
                false, UUID.randomUUID().toString(),UUID.randomUUID().toString(),UUID.randomUUID().toString(),
                Instant.now());
        Exception ex =null;
        RuntimeContext runtimeContext = putInputToRuntimeContext(input);
        try {
            requestValidator.validateRequest(runtimeContext);
        }catch(InvalidInputException e ) {
            ex = e;
        }
        assertNotNull(ex);
        logger.debug("=====================testNullVnfID=============================");
    }

    @Test
    public void testPositiveFlowWithConfigure() throws Exception {
        logger.debug("=====================testPositiveFlowWithConfigure=============================");
        Mockito.when(workflowManager.workflowExists(anyObject()))
                .thenReturn(new WorkflowExistsOutput(true,true));
        Mockito.when(workingStateManager.isVNFStable("1")).thenReturn(true);
        RequestHandlerInput input = this.getRequestHandlerInput("1", VNFOperation.Configure, 30,
                false,UUID.randomUUID().toString(),UUID.randomUUID().toString(),UUID.randomUUID().toString(),
                Instant.now());
        Exception ex =null;
        RuntimeContext runtimeContext = putInputToRuntimeContext(input);
        try {
            requestValidator.validateRequest(runtimeContext);
        }catch(Exception e ) {
            ex = e;
        }
        assertNull(ex);
        logger.debug("testPositiveFlowWithConfigure");
        logger.debug("=====================testPositiveFlowWithConfigure=============================");
    }

    @Test
    public void testVnfNotFound() throws Exception {
        logger.debug("=====================testVnfNotFound=============================");
        Mockito.when(workflowManager.workflowExists(anyObject()))
                .thenReturn(new WorkflowExistsOutput(true,true));
        RequestHandlerInput input = this.getRequestHandlerInput("8", VNFOperation.Configure, 30,
                false,UUID.randomUUID().toString(),UUID.randomUUID().toString(),UUID.randomUUID().toString(),
                Instant.now());
        Exception ex =null;
        RuntimeContext runtimeContext = putInputToRuntimeContext(input);
        try {
            requestValidator.validateRequest(runtimeContext);
        }catch(Exception e ) {
            ex = e;
        }
        assertNotNull(ex);
        logger.debug("=====================testVnfNotFound=============================");
    }

    @Test
    public void testNullCommand() throws Exception {
        logger.debug("=====================testNullCommand=============================");
        Mockito.when(workflowManager.workflowExists(anyObject()))
                .thenReturn(new WorkflowExistsOutput(true,true));
        RequestHandlerInput input = this.getRequestHandlerInput("7", null,30,
                false,UUID.randomUUID().toString(),UUID.randomUUID().toString(),UUID.randomUUID().toString(),
                Instant.now());
       Exception ex =null;
       RuntimeContext runtimeContext = putInputToRuntimeContext(input);
       try {
           requestValidator.validateRequest(runtimeContext);
       }catch(InvalidInputException e ) {
           ex = e;
       }
       assertNotNull(ex);
        logger.debug("=====================testNullCommand=============================");
    }

    @Test
    public void testNullVnfIDAndCommand() throws Exception {
        logger.debug("=====================testNullVnfIDAndCommand=============================");
        Mockito.when(workflowManager.workflowExists(anyObject()))
                .thenReturn(new WorkflowExistsOutput(true,true));
        RequestHandlerInput input = this.getRequestHandlerInput(null, null,30,
                false,UUID.randomUUID().toString(),UUID.randomUUID().toString(),UUID.randomUUID().toString(),
                Instant.now());
        Exception ex =null;
        RuntimeContext runtimeContext = putInputToRuntimeContext(input);
        try {
            requestValidator.validateRequest(runtimeContext);
        }catch(InvalidInputException e ) {
            ex = e;
        }
        assertNotNull(ex);
        logger.debug("=====================testNullVnfIDAndCommand=============================");
    }

    @Test
    public void testWorkflowNotFound() throws Exception {
        logger.debug("=====================testWorkflowNotFound=============================");
        Mockito.when(workflowManager.workflowExists(anyObject()))
                .thenReturn(new WorkflowExistsOutput(false,false));
        RequestHandlerInput input = this.getRequestHandlerInput("10", VNFOperation.Configure, 30,
                false,UUID.randomUUID().toString(),UUID.randomUUID().toString(),UUID.randomUUID().toString(),
                Instant.now());
        Exception ex =null;
        RuntimeContext runtimeContext = putInputToRuntimeContext(input);
        try {
            requestValidator.validateRequest(runtimeContext);
        }catch(Exception e ) {
            ex = e;
        }
        assertNotNull(ex);
        logger.debug("=====================testWorkflowNotFound=============================");
    }

    @Test
    public void testUnstableVnfWithConfigure() throws Exception {
        logger.debug("=====================testUnstableVnfWithConfigure=============================");
        Mockito.when(workflowManager.workflowExists(anyObject()))
                .thenReturn(new WorkflowExistsOutput(true,true));
        Mockito.when(lifecyclemanager.getNextState(anyString(), anyString(),anyString()))
                .thenThrow( new NoTransitionDefinedException("","",""));

        RequestHandlerInput input = this.getRequestHandlerInput("11", VNFOperation.Configure, 30,
                false,UUID.randomUUID().toString(),UUID.randomUUID().toString(),UUID.randomUUID().toString(),
                Instant.now());
        Exception ex =null;
        RuntimeContext runtimeContext = putInputToRuntimeContext(input);
        try {
            requestValidator.validateRequest(runtimeContext);
        }catch(Exception e ) {
            ex = e;
        }
        assertNotNull(ex);
        logger.debug("=====================testUnstableVnfWithConfigure=============================");
    }

    @Test
    public void testUnstableVnfWithTest() throws Exception {
        logger.debug("=====================testUnstableVnfWithTest=============================");
        Mockito.when(workflowManager.workflowExists(anyObject()))
                .thenReturn(new WorkflowExistsOutput(true,true));
        Mockito.when(lifecyclemanager.getNextState(anyString(), anyString(),anyString()))
                .thenThrow( new NoTransitionDefinedException("","",""));
        RequestHandlerInput input = this.getRequestHandlerInput("12", VNFOperation.Test,30,
                false,UUID.randomUUID().toString(),UUID.randomUUID().toString(),UUID.randomUUID().toString(),
                Instant.now());
        Exception ex =null;
        RuntimeContext runtimeContext = putInputToRuntimeContext(input);
        try {
            requestValidator.validateRequest(runtimeContext);
        }catch(Exception e ) {
            ex = e;
        }
        assertNotNull(ex);
        logger.debug("=====================testUnstableVnfWithTest=============================");
    }

    @Test
    public void testUnstableVnfWithStart() throws Exception {
        logger.debug("=====================testUnstableVnfWithStart=============================");
        Mockito.when(lifecyclemanager.getNextState(anyString(), anyString(),anyString()))
                .thenThrow( new NoTransitionDefinedException("","",""));

        RequestHandlerInput input = this.getRequestHandlerInput("13", VNFOperation.Start,30,
                false,UUID.randomUUID().toString(),UUID.randomUUID().toString(),UUID.randomUUID().toString(),
                Instant.now());
        Exception ex =null;
        RuntimeContext runtimeContext = putInputToRuntimeContext(input);
        try {
            requestValidator.validateRequest(runtimeContext);
        }catch(Exception e ) {
            ex = e;
        }
        assertNotNull(ex);
        logger.debug("=====================testUnstableVnfWithStart=============================");
    }

    @Test
    public void testUnstableVnfWithTerminate() throws Exception {
        logger.debug("=====================testUnstableVnfWithTerminate=============================");
        Mockito.when(lifecyclemanager.getNextState(anyString(), anyString(),anyString()))
                .thenThrow( new NoTransitionDefinedException("","",""));
        RequestHandlerInput input = this.getRequestHandlerInput("14", VNFOperation.Terminate,30,
                false,UUID.randomUUID().toString(),UUID.randomUUID().toString(),UUID.randomUUID().toString(),
                Instant.now());
        Exception ex =null;
        RuntimeContext runtimeContext = putInputToRuntimeContext(input);
        try {
            requestValidator.validateRequest(runtimeContext);
        }catch(Exception e ) {
            ex = e;
        }
        assertNotNull(ex);
        logger.debug("=====================testUnstableVnfWithTerminate=============================");
    }

    @Test
    public void testUnstableVnfWithRestart() throws Exception {
        logger.debug("=====================testUnstableVnfWithRestart=============================");
        Mockito.when(lifecyclemanager.getNextState(anyString(), anyString(),anyString()))
                .thenThrow( new NoTransitionDefinedException("","",""));

        RequestHandlerInput input = this.getRequestHandlerInput("26", VNFOperation.Restart,30,
                false,UUID.randomUUID().toString(),UUID.randomUUID().toString(),UUID.randomUUID().toString(),
                Instant.now());
        Exception ex =null;
        RuntimeContext runtimeContext = putInputToRuntimeContext(input);
        try {
            requestValidator.validateRequest(runtimeContext);
        }catch(Exception e ) {
            ex = e;
        }
        assertNotNull(ex);
        logger.debug("=====================testUnstableVnfWithRestart=============================");
    }

    @Test
    public void testUnstableVnfWithRebuild() throws Exception {
        logger.debug("=====================testUnstableVnfWithRebuild=============================");
        Mockito.when(lifecyclemanager.getNextState(anyString(), anyString(),anyString()))
                .thenThrow( new NoTransitionDefinedException("","",""));

        RequestHandlerInput input = this.getRequestHandlerInput("27", VNFOperation.Rebuild,30,
                false,UUID.randomUUID().toString(),UUID.randomUUID().toString(),UUID.randomUUID().toString(),
                Instant.now());
        Exception ex =null;
        RuntimeContext runtimeContext = putInputToRuntimeContext(input);
        try {
            requestValidator.validateRequest(runtimeContext);
        }catch(Exception e ) {
            ex = e;
        }
        assertNotNull(ex);
        logger.debug("=====================testUnstableVnfWithRebuild=============================");
    }

    @Test
    public void testAAIDown() throws Exception {
        logger.debug("=====================testAAIDown=============================");
        RequestHandlerInput input = this.getRequestHandlerInput("28", VNFOperation.Configure, 30,
                false,UUID.randomUUID().toString(),UUID.randomUUID().toString(),UUID.randomUUID().toString(),
                Instant.now());
        Exception ex =null;
        RuntimeContext runtimeContext = putInputToRuntimeContext(input);
        try {
            requestValidator.validateRequest(runtimeContext);
        }catch(Exception e ) {
            ex = e;
        }
        assertNotNull(ex);
        logger.debug("=====================testAAIDown=============================");
    }

    @Test
    public void testNegativeFlowWithTimeStamp() throws Exception {
        logger.debug("=====================testNegativeFlowWithTimeStamp=============================");
        Instant now =  Instant.now();
        Instant past =  now.minusMillis(1000000);
        RequestHandlerInput input = this.getRequestHandlerInput("35", VNFOperation.Configure, 30,
                false,UUID.randomUUID().toString(),UUID.randomUUID().toString(),UUID.randomUUID().toString(),past);
        Exception ex =null;
        RuntimeContext runtimeContext = putInputToRuntimeContext(input);
       
        try {
            requestValidator.validateRequest(runtimeContext);
        }catch(Exception e ) {
            ex = e;
        }
        assertNotNull(ex);
        logger.debug("testNegativeFlowWithTimeStamp");
        logger.debug("=====================testNegativeFlowWithTimeStamp=============================");
    }

    @Test
    public void rejectDuplicateRequest() throws Exception {
        String originatorID = UUID.randomUUID().toString();
        String requestID = UUID.randomUUID().toString();
        String subRequestID = UUID.randomUUID().toString();

        Mockito.when(workflowManager.workflowExists(anyObject()))
                .thenReturn(new WorkflowExistsOutput(true,true));
        Mockito.when(workingStateManager.isVNFStable("301")).thenReturn(true);
        Mockito.when(workingStateManager.isVNFStable("309")).thenReturn(true);
        RequestHandlerInput input = this.getRequestHandlerInput("301", VNFOperation.Configure,0,false,
                originatorID, requestID, subRequestID, Instant.now());

        RequestHandlerInput input1 = this.getRequestHandlerInput("309", VNFOperation.Configure,0,false,
                originatorID, requestID, subRequestID, Instant.now());
        Exception ex =null;
        RuntimeContext runtimeContext = putInputToRuntimeContext(input);
        RuntimeContext runtimeContext1 = putInputToRuntimeContext(input1);

        try {
            requestValidator.validateRequest(runtimeContext);
        }catch(Exception e ) {
            ex = e;
        }
        assertNull(ex);

        try {
            requestValidator.validateRequest(runtimeContext1);
        }catch(Exception e ) {
            ex = e;
        }
        assertNotNull(ex);
    }

    @Test
    public void testLockOperation() throws Exception {
        Mockito.when(workingStateManager.isVNFStable("no-matter")).thenReturn(true);
        testOperation("no-matter", VNFOperation.Lock);
    }

    @Test
    public void testUnlockOperation() throws Exception {
        Mockito.when(workingStateManager.isVNFStable("no-matter")).thenReturn(true);
        testOperation("no-matter", VNFOperation.Unlock);
    }

    @Test
    public void testCheckLockOperation() throws Exception {
        Mockito.when(workingStateManager.isVNFStable("no-matter")).thenReturn(true);
        testOperation("no-matter", VNFOperation.CheckLock);
    }

    @Test(expected = NoTransitionDefinedException.class)
    public void testLockOperationNegative() throws Exception {
        Mockito.when(lifecyclemanager.getNextState(anyString(), anyString(), eq(VNFOperation.Lock.toString())))
                .thenThrow(new NoTransitionDefinedException("", "", ""));
        Mockito.when(workingStateManager.isVNFStable("no-matter")).thenReturn(true);
        testOperation("no-matter", VNFOperation.Lock);
    }

    @Test(expected = NoTransitionDefinedException.class)
    public void testUnlockOperationNegative() throws Exception {
        Mockito.when(lifecyclemanager.getNextState(anyString(), anyString(), eq(VNFOperation.Unlock.toString())))
                .thenThrow(new NoTransitionDefinedException("", "", ""));
        Mockito.when(workingStateManager.isVNFStable("no-matter")).thenReturn(true);
        testOperation("no-matter", VNFOperation.Unlock);
    }

    @Test(expected = NoTransitionDefinedException.class)
    public void testCheckLockOperationNegative() throws Exception {
        Mockito.when(lifecyclemanager.getNextState(anyString(), anyString(), eq(VNFOperation.CheckLock.toString())))
                .thenThrow(new NoTransitionDefinedException("", "", ""));
        Mockito.when(workingStateManager.isVNFStable("no-matter")).thenReturn(true);
        testOperation("no-matter", VNFOperation.CheckLock);
    }

    @Test(expected = LCMOperationsDisabledException.class)
    public void testLCMOperationsDisabled() throws Exception {
        Mockito.when(lcmStateManager.isLCMOperationEnabled()).thenReturn(false);
        testOperation("no-matter", VNFOperation.Configure);
    }
    private void testOperation(String resource, VNFOperation operation) throws Exception {
        String originatorID = UUID.randomUUID().toString();
        String requestID = UUID.randomUUID().toString();
        String subRequestID = UUID.randomUUID().toString();

        RequestHandlerInput input = this.getRequestHandlerInput(resource, operation, 0, false, originatorID,
                requestID, subRequestID,  Instant.now());
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
        commonHeader.setFlags(new Flags(null, false, 0));
        ActionIdentifiers actionIdentifiers = new ActionIdentifiers();
        requestContext.setActionIdentifiers(actionIdentifiers);
        VNFContext vnfContext = new VNFContext();
        runtimeContext.setVnfContext(vnfContext);
        return runtimeContext;

    }

    private ResponseContext createResponseContextWithSuObjects(){
        ResponseContext responseContext = new ResponseContext();
        CommonHeader commonHeader = new CommonHeader();
        responseContext.setCommonHeader(commonHeader);
        responseContext.setStatus(new Status(0, null));
        commonHeader.setFlags(new Flags(null, false, 0));
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
        return runtimeContext;
    }
}
