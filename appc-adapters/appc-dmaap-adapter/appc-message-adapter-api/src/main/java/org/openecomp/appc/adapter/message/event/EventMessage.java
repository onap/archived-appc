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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion;

import java.io.IOException;
import java.io.Serializable;

/*
    {
        "EventHeader": {
        "eventTime": "2016-03-15T10:59:33.79Z",
        "apiVer": "1.01",
        "EventId": "<ECOMP_EVENT_ID>",
    },
        "EventStatus": {
            "code": "NNN",
            "reason": "A reason"
        }
    }
*/


@JsonSerialize(include = Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @JsonProperty("eventHeader")
    private EventHeader eventHeader;
    @JsonProperty("eventStatus")
    private EventStatus eventStatus;

    public EventMessage(EventHeader eventHeader, EventStatus eventStatus) {
        this.eventHeader = eventHeader;
        this.eventStatus = eventStatus;
    }

    public EventHeader getEventHeader() {
        return eventHeader;
    }

    public void setEventHeader(EventHeader eventHeader) {
        this.eventHeader = eventHeader;
    }

    public EventStatus getEventStatus() {
        return eventStatus;
    }

    public void setEventStatus(EventStatus eventStatus) {
        this.eventStatus = eventStatus;
    }

    public String toJson() {
        try {
            return OBJECT_MAPPER.writeValueAsString(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "EventMessage{" +
                "eventHeader=" + eventHeader +
                ", eventStatus=" + eventStatus +
                '}';
    }
}
