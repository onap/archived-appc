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

package org.openecomp.appc.licmgr.impl;

import static org.openecomp.appc.licmgr.Constants.ASDC_ARTIFACTS_FIELDS.ARTIFACT_CONTENT;

import java.util.Map;

import org.openecomp.appc.licmgr.LicenseDataAccessService;
import org.openecomp.appc.licmgr.LicenseManager;
import org.openecomp.appc.licmgr.exception.DataAccessException;
import org.openecomp.appc.licmgr.objects.LicenseModel;


@SuppressWarnings("all")
public class LicenseManagerImpl implements LicenseManager {

    private LicenseDataAccessService DAService;

    public void setDAService(LicenseDataAccessService daSrv){
        DAService = daSrv;
    }

    public LicenseManagerImpl() {
    }

    @Override
    public LicenseModel retrieveLicenseModel(String vnfType, String vnfVersion) throws DataAccessException {

        LicenseModel licenseModel;
        try {
            Map<String,String> resultMap = DAService.retrieveLicenseModelData(vnfType, vnfVersion);
            if (resultMap.isEmpty()) {
                throw new DataAccessException(String.format("License model not found for vnfType='%s' and vnfVersion='%s'", vnfType, vnfVersion));
            }
            String licenseModelXML = resultMap.get(ARTIFACT_CONTENT.name());
            licenseModel = convert(licenseModelXML);  // JAXBUtil.<VfLicenseModel>toObject(licenseModelXML, VfLicenseModel.class);
        } catch (DataAccessException le) {
            throw le;
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
        return licenseModel;
    }

    @Override
    public Map<String, String> retrieveLicenseModelData(String vnfType, String vnfVersion, String... fields)  throws DataAccessException {

        Map<String,String> resultMap = DAService.retrieveLicenseModelData(vnfType, vnfVersion, fields);
        return resultMap;
    }

    @Override
    public void storeArtifactPayload(Map<String, String> parameters) throws RuntimeException {

        DAService.storeArtifactPayload(parameters);
    }

    private static LicenseModel convert(String xml) {

        LicenseModel licenseModel = new LicenseModel();

        int posEntitlementStart = xml.indexOf("<entitlement-pool-uuid>");
        int posEntitlementEnd = xml.indexOf("</entitlement-pool-uuid>", posEntitlementStart);
        if (-1 != posEntitlementStart) {
            licenseModel.setEntitlementPoolUuid(xml.substring(posEntitlementStart + "<entitlement-pool-uuid>".length(), posEntitlementEnd));
        }

        int posLicenseStart = xml.indexOf("<license-key-group-uuid>");
        int posLicenseEnd = xml.indexOf("</license-key-group-uuid>", posEntitlementStart);
        if (-1 != posLicenseStart) {
            licenseModel.setLicenseKeyGroupUuid(xml.substring(posLicenseStart + "<license-key-group-uuid>".length(), posLicenseEnd));
        }

        return licenseModel;
    }

}
