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

package org.openecomp.appc.messageadapter.impl;


import org.openecomp.appc.adapter.factory.DmaapMessageAdapterFactoryImpl;
import org.openecomp.appc.adapter.factory.MessageService;
import org.openecomp.appc.adapter.message.MessageAdapterFactory;
import org.openecomp.appc.adapter.message.Producer;
import org.openecomp.appc.configuration.Configuration;
import org.openecomp.appc.configuration.ConfigurationFactory;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang.ObjectUtils;
import org.openecomp.appc.domainmodel.lcm.ResponseContext;
import org.openecomp.appc.domainmodel.lcm.VNFOperation;
import org.openecomp.appc.messageadapter.MessageAdapter;
import org.openecomp.appc.requesthandler.conv.Converter;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import java.util.HashSet;
import java.util.Properties;

public class MessageAdapterImpl implements MessageAdapter{

    private MessageService messageService;
    private Producer producer;
    private String partition ;
    private Configuration configuration;
    private HashSet<String> pool;
    private String writeTopic;
    private String apiKey;
    private String apiSecret;

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(MessageAdapterImpl.class);

    /**
     * Initialize producer client to post messages using configuration properties
     */
    @Override
    public void init(){
        this.producer = getProducer();
    }

    private Producer getProducer() {
        configuration = ConfigurationFactory.getConfiguration();
        Properties properties=configuration.getProperties();
        updateProperties(properties);
        
        BundleContext ctx = FrameworkUtil.getBundle(MessageAdapterImpl.class).getBundleContext();
        if (ctx != null) {
        	ServiceReference svcRef = ctx.getServiceReference(MessageAdapterFactory.class.getName());
        	if (svcRef != null) {
        		producer = ((MessageAdapterFactory) ctx.getService(svcRef)).createProducer(pool, writeTopic,apiKey, apiSecret);
        	}
        }
        return producer;
    }


    private void updateProperties(Properties props) {
        if (logger.isTraceEnabled()) {
            logger.trace("Entering to updateProperties with Properties = "+ ObjectUtils.toString(props));
        }
        pool = new HashSet<>();
        if (props != null) {
            // readTopic = props.getProperty("dmaap.topic.read");
            writeTopic = props.getProperty("appc.LCM.topic.write");
            apiKey = props.getProperty("appc.LCM.client.key");
            apiSecret = props.getProperty("appc.LCM.client.secret");
            messageService = MessageService.parse(props.getProperty("message.service.type"));
            //  READ_TIMEOUT = Integer.valueOf(props.getProperty("dmaap.topic.read.timeout", String.valueOf(READ_TIMEOUT)));
            String hostnames = props.getProperty("appc.LCM.poolMembers");
            if (hostnames != null && !hostnames.isEmpty()) {
                for (String name : hostnames.split(",")) {
                    pool.add(name);
                }
            }
        }
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

        String jsonMessage;
        try {
            jsonMessage = Converter.convAsyncResponseToDmaapOutgoingMessageJsonString(operation, rpcName, asyncResponse);
            if (logger.isDebugEnabled()) {
                logger.debug("DMaaP Response = " + jsonMessage);
            }
            success  = producer.post(this.partition, jsonMessage);
        } catch (JsonProcessingException e1) {
            logger.error("Error generating Json from DMaaP message "+ e1.getMessage());
            success= false;
        }catch (Exception e){
            logger.error("Error sending message to DMaaP "+e.getMessage());
            success= false;
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Exiting from post with (success = "+ ObjectUtils.toString(success)+")");
        }
        return success;
    }
}
