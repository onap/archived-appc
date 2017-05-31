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

package org.openecomp.appc.listener;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Properties;
import java.util.concurrent.ThreadPoolExecutor;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.appc.listener.AbstractListener;
import org.openecomp.appc.listener.ListenerProperties;

public class TestAbstractListener {

    private class DummyListener extends AbstractListener {
        public DummyListener(ListenerProperties props) {
            super(props);
        }

        public boolean getRun() {
            return run.get();
        }

        public ThreadPoolExecutor getExecutor() {
            return executor;
        }
    }

    private DummyListener listener;
    private ListenerProperties props;

    @Before
    public void setup() throws Exception {
        Properties regularProps = new Properties();
        regularProps.load(getClass().getResourceAsStream("/org/openecomp/appc/default.properties"));
        props = new ListenerProperties("", regularProps);
        listener = new DummyListener(props);
    }

    @Test
    public void testRun() {
        Thread t = new Thread(listener);
        t.run();
        assertFalse(t.isAlive()); // Should die immediately
    }

    @Test
    public void testStop() {
        listener.stop();
        assertFalse(listener.getRun());
        assertTrue(listener.getExecutor().isShutdown());
    }

    @Test
    public void testStopNow() {
        listener.stopNow();
        assertFalse(listener.getRun());
        assertTrue(listener.getExecutor().isShutdown());
    }

    @Test
    public void testBenchmark() {
        String out = listener.getBenchmark();
        assertNotNull(out);
        assertTrue(out.contains(listener.getListenerId()));
    }

    @Test
    public void testListenerId() {
        assertEquals(props.getPrefix(), listener.getListenerId());
        listener.setListenerId("newId");
        assertEquals("newId", listener.getListenerId());
    }
}
