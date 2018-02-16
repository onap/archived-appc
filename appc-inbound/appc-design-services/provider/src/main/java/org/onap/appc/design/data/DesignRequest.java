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

package org.onap.appc.design.data;


import com.fasterxml.jackson.annotation.JsonProperty;

public class DesignRequest {


    @JsonProperty("userID")
    String userId;

    @JsonProperty("vnf-type")
    String vnfType;

    @JsonProperty("vnfc-type")
    String vnfcType;

    @JsonProperty("protocol")
    String protocol;

    @JsonProperty("action")
    String action;

    @JsonProperty("artifact-name")
    String artifactName;

    @JsonProperty("artifact-contents")
    String artifactContents;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getVnfType() {
        return vnfType;
    }

    public void setVnfType(String vnfType) {
        this.vnfType = vnfType;
    }

    public String getVnfcType() {
        return vnfcType;
    }

    public void setVnfcType(String vnfcType) {
        this.vnfcType = vnfcType;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getArtifactName() {
        return artifactName;
    }

    public void setArtifactName(String artifactName) {
        this.artifactName = artifactName;
    }

    public String getArtifactContents() {
        return artifactContents;
    }

    public void setArtifactContents(String artifactContents) {
        this.artifactContents = artifactContents;
    }

    @Override
    public String toString() {
        return "DesignRequest [userId=" + userId + ", vnfType=" + vnfType + ", vnfcType=" + vnfcType + ", protocol="
            + protocol + ", action=" + action + ", artifactName=" + artifactName + ", artifactContents="
            + artifactContents + "]";
    }

}
