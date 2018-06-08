/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.aai.data;

import java.util.List;

public class AaiVnfInfo {

    private String vnfId;
    private String vnfName;
    private String vnfOamIpAddress;
    private List<AaiVmInfo> vmInfo;

    public String getVnfId() {
        return vnfId;
    }

    public void setVnfId(String vnfId) {
        this.vnfId = vnfId;
    }

    public String getVnfName() {
        return vnfName;
    }

    public void setVnfName(String vnfName) {
        this.vnfName = vnfName;
    }

    public String getVnfOamIpAddress() {
        return vnfOamIpAddress;
    }

    public void setVnfOamIpAddress(String vnfOamIpAddress) {
        this.vnfOamIpAddress = vnfOamIpAddress;
    }

    public List<AaiVmInfo> getVmInfo() {
        return vmInfo;
    }

    public void setVmInfo(List<AaiVmInfo> vmInfo) {
        this.vmInfo = vmInfo;
    }
}
