/*-
 * ============LICENSE_START=======================================================
 * openECOMP : APP-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 						reserved.
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
 */

package org.openecomp.appc.executor.impl;


import org.openecomp.appc.domainmodel.lcm.VNFOperation;
import org.openecomp.appc.lifecyclemanager.LifecycleManager;
import org.openecomp.appc.requesthandler.RequestHandler;
import org.openecomp.appc.workflow.WorkFlowManager;




public class CommandTaskFactory {
    private RequestHandler requestHandler;
    private WorkFlowManager workflowManager;
    private LifecycleManager lifecyclemanager;


    public void setWorkflowManager(WorkFlowManager workflowManager) {
        this.workflowManager = workflowManager;
    }

    public void setRequestHandler(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    public void setLifecyclemanager(LifecycleManager lifecyclemanager) {
        this.lifecyclemanager = lifecyclemanager;
    }


    public synchronized CommandTask getExecutionTask(String action){
        if (VNFOperation.Sync.toString().equals(action) || VNFOperation.Audit.toString().equals(action)){
            return new LCMReadonlyCommandTask(requestHandler,workflowManager);
        }else {
            return new LCMCommandTask(requestHandler,workflowManager,
                    lifecyclemanager);
        }
    }

}
