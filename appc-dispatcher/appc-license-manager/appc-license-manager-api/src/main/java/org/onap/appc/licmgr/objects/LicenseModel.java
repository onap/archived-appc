/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
 * Copyright (C) 2018 Nokia
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

package org.onap.appc.licmgr.objects;

public final class LicenseModel {

    private final String entitlementPoolUuid;
    private final String licenseKeyGroupUuid;

    public LicenseModel(String entitlementPoolUuid, String licenseKeyGroupUuid){
        this.entitlementPoolUuid = entitlementPoolUuid;
        this.licenseKeyGroupUuid = licenseKeyGroupUuid;
    }

    public String getEntitlementPoolUuid() {
        return entitlementPoolUuid;
    }
    public String getLicenseKeyGroupUuid() {
        return licenseKeyGroupUuid;
    }
}
