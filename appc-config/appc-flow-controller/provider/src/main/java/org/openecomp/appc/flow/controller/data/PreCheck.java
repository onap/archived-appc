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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PreCheck {

    
    @JsonProperty("precheck-operator")
    private String  precheckOperator;

    public String getPrecheckOperator() {
        return precheckOperator;
    }

    public void setPrecheckOperator(String precheckOperator) {
        this.precheckOperator = precheckOperator;
    }

    @JsonProperty("precheck-options")
    private List<PrecheckOption>  precheckOptions;

    public List<PrecheckOption> getPrecheckOptions() {
        return precheckOptions;
    }

    public void setPrecheckOptions(List<PrecheckOption> precheckOptions) {
        this.precheckOptions = precheckOptions;
    }

    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((precheckOperator == null) ? 0 : precheckOperator.hashCode());
        result = prime * result + ((precheckOptions == null) ? 0 : precheckOptions.hashCode());
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
        PreCheck other = (PreCheck) obj;
        if (precheckOperator == null) {
            if (other.precheckOperator != null)
                return false;
        } else if (!precheckOperator.equals(other.precheckOperator))
            return false;
        if (precheckOptions == null) {
            if (other.precheckOptions != null)
                return false;
        } else if (!precheckOptions.equals(other.precheckOptions))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "PreCheck [precheckOperator=" + precheckOperator + ", precheckOptions=" + precheckOptions + "]";
    }
    
    
    
    

}
