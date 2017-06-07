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

package org.openecomp.appc.transactionrecorder.objects;

import java.time.Instant;


public class TransactionRecord {

   /*
- Timestamp = RequestHandlerInput.RequestHeader.timeStamp
- Request ID = RequestHandlerInput.RequestHeader.requestID
- Start time = from flow
- End time = from flow
- VF_ID = RequestHandlerInput.targetID
- VF_type = genericVnf.getVnfType()
- Sub-component (optional) e.g. VFC_ID/VM UUID  - ???? empty
- Operation e.g. Start, Configure etc.  = CommandContext.Command
- Result - Success/Error code + description,as published to the initiator RequestHandlerResponse.ACCEPTED/RequestHandlerResponse.REJECTED + String (description)
    */

    private Instant timeStamp;
    private String requestID;
    private Instant startTime;
    private Instant endTime;
    private String targetID;
    private String targetType;
    private String subComponent;
    private String operation;
    private String resultCode;
    private String description;

    public Instant getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Instant timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getRequestID() {
        return requestID;
    }

    public void setRequestID(String requestID) {
        this.requestID = requestID;
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

    public String getTargetID() {
        return targetID;
    }

    public void setTargetID(String targetID) {
        this.targetID = targetID;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getSubComponent() {
        return subComponent;
    }

    public void setSubComponent(String subComponent) {
        this.subComponent = subComponent;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "TransactionRecord{" +
                "timeStamp=" + timeStamp +
                ", requestID='" + requestID + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", targetID='" + targetID + '\'' +
                ", targetType='" + targetType + '\'' +
                ", subComponent='" + subComponent + '\'' +
                ", operation='" + operation + '\'' +
                ", resultCode='" + resultCode + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
