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
import org.onap.appc.domainmodel.lcm.Flags.Mode;

public class TestFlags {
    private Flags flags;

    @Before
    public void setUp() {
        flags = new Flags();
    }

    @Test
    public void testIsForce() {
        flags.setForce(true);
        Assert.assertNotNull(flags.isForce());
        Assert.assertEquals(flags.isForce(), true);
    }

    @Test
    public void testGetTtl() {
        flags.setTtl(1);
        Assert.assertNotNull(flags.getTtl());
        Assert.assertEquals(flags.getTtl(), 1);
    }

    @Test
    public void testGetMode() {
        flags.setMode(Mode.EXCLUSIVE);
        Assert.assertNotNull(flags.getMode());
        Assert.assertEquals(flags.getMode(),Mode.EXCLUSIVE);
    }

    @Test
    public void testGetMode_ValidEnumConstant() {
        flags.setMode("EXCLUSIVE");
        Assert.assertNotNull(flags.getMode());
        Assert.assertEquals(flags.getMode(),Mode.EXCLUSIVE);
    }

    @Test(expected=java.lang.IllegalArgumentException.class)
    public void testGetMode_InvalidEnumConstant() {
        flags.setMode("EXCLUSIVEEEE");
        Assert.assertNotEquals(flags.getMode(),Mode.EXCLUSIVE);
    }

    @Test
    public void testToString_ReturnNonEmptyString() {
        assertNotEquals(flags.toString(), "");
        assertNotEquals(flags.toString(), null);
    }

    @Test
    public void testToString_ContainsString() {
        assertTrue(flags.toString().contains("Flags{force"));
    }
}
