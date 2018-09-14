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

package org.onap.appc.interfaces.service.data;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Request {

    @JsonProperty("action")
    private String action;

    @JsonProperty("action-identifiers")
    private ActionIdentifier actionIdentifiers;
    
    @JsonProperty("target-id")
     private String  targetId;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public ActionIdentifier getActionIdentifiers() {
        return actionIdentifiers;
    }

    public void setActionIdentifiers(ActionIdentifier actionIdentifiers) {
        this.actionIdentifiers = actionIdentifiers;
    }

    public String getTargetId() {
              return targetId;
     }

     public void setTargetId(String targetId) {
               this.targetId = targetId;
       }
}
