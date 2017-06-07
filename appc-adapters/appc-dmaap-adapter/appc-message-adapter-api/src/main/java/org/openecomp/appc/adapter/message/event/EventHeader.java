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

public class EventHeader {

    @JsonProperty("eventTime")
    private final String eventTime;

    @JsonProperty("apiVer")
    private final String apiVer;

    @JsonProperty("eventId")
    private final String eventId;

    public EventHeader(String eventTime, String apiVer, String eventId) {
        this.eventTime = eventTime;
        this.apiVer = apiVer;
        this.eventId = eventId;
    }

    public String getEventTime() {
        return eventTime;
    }

    public String getApiVer() {
        return apiVer;
    }

    public String getEventId() {
        return eventId;
    }

    @Override
    public String toString() {
        return "EventHeader{" +
                "eventTime='" + eventTime + '\'' +
                ", apiVer='" + apiVer + '\'' +
                ", eventId='" + eventId + '\'' +
                '}';
    }
}
