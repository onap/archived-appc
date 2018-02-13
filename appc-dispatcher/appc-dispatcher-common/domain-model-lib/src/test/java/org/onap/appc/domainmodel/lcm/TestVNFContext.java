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

public class TestVNFContext {

    private VNFContext vNFContext;

    @Before
    public void setUp() {
        vNFContext = new VNFContext();
    }

    @Test
    public void testGetId() {
        vNFContext.setId("1234AB56");
        Assert.assertNotNull(vNFContext.getId());
        Assert.assertEquals(vNFContext.getId(), "1234AB56");
    }

    @Test
    public void testGetType() {
        vNFContext.setType("abc");
        Assert.assertNotNull(vNFContext.getType());
        Assert.assertEquals(vNFContext.getType(), "abc");
    }

    @Test
    public void testGetVersion() {
        vNFContext.setVersion("2.0");
        Assert.assertNotNull(vNFContext.getVersion());
        Assert.assertEquals(vNFContext.getVersion(), "2.0");
    }

    @Test
    public void testGetStatus() {
        vNFContext.setStatus("200");
        Assert.assertNotNull(vNFContext.getStatus());
        Assert.assertEquals(vNFContext.getStatus(), "200");
    }

    @Test
    public void testToString_ReturnNonEmptyString() {
        assertNotEquals(vNFContext.toString(), "");
        assertNotEquals(vNFContext.toString(), null);
    }

    @Test
    public void testToString_ContainsString() {
        assertTrue(vNFContext.toString().contains("VNFContext{id"));
    }
}
