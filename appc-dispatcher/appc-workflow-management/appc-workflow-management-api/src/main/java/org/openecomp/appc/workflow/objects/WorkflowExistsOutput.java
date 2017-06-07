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

package org.openecomp.appc.workflow.objects;

public class WorkflowExistsOutput {

	private boolean mappingExist ;
	private boolean dgExist;
	private String workflowModule;
	private String workflowName;
	private String workflowVersion;


	public WorkflowExistsOutput() {
	}

	public WorkflowExistsOutput(boolean mappingExist, boolean dgExist) {
		this.mappingExist = mappingExist;
		this.dgExist = dgExist;
	}

	public boolean isMappingExist() {
		return mappingExist;
	}

	public void setMappingExist(boolean mappingExist) {
		this.mappingExist = mappingExist;
	}

	public boolean isDgExist() {
		return dgExist;
	}

	public void setDgExist(boolean dgExist) {
		this.dgExist = dgExist;
	}

	public String getWorkflowName() {
		return workflowName;
	}

	public void setWorkflowName(String workflowName) {
		this.workflowName = workflowName;
	}

	public String getWorkflowVersion() {
		return workflowVersion;
	}

	public void setWorkflowVersion(String workflowVersion) {
		this.workflowVersion = workflowVersion;
	}

	public String getWorkflowModule() {
		return workflowModule;
	}

	public void setWorkflowModule(String workflowModule) {
		this.workflowModule = workflowModule;
	}
	public boolean exists(){
		return mappingExist && dgExist;
	}

	@Override
	public String toString() {
		return "WorkflowExistsOutput{" +
				"mappingExist=" + mappingExist +
				", dgExist=" + dgExist +
				", workflowModule='" + workflowModule + '\'' +
				", workflowName='" + workflowName + '\'' +
				", workflowVersion='" + workflowVersion + '\'' +
				'}';
	}
}
