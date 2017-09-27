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
import org.openecomp.appc.i18n.Msg;
import org.openecomp.appc.oam.AppcOam;
import org.openecomp.appc.oam.OAMCommandStatus;
import org.openecomp.appc.oam.util.AsyncTaskHelper;
import org.openecomp.appc.oam.util.BundleHelper;
import org.openecomp.appc.oam.util.ConfigurationHelper;
import org.openecomp.appc.oam.util.OperationHelper;
import org.openecomp.appc.oam.util.StateHelper;
import org.openecomp.appc.statemachine.impl.readers.AppcOamStates;
import org.powermock.reflect.Whitebox;

import java.util.Date;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
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
    private BundleHelper mockBundleHelper = mock(BundleHelper.class);

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Before
    public void setUp() throws Exception {
        // to avoid operation on logger fail, mock up the logger
        EELFLogger mockLogger = mock(EELFLogger.class);

        testProcessor = spy(
            new TestProcessor(mockLogger, mockConfigHelper, mockStateHelper, null, mockOperHelper));
        Whitebox.setInternalState(testProcessor, "bundleHelper", mockBundleHelper);

        testBaseAcionRunnable = spy(new TestAbc(testProcessor));
        Whitebox.setInternalState(testBaseAcionRunnable, "commonHeader", mock(CommonHeader.class));
    }

    @Test
    public void testSetTimeoutValues() throws Exception {
        Whitebox.setInternalState(testBaseAcionRunnable, "timeoutMs", 0);
        Whitebox.setInternalState(testBaseAcionRunnable, "startTimeMs", 0);
        Whitebox.setInternalState(testBaseAcionRunnable, "doTimeoutChecking", false);
        long expectedTimeout = 10000L;
        Mockito.doReturn(expectedTimeout).when(mockConfigHelper).getOAMOperationTimeoutValue(any());
        testBaseAcionRunnable.setTimeoutValues();
        Assert.assertEquals("Should set timeoutMs", expectedTimeout, testBaseAcionRunnable.timeoutMs);
        Assert.assertTrue("Should set start time MS", testBaseAcionRunnable.startTimeMs != 0);
        Assert.assertTrue("Should do check", testBaseAcionRunnable.doTimeoutChecking);

        Whitebox.setInternalState(testBaseAcionRunnable, "timeoutMs", 0);
        Whitebox.setInternalState(testBaseAcionRunnable, "startTimeMs", 0);
        Whitebox.setInternalState(testBaseAcionRunnable, "doTimeoutChecking", false);
        expectedTimeout = 20000L;
        Mockito.doReturn(expectedTimeout).when(mockConfigHelper).getOAMOperationTimeoutValue(any());
        testBaseAcionRunnable.setTimeoutValues();
        Assert.assertEquals("Should set timeoutMs", expectedTimeout, testBaseAcionRunnable.timeoutMs);
        Assert.assertTrue("Should set start time MS", testBaseAcionRunnable.startTimeMs != 0);
        Assert.assertTrue("Should do check", testBaseAcionRunnable.doTimeoutChecking);

        Whitebox.setInternalState(testBaseAcionRunnable, "timeoutMs", 0);
        Whitebox.setInternalState(testBaseAcionRunnable, "startTimeMs", 0);
        Whitebox.setInternalState(testBaseAcionRunnable, "doTimeoutChecking", false);
        expectedTimeout = 0L;
        Mockito.doReturn(expectedTimeout).when(mockConfigHelper).getOAMOperationTimeoutValue(any());
        testBaseAcionRunnable.setTimeoutValues();
        Assert.assertEquals("Should set timeoutMs", expectedTimeout, testBaseAcionRunnable.timeoutMs);
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
        Mockito.doReturn(true).when(testBaseAcionRunnable).checkState();
        testBaseAcionRunnable.run();
        Assert.assertFalse("isWaiting should still be false",
            Whitebox.getInternalState(testBaseAcionRunnable, "isWaiting"));

        // with checkState return false
        Mockito.doReturn(false).when(testBaseAcionRunnable).checkState();
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
        Assert.assertEquals("Should return abort code", OAMCommandStatus.ABORT.getResponseCode(),
            testBaseAcionRunnable.status.getCode().intValue());
        Assert.assertTrue("Should set abort due to execution error message",
            testBaseAcionRunnable.status.getMessage().endsWith(
                String.format(testBaseAcionRunnable.ABORT_MESSAGE_FORMAT,
                    testRpc.name(), testBaseAcionRunnable.DUE_TO_EXECUTION_ERROR)));
    }

    @Test
    public void testCheckState() throws Exception {
        // 1. with isTimeout true
        Mockito.doReturn(true).when(testBaseAcionRunnable).isTimeout("checkState");
        Assert.assertTrue("Should return true", testBaseAcionRunnable.checkState());

        // 2. with isTimeout false and
        Mockito.doReturn(false).when(testBaseAcionRunnable).isTimeout("checkState");

        // 2.1 with task not all done
        Mockito.doReturn(false).when(mockBundleHelper).isAllTaskDone(any());
        Assert.assertFalse("Should return false", testBaseAcionRunnable.checkState());

        // 2. 2 with task all done
        Mockito.doReturn(true).when(mockBundleHelper).isAllTaskDone(any());

        // 2.2.1 with has bundle failure
        Mockito.doReturn(true).when(testBaseAcionRunnable).hasBundleOperationFailure();
        Assert.assertTrue("Should return true", testBaseAcionRunnable.checkState());

        // 2.2.2 with no bundle failure
        Mockito.doReturn(false).when(testBaseAcionRunnable).hasBundleOperationFailure();

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

    @SuppressWarnings("unchecked")
    @Test
    public void testHasBundleOperationFailure() throws Exception {
        Mockito.when(mockBundleHelper.getFailedMetrics(anyMap())).thenReturn(Long.valueOf("0"));
        Assert.assertFalse("should return false", testBaseAcionRunnable.hasBundleOperationFailure());

        Mockito.when(mockStateHelper.getCurrentOamState()).thenReturn(AppcOamStates.Restarting);
        long failedNumber = 1;
        Mockito.doReturn(failedNumber).when(mockBundleHelper).getFailedMetrics(anyMap());
        Assert.assertTrue("should return true", testBaseAcionRunnable.hasBundleOperationFailure());
        Mockito.verify(testBaseAcionRunnable, times(1)).setStatus(OAMCommandStatus.UNEXPECTED_ERROR,
            String.format(testBaseAcionRunnable.BUNDLE_OPERATION_FAILED_FORMAT, failedNumber));
        Mockito.verify(testBaseAcionRunnable, times(1)).postAction(AppcOamStates.Error);
    }

    @Test
    public void testAbortRunnable() throws Exception {
        Mockito.doReturn(AppcOamStates.Restarting).when(mockStateHelper).getCurrentOamState();
        AppcOam.RPC newRpc = AppcOam.RPC.restart;
        testBaseAcionRunnable.abortRunnable(newRpc);
        Assert.assertEquals("Should return abort code", OAMCommandStatus.ABORT.getResponseCode(),
            testBaseAcionRunnable.status.getCode().intValue());
        Assert.assertTrue("Should set abort due to new request message",
            testBaseAcionRunnable.status.getMessage().endsWith(
                String.format(testBaseAcionRunnable.ABORT_MESSAGE_FORMAT, testRpc.name(),
                    String.format(testBaseAcionRunnable.NEW_RPC_OPERATION_REQUEST, newRpc.name()))));
        Mockito.verify(mockOperHelper, times(1)).sendNotificationMessage(any(), any(), any());
        Mockito.verify(testBaseAcionRunnable, times(1)).resetLogProperties(false);
        Mockito.verify(testBaseAcionRunnable, times(1)).resetLogProperties(true);
    }
}
