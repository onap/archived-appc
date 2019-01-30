/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 IBM Intellectual Property. All rights reserved.
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

import org.junit.Before;
import org.junit.Test;

public class AaiVnfcInfoTest {

    private AaiVnfcInfo aaiVnfcInfo;
    
    @Before
    public void setUp()
    {
        aaiVnfcInfo= new AaiVnfcInfo();
    }
    
    @Test
    public void testVnfcId()
    {
        aaiVnfcInfo.setVnfcId("VnfcId");
        assertEquals("VnfcId", aaiVnfcInfo.getVnfcId());
    }
    
    @Test
    public void testVnfcName()
    {
        aaiVnfcInfo.setVnfcName("VnfcName");
        assertEquals("VnfcName", aaiVnfcInfo.getVnfcName());
    }
    
    @Test
    public void testVnfcFunctionCode()
    {
        aaiVnfcInfo.setVnfcFunctionCode("VnfcFunctionCode");
        assertEquals("VnfcFunctionCode", aaiVnfcInfo.getVnfcFunctionCode());
    }
    
    @Test
    public void testVnfcOamIpAddress()
    {
        aaiVnfcInfo.setVnfcOamIpAddress("VnfcOamIpAddress");
        assertEquals("VnfcOamIpAddress", aaiVnfcInfo.getVnfcOamIpAddress());
    }
}
