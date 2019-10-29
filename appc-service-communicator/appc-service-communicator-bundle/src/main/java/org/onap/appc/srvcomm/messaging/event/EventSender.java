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

package org.onap.appc.srvcomm.messaging.event;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.appc.configuration.Configuration;
import org.onap.appc.configuration.ConfigurationFactory;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.srvcomm.messaging.MessageDestination;
import org.onap.appc.srvcomm.messaging.MessagingConnector;
import java.util.Date;
import java.util.Map;


public class EventSender implements EventSenderInterface
{
    private final EELFLogger LOG = EELFManager.getInstance().getLogger(EventSender.class);
    public static final String PROPERTY_PREFIX = "dmaap.event";

    private static Configuration configuration = ConfigurationFactory.getConfiguration();

    private MessagingConnector messagingConnector;

    public EventSender(){
        messagingConnector = new MessagingConnector();
    }

    @Override
    public boolean sendEvent(MessageDestination destination, EventMessage msg) {
        String jsonStr = msg.toJson();
        String id = msg.getEventHeader().getEventId();
        LOG.info(String.format("Posting Message [%s - %s]", id, jsonStr));
        String propertyPrefix = destination.toString() + "." + PROPERTY_PREFIX;
        return messagingConnector.publishMessage(propertyPrefix, id, jsonStr);
    }

    @Override
    public boolean sendEvent(MessageDestination destination, EventMessage msg, String eventTopicName) {
        String jsonStr = msg.toJson();
        String id = msg.getEventHeader().getEventId();
        LOG.info(String.format("Posting Message [%s - %s]", id, jsonStr));
        String propertyPrefix = destination.toString() + "." + PROPERTY_PREFIX;
        return messagingConnector.publishMessage(propertyPrefix, id, eventTopicName, jsonStr);
    }

    @Override
    public boolean sendEvent(MessageDestination destination, Map<String, String> params, SvcLogicContext ctx) throws APPCException {

        if (params == null) {
            String message = "Parameters map is empty (null)";
            LOG.error(message);
            throw new APPCException(message);
        }
        String eventTime = new Date(System.currentTimeMillis()).toString();
        String apiVer = params.get("apiVer");
        String eventId = params.get("eventId");
        String reason = params.get("reason");
        String entityId = params.get("entityId");
        if(entityId != null){
            reason += "(" + entityId + ")";
        }
        Integer code = Integer.getInteger(params.get("code"), 500);

        if (eventTime == null || apiVer == null || eventId == null || reason == null) {
            String message = String.format("Missing input parameters: %s", params);
            LOG.error(message);
            throw new APPCException(message);
        }
        EventMessage eventMessage = new EventMessage(
                        new EventHeader(eventTime, apiVer, eventId),
                        new EventStatus(code, reason));

        return sendEvent(destination, eventMessage);
    }
    
}
