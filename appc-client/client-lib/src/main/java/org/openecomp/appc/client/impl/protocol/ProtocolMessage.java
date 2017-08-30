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

package org.openecomp.appc.client.impl.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

class ProtocolMessage {

    private String version;
    private String type;
    private String rpcName;
    private String correlationID; // correlation-id
    private String partition; // cambria.partition
    private JsonNode body;

    @JsonProperty
    String getVersion() {
        return version;
    }

    @JsonProperty
    void setVersion(String version) {
        this.version = version;
    }

    @JsonProperty
    String getType() {
        return type;
    }

    @JsonProperty
     void setType(String type) {
        this.type = type;
    }

    @JsonProperty("rpc-name")
     String getRpcName() {
        return rpcName;
    }

    @JsonProperty("rpc-name")
     void setRpcName(String rpcName) {
        this.rpcName = rpcName;
    }

    @JsonProperty("correlation-id")
     String getCorrelationID() {
        return correlationID;
    }

    @JsonProperty("correlation-id")
     void setCorrelationID(String correlationID) {
        this.correlationID = correlationID;
    }

    @JsonProperty("cambria.partition")
     String getPartition() {
        return partition;
    }

    @JsonProperty("cambria.partition")
     void setPartition(String partition) {
        this.partition = partition;
    }

    @JsonProperty
    JsonNode getBody() {
        return body;
    }

    @JsonProperty
     void setBody(JsonNode body) {
        this.body = body;
    }
}
