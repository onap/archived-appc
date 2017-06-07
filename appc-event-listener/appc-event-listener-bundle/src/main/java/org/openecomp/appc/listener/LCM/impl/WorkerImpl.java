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

import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.listener.EventHandler;
import org.openecomp.appc.listener.LCM.conv.Converter;
import org.openecomp.appc.listener.LCM.model.DmaapMessage;
import org.openecomp.appc.listener.LCM.model.DmaapOutgoingMessage;
import org.openecomp.appc.listener.LCM.operation.ProviderOperations;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

public class WorkerImpl implements Runnable {

    private final EELFLogger LOG = EELFManager.getInstance().getLogger(WorkerImpl.class);

    // Should have all of the data we need for processing
    private DmaapMessage event;

    // So we can post messages from inside the worker.
    private EventHandler dmaap;

    //so we know were to post the messages
    private final ProviderOperations providerOperations;


    public WorkerImpl(DmaapMessage message, EventHandler dmaap, ProviderOperations providerOperations) {
        this.event = message;
        this.dmaap = dmaap;
        this.providerOperations = providerOperations;
    }

    @Override
    public void run() {
        String requestIdWithSubId = extractRequestIdWithSubId(event.getBody());
        LOG.debug(String.format("Started working on %s", requestIdWithSubId));

        // Run the dg in a try catch to handle all exceptions and update the
        // message at the end
        try {
            JsonNode outputJsonNode = doDG(event.getRpcName(), event.getBody());
            DmaapOutgoingMessage dmaapOutgoingMessage= Converter.convJsonNodeToDmaapOutgoingMessage(event, outputJsonNode);
            postMessageToDMaaP(dmaapOutgoingMessage,requestIdWithSubId);
            Integer statusCode = extractStatusCode(dmaapOutgoingMessage.getBody());
            if (ProviderOperations.isSucceeded(statusCode)) {
                LOG.debug(String.format("Event %s finished successfully", requestIdWithSubId));
            } else {
                LOG.warn(String.format("Event %s failed", requestIdWithSubId));
            }

        } catch (Exception e) {
            // Unknown exception from DG method. Fail and pass the exception
            // along
            String msg = "Exception: " + e.getMessage();
            LOG.error(String.format("Event %s finished with failure. %s", requestIdWithSubId, msg));
            DmaapOutgoingMessage dmaapOutgoingMessage= Converter.buildDmaapOutgoingMessageWithUnexpectedError(event, e);
            postMessageToDMaaP(dmaapOutgoingMessage,requestIdWithSubId);
        }

        LOG.debug("Done working on " + requestIdWithSubId);
    }


    private Integer extractStatusCode(JsonNode event) {
        Integer statusCode = null;
        try {
            statusCode = Converter.extractStatusCode(event);
        } catch (Exception e) {
            LOG.error("failed to parse statusCode. Json not in expected format", e);
        }
        return statusCode;
    }


    private String extractRequestIdWithSubId(JsonNode event){
        String requestId = "";
        try {
            requestId = Converter.extractRequestIdWithSubId(event);
        } catch (Exception e) {
            LOG.error("failed to parse request-id and sub-request-id. Json not in expected format", e);
        }
        return requestId;
    }



    private void postMessageToDMaaP(DmaapOutgoingMessage dmaapOutgoingMessage,String requestIdWithSubId) {
        String dmaapOutgoingMessageJsonString;
        try {
            dmaapOutgoingMessageJsonString = Converter.convDmaapOutgoingMessageToJsonString(dmaapOutgoingMessage);
            dmaap.postStatus(dmaapOutgoingMessage.getCambriaPartition(),dmaapOutgoingMessageJsonString);
        } catch (JsonProcessingException e) {
            LOG.error("failed to postMessageToDMaaP requestIdWithSubId: "+requestIdWithSubId+" dmaapOutgoingMessage: "+dmaapOutgoingMessage, e);
        }
    }

    private JsonNode doDG(String rpcName, JsonNode msg) throws APPCException {
        return providerOperations.topologyDG(rpcName,msg);
    }
}
