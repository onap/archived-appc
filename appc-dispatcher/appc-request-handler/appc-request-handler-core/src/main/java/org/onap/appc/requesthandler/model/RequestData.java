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

package org.onap.appc.requesthandler.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestData {

    @JsonProperty("vnf-id")
    private String vnfID;
    
    @JsonProperty("current-request")
    private RequestModel currentRequest;

    @JsonProperty("in-progress-requests")
    private List<RequestModel> inProgressRequests;
    
    public String getVnfID() {
        return vnfID;
    }

    public void setVnfID(String vnfID) {
        this.vnfID = vnfID;
    }

    public RequestModel getCurrentRequest() {
        return currentRequest;
    }

    public void setCurrentRequest(RequestModel currentRequest) {
        this.currentRequest = currentRequest;
    }

    public List<RequestModel> getInProgressRequests() {
        return inProgressRequests;
    }

    public void setInProgressRequests(List<RequestModel> inProgressRequests) {
        this.inProgressRequests = inProgressRequests;
    }
    
}
