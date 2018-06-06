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


public class RuntimeContext {

    private RequestContext requestContext;
    private ResponseContext responseContext;
    private VNFContext vnfContext;
    private TransactionRecord transactionRecord;

    //TODO move fields timeStart abd isLockAcquired to a better place
    private Instant timeStart;
    private String rpcName;

    public String getRpcName() {
        return rpcName;
    }

    public void setRpcName(String rpcName) {
        this.rpcName = rpcName;
    }

    public RequestContext getRequestContext() {
        return requestContext;
    }

    public void setRequestContext(RequestContext requestContext) {
        this.requestContext = requestContext;
    }

    public ResponseContext getResponseContext() {
        return responseContext;
    }

    public void setResponseContext(ResponseContext responseContext) {
        this.responseContext = responseContext;
    }

    public Instant getTimeStart() {
        return timeStart;
    }

    public void setTimeStart(Instant timeStart) {
        this.timeStart = timeStart;
    }

    public VNFContext getVnfContext() {
        return vnfContext;
    }

    public void setVnfContext(VNFContext vnfContext) {
        this.vnfContext = vnfContext;
    }


    public TransactionRecord getTransactionRecord() {
        return transactionRecord;
    }

    public void setTransactionRecord(TransactionRecord transactionRecord) {
        this.transactionRecord = transactionRecord;
    }


    @Override
    public String toString() {
        return "RuntimeContext{" +
                "requestContext=" + requestContext +
                ", responseContext=" + responseContext +
                ", vnfContext=" + vnfContext +
                ", timeStart=" + timeStart +
                ", rpcName='" + rpcName + '\'' +
                '}';
    }
}
