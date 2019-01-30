/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018-2019 AT&T Intellectual Property. All rights reserved.
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
@JsonPropertyOrder({
"cloud-owner",
"region-id-list"
})
public class CloudOwnerList {

    @JsonProperty("cloud-owner")
    private String cloudOwner;
    @JsonProperty("region-id-list")
    private List<RegionIdList> regionIdList = null;

    @JsonProperty("cloud-owner")
    public String getCloudOwner() {
    return cloudOwner;
    }

    @JsonProperty("cloud-owner")
    public void setCloudOwner(String cloudOwner) {
    this.cloudOwner = cloudOwner;
    }

    @JsonProperty("region-id-list")
    public List<RegionIdList> getRegionIdList() {
    return regionIdList;
    }

    @JsonProperty("region-id-list")
    public void setRegionIdList(List<RegionIdList> regionIdList) {
    this.regionIdList = regionIdList;
    }


}
