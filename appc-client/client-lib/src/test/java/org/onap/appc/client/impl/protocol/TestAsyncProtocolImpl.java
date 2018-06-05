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

package org.onap.appc.client.impl.protocol;

import org.onap.appc.client.impl.core.MessageContext;
import org.onap.appc.client.impl.protocol.AsyncProtocol;
import org.onap.appc.client.impl.protocol.AsyncProtocolImpl;
import org.onap.appc.client.impl.protocol.ProtocolException;
import org.onap.appc.client.impl.protocol.RetrieveMessageCallback;
import org.onap.appc.client.impl.protocol.UEBPropertiesKeys;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestAsyncProtocolImpl {

    private static AsyncProtocol protocol;
    private static AtomicBoolean gotResponse;
    private static Properties props;

    private static class TestCallback implements RetrieveMessageCallback{

        public void onResponse(String payload, MessageContext context) {
            Assert.assertNotEquals(null, payload);
            Assert.assertNotEquals(null, context);
            protocol = null;
            gotResponse.set(true);
        }
    }

    @BeforeClass
    public static void setUp() throws IOException, ProtocolException {

        gotResponse = new AtomicBoolean(false);

        props = new Properties();
        String propFileName = "ueb.properties";

        InputStream input = TestAsyncProtocolImpl.class.getClassLoader().getResourceAsStream(propFileName);

        props.load(input);

        protocol = new AsyncProtocolImpl();
        protocol.init(props, new TestCallback());
    }

    public void testSendRequest() throws ProtocolException {

        MessageContext context = new MessageContext();
        context.setType("Test");

        protocol.sendRequest("{\"Test\":\"\"}", context);

        try {
            Long timeToSleep = Long.parseLong((String)props.get(UEBPropertiesKeys.TOPIC_READ_TIMEOUT))*2;
            Thread.sleep(timeToSleep);
        } catch (InterruptedException e) {
            Assert.assertFalse(e.getMessage(), false);
        }
        if (gotResponse.get() == false) {
            Assert.assertFalse("Message was not read !", true);
        }
    }
}
