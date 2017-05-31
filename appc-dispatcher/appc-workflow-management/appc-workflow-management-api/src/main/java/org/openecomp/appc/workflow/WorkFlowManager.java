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

package org.openecomp.appc.workflow;

import org.openecomp.appc.workflow.objects.WorkflowExistsOutput;
import org.openecomp.appc.workflow.objects.WorkflowRequest;
import org.openecomp.appc.workflow.objects.WorkflowResponse;

public interface WorkFlowManager {
	/**
	 * Execute workflow and return response.
	 * This method execute workflow with following steps.
	 * Retrieve workflow(DG) details - module, version and mode  from database based on command and vnf Type from incoming request.
	 * Execute workflow (DG) using SVC Logic Service reference
	 * Return response of workflow (DG) to caller.
	 * @param workflowRequest workflow execution request which contains vnfType, command, requestId, targetId, payload and (optional) confID;
	 * @return Workflow Response which contains execution status and payload from DG if any
	 */
	WorkflowResponse  executeWorkflow(WorkflowRequest workflowRequest);

	/**
	 * Check if workflow (DG) exists in database
	 * @param workflowQueryParams workflow request with command and vnf Type
	 * @return WorkflowExistsOutput.mappingExist True if workflow mapping exists else False. WorkflowExistsOutput.dgExist True if DG workflow exists else False.
     */
	WorkflowExistsOutput workflowExists(WorkflowRequest workflowQueryParams);

}
