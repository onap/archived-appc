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
package org.onap.appc.dg.common.impl;

import org.junit.Assert;
import org.junit.Test;
import org.onap.appc.dg.common.impl.Constants.LCMAttributes;
import org.onap.appc.dg.common.impl.Constants.LegacyAttributes;

public class TestConstants {
    private Constants constants=new Constants();
    private LegacyAttributes legacyAttributes=LegacyAttributes.ACTION;
    private LCMAttributes lCMAttributes=LCMAttributes.ACTION;

    @Test
    public void testConstants() {
        Assert.assertNotNull(constants);
        Assert.assertEquals("org.onap.appc.dg.error",constants.DG_ERROR_FIELD_NAME);
    }

    @Test
    public void testLegacyAttributes_Name() {
        Assert.assertEquals("ACTION",legacyAttributes.name());
    }

    @Test
    public void test_getValue_LegacyAttributes() {
        Assert.assertEquals("org.onap.appc.action",legacyAttributes.getValue());
    }

    @Test
    public void testEquals_for_LegacyAttributes() {
        Assert.assertTrue(legacyAttributes.equals(LegacyAttributes.ACTION));
        Assert.assertFalse(legacyAttributes.equals(null));
    }

    @Test
    public void testLCMAttributes_Name() {
        Assert.assertEquals("ACTION",lCMAttributes.name());
    }

    @Test
    public void test_getValue_LCMAttributes() {
        Assert.assertEquals("input.action",lCMAttributes.getValue());
    }

    @Test
    public void testEquals_for_LCMAttributes() {
        Assert.assertTrue(lCMAttributes.equals(LCMAttributes.ACTION));
        Assert.assertFalse(lCMAttributes.equals(null));
    }

}