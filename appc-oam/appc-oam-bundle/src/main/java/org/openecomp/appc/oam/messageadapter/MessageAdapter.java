/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.appc.oam.messageadapter;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang.ObjectUtils;
import org.openecomp.appc.adapter.message.MessageAdapterFactory;
import org.openecomp.appc.adapter.message.Producer;
import org.openecomp.appc.configuration.Configuration;
import org.openecomp.appc.configuration.ConfigurationFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import java.util.HashSet;
import java.util.Properties;

public class MessageAdapter {

    private final EELFLogger logger = EELFManager.getInstance().getLogger(MessageAdapter.class);

    private final String PROP_APPC_OAM_DISABLED = "appc.OAM.disabled";
    private final String PROP_APPC_OAM_TOPIC_WRITE = "appc.OAM.topic.write";
    private String PROP_APPC_OAM_CLIENT_KEY = "appc.OAM.client.key";
    private String PROP_APPC_OAM_CLIENT_SECRET = "appc.OAM.client.secret";
    private String PROP_APPC_OAM_POOLMEMBERS = "appc.OAM.poolMembers";

    private Producer producer;
    private String partition;
    private Configuration configuration;
    private HashSet<String> pool;
    private String writeTopic;
    private String apiKey;
    private String apiSecret;
    private boolean isDisabled;

    /**
     * Initialize producer client to post messages using configuration properties.
     */
    public void init() {
        configuration = ConfigurationFactory.getConfiguration();
        Properties properties = configuration.getProperties();
        updateProperties(properties);

        if (isAppcOamPropsListenerEnabled()) {
            createProducer();
        } else {
            logger.warn(String.format("The listener %s is disabled and will not be run", "appc.OAM"));
        }
    }

    /**
     * Create producer using MessageAdapterFactory which is found through bundle context.
     */
    void createProducer() {
        BundleContext ctx = FrameworkUtil.getBundle(MessageAdapter.class).getBundleContext();
        if (ctx == null) {
            logger.warn("MessageAdapter cannot create producer due to no bundle context.");
            return;
        }

        ServiceReference svcRef = ctx.getServiceReference(MessageAdapterFactory.class.getName());
        if (svcRef == null) {
            logger.warn("MessageAdapter cannot create producer due to no MessageAdapterFactory service reference.");
            return;
        }

        Producer localProducer = ((MessageAdapterFactory) ctx.getService(svcRef)).createProducer(pool, writeTopic,
                apiKey, apiSecret);

        for (String url : pool) {
            if (url.contains("3905") || url.contains("https")) {
                localProducer.useHttps(true);
                break;
            }
        }

        producer = localProducer;

        logger.debug("MessageAdapter created producer.");
    }

    /**
     * Read property value to set writeTopic, apiKey, apiSecret and pool.
     *
     * @param props of configuration
     */
    private void updateProperties(Properties props) {
        logger.trace("Entering to updateProperties with Properties = " + ObjectUtils.toString(props));

        pool = new HashSet<>();
        if (props != null) {
            isDisabled = Boolean.parseBoolean(props.getProperty(PROP_APPC_OAM_DISABLED));
            writeTopic = props.getProperty(PROP_APPC_OAM_TOPIC_WRITE);
            apiKey = props.getProperty(PROP_APPC_OAM_CLIENT_KEY);
            apiSecret = props.getProperty(PROP_APPC_OAM_CLIENT_SECRET);
            String hostnames = props.getProperty(PROP_APPC_OAM_POOLMEMBERS);
            if (hostnames != null && !hostnames.isEmpty()) {
                for (String name : hostnames.split(",")) {
                    pool.add(name);
                }
            }
        }
    }

    /**
     * Get producer. If it is null, call createProducer to create it again.
     *
     * @return Producer
     */
    Producer getProducer() {
        if (producer == null) {
            // In case, producer was not properly set yet, set it again.
            logger.info("Calling createProducer as producer is null.");
            createProducer();
        }

        return producer;
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

            Producer myProducer = getProducer();
            success = myProducer != null && myProducer.post(this.partition, jsonMessage);
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
