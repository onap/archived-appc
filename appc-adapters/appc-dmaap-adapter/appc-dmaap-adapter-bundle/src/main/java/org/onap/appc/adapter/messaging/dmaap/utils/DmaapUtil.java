/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.appc.adapter.messaging.dmaap.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.onap.appc.configuration.Configuration;
import org.onap.appc.configuration.ConfigurationFactory;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class DmaapUtil {

    private static final char   DELIMITER             = '_';

    static final String         DMAAP_PROPERTIES_PATH = "org.onap.appc.dmaap.profile.path";

    private static final EELFLogger log                = EELFManager.getInstance().getLogger(DmaapUtil.class);

    private DmaapUtil() {
    }

    private static String createPreferredRouteFileIfNotExist(String topic) throws IOException {
        String topicPreferredRouteFileName;
        topicPreferredRouteFileName = topic + "preferredRoute.properties";
        File fo = new File(topicPreferredRouteFileName);
        if (!fo.exists()) {
            ClassLoader classLoader = DmaapUtil.class.getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream("preferredRoute.txt");
            Properties props = new Properties();
            props.load(inputStream);
            String fileName = topic != null ? topic + DELIMITER + "MR1" : DELIMITER + "MR1";
            props.setProperty("preferredRouteKey", fileName);
            topicPreferredRouteFileName = topic + "preferredRoute.properties";
            props.store(new FileOutputStream(topicPreferredRouteFileName),
                    "preferredRoute.properties file created on the fly for topic:" + topic + " on:"
                            + System.currentTimeMillis());
        }
        return topicPreferredRouteFileName;
    }

    public static String createConsumerPropFile(String topic, Properties props) throws IOException {
        String defaultProfFileName = "consumer.properties";

        log.debug("Creating DMaaP Consumer Property File for topic " + topic);
        return createConsumerProducerPropFile(topic, defaultProfFileName, props);
    }

    public static String createProducerPropFile(String topic, Properties props) throws IOException {
        String defaultProfFileName = "producer.properties";

        log.debug("Creating DMaaP Producer Property File for topic " + topic);
        return createConsumerProducerPropFile(topic, defaultProfFileName, props);
    }

    private static String createConsumerProducerPropFile(String topic, String defaultProfFileName, Properties props)
            throws IOException {
        Properties defaultProps = getDefaultProperties(defaultProfFileName);

        defaultProps.setProperty("topic", topic);

        String preferredRouteFileName = DmaapUtil.createPreferredRouteFileIfNotExist(topic);
        if (props != null && !props.isEmpty()) {
            defaultProps.putAll(props);
        }
        defaultProps.setProperty("topic", topic);
        defaultProps.setProperty("DME2preferredRouterFilePath", preferredRouteFileName);
        String id = defaultProps.getProperty("id");
        String topicConsumerPropFileName = defaultProfFileName;
        topicConsumerPropFileName = id != null ? id + DELIMITER + topicConsumerPropFileName
                : DELIMITER + topicConsumerPropFileName;
        topicConsumerPropFileName = topic != null ? topic + DELIMITER + topicConsumerPropFileName
                : DELIMITER + topicConsumerPropFileName;

        defaultProps.store(new FileOutputStream(topicConsumerPropFileName), defaultProfFileName
                + " file created on the fly for topic:" + topic + " on:" + System.currentTimeMillis());
        return topicConsumerPropFileName;
    }

    private static Properties getDefaultProperties(String profileName) {
        Properties props = new Properties();

        // use appc configuration to get all properties which includes
        // appc.properties and system properties
        // allowing variable to be set in any location
        Configuration config = ConfigurationFactory.getConfiguration();
        String dmaapPropPath = config.getProperty(DMAAP_PROPERTIES_PATH);

        if (dmaapPropPath != null) {
            // load from file system

            File profileFile = new File(dmaapPropPath, profileName);
            FileInputStream inputStream = null;

            log.info("Loading DMaaP Profile from " + profileFile.getAbsolutePath());

            if (profileFile.exists()) {
                try {
                    inputStream = new FileInputStream(profileFile);
                    props.load(inputStream);
                } catch (IOException e) {
                    log.error("Exception loading DMaaP Profile from " + profileFile.getAbsolutePath(), e);
                } finally {
                    try {
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (IOException ex) {
                        log.warn("Exception closing DMaaP Profile file " + profileFile.getAbsolutePath(), ex);
                    }
                }
            }
        }
        if (props.isEmpty()) {
            // load default Profile from class
            log.info("Loading Default DMaaP Profile");

            ClassLoader classLoader = DmaapUtil.class.getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream(profileName);
            try {
                props.load(inputStream);
            } catch (IOException e) {
                log.error("Exception loading Default DMaaP Profile", e);
            }
        }

        return props;
    }
}
