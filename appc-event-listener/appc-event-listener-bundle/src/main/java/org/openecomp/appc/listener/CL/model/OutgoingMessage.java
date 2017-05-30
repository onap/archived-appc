/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 						reserved.
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

import java.net.InetAddress;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONObject;
import org.openecomp.appc.listener.util.Mapper;
import org.openecomp.appc.util.Time;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion;

/**
 * This class represents a message being sent out to DMaaP by APPC to update listeners on the status of a request
 *
 */
@JsonSerialize(include = Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OutgoingMessage extends CommonMessage {

    private static final long serialVersionUID = -5447940920271469613L;

    @JsonProperty("response")
    private Status response;

    @JsonProperty("responseTime")
    private String responseTime;

    @JsonProperty("originalRequest")
    private String originalRequest;

    public OutgoingMessage() {

    }

    public OutgoingMessage(IncomingMessage msg) {
        setId(msg.getId());
        setOriginalRequest(msg.getRequest());
        setRequestClient(msg.getRequestClient());
        setRequestTime(msg.getRequestTime());
        setVmName(msg.getVmName());
        setFromSystem(generateFrom());
        setResponse(Status.PENDING);
        setPolicyName(msg.getPolicyName());
        setPolicyVersion(msg.getPolicyVersion());
        setStartTime(msg.getStartTime());
    }

    @JsonProperty("duration")
    public long getDuration() {
        return System.currentTimeMillis() - getStartTime();
    }

    public Status getResponse() {
        return response;
    }

    public String getResponseTime() {
        return responseTime;
    }

    public String getOriginalRequest() {
        return originalRequest;
    }

    @JsonIgnore
    public void setResponse(Status response) {
        this.response = response;
    }

    public void setResponse(String responseString) {
        this.response = Status.valueOf(responseString);
    }

    public void setResponseTime(String responseTime) {
        this.responseTime = responseTime;
    }

    public void setOriginalRequest(String originalRequest) {
        this.originalRequest = originalRequest;
    }

    public void updateResponseTime() {
        SecureRandom rand = new SecureRandom();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss.SSS");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        String date = df.format(new Date(Time.utcTime()));
        this.responseTime = String.format("%s%03d", date, rand.nextInt(1000));
    }

    public String generateFrom() {
        String name;
        try {
            InetAddress iAddress = InetAddress.getLocalHost();
            name = iAddress.getCanonicalHostName();
        } catch (Exception e) {
            // Could not get anything from the InetAddress
            name = "UnknownHost";
        }
        return "appc@" + name;
    }

    public JSONObject toResponse() {
        updateResponseTime();
        JSONObject json = Mapper.toJsonObject(this);

        if (!json.has("message")) {
            // If there is no message, parrot the status (response field)
            // TODO - Can this be removed for 1602 making message truely optional?
            json.put("message", this.getResponse().toString());
        }

        // Removed duplication of status from message for 1602
        // json.put("message", String.format("%s: %s", request, json.get("message")));

        return json;
    }

    @Override
    public String toString() {
        return String.format("%s - %s", getId(), getResponse());
    }
}
