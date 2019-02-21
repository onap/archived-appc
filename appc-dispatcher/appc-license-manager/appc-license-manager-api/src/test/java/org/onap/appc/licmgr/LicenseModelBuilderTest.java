/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright 2019 IBM
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
package org.onap.appc.licmgr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Test;
import org.onap.appc.licmgr.objects.LicenseModelBuilder;

public class LicenseModelBuilderTest {

    LicenseModelBuilder licenseModelBuilder;

    @Before
    public void setUp() {
        licenseModelBuilder = new LicenseModelBuilder();
    }

    @Test
    public void testSetEntitlementPoolUuid() {
        String entitlementPoolUuid = "entitlementPoolUuid";
        LicenseModelBuilder licenseModelBuilderObj = licenseModelBuilder.setEntitlementPoolUuid(entitlementPoolUuid);
        assertSame(licenseModelBuilder, licenseModelBuilderObj);
    }

    @Test
    public void testsetLicenseKeyGroupUuid() {
        String licenseKeyGroupUuid = "licenseKeyGroupUuid";
        LicenseModelBuilder licenseModelBuilderObj = licenseModelBuilder.setLicenseKeyGroupUuid(licenseKeyGroupUuid);
        assertSame(licenseModelBuilder, licenseModelBuilderObj);
    }

    @Test
    public void testIsReady() {
        assertEquals(false, licenseModelBuilder.isReady());
    }

    @Test
    public void testBuild() {
        assertNotNull(licenseModelBuilder.build());
    }

}
