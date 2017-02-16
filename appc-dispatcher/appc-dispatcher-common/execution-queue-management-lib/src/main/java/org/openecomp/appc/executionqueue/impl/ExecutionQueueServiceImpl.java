/*-
 * ============LICENSE_START=======================================================
 * openECOMP : APP-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 						reserved.
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
 */

package org.openecomp.appc.executionqueue.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.executionqueue.ExecutionQueueService;
import org.openecomp.appc.executionqueue.MessageExpirationListener;
import org.openecomp.appc.executionqueue.impl.object.QueueMessage;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class ExecutionQueueServiceImpl<M extends Runnable> implements ExecutionQueueService<M> {

    private static final EELFLogger logger =
            EELFManager.getInstance().getLogger(ExecutionQueueServiceImpl.class);

    ExecutionQueueServiceImpl(){

    }

    @Override
    public void putMessage(M message) throws APPCException {
         this.putMessage(message,-1,null);
    }

    @Override
    public void putMessage(M message, long timeout, TimeUnit unit) throws APPCException{
        QueueMessage queueMessage = null;

        try {
            Date expirationTime = calculateExpirationTime(timeout,unit);
            queueMessage = new QueueMessage(message,expirationTime);
            QueueManager queueManager = QueueManager.getInstance();
            boolean enqueueTask = queueManager.enqueueTask(queueMessage);
            if(!enqueueTask){
                throw new APPCException("failed to put message in queue");
            }
        } catch (Exception e) {
            logger.error("Error in putMessage method of ExecutionQueueServiceImpl" + e.getMessage());
            throw new APPCException(e);
        }
    }

    @Override
    public void registerMessageExpirationListener(MessageExpirationListener listener) {
        QueueManager.getInstance().setListener(listener);
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
