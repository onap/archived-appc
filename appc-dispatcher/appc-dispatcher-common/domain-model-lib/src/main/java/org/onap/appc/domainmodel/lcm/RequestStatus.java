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

package org.onap.appc.domainmodel.lcm;

/**
 * This is a Enumeration class which introduces various Request Status
 */
public enum RequestStatus {
    // Unknown request status
    UNKNOWN(ExternalActionStatus.FAILED, "Status cannot be determined.",true),
    // Request just entered in APPC boundaries
    RECEIVED(ExternalActionStatus.IN_PROGRESS, "Request has been received.",false),
    // APPC has accepted the request/transaction to process after applying various business rules/validation
    ACCEPTED(ExternalActionStatus.IN_PROGRESS, "Request has been accepted and is in progress.",false),
    // APPC decided to reject the request based on various applicable business rules/validation.
    REJECTED(ExternalActionStatus.FAILED, "Request has been rejected.",true),
    // APPC has processed the VNF management request without any errors
    SUCCESSFUL(ExternalActionStatus.SUCCESSFUL, "Request has been successfully completed.",true),
    // APPC encountered error during processing the VNF management request
    FAILED(ExternalActionStatus.FAILED, "Request failed because of an error",true),
    // APPC Timed out because of reason that is out of control of APPC.
    TIMEOUT(ExternalActionStatus.FAILED, "Request failed because it timed out",true),
    // APPC aborted the request
    ABORTED(ExternalActionStatus.ABORTED, "Request was aborted",true),
    // APPC cannot find any related request
    NOT_FOUND(ExternalActionStatus.NOT_FOUND, "Request was not found",true);

    private ExternalActionStatus externalActionStatus;
    private String description;
    boolean terminal;

    RequestStatus(ExternalActionStatus externalActionStatus, String description, boolean terminal) {
        this.externalActionStatus = externalActionStatus;
        this.description = description;
        this.terminal=terminal;
    }

    public ExternalActionStatus getExternalActionStatus() {
        return externalActionStatus;
    }

    public String getExternalActionStatusName() {
        return externalActionStatus.name();
    }

    public String getDescription() {
        return description;
    }
    public boolean isTerminal() {
        return terminal;
    }
}
