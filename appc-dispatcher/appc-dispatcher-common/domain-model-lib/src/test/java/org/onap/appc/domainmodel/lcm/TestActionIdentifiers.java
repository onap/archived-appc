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
package org.onap.appc.domainmodel.lcm;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class TestActionIdentifiers {
    private ActionIdentifiers actionIdentifiers;

    @Before
    public void setUp() {
        actionIdentifiers = new ActionIdentifiers();
    }

    @Test
    public void testGetServiceInstanceId() {
        actionIdentifiers.setServiceInstanceId("serviceInstanceId");
        assertNotNull(actionIdentifiers.getServiceInstanceId());
        assertEquals("serviceInstanceId", actionIdentifiers.getServiceInstanceId());
    }

    @Test
    public void testGetVnfId() {
        actionIdentifiers.setVnfId("vnfId");
        assertNotNull(actionIdentifiers.getVnfId());
        assertEquals("vnfId", actionIdentifiers.getVnfId());
    }

    @Test
    public void testGetVnfcName() {
        actionIdentifiers.setVnfcName("vnfcName");
        assertNotNull(actionIdentifiers.getVnfcName());
        assertEquals("vnfcName", actionIdentifiers.getVnfcName());
    }

    @Test
    public void testGetVserverId() {
        actionIdentifiers.setvServerId("vServerId");
        assertNotNull(actionIdentifiers.getVserverId());
        assertEquals("vServerId", actionIdentifiers.getVserverId());
    }

    @Test
    public void testGetVfModuleId() {
        actionIdentifiers.setVfModuleId("vfModuleId");
        assertNotNull(actionIdentifiers.getVfModuleId());
        assertEquals("vfModuleId", actionIdentifiers.getVfModuleId());
    }

    @Test
    public void testToString_ReturnNonEmptyString() {
        assertNotEquals(actionIdentifiers.toString(), "");
        assertNotEquals(actionIdentifiers.toString(), null);
    }

    @Test
    public void testToString_ContainsString() {
        assertTrue(actionIdentifiers.toString().contains("ActionIdentifiers{serviceInstanceId"));
    }

}
