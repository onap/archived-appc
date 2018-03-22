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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.workflow.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.onap.appc.domainmodel.lcm.*;
import org.onap.appc.workflow.impl.WorkFlowManagerImpl;
import org.onap.appc.workflow.impl.WorkflowKey;
import org.onap.appc.workflow.impl.WorkflowResolver;
import org.onap.appc.workflow.objects.WorkflowExistsOutput;
import org.onap.appc.workflow.objects.WorkflowRequest;
import org.onap.appc.workflow.objects.WorkflowResponse;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicService;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Date;
import java.util.Properties;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;

@RunWith(PowerMockRunner.class)
@PrepareForTest( {  WorkflowResolver.class,WorkFlowManagerImpl.class} )
public class TestWorkFlowManager {
    public TestWorkFlowManager() {
    }

    @InjectMocks
    public WorkFlowManagerImpl workflowManger;

    WorkflowResolver workflowResolver;
    public SvcLogicService svcLogicService;

    @Before
    public void init(){

        this.workflowResolver= Mockito.mock(WorkflowResolver.class);
        this.svcLogicService=Mockito.mock(SvcLogicService.class);
        workflowManger.setWorkflowResolver(workflowResolver);
        workflowManger.setSvcLogicServiceRef(svcLogicService);

    }
    @Test
    public void testExecuteWorkFlow() throws  SvcLogicException{

        Mockito.when(workflowResolver.resolve(anyString(),anyString(),anyString(),anyString())).thenReturn(getWorkFlowKey());
        Mockito.when(svcLogicService.execute(anyString(),anyString(), anyString(),anyString(),anyObject())).thenReturn(createSvcExexuteSuccessResponse());

        WorkflowRequest workflowRequest =getWorkflowRequest("vSCP",300,new Date(), "2.00" ,"ST_249","O1652", "uid34", VNFOperation.Lock,"mj13","Payload");

        WorkflowResponse response=workflowManger.executeWorkflow(workflowRequest);
        Assert.assertTrue(response.getResponseContext().getStatus().getMessage().equals("success"));


    }

    @Test
    public  void testExecuteWorkFlowFalse() throws SvcLogicException{

        Mockito.when(workflowResolver.resolve(anyString(),anyString(),anyString(),anyString())).thenReturn(getWorkFlowKey());
        Mockito.when(svcLogicService.execute(anyString(),anyString(), anyString(),anyString(),anyObject())).thenReturn(createSvcExexuteFailureResponse());

        WorkflowRequest workflowRequest =getWorkflowRequest("vSCP",300,new Date(), "2.00" ,"ST_249","O1652", "uid34", VNFOperation.Lock,"mj13","Payload");

        WorkflowResponse response=workflowManger.executeWorkflow(workflowRequest);
        Assert.assertTrue(response.getResponseContext().getStatus().getMessage().equals("failure"));

    }

    @Test
    public void testExecuteWorkFlowAPIVersionStartWithOne() throws SvcLogicException{
        Mockito.when(workflowResolver.resolve(anyString(),anyString(),anyString(),anyString())).thenReturn(getWorkFlowKey());
        Mockito.when(svcLogicService.execute(anyString(),anyString(), anyString(),anyString(),anyObject())).thenReturn(createSvcExexuteSuccessResponse());

        WorkflowRequest workflowRequest =getWorkflowRequest("vSCP",300,new Date(), "1.00" ,"ST_249","O1652", "uid34", VNFOperation.Lock,"mj13","Payload");

        WorkflowResponse response=workflowManger.executeWorkflow(workflowRequest);
        Assert.assertTrue(response.getResponseContext().getStatus().getMessage().equals("success"));
    }

    @Test
    public void testWorkFlowExist() throws  SvcLogicException{
        Mockito.when(workflowResolver.resolve(anyString(),anyString(),anyString(),anyString())).thenReturn(getWorkFlowKey());
        Mockito.when(svcLogicService.hasGraph(anyString(),anyString(), anyString(),anyString())).thenReturn(true);

        WorkflowRequest workflowRequest =getWorkflowRequest("vSCP",300,new Date(), "2.00" ,"ST_249","O1652", "uid34", VNFOperation.Lock,"mj13","Payload");

        WorkflowExistsOutput response=workflowManger.workflowExists(workflowRequest);

        Assert.assertTrue(response.isMappingExist());
    }

    @Test
    public void testWorkFlowNotExist() throws  SvcLogicException{
        Mockito.when(workflowResolver.resolve(anyString(),anyString(),anyString(),anyString())).thenReturn(getWorkFlowKey());
        Mockito.when(svcLogicService.hasGraph(anyString(),anyString(), anyString(),anyString())).thenReturn(false);

        WorkflowRequest workflowRequest =getWorkflowRequest("vSCP",300,new Date(), "2.00" ,"ST_249","O1652", "uid34", VNFOperation.Lock,"mj13","Payload");

        WorkflowExistsOutput response=workflowManger.workflowExists(workflowRequest);

        Assert.assertTrue(response.isMappingExist());
    }

    private   WorkflowRequest getWorkflowRequest(String vnfType, int ttl,  Date timeStamp, String apiVersion, String requestId, String originatorID, String subRequestID, VNFOperation action, String vnfId ,String payload){
        WorkflowRequest workflowRequest=new WorkflowRequest();
        RuntimeContext runtimeContext=createRuntimeContext();

        runtimeContext.getRequestContext().getCommonHeader().getFlags().setTtl(ttl);
        runtimeContext.getRequestContext().getCommonHeader().setApiVer(apiVersion);
        runtimeContext.getRequestContext().getCommonHeader().setTimestamp(timeStamp);
        runtimeContext.getRequestContext().getCommonHeader().setRequestId(requestId);
        runtimeContext.getRequestContext().getCommonHeader().setSubRequestId(subRequestID);
        runtimeContext.getRequestContext().getCommonHeader().setOriginatorId(originatorID);
        runtimeContext.getRequestContext().setAction(action);
        runtimeContext.getRequestContext().getActionIdentifiers().setVnfId(vnfId);
        runtimeContext.getRequestContext().setPayload(payload);

        runtimeContext.getVnfContext().setType(vnfType);
        runtimeContext.getVnfContext().setId(vnfId);

        workflowRequest.setRequestContext(runtimeContext.getRequestContext());
        workflowRequest.setResponseContext(runtimeContext.getResponseContext());
        workflowRequest.setVnfContext(runtimeContext.getVnfContext());

        return  workflowRequest;
    }

    private RequestContext creatRequestContext(){
        RequestContext requestContext=new RequestContext();
        CommonHeader commonHeader = new CommonHeader();
        Flags flags = new Flags();
        ActionIdentifiers actionIdentifiers = new ActionIdentifiers();
        commonHeader.setFlags(flags);
        requestContext.setCommonHeader(commonHeader);
        requestContext.setActionIdentifiers(actionIdentifiers);

        return  requestContext;
    }
    private  ResponseContext createResponseContext(){
        ResponseContext responseContext=new ResponseContext();
        CommonHeader commonHeader = new CommonHeader();
        Flags flags = new Flags();
        Status status = new Status();
        responseContext.setCommonHeader(commonHeader);
        responseContext.setStatus(status);
        commonHeader.setFlags(flags);

        return  responseContext;
    }
    private RuntimeContext createRuntimeContext(){
        RuntimeContext runtimeContext=new RuntimeContext();
        RequestContext requestContext=creatRequestContext();
        ResponseContext responseContext=createResponseContext();
        runtimeContext.setRequestContext(requestContext);
        runtimeContext.setResponseContext(responseContext);
        VNFContext vnfContext=new VNFContext();
        runtimeContext.setVnfContext(vnfContext);

        return runtimeContext;
    }

    public WorkflowKey getWorkFlowKey(){
        WorkflowKey workflowKey=new WorkflowKey("APPCDG","2.0.0.0","dgModule");

        return workflowKey;
    }

    private Properties createSvcExexuteSuccessResponse(){
        Properties properties=new Properties();
        properties.setProperty("output.payload","success");
        properties.setProperty("SvcLogic.status","success");
        properties.setProperty("output.status.code","400");
        properties.setProperty("output.status.message","success");

        return properties;
    }

    private Properties createSvcExexuteFailureResponse(){
        Properties properties=new Properties();
        properties.setProperty("output.payload","failure");
        properties.setProperty("SvcLogic.status","failure");
        properties.setProperty("output.status.message","failure");

        return properties;
    }

}
