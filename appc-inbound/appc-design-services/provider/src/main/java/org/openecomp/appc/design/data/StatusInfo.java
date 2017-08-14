/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * 
 *  ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.appc.design.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StatusInfo{
	
	@JsonProperty("vnf-type")
	String vnf_type;
	
	@JsonProperty("vnfc-type")
	String vnfc_type;
	
	@JsonProperty("action")
	String action;
	
	@JsonProperty("artifact-status")
	String artifact_status;

	@JsonProperty("action-status")
	String action_status;
	
	public String getVnf_type() {
		return vnf_type;
	}

	public void setVnf_type(String vnf_type) {
		this.vnf_type = vnf_type;
	}

	public String getVnfc_type() {
		return vnfc_type;
	}

	public void setVnfc_type(String vnfc_type) {
		this.vnfc_type = vnfc_type;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getArtifact_status() {
		return artifact_status;
	}

	public void setArtifact_status(String artifact_status) {
		this.artifact_status = artifact_status;
	}

	public String getAction_status() {
		return action_status;
	}

	public void setAction_status(String action_status) {
		this.action_status = action_status;
	}

	


}
