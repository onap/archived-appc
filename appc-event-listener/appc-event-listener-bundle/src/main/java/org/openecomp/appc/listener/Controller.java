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

import java.util.Map;

/**
 * A controller is responsible for creating a listener for each ListenerProperties object that is passed in to it on
 * instantiation. The controller will create a thread pool that will contain all of the listener threads so no listener
 * can crash the controller.
 *
 */
public interface Controller {

    /**
     * Creates a new thread in the thread pool for an implementation of the {@see #Listener} class set in the
     * ListenerProperties. This thread is run immediately after it is created.
     */
    public void start();

    /**
     * Stops each of the listeners known by this controller. Takes an optional parameter that indicates the the listener
     * should stop immediately rather than waiting for all threads to complete.
     *
     * @param stopNow
     *            Determines what method the listeners should use to shutdown. If true, listeners will use the stopNow()
     *            method. Otherwise they will use the stop() method.
     */
    public void stop(boolean stopNow);

    /**
     * @return A Map of ListenerProperties and the Listener object that is running in the controllers thread pool.
     */
    public Map<ListenerProperties, Listener> getListeners();

}
