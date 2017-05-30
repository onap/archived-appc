/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 						reserved.
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

package org.openecomp.appc.dg.common.impl;



class Constants {

    public static final String DG_ERROR_FIELD_NAME = "org.openecomp.appc.dg.error";
    public static final String DG_OUTPUT_STATUS_MESSAGE = "output.status.message";
    public static final String API_VERSION_FIELD_NAME = "org.openecomp.appc.apiversion";
    public static final String REQ_ID_FIELD_NAME = "org.openecomp.appc.reqid";
    public static final String PAYLOAD = "payload";

    enum LegacyAttributes {
        Action("org.openecomp.appc.action"),
        VMID("org.openecomp.appc.vmid"),
        IdentityURL("org.openecomp.appc.identity.url"),
        TenantID("org.openecomp.appc.tenant.id");

        private String value;
        LegacyAttributes(String value) {this.value = value;}
        String getValue() {return value;}
    };

    enum LCMAttributes {
        Action("input.action"),
        Payload("input.payload"),
        VMID("vm-id"),
        IdentityURL("identity-url"),
        TenantID("tenant.id");

        private String value;
        LCMAttributes(String value) {this.value = value;}
        String getValue() {return value;}
    };

}
