/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright 2018 AT&T
 * =================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.client.impl.core;

import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class TimerServiceImplTest {

    private static TimerServiceImpl ts = new TimerServiceImpl(100);
    private static TimeoutHandlerImpl handler1;
    private static TimeoutHandlerImpl handler2;
    private static TimeoutHandlerImpl handler3;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        handler1 = new TimeoutHandlerImpl();
        ts.add("1", handler1);
        ts.cancel("1");

        handler2 = new TimeoutHandlerImpl();
        ts.add("2", handler2);

        handler3 = new TimeoutHandlerImpl();
        ts.add("3", handler3);
    }
    
    @AfterClass
    public static void cleanupAfterClass() throws Exception {
        ts.shutdown();
    }

    @Test
    public void testCancelledTimer() {
        assertTrue("TimerServiceImpl cancel failed!", handler1.getLatch().getCount() == 1);
    }

    @Test
    public void testTimeoutActionPerformed() throws InterruptedException {
        handler2.getLatch().await(2, TimeUnit.SECONDS);
        assertTrue("TimerServiceImpl timeout action not performed!", handler2.getLatch().getCount() == 0);
    }

    @Test
    public void testLateCancel() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(300);
        ts.cancel("3");
        assertTrue("TimerServiceImpl late cancel - action unexpectedly performed!", handler3.getLatch().getCount() == 0);
    }
    
    private static class TimeoutHandlerImpl implements ITimeoutHandler {

        private CountDownLatch latch = new CountDownLatch(1);

        public CountDownLatch getLatch() {
            return latch;
        }

        /**
         * When a timeout event is occurring, the new Timeout task will be assigned into a queue,
         * this queue is shared between both timeout and handlers which belong to same correlation ID.
         */
        @Override
        public void onTimeout() {
            latch.countDown();
        }
    }


}
