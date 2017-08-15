/*-
 * ============LICENSE_START=======================================================
 * ONAP : APP-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property.  All rights reserved.
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

package org.openecomp.appc.seqgen.objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.openecomp.appc.dg.objects.InventoryModel;
import org.openecomp.appc.dg.objects.VnfcDependencyModel;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SequenceGeneratorInput {

    @NotNull
    @JsonProperty("request-info")
    private RequestInfo requestInfo;

    @JsonIgnore
    private InventoryModel inventoryModel;

    @JsonIgnore
    private VnfcDependencyModel dependencyModel;

    @JsonProperty("tunable-parameters")
    private Map<String,String> tunableParams;

    @JsonProperty("capabilities")
    private Map<String,List<String>> capability;

    public RequestInfo getRequestInfo() {
        return requestInfo;
    }

    public void setRequestInfo(RequestInfo requestInfo) {
        this.requestInfo = requestInfo;
    }

    public InventoryModel getInventoryModel() {
        return inventoryModel;
    }

    public void setInventoryModel(InventoryModel inventoryModel) {
        this.inventoryModel = inventoryModel;
    }

    public VnfcDependencyModel getDependencyModel() {
        return dependencyModel;
    }

    public void setDependencyModel(VnfcDependencyModel dependencyModel) {
        this.dependencyModel = dependencyModel;
    }

    public Map<String, String> getTunableParams() {
        return tunableParams;
    }

    public void setTunableParams(Map<String, String> tunableParams) {
        this.tunableParams = tunableParams;
    }

    public Map<String, List<String>> getCapability() {
        return capability;
    }

    public void setCapability(Map<String, List<String>> capability) {
        this.capability = capability;
    }
}
