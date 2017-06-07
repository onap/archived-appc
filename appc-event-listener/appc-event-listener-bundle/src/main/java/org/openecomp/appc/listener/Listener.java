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

/**
 * This interface defines a listener that subscribes to a DMaaP topic and continually polls for messages. The
 * listener does all operations in the run() method and long running operations should be created in a separate worker
 * thread.
 *
 */
public interface Listener extends Runnable {

    /**
     * Should start a continuous poll to get messages from the message bus only ending when stop() or stopNow() are
     * called.
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run();

    /**
     * Signals the listener to stop accepting new messages to the queue and to cleanly finish processing all remaining
     * messages in the queue. This can take a significant amount of time to complete depending on the thread pool
     * characteristics. Similar to {@link #stopNow()}
     */
    public void stop();

    /**
     * Signals the listener to stop accepting new messages to the queue and to destroy all remaining messages in the
     * queue. This will complete quicker than {@link #stop()} at the cost of discarded requests. Recovery of these
     * requests would have to be caught downstream. Similar to {@link #stop()}
     */
    public void stopNow();

    /**
     * @return A string that shows various benchmarking data. Can be used by humans to tune the thread pool.
     */
    public String getBenchmark();

    /**
     * @return The listener's id when requesting messages from DMaaP. Also known as the group id.
     */
    public String getListenerId();

    /**
     * Sets the listener's id to use when requesting messages from DMaaP. Also known as the group id.
     * 
     * @param idString
     *            The new listener id
     */
    public void setListenerId(String idString);
}
