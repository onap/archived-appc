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

package org.onap.appc.validationpolicy.objects;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Rule {
    @JsonProperty("action-received")
    private String actionReceived;
    @JsonProperty("validation-rule")
    private String validationRule;
    @JsonProperty("in-progress-action-inclusion-list")
    private List<String> inclusionList;
    @JsonProperty("in-progress-action-exclusion-list")
    private List<String> exclusionList;

    public Rule(){

    }

    public String getActionReceived() {
        return actionReceived;
    }

    public void setActionReceived(String actionReceived) {
        this.actionReceived = actionReceived;
    }

    public String getValidationRule() {
        return validationRule;
    }

    public void setValidationRule(String validationRule) {
        this.validationRule = validationRule;
    }

    public List<String> getInclusionList() {
        return inclusionList;
    }

    public void setInclusionList(List<String> inclusionList) {
        this.inclusionList = inclusionList;
    }

    public List<String> getExclusionList() {
        return exclusionList;
    }

    public void setExclusionList(List<String> exclusionList) {
        this.exclusionList = exclusionList;
    }
}
