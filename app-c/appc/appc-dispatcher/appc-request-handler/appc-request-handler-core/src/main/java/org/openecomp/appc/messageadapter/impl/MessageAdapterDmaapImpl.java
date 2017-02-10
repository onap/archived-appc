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

package org.openecomp.appc.messageadapter.impl;

import java.util.HashSet;
import java.util.Properties;

import org.apache.commons.lang.ObjectUtils;
import org.openecomp.appc.adapter.dmaap.Producer;
import org.openecomp.appc.adapter.dmaap.DmaapProducer;
import org.openecomp.appc.configuration.Configuration;
import org.openecomp.appc.configuration.ConfigurationFactory;
import org.openecomp.appc.domainmodel.lcm.ResponseContext;
import org.openecomp.appc.domainmodel.lcm.VNFOperation;
import org.openecomp.appc.messageadapter.MessageAdapter;
import org.openecomp.appc.requesthandler.conv.Converter;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

import com.fasterxml.jackson.core.JsonProcessingException;

public class MessageAdapterDmaapImpl implements MessageAdapter{

    private Producer dmaapProducer;
    private String partition ;
    private Configuration configuration;
    private HashSet<String> pool;
    private String writeTopic;
    private String apiKey;
    private String apiSecret;

    private Integer READ_TIMEOUT;

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(MessageAdapterDmaapImpl.class);

    /**
     * Initialize dmaapProducer client to post messages using configuration properties
     */
    @Override
    public void init(){
        this.dmaapProducer = getDmaapProducer();
    }
    private Producer getDmaapProducer() {
        configuration = ConfigurationFactory.getConfiguration();
        Properties properties=configuration.getProperties();
        updateProperties(properties);
        Producer producer=new DmaapProducer(pool,writeTopic);
        producer.updateCredentials(apiKey, apiSecret);
        return producer;
    }


    private void updateProperties(Properties props) {
        if (logger.isTraceEnabled()) {
            logger.trace("Entering to updateProperties with Properties = "+ ObjectUtils.toString(props));
        }
        pool = new HashSet<>();
        if (props != null) {
            // readTopic = props.getProperty("dmaap.topic.read");
            writeTopic = props.getProperty("dmaap.topic.write");
            apiKey = props.getProperty("dmaap.client.key");
            apiSecret = props.getProperty("dmaap.client.secret");
            /*          clientName = props.getProperty("dmaap.client.name", "APP-C");
            clientId = props.getProperty("dmaap.client.name.id", "0");
            filter_json = props.getProperty("dmaap.topic.read.filter");
             */
            //  READ_TIMEOUT = Integer.valueOf(props.getProperty("dmaap.topic.read.timeout", String.valueOf(READ_TIMEOUT)));
            String hostnames = props.getProperty("dmaap.poolMembers");
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
            success  = dmaapProducer.post(this.partition, jsonMessage);
        } catch (JsonProcessingException e1) {
            logger.error("Error generating Jason from DMaaP message "+ e1.getMessage());
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
