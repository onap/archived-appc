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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Transaction {

    @JsonProperty("transactionId")
    private Integer transactionId;

    @JsonProperty("action")
    private String action;

    @JsonProperty("action-level")
    private String actionLevel;

    @JsonProperty("action-identifier")
    private ActionIdentifier actionIdentifier;

    @JsonProperty("payload")
    private String payload;

    @JsonProperty("parameters")
    private Map<String,String> parameters;

    @JsonProperty("precheck-operator")
    private String preCheckOperator;


    @JsonProperty("precheck-options")
    private List<PreCheckOption> precheckOptions;

    @JsonProperty("responses")
    private List<Response> responses;

    public Transaction(){
        responses = new LinkedList<>();
    }

    public Integer getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getActionLevel() {
        return actionLevel;
    }

    public void setActionLevel(String actionLevel) {
        this.actionLevel = actionLevel;
    }

    public ActionIdentifier getActionIdentifier() {
        return actionIdentifier;
    }

    public void setActionIdentifier(ActionIdentifier actionIdentifier) {
        this.actionIdentifier = actionIdentifier;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public List<Response> getResponses() {
        return responses;
    }

    public void setResponses(List<Response> responses) {
        this.responses = responses;
    }

    public void addResponse(Response response){
        this.responses.add(response);
    }

    public String getPreCheckOperator() {
        return preCheckOperator;
    }

    public void setPreCheckOperator(String preCheckOperator) {
        this.preCheckOperator = preCheckOperator;
    }

    public List<PreCheckOption> getPrecheckOptions() {
        return precheckOptions;
    }

    public void setPrecheckOptions(List<PreCheckOption> precheckOptions) {
        this.precheckOptions = precheckOptions;
    }
}
