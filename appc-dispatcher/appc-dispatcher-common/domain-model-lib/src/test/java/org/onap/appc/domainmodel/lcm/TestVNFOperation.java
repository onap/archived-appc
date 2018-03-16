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

import org.junit.Test;

public class TestVNFOperation {

    private VNFOperation vNFOperation = VNFOperation.ActionStatus;

    @Test
    public void testName() {
        assertEquals("ActionStatus", vNFOperation.name());
    }

    @Test
    public void testEqual() {
        assertTrue(vNFOperation.equals(VNFOperation.ActionStatus));
        assertFalse(vNFOperation.equals(null));
    }

    @Test
    public void testIsBuiltIn() {
        assertEquals(false, VNFOperation.ActionStatus.isBuiltIn());
    }

    @Test
    public void testFindByString() {
        VNFOperation vNFOperation = VNFOperation.findByString("ActionStatus");
        assertEquals(VNFOperation.ActionStatus, vNFOperation);
    }

    @Test
    public void testFindByString_EmptyString() {
        VNFOperation vNFOperation = VNFOperation.findByString("dfgdfd");
        assertEquals(null, vNFOperation);
    }
}
