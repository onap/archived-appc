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

package org.openecomp.appc.concurrent;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeoutException;

import org.junit.Test;
import org.openecomp.appc.concurrent.Signal;

public class TestSignal {

    private static final DateFormat formatter = new SimpleDateFormat("HH:mm:ss.SSS");
    public static final String SIGNAL_READY = "READY";
    public static final String SIGNAL_SHUTDOWN = "SHUTDOWN";

    @Test
    public void TestSimpleSignal() throws TimeoutException {

        Signal mySignal = new Signal(Thread.currentThread());
        mySignal.setTimeout(5000L);
        Fred fred = new Fred(mySignal);
        Thread t1 = new Thread(fred);

        /*
         * Verify that fred is dead, then start him and wait for him to signal us he is ready to proceed
         */
        assertFalse(t1.isAlive());
        t1.start();
        System.out.println(formatter.format(new Date()) + " MAIN: Waiting for Ready...");
        mySignal.waitFor(SIGNAL_READY);
        System.out.println(formatter.format(new Date()) + " MAIN: Signal Ready received");

        /*
         * Verify that fred is still alive and we will sleep for a while (simulate doing things)
         */
        assertTrue(t1.isAlive());
        try {
            Thread.sleep(250L);
        } catch (InterruptedException e) {
            // ignored
        }

        /*
         * Verify that fred is still alive and signal him to shutdown
         */
        assertTrue(t1.isAlive());
        System.out.println(formatter.format(new Date()) + " MAIN: Signaling shutdown");
        fred.getSignal().signal(SIGNAL_SHUTDOWN);

        /*
         * Wait a little bit
         */
        try {
            Thread.sleep(250L);
        } catch (InterruptedException e) {
            // ignored
        }

        /*
         * Verify that fred is dead now and that he completed normally
         */
        System.out.println(formatter.format(new Date()) + " MAIN: Shutting down...");
        assertFalse(t1.isAlive());
        assertTrue(fred.isCompleted());
    }

    public class Fred implements Runnable {
        private Signal signal;
        private Signal parentSignal;
        private boolean completed = false;

        public Fred(Signal parentSignal) {
            this.parentSignal = parentSignal;
        }

        @Override
        public void run() {
            signal = new Signal(Thread.currentThread());
            signal.setTimeout(5000L);
            try {
                Thread.sleep(250L);
            } catch (InterruptedException e) {
                // Ignore
            }

            System.out.println(formatter.format(new Date()) + " FRED: Signaling ready...");
            parentSignal.signal(SIGNAL_READY);

            try {
                System.out.println(formatter.format(new Date()) + " FRED: Waiting for shutdown...");
                signal.waitFor(SIGNAL_SHUTDOWN);
                System.out.println(formatter.format(new Date()) + " FRED: Received shutdown");
                completed = true;
            } catch (TimeoutException e) {
                e.printStackTrace();
            }
        }

        public boolean isCompleted() {
            return completed;
        }

        public Signal getSignal() {
            return signal;
        }
    }
}
