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

package org.openecomp.appc.domainmodel.lcm;

public enum VNFOperation {

	Configure, Test, HealthCheck, Start, Terminate, Restart, Rebuild, Stop, ConfigModify,
	ConfigScaleOut,ConfigRestore,Backup, Snapshot,
	SoftwareUpload, LiveUpgrade, Rollback, Sync, Audit, Test_lic, Migrate, Evacuate,ConfigBackup,ConfigBackupDelete,ConfigExport,
	Lock(true), Unlock(true), CheckLock(true);

	private boolean builtIn;

	VNFOperation(boolean builtIn) {
		this.builtIn = builtIn;
	}

	VNFOperation() {
		this(false);
	}

	/**
	 * Operations handled directly by the RequestHandler without further call to DG are built-in operations.
	 */
	public boolean isBuiltIn() {
		return builtIn;
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
