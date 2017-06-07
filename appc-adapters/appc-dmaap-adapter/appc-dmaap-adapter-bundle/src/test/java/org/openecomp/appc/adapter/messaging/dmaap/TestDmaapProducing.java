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

package org.openecomp.appc.adapter.messaging.dmaap;


import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.appc.adapter.message.Producer;
import org.openecomp.appc.adapter.messaging.dmaap.http.HttpDmaapProducerImpl;
import org.openecomp.appc.adapter.messaging.dmaap.impl.DmaapProducerImpl;
import org.openecomp.appc.configuration.Configuration;
import org.openecomp.appc.configuration.ConfigurationFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Must have a DMaaP cluster or simulator up and running
 * Update the hostname, topic, client properties in
 * resources/org/openecomp/appc/default.properties
 *
 */
public class TestDmaapProducing {

    private static Producer httpProducer;
    private static Producer dmaapProducer;

    @BeforeClass
    public static void setUp() {

        Configuration configuration = ConfigurationFactory.getConfiguration();

        List<String> hosts = Arrays.asList(configuration.getProperty("poolMembers").split(","));
        String topic = configuration.getProperty("topic.write");
        String user = configuration.getProperty("dmaap.appc.username");
        String password = configuration.getProperty("dmaap.appc.password");

        dmaapProducer = new DmaapProducerImpl(hosts, topic,user,password);
        httpProducer = new HttpDmaapProducerImpl(hosts, topic);
        httpProducer.updateCredentials(user,password);
    }

    @Test
    @Ignore
    public void testHttpPostMessage() {
        testPostMessage(httpProducer);
    }

    @Test
    @Ignore
    public void testPostMessages() {
        testPostMessage(dmaapProducer);
    }

    private void testPostMessage(Producer producer) {
        Assert.assertTrue(producer.post("partition", "{\"message\": \"Hello, world!\"}"));
    }

}
