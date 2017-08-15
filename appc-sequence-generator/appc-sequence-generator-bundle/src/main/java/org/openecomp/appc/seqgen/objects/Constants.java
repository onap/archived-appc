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

public class Constants {
    public static final String RETRY_COUNT = "retry-count";
    public static final String WAIT_TIME = "wait-time";
    public static final Integer WAIT_TIME_VALUE = 60;
    public static final Integer RETRY_COUNT_VALUE = 4;
    public static final String STRATEGY = "strategy";
    public static final String VNFC_TYPE = "vnfc-type";

    public enum CapabilityLevel{
        VNF("vnf"),VNFC("vnfc"),VM("vm");
        private String level;
        CapabilityLevel(String level) {
            this.level=level;
        }

        public String getLevel() {
            return level;
        }
    }

    public enum Capabilties{
        START_APPLICATION("StartApplication"),HEALTH_CHECK("HealthCheck"),STOP_APPLICATION("StopApplication");
        private String capability;

        Capabilties(String capability) {
            this.capability=capability;
        }
        public String getCapability(){
            return capability;
        }
    }

    public enum ResponseMessage{
        HEALTHY("healthy"),UNHEALTHY("unhealthy"),SUCCESS("success"),FAILURE("failure");

        public String getResponse() {
            return response;
        }

        private String response;
        ResponseMessage(String response){
            this.response=response;
        }

    }

    public enum ResponseAction{
        STOP("stop"),RETRY("retry"),IGNORE("ignore"),WAIT("wait"),CONTINUE("Continue");

        ResponseAction(String action) {
            this.action=action;
        }

        private String action;

        public String getAction() {
            return action;
        }

    }

    public enum Action{
        START("Start"),START_APPLICATION("StartApplication"),HEALTH_CHECK("HealthCheck"),STOP_APPLICATION("StopApplication"),STOP("Stop");

        Action(String actionType) {
            this.actionType=actionType;
        }

        public String getActionType() {
            return actionType;
        }

        private String actionType;

    }
    public enum ActionLevel{
        VM("vm"),VNFC("vnfc"),VNF("vnf"),VF_MODULE("vf-module");
        private String action;
        ActionLevel(String action){
            this.action=action;
        }
        public String getAction() {
            return action;
        }
    }

    public enum PreCheckOperator{
        ANY("any"),ALL("all");

        PreCheckOperator(String operator){
            this.operator=operator;
        }

        public String getOperator() {
            return operator;
        }

        private String operator;

    }
}
