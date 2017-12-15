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

import com.fasterxml.jackson.annotation.JsonCreator;

/**
* The status of the requested LCM action
*
*/
public enum LcmActionStatus {

    IN_PROGRESS("IN_PROGRESS"),
    SUCCESSFUL("SUCCESSFUL"),
    FAILED("FAILED"),
    NOT_FOUND("NOT_FOUND"),
    ABORTED("ABORTED"),
    MULTIPLE_REQUESTS_FOUND("MULTIPLE_REQUESTS_FOUND");

    private String value;

    LcmActionStatus(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static LcmActionStatus fromValue(String text) {
        for (LcmActionStatus var : LcmActionStatus.values()) {
            if (String.valueOf(var.value).equals(text)) {
                return var;
            }
        }
        return null;
    }

}
