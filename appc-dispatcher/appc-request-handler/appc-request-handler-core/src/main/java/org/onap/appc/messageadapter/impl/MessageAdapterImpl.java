/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications Copyright (C) 2019 Ericsson
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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.messageadapter.impl;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang.ObjectUtils;
import org.onap.appc.domainmodel.lcm.ResponseContext;
import org.onap.appc.domainmodel.lcm.VNFOperation;
import org.onap.appc.messageadapter.MessageAdapter;
import org.onap.appc.requesthandler.conv.Converter;
import org.onap.appc.srvcomm.messaging.MessagingConnector;

public class MessageAdapterImpl implements MessageAdapter{

    private MessagingConnector messageService;
    private String partition ;

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(MessageAdapterImpl.class);

    /**
     * Initialize producer client to post messages using configuration properties
     */
    @Override
    public void init(){
    	logger.debug("MessageAdapterImpl - init");
        this.messageService = new MessagingConnector();
    }
    
    public void init(MessagingConnector connector) {
        logger.debug("MessageAdapterImpl - init");
        this.messageService = connector;
    }

    /**
     * Posts message to DMaaP. As DMaaP accepts only json messages this method first convert dmaapMessage to json format and post it to DMaaP.
     * @param asyncResponse response data that based on it a message will be send to DMaaP (the format of the message that will be sent to DMaaP based on the action and its YANG domainmodel).
     * @return True if message is postes successfully else False
     */
    @Override
    public boolean post(VNFOperation operation, String rpcName, ResponseContext asyncResponse){
        boolean success;
        if (logger.isTraceEnabled()) {
            logger.trace("Entering to post with AsyncResponse = " + ObjectUtils.toString(asyncResponse));
        }
        logger.debug("Entered MessageAdapterImpl.post()");
        String jsonMessage;
        try {
        	logger.debug("Before converting Async Response");
            jsonMessage = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(operation, rpcName, asyncResponse);
            if (logger.isDebugEnabled()) {
                logger.debug("DMaaP Response = " + jsonMessage);
            }
            logger.debug("Before Invoking producer.post(): jsonMessage is::" + jsonMessage);
            success = messageService.publishMessage("appc.LCM", this.partition, jsonMessage);
            logger.debug("After Invoking producer.post()");
        } catch (JsonProcessingException e1) {
            logger.error("Error generating Json from DMaaP message " + e1.getMessage());
            success = false;
        }catch (Exception e){
            logger.error("Error sending message to DMaaP " + e.getMessage());
            success = false;
        }
        logger.debug("Exiting MesageAdapterImpl.post()");
        if (logger.isTraceEnabled()) {
            logger.trace("Exiting from post with (success = " + ObjectUtils.toString(success) + ")");
        }
        return success;
    }
}
