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

import com.fasterxml.jackson.annotation.JsonCreator;

/**
* The action to be taken by APP-C, e.g. Restart, Rebuild, Migrate
*
*/
public enum Action {

    Restart("Restart"),
    Rebuild("Rebuild"),
    Migrate("Migrate"),
    Evacuate("Evacuate"),
    Snapshot("Snapshot"),
    Rollback("Rollback"),
    Sync("Sync"),
    Audit("Audit"),
    Stop("Stop"),
    Start("Start"),
    Terminate("Terminate"),
    SoftwareUpload("SoftwareUpload"),
    HealthCheck("HealthCheck"),
    LiveUpgrade("LiveUpgrade"),
    Lock("Lock"),
    Unlock("Unlock"),
    Test("Test"),
    CheckLock("CheckLock"),
    Configure("Configure"),
    ConfigModify("ConfigModify"),
    ConfigScaleOut("ConfigScaleOut"),
    ConfigRestore("ConfigRestore"),
    ConfigBackup("ConfigBackup"),
    ConfigBackupDelete("ConfigBackupDelete"),
    ConfigExport("ConfigExport");

    private String value;

    Action(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static Action fromValue(String text) {
        for (Action var : Action.values()) {
            if (String.valueOf(var.value).equals(text)) {
                return var;
            }
        }
        return null;
    }

}
