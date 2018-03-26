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
package org.onap.appc.sdc.artifacts.object;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class VnfcTest {

    private Vnfc vnfc;
    @Before
    public void setUp() {
        vnfc=new Vnfc();
    }
    
    @Test
    public void testGetVnfcType() {
        vnfc.setVnfcType("VnfcType");
        Assert.assertNotNull(vnfc.getVnfcType());
        Assert.assertEquals("VnfcType", vnfc.getVnfcType());
    }
    @Test
    public void testIsmandatory() {
        vnfc.setMandatory(true);
        Assert.assertNotNull(vnfc.isMandatory());
        Assert.assertEquals(true, vnfc.isMandatory());
    }
    @Test
    public void testResilienceType() {
        vnfc.setResilienceType("resilienceType");
        Assert.assertNotNull(vnfc.getResilienceType());
        Assert.assertEquals("resilienceType", vnfc.getResilienceType());
    }
    @Test
    public void testGetParents() {
        List<String> parents=new ArrayList<String>();
        parents.add("parrent1");
        parents.add("parrent2");
        vnfc.setParents(parents);
        Assert.assertNotNull(vnfc.getParents());
        Assert.assertEquals(true, vnfc.getParents().contains("parrent2"));
        Assert.assertFalse(vnfc.getParents().isEmpty());
    }
}
