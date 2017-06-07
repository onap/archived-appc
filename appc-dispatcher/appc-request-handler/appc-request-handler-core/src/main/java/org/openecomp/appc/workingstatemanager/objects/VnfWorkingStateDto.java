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

package org.openecomp.appc.workingstatemanager.objects;


public class VnfWorkingStateDto {
    private String vnfId;
    private VNFWorkingState state;
    private String ownerId;
    private long updated;
    private long ver;

    public VnfWorkingStateDto() {
    }

    public VnfWorkingStateDto(String vnfId) {
        this.vnfId = vnfId;
    }

    public String getVnfId() {
        return vnfId;
    }

    public void setVnfId(String vnfId) {
        this.vnfId = vnfId;
    }

    public VNFWorkingState getState() {
        return state;
    }

    public void setState(VNFWorkingState state) {
        this.state = state;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }

    public long getVer() {
        return ver;
    }

    public void setVer(long ver) {
        this.ver = ver;
    }


    @Override
    public String toString() {
        return "VnfWorkingStateDto{" +
                "vnfId='" + vnfId + '\'' +
                ", state=" + state +
                ", ownerId='" + ownerId + '\'' +
                ", updated=" + updated +
                ", ver=" + ver +
                '}';
    }
}
