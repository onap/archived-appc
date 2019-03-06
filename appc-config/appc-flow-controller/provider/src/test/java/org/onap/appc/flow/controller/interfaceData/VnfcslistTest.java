/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 IBM.
 * =============================================================================
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

package org.onap.appc.flow.controller.interfaceData;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class VnfcslistTest {
    private Vnfcslist vnfcslist;
    
    @Before
    public void setUp()
    {
        vnfcslist = new Vnfcslist();
    }
    
    @Test
    public void testVnfcType()
    {
        vnfcslist.setVnfcType("VnfcType");
        assertEquals("VnfcType", vnfcslist.getVnfcType());
    }
    
    @Test
    public void testVnfcName()
    {
        vnfcslist.setVnfcName("VnfcName");
        assertEquals("VnfcName", vnfcslist.getVnfcName());
    }
    
    @Test
    public void testVnfcFunctionCode()
    {
        vnfcslist.setVnfcFunctionCode("VnfcFunctionCode");
        assertEquals("VnfcFunctionCode", vnfcslist.getVnfcFunctionCode());
    }
}
