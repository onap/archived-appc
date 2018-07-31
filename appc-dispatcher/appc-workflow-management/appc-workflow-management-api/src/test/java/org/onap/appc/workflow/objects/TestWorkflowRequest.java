/*
* ============LICENSE_START=======================================================
* ONAP : APPC
* ================================================================================
* Copyright 2018 TechMahindra
*=================================================================================
* Modifications Copyright 2018 IBM.
*=================================================================================
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* ============LICENSE_END=========================================================
*/
package org.onap.appc.workflow.objects;

import static org.junit.Assert.*;

import org.junit.Test;
import org.onap.appc.domainmodel.lcm.RequestContext;
import org.onap.appc.domainmodel.lcm.ResponseContext;
import org.onap.appc.domainmodel.lcm.VNFContext;

public class TestWorkflowRequest {
    private WorkflowRequest workflowRequest=new WorkflowRequest(); 
    @Test
    public void testToString_ReturnNonEmptyString() {
        assertNotEquals(workflowRequest.toString(), "");
        assertNotEquals(workflowRequest.toString(), null);
    }

    @Test
    public void testToString_ContainsString() {
        assertTrue(workflowRequest.toString().contains("requestContext"));
    }
    
    @Test
    public void testGetRequestContext() {
        RequestContext requestContext= new RequestContext();
        workflowRequest.setRequestContext(requestContext);
        assertEquals(requestContext, workflowRequest.getRequestContext());
    }
    
    @Test
    public void testGetResponseContext() {
        ResponseContext responseContext= new ResponseContext();
        workflowRequest.setResponseContext(responseContext);
        assertEquals(responseContext, workflowRequest.getResponseContext());
    }
    
    @Test
    public void testGetVnfContext() {
        VNFContext vnfContext= new VNFContext();
        workflowRequest.setVnfContext(vnfContext);
        assertEquals(vnfContext, workflowRequest.getVnfContext());
    }
}