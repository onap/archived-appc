/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.flow.controller.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ActionIdentifier {

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((vnfId == null) ? 0 : vnfId.hashCode());
        result = prime * result + ((vnfcName == null) ? 0 : vnfcName.hashCode());
        result = prime * result + ((vserverId == null) ? 0 : vserverId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ActionIdentifier other = (ActionIdentifier) obj;
        if (vnfId == null) {
            if (other.vnfId != null)
                return false;
        } else if (!vnfId.equals(other.vnfId))
            return false;
        if (vnfcName == null) {
            if (other.vnfcName != null)
                return false;
        } else if (!vnfcName.equals(other.vnfcName))
            return false;
        if (vserverId == null) {
            if (other.vserverId != null)
                return false;
        } else if (!vserverId.equals(other.vserverId))
            return false;
        return true;
    }

    @JsonProperty("vnf-id")
    private String vnfId ;
    @JsonProperty("vserver-id")
    private String vserverId;
    @JsonProperty("vnfc-name")
    private String vnfcName;

    public String getVserverId() {
        return vserverId;
    }

    public void setVserverId(String vserverId) {
        this.vserverId = vserverId;
    }

    public String getVnfcName() {
        return vnfcName;
    }

    public void setVnfcName(String vnfcName) {
        this.vnfcName = vnfcName;
    }

    public String getVnfId() {
        return vnfId;
    }

    public void setVnfId(String vnfId) {
        this.vnfId = vnfId;
    }

    @Override
    public String toString() {
        return "ActionIdentifier [vnfId=" + vnfId + ", vserverId=" + vserverId + ", vnfcName=" + vnfcName + "]";
    }

}
