/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.appc.client.impl.core;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

import java.util.List;
import java.util.concurrent.*;

class TimerServiceImpl implements ITimerService {

    private final EELFLogger LOG = EELFManager.getInstance().getLogger(TimerServiceImpl.class);
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final ConcurrentHashMap<String, Future> timeOutEvents = new ConcurrentHashMap<>();
    private final long responseTimeout;

    TimerServiceImpl(long responseTimeout) {
        this.responseTimeout = responseTimeout;
    }

    @Override
    public synchronized void cancel(String correlationID) {
        Future timeOutEvent = timeOutEvents.remove(correlationID);
        if (timeOutEvent != null){
            timeOutEvent.cancel(true);
        }
    }

    @Override
    public synchronized void add(String correlationID, ITimeoutHandler handler) {
        Future timeOutEvent = scheduler.schedule(new HandleTimeout(correlationID, handler), responseTimeout, TimeUnit.MILLISECONDS);
        timeOutEvents.put(correlationID, timeOutEvent);
    }

    @Override
    public void shutdown() {
        List<Runnable> listTask = scheduler.shutdownNow();
        LOG.info("the amount of tasks that never commenced execution " + listTask.size());
    }

    private class HandleTimeout implements Runnable {

        String correlationID;
        ITimeoutHandler handler;

        HandleTimeout(String correlationID, ITimeoutHandler handler) {
            this.correlationID = correlationID;
            this.handler = handler;
        }

        @Override
        public void run(){
            System.out.println("Timeout event of request " + correlationID);
            handler.onTimeout();
            timeOutEvents.remove(correlationID);
        }
    }

}
