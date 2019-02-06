/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications Copyright (C) 2019 Ericsson
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

package org.onap.appc.executionqueue.impl;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.onap.appc.executionqueue.helper.Util;
import org.onap.appc.executionqueue.impl.object.QueueMessage;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class QueueManager {

    private final EELFLogger logger = EELFManager.getInstance().getLogger(QueueManager.class);

    private ExecutorService messageExecutor;
    private int maxThreadSize;
    private int maxQueueSize;
    private Util executionQueueUtil;

    public QueueManager() {

    }

    /**
     * Initialization method used by blueprint
     */
    public void init() {
        maxThreadSize = executionQueueUtil.getThreadPoolSize();
        maxQueueSize = executionQueueUtil.getExecutionQueueSize();
        messageExecutor = new ThreadPoolExecutor(
            maxThreadSize,
            maxThreadSize,
            0L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue(maxQueueSize),
            executionQueueUtil.getThreadFactory(true, "appc-dispatcher"),
            new ThreadPoolExecutor.AbortPolicy());
    }

    /**
     * Destory method used by blueprint
     */
    public void stop() {
        // Disable new tasks from being submitted
        messageExecutor.shutdown();
        List<Runnable> rejectedRunnables = messageExecutor.shutdownNow();
        logger.info(String.format("Rejected %d waiting tasks include ", rejectedRunnables.size()));

        try {
            messageExecutor.shutdownNow(); // Cancel currently executing tasks
            // Wait a while for tasks to respond to being cancelled
            while (!messageExecutor.awaitTermination(100, TimeUnit.MILLISECONDS)) {
                logger.debug("QueueManager is being shut down because it still has threads not interrupted");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            messageExecutor.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Injected by blueprint
     *
     * @param executionQueueUtil
     */
    public void setExecutionQueueUtil(Util executionQueueUtil) {
        this.executionQueueUtil = executionQueueUtil;
    }

    public boolean enqueueTask(QueueMessage queueMessage) {
        boolean isEnqueued = true;
        try {
            messageExecutor.execute(() -> queueMessage.getMessage().run());
        } catch (RejectedExecutionException ree) {
            isEnqueued = false;
        }

        return isEnqueued;
    }
}
