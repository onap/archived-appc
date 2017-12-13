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

package org.openecomp.sdnc.config.audit.node;


import java.util.Map;

public class Parameters
{

    String payloadX ;

    public String getPayloadX() {
        return payloadX;
    }
    public void setPayloadX(String payloadX) {
        this.payloadX = payloadX;
    }

    String payloadXtype;

    public String getPayloadXtype() {
        return payloadXtype;
    }
    public void setPayloadXtype(String payloadXtype) {
        this.payloadXtype = payloadXtype;
    }

    String payloadY ;

    public String getPayloadY() {
        return payloadY;
    }
    public void setPayloadY(String payloadY) {
        this.payloadY = payloadY;
    }

    String payloadYtype;

    public String getPayloadYtype() {
        return payloadYtype;
    }
    public void setPayloadYtype(String payloadYtype) {
        this.payloadYtype = payloadYtype;
    }

    String compareDataType;

    public String getCompareDataType() {
        return compareDataType;
    }
    public void setCompareDataType(String compareDataType) {
        this.compareDataType = compareDataType;
    }

    String compareType;

    public String getCompareType() {
        return compareType;
    }
    public void setCompareType(String compareType) {
        this.compareType = compareType;
    }

    String requestIdentifier;

    public String getRequestIdentifier() {
        return requestIdentifier;
    }
    public void setRequestIdentifier(String requestIdentifier) {
        this.requestIdentifier = requestIdentifier;
    }
    public Parameters(Map<String, String> inParams)
    {
        this.compareType = inParams.get("compareType");
        this.compareDataType = inParams.get("compareDataType");
        this.payloadX= inParams.get("sourceData");
        this.payloadY= inParams.get("targetData");
        this.payloadXtype = inParams.get("sourceDataType");
        this.payloadYtype = inParams.get("targetDataType");
        this.requestIdentifier = inParams.get("requestIdentifier");

    }
}
