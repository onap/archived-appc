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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DmaapUtil {
    private final static String delimiter = "_";
    private static String createPreferredRouteFileIfNotExist(String topic) throws IOException {
        String topicPreferredRouteFileName = null;
        topicPreferredRouteFileName = topic+"preferredRoute.properties";
        File fo= new File(topicPreferredRouteFileName);
        if(!fo.exists()) {
            ClassLoader classLoader = DmaapUtil.class.getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream("preferredRoute.txt");
            Properties props = new Properties();
            props.load(inputStream);
            String fileName = topic != null ? topic+delimiter+"MR1" : delimiter+"MR1";
            props.setProperty("preferredRouteKey", fileName);
            topicPreferredRouteFileName = topic + "preferredRoute.properties";
            props.store(new FileOutputStream(topicPreferredRouteFileName), "preferredRoute.properties file created on the fly for topic:" + topic + " on:" + System.currentTimeMillis());
        }
        return topicPreferredRouteFileName;
    }

    public static String createConsumerPropFile(String topic, Properties props)throws IOException {
        String defaultProfFileName = "consumer.properties";
        String topicConsumerPropFileName = createConsumerProducerPropFile(topic, defaultProfFileName,props);
        return topicConsumerPropFileName;
    }

    public static String createProducerPropFile(String topic, Properties props)throws IOException {
        String defaultProfFileName = "producer.properties";
        String topicConsumerPropFileName = createConsumerProducerPropFile(topic, defaultProfFileName,props);
        return topicConsumerPropFileName;
    }

    private static String createConsumerProducerPropFile(String topic, String defaultProfFileName, Properties props) throws IOException {
        ClassLoader classLoader = DmaapUtil.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(defaultProfFileName);
        Properties defaultProps = new Properties();
        defaultProps.load(inputStream);
        defaultProps.setProperty("topic",topic);

        String preferredRouteFileName = DmaapUtil.createPreferredRouteFileIfNotExist(topic);
        if(props != null && !props.isEmpty()){
            defaultProps.putAll(props);
        }
        defaultProps.setProperty("topic",topic);
        defaultProps.setProperty("DME2preferredRouterFilePath",preferredRouteFileName);
        String id = defaultProps.getProperty("id");
        String topicConsumerPropFileName = defaultProfFileName;
        topicConsumerPropFileName = id != null ? id+delimiter+topicConsumerPropFileName : delimiter+topicConsumerPropFileName;
        topicConsumerPropFileName = topic != null ? topic+delimiter+topicConsumerPropFileName : delimiter+topicConsumerPropFileName;

        defaultProps.store(new FileOutputStream(topicConsumerPropFileName), defaultProfFileName+" file created on the fly for topic:"+topic+" on:"+System.currentTimeMillis());
        return topicConsumerPropFileName;
    }

}
