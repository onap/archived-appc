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


    @Test
    public void testToStringWithASingleVserver() {
        vnfc.setVnfcName("A");
        vnfc.setVnfcType("B");
        vnfc.setMandatory(false);
        Vserver vserver = new Vserver();
        vserver.setId("V1");
        vserver.setName("V1-Name");
        vserver.setRelatedLink("V1-relatedlink");
        vserver.setTenantId("V1-T1");
        vserver.setUrl("http://v1.net");
        vserver.setVnfc(vnfc);
        vnfc.addVserver(vserver);
        
       
       System.out.println("ok vnfc = " + vnfc.toString());
       
       assertEquals(vnfc.toString(),"Vnfc : vnfcType = B, vnfcName = A, resilienceType = null, mandatory = falseVserver : url = http://v1.net, tenantId = V1-T1, id = V1 ,relatedLink = V1-relatedlink , name = V1-Name, \n");
    }
    
    @Test
    public void testHashCode() {
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
        System.out.println("ok vnfc hashcode = " + vnfc.hashCode());

       assertTrue(vnfc.hashCode() == 81141437);
    }
    
    @Test
    public void testCompareVnfc() {
        Vnfc vnfc1 = new Vnfc();
        vnfc1.setVnfcName("A");
        vnfc1.setVnfcType("B");
        vnfc1.setMandatory(false);
        vnfc1.setResilienceType("RS1");

        vnfc.setVnfcName("A");
        vnfc.setVnfcType("B");
        vnfc.setResilienceType("RS1");

        vnfc.setMandatory(true);
       
       
       assertFalse(vnfc.equals(vnfc1));
    }
    
    @Test
    public void testToStringWithVserverList() {
        vnfc.setVnfcName("A");
        vnfc.setVnfcType("B");
        vnfc.setMandatory(false);
        List<Vserver> vserverList=new LinkedList<>();
        Vserver vserver = new Vserver();
        vserver.setId("V1");
        vserver.setName("V1-Name");
        vserver.setRelatedLink("V1-relatedlink");
        vserver.setTenantId("V1-T1");
        vserver.setUrl("http://v1.net");
        vserver.setVnfc(vnfc);
        vserverList.add(vserver);
        vnfc.setVserverList(vserverList);
        
       
       System.out.println("vnfc = " + vnfc.toString());
       
       assertEquals(vnfc.toString(),"Vnfc : vnfcType = B, vnfcName = A, resilienceType = null, mandatory = falseVserver : url = http://v1.net, tenantId = V1-T1, id = V1 ,relatedLink = V1-relatedlink , name = V1-Name, \n");
    }
    
    @Test
    public void testEquals()
    {
    	Vnfc vnfc1=new Vnfc();
    	vnfc1.setMandatory(true);
    	vnfc1.setResilienceType("RS1");
    	vnfc1.setVnfcName("A");
    	vnfc1.setVnfcType("testType");
    	List<Vserver> vserverList=new LinkedList<>();
        Vserver vserver = new Vserver();
        vserver.setId("V1");
        vserver.setName("V1-Name");
        vserver.setRelatedLink("V1-relatedlink");
        vserver.setTenantId("V1-T1");
        vserver.setUrl("http://v1.net");
        vserver.setVnfc(vnfc);
        vserverList.add(vserver);
        vnfc1.setVserverList(vserverList);
        
        Vnfc vnfc2=new Vnfc();
    	vnfc2.setMandatory(true);
    	vnfc2.setResilienceType("RS1");
    	vnfc2.setVnfcName("A");
    	vnfc2.setVnfcType("testType");
    	vnfc2.setVserverList(vserverList);
    	
    	assertTrue(vnfc1.equals(vnfc2));
    }
}
