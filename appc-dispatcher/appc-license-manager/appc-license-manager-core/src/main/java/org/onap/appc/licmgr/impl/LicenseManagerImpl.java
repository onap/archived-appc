/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
 * Modifications Copyright (C) 2018 Nokia
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
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import org.onap.appc.licmgr.LicenseDataAccessService;
import org.onap.appc.licmgr.LicenseManager;
import org.onap.appc.licmgr.exception.DataAccessException;
import org.onap.appc.licmgr.objects.LicenseModel;
import org.onap.appc.licmgr.objects.LicenseModelBuilder;


@SuppressWarnings("all")
public class LicenseManagerImpl implements LicenseManager {

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
            convert(licenseModelXML, builder);
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

    private LicenseModelBuilder convert(String xml, LicenseModelBuilder builder)
        throws XMLStreamException, IOException {
        XmlToLicenseModelConverter converter = new XmlToLicenseModelConverter(
            XMLInputFactory.newInstance());
        converter.convert(VfLicenseModelXmlConsumer::setLicenseModel, xml, builder);
        return builder;
    }
}
