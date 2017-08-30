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

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

import java.util.concurrent.TimeoutException;

/** Handles sync requests
 */
class SyncRequestResponseHandler<T> extends AbstractRequestResponseHandler {

    private final EELFLogger LOG = EELFManager.getInstance().getLogger(SyncRequestResponseHandler.class);
    private T responseObject = null;
    private CoreException coreException = null;
    private TimeoutException timeoutException = null;

    SyncRequestResponseHandler(String corrID,
                               ICoreResponseHandler callback,
                               CoreManager coreManager){
        super(corrID, callback, coreManager);
    }

    /**
     *  Calls API callback for getting response object. in case of complete response notifies consumer
     *  thread for receiving response
     * @param response - Response
     * @param type - Type of Response
     */
    synchronized void runTask(String response, String type) {
        try {
            responseObject = ((ICoreSyncResponseHandler) businessCallback).onResponse(response, type);
        } catch (CoreException e) {
            coreException = e;
        }
        if(responseObject != null || coreException != null) {
            notify();
        }
    }


    /**
     * Returns response. goes sleep until coming either timeout event or complete response
     */
    public synchronized  <T> T getResponse() throws CoreException, TimeoutException {
        try{
            if(!isResponseReceived()){
                wait();
            }
            if (coreException != null) {
                throw coreException;
            }
            if ( timeoutException != null) {
                throw timeoutException;
            }

        } catch (InterruptedException e) {
            throw new CoreException(e);
        } finally{
            coreManager.unregisterHandler(corrID);
            coreManager.cancelTimer(corrID);
        }
        return (T) responseObject;
    }

    /**
     * indicates if a response received
     * @return
     */
    private boolean isResponseReceived() {
        return responseObject != null;
    }

    @Override
    public synchronized void onTimeOut() {
        LOG.error("sync response handler on timeout correlation ID <" + corrID + ">.");
        timeoutException = new TimeoutException("timeout for request with correlation-id " + corrID);
        notify();
    }




}
