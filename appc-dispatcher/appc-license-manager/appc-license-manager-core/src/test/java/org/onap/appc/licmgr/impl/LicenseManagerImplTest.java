/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 Nokia.
 * ================================================================================
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

package org.onap.appc.licmgr.impl;


import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.BDDMockito.given;
import static org.onap.appc.licmgr.Constants.SDC_ARTIFACTS_FIELDS.ARTIFACT_CONTENT;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.appc.licmgr.LicenseDataAccessService;
import org.onap.appc.licmgr.exception.DataAccessException;
import org.onap.appc.licmgr.objects.LicenseModel;

@RunWith(MockitoJUnitRunner.class)
public class LicenseManagerImplTest {

    private static final String VNF_TYPE = "default_vnf_type";
    private static final String VNF_VERSION = "default_vnf_version";
    private LicenseManagerImpl licenseManager;

    @Mock
    private LicenseDataAccessService licenseDataAccessService;

    @Before
    public void setUp() {
        licenseManager = new LicenseManagerImpl();
    }

    @Test
    public void retrieveLicenseModel_shouldThrowException_whenRetrieveLicenseModelDataIsEmpty() {

        // GIVEN
        String expectedMessage = String
            .format("License model not found for vnfType='%s' and vnfVersion='%s'", VNF_TYPE, VNF_VERSION);
        given(licenseDataAccessService.retrieveLicenseModelData(VNF_TYPE, VNF_VERSION))
            .willReturn(Collections.emptyMap());

        // WHEN THEN
        licenseManager.setDAService(licenseDataAccessService);

        assertThatExceptionOfType(DataAccessException.class)
            .isThrownBy(() -> licenseManager.retrieveLicenseModel(VNF_TYPE, VNF_VERSION))
            .withMessage(expectedMessage);
    }

    @Test
    public void retrieveLicenseModel_shouldReturnLicenseModelWithNullValues_whenXmlIsMalformed() {

        // GIVEN
        String malformedXml = "xyz";

        Map<String, String> licenseModelData = new HashMap<>();
        licenseModelData.put(ARTIFACT_CONTENT.name(), malformedXml);
        given(licenseDataAccessService.retrieveLicenseModelData(VNF_TYPE, VNF_VERSION))
            .willReturn(licenseModelData);

        // WHEN
        licenseManager.setDAService(licenseDataAccessService);
        LicenseModel licenseModel = licenseManager.retrieveLicenseModel(VNF_TYPE, VNF_VERSION);

        // THEN
        assertNull(licenseModel.getEntitlementPoolUuid());
        assertNull(licenseModel.getLicenseKeyGroupUuid());
    }

    @Test
    public void retrieveLicenseModel_shouldReturnCorrectLicenseModel_whenCorrectXmlExists() {

        // GIVEN
        String expectedEntitlementPool = "default_entitlement_pool";
        String expectedKeyGroup = "default_key_group";

        String correctlyFormedXml = String.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<parent>\n"
            + "<entitlement-pool-uuid>%s</entitlement-pool-uuid>\n"
            + "<license-key-group-uuid>%s</license-key-group-uuid>\n"
            + "</parent>", expectedEntitlementPool, expectedKeyGroup);

        Map<String, String> licenseModelData = new HashMap<>();
        licenseModelData.put(ARTIFACT_CONTENT.name(), correctlyFormedXml);
        given(licenseDataAccessService.retrieveLicenseModelData(VNF_TYPE, VNF_VERSION))
            .willReturn(licenseModelData);

        // WHEN
        licenseManager.setDAService(licenseDataAccessService);
        LicenseModel licenseModel = licenseManager.retrieveLicenseModel(VNF_TYPE, VNF_VERSION);

        // THEN
        assertEquals(expectedEntitlementPool, licenseModel.getEntitlementPoolUuid());
        assertEquals(expectedKeyGroup, licenseModel.getLicenseKeyGroupUuid());
    }
}