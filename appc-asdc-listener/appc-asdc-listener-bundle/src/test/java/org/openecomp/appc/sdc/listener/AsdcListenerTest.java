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
package org.openecomp.appc.sdc.listener;

import com.att.eelf.configuration.EELFLogger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openecomp.sdc.api.IDistributionClient;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Thread.class)
public class AsdcListenerTest {
    private AsdcListener asdcListener;
    private EELFLogger mockLogger = mock(EELFLogger.class);

    @Before
    public void setUp() throws Exception {
        asdcListener = new AsdcListener();

        // to avoid operation on logger fail, mock up the logger
        Whitebox.setInternalState(asdcListener, "logger", mockLogger);
    }

    @Test
    public void testStart() throws Exception {
        asdcListener.start();
        Assert.assertTrue("Should created startThread",
                Whitebox.getInternalState(asdcListener, "startThread") != null);
    }

    @Test
    public void testStop() throws Exception {
        // test interrupt thread and other null case
        MockThread mockThread = spy(new MockThread());
        mockThread.setNewState(Thread.State.TIMED_WAITING);
        Whitebox.setInternalState(asdcListener, "startThread", mockThread);

        asdcListener.stop();
        Mockito.verify(mockThread, times(1)).interrupt();
        Assert.assertTrue("Should reset startThread",
                Whitebox.getInternalState(asdcListener, "startThread") == null);

        // test other non-null case and thread null case
        IDistributionClient mockClient = mock(IDistributionClient.class);
        Whitebox.setInternalState(asdcListener, "client", mockClient);
        AsdcCallback mockCallback = mock(AsdcCallback.class);
        Whitebox.setInternalState(asdcListener, "callback", mockCallback);
        CountDownLatch mockLatch = mock(CountDownLatch.class);
        Whitebox.setInternalState(asdcListener, "latch", mockLatch);

        asdcListener.stop();

        Mockito.verify(mockLatch, times(1)).await(10, TimeUnit.SECONDS);
        Mockito.verify(mockCallback, times(1)).stop();
        Mockito.verify(mockClient, times(1)).stop();
        Assert.assertTrue("Should reset latch",
                Whitebox.getInternalState(asdcListener, "latch") == null);
        Assert.assertTrue("Should reset callback",
                Whitebox.getInternalState(asdcListener, "callback") == null);
        Assert.assertTrue("Should reset client",
                Whitebox.getInternalState(asdcListener, "client") == null);
    }

    @Test
    public void testStopStartThread() throws Exception {
        // null case
        asdcListener.stopStartThread(123);
        Mockito.verify(mockLogger, times(0)).debug(String.valueOf(any()));

        MockThread mockThread = spy(new MockThread());

        // thread terminated case
        Whitebox.setInternalState(asdcListener, "startThread", mockThread);
        mockThread.setNewState(Thread.State.TERMINATED);
        asdcListener.stopStartThread(123);
        Mockito.verify(mockThread, times(0)).interrupt();
        Mockito.verify(mockLogger, times(1)).debug(String.valueOf(any()));
        Assert.assertTrue("Should reset startThread",
                Whitebox.getInternalState(asdcListener, "startThread") == null);

        // thread not termianted case
        int timesCallThread = 0;
        int timesCallLogger = 1;
        for(Thread.State state : Thread.State.values()) {
            if (state == Thread.State.TERMINATED) {
                continue;
            }
            Whitebox.setInternalState(asdcListener, "startThread", mockThread);
            mockThread.setNewState(state);
            asdcListener.stopStartThread(123);
            Mockito.verify(mockThread, times(++ timesCallThread)).interrupt();
            Mockito.verify(mockLogger, times(timesCallLogger += 2)).debug(String.valueOf(any()));
            Assert.assertTrue("Should reset startThread",
                    Whitebox.getInternalState(asdcListener, "startThread") == null);
        }
    }

    /*
     * I have used the following PowerMockito (due to Thread.getName() is a final method)
     * try to mock up the thread behavior. But the mock Thread.getName() always returns null
     * which works in intelliJ Junit test, but not Jenkins build:
     *     Thread mockThread = PowerMockito.mock(Thread.class);
     *      PowerMockito.doReturn(Thread.State.TERMINATED).when(mockThread).getState();
     *      PowerMockito.doReturn("testing").when(mockThread).getName();
     * Hence, here goes the MockThread class to override Thread to my expected behavior.
     */
    class MockThread extends Thread {
        private State state;

        private MockThread() {
            super.setName("testing");
        }

        void setNewState(State newState) {
            state = newState;
        }

        @Override
        public State getState() {
            return state;
        }
    }
}