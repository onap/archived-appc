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

package org.openecomp.appc.listener.LCM.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.JsonNode;


@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DmaapMessage{

    @JsonProperty("version")
    private String version;

    @JsonProperty("type")
    private String type;

    @JsonProperty("correlation-id")
    private String correlationID;

    @JsonProperty("cambria.partition")
    private String cambriaPartition;

    @JsonProperty("rpc-name")
    private String rpcName;

    @JsonProperty("body")
    private JsonNode body;

    public DmaapMessage() {
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCorrelationID() {
        return correlationID;
    }

    public void setCorrelationID(String correlationID) {
        this.correlationID = correlationID;
    }

    public String getCambriaPartition() {
        return cambriaPartition;
    }

    public void setCambriaPartition(String cambriaPartition) {
        this.cambriaPartition = cambriaPartition;
    }

    public String getRpcName() {
        return rpcName;
    }

    public void setRpcName(String rpcName) {
        this.rpcName = rpcName;
    }

    public JsonNode getBody() {
        return body;
    }

    public void setBody(JsonNode body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "DmaapMessage{" +
                "version='" + version + '\'' +
                ", type='" + type + '\'' +
                ", correlationId='" + correlationID + '\'' +
                ", cambriaPartition='" + cambriaPartition + '\'' +
                ", rpcName='" + rpcName + '\'' +
                ", body=" + body +
                '}';
    }

}

