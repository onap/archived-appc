/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications Copyright (C) 2018-2019 Orange
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
    DistributeTraffic,
    DistributeTrafficCheck,
    Evacuate,
    GetConfig,
    HealthCheck,
    LicenseManagement,
    LiveUpgrade,
    Lock(true),
    Migrate,
    PostEvacuate,
    PostMigrate,
    PostRebuild,
    Provisioning,
    PreConfigure,
    PreEvacuate,
    PreMigrate,
    PreRebuild,
    Query,
    QuiesceTraffic,
    ResumeTraffic,
    Reboot,
    Rebuild,
    Restart,
    Rollback,
    Snapshot,
    SoftwareUpload,
    Start,
    StartApplication,
    StartTraffic,
    StatusTraffic,
    Stop,
    StopApplication,
    StopTraffic,
    Sync,
    Terminate,
    Test,
    Test_lic,
    Unlock(true),
    UpgradePreCheck,
    UpgradeSoftware,
    DownloadNeSw,
    ActivateNeSw,
    UpgradePostCheck,
    UpgradeBackup,
    UpgradeBackout;

    private boolean builtIn;

    VNFOperation() {
        this.builtIn = false;
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
