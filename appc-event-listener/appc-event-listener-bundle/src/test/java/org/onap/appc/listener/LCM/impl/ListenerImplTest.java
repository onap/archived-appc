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

package org.onap.appc.listener.LCM.impl;

import static org.junit.Assert.fail;

import java.util.Properties;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.onap.appc.listener.Listener;
import org.onap.appc.listener.ListenerProperties;
import org.onap.appc.listener.demo.impl.ListenerImpl;

@Ignore
public class ListenerImplTest {

    private static final String PROP_FILE = "/org/onap/appc/default.properties";

    private Listener listener;
    private Properties props;

    @Before
    public void setup() {
        props = new Properties();
        try {
            props.load(getClass().getResourceAsStream(PROP_FILE));
            props.setProperty("topic.read", "DCAE-CLOSED-LOOP-EVENTS-DEV1510SIM");
        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed to setup test: " + e.getMessage());
        }
        listener = new ListenerImpl(new ListenerProperties("appc.ClosedLoop", props));
    }

    @Test
    public void testRun() {
        try {
            Thread t = new Thread(listener);
            t.start();

            Thread.sleep(5000);

            listener.stopNow();

            System.out.println(listener.getBenchmark());

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void printSampleData() {
        try {
            props.setProperty("threads.queuesize.min", "1");
            props.setProperty("threads.queuesize.max", "1");
            props.setProperty("threads.poolsize.min", "1");
            props.setProperty("threads.poolsize.max", "1");

            Thread t = new Thread(listener);
            t.start();

            Thread.sleep(2000);

            listener.stop();

            System.out.println(listener.getBenchmark());

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}
