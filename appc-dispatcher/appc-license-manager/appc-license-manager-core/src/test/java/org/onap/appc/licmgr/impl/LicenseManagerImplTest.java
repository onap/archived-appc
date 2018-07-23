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
import static org.mockito.BDDMockito.given;
import static org.onap.appc.licmgr.Constants.SDC_ARTIFACTS_FIELDS.ARTIFACT_CONTENT;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import org.junit.Before;
import java.util.Map;
import java.util.Scanner;
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
        licenseManager.setDAService(licenseDataAccessService);
    }

    @Test
    public void retrieveLicenseModel_shouldThrowException_whenRetrieveLicenseModelDataIsEmpty() {

        // GIVEN
        String expectedMessage = String
            .format("License model not found for vnfType='%s' and vnfVersion='%s'", VNF_TYPE, VNF_VERSION);
        given(licenseDataAccessService.retrieveLicenseModelData(VNF_TYPE, VNF_VERSION))
            .willReturn(Collections.emptyMap());

        // WHEN THEN
        assertThatExceptionOfType(DataAccessException.class)
            .isThrownBy(() -> licenseManager.retrieveLicenseModel(VNF_TYPE, VNF_VERSION))
            .withMessage(expectedMessage);
    }

    @Test
    public void retrieveLicenseModel_shouldThrowException_whenXmlIsMalformed() {

        // GIVEN
        String malformedXml = "xyz";

        Map<String, String> licenseModelData = new HashMap<>();
        licenseModelData.put(ARTIFACT_CONTENT.name(), malformedXml);
        given(licenseDataAccessService.retrieveLicenseModelData(VNF_TYPE, VNF_VERSION))
            .willReturn(licenseModelData);

        // WHEN THEN
        assertThatExceptionOfType(DataAccessException.class)
            .isThrownBy(() -> licenseManager.retrieveLicenseModel(VNF_TYPE, VNF_VERSION));
    }

    @Test
    public void retrieveLicenseModel_shouldReturnCorrectLicenseModel_whenCorrectXmlExists() {

        // GIVEN
        String expectedEntitlementPool = "default_entitlement_pool_uuid";
        String expectedKeyGroup = "default_lkg_uuid";

        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("test-vf-license-model.xml");
        String correctlyFormedXml = new Scanner(inputStream).useDelimiter("\\A").next();

        Map<String, String> licenseModelData = new HashMap<>();
        licenseModelData.put(ARTIFACT_CONTENT.name(), correctlyFormedXml);
        given(licenseDataAccessService.retrieveLicenseModelData(VNF_TYPE, VNF_VERSION))
            .willReturn(licenseModelData);

        // WHEN
        LicenseModel licenseModel = licenseManager.retrieveLicenseModel(VNF_TYPE, VNF_VERSION);

        // THEN
        assertEquals(expectedEntitlementPool, licenseModel.getEntitlementPoolUuid());
        assertEquals(expectedKeyGroup, licenseModel.getLicenseKeyGroupUuid());
    }
}