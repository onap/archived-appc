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

/** Handles async responses
 */
class AsyncRequestResponseHandler extends AbstractRequestResponseHandler {

    private final EELFLogger LOG = EELFManager.getInstance().getLogger(AsyncRequestResponseHandler.class);

    AsyncRequestResponseHandler(String corrID,
                                ICoreResponseHandler businessCallback,
                                CoreManager coreManager)
    {
        super(corrID, businessCallback, coreManager);
    }

    /**
     *  Calls API callback for sending response to consumer's listener. in case of complete response cleans timer and
     *  unregisters the handler.
     * @param response - Response
     * @param type - Type of Response
     */
    public void runTask(String response, String type) {
        boolean finalTask = false;
        try {
            finalTask = ((ICoreAsyncResponseHandler) businessCallback).onResponse(response, type);
        } catch (Exception e){
            LOG.error("Error on API layer, for request with correlation-id " + corrID,  e);
        }
        if (finalTask){
            coreManager.cancelTimer(corrID);
            coreManager.unregisterHandler(corrID);
        }
        else{
            response = null;
            type = null;
        }
    }

    /**
     * Calls to API layer for sending timeout exception.
     */
    @Override
    public void onTimeOut() {
        LOG.info("timeout for request with correlation-id " + corrID);
        ((ICoreAsyncResponseHandler)businessCallback).onException(new TimeoutException("timeout for request with correlation-id " + corrID));
    }
}
