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

package org.openecomp.appc.adapter.message.event;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EventStatus {

    @JsonProperty("code")
    private final Integer code;

    @JsonProperty("reason")
    private final String reason;

    public EventStatus(Integer code, String aReason) {
        this.code = code;
        reason = aReason;
    }


    public Integer getCode() {
        return code;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return "EventStatus{" +
                "code=" + code +
                ", reason='" + reason + '\'' +
                '}';
    }
}
