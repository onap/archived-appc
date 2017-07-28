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

package org.openecomp.appc.executor.impl;


import org.openecomp.appc.domainmodel.lcm.ActionLevel;
import org.openecomp.appc.domainmodel.lcm.RuntimeContext;
import org.openecomp.appc.domainmodel.lcm.VNFOperation;
import org.openecomp.appc.lifecyclemanager.LifecycleManager;
import org.openecomp.appc.requesthandler.RequestHandler;
import org.openecomp.appc.workflow.WorkFlowManager;




public class CommandTaskFactory {

//    private LCMCommandTask lcmCommandTask;
//    private LCMReadonlyCommandTask LCMReadonlyCommandTask;

    private RequestHandler vnfRequestHandler;
    private RequestHandler vmRequestHandler;
    private WorkFlowManager workflowManager;
    private LifecycleManager lifecyclemanager;


    public void setWorkflowManager(WorkFlowManager workflowManager) {
        this.workflowManager = workflowManager;
    }

    public void setVnfRequestHandler(RequestHandler vnfRequestHandler) {
        this.vnfRequestHandler = vnfRequestHandler;
    }

    public void setVmRequestHandler(RequestHandler vmRequestHandler) {
        this.vmRequestHandler = vmRequestHandler;
    }

    public void setLifecyclemanager(LifecycleManager lifecyclemanager) {
        this.lifecyclemanager = lifecyclemanager;
    }


    public synchronized CommandTask getExecutionTask(RuntimeContext runtimeContext){
        String action = runtimeContext.getRequestContext().getAction().name();
        ActionLevel actionLevel = runtimeContext.getRequestContext().getActionLevel();
        RequestHandler requestHandler = readRequestHandler(actionLevel);
        if(ActionLevel.VM.equals(actionLevel)){
            return new LCMReadonlyCommandTask(runtimeContext,requestHandler,workflowManager);
        }
        if (VNFOperation.Sync.toString().equals(action) ||
                VNFOperation.Audit.toString().equals(action) ||
                VNFOperation.ConfigBackup.toString().equals(action) ||
                VNFOperation.ConfigBackupDelete.toString().equals(action) ||
                VNFOperation.ConfigExport.toString().equals(action)){
            return new LCMReadonlyCommandTask(runtimeContext,requestHandler,workflowManager);
        }else {
            return new LCMCommandTask(runtimeContext,requestHandler,workflowManager,
                    lifecyclemanager);
        }
    }

    private RequestHandler readRequestHandler(ActionLevel actionLevel) {
        if (ActionLevel.VM.equals(actionLevel)) {
            return vmRequestHandler;
        }
        return vnfRequestHandler;
    }

}
