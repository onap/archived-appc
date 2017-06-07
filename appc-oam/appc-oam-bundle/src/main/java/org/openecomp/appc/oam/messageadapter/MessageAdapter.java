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

package org.openecomp.appc.oam.messageadapter;

import org.openecomp.appc.adapter.message.MessageAdapterFactory;
import org.openecomp.appc.adapter.message.Producer;
import org.openecomp.appc.configuration.Configuration;
import org.openecomp.appc.configuration.ConfigurationFactory;
import org.openecomp.appc.listener.impl.EventHandlerImpl;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang.ObjectUtils;

import java.util.HashSet;
import java.util.Properties;

public class MessageAdapter {

    private Producer producer;
    private String partition ;
    private Configuration configuration;
    private HashSet<String> pool;
    private String writeTopic;
    private String apiKey;
    private String apiSecret;

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(MessageAdapter.class);

    /**
     * Initialize producer client to post messages using configuration properties
     */
    public void init(){
        this.producer = getProducer();
    }

    private Producer getProducer() {
        configuration = ConfigurationFactory.getConfiguration();
        Properties properties=configuration.getProperties();
        updateProperties(properties);
        Producer localProducer = null;
        
        BundleContext ctx = FrameworkUtil.getBundle(EventHandlerImpl.class).getBundleContext();
        if (ctx != null) {
        	ServiceReference svcRef = ctx.getServiceReference(MessageAdapterFactory.class.getName());
        	if (svcRef != null) {
        		localProducer = ((MessageAdapterFactory) ctx.getService(svcRef)).createProducer(pool, writeTopic, apiKey, apiSecret);
		        for (String url : pool) {
		            if (url.contains("3905") || url.contains("https")) {
		            	localProducer.useHttps(true);
		                break;
		            }
		        }
        	}
        }

        return localProducer;
    }

    private void updateProperties(Properties props) {
        if (logger.isTraceEnabled()) {
            logger.trace("Entering to updateProperties with Properties = "+ ObjectUtils.toString(props));
        }
        pool = new HashSet<>();
        if (props != null) {
            writeTopic = props.getProperty("appc.OAM.topic.write");
            apiKey = props.getProperty("appc.OAM.client.key");
            apiSecret = props.getProperty("appc.OAM.client.secret");
            String hostnames = props.getProperty("appc.OAM.poolMembers");
            if (hostnames != null && !hostnames.isEmpty()) {
                for (String name : hostnames.split(",")) {
                    pool.add(name);
                }
            }
        }
    }

    /**
     * Posts message to UEB. As UEB accepts only json messages this method first convert uebMessage to json format and post it to UEB.
     * @param oamContext response data that based on it a message will be send to UEB (the format of the message that will be sent to UEB based on the action and its YANG domainmodel).
     * @return True if message is postes successfully else False
     */
    public boolean post(OAMContext oamContext){
        boolean success;
        if (logger.isTraceEnabled()) {
            logger.trace("Entering to post with AsyncResponse = " + ObjectUtils.toString(oamContext));
        }

        String jsonMessage;
        try {
            jsonMessage = Converter.convAsyncResponseToUebOutgoingMessageJsonString(oamContext);
            if (logger.isDebugEnabled()) {
                logger.debug("UEB Response = " + jsonMessage);
            }
            success  = producer.post(this.partition, jsonMessage);
        } catch (JsonProcessingException e1) {
            logger.error("Error generating Jason from UEB message "+ e1.getMessage());
            success= false;
        }catch (Exception e){
            logger.error("Error sending message to UEB "+e.getMessage());
            success= false;
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Exiting from post with (success = "+ ObjectUtils.toString(success)+")");
        }
        return success;
    }
}
