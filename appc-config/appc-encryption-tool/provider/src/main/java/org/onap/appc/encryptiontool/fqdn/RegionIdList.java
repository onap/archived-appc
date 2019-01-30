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
@JsonPropertyOrder({ "region-id", "tenant-id-list" })
public class RegionIdList {

	private String regionId;
	private List<String> tenantIdList = null;

	@JsonProperty("region-id")
	public String getRegionId() {
		return regionId;
	}

	@JsonProperty("region-id")
	public void setRegionId(String regionId) {
		this.regionId = regionId;
	}

	@JsonProperty("tenant-id-list")
	public List<String> getTenantIdList() {
		return tenantIdList;
	}

	@JsonProperty("tenant-id-list")
	public void setTenantIdList(List<String> tenantIdList) {
		this.tenantIdList = tenantIdList;
	}

}
