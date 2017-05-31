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

package org.openecomp.appc.domainmodel.lcm;

import java.time.Instant;


public class CommonHeader {

    private Flags flags;
    private Instant timestamp;
    private String apiVer;
    private String originatorId;
    private String requestId;
    private String subRequestId;

    public Flags getFlags() {
        return flags;
    }

    public void setFlags(Flags flags) {
        this.flags = flags;
    }

    public Instant getTimeStamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getApiVer() {
        return apiVer;
    }

    public void setApiVer(String apiVer) {
        this.apiVer = apiVer;
    }

    public String getOriginatorId() {
        return originatorId;
    }

    public void setOriginatorId(String originatorId) {
        this.originatorId = originatorId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getSubRequestId() {
        return subRequestId;
    }

    public void setSubRequestId(String subRequestId) {
        this.subRequestId = subRequestId;
    }

    @Override
    public String toString() {
        return "CommonHeader{" +
                "flags=" + flags +
                ", timestamp=" + timestamp +
                ", apiVer='" + apiVer + '\'' +
                ", originatorId='" + originatorId + '\'' +
                ", requestId='" + requestId + '\'' +
                ", subRequestId='" + subRequestId + '\'' +
                '}';
    }
}
