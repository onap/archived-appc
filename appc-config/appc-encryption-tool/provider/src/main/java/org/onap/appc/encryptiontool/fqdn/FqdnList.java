/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.appc.encryptiontool.fqdn;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "vnf-management-server-fqdn", "cloud-owner-list", "description", "username", "create-date",
		"modify-username", "modify-date" })
public class FqdnList {

	private String vnfManagementServerFqdn;
	private List<CloudOwnerList> cloudOwnerList = null;
	private String description;
	private String username;
	private String createDate;
	private String modifyUsername;
	private String modifyDate;

	@JsonProperty("vnf-management-server-fqdn")
	public String getVnfManagementServerFqdn() {
		return vnfManagementServerFqdn;
	}

	@JsonProperty("vnf-management-server-fqdn")
	public void setVnfManagementServerFqdn(String vnfManagementServerFqdn) {
		this.vnfManagementServerFqdn = vnfManagementServerFqdn;
	}

	@JsonProperty("cloud-owner-list")
	public List<CloudOwnerList> getCloudOwnerList() {
		return cloudOwnerList;
	}

	@JsonProperty("cloud-owner-list")
	public void setCloudOwnerList(List<CloudOwnerList> cloudOwnerList) {
		this.cloudOwnerList = cloudOwnerList;
	}

	@JsonProperty("description")
	public String getDescription() {
		return description;
	}

	@JsonProperty("description")
	public void setDescription(String description) {
		this.description = description;
	}

	@JsonProperty("username")
	public String getUsername() {
		return username;
	}

	@JsonProperty("username")
	public void setUsername(String username) {
		this.username = username;
	}

	@JsonProperty("create-date")
	public String getCreateDate() {
		return createDate;
	}

	@JsonProperty("create-date")
	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	@JsonProperty("modify-username")
	public String getModifyUsername() {
		return modifyUsername;
	}

	@JsonProperty("modify-username")
	public void setModifyUsername(String modifyUsername) {
		this.modifyUsername = modifyUsername;
	}

	@JsonProperty("modify-date")
	public String getModifyDate() {
		return modifyDate;
	}

	@JsonProperty("modify-date")
	public void setModifyDate(String modifyDate) {
		this.modifyDate = modifyDate;
	}

}