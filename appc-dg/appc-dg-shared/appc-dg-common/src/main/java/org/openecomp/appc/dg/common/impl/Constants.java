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

package org.openecomp.appc.dg.common.impl;



class Constants {

    public static final String DG_ERROR_FIELD_NAME = "org.openecomp.appc.dg.error";
    public static final String DG_OUTPUT_STATUS_MESSAGE = "output.status.message";
    public static final String EVENT_MESSAGE = "event-message";
    public static final String ATTRIBUTE_ERROR_MESSAGE = "error-message";
    public static final String ATTRIBUTE_SUCCESS_MESSAGE = "success-message";
    public static final String DG_ERROR_CODE = "output.status.dgerror.code";
    public static final String API_VERSION_FIELD_NAME = "org.openecomp.appc.apiversion";
    public static final String REQ_ID_FIELD_NAME = "org.openecomp.appc.reqid";
    public static final String PAYLOAD = "payload";
    public static final String OUTPUT_PAYLOAD = "output.payload";

    //Added for VnfExecution Flow
    public static final String FLOW_STRATEGY = "FlowStrategy" ;
    public static final String DEPENDENCY_TYPE = "DependencyType";
    public static final String VNF_TYPE = "vnfType";
    public static final String VNF_VERION = "vnfVersion";


    public static final String APPC_INSTANCE_ID= "appc-instance-id";

    //Added for Cvaas
    public static final String CVAAS_DIRECTORY_PATH = "cvaas-directory-path";
    public static final String CVAAS_FILE_NAME = "cvaas-file-name";
    public static final String CVAAS_FILE_CONTENT = "cvaas-file-content";

    enum LegacyAttributes {
        Action("org.openecomp.appc.action"),
        VMID("org.openecomp.appc.vmid"),
        IdentityURL("org.openecomp.appc.identity.url"),
        TenantID("org.openecomp.appc.tenant.id"),
        SkipHypervisorCheck("org.openecomp.appc.skiphypervisorcheck");

        private String value;
        LegacyAttributes(String value) {this.value = value;}
        String getValue() {return value;}
    };

    enum LCMAttributes {
        Action("input.action"),
        Payload("input.payload"),
        VMID("vm-id"),
        IdentityURL("identity-url"),
        TenantID("tenant.id"),
        SkipHypervisorCheck("skip-hypervisor-check");

        private String value;
        LCMAttributes(String value) {this.value = value;}
        String getValue() {return value;}
    };

    // DG Resolver Constants
    public static final String IN_PARAM_VNF_TYPE = "vnfType";
    public static final String IN_PARAM_VNFC_TYPE = "vnfcType";
    public static final String IN_PARAM_ACTION = "action";
    public static final String IN_PARAM_API_VERSION = "api-ver";

    public static final String OUT_PARAM_DG_NAME = "dg_name";
    public static final String OUT_PARAM_DG_VERSION= "dg_version";
    public static final String OUT_PARAM_DG_MODULE = "dg_module";

    public static final String TABLE_NAME = "VNFC_DG_MAPPING";
    public static final String TABLE_COLUMN_VNF_TYPE = "VNF_TYPE";
    public static final String TABLE_COLUMN_VNFC_TYPE = "VNFC_TYPE";
    public static final String TABLE_COLUMN_ACTION = "ACTION";
    public static final String TABLE_COLUMN_API_VERSION = "API_VERSION";
    public static final String TABLE_COLUMN_DG_NAME = "DG_NAME";
    public static final String TABLE_COLUMN_DG_VERSION= "DG_VERSION";
    public static final String TABLE_COLUMN_DG_MODULE = "DG_MODULE";
}
