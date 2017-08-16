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

package org.openecomp.appc.oam.util;

import com.att.eelf.configuration.EELFLogger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.appc.oam.AppcOam;
import org.openecomp.appc.oam.processor.BaseActionRunnable;
import org.powermock.reflect.Whitebox;

import java.util.Arrays;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

public class AsyncTaskHelperTest {
    private AsyncTaskHelper asyncTaskHelper;
    private ScheduledExecutorService mockScheduler = mock(ScheduledExecutorService.class);
    private BaseActionRunnable mockRunnable = mock(BaseActionRunnable.class);

    @Before
    public void setUp() throws Exception {
        asyncTaskHelper = new AsyncTaskHelper(null);

        Whitebox.setInternalState(asyncTaskHelper, "scheduledExecutorService", mockScheduler);
        // to avoid operation on logger fail, mock up the logger
        EELFLogger mockLogger = mock(EELFLogger.class);
        Whitebox.setInternalState(asyncTaskHelper, "logger", mockLogger);
    }

    @Test
    public void testClose() throws Exception {
        asyncTaskHelper.close();
        Mockito.verify(mockScheduler, times(1)).shutdown();
    }

    @Test
    public void testGetCurrentAsyncTask() throws Exception {
        Future<?> mockTask = mock(Future.class);
        Whitebox.setInternalState(asyncTaskHelper, "backgroundOamTask", mockTask);
        Assert.assertEquals("Should return mock task", mockTask, asyncTaskHelper.getCurrentAsyncTask());
    }

    @Test
    public void testScheduleAsyncTaskWithMmod() throws Exception {
        // test maintenance mode
        ScheduledFuture<?> mockTask0 = mock(ScheduledFuture.class);
        Whitebox.setInternalState(asyncTaskHelper, "backgroundOamTask", mockTask0);

        ScheduledFuture<?> mockTask1 = mock(ScheduledFuture.class);
        Mockito.doReturn(mockTask1).when(mockScheduler).scheduleWithFixedDelay(
                mockRunnable, asyncTaskHelper.MMODE_TASK_DELAY,
                asyncTaskHelper.MMODE_TASK_DELAY, TimeUnit.MILLISECONDS);
        asyncTaskHelper.scheduleAsyncTask(AppcOam.RPC.maintenance_mode, mockRunnable);
        Mockito.verify(mockTask0, times(1)).cancel(true);
        Assert.assertEquals(mockTask1, asyncTaskHelper.scheduleAsyncTask(AppcOam.RPC.maintenance_mode, mockRunnable));
        Assert.assertEquals("Should set backgroundOamTask", mockTask1, asyncTaskHelper.getCurrentAsyncTask());
    }

    @Test
    public void testScheduleAsyncTaskWithStart() throws Exception {
        for (AppcOam.RPC rpc : Arrays.asList(AppcOam.RPC.start, AppcOam.RPC.stop, AppcOam.RPC.restart)) {
            runTest(rpc);
        }
    }

    private void runTest(AppcOam.RPC rpc) {
        ScheduledFuture<?> mockTask0 = mock(ScheduledFuture.class);
        Whitebox.setInternalState(asyncTaskHelper, "backgroundOamTask", mockTask0);
        BaseActionRunnable mockRunnable0 = mock(BaseActionRunnable.class);
        Whitebox.setInternalState(asyncTaskHelper, "taskRunnable", mockRunnable0);

        ScheduledFuture<?> mockTask2 = mock(ScheduledFuture.class);
        Mockito.doReturn(mockTask2).when(mockScheduler).scheduleWithFixedDelay(
                mockRunnable, asyncTaskHelper.COMMON_INITIAL_DELAY,
                asyncTaskHelper.COMMON_INTERVAL, TimeUnit.MILLISECONDS);
        asyncTaskHelper.scheduleAsyncTask(rpc, mockRunnable);
        Mockito.verify(mockTask0, times(1)).cancel(true);
        Mockito.verify(mockRunnable0, times(1)).abortRunnable(rpc);
        Assert.assertEquals(mockTask2, asyncTaskHelper.scheduleAsyncTask(rpc, mockRunnable));
        Assert.assertEquals("Should set backgroundOamTask", mockTask2, asyncTaskHelper.getCurrentAsyncTask());
    }

    @Test
    public void testCancelAsyncTask() throws Exception {
        Future<?> mockTask = mock(Future.class);
        Whitebox.setInternalState(asyncTaskHelper, "backgroundOamTask", mockTask);
        asyncTaskHelper.cancelAsyncTask(mockTask);
        Mockito.verify(mockTask, times(1)).cancel(false);
        Assert.assertTrue("Should have reset backgroundOamTask",
                asyncTaskHelper.getCurrentAsyncTask() == null);


        Whitebox.setInternalState(asyncTaskHelper, "backgroundOamTask", mockTask);
        Future<?> mockTask2 = mock(Future.class);
        asyncTaskHelper.cancelAsyncTask(mockTask2);
        Mockito.verify(mockTask2, times(1)).cancel(false);
        Assert.assertEquals("Should not reset backgroundOamTask",
                mockTask, asyncTaskHelper.getCurrentAsyncTask());
    }

}
