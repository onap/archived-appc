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

package org.onap.appc.requesthandler.exceptions;


import com.att.eelf.i18n.EELFResourceManager;
import org.onap.appc.executor.objects.LCMCommandStatus;
import org.onap.appc.executor.objects.Params;
import org.onap.appc.i18n.Msg;
import org.onap.appc.logging.LoggingConstants;

public class DGWorkflowNotFoundException extends RequestValidationException {
    public final String workflowModule;
    public final String workflowName;
    public final String workflowVersion;
    public DGWorkflowNotFoundException(String message,String workflowModule,String workflowName,String workflowVersion,String vnfType,String action){
        super(message);
        this.workflowModule = workflowModule;
        this.workflowName = workflowName;
        this.workflowVersion = workflowVersion;
        super.setLcmCommandStatus(LCMCommandStatus.DG_WORKFLOW_NOT_FOUND);
        super.setParams(new Params().addParam("actionName", action)
                .addParam("dgModule", workflowModule).addParam("dgName", workflowName).addParam("dgVersion", workflowVersion));
        super.setLogMessage(EELFResourceManager.format(Msg.APPC_WORKFLOW_NOT_FOUND, vnfType, action));
        super.setTargetEntity(LoggingConstants.TargetNames.APPC);
        super.setTargetService(LoggingConstants.TargetNames.WORKFLOW_MANAGER);
    }
}
