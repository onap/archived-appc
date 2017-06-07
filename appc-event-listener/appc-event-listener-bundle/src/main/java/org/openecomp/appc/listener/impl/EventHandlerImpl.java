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

package org.openecomp.appc.listener.impl;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

import org.openecomp.appc.adapter.factory.DmaapMessageAdapterFactoryImpl;
import org.openecomp.appc.adapter.factory.MessageService;
import org.openecomp.appc.adapter.message.Consumer;
import org.openecomp.appc.adapter.message.MessageAdapterFactory;
import org.openecomp.appc.adapter.message.Producer;
import org.openecomp.appc.listener.EventHandler;
import org.openecomp.appc.listener.ListenerProperties;
import org.openecomp.appc.listener.util.Mapper;
import org.openecomp.appc.logging.LoggingConstants;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.MDC;

import java.util.*;

/**
 * This class is a wrapper for the DMaaP client provided in appc-dmaap-adapter. Its aim is to ensure that only well formed
 * messages are sent and received on DMaaP.
 * 
 */
public class EventHandlerImpl implements EventHandler {

    private final EELFLogger LOG = EELFManager.getInstance().getLogger(EventHandlerImpl.class);

    /*
     * The amount of time in seconds to keep a connection to a topic open while waiting for data
     */
    private int READ_TIMEOUT = 60;

    /*
     * The pool of hosts to query against
     */
    private Collection<String> pool;

    /*
     * The topic to read messages from
     */
    private String readTopic;

    /*
     * The topic to write messages to
     */
    private Set<String> writeTopics;

    /*
     * The client (group) name to use for reading messages
     */
    private String clientName;

    /*
     * The id of the client (group) that is reading messages
     */
    private String clientId;

    /*
     * The api public key to use for authentication
     */
    private String apiKey;

    /*
     * The api secret key to use for authentication
     */
    private String apiSecret;

    /*
     * A json object containing filter arguments.
     */
    private String filter_json;

    private MessageService messageService;

    private Consumer reader = null;
    private Producer producer = null;
    
    public EventHandlerImpl(ListenerProperties props) {
        pool = new HashSet<String>();
        writeTopics = new HashSet<String>();

        if (props != null) {
            readTopic = props.getProperty(ListenerProperties.KEYS.TOPIC_READ);
            clientName = props.getProperty(ListenerProperties.KEYS.CLIENT_NAME, "APP-C");
            clientId = props.getProperty(ListenerProperties.KEYS.CLIENT_ID, "0");
            apiKey = props.getProperty(ListenerProperties.KEYS.AUTH_USER_KEY);
            apiSecret = props.getProperty(ListenerProperties.KEYS.AUTH_SECRET_KEY);

            filter_json = props.getProperty(ListenerProperties.KEYS.TOPIC_READ_FILTER);

            READ_TIMEOUT = Integer
                .valueOf(props.getProperty(ListenerProperties.KEYS.TOPIC_READ_TIMEOUT, String.valueOf(READ_TIMEOUT)));

            String hostnames = props.getProperty(ListenerProperties.KEYS.HOSTS);
            if (hostnames != null && !hostnames.isEmpty()) {
                for (String name : hostnames.split(",")) {
                    pool.add(name);
                }
            }

            String writeTopicStr = props.getProperty(ListenerProperties.KEYS.TOPIC_WRITE);
            if (writeTopicStr != null) {
                for (String topic : writeTopicStr.split(",")) {
                    writeTopics.add(topic);
                }
            }

            messageService = MessageService.parse(props.getProperty(ListenerProperties.KEYS.MESSAGE_SERVICE));

            LOG.info(String.format(
                "Configured to use %s client on host pool [%s]. Reading from [%s] filtered by %s. Wriring to [%s]. Authenticated using %s",
                messageService, hostnames, readTopic, filter_json, writeTopics, apiKey));
        }
    }

    @Override
    public List<String> getIncomingEvents() {
        return getIncomingEvents(1000);
    }

    @Override
    public List<String> getIncomingEvents(int limit) {
        List<String> out = new ArrayList<String>();
        LOG.info(String.format("Getting up to %d incoming events", limit));
        // reuse the consumer object instead of creating a new one every time
        if (reader == null) {
        	LOG.info("Getting Consumer...");
        	reader = getConsumer();
        }
        
        List<String> items = null;
        try{
            items = reader.fetch(READ_TIMEOUT * 1000, limit);
        }catch(Error r){
            LOG.error("EvenHandlerImpl.getIncomingEvents",r);
        }
        
        
        for (String item : items) {
            out.add(item);
        }
        LOG.info(String.format("Read %d messages from %s as %s/%s.", out.size(), readTopic, clientName, clientId));
        return out;
    }

    @Override
    public <T> List<T> getIncomingEvents(Class<T> cls) {
        return getIncomingEvents(cls, 1000);
    }

    @Override
    public <T> List<T> getIncomingEvents(Class<T> cls, int limit) {
        List<String> incomingStrings = getIncomingEvents(limit);
        return Mapper.mapList(incomingStrings, cls);
    }

    @Override
    public void postStatus(String event) {
        postStatus(null, event);
    }

    @Override
    public void postStatus(String partition, String event) {
        LOG.debug(String.format("Posting Message [%s]", event));
        if (producer == null) {
        	LOG.info("Getting Producer...");
        	producer = getProducer();
        }
        producer.post(partition, event);
    }

    /**
     * Returns a consumer object for direct access to our Cambria consumer interface
     * 
     * @return An instance of the consumer interface
     */
    protected Consumer getConsumer() {
        LOG.debug(String.format("Getting Consumer: %s  %s/%s/%s", pool, readTopic, clientName, clientId));
        if (filter_json == null && writeTopics.contains(readTopic)) {
            LOG.error(
                "*****We will be writing and reading to the same topic without a filter. This will cause an infinite loop.*****");
        }
        
        Consumer out=null;
        BundleContext ctx = FrameworkUtil.getBundle(EventHandlerImpl.class).getBundleContext();
        if (ctx != null) {
        	ServiceReference svcRef = ctx.getServiceReference(MessageAdapterFactory.class.getName());
        	if (svcRef != null) {
		        try{
		            out = ((MessageAdapterFactory) ctx.getService(svcRef)).createConsumer(pool, readTopic, clientName, clientId, filter_json, apiKey, apiSecret);
		        }catch(Error e){
		            //TODO:create eelf message
		            LOG.error("EvenHandlerImp.getConsumer calling MessageAdapterFactor.createConsumer",e);
		        }
		        for (String url : pool) {
		            if (url.contains("3905") || url.contains("https")) {
		                out.useHttps(true);
		                break;
		            }
		        }
        	}
        }
        return out;
    }

    /**
     * Returns a consumer object for direct access to our Cambria producer interface
     * 
     * @return An instance of the producer interface
     */
    protected Producer getProducer() {
        LOG.debug(String.format("Getting Producer: %s  %s", pool, readTopic));

        Producer out = null;
        BundleContext ctx = FrameworkUtil.getBundle(EventHandlerImpl.class).getBundleContext();
        if (ctx != null) {
        	ServiceReference svcRef = ctx.getServiceReference(MessageAdapterFactory.class.getName());
        	if (svcRef != null) {
        		out = ((MessageAdapterFactory) ctx.getService(svcRef)).createProducer(pool, writeTopics,apiKey, apiSecret);
		        for (String url : pool) {
		            if (url.contains("3905") || url.contains("https")) {
		                out.useHttps(true);
		                break;
		            }
		        }
        	}
        }
        return out;
    }

    @Override
    public void closeClients() {
    	LOG.debug("Closing Consumer and Producer DMaaP clients");
        if (reader != null) {
        	reader.close();
        }
        if (producer != null) {
        	producer.close();
        }
    }
    
    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public String getClientName() {
        return clientName;
    }

    @Override
    public void setClientName(String clientName) {
        this.clientName = clientName;
        MDC.put(LoggingConstants.MDCKeys.PARTNER_NAME, clientName);
    }

    @Override
    public void addToPool(String hostname) {
        pool.add(hostname);
    }

    @Override
    public Collection<String> getPool() {
        return pool;
    }

    @Override
    public void removeFromPool(String hostname) {
        pool.remove(hostname);
    }

    @Override
    public String getReadTopic() {
        return readTopic;
    }

    @Override
    public void setReadTopic(String readTopic) {
        this.readTopic = readTopic;
    }

    @Override
    public Set<String> getWriteTopics() {
        return writeTopics;
    }

    @Override
    public void setWriteTopics(Set<String> writeTopics) {
        this.writeTopics = writeTopics;
    }

    @Override
    public void clearCredentials() {
        apiKey = null;
        apiSecret = null;
    }

    @Override
    public void setCredentials(String key, String secret) {
        apiKey = key;
        apiSecret = secret;
    }
}
