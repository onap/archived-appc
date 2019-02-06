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

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.executionqueue.ExecutionQueueService;
import org.onap.appc.executionqueue.impl.object.QueueMessage;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class ExecutionQueueServiceImpl<M extends Runnable> implements ExecutionQueueService<M> {

    private final EELFLogger logger = EELFManager.getInstance().getLogger(ExecutionQueueServiceImpl.class);

    private QueueManager queueManager;

    public ExecutionQueueServiceImpl(){

    }

    @Override
    public void putMessage(M message) throws APPCException {
         this.putMessage(message, -1, null);
    }

    /**
     * Injected by blueprint
     * @param queueManager
     */
    public void setQueueManager(QueueManager queueManager) {
        this.queueManager = queueManager;
    }

    @Override
    public void putMessage(M message, long timeout, TimeUnit unit) throws APPCException{
        QueueMessage queueMessage;

        try {
            Date expirationTime = calculateExpirationTime(timeout, unit);
            queueMessage = new QueueMessage(message,expirationTime);
            boolean enqueueTask = queueManager.enqueueTask(queueMessage);
            if(!enqueueTask){
                throw new APPCException("failed to put message in queue");
            }
        } catch (Exception e) {
            logger.error("Error in putMessage method of ExecutionQueueServiceImpl" + e.getMessage());
            throw new APPCException(e);
        }
    }

    private Date calculateExpirationTime(long timeToLive, TimeUnit unit) {
        Date expirationTime = null;
        if(timeToLive > 0){
            long currentTime = System.currentTimeMillis();
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(currentTime + unit.toMillis(timeToLive));
            expirationTime = cal.getTime();
        }
        return expirationTime;
    }

}
