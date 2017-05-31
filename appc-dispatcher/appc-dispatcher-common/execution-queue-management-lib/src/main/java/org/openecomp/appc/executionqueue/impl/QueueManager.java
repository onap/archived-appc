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

package org.openecomp.appc.executionqueue.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.openecomp.appc.executionqueue.MessageExpirationListener;
import org.openecomp.appc.executionqueue.helper.Util;
import org.openecomp.appc.executionqueue.impl.object.QueueMessage;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class QueueManager {

    private LinkedBlockingQueue<QueueMessage<? extends Runnable>> queue;

    private MessageExpirationListener listener;

    private static int MAX_QUEUE_SIZE = Util.getExecutionQueSize();

    private static int MAX_THREAD_SIZE = Util.getThreadPoolSize();

    private ExecutorService messageExecutor;

    private static final EELFLogger logger =
            EELFManager.getInstance().getLogger(QueueManager.class);

    private QueueManager(){
        init();
    }

    private static class QueueManagerHolder {
        private static final QueueManager INSTANCE = new QueueManager();
    }

    public static QueueManager getInstance() {
        return QueueManagerHolder.INSTANCE;
    }

    private void init(){
        queue = new LinkedBlockingQueue<QueueMessage<? extends Runnable>>(MAX_QUEUE_SIZE);
        messageExecutor = Executors.newFixedThreadPool(MAX_THREAD_SIZE,Util.getThreadFactory(true));

        for(int i=0;i<MAX_THREAD_SIZE;i++){
            messageExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    while (true){
                        try{
                            QueueMessage<? extends Runnable> queueMessage = queue.take();
                            if (queueMessage.isExpired()) {
                                logger.debug("Message expired "+ queueMessage.getMessage());
                                if(listener != null){
                                    listener.onMessageExpiration(queueMessage.getMessage());
                                }
                                else{
                                    logger.warn("Listener not available for expired message ");
                                }
                            }
                            else{
                                queueMessage.getMessage().run();
                            }
                        } catch (Exception e) {
                            logger.error("Error in startMessagePolling method of ExecutionQueueServiceImpl" + e.getMessage());
                        }
                    }
                }
            });
        }
    }

    public void setListener(MessageExpirationListener listener) {
        this.listener = listener;
    }

    public boolean enqueueTask(QueueMessage<? extends Runnable> queueMessage) {
        return queue.offer(queueMessage);
    }

}
