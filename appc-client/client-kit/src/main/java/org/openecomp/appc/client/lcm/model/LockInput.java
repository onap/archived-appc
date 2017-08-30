/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

/**
 * NOTE: This file is auto-generated and should not be changed manually.
 */
package org.openecomp.appc.client.lcm.model;

import com.fasterxml.jackson.annotation.JsonProperty;

@javax.annotation.Generated(
    value = {"templates/client-kit/open-api-to-java.ftl"},
    date = "2017-05-04T20:09:01.607+05:30",
    comments = "Auto-generated from Open API specification")
public class LockInput {

    @JsonProperty("common-header")
    private CommonHeader commonHeader;

    @JsonProperty("action")
    private Action action;

    @JsonProperty("action-identifiers")
    private ActionIdentifiers actionIdentifiers;

    @JsonProperty("payload")
    private Payload payload;

    /**
     * A common header for all APP-C requests
     */
    public CommonHeader getCommonHeader() {
        return commonHeader;
    }

    /**
     * A common header for all APP-C requests
     */
    public void setCommonHeader(CommonHeader commonHeader) {
        this.commonHeader = commonHeader;
    }

    /**
     * The action to be taken by APP-C, e.g. Restart, Rebuild, Migrate
     */
    public Action getAction() {
        return action;
    }

    /**
     * The action to be taken by APP-C, e.g. Restart, Rebuild, Migrate
     */
    public void setAction(Action action) {
        this.action = action;
    }

    /**
     * A block containing the action arguments. These are used to specify the object upon which APP-C LCM command is to operate
     */
    public ActionIdentifiers getActionIdentifiers() {
        return actionIdentifiers;
    }

    /**
     * A block containing the action arguments. These are used to specify the object upon which APP-C LCM command is to operate
     */
    public void setActionIdentifiers(ActionIdentifiers actionIdentifiers) {
        this.actionIdentifiers = actionIdentifiers;
    }

    /**
     * The payload can be any valid JSON string value. Json escape characters need to be added when required to include an inner json within the payload to make it a valid json string value
     */
    public Payload getPayload() {
        return payload;
    }

    /**
     * The payload can be any valid JSON string value. Json escape characters need to be added when required to include an inner json within the payload to make it a valid json string value
     */
    public void setPayload(Payload payload) {
        this.payload = payload;
    }

}
