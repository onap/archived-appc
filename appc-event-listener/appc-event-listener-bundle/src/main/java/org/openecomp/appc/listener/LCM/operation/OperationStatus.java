/*-
 * ============LICENSE_START=======================================================
 * APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Amdocs
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
 * ============LICENSE_END=========================================================
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */

package org.openecomp.appc.listener.LCM.operation;


public class OperationStatus {
    public final static OperationStatus PENDING = new OperationStatus("PENDING", "PENDING");
    public final static OperationStatus ACTIVE = new OperationStatus("ACTIVE", "ACTIVE");
    public final static OperationStatus SUCCESS = new OperationStatus("SUCCESS", "SUCCESS");
    public final static OperationStatus FAILURE = new OperationStatus("FAILURE", "FAILURE");

    private String code;
    private String value;

    public OperationStatus() {
    }


    public OperationStatus(String code, String value) {
        this.code = code;
        this.value = value;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isSucceeded() {
        if (code == null) {
            return false;
        }
        int intCode = Integer.parseInt(code);
        return (intCode >= 200) && (intCode < 300); // All 2xx statuses are success
    }
}
