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

public class AaiVnfcInfo {

    private String vnfcId;
    private String vnfcName;
    private String vnfcFunctionCode;
    private String vnfcOamIpAddress;

    public String getVnfcId() {
        return vnfcId;
    }

    public void setVnfcId(String vnfcId) {
        this.vnfcId = vnfcId;
    }

    public String getVnfcName() {
        return vnfcName;
    }

    public void setVnfcName(String vnfcName) {
        this.vnfcName = vnfcName;
    }

    public String getVnfcFunctionCode() {
        return vnfcFunctionCode;
    }

    public void setVnfcFunctionCode(String vnfcFunctionCode) {
        this.vnfcFunctionCode = vnfcFunctionCode;
    }

    public String getVnfcOamIpAddress() {
        return vnfcOamIpAddress;
    }

    public void setVnfcOamIpAddress(String vnfcOamIpAddress) {
        this.vnfcOamIpAddress = vnfcOamIpAddress;
    }
}
