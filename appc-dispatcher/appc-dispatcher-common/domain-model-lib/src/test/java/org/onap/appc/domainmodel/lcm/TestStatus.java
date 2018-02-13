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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestStatus {
    private Status status;

    @Before
    public void setUp() {
        status = new Status();
    }

    @Test
    public void testGetCode() {
        status.setCode(200);
        Assert.assertNotNull(status.getCode());
        Assert.assertEquals(status.getCode(), 200);
    }

    @Test
    public void testGetMessage() {
        status.setMessage("SUCCESS");
        Assert.assertNotNull(status.getMessage());
        Assert.assertEquals(status.getMessage(), "SUCCESS");
    }

    @Test
    public void testToString_ReturnNonEmptyString() {
        assertNotEquals(status.toString(), "");
        assertNotEquals(status.toString(), null);
    }

    @Test
    public void testToString_ContainsString() {
        assertTrue(status.toString().contains("Status{code"));
    }
}
