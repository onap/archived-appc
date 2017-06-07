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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.openecomp.appc.util.StringHelper;

/**
 * This class is used to synchronize signaling of status between threads.
 * <p>
 * In complex multi-threaded applications it is often necessary to synchronize operations between threads. This is
 * especially true in complex algorithms where processing dependencies exist between different threads and the
 * synchronization of the operations of those threads is required. This class is a framework to enable multi-thread
 * signaling and wait/post logic that makes the thread synchronization easier.
 * </p>
 * <p>
 * Basically, in thread synchronization, one thread is the "waiter" and one or more other threads are the "notifiers".
 * The notifiers send signals to the waiter to inform that thread that certain conditions are true, processing has been
 * completed, or to inform the waiter of the state of the other thread(s). In the basic java framework, the waiter and
 * notifier are simply using the wait/notify mechanism provided, which does not allow for different conditions, state,
 * or "signals" to exist. The wait/notify mechanism, in combination with the object mutex, provides basic blocking and
 * releasing of a thread's dispatching state.
 * </p>
 * <p>
 * This class builds upon the java wait/notify mechanism and allows for "signals" to be defined. These signals are
 * simply string constants that mean something to the waiter and notifier threads. Any number of signals may be defined,
 * and it is possible to wait for more than one signal to be received, wait for any one of a set to be received, or to
 * test if a signal has been received without blocking.
 * </p>
 * <p>
 * Some operations are blocking operations. These stop the execution of the calling thread until the specified condition
 * is true. These blocking methods are all named "wait...", such as {@link #waitFor(String...)} and
 * {@link #waitForAny(String...)}. The thread making the call to these blocking methods MUST be the waiter thread (the
 * thread registered with the signal object).
 * </p>
 * <p>
 * Some operations are non-blocking. These operations allow for the testing or setting of signal conditions and do not
 * block the caller. When calling these methods ({@link #isSignaled(String)}, {@link #signal(String)}, and
 * {@link #setTimeout(long)} the waiter thread mutex will be held and may block the waiter thread for the duration of
 * the method call.
 * </p>
 */
public class Signal {

    /**
     * The thread must be the thread of the waiter that is waiting for the signals to be received. It is the recipient
     * of the signaled condition. This allows any number of other threads to send signals to the recipient and have the
     * recipient synchronize its operation with the receipt of the appropriate signal(s).
     */
    private Thread thread;

    /**
     * The amount of time to wait for a signal to be receieved. Set to zero to wait forever.
     */
    private long timeout = 0L;

    /**
     * The collection of all received signals. Note, this need not be a synchronized collection because it will always
     * be accessed while holding the mutex of the thread, therefore it is implicitly synchronized.
     */
    private List<String> receivedSignals;

    /**
     * A signal object must access a thread that is waiting for the receipt of the signal(s).
     */
    public Signal(Thread thread) {
        this.thread = thread;
        receivedSignals = new ArrayList<String>();
    }

    /**
     * Checks the waiter to see if it has been signaled
     * 
     * @param signal
     *            The signal to check for
     * @return True if the signal has been received, false otherwise
     */
    public boolean isSignaled(String signal) {
        synchronized (thread) {
            return _signaled(signal);
        }
    }

    /**
     * Sends the indicated signal to the waiter.
     * 
     * @param signal
     *            The signal that is to be sent to the waiting thread and to notify it to process the signal.
     */
    public void signal(String signal) {
        synchronized (thread) {
            if (!_signaled(signal)) {
                receivedSignals.add(signal);
            }
            thread.notify();
        }
    }

    /**
     * Blocks the waiting thread until all of the indicated signals have been received, or the wait times out.
     * 
     * @param signals
     *            The signals to be received. The waiter is blocked forever or until all of the signals are received.
     * @throws TimeoutException
     *             If the wait has timed out waiting for a response
     */
    public void waitFor(String... signals) throws TimeoutException {
        long limit = System.currentTimeMillis() + timeout;
        synchronized (thread) {
            while (true) {
                boolean complete = true;
                for (String signal : signals) {
                    if (!_signaled(signal)) {
                        complete = false;
                    }
                }

                if (complete) {
                    receivedSignals.removeAll(Arrays.asList(signals));
                    return;
                }

                if (timeout > 0) {
                    if (System.currentTimeMillis() > limit) {
                        throw new TimeoutException(String.format("Signals %s not received in the allotted timeout.",
                            StringHelper.asList(signals)));
                    }
                }

                try {
                    thread.wait(timeout);
                } catch (InterruptedException e) {
                    /*
                     * Interrupted exceptions are ignored
                     */
                }
            }
        }
    }

    /**
     * This method blocks the waiter until at least one of the indicated signals have been received.
     * 
     * @param signals
     *            A list of signals, any one of which will satisfy the wait condition
     * @return The signal that satisfied the wait
     * @throws TimeoutException
     *             If none of the signals have been received within the allotted time
     */
    public String waitForAny(String... signals) throws TimeoutException {
        long limit = System.currentTimeMillis() + timeout;
        synchronized (thread) {
            while (true) {
                for (String signal : signals) {
                    if (!_signaled(signal)) {
                        receivedSignals.remove(signal);
                        return signal;
                    }
                }

                if (timeout > 0) {
                    if (System.currentTimeMillis() > limit) {
                        throw new TimeoutException(
                            String.format("One of signals \"%s\" not received in the allotted timeout.",
                                StringHelper.asList(signals)));
                    }
                }

                try {
                    thread.wait(timeout);
                } catch (InterruptedException e) {
                    /*
                     * Interrupted exceptions are ignored
                     */
                }
            }
        }
    }

    /**
     * This private method is used to handle the check for signaled status. Note that this method assumes the caller
     * holds the thread mutex.
     * 
     * @param signals
     *            The list of signals to check for
     * @return True if any one of the signals has been received.
     */
    private boolean _signaled(String... signals) {
        for (String signal : signals) {
            if (receivedSignals.contains(signal)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets the timeout value for waiting for signals to be received
     * 
     * @param timeout
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}
