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
import static org.mockito.BDDMockito.given;

import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.appc.licmgr.LicenseDataAccessService;
import org.onap.appc.licmgr.exception.DataAccessException;

@RunWith(MockitoJUnitRunner.class)
public class LicenseManagerImplTest {

    @Mock
    private LicenseDataAccessService licenseDataAccessService;

    @Test
    public void retrieveLicenseModel_shouldThrowException_whenRetrieveLicenseModelDataIsEmpty() {

        // GIVEN
        String vnfType = "dummy1";
        String vnfVersion = "dummy2";
        String expectedMessage = String
            .format("License model not found for vnfType='%s' and vnfVersion='%s'", vnfType, vnfVersion);
        given(licenseDataAccessService.retrieveLicenseModelData(vnfType, vnfVersion)).willReturn(Collections.emptyMap());

        // WHEN THEN
        LicenseManagerImpl licenseManager = new LicenseManagerImpl();
        licenseManager.setDAService(licenseDataAccessService);

        assertThatExceptionOfType(DataAccessException.class)
            .isThrownBy(() -> licenseManager.retrieveLicenseModel(vnfType, vnfVersion))
            .withMessage(expectedMessage);
    }
}