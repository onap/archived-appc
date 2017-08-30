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
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** Creates a task queue pool that reuses a fixed number of threads.
 * Assigns one thread for each queue.
 */
class TaskQueueManager {
    private final EELFLogger LOG = EELFManager.getInstance().getLogger(TaskQueueManager.class);
    private ExecutorService executorService;
    private final static String DEFAULT_POOL_SIZE = "10";
    private final static String CLIENT_POOL_SIZE = "client.pool.size";
    private TaskQueue[] queues;
    private int poolInt;

    TaskQueueManager(Properties properties){
        String size = properties.getProperty(CLIENT_POOL_SIZE, DEFAULT_POOL_SIZE);
        poolInt = Integer.parseInt(size);
        this.executorService = Executors.newFixedThreadPool(poolInt);
        initTaskQueues();
    }

    private void initTaskQueues(){
        queues = new TaskQueue[poolInt];
        for(int i=0; i<poolInt; i++){
            queues[i] = new TaskQueue();
            this.executorService.submit(queues[i]);
        }
    }

    void submit(String corrID, Runnable task) throws InterruptedException {
        TaskQueue queue = getTaskQueue(corrID);
        queue.addTask(task);
    }

    /**
     * ensures synchronous handling all responses and timeout belongs to same correlation ID
     * @param corrID
     * @return - @{@link TaskQueue}
     */
    private TaskQueue getTaskQueue(String corrID){
        int index = Math.abs(corrID.hashCode()) % poolInt;
        return queues[index];
    }

    /**
     * goes over queues for stopping threads
     * @throws InterruptedException
     */
    void stopQueueManager() throws InterruptedException {
        for(int i=0; i<poolInt; i++){
            queues[i].stopQueue();
            queues[i].addTask(new Runnable() {
                @Override
                public void run() {
                    /**
                     * wake up the queue for stopping thread
                     */
                }
            });
        }
        List<Runnable> listTask = executorService.shutdownNow();
        if (!executorService.awaitTermination(6, TimeUnit.SECONDS))
            System.err.println("Pool did not terminate");
        LOG.info("the amount of tasks that never commenced execution " + listTask.size());
    }
}
