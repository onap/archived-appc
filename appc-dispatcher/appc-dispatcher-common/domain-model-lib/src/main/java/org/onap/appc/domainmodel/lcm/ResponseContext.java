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

package org.onap.appc.domainmodel.lcm;

import java.util.HashMap;
import java.util.Map;


public class ResponseContext {
    private CommonHeader commonHeader;
    private Status status;
    private Map<String, String> additionalContext;
    /** Carries json String response payload */
    private String payload;
    /** Carries non-String response payload, such as List or Map of object */
    private Object payloadObject;

    public CommonHeader getCommonHeader() {
        return commonHeader;
    }

    public void setCommonHeader(CommonHeader commonHeader) {
        this.commonHeader = commonHeader;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Object getPayloadObject() {
        return payloadObject;
    }

    public void setPayloadObject(Object payloadObject) {
        this.payloadObject = payloadObject;
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
        return "ResponseContext{" +
                "commonHeader=" + commonHeader +
                ", status=" + status +
                ", payload='" + payload + '\'' +
                ", additionalContext=" + additionalContext +
                '}';
    }

}
