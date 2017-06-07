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

package org.openecomp.appc.logging;

public class LoggingConstants {

    public static class MDCKeys {

        public static final String ERROR_CODE = "ErrorCode";
        public static final String ERROR_DESCRIPTION = "ErrorDescription";
        public static final String STATUS_CODE = "StatusCode";
        public static final String RESPONSE_CODE = "ResponseCode";
        public static final String RESPONSE_DESCRIPTION = "ResponseDescription";
        public static final String TARGET_ENTITY = "TargetEntity";
        public static final String TARGET_SERVICE_NAME = "TargetServiceName";
        public static final String PARTNER_NAME = "PartnerName";
        public static final String SERVER_NAME = "ServerName";
        public static final String BEGIN_TIMESTAMP = "BeginTimestamp";
        public static final String END_TIMESTAMP = "EndTimestamp";
        public static final String ELAPSED_TIME = "ElapsedTime";
        public static final String CLASS_NAME = "ClassName";
        public static final String TARGET_VIRTUAL_ENTITY = "TargetVirtualEntity";
    }

    public static class StatusCodes {
        public static final String COMPLETE = "COMPLETE";
        public static final String ERROR = "ERROR";
    }

    public static class TargetNames {
        public static final String APPC = "APPC";
        public static final String AAI = "A&AI";
        public static final String DB = "DataBase";
        public static final String APPC_PROVIDER = "APPC Provider";
        public static final String APPC_OAM_PROVIDER = "APPC OAM Provider";
        public static final String STATE_MACHINE = "StateMachine";
        public static final String WORKFLOW_MANAGER = "WorkflowManager";
        public static final String REQUEST_VALIDATOR = "RequestValidator";
        public static final String LOCK_MANAGER = "LockManager";
        public static final String REQUEST_HANDLER = "RequestHandler";
    }

    public static class TargetServiceNames{

        public static class AAIServiceNames{
            public static final String QUERY = "query";
            public static final String GET_VNF_DATA = "getVnfData";
        }

    }

}
