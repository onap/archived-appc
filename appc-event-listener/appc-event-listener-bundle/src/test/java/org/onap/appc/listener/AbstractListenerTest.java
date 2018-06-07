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

package org.onap.appc.listener;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.onap.appc.listener.AbstractListener;
import org.onap.appc.listener.ListenerProperties;
import sun.awt.windows.ThemeReader;

public class AbstractListenerTest {

    private DummyListener listener;
    private ListenerProperties props;

    @Before
    public void setup() throws Exception {
        Properties regularProps = new Properties();
        regularProps.load(getClass().getResourceAsStream("/org/onap/appc/default.properties"));
        props = new ListenerProperties("", regularProps);
        listener = new DummyListener(props);
    }

    @Test
    public void stop_should_shutdown_executor() {

        EventHandler mockEventHandler = mock(EventHandler.class);
        listener.setEventHandler(mockEventHandler);

        Thread thread = new Thread(listener);
        thread.start();

        assertTrue(thread.isAlive());
        assertTrue(listener.getRun());
        assertFalse(listener.getExecutor().isShutdown());
        assertFalse(listener.getExecutor().isTerminated());

        listener.stop();

        assertFalse(listener.getRun());
        assertTrue(listener.getExecutor().isShutdown());
        assertTrue(listener.getExecutor().isTerminated());

        verify(mockEventHandler).closeClients();

    }

    @Test
    public void stopNow_should_clear_executors_queue_and_call_stop() throws InterruptedException {
        EventHandler mockEventHandler = mock(EventHandler.class);
        listener.setEventHandler(mockEventHandler);

        ThreadPoolExecutor mockExecutor = mock(ThreadPoolExecutor.class);
        BlockingQueue<Runnable> mockBlockingQueue = mock(BlockingQueue.class);
        listener.setExecutor(mockExecutor);
        when(mockExecutor.getQueue()).thenReturn(mockBlockingQueue);

        Thread thread = new Thread(listener);
        thread.start();

        assertTrue(thread.isAlive());
        assertTrue(listener.getRun());

        listener.stopNow();

        assertFalse(listener.getRun());
        verify(mockExecutor).shutdown();
        verify(mockExecutor).awaitTermination(anyLong(), any(TimeUnit.class));
        verify(mockBlockingQueue).clear();
        verify(mockEventHandler).closeClients();
    }

    @Test
    public void getBenchmark_result_should_contain_listenerId() {
        String out = listener.getBenchmark();
        assertNotNull(out);
        assertTrue(out.contains(listener.getListenerId()));
    }

    @Test
    public void getListenerId_should_return_properties_prefix_by_default() {
        assertEquals(props.getPrefix(), listener.getListenerId());
        listener.setListenerId("newId");
        assertEquals("newId", listener.getListenerId());
    }


    private class DummyListener extends AbstractListener {

        DummyListener(ListenerProperties props) {
            super(props);
        }

        boolean getRun() {
            return run.get();
        }

        public ThreadPoolExecutor getExecutor() {
            return executor;
        }

        void setEventHandler(EventHandler eventHandler){
            dmaap = eventHandler;
        }

        void setExecutor(ThreadPoolExecutor executor){
            this.executor = executor;
        }

        @Override
        public void run() {

            while (run.get()) {
            }
        }
    }
}
