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

public class RequestInfoBuilder {

    private String action;

    private String actionLevel;

    private ActionIdentifier actionIdentifier;

    private String payload;

    public RequestInfoBuilder actionIdentifier(){
        this.actionIdentifier = new ActionIdentifier();
        return this;
    }

    public RequestInfoBuilder vnfId(String vnfId){
        this.actionIdentifier.setVnfId(vnfId);
        return this;
    }

    public RequestInfoBuilder vnfcName(String vnfcName){
        this.actionIdentifier.setVnfcName(vnfcName);
        return this;
    }

    public RequestInfoBuilder vServerId(String vServerId){
        this.actionIdentifier.setvServerId(vServerId);
        return this;
    }

    public RequestInfoBuilder action(String action){
        this.action = action;
        return this;
    }

    public RequestInfoBuilder actionLevel(String actionLevel){
        this.actionLevel = actionLevel;
        return this;
    }

    public RequestInfoBuilder payload(String payload){
        this.payload = payload;
        return this;
    }

    public RequestInfo build(){
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setAction(this.action);
        requestInfo.setActionIdentifier(this.actionIdentifier);
        requestInfo.setActionLevel(this.actionLevel);
        requestInfo.setPayload(this.payload);
        return requestInfo;
    }
}
