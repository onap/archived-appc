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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/** Responsible to ensure synchronous handling of responses and timouts.
 */
class TaskQueue implements Runnable{

    private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
    private final EELFLogger LOG = EELFManager.getInstance().getLogger(TaskQueue.class);

    private boolean isShutdown;

    synchronized void addTask(Runnable task) throws InterruptedException {
            queue.put(task);
    }

    public void run() {
        Runnable task;
        while(!Thread.currentThread().isInterrupted() && !isShutdown){
            try {
                task = queue.take();
                task.run();
            } catch (InterruptedException e) {
                LOG.error("could not take task from queue", e);
            } catch (RuntimeException e) {
                LOG.error("could not run task", e);
            }
            LOG.info("THR# <" + Thread.currentThread().getId() + "> shutdown indicator " + isShutdown);
        }
        LOG.info("THR# <" + Thread.currentThread().getId() + "> in shutdown process.");
    }

    void stopQueue(){
        isShutdown = true;
    }
}
