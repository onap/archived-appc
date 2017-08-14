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

package org.openecomp.appc.domainmodel.lcm;

public enum VNFOperation {
    Configure, Test, HealthCheck, Start, Terminate, Restart, Rebuild, Stop, ConfigModify,
    ConfigScaleOut,ConfigRestore,Backup, Snapshot,
    SoftwareUpload, LiveUpgrade, Rollback, Test_lic, Migrate, Evacuate,StopApplication, StartApplication,
    Sync(OperationType.ReadOnly), Audit(OperationType.ReadOnly),
    ConfigBackup(OperationType.ReadOnly),ConfigBackupDelete(OperationType.ReadOnly),ConfigExport(OperationType.ReadOnly),
    Lock(OperationType.BuiltIn), Unlock(OperationType.BuiltIn), CheckLock(OperationType.BuiltIn);

    private OperationType operationType;

    VNFOperation(OperationType operationType){
        this.operationType=operationType;
    }

    VNFOperation() {
        this.operationType=OperationType.OrchestrationStatusUpdate;
    }
    /**
     * Operations handled directly by the RequestHandler without further call to DG are built-in operations.
     */
    public boolean isBuiltIn() {
        return this.operationType.equals(OperationType.BuiltIn);
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public static VNFOperation findByString(String operationName) {
        for(VNFOperation operation: VNFOperation.values()) {
            if(operation.name().equals(operationName)) {
                return operation;
            }
        }
        return null;
    }
}
