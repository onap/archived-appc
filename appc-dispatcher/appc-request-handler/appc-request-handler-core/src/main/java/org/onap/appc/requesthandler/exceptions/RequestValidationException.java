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

package org.onap.appc.requesthandler.exceptions;


import org.onap.appc.executor.objects.LCMCommandStatus;
import org.onap.appc.executor.objects.Params;

public class RequestValidationException extends Exception {
    private LCMCommandStatus lcmCommandStatus;
    private Params params;
    private String logMessage;
    private String targetEntity;
    private String targetService;

    public LCMCommandStatus getLcmCommandStatus() {
        return lcmCommandStatus;
    }

    public void setLcmCommandStatus(LCMCommandStatus lcmCommandStatus) {
        this.lcmCommandStatus = lcmCommandStatus;
    }

    public Params getParams() {
        return params;
    }

    public void setParams(Params params) {
        this.params = params;
    }

    public String getLogMessage() {
        return logMessage;
    }

    public void setLogMessage(String logMessage) {
        this.logMessage = logMessage;
    }

    public RequestValidationException( String message){
        super(message);
    }

    public RequestValidationException( String message , LCMCommandStatus lcmCommandStatus, Params params){
        super(message);
        this.lcmCommandStatus = lcmCommandStatus;
        this.params =params;
    }

    public String getTargetEntity() {
        return targetEntity;
    }

    public void setTargetEntity(String targetEntity) {
        this.targetEntity = targetEntity;
    }

    public String getTargetService() {
        return targetService;
    }

    public void setTargetService(String targetService) {
        this.targetService = targetService;
    }



}
