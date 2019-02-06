/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 Ericsson
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
 *
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.executionqueue.impl;

import org.junit.Test;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.mockito.Mockito;
import org.onap.appc.executionqueue.helper.Util;
import org.powermock.reflect.Whitebox;

public class QueueManagerTest {

    @Test(expected = NullPointerException.class)
    public void testInit() {
        QueueManager qm = new QueueManager();
        Util util = Mockito.mock(Util.class);
        Mockito.when(util.getExecutionQueueSize()).thenReturn(1);
        Mockito.when(util.getThreadPoolSize()).thenReturn(1);
        qm.setExecutionQueueUtil(util);
        qm.init();
    }

    @Test
    public void testStop() throws InterruptedException {
        QueueManager qm = Mockito.spy(new QueueManager());
        ExecutorService executor = Mockito.mock(ExecutorService.class);
        Mockito.when(executor.shutdownNow()).thenReturn(new ArrayList<Runnable>());
        Mockito.when(executor.awaitTermination(100, TimeUnit.MILLISECONDS)).thenReturn(false).thenReturn(true);
        Whitebox.setInternalState(qm, "messageExecutor", executor);
        qm.stop();
    }

    @Test
    public void testEnqueueTask() throws InterruptedException {
        QueueManager qm = Mockito.spy(new QueueManager());
        ExecutorService executor = Mockito.mock(ExecutorService.class);
        Mockito.when(executor.shutdownNow()).thenReturn(new ArrayList<Runnable>());
        Mockito.when(executor.awaitTermination(100, TimeUnit.MILLISECONDS)).thenReturn(false).thenReturn(true);
        Whitebox.setInternalState(qm, "messageExecutor", executor);
        qm.enqueueTask(null);
    }

}
