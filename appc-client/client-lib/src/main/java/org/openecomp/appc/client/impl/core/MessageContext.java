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

package org.openecomp.appc.client.impl.core;

/** Helper class for wrapping request/response information.
 */
public class MessageContext {

    /**
     * valid values of type are response/error
     */
    private String type;

    /**
     * RPC name
     */
    private String rpc;

    /**
     * correlation ID
     */
    private String correlationID;

    /**
     * partitioner for message bus usage
     */
    private String partitioner;


    public String getRpc() {
        return rpc;
    }

    public void setRpc(String rpc) {
        this.rpc = rpc;
    }

    public String getCorrelationID() {
        return correlationID;
    }

    public void setCorrelationID(String correlationID) {
        this.correlationID = correlationID;
    }

    public String getPartiton() {
        return partitioner;
    }

    public void setPartiton(String partitioner) {
        this.partitioner = partitioner;
    }

    public void setType(String type){
        this.type = type;
    }

    public String getType(){
        return type;
    }


}
