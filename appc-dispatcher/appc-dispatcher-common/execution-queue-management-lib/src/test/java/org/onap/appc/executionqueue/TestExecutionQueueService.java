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

package org.onap.appc.executionqueue;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.executionqueue.helper.Util;
import org.onap.appc.executionqueue.impl.ExecutionQueueServiceImpl;
import org.onap.appc.executionqueue.impl.QueueManager;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
public class TestExecutionQueueService {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @InjectMocks
    ExecutionQueueServiceImpl service;
    @Spy
    QueueManager queueManager = new QueueManager();
    @Spy
    Util executionQueueUtil = new Util();

    @Before
    public void setup() {
        Mockito.doReturn(true).when(queueManager).enqueueTask(any());
    }

    @Test
    public void testPositiveFlow() {
        Message message = new Message();
        try {
            service.putMessage(message);
            Mockito.verify(queueManager, times(1)).enqueueTask(any());
        } catch (APPCException e) {
            Assert.fail(e.toString());
        }
    }

    @Test
    public void testPutMessageExceptionFlow() throws APPCException {
        QueueManager queueManager = Mockito.mock(QueueManager.class);
        Mockito.when(queueManager.enqueueTask(Mockito.any())).thenReturn(false);
        service.setQueueManager(queueManager);
        expectedEx.expect(APPCException.class);
        service.putMessage(new Thread(), 1, TimeUnit.MILLISECONDS);
    }
}
