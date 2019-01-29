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

public class AaiVmInfoTest {
    private AaiVmInfo aaiVmInfo;
    
    @Before
    public void setUp()
    {
        aaiVmInfo= new AaiVmInfo();
    }
    
    @Test
    public void testVserverId()
    {
        aaiVmInfo.setVserverId("vserverId");
        assertEquals("vserverId", aaiVmInfo.getVserverId());
    }
    
    @Test
    public void testVserverName()
    {
        aaiVmInfo.setVserverName("VserverName");
        assertEquals("VserverName",aaiVmInfo.getVserverName());
    }
    
    @Test
    public void testVnfcInfo()
    {
        List<AaiVnfcInfo> vnfcInfoList = new ArrayList<>();
        AaiVnfcInfo vnfcInfo= new AaiVnfcInfo();
        vnfcInfoList.add(vnfcInfo);
        aaiVmInfo.setVnfcInfo(vnfcInfoList);
        assertEquals(vnfcInfoList,aaiVmInfo.getVnfcInfo());
    }
}
