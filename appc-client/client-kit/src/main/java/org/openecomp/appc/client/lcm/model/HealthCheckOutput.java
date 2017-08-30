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

@javax.annotation.Generated(
    value = {"templates/client-kit/open-api-to-java.ftl"},
    date = "2017-05-04T20:09:01.596+05:30",
    comments = "Auto-generated from Open API specification")
public class HealthCheckOutput {

    @JsonProperty("common-header")
    private CommonHeader commonHeader;

    @JsonProperty("status")
    private Status status;

    /**
     * A common header for all APP-C requests
     */
    public CommonHeader getCommonHeader() {
        return commonHeader;
    }

    /**
     * A common header for all APP-C requests
     */
    public void setCommonHeader(CommonHeader commonHeader) {
        this.commonHeader = commonHeader;
    }

    /**
     * The specific response codes are to be aligned with ASDC reference doc (main table removed to avoid duplication and digression from main table). See ASDC and ECOMP Distribution Consumer Interface Agreement
     */
    public Status getStatus() {
        return status;
    }

    /**
     * The specific response codes are to be aligned with ASDC reference doc (main table removed to avoid duplication and digression from main table). See ASDC and ECOMP Distribution Consumer Interface Agreement
     */
    public void setStatus(Status status) {
        this.status = status;
    }

}
