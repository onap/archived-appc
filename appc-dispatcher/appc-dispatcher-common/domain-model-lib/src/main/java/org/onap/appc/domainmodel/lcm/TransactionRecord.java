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

import java.time.Instant;


public class TransactionRecord {

    private String transactionId;
    private Instant originTimestamp;
    private String requestId;
    private String subRequestId;
    private String originatorId;
    private Instant startTime;
    private Instant endTime;
    private String targetId;
    private String targetType;
    private VNFOperation operation;
    private int resultCode;
    private String description;
    private RequestStatus requestState;
    private String serviceInstanceId;
    private String vnfcName;
    private String vserverId;
    private String vfModuleId;
    private Flags.Mode mode;

    public Instant getOriginTimestamp() {
        return originTimestamp;
    }

    public void setOriginTimestamp(Instant originTimestamp) {
        this.originTimestamp = originTimestamp;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    public String getMode() {
        return mode.name();
    }

    public void setMode(Flags.Mode mode) {
        this.mode=mode;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getServiceInstanceId() {
        return serviceInstanceId;
    }

    public void setServiceInstanceId(String serviceInstanceId) {
        this.serviceInstanceId = serviceInstanceId;
    }

    public String getVnfcName() {
        return vnfcName;
    }

    public void setVnfcName(String vnfcName) {
        this.vnfcName = vnfcName;
    }

    public String getVserverId() {
        return vserverId;
    }

    public void setVserverId(String vserverId) {
        this.vserverId = vserverId;
    }

    public String getVfModuleId() {
        return vfModuleId;
    }

    public void setVfModuleId(String vfModuleId) {
        this.vfModuleId = vfModuleId;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public VNFOperation getOperation() {
        return operation;
    }

    public void setOperation(VNFOperation operation) {
        this.operation = operation;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequestState() {
        return requestState.name();
    }

    public void setRequestState(RequestStatus requestState) {
        this.requestState = requestState;
    }

    public String getOriginatorId() {
        return originatorId;
    }

    public void setOriginatorId(String originatorId) {
        this.originatorId = originatorId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getSubRequestId() {
        return subRequestId;
    }

    public void setSubRequestId(String subRequestId) {
        this.subRequestId = subRequestId;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    @Override
    public String toString() {
        return "TransactionRecord{" +
                "transactionId='" + transactionId + '\'' +
                ", originTimestamp=" + originTimestamp +
                ", requestId='" + requestId + '\'' +
                ", subRequestId='" + subRequestId + '\'' +
                ", originatorId='" + originatorId + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", targetId='" + targetId + '\'' +
                ", targetType='" + targetType + '\'' +
                ", operation='" + operation + '\'' +
                ", resultCode='" + resultCode + '\'' +
                ", description='" + description + '\'' +
                ", requestState='" + requestState + '\'' +
                ", serviceInstanceId='" + serviceInstanceId + '\'' +
                ", vnfcName='" + vnfcName + '\'' +
                ", vserverId='" + vserverId + '\'' +
                ", vfModuleId='" + vfModuleId + '\'' +
                '}';
    }
}
