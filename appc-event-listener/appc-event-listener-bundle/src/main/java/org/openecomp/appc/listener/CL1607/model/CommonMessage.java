/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.appc.listener.CL1607.model;

import java.io.Serializable;
import java.util.Collection;

import org.json.JSONObject;
import org.openecomp.appc.listener.util.Mapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize.Inclusion;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * This class holds attributes that are common to DMaaP messages both coming in from DCAE and being sent out by APPC
 *
 */
@JsonSerialize(include = Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommonMessage implements Serializable {
    /*
     * { "CommonHeader": { "TimeStamp": "0000-00-00T00:00:00.000Z", "APIver": "1.01", "OriginatorID": "policy.pdp01",
     * "RequestID": "b74d13c5-bb26-4b04-992c-4679dfc8280e", "SubrequestID": "1" }, "Action": "RESTART", "Payload": {
     * "AICVServerSelfLink":
     * "http://192.168.1.2:8774/v2/abcde12345fghijk6789lmnopq123rst/servers/abc12345-1234-5678-890a-abcdefg12345",
     * "VNF_NAME": "test", "VMID": "abc12345-1234-5678-890a-abcdefg12345", "TenantID":
     * "abcde12345fghijk6789lmnopq123rst", "LOC_ID": "Test", "in-maint": "false", "Identity":
     * "http://example.com:5000/v2.0", "Prov_status": "ACTIVE", "OAM_IPV4": "192.168.1.2",
     * "is-closed-loop-disabled": "false", "VM_NAME": "basx0001vm034", "OAM_IPV6": "aaaa::bbbb:cccc:dddd:eeee/64" } }
     */

    private static final long serialVersionUID = 1L;

    /*
     * The common header
     */
    @JsonProperty("CommonHeader")
    private CommonHeader header;

    /*
     * The payload
     */
    @JsonProperty("Payload")    
    private Payload payload;

    @JsonIgnore
    private long startTime = System.currentTimeMillis();

    /*
     * Getters and Setters
     */

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * @return the header
     */
    public CommonHeader getHeader() {
        return header;
    }

    /**
     * @param header
     *            the header to set
     */
    public void setHeader(CommonHeader header) {
        this.header = header;
    }

    /**
     * @return the payload
     */
    public Payload getPayload() {
        return payload;
    }

    /**
     * @param payload
     *            the payload to set
     */
    public void setPayload(Payload payload) {
        this.payload = payload;
    }

    /**
     * Convenience method to return a json representation of this object.
     * 
     * @return The json representation of this object
     */
    public JSONObject toJson() {
        return Mapper.toJsonObject(this);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CommonHeader {
        /*
         * "CommonHeader": { "TimeStamp": "2016-05-11T13:53:53.146Z", "APIver": "1.01", "OriginatorID": "policy.pdp01",
         * "RequestID": "b74d13c5-bb26-4b04-992c-4679dfc8280e", "SubrequestID": "1" }
         */

        /*
         * The timestamp of the message
         */
        @JsonProperty("TimeStamp")
        private String timeStamp;

        /*
         * The API version of the message
         */
        @JsonProperty("APIver")
        private String apiVer;

        /*
         * The Originator ID of the message
         */
        @JsonProperty("OriginatorID")
        private String originatorId;

        /*
         * The Request Id of the message
         */
        @JsonProperty("RequestID")
        private String requestID;

        /*
         * The Subrequest Id of the message
         */
        @JsonProperty("SubRequestID")
        private String subRequestId;

        /**
         * @return the timeStamp
         */
        public String getTimeStamp() {
            return timeStamp;
        }

        /**
         * @param timeStamp
         *            the timeStamp to set
         */
        public void setTimeStamp(String timeStamp) {
            this.timeStamp = timeStamp;
        }

        /**
         * @return the apiVer
         */
        public String getApiVer() {
            return apiVer;
        }

        /**
         * @param apiVer
         *            the apiVer to set
         */
        public void setApiVer(String apiVer) {
            this.apiVer = apiVer;
        }

        /**
         * @return the originatorId
         */
        public String getOriginatorId() {
            return originatorId;
        }

        /**
         * @param originatorId
         *            the originatorId to set
         */
        public void setOriginatorId(String originatorId) {
            this.originatorId = originatorId;
        }

        /**
         * @return the requestID
         */
        public String getRequestID() {
            return requestID;
        }

        /**
         * @param requestID
         *            the requestID to set
         */
        public void setRequestID(String requestID) {
            this.requestID = requestID;
        }

        /**
         * @return the subRequestId
         */
        public String getSubRequestId() {
            return subRequestId;
        }

        /**
         * @param subRequestId
         *            the subRequestId to set
         */
        public void setSubRequestId(String subRequestId) {
            this.subRequestId = subRequestId;
        }
    };

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Payload {
        /*
         * "Payload": { "AICVServerSelfLink":
         * "http://192.168.1.2:8774/v2/abcde12345fghijk6789lmnopq123rst/servers/abc12345-1234-5678-890a-abcdefg12345",
         * "VNF_NAME": "test", "VMID": "abc12345-1234-5678-890a-abcdefg12345", "TenantID":
         * "abcde12345fghijk6789lmnopq123rst", "LOC_ID": "Test", "in-maint": "false", "Identity":
         * "http://example.com:5000/v2.0", "Prov_status": "ACTIVE", "OAM_IPV4": "192.168.1.2",
         * "is-closed-loop-disabled": "false", "VM_NAME": "test", "OAM_IPV6": "aaaa::bbbb:cccc:dddd:eeee/64" }
         */

        /*
         * The TenantID of the message
         */
        @JsonProperty("AICVServerSelfLink")
        private String aicvServerSelfLink;

        /*
         * The VNF_NAME of the message
         */
        @JsonProperty("VNF_NAME")
        private String vnfName;

        /*
         * The AICVMID of the message
         */
        @JsonProperty("AICVMID")
        private String aicVmId;

        /*
         * The AICTenantID of the message
         */
        @JsonProperty("AICTenantID")
        private String aicTenantID;

        /*
         * The LOC_ID of the message
         */
        @JsonProperty("LOC_ID")
        private String locId;

        /*
         * The in-maint of the message
         */
        @JsonProperty("in-maint")
        private String inMaint;

        /*
         * The AICIdentity of the message
         */
        @JsonProperty("AICIdentity")
        private String aicIdentity;

        /*
         * The AICIdentity of the message
         */
        @JsonProperty("Prov_status")
        private String provStatus;

        /*
         * The OAM_IPV4 of the message
         */
        @JsonProperty("OAM_IPV4")
        private String oamIPV4;

        /*
         * The is-closed-loop-disabled of the message
         */
        @JsonProperty("is-closed-loop-disabled")
        private String closedLoopDisabled;

        /*
         * The VM_NAME of the message
         */
        @JsonProperty("VM_NAME")
        private String vmName;

        /*
         * The OAM_IPV6 of the message
         */
        @JsonProperty("OAM_IPV6")
        private String oamIPV6;

        /**
         * @return the aicvServerSelfLink
         */
        public String getAicvServerSelfLink() {
            return aicvServerSelfLink;
        }

        /**
         * @param aicvServerSelfLink
         *            the aicvServerSelfLink to set
         */
        public void setAicvServerSelfLink(String aicvServerSelfLink) {
            this.aicvServerSelfLink = aicvServerSelfLink;
        }

        /**
         * @return the vnfName
         */
        public String getVnfName() {
            return vnfName;
        }

        /**
         * @param vnfName
         *            the vnfName to set
         */
        public void setVnfName(String vnfName) {
            this.vnfName = vnfName;
        }

        /**
         * @return the aicVmId
         */
        public String getAicVmId() {
            return aicVmId;
        }

        /**
         * @param aicVmId
         *            the aicVmId to set
         */
        public void setAicVmId(String aicVmId) {
            this.aicVmId = aicVmId;
        }

        /**
         * @return the aicTenantID
         */
        public String getAicTenantID() {
            return aicTenantID;
        }

        /**
         * @param aicTenantID
         *            the aicTenantID to set
         */
        public void setAicTenantID(String aicTenantID) {
            this.aicTenantID = aicTenantID;
        }

        /**
         * @return the locId
         */
        public String getLocId() {
            return locId;
        }

        /**
         * @param locId
         *            the locId to set
         */
        public void setLocId(String locId) {
            this.locId = locId;
        }

        /**
         * @return the inMaint
         */
        public String getInMaint() {
            return inMaint;
        }

        /**
         * @param inMaint
         *            the inMaint to set
         */
        public void setInMaint(String inMaint) {
            this.inMaint = inMaint;
        }

        /**
         * @return the aicIdentity
         */
        public String getAicIdentity() {
            return aicIdentity;
        }

        /**
         * @param aicIdentity
         *            the aicIdentity to set
         */
        public void setAicIdentity(String aicIdentity) {
            this.aicIdentity = aicIdentity;
        }

        /**
         * @return the provStatus
         */
        public String getProvStatus() {
            return provStatus;
        }

        /**
         * @param provStatus
         *            the provStatus to set
         */
        public void setProvStatus(String provStatus) {
            this.provStatus = provStatus;
        }

        /**
         * @return the oamIPV4
         */
        public String getOamIPV4() {
            return oamIPV4;
        }

        /**
         * @param oamIPV4
         *            the oamIPV4 to set
         */
        public void setOamIPV4(String oamIPV4) {
            this.oamIPV4 = oamIPV4;
        }

        /**
         * @return the closedLoopDisabled
         */
        public String getClosedLoopDisabled() {
            return closedLoopDisabled;
        }

        /**
         * @param closedLoopDisabled
         *            the closedLoopDisabled to set
         */
        public void setClosedLoopDisabled(String closedLoopDisabled) {
            this.closedLoopDisabled = closedLoopDisabled;
        }

        /**
         * @return the vmNAME
         */
        public String getVmName() {
            return vmName;
        }

        /**
         * @param vmNAME
         *            the vmNAME to set
         */
        public void setVmName(String vmName) {
            this.vmName = vmName;
        }

        /**
         * @return the oamIPV6
         */
        public String getOamIPV6() {
            return oamIPV6;
        }

        /**
         * @param oamIPV6
         *            the oamIPV6 to set
         */
        public void setOamIPV6(String oamIPV6) {
            this.oamIPV6 = oamIPV6;
        }

    };

}
