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
package org.onap.appc.data.services;

public class AppcDataServiceConstant {

    public static final String INPUT_PARAM_RESPONSE_PREFIX = "responsePrefix";
    public static final String OUTPUT_STATUS_SUCCESS = "success";
    public static final String OUTPUT_STATUS_FAILURE = "failure";
    public static final String INPUT_PARAM_MESSAGE = "message";
    public static final String INPUT_PARAM_MESSAGE_TYPE = "messageType";
    public static final String OUTPUT_PARAM_STATUS = "status";
    public static final String OUTPUT_PARAM_ERROR_MESSAGE = "error-message";
    public static final String INPUT_PARAM_FILE_CATEGORY = "fileCategory";
    public static final String INPUT_PARAM_VM_INSTANCE = "vmInstance";
    public static final String INPUT_PARAM_SDC_ARTIFACT_IND = "asdcArtifactInd";
    public static final String INPUT_PARAM_VNF_ID = "vnfId";
    public static final String INPUT_PARAM_VM_NAME = "vmName";
    
    public static final String INPUT_PARAM_FILE_ID = "fileId";
    
    public static final String INPUT_PARAM_UPLOAD_CONFIG_ID= "uploadConfigId";
    public static final String CAPABILITY_VM_LEVEL="vm";
    public static final String KEY_VSERVER_ID = "vserver-id";
    public static final String KEY_VNFC_FUNCTION_CODE = "vnfc-function-code";

    public enum ACTIONS
    {
       Start, Stop, Restart;
    }
    
    
}