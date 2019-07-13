/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
 * Modifications Copyright (C) 2019 IBM
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

package org.onap.appc.adapter.message.event;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EventStatus implements Serializable {
  private static final long serialVersionUID = 1L;
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
