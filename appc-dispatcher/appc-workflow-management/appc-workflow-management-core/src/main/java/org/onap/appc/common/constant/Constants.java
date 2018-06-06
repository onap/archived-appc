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

package org.onap.appc.common.constant;

public class Constants {

    public static final String DG_ATTRIBUTE_STATUS = "SvcLogic.status";
    public static final String DG_STATUS_SUCCESS = "success";
    public static final String DG_ATTRIBUTE_STATUS_CODE = "SvcLogic.status.code";
    public static final String DG_OUTPUT_STATUS_CODE = "output.status.code";
    public static final String DG_OUTPUT_STATUS_MESSAGE = "output.status.message";


    /**
     * The name of the property that contains the VM id value in the graph's context
     */
    public static final String VF_ID = "org.onap.appc.vfid";

    /**
     * The name of the property that contains the VF Type value in the graph's context
     */
    public static final String VF_TYPE = "org.onap.appc.vftype";

    /**
     * The name of the property that contains the service request id value in the graph's context
     */
    public static final String REQUEST_ID = "org.onap.appc.reqid";

    /**
     * The name of the property that indicates which method of the IaaS adapter to call
     */
    public static final String ACTION = "org.onap.appc.action";

    public static final String PAYLOAD = "payload";

    public static final String CONF_ID = "org.onap.appc.confid";

    public static final String API_VERSION = "org.onap.appc.apiversion";

    public static final String ORIGINATOR_ID = "org.onap.appc.originatorid";

    public static final String OBJECT_ID ="org.onap.appc.objectid";

    public static final String SUB_REQUEST_ID = "org.onap.appc.subrequestid";

    public static final String ERROR_MESSAGE = "error-message";

}
