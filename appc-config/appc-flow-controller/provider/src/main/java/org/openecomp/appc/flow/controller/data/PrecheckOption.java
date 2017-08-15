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

import com.fasterxml.jackson.annotation.JsonProperty;

public class PrecheckOption {

    
    @JsonProperty("pre-transaction-id")
    private int  pTransactionID ;

    @JsonProperty("param-name")
    private String  paramName ;
    
    
    @JsonProperty("param-value")
    private String  paramValue ;
    
    @JsonProperty("rule")
    private String  rule ;

    public int getpTransactionID() {
        return pTransactionID;
    }

    public void setpTransactionID(int pTransactionID) {
        this.pTransactionID = pTransactionID;
    }

    public String getParamName() {
        return paramName;
    }

    public void setParamName(String paramName) {
        this.paramName = paramName;
    }

    public String getParamValue() {
        return paramValue;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }

    

    @Override
    public String toString() {
        return "PrecheckOption [pTransactionID=" + pTransactionID + ", paramName=" + paramName + ", paramValue="
                + paramValue + ", rule=" + rule + "]";
    }

    
    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PrecheckOption other = (PrecheckOption) obj;
        if (pTransactionID != other.pTransactionID)
            return false;
        if (paramName == null) {
            if (other.paramName != null)
                return false;
        } else if (!paramName.equals(other.paramName))
            return false;
        if (paramValue == null) {
            if (other.paramValue != null)
                return false;
        } else if (!paramValue.equals(other.paramValue))
            return false;
        if (rule != other.rule)
            return false;
        return true;
    }
    
    
}
