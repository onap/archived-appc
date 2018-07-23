/*
* ============LICENSE_START=======================================================
* ONAP : APPC
* ================================================================================
* Copyright 2018 TechMahindra
* ================================================================================
* Modifications Copyright (C) 2018 Nokia
* ================================================================================
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
package org.onap.appc.licmgr.objects;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestLicenseModel {
    private LicenseModel licenseModel;

    @Before
    public void setUp() {
        licenseModel = new LicenseModel("EntitlementPoolUuid", "LicenseKeyGroupUuid");
    }

    @Test
    public void testGetEntitlementPoolUuid() {
        Assert.assertNotNull(licenseModel.getEntitlementPoolUuid());
        Assert.assertEquals(licenseModel.getEntitlementPoolUuid(), "EntitlementPoolUuid");
    }

    @Test
    public void testGetLicenseKeyGroupUuid() {
        Assert.assertNotNull(licenseModel.getLicenseKeyGroupUuid());
        Assert.assertEquals(licenseModel.getLicenseKeyGroupUuid(), "LicenseKeyGroupUuid");
    }

}
