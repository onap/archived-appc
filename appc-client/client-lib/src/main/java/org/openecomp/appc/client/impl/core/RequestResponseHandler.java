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

interface RequestResponseHandler {

    /**
     * sends request, registers handler of response and start timer.
     * @param request - Request
     * @param corrId - correlation ID
     * @param rpcName - RPC name
     * @throws CoreException - @{@link CoreException}
     */
    void sendRequest(String request, String corrId, String rpcName) throws CoreException;

    /**
     * submits a handler task to task queue @{@link TaskQueue}, this task will be performed only if this handler is
     * still existing in core registry @{@link CoreRegistry}, others timeout was occurred .
     * @param ctx - Message Context @{@link MessageContext}
     * @param response - Response from backend
     */
    void handleResponse(MessageContext ctx, String response);

    /**
     * handles timeout event
     */
    void onTimeOut();
}
