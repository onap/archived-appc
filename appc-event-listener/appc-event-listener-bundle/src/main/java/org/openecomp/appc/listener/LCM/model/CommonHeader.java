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

package org.openecomp.appc.listener.LCM.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;

import org.openecomp.appc.util.Time;

public class CommonHeader implements Serializable {
    @JsonProperty("timestamp")
    private String timeStamp;
    @JsonProperty("api-ver")
    private String apiVer;
    @JsonProperty("originator-id")
    private String originatorId;
    @JsonProperty("request-id")
    private String requestID;
    @JsonProperty("sub-request-id")
    private String subRequestId;
    @JsonProperty("flags")
    private Map<String, String> flags;

    private static final DateFormat ZULU_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'Z'");

    public CommonHeader() {
    }

    public CommonHeader(CommonHeader commonHeader) {
        // changed to current system time
        timeStamp = ZULU_FORMATTER.format(Time.utcDate());

        apiVer = commonHeader.getApiVer();
        originatorId = commonHeader.getOriginatorId();
        requestID = commonHeader.getRequestID();
        subRequestId = commonHeader.getSubRequestId();
        flags = commonHeader.getFlags();
    }



    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getApiVer() {
        return apiVer;
    }

    public void setApiVer(String apiVer) {
        this.apiVer = apiVer;
    }

    public String getRequestID() {
        return requestID;
    }

    public void setRequestID(String requestID) {
        this.requestID = requestID;
    }

    public String getOriginatorId() {
        return originatorId;
    }

    public void setOriginatorId(String originatorId) {
        this.originatorId = originatorId;
    }

    public String getSubRequestId() {
        return subRequestId;
    }

    public void setSubRequestId(String subRequestId) {
        this.subRequestId = subRequestId;
    }

    public Map<String, String> getFlags() {
        return flags;
    }

    public void setFlags(Map<String, String> flags) {
        this.flags = flags;
    }
}
