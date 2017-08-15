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

public class ResponseAction {
    
    @JsonProperty("wait")
    private String wait;
    
    @JsonProperty("retry")
    private String retry;
    
    @JsonProperty("jump")
    private String jump;
    
    @JsonProperty("ignore")
    private boolean  ignore;
    
    @JsonProperty("stop")
    private boolean stop;
    
    @JsonProperty("intermediate-message")
    private boolean intermediateMessage;

    public String getWait() {
        return wait;
    }

    public void setWait(String wait) {
        this.wait = wait;
    }

    public String getRetry() {
        return retry;
    }

    public void setRetry(String retry) {
        this.retry = retry;
    }

    public String getJump() {
        return jump;
    }

    public void setJump(String jump) {
        this.jump = jump;
    }

    public boolean isIgnore() {
        return ignore;
    }

    public void setIgnore(boolean ignore) {
        this.ignore = ignore;
    }

    public boolean isStop() {
        return stop;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }



    @Override
    public String toString() {
        return "ResponseAction [wait=" + wait + ", retry=" + retry + ", jump=" + jump + ", ignore=" + ignore + ", stop="
                + stop + ", intermediateMessage=" + intermediateMessage + "]";
    }

    public boolean isIntermediateMessage() {
        return intermediateMessage;
    }

    public void setIntermediateMessage(boolean intermediateMessage) {
        this.intermediateMessage = intermediateMessage;
    }
    
    

}
