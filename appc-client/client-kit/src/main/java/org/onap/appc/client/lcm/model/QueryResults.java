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

/**
 * NOTE: This file is auto-generated and should not be changed manually.
 */
package org.onap.appc.client.lcm.model;

import com.fasterxml.jackson.annotation.JsonProperty;

@javax.annotation.Generated(
    value = {"client-kit/open-api-to-java.ftl"},
    date = "2017-11-16T17:10:10.326Z",
    comments = "Auto-generated from Open API specification")
public class QueryResults {

    @JsonProperty("vserver-id")
    private String vserverId;

    @JsonProperty("vm-state")
    private VmState vmState;

    @JsonProperty("vm-status")
    private VmStatus vmStatus;

    /**
     * Identifier of a VM
     */
    public String getVserverId() {
        return vserverId;
    }

    /**
     * Identifier of a VM
     */
    public void setVserverId(String vserverId) {
        this.vserverId = vserverId;
    }

    /**
     * The state of a VM
     */
    public VmState getVmState() {
        return vmState;
    }

    /**
     * The state of a VM
     */
    public void setVmState(VmState vmState) {
        this.vmState = vmState;
    }

    /**
     * The status of a VM
     */
    public VmStatus getVmStatus() {
        return vmStatus;
    }

    /**
     * The status of a VM
     */
    public void setVmStatus(VmStatus vmStatus) {
        this.vmStatus = vmStatus;
    }

}
