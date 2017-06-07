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

package org.openecomp.appc.oam;


import org.openecomp.appc.executor.objects.Params;
import org.openecomp.appc.util.MessageFormatter;

import java.util.Map;

public enum OAMCommandStatus {

    ACCEPTED(100,"ACCEPTED - request accepted"),

    //ERROR(2xx) – request can’t be handled due to some technical error
    UNEXPECTED_ERROR(200,"UNEXPECTED ERROR - ${errorMsg}"),

    SUCCESS(400,"SUCCESS - request has been processed successfully"),
        ;


    public static final String errorDgMessageParamName = "errorDgMessage";

	private int responseCode;
	private String responseMessage;




    OAMCommandStatus(int responseCode, String responseMessage) {
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
        return "OAMCommandStatus{" +
                "responseCode=" + responseCode +
                ", responseMessage='" + responseMessage + '\'' +
                '}';
    }
}

