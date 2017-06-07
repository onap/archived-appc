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

package org.openecomp.appc.listener.LCM.model;

import org.openecomp.appc.listener.LCM.operation.OperationStatus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class InputBody {

    @JsonProperty("common-header")
    private CommonHeader commonHeader;

    @JsonProperty("action-identifiers")
    private ActionIdentifiers actionIdentifiers;

    @JsonProperty("action")
    private String action;

    @JsonProperty("payload")
    private Object payload;


    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }
    @JsonIgnore
    public void setPayloadAsString(String payload) {
        this.payload = payload;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public ActionIdentifiers getActionIdentifiers() {
        return actionIdentifiers;
    }

    public void setActionIdentifiers(ActionIdentifiers actionIdentifiers) {
        this.actionIdentifiers = actionIdentifiers;
    }

    public CommonHeader getCommonHeader() {
        return commonHeader;
    }

    public void setCommonHeader(CommonHeader commonHeader) {
        this.commonHeader = commonHeader;
    }

//    public String toOutgoing(OperationStatus operationStatus) {
//        OutputBody out = new OutputBody(this);
//        out.setStatus(new ResponseStatus(operationStatus.getCode(), operationStatus.getValue()));
//        return out.toResponse().toString();
//    }

//    public String toOutgoing(OperationStatus operationStatus,String islocked) {
//        OutputBody out = new OutputBody(this);
//        out.setStatus(new ResponseStatus(operationStatus.getCode(), operationStatus.getValue()));
//        out.setLocked(islocked);
//        return out.toResponse().toString();
//    }

    @JsonIgnore
    public boolean isValid() {
        return getCommonHeader() != null;
    }
}
