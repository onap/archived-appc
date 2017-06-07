/*-
 * ============LICENSE_START=======================================================
 * APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Amdocs
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
 * ============LICENSE_END=========================================================
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */

package org.openecomp.appc.requesthandler;


import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openecomp.appc.domainmodel.lcm.*;
import org.openecomp.appc.executor.UnstableVNFException;
import org.openecomp.appc.lifecyclemanager.LifecycleManager;
import org.openecomp.appc.lifecyclemanager.objects.LifecycleException;
import org.openecomp.appc.lifecyclemanager.objects.NoTransitionDefinedException;
import org.openecomp.appc.requesthandler.exceptions.*;
import org.openecomp.appc.requesthandler.impl.RequestHandlerImpl;
import org.openecomp.appc.requesthandler.impl.RequestValidatorImpl;
import org.openecomp.appc.requesthandler.objects.RequestHandlerInput;
import org.openecomp.appc.transactionrecorder.TransactionRecorder;
import org.openecomp.appc.workflow.WorkFlowManager;
import org.openecomp.appc.workflow.objects.WorkflowExistsOutput;
import org.openecomp.appc.workflow.objects.WorkflowRequest;
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

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;


@RunWith(PowerMockRunner.class)
@PrepareForTest( {WorkingStateManager.class,FrameworkUtil.class, TransactionRecorder.class, RequestHandlerImpl.class,RequestValidatorImpl.class, TransactionRecorder.class})
public class TestRequestValidator {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(TestRequestHandler.class);

    private static final String TTL_FLAG= "TTL";

    private RequestValidatorImpl requestValidator;

    AAIService aaiAdapter ;
    LifecycleManager lifecyclemanager;
    WorkFlowManager workflowManager;
    WorkingStateManager workingStateManager ;
    LCMStateManager lcmStateManager;
//    AppcDAOImpl dao ;

    private final BundleContext bundleContext= Mockito.mock(BundleContext.class);
    private final Bundle bundleService=Mockito.mock(Bundle.class);
    private final ServiceReference sref=Mockito.mock(ServiceReference.class);



    @Before
    public void init() throws Exception {

//        dao = Mockito.mock(AppcDAOImpl.class);
//        PowerMockito.whenNew(AppcDAOImpl.class).withNoArguments().thenReturn(dao);
//        Mockito.doNothing().when(dao).storeTransactionRecord((TransactionRecord)anyObject());
        //   PowerMockito.when(dao.queryWorkflow(anyString(),anyString())).thenReturn(true);

        // ***
        AAIService aaiService = Mockito.mock(AAIService.class);;
        PowerMockito.mockStatic(FrameworkUtil.class);
        PowerMockito.when(FrameworkUtil.getBundle(AAIService.class)).thenReturn(bundleService);
        PowerMockito.when(bundleService.getBundleContext()).thenReturn(bundleContext);
        PowerMockito.when(bundleContext.getServiceReference(AAIService.class.getName())).thenReturn(sref);
        PowerMockito.when(bundleContext.<AAIService>getService(sref)).thenReturn(aaiService);
        PowerMockito.when(aaiService.query(anyString(),anyBoolean(),anyString(),anyString(),anyString(),anyString(),(SvcLogicContext)anyObject())).thenAnswer(new Answer<SvcLogicResource.QueryStatus>() {
            @Override
            public SvcLogicResource.QueryStatus answer(InvocationOnMock invocation) throws Throwable {
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
            }
        });
        PowerMockito.when(aaiService.update(anyString(),anyString(),(Map)anyObject(),anyString(),(SvcLogicContext)anyObject())).thenReturn(SvcLogicResource.QueryStatus.SUCCESS);
        //PowerMockito.when(requestHandler.getVnfdata(anyString(), anyString(), (SvcLogicContext)anyObject())).thenReturn()
        //  ***


        aaiAdapter = Mockito.mock(AAIService.class);
        lifecyclemanager= Mockito.mock(LifecycleManager.class);
        workflowManager= Mockito.mock(WorkFlowManager.class);
        workingStateManager = Mockito.mock(WorkingStateManager.class);
        lcmStateManager = Mockito.mock(LCMStateManager.class);

        //  transactionRecorder= spy(TransactionRecorder.class);
        requestValidator = new RequestValidatorImpl();
//        requestValidator = Mockito.mock(RequestValidator.class);
        requestValidator.setWorkflowManager(workflowManager);
        requestValidator.setLifecyclemanager(lifecyclemanager);
        requestValidator.setWorkingStateManager(workingStateManager);
        requestValidator.setLcmStateManager(lcmStateManager);

        Mockito.when(lcmStateManager.isLCMOperationEnabled()).thenReturn(true);
       /* Mockito.when(workingStateManager.isVNFStable("1")).thenReturn(true);
        Mockito.when(aaiAdapter.requestGenericVnfData("1")).thenReturn(getGenericVnf("FIREWALL","INSTNATIATED"));*/
        // Mockito.when(workflowManager.workflowExists((WorkflowRequest)anyObject())).thenReturn(true);

        /*PowerMockito.when(getAaiadapter().requestGenericVnfData("39")).thenReturn(getGenericVnf("FIREWALL","INSTANTIATED"));
        Mockito.when(workingStateManager.isVNFStable("39")).thenReturn(true);
        PowerMockito.when(getAaiadapter().requestGenericVnfData("8")).thenThrow(new AAIAdapterException("404"));
        Mockito.when(workingStateManager.isVNFStable("8")).thenReturn(true);
        PowerMockito.when(getAaiadapter().requestGenericVnfData("9")).thenReturn(getGenericVnf("FIREWALL","INSTANTIATED"));
        Mockito.when(workingStateManager.isVNFStable("9")).thenReturn(true);
        PowerMockito.when(getAaiadapter().requestGenericVnfData("10")).thenReturn(getGenericVnf("WrongRouter","INSTANTIATED"));
        Mockito.when(workingStateManager.isVNFStable("10")).thenReturn(true);
        PowerMockito.when(getAaiadapter().requestGenericVnfData("11")).thenReturn(getGenericVnf("FIREWALL","INSTANTIATED"));
        Mockito.when(workingStateManager.isVNFStable("11")).thenReturn(true);
        PowerMockito.when(getAaiadapter().requestGenericVnfData("12")).thenReturn(getGenericVnf("FIREWALL","NOT_INSTANTIATED"));
        Mockito.when(workingStateManager.isVNFStable("12")).thenReturn(true);
        PowerMockito.when(getAaiadapter().requestGenericVnfData("13")).thenReturn(getGenericVnf("FIREWALL","TESTING"));
        Mockito.when(workingStateManager.isVNFStable("13")).thenReturn(true);
        PowerMockito.when(getAaiadapter().requestGenericVnfData("14")).thenReturn(getGenericVnf("FIREWALL","REBUILDING"));
        Mockito.when(workingStateManager.isVNFStable("14")).thenReturn(true);
        PowerMockito.when(getAaiadapter().requestGenericVnfData("26")).thenReturn(getGenericVnf("FIREWALL","NOT_INSTANTIATED"));
        Mockito.when(workingStateManager.isVNFStable("26")).thenReturn(true);
        PowerMockito.when(getAaiadapter().requestGenericVnfData("27")).thenReturn(getGenericVnf("FIREWALL","RESTARTING"));
        Mockito.when(workingStateManager.isVNFStable("27")).thenReturn(true);
        PowerMockito.when(getAaiadapter().requestGenericVnfData("28")).thenThrow(new RuntimeException("AAI Down Excpetion"));
        Mockito.when(workingStateManager.isVNFStable("28")).thenReturn(true);
        PowerMockito.when(getAaiadapter().requestGenericVnfData("35")).thenReturn(getGenericVnf("FIREWALL","INSTANTIATED"));
        Mockito.when(workingStateManager.isVNFStable("35")).thenReturn(true);*/

        /*for(Integer i=130; i<=140 ; i++)
        {
            PowerMockito.when(getAaiadapter().requestGenericVnfData(i.toString())).thenReturn(getGenericVnf("FIREWALL","INSTANTIATED"));
            Mockito.when(workingStateManager.isVNFStable(i.toString())).thenReturn(true);
        }
        PowerMockito.when(getAaiadapter().requestGenericVnfData("39")).thenReturn(getGenericVnf("FIREWALL","INSTANTIATED"));
        Mockito.when(workingStateManager.isVNFStable("39")).thenReturn(true);
        PowerMockito.when(getAaiadapter().requestGenericVnfData("40")).thenReturn(getGenericVnf("FIREWALL","INSTANTIATED"));
        Mockito.when(workingStateManager.isVNFStable("40")).thenReturn(true).thenReturn(false);


        PowerMockito.when(getAaiadapter().requestGenericVnfData("38")).thenReturn(getGenericVnf("FIREWALL","INSTANTIATED"));
        Mockito.when(workingStateManager.isVNFStable("38")).thenReturn(true).thenReturn(false);


        PowerMockito.when(getAaiadapter().requestGenericVnfData("201")).thenReturn(getGenericVnf("FIREWALL","INSTANTIATED")).thenReturn(getGenericVnf("FIREWALL","CONFIGURED"));
        Mockito.when(workingStateManager.isVNFStable("201")).thenReturn(true);
        PowerMockito.when(getAaiadapter().requestGenericVnfData("202")).thenReturn(getGenericVnf("FIREWALL","INSTANTIATED")).thenReturn(getGenericVnf("FIREWALL","ERROR"));
        Mockito.when(workingStateManager.isVNFStable("202")).thenReturn(true).thenReturn(false);

        PowerMockito.when(getAaiadapter().requestGenericVnfData("301")).thenReturn(getGenericVnf("FIREWALL","INSTANTIATED"));
        Mockito.when(workingStateManager.isVNFStable("301")).thenReturn(true).thenReturn(false);

        PowerMockito.when(getAaiadapter().requestGenericVnfData("302")).thenReturn(getGenericVnf("FIREWALL","INSTANTIATED"));
        Mockito.when(workingStateManager.isVNFStable("302")).thenReturn(true).thenReturn(true);

        PowerMockito.when(getAaiadapter().requestGenericVnfData("303")).thenReturn(getGenericVnf("FIREWALL","INSTANTIATED"));
        Mockito.when(workingStateManager.isVNFStable("303")).thenReturn(true).thenReturn(true);

        PowerMockito.when(getAaiadapter().requestGenericVnfData("309")).thenReturn(getGenericVnf("FIREWALL","INSTANTIATED"));
        Mockito.when(workingStateManager.isVNFStable("309")).thenReturn(true).thenReturn(true);

        PowerMockito.when(getAaiadapter().requestGenericVnfData("310")).thenReturn(getGenericVnf("FIREWALL","INSTANTIATED"));
        Mockito.when(workingStateManager.isVNFStable("310")).thenReturn(true).thenReturn(true);*/
    }
    public AAIService getAaiadapter() {
        return this.aaiAdapter;
    }
/*    public GenericVnf getGenericVnf(String vnfType, String operationalState) {
        GenericVnf genericVnf = new GenericVnf();
        genericVnf.setVnfType(vnfType);
        // genericVnf.setOperationalState(operationalState);
        genericVnf.setOrchestrationStatus(operationalState);
        return genericVnf;
    }*/
    private RequestHandlerInput getRequestHandlerInput(String vnfID, VNFOperation action, int ttl, boolean force, String originatorId, String requestId, String subRequestId, Instant timeStamp){
        String API_VERSION= "2.0.0";
        RequestHandlerInput input = new RequestHandlerInput();
        RuntimeContext runtimeContext = createRuntimeContextWithSubObjects();
        RequestContext requestContext = runtimeContext.getRequestContext();
        input.setRequestContext(requestContext);
        requestContext.getActionIdentifiers().setVnfId(vnfID);
        requestContext.setAction(action);
        if (action != null) {
            input.setRpcName(convertActionNameToUrl(action.name()));
        }
        else{
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
    public void testNullVnfID() throws NoTransitionDefinedException, LifecycleException, InvalidInputException, RequestExpiredException, UnstableVNFException, DuplicateRequestException, VNFNotFoundException, WorkflowNotFoundException, DGWorkflowNotFoundException, MissingVNFDataInAAIException, LCMOperationsDisabledException {
        logger.debug("=====================testNullVnfID=============================");
        Mockito.when(workflowManager.workflowExists((WorkflowRequest)anyObject())).thenReturn(new WorkflowExistsOutput(true,true));
        RequestHandlerInput input = this.getRequestHandlerInput(null, VNFOperation.Configure, 30,
                false, UUID.randomUUID().toString(),UUID.randomUUID().toString(),UUID.randomUUID().toString(), Instant.now());
        Exception ex =null;
        RuntimeContext runtimeContext = putInputToRuntimeContext(input);
        try {
            requestValidator.validateRequest(runtimeContext);
        }catch(InvalidInputException e ) {
            ex = e;
        }
//        assertEquals(new InvalidInputException("vnfID or command is null") ,ex);
        assertNotNull(ex);
        logger.debug("=====================testNullVnfID=============================");
    }

    @Test
    public void testPositiveFlowWithConfigure() throws  NoTransitionDefinedException, LifecycleException, InvalidInputException, RequestExpiredException, UnstableVNFException, DuplicateRequestException, VNFNotFoundException, WorkflowNotFoundException,DGWorkflowNotFoundException {
        logger.debug("=====================testPositiveFlowWithConfigure=============================");
        Mockito.when(workflowManager.workflowExists((WorkflowRequest)anyObject())).thenReturn(new WorkflowExistsOutput(true,true));
        Mockito.when(workingStateManager.isVNFStable("1")).thenReturn(true);
        RequestHandlerInput input = this.getRequestHandlerInput("1", VNFOperation.Configure, 30,
                false,UUID.randomUUID().toString(),UUID.randomUUID().toString(),UUID.randomUUID().toString(), Instant.now());
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
    public void testVnfNotFound() throws  NoTransitionDefinedException, LifecycleException, InvalidInputException, RequestExpiredException, UnstableVNFException, DuplicateRequestException, VNFNotFoundException, WorkflowNotFoundException,DGWorkflowNotFoundException {
        logger.debug("=====================testVnfNotFound=============================");
        Mockito.when(workflowManager.workflowExists((WorkflowRequest)anyObject())).thenReturn(new WorkflowExistsOutput(true,true));
        RequestHandlerInput input = this.getRequestHandlerInput("8", VNFOperation.Configure, 30,
                false,UUID.randomUUID().toString(),UUID.randomUUID().toString(),UUID.randomUUID().toString(), Instant.now());
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
    public void testNullCommand() throws NoTransitionDefinedException, LifecycleException, InvalidInputException, RequestExpiredException, UnstableVNFException, DuplicateRequestException, VNFNotFoundException, WorkflowNotFoundException, DGWorkflowNotFoundException, MissingVNFDataInAAIException, LCMOperationsDisabledException {
        logger.debug("=====================testNullCommand=============================");
        Mockito.when(workflowManager.workflowExists((WorkflowRequest)anyObject())).thenReturn(new WorkflowExistsOutput(true,true));
        RequestHandlerInput input = this.getRequestHandlerInput("7", null,30,
                false,UUID.randomUUID().toString(),UUID.randomUUID().toString(),UUID.randomUUID().toString(), Instant.now());
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
    public void testNullVnfIDAndCommand() throws NoTransitionDefinedException, LifecycleException, InvalidInputException, RequestExpiredException, UnstableVNFException, DuplicateRequestException, VNFNotFoundException, WorkflowNotFoundException, DGWorkflowNotFoundException, MissingVNFDataInAAIException, LCMOperationsDisabledException {
        logger.debug("=====================testNullVnfIDAndCommand=============================");
        Mockito.when(workflowManager.workflowExists((WorkflowRequest)anyObject())).thenReturn(new WorkflowExistsOutput(true,true));
        RequestHandlerInput input = this.getRequestHandlerInput(null, null,30,
                false,UUID.randomUUID().toString(),UUID.randomUUID().toString(),UUID.randomUUID().toString(), Instant.now());
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
    public void testWorkflowNotFound() throws  NoTransitionDefinedException, LifecycleException, InvalidInputException, RequestExpiredException, UnstableVNFException, DuplicateRequestException, VNFNotFoundException, WorkflowNotFoundException,DGWorkflowNotFoundException {
        logger.debug("=====================testWorkflowNotFound=============================");
        Mockito.when(workflowManager.workflowExists((WorkflowRequest)anyObject())).thenReturn(new WorkflowExistsOutput(false,false));
        RequestHandlerInput input = this.getRequestHandlerInput("10", VNFOperation.Configure, 30,
                false,UUID.randomUUID().toString(),UUID.randomUUID().toString(),UUID.randomUUID().toString(), Instant.now());
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
    public void testUnstableVnfWithConfigure() throws  LifecycleException, NoTransitionDefinedException, InvalidInputException, RequestExpiredException, UnstableVNFException, DuplicateRequestException, VNFNotFoundException, WorkflowNotFoundException,DGWorkflowNotFoundException {
        logger.debug("=====================testUnstableVnfWithConfigure=============================");
        Mockito.when(workflowManager.workflowExists((WorkflowRequest)anyObject())).thenReturn(new WorkflowExistsOutput(true,true));
        Mockito.when(lifecyclemanager.getNextState(anyString(), anyString(),anyString())).thenThrow( new NoTransitionDefinedException("","",""));

        RequestHandlerInput input = this.getRequestHandlerInput("11", VNFOperation.Configure, 30,
                false,UUID.randomUUID().toString(),UUID.randomUUID().toString(),UUID.randomUUID().toString(), Instant.now());
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
    public void testUnstableVnfWithTest() throws  LifecycleException, NoTransitionDefinedException, InvalidInputException, RequestExpiredException, UnstableVNFException, DuplicateRequestException, VNFNotFoundException, WorkflowNotFoundException,DGWorkflowNotFoundException {
        logger.debug("=====================testUnstableVnfWithTest=============================");
        Mockito.when(workflowManager.workflowExists((WorkflowRequest)anyObject())).thenReturn(new WorkflowExistsOutput(true,true));
        Mockito.when(lifecyclemanager.getNextState(anyString(), anyString(),anyString())).thenThrow( new NoTransitionDefinedException("","",""));
        RequestHandlerInput input = this.getRequestHandlerInput("12", VNFOperation.Test,30,
                false,UUID.randomUUID().toString(),UUID.randomUUID().toString(),UUID.randomUUID().toString(), Instant.now());
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
    public void testUnstableVnfWithStart() throws  LifecycleException, NoTransitionDefinedException, InvalidInputException, RequestExpiredException, UnstableVNFException, DuplicateRequestException, VNFNotFoundException, WorkflowNotFoundException,DGWorkflowNotFoundException {
        logger.debug("=====================testUnstableVnfWithStart=============================");
        Mockito.when(lifecyclemanager.getNextState(anyString(), anyString(),anyString())).thenThrow( new NoTransitionDefinedException("","",""));

        RequestHandlerInput input = this.getRequestHandlerInput("13", VNFOperation.Start,30,
                false,UUID.randomUUID().toString(),UUID.randomUUID().toString(),UUID.randomUUID().toString(), Instant.now());
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
    public void testUnstableVnfWithTerminate() throws  LifecycleException, NoTransitionDefinedException, InvalidInputException, RequestExpiredException, UnstableVNFException, DuplicateRequestException, VNFNotFoundException, WorkflowNotFoundException,DGWorkflowNotFoundException {
        logger.debug("=====================testUnstableVnfWithTerminate=============================");
        Mockito.when(lifecyclemanager.getNextState(anyString(), anyString(),anyString())).thenThrow( new NoTransitionDefinedException("","",""));
        RequestHandlerInput input = this.getRequestHandlerInput("14", VNFOperation.Terminate,30,
                false,UUID.randomUUID().toString(),UUID.randomUUID().toString(),UUID.randomUUID().toString(), Instant.now());
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
    public void testUnstableVnfWithRestart() throws  LifecycleException, NoTransitionDefinedException, InvalidInputException, RequestExpiredException, UnstableVNFException, DuplicateRequestException, VNFNotFoundException, WorkflowNotFoundException,DGWorkflowNotFoundException {
        logger.debug("=====================testUnstableVnfWithRestart=============================");
        Mockito.when(lifecyclemanager.getNextState(anyString(), anyString(),anyString())).thenThrow( new NoTransitionDefinedException("","",""));

        RequestHandlerInput input = this.getRequestHandlerInput("26", VNFOperation.Restart,30,
                false,UUID.randomUUID().toString(),UUID.randomUUID().toString(),UUID.randomUUID().toString(), Instant.now());
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
    public void testUnstableVnfWithRebuild() throws  LifecycleException, NoTransitionDefinedException, InvalidInputException, RequestExpiredException, UnstableVNFException, DuplicateRequestException, VNFNotFoundException, WorkflowNotFoundException,DGWorkflowNotFoundException {
        logger.debug("=====================testUnstableVnfWithRebuild=============================");
        Mockito.when(lifecyclemanager.getNextState(anyString(), anyString(),anyString())).thenThrow( new NoTransitionDefinedException("","",""));

        // Mockito.doReturn(this.getGenericVnf("Firewall", "NOT_INSTANTIATED")).when(getAaiadapter()).requestGenericVnfData("8");
        RequestHandlerInput input = this.getRequestHandlerInput("27", VNFOperation.Rebuild,30,
                false,UUID.randomUUID().toString(),UUID.randomUUID().toString(),UUID.randomUUID().toString(), Instant.now());
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
        //            AAIAdapter aaiAdapter = Mockito.mock(AAIAdapterImpl.class);
        //            RequestHandler requestHandler=RequestHandlerSingleton.getRequestHandler(new WorkFlowManagerImpl(),aaiAdapter,new LifecycleManagerImpl());
        //            RequestHandler requestHandler = new RequestHandlerImpl(new WorkFlowManagerImpl(),aaiAdapter,new LifecycleManagerImpl());
        RequestHandlerInput input = this.getRequestHandlerInput("28", VNFOperation.Configure, 30,
                false,UUID.randomUUID().toString(),UUID.randomUUID().toString(),UUID.randomUUID().toString(), Instant.now());
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
    public void testNegativeFlowWithTimeStamp() throws  NoTransitionDefinedException, LifecycleException, InvalidInputException, RequestExpiredException, UnstableVNFException, DuplicateRequestException, VNFNotFoundException, WorkflowNotFoundException,DGWorkflowNotFoundException {
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
    public void rejectDuplicateRequest() throws NoTransitionDefinedException, LifecycleException, InvalidInputException, RequestExpiredException, UnstableVNFException, DuplicateRequestException, VNFNotFoundException, WorkflowNotFoundException,DGWorkflowNotFoundException {
        String originatorID = UUID.randomUUID().toString();
        String requestID = UUID.randomUUID().toString();
        String subRequestID = UUID.randomUUID().toString();

        Mockito.when(workflowManager.workflowExists((WorkflowRequest)anyObject())).thenReturn(new WorkflowExistsOutput(true,true));
        Mockito.when(workingStateManager.isVNFStable("301")).thenReturn(true);
        Mockito.when(workingStateManager.isVNFStable("309")).thenReturn(true);
        RequestHandlerInput input = this.getRequestHandlerInput("301", VNFOperation.Configure,0,false,originatorID, requestID, subRequestID, Instant.now());

        RequestHandlerInput input1 = this.getRequestHandlerInput("309", VNFOperation.Configure,0,false,originatorID, requestID, subRequestID, Instant.now());
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
    public void testLockOperation() throws RequestExpiredException, DuplicateRequestException, DGWorkflowNotFoundException, VNFNotFoundException, WorkflowNotFoundException, LifecycleException, UnstableVNFException, NoTransitionDefinedException, InvalidInputException, MissingVNFDataInAAIException, LCMOperationsDisabledException {
        Mockito.when(workingStateManager.isVNFStable("no-matter")).thenReturn(true);
        testOperation("no-matter", VNFOperation.Lock);
    }

    @Test
    public void testUnlockOperation() throws RequestExpiredException, DuplicateRequestException, DGWorkflowNotFoundException, VNFNotFoundException, WorkflowNotFoundException, LifecycleException, UnstableVNFException, NoTransitionDefinedException, InvalidInputException, MissingVNFDataInAAIException, LCMOperationsDisabledException {
        Mockito.when(workingStateManager.isVNFStable("no-matter")).thenReturn(true);
        testOperation("no-matter", VNFOperation.Unlock);
    }

    @Test
    public void testCheckLockOperation() throws RequestExpiredException, DuplicateRequestException, DGWorkflowNotFoundException, VNFNotFoundException, WorkflowNotFoundException, LifecycleException, UnstableVNFException, NoTransitionDefinedException, InvalidInputException, MissingVNFDataInAAIException, LCMOperationsDisabledException {
        Mockito.when(workingStateManager.isVNFStable("no-matter")).thenReturn(true);
        testOperation("no-matter", VNFOperation.CheckLock);
    }

    @Test(expected = NoTransitionDefinedException.class)
    public void testLockOperationNegative() throws RequestExpiredException, DuplicateRequestException, DGWorkflowNotFoundException, VNFNotFoundException, WorkflowNotFoundException, LifecycleException, UnstableVNFException, NoTransitionDefinedException, InvalidInputException, MissingVNFDataInAAIException, LCMOperationsDisabledException {
        Mockito.when(lifecyclemanager.getNextState(anyString(), anyString(), eq(VNFOperation.Lock.toString()))).thenThrow(new NoTransitionDefinedException("", "", ""));
        Mockito.when(workingStateManager.isVNFStable("no-matter")).thenReturn(true);
        testOperation("no-matter", VNFOperation.Lock);
    }

    @Test(expected = NoTransitionDefinedException.class)
    public void testUnlockOperationNegative() throws RequestExpiredException, DuplicateRequestException, DGWorkflowNotFoundException, VNFNotFoundException, WorkflowNotFoundException, LifecycleException, UnstableVNFException, NoTransitionDefinedException, InvalidInputException, MissingVNFDataInAAIException, LCMOperationsDisabledException {
        Mockito.when(lifecyclemanager.getNextState(anyString(), anyString(), eq(VNFOperation.Unlock.toString()))).thenThrow(new NoTransitionDefinedException("", "", ""));
        Mockito.when(workingStateManager.isVNFStable("no-matter")).thenReturn(true);
        testOperation("no-matter", VNFOperation.Unlock);
    }

    @Test(expected = NoTransitionDefinedException.class)
    public void testCheckLockOperationNegative() throws RequestExpiredException, DuplicateRequestException, DGWorkflowNotFoundException, VNFNotFoundException, WorkflowNotFoundException, LifecycleException, UnstableVNFException, NoTransitionDefinedException, InvalidInputException, MissingVNFDataInAAIException, LCMOperationsDisabledException {
        Mockito.when(lifecyclemanager.getNextState(anyString(), anyString(), eq(VNFOperation.CheckLock.toString()))).thenThrow(new NoTransitionDefinedException("", "", ""));
        Mockito.when(workingStateManager.isVNFStable("no-matter")).thenReturn(true);
        testOperation("no-matter", VNFOperation.CheckLock);
    }

    @Test(expected = LCMOperationsDisabledException.class)
    public void testLCMOperationsDisabled() throws RequestExpiredException, DuplicateRequestException, DGWorkflowNotFoundException, VNFNotFoundException, WorkflowNotFoundException, LifecycleException, UnstableVNFException, NoTransitionDefinedException, InvalidInputException, MissingVNFDataInAAIException, LCMOperationsDisabledException {
        Mockito.when(lcmStateManager.isLCMOperationEnabled()).thenReturn(false);
        testOperation("no-matter", VNFOperation.Configure);
    }
    private void testOperation(String resource, VNFOperation operation) throws WorkflowNotFoundException, DuplicateRequestException, DGWorkflowNotFoundException, VNFNotFoundException, InvalidInputException, LifecycleException, UnstableVNFException, NoTransitionDefinedException, RequestExpiredException, MissingVNFDataInAAIException, LCMOperationsDisabledException {
        String originatorID = UUID.randomUUID().toString();
        String requestID = UUID.randomUUID().toString();
        String subRequestID = UUID.randomUUID().toString();

        RequestHandlerInput input = this.getRequestHandlerInput(resource, operation, 0, false, originatorID, requestID, subRequestID,  Instant.now());
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
        //runtimeContext.getRequestContext().getActionIdentifiers().setVnfId(input.getRequestContext().getActionIdentifiers().getVnfId());
        return runtimeContext;

        //String vnfID, VNFOperation action, int ttl, boolean force, String originatorId, String requestId, String subRequestId, Date timeStamp
    }
}
