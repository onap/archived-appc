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

package org.onap.appc.executor.objects;

import org.onap.appc.util.MessageFormatter;

import java.util.Map;

public enum LCMCommandStatus {

    ACCEPTED(100, "ACCEPTED - request accepted"),

    //ERROR(2xx) - request can't be handled due to some technical error
    UNEXPECTED_ERROR(200, "UNEXPECTED ERROR - ${errorMsg}"),

    //REJECT(3xx) - request has been rejected due to some business reason
    // (e.g. no such service-instance-id, command is not supported, etc)
    REJECTED(300, "REJECTED - ${errorMsg}"),
    /*
     * TODO: Change responseMessage from "INVALID INPUT PARAMETER" to "INVALID INPUT PARAMETER(S)" tracked ATTAPPC-4863
     *
     * With consideration of updating integration test case effort, "INVALID INPUT PARAMETER" is used while the task
     * ATTAPPC-4863 is tracking the need of this change when resource is available.
     *
     * However, when pushing this file to ONAP, responseMessage should be changed to "INVALID INPUT PARAMETER(S)",
     * and this comments should be removed.
     */
    // TODO 77777777 to support "${paramName} with invalid value ${paramValue}"
    INVALID_INPUT_PARAMETER(301, "INVALID INPUT PARAMETER - ${errorMsg}"),
    MISSING_MANDATORY_PARAMETER(302, "MISSING MANDATORY PARAMETER - Parameter/s ${paramName} is/are missing" ),
    REQUEST_PARSING_FAILED(303, "REQUEST PARSING FAILED - ${errorMsg}"),
    VNF_NOT_FOUND(306, "VNF NOT FOUND - VNF with ID ${vnfId} was not found" ),
    DG_WORKFLOW_NOT_FOUND(307, "DG WORKFLOW NOT FOUND - No DG workflow found for the combination of ${dgModule} module ${dgName} name and ${dgVersion} version"),//TODO need to support it
    WORKFLOW_NOT_FOUND(308, "WORKFLOW NOT FOUND - No workflow found for VNF type ${vnfTypeVersion} and ${actionName} action"),
    EXPIRED_REQUEST(311, "EXPIRED REQUEST"),
    DUPLICATE_REQUEST(312, "DUPLICATE REQUEST"),
    MISSING_VNF_DATA_IN_AAI(313, "MISSING VNF DATA IN A&AI - ${attributeName} not found for VNF ID = ${vnfId}"),
    VSERVER_NOT_FOUND(314, "VSERVER NOT FOUND - vserver with ID ${id} was not found"),
    MULTIPLE_REQUESTS_FOUND(315, "MULTIPLE REQUESTS FOUND - using search criteria: ${parameters}"),
    POLICY_VALIDATION_FAILURE(316,"POLICY VALIDATION FAILURE - ${errorMsg}"),
    EXLCUSIVE_REQUEST_IN_PROGRESS(317,"EXCLUSIVE REQUEST IN PROGRESS - ${errorMsg}"),
    LOCKED_VNF_ID(318,"${errorMsg}"),

    SUCCESS(400, "SUCCESS - request has been processed successfully"),

    //ERROR(4xx) - failure for Async response
    DG_FAILURE(401, "DG FAILURE - ${errorMsg}"),
    EXPIRED_REQUEST_FAILURE(404, "EXPIRED REQUEST FAILURE - failed after accepted because TTL expired");


    //ERROR(5xx) - failure for Intermediate response
    //        FAILURE(5xx) - request processing results with failure. The FAILURE response is always transmitted asynchronously, via DMaaP.

    public static final String errorDgMessageParamName = "errorDgMessage";

    private int responseCode;
    private String responseMessage;


    LCMCommandStatus(int responseCode, String responseMessage) {
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public int getResponseCode() {
        return responseCode;
    }

    /**
     * @return messageTemplate
     */
    public String getFormattedMessage(Params params) {
        Map<String, Object> paramsMap = params != null ? params.getParams() : null;
        return MessageFormatter.format(getResponseMessage(), paramsMap);
    }

    public String getFormattedMessageWithCode(Params params) {
        return getResponseCode() + "-" + getFormattedMessage(params);
    }

    @Override
    public String toString() {
        return "LCMCommandStatus{" +
            "responseCode=" + responseCode +
            ", responseMessage='" + responseMessage + '\'' +
            '}';
    }
}
