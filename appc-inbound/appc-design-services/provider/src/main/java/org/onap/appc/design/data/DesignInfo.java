/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.design.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DesignInfo{

    @JsonProperty("vnf-type")
    String vnfType;

    @JsonProperty("vnfc-type")
    String vnfcType;

    @JsonProperty("protocol")
    String protocol;

    @JsonProperty("incart")
    String inCart;

    @JsonProperty("action")
    String action;

    @JsonProperty("artifact-name")
    String artifact_name;

    @JsonProperty("artifact-type")
    String artifact_type;

    @JsonProperty("permission")
    String permission;

    @JsonProperty("creatorUserId")
    String creatorUserId;

    public String getArtifact_type() {
        return artifact_type;
    }

    public void setArtifact_type(String artifact_type) {
        this.artifact_type = artifact_type;
    }

    public String getArtifact_name() {
        return artifact_name;
    }

    public void setArtifact_name(String artifact_name) {
        this.artifact_name = artifact_name;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getVnf_type() {
        return vnfType;
    }

    public void setvnfType(String vnfType) {
        this.vnfType = vnfType;
    }

    public String getVnfc_type() {
        return vnfcType;
    }

    public void setVnfc_type(String vnfcType) {
        this.vnfcType = vnfcType;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getInCart() {
        return inCart;
    }

    public void setInCart(String inCart) {
        this.inCart = inCart;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public String getCreatorUserId() {
        return creatorUserId;
    }

    public void setCreatorUserId(String creatorUserId) {
        this.creatorUserId = creatorUserId;
    }

    @Override
    public String toString() {
        return "DesignInfo [vnfType=" + vnfType + ", vnfcType=" + vnfcType + ", protocol=" + protocol + ", inCart="
                + inCart + "]";
    }

}
