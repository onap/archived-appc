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

/**
 * Flags are generic flags that apply to any and all commands, all are optional
 */
@javax.annotation.Generated(
    value = {"templates/client-kit/open-api-to-java.ftl"},
    date = "2017-05-04T20:09:01.498+05:30",
    comments = "Auto-generated from Open API specification")
public class Flags {

    public enum Mode {
        EXCLUSIVE,
        NORMAL;
    }

    @JsonProperty("mode")
    private Mode mode;

    public enum Force {
        TRUE,
        FALSE;
    }

    @JsonProperty("force")
    private Force force;

    @JsonProperty("ttl")
    private int ttl;

    /**
     * EXCLUSIVE (accept no queued requests on this VNF while processing) or NORMAL (queue other requests until complete)
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * EXCLUSIVE (accept no queued requests on this VNF while processing) or NORMAL (queue other requests until complete)
     */
    public void setMode(Mode mode) {
        this.mode = mode;
    }

    /**
     * TRUE/FALSE - Execute action even if target is in unstable (i.e. locked, transiting, etc.) state
     */
    public Force getForce() {
        return force;
    }

    /**
     * TRUE/FALSE - Execute action even if target is in unstable (i.e. locked, transiting, etc.) state
     */
    public void setForce(Force force) {
        this.force = force;
    }

    /**
     * The timeout value (expressed in seconds) for action execution, between action being received by APPC and action initiation
     */
    public int getTtl() {
        return ttl;
    }

    /**
     * The timeout value (expressed in seconds) for action execution, between action being received by APPC and action initiation
     */
    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

}
