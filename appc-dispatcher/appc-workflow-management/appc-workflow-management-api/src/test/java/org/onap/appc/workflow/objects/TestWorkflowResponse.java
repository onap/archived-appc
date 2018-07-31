/*
* ============LICENSE_START=======================================================
* ONAP : APPC
* ================================================================================
* Copyright 2018 TechMahindra
*=================================================================================
* Modification Copyright 2018 IBM.
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
import org.onap.appc.domainmodel.lcm.ResponseContext;

public class TestWorkflowResponse {
    private WorkflowResponse workflowResponse=new WorkflowResponse(); 
    @Test
    public void testToString_ReturnNonEmptyString() {
        assertNotEquals(workflowResponse.toString(), "");
        assertNotEquals(workflowResponse.toString(), null);
    }

    @Test
    public void testToString_ContainsString() {
        assertTrue(workflowResponse.toString().contains("responseContext"));
    }
    
    @Test
    public void testGetResponseContext() {
        ResponseContext responseContext= new ResponseContext();
        workflowResponse.setResponseContext(responseContext);
        assertEquals(responseContext, workflowResponse.getResponseContext());
    }
}
