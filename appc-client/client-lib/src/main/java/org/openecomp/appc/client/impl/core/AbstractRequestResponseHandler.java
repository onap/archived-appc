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

/** Abstract request response handler class, responsible for common functionality of
 * @{@link AsyncRequestResponseHandler} and @{@link SyncRequestResponseHandler}
 */
abstract class AbstractRequestResponseHandler implements RequestResponseHandler {

    private final EELFLogger LOG = EELFManager.getInstance().getLogger(AbstractRequestResponseHandler.class);
    ICoreResponseHandler businessCallback;
    protected String corrID;
    CoreManager coreManager;


    AbstractRequestResponseHandler(String corrID,
                                   ICoreResponseHandler businessCallback,
                                   CoreManager coreManager)
    {
        this.businessCallback = businessCallback;
        this.corrID = corrID;
        this.coreManager = coreManager;
    }

    public synchronized void handleResponse(final MessageContext ctx, final String response) {
        try {
            coreManager.submitTask(ctx.getCorrelationID(), new Runnable() {
                @Override
                public void run() {
                    LOG.info("handling response of corrID <" + corrID + ">" + "response " + response);
                    if(coreManager.isExistHandler(corrID)) {
                        runTask(response, ctx.getType());
                    }

                }
            });
        } catch (InterruptedException e) {
            LOG.error("could not handle response <" + response + "> of corrID <" + corrID + ">", e);
        }
    }

    /**
     *
     * @param response - Response
     * @param type - Type of Response
     */
    abstract void runTask(String response, String type);

    @Override
    public void sendRequest(String request, String corrId, String rpcName) throws CoreException {
        if(!coreManager.isShutdownInProgress()) {
            coreManager.registerHandler(corrId, this);
            coreManager.sendRequest(request, corrId, rpcName);
            coreManager.startTimer(corrId);
        }else{
            throw new CoreException("Shutdown is in progress. Request will not be handled");
        }
    }

}
