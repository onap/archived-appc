/*
* ============LICENSE_START=======================================================
* ONAP : APPC
* ================================================================================
* Copyright 2018 TechMahindra
*=================================================================================
* Modifications Copyright 2019 IBM.
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
package org.onap.appc.adapter.openstack.heat.model;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class TestData {
    private Data data;

    @Before
    public void setUp() {
        data=new Data();
    }

    @Test
    public void testGetStatus() {
        data.setStatus("status");
        assertNotNull(data.getStatus());
        assertEquals(data.getStatus(),"status");
    }

    @Test
    public void testGetName() {
        data.setName("XYZ");
        assertNotNull(data.getName());
        assertEquals(data.getName(),"XYZ");
    }

    @Test
    public void testGetStackUserProjectId() {
        data.setStackUserProjectId("stackUserProjectId");
        assertNotNull(data.getStackUserProjectId());
        assertEquals(data.getStackUserProjectId(),"stackUserProjectId");
    }

    @Test
    public void testGetAction() {
        data.setAction("action");
        assertNotNull(data.getAction());
        assertEquals(data.getAction(),"action");
    }

    @Test
    public void testGetProjectId() {
        data.setProjectId("projectId");
        assertNotNull(data.getProjectId());
        assertEquals(data.getProjectId(),"projectId");
    }

    @Test
    public void testGetId() {
        data.setId("Id");
        assertNotNull(data.getId());
        assertEquals(data.getId(),"Id");
    }

    @Test
    public void testToString_ReturnNonEmptyString() {
        assertNotEquals(data.toString(), "");
        assertNotEquals(data.toString(), null);
    }
    
    @Test
    public void testEnvironment() {
        Environment env = new Environment();
        data.setEnvironment(env);
        assertEquals(env, data.getEnvironment());
    }
    
    @Test
    public void testResources() {
        Resources__ resources = new Resources__();
        data.setResources(resources);
        assertEquals(resources, data.getResources());
    }
    
    @Test
    public void testTemplate() {
        Template template = new Template();
        data.setTemplate(template);
        assertEquals(template, data.getTemplate());
    }

}
