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

package org.openecomp.appc.requesthandler;

import org.openecomp.appc.domainmodel.lcm.RuntimeContext;
import org.openecomp.appc.executor.UnstableVNFException;
import org.openecomp.appc.requesthandler.objects.RequestHandlerInput;
import org.openecomp.appc.requesthandler.objects.RequestHandlerOutput;

/**
 * This class provides application logic for the Request/Response Handler Component.
 *
 */
public interface RequestHandler {
    /**
     * It receives requests from the north-bound REST API (Communication) Layer and
     * performs following validations.
     * 1. VNF exists in A&AI for the given targetID (VnfID)
     * 2. For the current VNF  Orchestration Status, the command can be executed
     * 3. For the given VNF type and Operation, there exists work-flow definition in the APPC database
     * If any of the validation fails, it returns appropriate response
     *
     * @param input RequestHandlerInput object which contains request header and  other request parameters like command , target Id , payload etc.
     * @return response for request as enum with Return code and message.
     */
    RequestHandlerOutput handleRequest(RequestHandlerInput input);

    /**
     * This method perform operations required before execution of workflow starts. It retrieves next state for current operation from Lifecycle manager and update it in AAI.
     * @param vnf_id vnf id or target Id on which updates required
     * @param  requestIdentifierString - string contains id uniquely represents the request
     * @param forceFlag
     * @return true in case AAI updates are successful. false for any error or exception.
     */
    void onRequestExecutionStart(String vnf_id, boolean readOnlyActivity, String requestIdentifierString, boolean forceFlag) throws UnstableVNFException;

    /**
     * This method perform following operations required after execution of workflow.
     * It posts asynchronous response to message bus (DMaaP).
     * Unlock VNF Id
     * Removes request from request registry.
     * Generate audit logs.
     * Adds transaction record to database id if transaction logging is enabled.
     * @param runtimeContext RuntimeContext object which contains all parameters from request, response and few parameters from AA&I
     * @param isAAIUpdated boolean flag which indicate AAI upodate status after request completion.
     */
    void onRequestExecutionEnd(RuntimeContext runtimeContext, boolean isAAIUpdated);

    /**
     * This method perform following operations required if TTL ends when request still waiting in execution queue .
     * It posts asynchronous response to message bus (DMaaP).
     * Unlock VNF Id
     * Removes request from request registry.
     * @param runtimeContext RuntimeContext object which contains all parameters from request, response and few parameters from AA&I;
     * @param updateAAI boolean flag which indicate AAI upodate status after request completion.
     */
     void onRequestTTLEnd(RuntimeContext runtimeContext, boolean updateAAI);

    /**
     * This method returns the count of in progress requests
     * * @return in progress requests count
     */
     int getInprogressRequestCount();
}
