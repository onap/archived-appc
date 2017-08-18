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

package org.openecomp.appc.executionqueue.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.openecomp.appc.executionqueue.MessageExpirationListener;
import org.openecomp.appc.executionqueue.helper.Util;
import org.openecomp.appc.executionqueue.impl.object.QueueMessage;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class QueueManager {

    private final EELFLogger logger = EELFManager.getInstance().getLogger(QueueManager.class);

    private LinkedBlockingQueue<QueueMessage> queue;
    private MessageExpirationListener listener;
    private ExecutorService messageExecutor;
    private int max_thread_size;
    private int max_queue_size;
    private Util executionQueueUtil;

    public QueueManager() {

    }

    /**
     * Initialization method used by blueprint
     */
    public void init() {
        max_thread_size = executionQueueUtil.getThreadPoolSize();
        max_queue_size = executionQueueUtil.getExecutionQueueSize();
        messageExecutor = new ThreadPoolExecutor(
            max_thread_size,
            max_thread_size,
            0L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue(max_queue_size),
            executionQueueUtil.getThreadFactory(true, "appc-dispatcher"),
            new ThreadPoolExecutor.AbortPolicy());
    }

    /**
     * Destory method used by blueprint
     */
    public void stop() {
        messageExecutor.shutdownNow();
    }

    public void setListener(MessageExpirationListener listener) {
        this.listener = listener;
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
            messageExecutor.execute(() -> {
                try {
                    if (queueMessage.isExpired()) {
                        logger.debug("Message expired " + queueMessage.getMessage());
                        if (listener != null) {
                            listener.onMessageExpiration(queueMessage.getMessage());
                        } else {
                            logger.warn("Listener not available for expired message ");
                        }
                    } else {
                        queueMessage.getMessage().run();
                    }
                } catch (Exception e) {
                    logger.error("Error in startMessagePolling method of ExecutionQueueServiceImpl: " + e.getMessage());
                }
            });
        } catch (RejectedExecutionException ree) {
            isEnqueued = false;
        }

        return isEnqueued;
    }
}
