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

package org.openecomp.appc.adapter.messaging.dmaap.impl;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.openecomp.sdnc.sli.SvcLogicContext;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.openecomp.appc.adapter.message.EventSender;
import org.openecomp.appc.adapter.message.MessageDestination;
import org.openecomp.appc.adapter.message.Producer;
import org.openecomp.appc.adapter.message.event.EventHeader;
import org.openecomp.appc.adapter.message.event.EventMessage;
import org.openecomp.appc.adapter.message.event.EventStatus;
import org.openecomp.appc.adapter.messaging.dmaap.impl.DmaapProducerImpl;
import org.openecomp.appc.configuration.Configuration;
import org.openecomp.appc.configuration.ConfigurationFactory;
import org.openecomp.appc.exceptions.APPCException;

public class EventSenderDmaapImpl implements EventSender
{
    private static final EELFLogger LOG = EELFManager.getInstance().getLogger(EventSenderDmaapImpl.class);
    public static final String EVENT_TOPIC_WRITE = "dmaap.event.topic.write";
    public static final String DMAAP_USERNAME = "dmaap.appc.username";
    public static final String DMAAP_PASSWORD = "dmaap.appc.password";
    public static final String EVENT_POOL_MEMBERS = "dmaap.event.pool.members";

    private static Configuration configuration = ConfigurationFactory.getConfiguration();

    private Map<String,Producer> producerMap = new ConcurrentHashMap<>();

    public Map<String, Producer> getProducerMap() {
        return producerMap;
    }

    public void setProducerMap(Map<String, Producer> producerMap) {
        this.producerMap = producerMap;
    }

    public EventSenderDmaapImpl(){

    }

    public void initialize(){
        Properties properties = configuration.getProperties();
        String writeTopic;
        String username;
        String password;
        final List<String> pool = new ArrayList<>();

        for(MessageDestination destination: MessageDestination.values()){
            writeTopic = properties.getProperty(destination + "." +  EVENT_TOPIC_WRITE);
            username = properties.getProperty(destination + "." + DMAAP_USERNAME);
            password = properties.getProperty(destination + "." + DMAAP_PASSWORD);
            String hostNames = properties.getProperty(destination + "." + EVENT_POOL_MEMBERS);

            if (hostNames != null && !hostNames.isEmpty()) {
                LOG.debug(String.format("hostNames = %s, taken from property: %s", hostNames, destination + "." + EVENT_POOL_MEMBERS));
                Collections.addAll(pool, hostNames.split(","));
            }

            LOG.debug(String.format("pool = %s, taken from property: %s", pool, destination + "." + EVENT_POOL_MEMBERS));
            LOG.debug(String.format("writeTopic = %s, taken from property: %s", writeTopic, destination + "." + EVENT_TOPIC_WRITE));
            LOG.debug(String.format("username = %s, taken from property: %s", username, destination + "." + DMAAP_USERNAME));
            Producer producer = new DmaapProducerImpl(pool, writeTopic,username, password);

            for (String url : pool) {
                if (url.contains("3905") || url.contains("https")) {
                    LOG.debug("Producer should use HTTPS");
                    producer.useHttps(true);
                    break;
                }
            }
            producerMap.put(destination.toString(),producer);
        }

    }

    @Override
    public boolean sendEvent(MessageDestination destination, EventMessage msg) {
        String jsonStr = msg.toJson();
        String id = msg.getEventHeader().getEventId();
        LOG.info(String.format("Posting Message [%s - %s]", id, jsonStr));
        Producer producer = producerMap.get(destination.toString());
        return producer.post(id, jsonStr);
    }

    @Override
    public boolean sendEvent(MessageDestination destination, EventMessage msg, String eventTopicName) {
        String jsonStr = msg.toJson();
        String id = msg.getEventHeader().getEventId();
        LOG.info(String.format("Posting Message [%s - %s]", id, jsonStr));
        Producer producer = createProducer(destination, eventTopicName);
        return producer.post(id, jsonStr);
    }
    
    private Producer createProducer(MessageDestination destination, String eventTopicName) {
        Properties properties = configuration.getProperties();
        final List<String> pool = new ArrayList<>();
        String username = properties.getProperty(destination + "." + DMAAP_USERNAME);
        String password = properties.getProperty(destination + "." + DMAAP_PASSWORD);
        String hostNames = properties.getProperty(destination + "." + EVENT_POOL_MEMBERS);

        if (hostNames != null && !hostNames.isEmpty()) {
            LOG.debug(String.format("hostNames = %s, taken from property: %s", hostNames, destination + "." + EVENT_POOL_MEMBERS));
            Collections.addAll(pool, hostNames.split(","));
        }

        LOG.debug(String.format("pool = %s, taken from property: %s", pool, destination + "." + EVENT_POOL_MEMBERS));
        LOG.debug(String.format("writeTopic = %s, taken from property: %s", eventTopicName, destination + "." + EVENT_TOPIC_WRITE));
        LOG.debug(String.format("username = %s, taken from property: %s", username, destination + "." + DMAAP_USERNAME));
        Producer producer = new DmaapProducerImpl(pool, eventTopicName,username, password);

        for (String url : pool) {
            if (url.contains("3905") || url.contains("https")) {
                LOG.debug("Producer should use HTTPS");
                producer.useHttps(true);
                break;
            }
        }
        return producer;
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
        String entityId=params.get("entityId");
        if(entityId!=null){
            reason=reason+"("+entityId+")";
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

        return sendEvent(destination,eventMessage);
    }
}
