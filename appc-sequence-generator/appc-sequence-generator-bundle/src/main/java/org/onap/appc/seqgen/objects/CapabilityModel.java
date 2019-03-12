/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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
 *
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.seqgen.objects;

import java.util.List;
import java.util.Map;

public class CapabilityModel {

    private List<String> vnfCapabilities;
    private List<String> vfModuleCapabilities;
    private Map<String, List<String>> vmCapabilities;
    private List<String> vnfcCapabilities;

    public CapabilityModel() {
    }
    
    public CapabilityModel( List<String> vnfCapabilities,
                            List<String> vfModuleCapabilities,
                            Map<String, List<String>> vmCapabilities,
                            List<String> vnfcCapabilities) {

        this.vnfCapabilities = vnfCapabilities;
        this.vfModuleCapabilities = vfModuleCapabilities;
        this.vmCapabilities = vmCapabilities;
        this.vnfcCapabilities = vnfcCapabilities;
    }
    public List<String> getVnfCapabilities() {
        return vnfCapabilities;
    }
    public List<String> getVfModuleCapabilities() {
        return vfModuleCapabilities;
    }
    public Map<String,List<String>> getVmCapabilities() {
        return vmCapabilities;
    }
    public List<String> getVnfcCapabilities() {
        return vnfcCapabilities;
    }
    @Override
    public String toString() {
        return "CapabilitiesModel = " + "vnf=" + getVnfCapabilities() +
                "vfModule=" + getVfModuleCapabilities() +
                "vm=" + getVmCapabilities() +
                "vnfc=" + getVnfcCapabilities();
    }
}
