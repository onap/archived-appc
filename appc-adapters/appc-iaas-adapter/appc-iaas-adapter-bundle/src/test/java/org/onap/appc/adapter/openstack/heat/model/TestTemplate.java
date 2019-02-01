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

public class TestTemplate {
    private Template template;

    @Before
    public void setUp() {
        template = new Template();
    }

    @Test
    public void testGetHeatTemplateVersion() {
        template.setHeatTemplateVersion("1.0");
        assertEquals("1.0", template.getHeatTemplateVersion());
    }

    @Test
    public void testToString_ReturnNonEmptyString() {
        assertNotEquals("",template.toString());
        assertNotEquals(null,template.toString());
    }

    @Test
    public void testResources() {
        Resources_ resources = new Resources_();
        template.setResources(resources);
        assertSame(resources, template.getResources());
    }

}
