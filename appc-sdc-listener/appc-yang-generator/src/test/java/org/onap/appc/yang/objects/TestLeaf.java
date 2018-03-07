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
package org.onap.appc.yang.objects;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestLeaf {
    private Leaf leaf;

    @Before
    public void setUp() {
        leaf=new Leaf();
    }

    @Test
    public void testGetName() {
        leaf.setName("ABC");
        Assert.assertNotNull(leaf.getName());
        Assert.assertEquals(leaf.getName(), "ABC");
    }

    @Test
    public void testGetType() {
        leaf.setType("A");
        Assert.assertNotNull(leaf.getType());
        Assert.assertEquals(leaf.getType(), "A");
    }

    @Test
    public void testGetDescription() {
        leaf.setDescription("ABCD");
        Assert.assertNotNull(leaf.getDescription());
        Assert.assertEquals(leaf.getDescription(), "ABCD");
    }

    @Test
    public void testGetMandatory() {
        leaf.setMandatory("X");
        Assert.assertNotNull(leaf.getMandatory());
        Assert.assertEquals(leaf.getMandatory(), "X");
    }

    @Test
    public void testGetDefaultValue() {
        leaf.setDefaultValue("default");
        Assert.assertNotNull(leaf.getDefaultValue());
        Assert.assertEquals(leaf.getDefaultValue(), "default");
    }

    @Test
    public void testToString_ReturnNonEmptyString() {
        assertNotEquals(leaf.toString(), "");
        assertNotEquals(leaf.toString(), null);
    }

    @Test
    public void testToString_ContainsString() {
        assertTrue(leaf.toString().contains("name"));
    }
}
