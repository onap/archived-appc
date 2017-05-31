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

package org.openecomp.appc.listener.CL.model;

import java.io.Serializable;

import org.json.JSONObject;
import org.openecomp.appc.listener.util.Mapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion;

/**
 * This class holds attributes that are common to DMaaP messages both coming in from DCAE and being sent out by APPC
 *
 */
@JsonSerialize(include = Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommonMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /*
     * The unique id of the event as of 1602
     */
    @JsonProperty("eventID")
    private String id;

    /*
     * The time that the request was sent out.
     */
    @JsonProperty("requestTime")
    private String requestTime;

    /*
     * The originator of the event
     */
    @JsonProperty("requestClient")
    private String requestClient;

    /*
     * The system that sent the message
     */
    @JsonProperty("from")
    private String fromSystem;

    /*
     * The actual trap message
     */
    @JsonProperty("message")
    private String message;

    /*
     * The vm name associated with the event
     */
    @JsonProperty("VMName")
    private String vmName;

    /*
     * The policy name on the incoming event
     */
    @JsonProperty("policyName")
    private String policyName;

    /*
     * The policy version on the incoming event
     */
    @JsonProperty("policyVersion")
    private String policyVersion;

    @JsonIgnore
    private long startTime = System.currentTimeMillis();

    /*
     * Getters and Setters
     */

    public String getId() {
        return id;
    }

    public String getRequestTime() {
        return requestTime;
    }

    public String getRequestClient() {
        return requestClient;
    }

    public String getFromSystem() {
        return fromSystem;
    }

    public String getMessage() {
        return message;
    }

    public String getPolicyName() {
        return policyName;
    }

    public String getPolicyVersion() {
        return policyVersion;
    }

    public String getVmName() {
        return vmName;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setId(String eventId) {
        id = eventId;
    }

    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime;
    }

    public void setRequestClient(String requestClient) {
        this.requestClient = requestClient;
    }

    public void setFromSystem(String fromSystem) {
        this.fromSystem = fromSystem;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setPolicyName(String name) {
        policyName = name;
    }

    public void setPolicyVersion(String version) {
        policyVersion = version;
    }

    public void setVmName(String vmName) {
        this.vmName = vmName;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * Convenience method to return a json representation of this object.
     * 
     * @return The json representation of this object
     */
    public JSONObject toJson() {
        return Mapper.toJsonObject(this);
    }

}
