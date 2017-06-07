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

    public OutgoingMessage() {

    }

    public OutgoingMessage(IncomingMessage msg) {
    	setHeader(msg.getHeader());
    	setPayload(msg.getPayload());
//        setId(msg.getId());
//        setOriginalRequest(msg.getRequest());
//        setRequestClient(msg.getRequestClient());
//        setRequestTime(msg.getRequestTime());
//        setVmName(msg.getVmName());
//        setFromSystem(generateFrom());
//        setResponse(Status.PENDING);
//        setPolicyName(msg.getPolicyName());
//        setPolicyVersion(msg.getPolicyVersion());
//        setStartTime(msg.getStartTime());
    }
    
    private static final long serialVersionUID = -5447940920271469613L;
    /*
     * The status of the response
     */
    @JsonProperty("Status")
    private OutStatus status;

    /**
	 * @return the status
	 */
	public OutStatus getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(OutStatus status) {
		this.status = status;
	}

	public void updateResponseTime() {
        SecureRandom rand = new SecureRandom();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss.SSS");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        String date = df.format(new Date(Time.utcTime()));
        //this.responseTime = String.format("%s%03d", date, rand.nextInt(1000));
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
            //json.put("message", this.getResponse().toString());
        }

        // Removed duplication of status from message for 1602
        // json.put("message", String.format("%s: %s", request, json.get("message")));

        return json;
    }

//    @Override
//    public String toString() {
//        return String.format("%s - %s", getId(), getResponse());
//    }
    
    public static class OutStatus{
    	@JsonProperty("Code")
    	private String code;
    	
    	@JsonProperty("Value")
    	private String value;

		/**
		 * @return the code
		 */
		public String getCode() {
			return code;
		}

		/**
		 * @param code the code to set
		 */
		public void setCode(String code) {
			this.code = code;
		}

		/**
		 * @return the value
		 */
		public String getValue() {
			return value;
		}

		/**
		 * @param value the value to set
		 */
		public void setValue(String value) {
			this.value = value;
		}
    	
    }

	public void setResponse(Status newStatus) {
		if(this.status == null){
			this.status = new OutStatus();
		}
		
		switch (newStatus){
		case ACCEPTED:
			this.status.setValue(newStatus.getValue());
			this.status.setCode("100");
			break;

		case FAILURE:
			this.status.setValue(newStatus.getValue());
			this.status.setCode("500");
			break;

		case SUCCESS:
			this.status.setValue(newStatus.getValue());
			this.status.setCode("400");
			break;
		default:
			break;
			
		}
		
	}
}
