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

package org.openecomp.appc.flow.controller.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Transaction {

    @JsonProperty("transaction-id")
    private int transactionId ;

    @JsonProperty("action")
    private String action ;

    @JsonProperty("action-level")
    private String actionLevel ;

    @JsonProperty("action-identifier")
    private ActionIdentifier actionIdentifier ;
    
    @JsonProperty("parameters")
    private List<Parameters> parameters ;

    private String executionType;
    
    private String uId;
    
    private String statusCode;

    private String pswd;
    
    private String executionEndPoint;
    
    private String executionModule;
    
    private String executionRPC;
    
    @JsonProperty("state")
    private String state;
    
    @JsonProperty("precheck")
    private PreCheck precheck;

    @JsonProperty("payload")
    private String payload ;

    @JsonProperty("responses")
    private List<Response> responses ;
    
    
    private String status = "PENDING";
    
    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getPswd() {
        return pswd;
    }

    public void setPswd(String pswd) {
        this.pswd = pswd;
    }
    
    public String getExecutionEndPoint() {
        return executionEndPoint;
    }

    public void setExecutionEndPoint(String executionEndPoint) {
        this.executionEndPoint = executionEndPoint;
    }


    public String getExecutionType() {
        return executionType;
    }

    public void setExecutionType(String executionType) {
        this.executionType = executionType;
    }

    public String getExecutionModule() {
        return executionModule;
    }


    public void setExecutionModule(String executionModule) {
        this.executionModule = executionModule;
    }


    public String getExecutionRPC() {
        return executionRPC;
    }


    public void setExecutionRPC(String executionRPC) {
        this.executionRPC = executionRPC;
    }


    public List<Parameters> getParameters() {
        return parameters;
    }


    public void setParameters(List<Parameters> parameters) {
        this.parameters = parameters;
    }
    
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public PreCheck getPrecheck() {
        return precheck;
    }

    public void setPrecheck(PreCheck precheck) {
        this.precheck = precheck;
    }

    public String getStatus() {
        return status;
    }


    public void setStatus(String status) {
        this.status = status;
    }

    public List<Response> getResponses() {
        return responses;
    }


    public void setResponses(List<Response> responses) {
        this.responses = responses;
    }

    public int getTransactionId() {
        return transactionId;
    }


    public void setTransactionId(int transactionId) {
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



    public String getPayload() {
        return payload;
    }


    public void setPayload(String payload) {
        this.payload = payload;
    }


    public ActionIdentifier getActionIdentifier() {
        return actionIdentifier;
    }


    public void setActionIdentifier(ActionIdentifier actionIdentifier) {
        this.actionIdentifier = actionIdentifier;
    }
    
    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public String toString() {
        return "Transaction [transactionId=" + transactionId + ", action=" + action + ", actionLevel=" + actionLevel
                + ", actionIdentifier=" + actionIdentifier + ", parameters=" + parameters + ", executionType="
                + executionType + ", uId=" + uId + ", statusCode=" + statusCode + ", pswd=" + pswd
                + ", executionEndPoint=" + executionEndPoint + ", executionModule=" + executionModule
                + ", executionRPC=" + executionRPC + ", state=" + state + ", precheck=" + precheck + ", payload="
                + payload + ", responses=" + responses + ", status=" + status + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((action == null) ? 0 : action.hashCode());
        result = prime * result + ((actionIdentifier == null) ? 0 : actionIdentifier.hashCode());
        result = prime * result + ((actionLevel == null) ? 0 : actionLevel.hashCode());
        result = prime * result + ((executionEndPoint == null) ? 0 : executionEndPoint.hashCode());
        result = prime * result + ((executionModule == null) ? 0 : executionModule.hashCode());
        result = prime * result + ((executionRPC == null) ? 0 : executionRPC.hashCode());
        result = prime * result + ((executionType == null) ? 0 : executionType.hashCode());
        result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
        result = prime * result + ((payload == null) ? 0 : payload.hashCode());
        result = prime * result + ((precheck == null) ? 0 : precheck.hashCode());
        result = prime * result + ((pswd == null) ? 0 : pswd.hashCode());
        result = prime * result + ((responses == null) ? 0 : responses.hashCode());
        result = prime * result + ((state == null) ? 0 : state.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((statusCode == null) ? 0 : statusCode.hashCode());
        result = prime * result + transactionId;
        result = prime * result + ((uId == null) ? 0 : uId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Transaction other = (Transaction) obj;
        if (action == null) {
            if (other.action != null)
                return false;
        } else if (!action.equals(other.action))
            return false;
        if (actionIdentifier == null) {
            if (other.actionIdentifier != null)
                return false;
        } else if (!actionIdentifier.equals(other.actionIdentifier))
            return false;
        if (actionLevel == null) {
            if (other.actionLevel != null)
                return false;
        } else if (!actionLevel.equals(other.actionLevel))
            return false;
        if (executionEndPoint == null) {
            if (other.executionEndPoint != null)
                return false;
        } else if (!executionEndPoint.equals(other.executionEndPoint))
            return false;
        if (executionModule == null) {
            if (other.executionModule != null)
                return false;
        } else if (!executionModule.equals(other.executionModule))
            return false;
        if (executionRPC == null) {
            if (other.executionRPC != null)
                return false;
        } else if (!executionRPC.equals(other.executionRPC))
            return false;
        if (executionType == null) {
            if (other.executionType != null)
                return false;
        } else if (!executionType.equals(other.executionType))
            return false;
        if (parameters == null) {
            if (other.parameters != null)
                return false;
        } else if (!parameters.equals(other.parameters))
            return false;
        if (payload == null) {
            if (other.payload != null)
                return false;
        } else if (!payload.equals(other.payload))
            return false;
        if (precheck == null) {
            if (other.precheck != null)
                return false;
        } else if (!precheck.equals(other.precheck))
            return false;
        if (pswd == null) {
            if (other.pswd != null)
                return false;
        } else if (!pswd.equals(other.pswd))
            return false;
        if (responses == null) {
            if (other.responses != null)
                return false;
        } else if (!responses.equals(other.responses))
            return false;
        if (state == null) {
            if (other.state != null)
                return false;
        } else if (!state.equals(other.state))
            return false;
        if (status == null) {
            if (other.status != null)
                return false;
        } else if (!status.equals(other.status))
            return false;
        if (statusCode == null) {
            if (other.statusCode != null)
                return false;
        } else if (!statusCode.equals(other.statusCode))
            return false;
        if (transactionId != other.transactionId)
            return false;
        if (uId == null) {
            if (other.uId != null)
                return false;
        } else if (!uId.equals(other.uId))
            return false;
        return true;
    }


}



