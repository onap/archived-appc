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

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.sql.rowset.CachedRowSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.openecomp.appc.design.services.util.DesignServiceConstants;
import org.openecomp.sdnc.sli.resource.dblib.DBResourceManager;
import org.openecomp.sdnc.sli.resource.dblib.DbLibService;

public class DesignRequest{


	@JsonProperty("userID")
	String userId ;

	@JsonProperty("vnf-type")
	String vnf_type;
	
	@JsonProperty("vnfc-type")
	String vnfc_type;
	
	@JsonProperty("protocol")
	String protocol;
	
	@JsonProperty("action")
	String action;
	
	@JsonProperty("artifact-name")
	String artifact_name;
	
	@JsonProperty("artifact-contents")
	String artifact_contents ;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

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

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getArtifact_name() {
		return artifact_name;
	}

	public void setArtifact_name(String artifact_name) {
		this.artifact_name = artifact_name;
	}

	public String getArtifact_contents() {
		return artifact_contents;
	}

	public void setArtifact_contents(String artifact_contents) {
		this.artifact_contents = artifact_contents;
	}

	@Override
	public String toString() {
		return "DesignRequest [userId=" + userId + ", vnf_type=" + vnf_type + ", vnfc_type=" + vnfc_type + ", protocol="
				+ protocol + ", action=" + action + ", artifact_name=" + artifact_name + ", artifact_contents="
				+ artifact_contents + "]";
	}
	
	

}
