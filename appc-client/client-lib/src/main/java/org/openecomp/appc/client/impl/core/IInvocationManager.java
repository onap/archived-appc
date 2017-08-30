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

import java.util.Properties;
import java.util.concurrent.TimeoutException;

/**
 */
public interface IInvocationManager {

    /**
     * initializes the manager
     * @param prop properties to read from
     * @throws CoreException thrown if madatory fields are not set right
     */
    void init(Properties prop) throws CoreException;

    /**
     * handles the flow of an async request
     * @param request the request body
     * @param listener business response handler
     * @param correlationId unique id of the request
     * @param rpcName rpc call name
     * @throws CoreException thrown if the request failed to be sent
     */
    void asyncRequest(String request, ICoreAsyncResponseHandler listener, String correlationId, String rpcName) throws CoreException;

    /**
     * handles to flow of a sync request
     * @param request the request body
     * @param callback business response handler
     * @param correlationId unique id of the request
     * @param rpcName rpc call name
     * @return the output object to be returned
     * @throws CoreException thrown if the request failed to be sent
     * @throws TimeoutException thrown if timeout has exceeded
     */
    <T> T syncRequest(String request, ICoreSyncResponseHandler callback, String correlationId, String rpcName) throws CoreException, TimeoutException;

    /**
     * shuts the invocation manager down.
     * @param isForceShutdown if true, shutdown will be forced, otherwise it will be gracefully
     */
    void shutdown(boolean isForceShutdown);
}
