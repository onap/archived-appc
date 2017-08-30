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
 * layer for passing requests from API to Core
 */
class InvocationManager implements IInvocationManager{

    protected CoreManager coreManager = null;

    InvocationManager(){
    }

    public void init(Properties properties) throws CoreException {
        coreManager = new CoreManager(properties);
    }

    /**
     *
     * @param request
     * @param businessCallback
     * @param correlationId
     * @param rpcName
     * @throws CoreException
     */
    public void asyncRequest(String request, ICoreAsyncResponseHandler businessCallback, String correlationId, String rpcName) throws CoreException {
        AsyncRequestResponseHandler requestResponseHandler = new AsyncRequestResponseHandler(correlationId, businessCallback, coreManager);
        requestResponseHandler.sendRequest(request, correlationId, rpcName);
    }

    public <T> T syncRequest(String request, ICoreSyncResponseHandler businessCallback, String correlationId, String rpcName ) throws CoreException, TimeoutException {
        SyncRequestResponseHandler requestResponseHandler = new SyncRequestResponseHandler(correlationId, businessCallback, coreManager);
        requestResponseHandler.sendRequest(request, correlationId, rpcName);
        T responseObject = (T) requestResponseHandler.getResponse();
        return responseObject;
    }

    @Override
    public void shutdown(boolean isForceShutdown) {
        coreManager.shutdown(isForceShutdown);
    }

}
