/*-
 * ============LICENSE_START=======================================================
 * APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Amdocs
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
 * ============LICENSE_END=========================================================
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */

package org.openecomp.appc.licmgr;

import java.util.Map;

import org.openecomp.appc.licmgr.LicenseManager;
import org.openecomp.appc.licmgr.exception.DataAccessException;
import org.openecomp.appc.licmgr.objects.LicenseModel;


public class LicenseManagerMock implements LicenseManager {

    LicenseModel licenseModel;
    @Override
    public LicenseModel retrieveLicenseModel(String vnfType, String vnfVersion) throws DataAccessException {
        if (vnfType == "VSCP" && vnfVersion == "123"){
            return licenseModel;
        }
        else {
            return null;
        }
    }

    @Override public Map<String, String> retrieveLicenseModelData(String vnfType, String vnfVersion, String... fields)
                    throws DataAccessException {
        return null;
    }

    @Override public void storeArtifactPayload(Map<String, String> parameters) throws RuntimeException {

    }

    public void storeLicenseModel(LicenseModel licenseModel){
        this.licenseModel = licenseModel;

    }
}
