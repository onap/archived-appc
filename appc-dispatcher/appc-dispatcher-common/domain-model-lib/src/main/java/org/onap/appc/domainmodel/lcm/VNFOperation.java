/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Copyright (C) 2018 Orange
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

public enum VNFOperation {
    ActionStatus,
    AttachVolume,
    Audit,
    Backup,
    CheckLock(true),
    Configure,
    ConfigBackup,
    ConfigBackupDelete,
    ConfigExport,
    ConfigModify,
    ConfigRestore,
    ConfigScaleOut,
    DetachVolume,
    Evacuate,
    HealthCheck,
    LiveUpgrade,
    Lock(true),
    Migrate,
    Query,
    QuiesceTraffic,
    ResumeTraffic,
    DistributeTraffic,
    Reboot,
    Rebuild,
    Restart,
    Rollback,
    Snapshot,
    SoftwareUpload,
    Start,
    StartApplication,
    Stop,
    StopApplication,
    Sync,
    Terminate,
    Test,
    Test_lic,
    Unlock(true),
    UpgradePreCheck,
    UpgradeSoftware,
    UpgradePostCheck,
    UpgradeBackup,
    UpgradeBackout;

    private boolean builtIn;

    VNFOperation() {
        this.builtIn=false;
    }

    /**
     * Operations handled directly by the RequestHandler without further call to DG are built-in operations.
     */
    public boolean isBuiltIn() {
        return builtIn;
    }

    VNFOperation(boolean builtIn) {
        this.builtIn = builtIn;
    }

    public static VNFOperation findByString(String operationName) {
        for (VNFOperation operation : VNFOperation.values()) {
            if (operation.name().equals(operationName)) {
                return operation;
            }
        }
        return null;
    }
}
