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

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DesignResponse{

    @JsonProperty("userID")
    String userId;

    @JsonProperty("designInfo")
    List<DesignInfo> designInfoList;

    @JsonProperty("statusInfo")
    List<StatusInfo> statusInfoList;

    @JsonProperty("artifactInfo")
    List<ArtifactInfo> artifactInfo;

    @JsonProperty("vnf-type")
    String vnfType;

    @JsonProperty("users")
    List<UserPermissionInfo> users;

    public List<ArtifactInfo> getArtifactInfo() {
        return artifactInfo;
    }

    public void setArtifactInfo(List<ArtifactInfo> artifactInfo) {
        this.artifactInfo = artifactInfo;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<DesignInfo> getDesignInfoList() {
        return designInfoList;
    }

    public void setDesignInfoList(List<DesignInfo> designInfoList) {
        this.designInfoList = designInfoList;
    }

    public List<StatusInfo> getStatusInfoList() {
        return statusInfoList;
    }

    public void setStatusInfoList(List<StatusInfo> statusInfoList) {
        this.statusInfoList = statusInfoList;
    }

    public String getVnfType() {
        return vnfType;
    }

    public void setVnfType(String vnfType) {
        this.vnfType = vnfType;
    }

    public List<UserPermissionInfo> getUsers() {
        return users;
    }

    public void setUsers(List<UserPermissionInfo> userPermInfoList) {
        this.users = userPermInfoList;
    }

}
