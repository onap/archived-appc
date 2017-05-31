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

package org.openecomp.appc.listener.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.openecomp.appc.listener.Controller;
import org.openecomp.appc.listener.Listener;
import org.openecomp.appc.listener.ListenerProperties;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;


/**
 * A common implementation of a controller. This controller should not need to be modified to implement new listeners
 *
 */
public class ControllerImpl implements Controller {

    private final EELFLogger LOG = EELFManager.getInstance().getLogger(ControllerImpl.class);

    private int LISTENER_COUNT = 1;

    private Map<ListenerProperties, Listener> listeners = null;

    private ThreadPoolExecutor executor;

    /**
     * Creates a Controller with the set of listener properties which will be used to start listener threads.
     *
     * @param properties
     *            A non null Set of ListenerProperties
     */
    public ControllerImpl(Set<ListenerProperties> properties) {
        listeners = new HashMap<ListenerProperties, Listener>();
        for (ListenerProperties props : properties) {
            if (props.getClass() != null) {
                listeners.put(props, null);
            } else {
                LOG.error(String.format(
                    "The ListenerProperties %s has no Listener class associated with it and will not run.", props));
            }
        }

        LISTENER_COUNT = properties.size();

        executor = new ThreadPoolExecutor(LISTENER_COUNT, LISTENER_COUNT, 1, TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(LISTENER_COUNT));

        // Custom Named thread factory
        BasicThreadFactory threadFactory = new BasicThreadFactory.Builder().namingPattern("Appc-Listener-%d").build();
        executor.setThreadFactory(threadFactory);
    }

    @Override
    public void start() {
        LOG.info("Starting DMaaP Controller.");
        for (ListenerProperties props : listeners.keySet()) {
            try {
                if (props.isDisabled()) {
                    LOG.warn(String.format("The listener %s is disabled and will not be run", props.getPrefix()));
                } else {
                    Listener l = props.getListenerClass().getConstructor(ListenerProperties.class).newInstance(props);
                    l.setListenerId(props.getPrefix());
                    listeners.put(props, l);
                    executor.execute(l);
                }
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error(String.format("Exception while starting listener %s.", props), e);
            }
        }
    }

    @Override
    public void stop(boolean stopNow) {
        LOG.info("Stopping DMaaP Controller.");
        Iterator<Listener> itr = listeners.values().iterator();
        while (itr.hasNext()) {
            Listener l = itr.next();
            if (stopNow) {
                l.stopNow();
            } else {
                l.stop();
            }
            itr.remove();
        }
        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
        }
    }

    @Override
    public Map<ListenerProperties, Listener> getListeners() {
        return listeners;
    }
}
