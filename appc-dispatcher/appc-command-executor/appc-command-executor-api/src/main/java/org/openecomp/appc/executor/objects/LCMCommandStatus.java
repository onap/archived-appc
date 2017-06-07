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

package org.openecomp.appc.executor.objects;


import org.apache.commons.lang3.StringUtils;
import org.openecomp.appc.domainmodel.lcm.Status;
import org.openecomp.appc.util.MessageFormatter;

import java.util.Map;

public enum LCMCommandStatus {

    ACCEPTED(100,"ACCEPTED - request accepted"),

    //ERROR(2xx) – request can’t be handled due to some technical error
    UNEXPECTED_ERROR(200,"UNEXPECTED ERROR - ${errorMsg}"),

    //REJECT(3xx) – request has been rejected due to some business reason (e.g. no such service-instance-id, command is not supported, etc)
    REJECTED(300,"REJECTED - ${errorMsg}"),
    INVALID_INPUT_PARAMETER(301,"INVALID INPUT PARAMETER - ${errorMsg}"),// TODO 77777777 to support "${paramName} with invalid value ${paramValue}"
    MISSING_MANDATORY_PARAMETER(302,"MISSING MANDATORY PARAMETER - Parameter/s ${paramName} is/are missing" ),
    REQUEST_PARSING_FAILED(303,"REQUEST PARSING FAILED - ${errorMsg}"),
    NO_TRANSITION_DEFINE(304,"ACTION IS NOT ALLOWED - Action ${actionName} is not allowed for VNF in state ${currentState}"),
    INVALID_VNF_STATE(305,"Request rejected because VNF status in A&AI is - ${currentState}" ),
    VNF_NOT_FOUND(306,"VNF NOT FOUND - VNF with ID ${vnfId} was not found" ),
    DG_WORKFLOW_NOT_FOUND(307,"DG WORKFLOW NOT FOUND - No DG workflow found for the combination of ${dgModule} module ${dgName} name and ${dgVersion} version"),//TODO need to support it
    WORKFLOW_NOT_FOUND(308,"WORKFLOW NOT FOUND - No workflow found for VNF type ${vnfTypeVersion} and ${actionName} action"),
	UNSTABLE_VNF(309,"UNSTABLE VNF - VNF ${vnfId} is not stable to accept the command"),
    LOCKING_FAILURE(310,"LOCKING FAILURE - ${errorMsg}" ),
	EXPIRED_REQUEST(311,"EXPIRED REQUEST"),
    DUPLICATE_REQUEST(312,"DUPLICATE REQUEST"),
    MISSING_VNF_DATA_IN_AAI(313,"MISSING VNF DATA IN A&AI - ${attributeName} not found for VNF ID = ${vnfId}"),

    SUCCESS(400,"SUCCESS - request has been processed successfully"),


    //        FAILURE(5xx) – request processing results with failure. The FAILURE response is always transmitted asynchronously, via DMaaP.
    DG_FAILURE(401,"DG FAILURE - ${errorMsg}"),
    NO_TRANSITION_DEFINE_FAILURE(402,"NO TRANSITION DEFINE - No Transition Defined for ${actionName} action and ${currentState} state"),
    UPDATE_AAI_FAILURE(403,"UPDATE_AAI_FAILURE - failed to update AAI. ${errorMsg}"),
    EXPIRED_REQUEST_FAILURE(404,"EXPIRED REQUEST FAILURE - failed after accepted because TTL expired"),
    UNEXPECTED_FAILURE(405,"UNEXPECTED FAILURE - ${errorMsg}"),
    UNSTABLE_VNF_FAILURE(406,"UNSTABLE VNF FAILURE - VNF ${vnfId} is not stable to accept the command"),

        ;


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
     *
     * @return  messageTemplate
     */


    public String getFormattedMessage(Params params){
            Map<String,Object> paramsMap = params != null ? params.getParams() : null;
            return MessageFormatter.format(getResponseMessage(),paramsMap);

        }

    public String getFormattedMessageWithCode(Params params){
        return getResponseCode()+"-" + getFormattedMessage(params);
    }

    @Override
    public String toString() {
        return "LCMCommandStatus{" +
                "responseCode=" + responseCode +
                ", responseMessage='" + responseMessage + '\'' +
                '}';
    }

    public Status toStatus(Params params) {
        return new Status(responseCode, getFormattedMessage(params));
    }
}

