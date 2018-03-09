/*
* ============LICENSE_START=======================================================
* ONAP : APPC
* ================================================================================
* Copyright 2018 TechMahindra
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestWorkflowExistsOutput {
    private WorkflowExistsOutput workflowExistsOutput;

    private WorkflowExistsOutput workflowExistsOutput1;
    @Before
    public void SetUp() {
        workflowExistsOutput=new WorkflowExistsOutput();
        workflowExistsOutput1=new WorkflowExistsOutput(true,true);
    }

    @Test
    public void testGetWorkflowName() {
        workflowExistsOutput.setWorkflowName("workflowName");
        Assert.assertNotNull(workflowExistsOutput.getWorkflowName());
        Assert.assertEquals(workflowExistsOutput.getWorkflowName(), "workflowName");
    }

    @Test
    public void testGetWorkflowVersion() {
        workflowExistsOutput.setWorkflowVersion("1.0");
        Assert.assertNotNull(workflowExistsOutput.getWorkflowVersion());
        Assert.assertEquals(workflowExistsOutput.getWorkflowVersion(), "1.0");
    }

    @Test
    public void testGetWorkflowModule() {
        workflowExistsOutput.setWorkflowModule("appc");
        Assert.assertNotNull(workflowExistsOutput.getWorkflowModule());
        Assert.assertEquals(workflowExistsOutput.getWorkflowModule(), "appc");
    }

    @Test
    public void testMappingExist() {
        workflowExistsOutput.setMappingExist(true);
        Assert.assertNotNull(workflowExistsOutput.isMappingExist());
        Assert.assertEquals(workflowExistsOutput.isMappingExist(), true);
    }

    @Test
    public void testDgExist() {
        workflowExistsOutput.setDgExist(true);
        Assert.assertNotNull(workflowExistsOutput.isDgExist());
        Assert.assertEquals(workflowExistsOutput.isDgExist(), true);
    }

    @Test
    public void testexists() {
        Assert.assertNotNull(workflowExistsOutput1.exists());
        
    }

    @Test
    public void testToString_ReturnNonEmptyString() {
        assertNotEquals(workflowExistsOutput.toString(), "");
        assertNotEquals(workflowExistsOutput.toString(), null);
    }

    @Test
    public void testToString_ContainsString() {
        assertTrue(workflowExistsOutput.toString().contains("dgExist"));
    }
}
