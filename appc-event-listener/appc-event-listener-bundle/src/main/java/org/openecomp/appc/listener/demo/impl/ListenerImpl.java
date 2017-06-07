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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.RejectedExecutionException;

import org.openecomp.appc.listener.AbstractListener;
import org.openecomp.appc.listener.ListenerProperties;
import org.openecomp.appc.listener.demo.model.IncomingMessage;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;

public class ListenerImpl extends AbstractListener {

    private final EELFLogger LOG = EELFManager.getInstance().getLogger(ListenerImpl.class);

    private long startTime = 0;

    public ListenerImpl(ListenerProperties props) {
        super(props);
        String url = props.getProperty("provider.url");        
        LOG.info("DMaaP Provider Endpoint: " + url);
        ProviderOperations.setUrl(url);

        // Set Basic Auth
        String user = props.getProperty("provider.user");
        String pass = props.getProperty("provider.pass");
        ProviderOperations.setAuthentication(user, pass);
    }

    @Override
    public void run() {
        // Some vars for benchmarking
        startTime = System.currentTimeMillis();

        LOG.info("Running DMaaP Listener");

        while (run.get()) {
            // Only update if the queue is low. otherwise we read in more
            // messages than we need
            try {
                if (executor.getQueue().size() <= QUEUED_MIN) {
                    LOG.debug("DMaaP queue running low. Querying for more jobs");
                    List<IncomingMessage> messages = dmaap.getIncomingEvents(IncomingMessage.class, QUEUED_MAX);
                    LOG.debug(String.format("Read %d messages from dmaap", messages.size()));
                    for (IncomingMessage incoming : messages) {
                        // Acknowledge that we read the event
                        LOG.info("Acknowledging Message: " + incoming.getHeader().getRequestID());
                        
                        //TODO: Should we post a pending status for 1607
                        //dmaap.postStatus(incoming.toOutgoing(Status.PENDING, null).toString());
                    }
                    for (IncomingMessage incoming : messages) {
                        // Add to pool if still running
                        if (run.get()) {
                            LOG.info(String.format("Adding DMaaP message to pool queue [%s]", incoming.getHeader().getRequestID()));
                            if (incoming.isValid()) {
                                try {
                                    executor.execute(new WorkerImpl(incoming, dmaap));
                                } catch (RejectedExecutionException rejectEx) {
                                    LOG.error("Task Rejected: ", rejectEx);
                                }
                            } else {
                                // Badly formed message
                                LOG.error("Message was not valid. Rejecting");
                            }
                        } else {
                            LOG.info("Run stopped. Orphaning Message: " + incoming.getHeader().getRequestID());
                        }
                    }
                }
            } catch (Exception e) {
                LOG.error("Exception " + e.getClass().getSimpleName() + " caught in DMaaP listener");
                LOG.error(EELFResourceManager.format(e));
                LOG.error("DMaaP Listener logging and ignoring the exception, continue...");
            }
        }

        LOG.info("Stopping DMaaP Listener thread");

        // We've told the listener to stop
        // TODO - Should we:
        // 1) Put a message back on the queue indicating that APP-C never got to
        // the message
        // or
        // 2) Let downstream figure it out after timeout between PENDING and
        // ACTIVE messages
    }

    @Override
    public String getBenchmark() {
        long time = System.currentTimeMillis();
        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        String runningTime = df.format(new Date(time - startTime));

        String out = String.format("Running for %s and completed %d jobs using %d threads.", runningTime,
            executor.getCompletedTaskCount(), executor.getPoolSize());
        LOG.info("***BENCHMARK*** " + out);
        return out;
    }

}
