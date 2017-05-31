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

package org.openecomp.appc.executionqueue;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.executionqueue.ExecutionQueueService;
import org.openecomp.appc.executionqueue.impl.ExecutionQueueServiceFactory;
import org.powermock.api.mockito.PowerMockito;

import java.util.concurrent.TimeUnit;


public class TestExecutionQueueService {

    @Test
    public void testPositiveFlow(){
        Message message = new Message();
        ExecutionQueueService service =  ExecutionQueueServiceFactory.getExecutionQueueService();
        try {
            service.putMessage(message);
            waitFor(5000);
            Assert.assertTrue(message.isRunExecuted());
        } catch (APPCException e) {
            Assert.fail(e.toString());
        }
    }

//    @Test
    public void testTimeout(){
        ExecutionQueueService service =  ExecutionQueueServiceFactory.getExecutionQueueService();
        Message message = new Message();
        Listener listener = new Listener();
        service.registerMessageExpirationListener(listener);
        try {
            service.putMessage(message,1, TimeUnit.MILLISECONDS);
            waitFor(5000);
            Assert.assertTrue(listener.isListenerExecuted());
        } catch (APPCException e) {
            e.printStackTrace();
        }
    }

    private void waitFor(long milliSeconds){
        try {
            Thread.sleep(milliSeconds);
        } catch (InterruptedException e) {
            Assert.fail(e.toString());
        }
    }
}
