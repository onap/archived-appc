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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion;

/**
 * This class reperesnts a message coming in from DCAE.
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
    @JsonProperty("request")
    private String request;

    /*
     * The url for the server used for auth. http://<compute>:<port>/<tenentId>/server/<serverID>
     */
    @JsonProperty("VServerSelfLink")
    private String Url;

    /*
     * The tenant Id in OpenStack that the server belongs to
     */
    @JsonProperty("TenantID")
    private String TenantId;

    /*
     * The VM's UUID in 
     */
    @JsonProperty("VMID")
    private String VmId;

    @JsonProperty("Identity")
    private String identityUrl;

    public String getRequest() {
        return request;
    }

    @JsonIgnore
    public Action getAction() {
        return Action.toAction(request);
    }

    public String getUrl() {
        return Url;
    }

    public String getTenantId() {
        return TenantId;
    }

    public String getVmId() {
        return VmId;
    }

    public String getIdentityUrl() {
        return identityUrl;
    }

    public void setUrl(String Url) {
        this.Url = Url;
    }

    public void setTenantId(String TenantId) {
        this.TenantId = TenantId;
    }

    public void setVmId(String VmId) {
        this.VmId = VmId;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public void setIdentityUrl(String identityUrl) {
        this.identityUrl = identityUrl;
    }

    @Override
    public String toString() {
        String time = getRequestTime() != null ? getRequestTime() : "N/A";
        // String req = request != null ? request : "N/A";
        return String.format("[%s - %s]", time, getId());
    }

    public String toOutgoing(Status status) {
        return toOutgoing(status, getMessage());
    }

    public String toOutgoing(Status status, String msg) {
        OutgoingMessage out = new OutgoingMessage(this);
        out.setResponse(status);
        out.setMessage(msg);
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
