/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 Nokia
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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.onap.appc.licmgr.objects.LicenseModelBuilder;

class VfLicenseModelXmlConsumer {

    private static final String ENTITLEMENT_POOL_UUID = "entitlement-pool-uuid";
    private static final String LICENSE_KEY_GROUP_UUID = "license-key-group-uuid";

    static void setLicenseModel(XMLStreamReader re, LicenseModelBuilder builder) throws XMLStreamException {
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