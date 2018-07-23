/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
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

import static org.onap.appc.licmgr.Constants.SDC_ARTIFACTS_FIELDS.ARTIFACT_CONTENT;

import java.io.IOException;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.onap.appc.licmgr.LicenseDataAccessService;
import org.onap.appc.licmgr.LicenseManager;
import org.onap.appc.licmgr.exception.DataAccessException;
import org.onap.appc.licmgr.impl.xml.XmlInputFactoryWrapper;
import org.onap.appc.licmgr.impl.xml.XmlToLicenseModelConverter;
import org.onap.appc.licmgr.objects.LicenseModel;
import org.onap.appc.licmgr.objects.LicenseModelBuilder;


@SuppressWarnings("all")
public class LicenseManagerImpl implements LicenseManager {

    public static final String ENTITLEMENT_POOL_UUID = "entitlement-pool-uuid";
    public static final String LICENSE_KEY_GROUP_UUID = "license-key-group-uuid";
    private LicenseDataAccessService DAService;

    public LicenseManagerImpl() {

    }

    @Override
    public LicenseModel retrieveLicenseModel(String vnfType, String vnfVersion) throws DataAccessException {

        LicenseModelBuilder builder = new LicenseModelBuilder();
        try {
            Map<String, String> resultMap = DAService.retrieveLicenseModelData(vnfType, vnfVersion);
            if (resultMap.isEmpty()) {
                throw new DataAccessException(
                    String.format("License model not found for vnfType='%s' and vnfVersion='%s'", vnfType, vnfVersion));
            }
            String licenseModelXML = resultMap.get(ARTIFACT_CONTENT.name());
            Converter.convert(licenseModelXML, builder);
        } catch (DataAccessException le) {
            throw le;
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
        return builder.build();
    }

    public void setDAService(LicenseDataAccessService daSrv) {
        DAService = daSrv;
    }

    private static class Converter {

        private static LicenseModelBuilder convert(String xml, LicenseModelBuilder builder)
            throws XMLStreamException, IOException {
            XmlToLicenseModelConverter converter = new XmlToLicenseModelConverter(
                new XmlInputFactoryWrapper().getFactory());
            converter.apply(Converter::setLicenseModel, xml, builder);
            return builder;
        }

        private static void setLicenseModel(XMLStreamReader re, LicenseModelBuilder builder) throws XMLStreamException {
            while (re.hasNext() && !builder.isReady()) {
                re.next();
                if (re.isStartElement() && !trySetEntitlementPoolUuid(re, builder)) {
                    trySetLicenseKeyGroupUuid(re, builder);
                }
            }
        }

        private static boolean trySetEntitlementPoolUuid(XMLStreamReader re, LicenseModelBuilder builder)
            throws XMLStreamException {
            if (re.getName().getLocalPart().equals(ENTITLEMENT_POOL_UUID)) {
                builder.setEntitlementPoolUuid(re.getElementText());
                return true;
            }
            return false;
        }

        private static void trySetLicenseKeyGroupUuid(XMLStreamReader re, LicenseModelBuilder builder)
            throws XMLStreamException {
            if (re.getName().getLocalPart().equals(LICENSE_KEY_GROUP_UUID)) {
                builder.setLicenseKeyGroupUuid(re.getElementText());
            }
        }
    }
}
