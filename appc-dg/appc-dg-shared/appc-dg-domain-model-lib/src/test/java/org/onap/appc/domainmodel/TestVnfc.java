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
package org.onap.appc.domainmodel;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class TestVnfc {
    private Vnfc vnfc;

    @Before
    public void SetUp() {
        vnfc=new Vnfc();
    }

    @Test
    public void testGetVnfcType() {
        vnfc.setVnfcType("1");
        assertNotNull(vnfc.getVnfcType());
        assertEquals(vnfc.getVnfcType(),"1");
    }

    @Test
    public void testGetResilienceType() {
        vnfc.setResilienceType("resilienceType");
        assertNotNull(vnfc.getResilienceType());
        assertEquals(vnfc.getResilienceType(),"resilienceType");
    }

    @Test
    public void testGetVnfcName() {
        vnfc.setVnfcName("vnfcName");
        assertNotNull(vnfc.getVnfcName());
        assertEquals(vnfc.getVnfcName(),"vnfcName");
    }

    @Test
    public void testGetvserverList() {
        List<Vserver> vserverList=new LinkedList<>();
        vnfc.setVserverList(vserverList);
        assertNotNull(vnfc.getVserverList());
        assertEquals(vnfc.getVserverList(),vserverList);
    }

    @Test
    public void testIsMandatory() {
        vnfc.setMandatory(false);
        assertNotNull(vnfc.isMandatory());
        assertEquals(vnfc.isMandatory(),false);
    }
}
