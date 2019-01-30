/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 IBM.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.aai.data;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class AaiVnfInfoTest {
    
    public AaiVnfInfo aaiVnfInfo;

    @Before
    public void setUp()
    {
        aaiVnfInfo= new AaiVnfInfo();
    }
    
    @Test
    public void testVnfId()
    {
        aaiVnfInfo.setVnfId("vnfId");
        assertEquals("vnfId", aaiVnfInfo.getVnfId());
    }
    
    @Test
    public void testVnfName()
    {
        aaiVnfInfo.setVnfName("VnfName");
        assertEquals("VnfName", aaiVnfInfo.getVnfName());
    }
    
    @Test
    public void testVnfOamIpAddress()
    {
        aaiVnfInfo.setVnfOamIpAddress("VnfOamIpAddress");
        assertEquals("VnfOamIpAddress", aaiVnfInfo.getVnfOamIpAddress());
    }
    
    @Test
    public void testVmInfo()
    {
        List<AaiVmInfo> VmInfoList= new ArrayList<>();
        AaiVmInfo aaiVmInfo = new AaiVmInfo();
        VmInfoList.add(aaiVmInfo);
        aaiVnfInfo.setVmInfo(VmInfoList);
        assertEquals(VmInfoList, aaiVnfInfo.getVmInfo());
    }
}
