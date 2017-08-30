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

/**
 * NOTE: This file is auto-generated and should not be changed manually.
 */
package org.openecomp.appc.client.lcm.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A common header for all APP-C requests
 */
@javax.annotation.Generated(
    value = {"templates/client-kit/open-api-to-java.ftl"},
    date = "2017-05-04T20:09:01.49+05:30",
    comments = "Auto-generated from Open API specification")
public class CommonHeader {

    @JsonProperty("timestamp")
    private ZULU timestamp;

    @JsonProperty("api-ver")
    private String apiVer;

    @JsonProperty("originator-id")
    private String originatorId;

    @JsonProperty("request-id")
    private String requestId;

    @JsonProperty("sub-request-id")
    private String subRequestId;

    @JsonProperty("flags")
    private Flags flags;

    /**
     * Define a common definition of a time stamp (expressed as a formatted string) as follows yyyy-MM-ddTHH:mm:ss.SSSSSSSSZ
     */
    public ZULU getTimestamp() {
        return timestamp;
    }

    /**
     * Define a common definition of a time stamp (expressed as a formatted string) as follows yyyy-MM-ddTHH:mm:ss.SSSSSSSSZ
     */
    public void setTimestamp(ZULU timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * api-ver is the API version identifier. A given release of APPC should support all previous versions of APPC API (correlate with general requirements)
     */
    public String getApiVer() {
        return apiVer;
    }

    /**
     * api-ver is the API version identifier. A given release of APPC should support all previous versions of APPC API (correlate with general requirements)
     */
    public void setApiVer(String apiVer) {
        this.apiVer = apiVer;
    }

    /**
     * originator-id an identifier of the calling system which can be used addressing purposes, i.e. returning asynchronous response to the proper destination over UEB (especially in case of multiple consumers of APP-C APIs)
     */
    public String getOriginatorId() {
        return originatorId;
    }

    /**
     * originator-id an identifier of the calling system which can be used addressing purposes, i.e. returning asynchronous response to the proper destination over UEB (especially in case of multiple consumers of APP-C APIs)
     */
    public void setOriginatorId(String originatorId) {
        this.originatorId = originatorId;
    }

    /**
     * UUID for the request ID. An OSS/BSS identifier for the request that caused the current action. Multiple API calls may be made with the same request-id The request-id shall be recorded throughout the operations on a single request
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * UUID for the request ID. An OSS/BSS identifier for the request that caused the current action. Multiple API calls may be made with the same request-id The request-id shall be recorded throughout the operations on a single request
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /**
     * Uniquely identifies a specific LCM action. It is persistent over the life-cycle of a single request
     */
    public String getSubRequestId() {
        return subRequestId;
    }

    /**
     * Uniquely identifies a specific LCM action. It is persistent over the life-cycle of a single request
     */
    public void setSubRequestId(String subRequestId) {
        this.subRequestId = subRequestId;
    }

    /**
     * Flags are generic flags that apply to any and all commands, all are optional
     */
    public Flags getFlags() {
        return flags;
    }

    /**
     * Flags are generic flags that apply to any and all commands, all are optional
     */
    public void setFlags(Flags flags) {
        this.flags = flags;
    }

}
