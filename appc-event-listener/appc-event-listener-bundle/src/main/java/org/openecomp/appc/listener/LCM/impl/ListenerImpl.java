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

package org.openecomp.appc.listener.LCM.impl;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.openecomp.appc.listener.AbstractListener;
import org.openecomp.appc.listener.ListenerProperties;
import org.openecomp.appc.listener.LCM.conv.Converter;
import org.openecomp.appc.listener.LCM.model.DmaapIncomingMessage;
import org.openecomp.appc.listener.LCM.operation.ProviderOperations;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.RejectedExecutionException;

public class ListenerImpl extends AbstractListener {

    private final EELFLogger LOG = EELFManager.getInstance().getLogger(ListenerImpl.class);

    private long startTime = 0;

    private final ProviderOperations providerOperations;

    public ListenerImpl(ListenerProperties props) {
        super(props);

        String url = props.getProperty("provider.url");
        LOG.info("DMaaP Provider Endpoint: " + url);
        providerOperations = new ProviderOperations();
        providerOperations.setUrl(url);

        // Set Basic Auth
        String user = props.getProperty("provider.user");
        String pass = props.getProperty("provider.pass");
        providerOperations.setAuthentication(user, pass);
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


                    List<DmaapIncomingMessage> messages = dmaap.getIncomingEvents(DmaapIncomingMessage.class, QUEUED_MAX);
                    LOG.debug(String.format("Read %d messages from dmaap", messages.size()));
                    for (DmaapIncomingMessage incoming : messages) {
                        // Acknowledge that we read the event
                        if (isValid(incoming)) {
                            String requestIdWithSubId = getRequestIdWithSubId(incoming.getBody());
                            LOG.info("Acknowledging Message: " + requestIdWithSubId);
//                            dmaap.postStatus(incoming.toOutgoing(OperationStatus.PENDING));
                        }
                    }
                    for (DmaapIncomingMessage incoming : messages) {
                        String requestIdWithSubId = getRequestIdWithSubId(incoming.getBody());
                        // Add to pool if still running
                        if (run.get()) {
                            if (isValid(incoming)) {
                                LOG.info(String.format("Adding DMaaP message to pool queue [%s]", requestIdWithSubId));
                                try {
                                    executor.execute(new WorkerImpl(incoming, dmaap, providerOperations));
                                } catch (RejectedExecutionException rejectEx) {
                                    LOG.error("Task Rejected: ", rejectEx);
                                }
                            } else {
                                // Badly formed message
                                LOG.error("Message was not valid. Rejecting message: "+incoming);
                            }
                        } else {
                            if (isValid(incoming)) {
                                LOG.info("Run stopped. Orphaning Message: " + requestIdWithSubId);
                            }
                            else {
                                // Badly formed message
                                LOG.error("Message was not valid. Rejecting message: "+incoming);
                            }
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

    private boolean isValid(DmaapIncomingMessage incoming) {
        return ((incoming != null) &&
                incoming.getBody() != null
                && !StringUtils.isEmpty(incoming.getRpcName()));
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

    private String getRequestIdWithSubId(JsonNode event){
        String requestId = "";
        try {
            requestId = Converter.extractRequestIdWithSubId(event);
        } catch (Exception e) {
            LOG.error("failed to parse request-id and sub-request-id. Json not in expected format", e);
        }
        return requestId;
    }
}
