/*-
 * ============LICENSE_START=======================================================
 * ONAP : APP-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property.  All rights reserved.
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
 */

package org.openecomp.appc.flow.controller.utils;

public class FlowControllerConstants {
            
        public static String STRING_ENCODING = "utf-8";
        public static String Y = "Y";
        public static String N = "N";
        public static String DATA_TYPE_TEXT = "TEXT";
        public static String DATA_TYPE_JSON = "JSON";
        public static String DATA_TYPE_XML = "XML";
        public static String DATA_TYPE_SQL = "SQL";
    
        public static String INPUT_PARAM_RESPONSE_PRIFIX = "responsePrefix";
        
        public static String OUTPUT_PARAM_STATUS = "status";
        public static String OUTPUT_PARAM_ERROR_MESSAGE = "error-message";
        public static String OUTPUT_STATUS_SUCCESS = "success";
        public static String OUTPUT_STATUS_FAILURE = "failure";
        
        public static final String DESINGTIME = "DesignTime";
        public static final String RUNTIME = "RunTime";
        public static final String APPC_FLOW_CONTROLLER = "/appc-flow-controller.properties";
        public static final String VNF_TYPE = "vnf-type";
        public static final String ACTION = "action";
        public static final String VNFC_TYPE = "vnfc-type";
        public static final String VM_INSTANCE = "vm-instance";
        public static final String VM = "vm";
        public static final String VNFC = "vnfc";
        public static final String REFERENCE = "reference";
        public static final String VNFC_INSTANCE = "vnfc-instance";
        public static final String DEVICE_PROTOCOL = "device-protocol";
        public static final String DG_RPC = "dg-rpc";
        public static final String MODULE = "module";
        public static final String USER_NAME = "user-name";
        public static final String PORT_NUMBER = "port-number";
        public static final String DOWNLOAD_DG_REFERENCE = "download-dg-reference";
        public static final String REQUEST_ACTION = "request-action";
        public static final String VNF = "vnf";
        public static final String EXTERNAL = "External";
        public static final String ACTION_LEVEL = "action-level";
        public static final String ARTIFACT_NAME = "artifact-name";
                
        public static enum endPointType {DG,REST,NODE};        
        public static enum flowStatus {PENDING,IN_PROCESS,COMPLETED};
        
        public static final String GENERATION_NODE = "GENERATION-NODE";
        public static final String SEQUENCE_TYPE = "SEQUENCE-TYPE";
        public static final String CATEGORY = "CATEGORY";
        public static final String EXECUTION_NODE = "EXECUTION-NODE";
        

        public static final String REQUEST_ID = "reqeust-id";
        public static final String ARTIFACT_CONTENT = "artifact-content";
        public static final String ARTIFACT_CONTENT_ESCAPED = "artifact-content-escaped";
        public static final String FLOW_SEQUENCE = "flow-sequence";
        public static final String EXECUTTION_MODULE = "execution-module";
        public static final String EXECUTION_RPC = "execution-rpc";
        public static final String EXECUTION_TYPE = "execution-type";
        public static final String GRAPH = "graph";
        public static final String NODE = "node";
        public static final String REST = "rest";
        
        
        public static final String DB_SDC_ARTIFACTS = "ASDC_ARTIFACTS";
        public static final String DB_SDC_REFERENCE = "ASDC_REFERENCE";
        public static final String DB_REQUEST_ARTIFACTS = "REQUEST_ARTIFACTS";
        public static final String DB_MULTISTEP_FLOW_REFERENCE = "MULTISTEP_FLOW_REFERENCE";
        public static final String DB_PROTOCOL_REFERENCE = "PROTOCOL_REFERENCE";
        public static final String DB_PROCESS_FLOW_REFERENCE = "PROCESS_FLOW_REFERENCE";
        public static final String MOCK_HEALTHCHECK = "mock-healthcheck";
        public static final String ACTION_IDENTIFIER = "action-identifier";
        public static final String PAYLOAD = "payload";
        public static final String FAILURE = "failure";
        public static final String SUCCESS = "success";
        public static final String OTHERS = "Others";
        public static final String RESPONSE_PREFIX = "response-prefix";
        public static final String OUTPUT_STATUS_MESSAGE = "status-message";
        public static final String HEALTHY = "healthy";
        public static final String INPUT_URL = "input.url";
        public static final String INPUT_HOST_IP_ADDRESS = "host-ip-address";
        public static final String INPUT_PORT_NUMBER = "port-number";
        public static final String INPUT_CONTEXT = "context";
        public static final String INPUT_SUB_CONTEXT = "sub-context";
        public static final String INPUT_REQUEST_ACTION_TYPE = "request-action-type";
        public static final String INPUT_REQUEST_ACTION = "request-action";
        public static final String HTTP = "http://";
        
        public static final String VNF_ID = "vnf-id";
        public static final String VSERVER_ID = "vserver-id";
        public static final String SEQ_GENERATOR_URL = "seq_generator_url";
        public static final String SEQ_GENERATOR_UID = "seq_generator.uid";
        public static final String SEQ_GENERATOR_PWD = "seq_generator.pwd";
        public static final String CAPABILITY ="capability";
        public static final String DEPENDENCYMODEL ="tosca_dependency_model";
        public static final String VF_MODULE ="vf-module";
        public static final String VNFC_NAME = "vnfc-name";
    }
