/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.appc.sdc.artifacts.helper;

/**
 * Constants required in SDC listener module
 */
public class Constants {
    static final String SDC_ARTIFACTS = "ASDC_ARTIFACTS";
    static final String SDC_REFERENCE = "ASDC_REFERENCE";

    static final String AND = " AND ";

    static final String ARTIFACT_TYPE = "ARTIFACT_TYPE";
    static final String ARTIFACT_NAME = "ARTIFACT_NAME";

    static final String VF_LICENSE = "VF_LICENSE";

    public enum SDC_ARTIFACTS_FIELDS {
        SERVICE_UUID,
        DISTRIBUTION_ID,
        SERVICE_NAME,
        SERVICE_DESCRIPTION,
        RESOURCE_UUID,
        RESOURCE_INSTANCE_NAME,
        RESOURCE_NAME,
        RESOURCE_VERSION,
        RESOURCE_TYPE,
        ARTIFACT_UUID,
        ARTIFACT_VERSION,
        ARTIFACT_DESCRIPTION,
        INTERNAL_VERSION,
        CREATION_DATE,
        ARTIFACT_CONTENT
    }

    public enum SDC_REFERENCE_FIELDS{
        SDC_REFERENCE_ID,
        VNF_TYPE,
        VNFC_TYPE,
        FILE_CATEGORY,
        ACTION
    }

    static final String COMMA = " , ";
    static final String QUERY_PLACEHOLDER = " = ? ";
    static final String SELECT_FROM = "SELECT * FROM " ;
    static final String WHERE = " WHERE ";
    static final String INSERT = "INSERT INTO ";
}
