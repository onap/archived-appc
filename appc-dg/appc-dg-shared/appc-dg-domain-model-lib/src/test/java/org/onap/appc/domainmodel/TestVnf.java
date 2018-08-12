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
package org.onap.appc.domainmodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class TestVnf {
    private Vnf vnf;

    @Before
    public void SetUp() {
        vnf=new Vnf();
    }

    @Test
    public void testGetVnfId() {
        vnf.setVnfId("Z");
        assertNotNull(vnf.getVnfId());
        assertEquals(vnf.getVnfId(),"Z");
    }

    @Test
    public void testGetvnfType() {
        vnf.setVnfType("A");
        assertNotNull(vnf.getVnfType());
        assertEquals(vnf.getVnfType(),"A");
    }

    @Test
    public void testGetVnfVersion() {
        vnf.setVnfVersion("1.0");
        assertNotNull(vnf.getVnfVersion());
        assertEquals(vnf.getVnfVersion(),"1.0");
    }

    @Test
    public void testList() {
        List<Vserver> vservers = new LinkedList<>();
        vnf.setVservers(vservers);
        assertNotNull(vnf.getVservers());
        assertEquals(vnf.getVservers(),vservers);
        
    }

    @Test
    public void testToString_ReturnNonEmptyString() {
        assertNotEquals(vnf.toString(), "");
        assertNotEquals(vnf.toString(), null);
    }

    @Test
    public void testToString_ContainsString() {
        assertTrue(vnf.toString().contains("vnfId"));
    }

    @Test
    public void testetVnfcs() {
        Vnfc vnfc = new Vnfc();
        vnfc.setVnfcName("A");
        vnfc.setVnfcType("B");
        vnfc.setResilienceType("RS1");
        vnfc.setMandatory(true);
        List<Vserver> vserverList=new LinkedList<>();
        Vserver vserver = new Vserver();
        vserver.setId("V1");
        vserver.setName("V1-Name");
        vserver.setRelatedLink("V1-relatedlink");
        vserver.setTenantId("V1-T1");
        vserver.setUrl("http://v1.net");
        vserver.setVnfc(vnfc);
        vserverList.add(vserver);
        vnfc.addVservers(vserverList);
        vnf.setVservers(vserverList);
        assertTrue(vnf.getVnfcs()!=null);
    }
    
    @Test
    public void testGetSetIdentityUrl()
    {
      Vnf vnf = new Vnf();
      vnf.setIdentityUrl("testIdentityUrl");
      assertEquals("testIdentityUrl", vnf.getIdentityUrl());
    }
}
