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
package org.openecomp.appc.client.lcm.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A block containing the action arguments. These are used to specify the object upon which APP-C LCM command is to operate
 */
@javax.annotation.Generated(
    value = {"templates/client-kit/open-api-to-java.ftl"},
    date = "2017-05-04T20:09:01.501+05:30",
    comments = "Auto-generated from Open API specification")
public class ActionIdentifiers {

    @JsonProperty("service-instance-id")
    private String serviceInstanceId;

    @JsonProperty("vnf-id")
    private String vnfId;

    @JsonProperty("vnfc-name")
    private String vnfcName;

    @JsonProperty("vserver-id")
    private String vserverId;

    /**
     * identifies a specific service the command refers to. When multiple APP-C instances are used and applied to a subset of services, this will become significant . The field is mandatory when the vnf-id is empty
     */
    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    /**
     * identifies a specific service the command refers to. When multiple APP-C instances are used and applied to a subset of services, this will become significant . The field is mandatory when the vnf-id is empty
     */
    public void setServiceInstanceId(String serviceInstanceId) {
        this.serviceInstanceId = serviceInstanceId;
    }

    /**
     * identifies the VNF to which this action is to be applied(vnf-id uniquely identifies the service-instance referred to). Note that some actions are applied to multiple VNFs in the same service. When this is the case, vnf-id may be left out, but service-instance-id must appear. The field is mandatory when service-instance-id is empty
     */
    public String getVnfId() {
        return vnfId;
    }

    /**
     * identifies the VNF to which this action is to be applied(vnf-id uniquely identifies the service-instance referred to). Note that some actions are applied to multiple VNFs in the same service. When this is the case, vnf-id may be left out, but service-instance-id must appear. The field is mandatory when service-instance-id is empty
     */
    public void setVnfId(String vnfId) {
        this.vnfId = vnfId;
    }

    /**
     * identifies the VNFC to which this action is to be applied. Some actions apply only to a component within a VNF (e.g. RESTART is sometimes applied to on VM only). In such a case, the name of the VNFC is used to search for the component within the VNF
     */
    public String getVnfcName() {
        return vnfcName;
    }

    /**
     * identifies the VNFC to which this action is to be applied. Some actions apply only to a component within a VNF (e.g. RESTART is sometimes applied to on VM only). In such a case, the name of the VNFC is used to search for the component within the VNF
     */
    public void setVnfcName(String vnfcName) {
        this.vnfcName = vnfcName;
    }

    /**
     * identifies a specific VM within the given service/vnf to which this action is to be applied
     */
    public String getVserverId() {
        return vserverId;
    }

    /**
     * identifies a specific VM within the given service/vnf to which this action is to be applied
     */
    public void setVserverId(String vserverId) {
        this.vserverId = vserverId;
    }

}
