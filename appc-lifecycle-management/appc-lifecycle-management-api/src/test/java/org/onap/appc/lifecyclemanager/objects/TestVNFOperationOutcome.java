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
package org.onap.appc.lifecyclemanager.objects;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestVNFOperationOutcome {
    private VNFOperationOutcome vnfOperationOutcome=VNFOperationOutcome.EXPIRE;

    @Test
    public void testName() {
        assertEquals("EXPIRE",vnfOperationOutcome.name());
    }

    @Test
    public void testEquals()  {
        assertTrue(vnfOperationOutcome.equals(VNFOperationOutcome.EXPIRE));
        assertFalse(vnfOperationOutcome.equals(null));
    }

    @Test
    public void testToString_ReturnNonEmptyString() {
        assertNotEquals(vnfOperationOutcome.toString(), "");
        assertNotEquals(vnfOperationOutcome.toString(), null);
        
    }

    @Test
    public void testTostring() {
        assertTrue(vnfOperationOutcome.toString().contains(vnfOperationOutcome.name()));
    }

}
