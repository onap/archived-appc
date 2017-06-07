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

package org.openecomp.appc.listener.demo.impl;

import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.listener.EventHandler;
import org.openecomp.appc.listener.demo.model.IncomingMessage;
import org.openecomp.appc.listener.demo.model.Status;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class WorkerImpl implements Runnable {

    private final EELFLogger LOG = EELFManager.getInstance().getLogger(WorkerImpl.class);

    // Should have all of the data we need for processing
    private IncomingMessage event;

    // So we can post messages from inside the worker.
    private EventHandler dmaap;

    public WorkerImpl(IncomingMessage message, EventHandler dmaap) {
        this.event = message;
        this.dmaap = dmaap;
    }

    @Override
    public void run() {
        LOG.debug(String.format("Started working on %s",  event.getHeader().getRequestID()));
    
        dmaap.postStatus(event.toOutgoing(Status.ACCEPTED));
        // Run the dg in a try catch to handle all exceptions and update the
        // message at the end
        try {
            if (doDG(event)) {
                dmaap.postStatus(event.toOutgoing(Status.SUCCESS));
                LOG.debug(String.format("Event %s finished successfully",  event.getHeader().getRequestID()));
            } else {
                // Should never happen. Exception with message should be thrown instead.
                LOG.error(String.format(
                    "We somehow returned false from doDG() instead of throwing exception. Incoming event [%s]",
                    event.toJson().toString()));
                dmaap.postStatus(event.toOutgoing(Status.FAILURE));
            }

        } catch (Exception e) {
            // Unknown exception from DG method. Fail and pass the exception
            // along
            String msg = "Exception: " + e.getMessage();
            LOG.warn(String.format("Event %s finished with failure. %s", event.getHeader().getRequestID(), msg));
            //TODO: should a message be included? there is nothing in the API spec for a msg?            
            //dmaap.postStatus(event.toOutgoing(Status.FAILURE, msg));
            dmaap.postStatus(event.toOutgoing(Status.FAILURE));
        }

        LOG.debug("Done working on " + event.getHeader().getRequestID());
    }

    private boolean doDG(IncomingMessage msg) throws APPCException {
        return ProviderOperations.topologyDG(msg);
    }
}
