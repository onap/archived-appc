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

package org.openecomp.appc.listener.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion;

/**
 * This class represents a message coming in from DCAE.
 *
 */
@JsonSerialize(include = Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class IncomingMessage extends CommonMessage {

    private static final long serialVersionUID = 1L;

    /*
     * The action being requested. Its presence signals that it is an incoming message and it is not present on outgoing
     * messages
     */
    //TODO; use enum
    @JsonProperty("Action")
    private String action;


    public String getRequest() {
        return action;
    }

    @JsonIgnore
    public Action getAction() {
        return Action.toAction(action);
    }

    public void setRequest(String request) {
        this.action = request;
    }

//    @Override
//    public String toString() {
//        String time = getRequestTime() != null ? getRequestTime() : "N/A";
//        // String req = request != null ? request : "N/A";
//        return String.format("[%s - %s]", time, getId());
//    }

//    public String toOutgoing(Status status) {
//        return toOutgoing(status);
//    }

    public String toOutgoing(Status status) {
        OutgoingMessage out = new OutgoingMessage(this);
        out.setResponse(status);
        return out.toResponse().toString();
    }

    /**
     * Determines if this message should be parsed parsed. Will eventually check that the message is well formed, has
     * all required fields, and had not exceeded any timing restrictions.
     *
     * @return True if the message should be parsed. False otherwise
     */
    public boolean isValid() {
        return true;
    }
}
