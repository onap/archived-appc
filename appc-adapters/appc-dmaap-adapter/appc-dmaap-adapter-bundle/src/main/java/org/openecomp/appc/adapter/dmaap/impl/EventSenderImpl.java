/*-
 * ============LICENSE_START=======================================================
 * openECOMP : APP-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 						reserved.
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
 */

package org.openecomp.appc.adapter.dmaap.impl;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.openecomp.appc.adapter.dmaap.EventSender;
import org.openecomp.appc.adapter.dmaap.Producer;
import org.openecomp.appc.adapter.dmaap.DmaapDestination;
import org.openecomp.appc.adapter.dmaap.event.EventHeader;
import org.openecomp.appc.adapter.dmaap.event.EventMessage;
import org.openecomp.appc.adapter.dmaap.event.EventStatus;
import org.openecomp.appc.adapter.dmaap.DmaapProducer;
import org.openecomp.appc.configuration.Configuration;
import org.openecomp.appc.configuration.ConfigurationFactory;
import org.openecomp.appc.exceptions.APPCException;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.openecomp.sdnc.sli.SvcLogicContext;


public class EventSenderImpl implements EventSender
{
    private static final EELFLogger LOG = EELFManager.getInstance().getLogger(EventSenderImpl.class);
    public static final String EVENT_TOPIC_WRITE = "event.topic.write";
    public static final String EVENT_CLIENT_KEY = "event.client.key";
    public static final String EVENT_CLIENT_SECRET = "event.client.secret";
    public static final String EVENT_POOL_MEMBERS = "event.pool.members";

    private static Configuration configuration = ConfigurationFactory.getConfiguration();

    private Map<String,Producer> producerMap = new ConcurrentHashMap<>();

    public Map<String, Producer> getProducerMap() {
        return producerMap;
    }

    public void setProducerMap(Map<String, Producer> producerMap) {
        this.producerMap = producerMap;
    }

    public EventSenderImpl(){

    }

    public void initialize(){
        Properties properties = configuration.getProperties();
        String writeTopic;
        String apiKey;
        String apiSecret;
        final List<String> pool = new ArrayList<>();

        for(DmaapDestination destination:DmaapDestination.values()){
            writeTopic = properties.getProperty(destination + "." +  EVENT_TOPIC_WRITE);
            apiKey = properties.getProperty(destination + "." + EVENT_CLIENT_KEY);
            apiSecret = properties.getProperty(destination + "." + EVENT_CLIENT_SECRET);
            String hostNames = properties.getProperty(destination + "." + EVENT_POOL_MEMBERS);

            if (hostNames != null && !hostNames.isEmpty()) {
                LOG.debug(String.format("hostNames = %s, taken from property: %s", hostNames, destination + "." + EVENT_POOL_MEMBERS));
                Collections.addAll(pool, hostNames.split(","));
            }

            LOG.debug(String.format("pool = %s, taken from property: %s", pool, destination + "." + EVENT_POOL_MEMBERS));
            LOG.debug(String.format("writeTopic = %s, taken from property: %s", writeTopic, destination + "." + EVENT_TOPIC_WRITE));
            LOG.debug(String.format("apiKey = %s, taken from property: %s", apiKey, destination + "." + EVENT_CLIENT_KEY));
            Producer producer = new DmaapProducer(pool, writeTopic);

            if (apiKey != null && apiSecret != null) {
                producer.updateCredentials(apiKey, apiSecret);
            }

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
    public boolean sendEvent(DmaapDestination destination,EventMessage msg) {
        String jsonStr = msg.toJson();
        String id = msg.getEventHeader().getEventId();
        LOG.info(String.format("Posting Message [%s - %s]", id, jsonStr));
        Producer producer = producerMap.get(destination.toString());
        return producer.post(id, jsonStr);
    }

    @Override
    public boolean sendEvent(DmaapDestination destination,Map<String, String> params, SvcLogicContext ctx) throws APPCException {

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
        EventMessage dmaapEventMessage = new EventMessage(
                        new EventHeader(eventTime, apiVer, eventId),
                        new EventStatus(code, reason));

        return sendEvent(destination,dmaapEventMessage);
    }
}
