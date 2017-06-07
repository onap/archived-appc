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

package org.openecomp.appc.workflow;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openecomp.appc.workflow.impl.WorkFlowManagerImpl;
import org.openecomp.appc.workflow.objects.WorkflowRequest;
import org.openecomp.sdnc.sli.SvcLogicException;
import org.openecomp.sdnc.sli.SvcLogicGraph;
import org.openecomp.sdnc.sli.SvcLogicNode;
import org.openecomp.sdnc.sli.SvcLogicStore;
import org.openecomp.sdnc.sli.provider.SvcLogicActivator;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest( {SvcLogicActivator.class, FrameworkUtil.class, WorkFlowManagerImpl.class} )
public class TestWorkFlowManager {
    public TestWorkFlowManager() {
    }

    private WorkFlowManagerImpl workflowManger ;
    private String command="Configure";
    protected SvcLogicGraph svcLogicGraph=null;


    //
    private final SvcLogicStore svcLogicStore= Mockito.mock(SvcLogicStore.class);
    private final BundleContext bundleContext=Mockito.mock(BundleContext.class);
    private final Bundle bundleSvcLogicService=Mockito.mock(Bundle.class);
    private final ServiceReference serviceReferenceSvcLogicService=Mockito.mock(ServiceReference.class);



    @Before
    public void setupMock() throws Exception {
        /*
        // DAO Mock
        dao = Mockito.mock(AppcDAOImpl.class);
        PowerMockito.whenNew(AppcDAOImpl.class).withNoArguments().thenReturn(dao);

        // SVC Logic Mock
        SvcLogicServiceImpl svcLogicService=new SvcLogicServiceImpl();
        PowerMockito.mockStatic(SvcLogicActivator.class);
        PowerMockito.mockStatic(FrameworkUtil.class);
        PowerMockito.when(SvcLogicActivator.getStore()).thenReturn(svcLogicStore);
        PowerMockito.when(FrameworkUtil.getBundle(SvcLogicService.class)).thenReturn(bundleSvcLogicService);
        PowerMockito.when(bundleSvcLogicService.getBundleContext()).thenReturn(bundleContext);
        PowerMockito.when(bundleContext.getServiceReference(SvcLogicService.NAME)).thenReturn(serviceReferenceSvcLogicService);
        PowerMockito.when(bundleContext.getService(serviceReferenceSvcLogicService)).thenReturn(svcLogicService);

        try {
            PowerMockito.when(svcLogicStore.fetch(anyString(), eq("FIREWALL_Configure"), anyString(), anyString())).thenReturn(createGraph("FIREWALL_Configure"));
            PowerMockito.when(svcLogicStore.fetch(anyString(), eq("FIREWALL_Restart"), anyString(), anyString())).thenReturn(createGraph("FIREWALL_Restart"));
            PowerMockito.when(svcLogicStore.fetch(anyString(), eq("FIREWALL_Test"), anyString(), anyString())).thenReturn(createGraph("FIREWALL_Test"));
            PowerMockito.when(svcLogicStore.fetch(anyString(), eq("FIREWALL_Rebuild"), anyString(), anyString())).thenReturn(createGraph("FIREWALL_Rebuild"));
            PowerMockito.when(svcLogicStore.fetch(anyString(), eq("FIREWALL_Terminate"), anyString(), anyString())).thenReturn(createGraph("FIREWALL_Terminate"));
            PowerMockito.when(svcLogicStore.fetch(anyString(), eq("FIREWALL_Start"), anyString(), anyString())).thenReturn(createGraph("FIREWALL_Start"));
            svcLogicService.registerExecutor("switch", new SwitchNodeExecutor());
            svcLogicService.registerExecutor("execute",new ReturnNodeExecutor());
            svcLogicService.registerExecutor("return",new ReturnNodeExecutor());
        } catch (SvcLogicException e) {
            e.printStackTrace();
        }

        workflowManger = new WorkFlowManagerImpl();

        PowerMockito.when(getDao().retrieveWorkflowDetails("FIREWALL","Configure")).thenReturn(getWorkflow());
        PowerMockito.when(getDao().retrieveWorkflowDetails("FIREWALL","")).thenThrow(new DAOException());
        PowerMockito.when(getDao().retrieveWorkflowDetails("","Configure")).thenThrow(new DAOException());
         */
    }

    @Test
    public void testEmptyVnfTypeFlow(){
        /*
        WorkflowRequest workflowRequest = getWorkflowRequest("","1","1",command);
        setSvcLogicGraph(createGraph(""+"_"+command));
        WorkflowResponse response =workflowManger.executeWorkflow(workflowRequest);
        assertFalse(response.isExecutionSuccess());
         */
    }

    /*
    @Test
    public void testExecuteWorkflow(){
        //PowerMockito.when(getDao().retrieveWorkflowDetails(anyString(),anyString())).thenReturn(getWorkflow());
        WorkflowRequest workflowRequest = getWorkflowRequest("FIREWALL","1","1",command);
        setSvcLogicGraph(createGraph("FIREWALL"+"_"+command));
        WorkflowResponse response =workflowManger.executeWorkflow(workflowRequest);
        assertFalse(response.isExecutionSuccess());
    }

    @Test
    public void testExecuteWorkflowEmptyPayload(){
        //PowerMockito.when(getDao().retrieveWorkflowDetails(anyString(),anyString())).thenReturn(getWorkflow());
        WorkflowRequest workflowRequest = getWorkflowRequest("FIREWALL","1","1",command);
        workflowRequest.setPayload("{payload:\"payload\"}");
        setSvcLogicGraph(createGraph(""+"_"+command));
        WorkflowResponse response =workflowManger.executeWorkflow(workflowRequest);
        assertFalse(response.isExecutionSuccess());
    }

    @Test
    public void testWorkflowExist(){
        //PowerMockito.when(getDao().queryWorkflow(anyString(),anyString())).thenReturn(true);
        WorkflowRequest workflowRequest = getWorkflowRequest("FIREWALL","1","1",command);
        boolean success = workflowManger.workflowExists(workflowRequest);
        assertTrue(success);
    }

    @Test
    public void testWorkflowExistFalse(){
        //PowerMockito.when(getDao().queryWorkflow(anyString(),anyString())).thenReturn(false);
        WorkflowRequest workflowRequest = getWorkflowRequest("FIREWALL","1","1",command);
        setSvcLogicGraph(createGraph(""+"_"+command));
        boolean success = workflowManger.workflowExists(workflowRequest);
        assertFalse(success);
    }


    @Test
    public void testEmptyCommandFlow(){
        WorkflowRequest workflowRequest = getWorkflowRequest("FIREWALL","1","1","");
        WorkflowResponse response =workflowManger.executeWorkflow(workflowRequest);
        assertFalse(response.isExecutionSuccess());
    }
     */


    public void setSvcLogicGraph(SvcLogicGraph svcLogicGraph) {
        this.svcLogicGraph = svcLogicGraph;
    }

    public SvcLogicGraph getSvcLogicGraph() {
        return svcLogicGraph;
    }

    protected SvcLogicGraph createGraph(String rpc) {
        SvcLogicGraph svcLogicGraph = new SvcLogicGraph();
        svcLogicGraph.setModule("APPC");
        svcLogicGraph.setRpc(rpc);
        svcLogicGraph.setMode("sync");
        svcLogicGraph.setVersion("2.0.0");
        SvcLogicNode svcLogicRootNode = new SvcLogicNode(1, "switch", svcLogicGraph);
        SvcLogicNode svcLogicConfigureNode = new SvcLogicNode(2, "return", svcLogicGraph);
        SvcLogicNode svcLogicOtherNode = new SvcLogicNode(3, "return", svcLogicGraph);
        try {
            svcLogicConfigureNode.setAttribute("status", "success");
            svcLogicOtherNode.setAttribute("status", "failure");
            svcLogicRootNode.setAttribute("test", "$org.openecomp.appc.action");
            svcLogicRootNode.addOutcome("Configure", svcLogicConfigureNode);
            svcLogicRootNode.addOutcome("Other", svcLogicOtherNode);
        } catch (SvcLogicException e) {
            e.printStackTrace();
        }
        svcLogicGraph.setRootNode(svcLogicRootNode);
        return svcLogicGraph;
    }
}
