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


package org.openecomp.appc.oam.processor;

import com.att.eelf.configuration.EELFLogger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.oam.rev170303.common.header.CommonHeader;
import org.openecomp.appc.configuration.Configuration;
import org.openecomp.appc.i18n.Msg;
import org.openecomp.appc.oam.AppcOam;
import org.openecomp.appc.oam.OAMCommandStatus;
import org.openecomp.appc.oam.util.AsyncTaskHelper;
import org.openecomp.appc.oam.util.ConfigurationHelper;
import org.openecomp.appc.oam.util.OperationHelper;
import org.openecomp.appc.oam.util.StateHelper;
import org.openecomp.appc.statemachine.impl.readers.AppcOamStates;
import org.powermock.reflect.Whitebox;

import java.util.Date;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;

public class BaseActionRunnableTest {
    private AppcOam.RPC testRpc = AppcOam.RPC.maintenance_mode;
    private AppcOamStates targetState = AppcOamStates.MaintenanceMode;

    private class TestProcessor extends BaseProcessor {
        /**
         * Constructor
         *
         * @param eelfLogger            for logging
         * @param configurationHelperIn for property reading
         * @param stateHelperIn         for APP-C OAM state checking
         * @param asyncTaskHelperIn     for scheduling async task
         * @param operationHelperIn     for operational helper
         */
        TestProcessor(EELFLogger eelfLogger,
                      ConfigurationHelper configurationHelperIn,
                      StateHelper stateHelperIn,
                      AsyncTaskHelper asyncTaskHelperIn,
                      OperationHelper operationHelperIn) {
            super(eelfLogger, configurationHelperIn, stateHelperIn, asyncTaskHelperIn, operationHelperIn);

            // must set rpc and auditMsg
            rpc = testRpc;
            auditMsg = Msg.OAM_OPERATION_STARTING;
            startTime = new Date();
        }
    }

    class TestAbc extends BaseActionRunnable {
        boolean doActionResult;

        TestAbc(BaseProcessor parent) {
            super(parent);

            actionName = "testing";
            auditMsg = Msg.OAM_OPERATION_MAINTENANCE_MODE;
            finalState = targetState;
        }

        @Override
        boolean doAction() {
            return doActionResult;
        }
    }

    private TestAbc testBaseAcionRunnable;
    private BaseProcessor testProcessor;
    private StateHelper mockStateHelper = mock(StateHelper.class);
    private OperationHelper mockOperHelper = mock(OperationHelper.class);
    private ConfigurationHelper mockConfigHelper = mock(ConfigurationHelper.class);
    private Configuration mockConfig = mock(Configuration.class);

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Before
    public void setUp() throws Exception {
        // to avoid operation on logger fail, mock up the logger
        EELFLogger mockLogger = mock(EELFLogger.class);

        Mockito.doReturn(mockConfig).when(mockConfigHelper).getConfig();
        Mockito.doReturn(10).when(mockConfig).getIntegerProperty(any(), anyInt());

        testProcessor = spy(
                new TestProcessor(mockLogger, mockConfigHelper, mockStateHelper, null, mockOperHelper));
        testBaseAcionRunnable = spy(new TestAbc(testProcessor));

        Whitebox.setInternalState(testBaseAcionRunnable, "commonHeader", mock(CommonHeader.class));
    }

    @Test
    public void testSetTimeoutValues() throws Exception {
        Whitebox.setInternalState(testBaseAcionRunnable, "timeoutMs", 0);
        Whitebox.setInternalState(testBaseAcionRunnable, "startTimeMs", 0);
        Whitebox.setInternalState(testBaseAcionRunnable, "doTimeoutChecking", false);
        testBaseAcionRunnable.setTimeoutValues();
        Assert.assertEquals("Should set timeoutMs", 10 * 1000, testBaseAcionRunnable.timeoutMs);
        Assert.assertTrue("Should set start time MS", testBaseAcionRunnable.startTimeMs != 0);
        Assert.assertTrue("Should do check", testBaseAcionRunnable.doTimeoutChecking);

        Whitebox.setInternalState(testBaseAcionRunnable, "timeoutMs", 0);
        Whitebox.setInternalState(testBaseAcionRunnable, "startTimeMs", 0);
        Whitebox.setInternalState(testBaseAcionRunnable, "doTimeoutChecking", false);
        int timeoutSeconds = 20;
        Whitebox.setInternalState(testProcessor, "timeoutSeconds", timeoutSeconds);
        testBaseAcionRunnable.setTimeoutValues();
        Assert.assertEquals("Should set timeoutMs", timeoutSeconds * 1000, testBaseAcionRunnable.timeoutMs);
        Assert.assertTrue("Should set start time MS", testBaseAcionRunnable.startTimeMs != 0);
        Assert.assertTrue("Should do check", testBaseAcionRunnable.doTimeoutChecking);

        Whitebox.setInternalState(testBaseAcionRunnable, "timeoutMs", 0);
        Whitebox.setInternalState(testBaseAcionRunnable, "startTimeMs", 0);
        Whitebox.setInternalState(testBaseAcionRunnable, "doTimeoutChecking", false);

        timeoutSeconds = 0;
        Whitebox.setInternalState(testProcessor, "timeoutSeconds", timeoutSeconds);
        Mockito.doReturn(0).when(mockConfig).getIntegerProperty(
                testBaseAcionRunnable.OAM_OPERATION_TIMEOUT_SECOND, testBaseAcionRunnable.DEFAULT_OAM_OPERATION_TIMEOUT);
        testBaseAcionRunnable.setTimeoutValues();
        Assert.assertEquals("Should set timeoutMs", timeoutSeconds * 1000, testBaseAcionRunnable.timeoutMs);
        Assert.assertTrue("Should not set start time MS", testBaseAcionRunnable.startTimeMs == 0);
        Assert.assertFalse("Should not do check", testBaseAcionRunnable.doTimeoutChecking);
    }
    @Test
    public void testRun() throws Exception {
        // test doAction failed
        Whitebox.setInternalState(testBaseAcionRunnable, "doActionResult", false);
        testBaseAcionRunnable.run();
        Assert.assertFalse("isWaiting should still be false",
                Whitebox.getInternalState(testBaseAcionRunnable, "isWaiting"));

        // test doAction success
        Whitebox.setInternalState(testBaseAcionRunnable, "doActionResult", true);

        // with checkState return true
        Mockito.doReturn(targetState).when(mockStateHelper).getBundlesState();
        testBaseAcionRunnable.run();
        Assert.assertFalse("isWaiting should still be false",
                Whitebox.getInternalState(testBaseAcionRunnable, "isWaiting"));

        // with checkState return false
        Mockito.doReturn(AppcOamStates.Started).when(mockStateHelper).getBundlesState();
        testBaseAcionRunnable.run();
        Assert.assertTrue("isWaiting should still be true",
                Whitebox.getInternalState(testBaseAcionRunnable, "isWaiting"));

        // should stay
        testBaseAcionRunnable.run();
        Mockito.verify(testBaseAcionRunnable, times(1)).keepWaiting();
    }

    @Test
    public void testSetAbortStatus() throws Exception {
        testBaseAcionRunnable.setAbortStatus();
        Assert.assertEquals("Should return reject code", OAMCommandStatus.REJECTED.getResponseCode(),
                testBaseAcionRunnable.status.getCode().intValue());
        Assert.assertTrue("Should set abort message",
                testBaseAcionRunnable.status.getMessage().endsWith(
                        String.format(testBaseAcionRunnable.ABORT_MESSAGE_FORMAT, testRpc.name())));
    }

    @Test
    public void testCheckState() throws Exception {
        Mockito.doReturn(targetState).when(mockStateHelper).getBundlesState();
        Assert.assertTrue("Should return true", testBaseAcionRunnable.checkState());

        Mockito.doReturn(AppcOamStates.Started).when(mockStateHelper).getBundlesState();
        Assert.assertFalse("Should return false", testBaseAcionRunnable.checkState());
    }

    @Test
    public void testPostAction() throws Exception {
        Mockito.doReturn(AppcOamStates.Started).when(mockStateHelper).getCurrentOamState();
        // set status to avoid NPE when using status
        testBaseAcionRunnable.setAbortStatus();

        // test no parameter
        testBaseAcionRunnable.postAction(null);
        Mockito.verify(mockOperHelper, times(1)).sendNotificationMessage(any(), any(), any());
        Mockito.verify(mockStateHelper, times(0)).setState(any());
        Mockito.verify(testProcessor, times(1)).cancelAsyncTask();

        // test with parameter
        testBaseAcionRunnable.postAction(AppcOamStates.Error);
        Mockito.verify(mockOperHelper, times(2)).sendNotificationMessage(any(), any(), any());
        Mockito.verify(mockStateHelper, times(1)).setState(any());
        Mockito.verify(testProcessor, times(2)).cancelAsyncTask();
    }

    @Test
    public void testIsTimeout() throws Exception {
        String parentName = "testing";
        Whitebox.setInternalState(testBaseAcionRunnable, "doTimeoutChecking", false);
        Assert.assertFalse("Should not be timeout", testBaseAcionRunnable.isTimeout(parentName));

        Mockito.doReturn(AppcOamStates.Started).when(mockStateHelper).getCurrentOamState();
        Whitebox.setInternalState(testBaseAcionRunnable, "doTimeoutChecking", true);
        Whitebox.setInternalState(testBaseAcionRunnable, "timeoutMs", System.currentTimeMillis() + 100);
        Whitebox.setInternalState(testBaseAcionRunnable, "startTimeMs", 2);
        Assert.assertFalse("Should not be timeout", testBaseAcionRunnable.isTimeout(parentName));

        long timeoutMs = 1;
        Whitebox.setInternalState(testBaseAcionRunnable, "timeoutMs", timeoutMs);
        Whitebox.setInternalState(testBaseAcionRunnable, "startTimeMs", 2);
        Assert.assertTrue("Should be timeout", testBaseAcionRunnable.isTimeout(parentName));
        Mockito.verify(testBaseAcionRunnable, times(1)).postAction(any());
        Assert.assertEquals("Should return timeout code", OAMCommandStatus.TIMEOUT.getResponseCode(),
                testBaseAcionRunnable.status.getCode().intValue());
        Assert.assertTrue("Should set timeout message",
                testBaseAcionRunnable.status.getMessage().endsWith(
                        String.format(testBaseAcionRunnable.TIMEOUT_MESSAGE_FORMAT, testRpc.name(), timeoutMs)));
    }
}
