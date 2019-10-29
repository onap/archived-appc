/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.oam.messageadapter;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang.ObjectUtils;
import org.onap.appc.srvcomm.messaging.MessagingConnector;

public class MessageAdapter {

    private final EELFLogger logger = EELFManager.getInstance().getLogger(MessageAdapter.class);

    private static final String  PROPERTIES_PREFIX = "appc.OAM";

    private MessagingConnector messagingConnector;
    private String partition;
    private boolean isDisabled;

    /**
     * Initialize producer client to post messages using configuration properties.
     */
    public void init() {

        if (isAppcOamPropsListenerEnabled()) {
            messagingConnector = new MessagingConnector();
        } else {
            logger.warn(String.format("The listener %s is disabled and will not be run", "appc.OAM"));
        }
    }
    
    public void init(MessagingConnector connector) {

        if (isAppcOamPropsListenerEnabled()) {
            messagingConnector = connector;
        } else {
            logger.warn(String.format("The listener %s is disabled and will not be run", "appc.OAM"));
        }
    }


    /**
     * Get producer. If it is null, call createProducer to create it again.
     *
     * @return Producer
     */
    MessagingConnector getMessagingConnector() {
        if (messagingConnector == null) {
            messagingConnector = new MessagingConnector();
        }

        return messagingConnector;
    }

    /**
     * Posts message to UEB. As UEB accepts only json messages this method first convert uebMessage to json format
     * and post it to UEB.
     *
     * @param oamContext response data that based on it a message will be send to UEB (the format of the message that
     *                   will be sent to UEB based on the action and its YANG domainmodel).
     */
    public void post(OAMContext oamContext) {
        if (logger.isTraceEnabled()) {
            logger.trace("Entering to post with AsyncResponse = " + ObjectUtils.toString(oamContext));
        }

        boolean success;
        String jsonMessage;
        try {
            jsonMessage = Converter.convAsyncResponseToUebOutgoingMessageJsonString(oamContext);
            if (logger.isDebugEnabled()) {
                logger.debug("UEB Response = " + jsonMessage);
            }

            MessagingConnector connector = getMessagingConnector();
            success = connector != null && connector.publishMessage(PROPERTIES_PREFIX, this.partition, jsonMessage);
        } catch (JsonProcessingException e1) {
            logger.error("Error generating Json from UEB message " + e1.getMessage());
            success = false;
        } catch (Exception e) {
            logger.error("Error sending message to UEB " + e.getMessage(), e);
            success = false;
        }

        if (logger.isTraceEnabled()) {
            logger.trace("Exiting from post with (success = " + ObjectUtils.toString(success) + ")");
        }
    }

    private boolean isAppcOamPropsListenerEnabled() {
        return !isDisabled;
    }
}
