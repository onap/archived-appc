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

package org.openecomp.appc.domainmodel.lcm;

import java.util.HashMap;
import java.util.Map;


public class RequestContext {

    private CommonHeader commonHeader;
    private ActionIdentifiers actionIdentifiers;
    private VNFOperation action;
    private String payload;
    private Map<String, String> additionalContext;

    public CommonHeader getCommonHeader() {
        return commonHeader;
    }

    public void setCommonHeader(CommonHeader commonHeader) {
        this.commonHeader = commonHeader;
    }

    public ActionIdentifiers getActionIdentifiers() {
        return actionIdentifiers;
    }

    public void setActionIdentifiers(ActionIdentifiers actionIdentifiers) {
        this.actionIdentifiers = actionIdentifiers;
    }

    public VNFOperation getAction() {
        return action;
    }

    public void setAction(VNFOperation action) {
        this.action = action;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Map<String, String> getAdditionalContext() {
        return additionalContext;
    }

    public void setAdditionalContext(Map<String, String> additionalContext) {
        this.additionalContext = additionalContext;
    }

    public void addKeyValueToAdditionalContext(String key, String value ){
        if (this.additionalContext == null) {
            this.additionalContext = new HashMap<>();
        }
        this.additionalContext.put(key, value);
    }

    @Override
    public String toString() {
        return "RequestContext{" +
                "commonHeader=" + commonHeader +
                ", actionIdentifiers=" + actionIdentifiers +
                ", action=" + action +
                ", payload='" + payload + '\'' +
                ", additionalContext=" + additionalContext +
                '}';
    }
}
