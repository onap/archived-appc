/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications (C) 2018 Ericsson
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

package org.onap.appc.oam.processor;

import com.att.eelf.configuration.EELFLogger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.common.header.CommonHeader;
import org.onap.appc.i18n.Msg;
import org.onap.appc.oam.AppcOam;
import org.onap.appc.oam.OAMCommandStatus;
import org.onap.appc.oam.util.AsyncTaskHelper;
import org.onap.appc.oam.util.BundleHelper;
import org.onap.appc.oam.util.ConfigurationHelper;
import org.onap.appc.oam.util.OperationHelper;
import org.onap.appc.oam.util.StateHelper;
import org.onap.appc.statemachine.impl.readers.AppcOamStates;
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

    private TestAbc testBaseActionRunnable;
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

        testBaseActionRunnable = spy(new TestAbc(testProcessor));
        Whitebox.setInternalState(testBaseActionRunnable, "commonHeader", mock(CommonHeader.class));
    }

    @Test
    public void testSetTimeoutValues() throws Exception {
        Whitebox.setInternalState(testBaseActionRunnable, "timeoutMs", 0);
        Whitebox.setInternalState(testBaseActionRunnable, "startTimeMs", 0);
        Whitebox.setInternalState(testBaseActionRunnable, "doTimeoutChecking", false);
        long expectedTimeout = 10000L;
        Mockito.doReturn(expectedTimeout).when(mockConfigHelper).getOAMOperationTimeoutValue(any());
        testBaseActionRunnable.setTimeoutValues();
        Assert.assertEquals("Should set timeoutMs", expectedTimeout, testBaseActionRunnable.timeoutMs);
        Assert.assertTrue("Should set start time MS", testBaseActionRunnable.startTimeMs != 0);
        Assert.assertTrue("Should do check", testBaseActionRunnable.doTimeoutChecking);

        Whitebox.setInternalState(testBaseActionRunnable, "timeoutMs", 0);
        Whitebox.setInternalState(testBaseActionRunnable, "startTimeMs", 0);
        Whitebox.setInternalState(testBaseActionRunnable, "doTimeoutChecking", false);
        expectedTimeout = 20000L;
        Mockito.doReturn(expectedTimeout).when(mockConfigHelper).getOAMOperationTimeoutValue(any());
        testBaseActionRunnable.setTimeoutValues();
        Assert.assertEquals("Should set timeoutMs", expectedTimeout, testBaseActionRunnable.timeoutMs);
        Assert.assertTrue("Should set start time MS", testBaseActionRunnable.startTimeMs != 0);
        Assert.assertTrue("Should do check", testBaseActionRunnable.doTimeoutChecking);

        Whitebox.setInternalState(testBaseActionRunnable, "timeoutMs", 0);
        Whitebox.setInternalState(testBaseActionRunnable, "startTimeMs", 0);
        Whitebox.setInternalState(testBaseActionRunnable, "doTimeoutChecking", false);
        expectedTimeout = 0L;
        Mockito.doReturn(expectedTimeout).when(mockConfigHelper).getOAMOperationTimeoutValue(any());
        testBaseActionRunnable.setTimeoutValues();
        Assert.assertEquals("Should set timeoutMs", expectedTimeout, testBaseActionRunnable.timeoutMs);
        Assert.assertTrue("Should not set start time MS", testBaseActionRunnable.startTimeMs == 0);
        Assert.assertFalse("Should not do check", testBaseActionRunnable.doTimeoutChecking);
    }

    @Test
    public void testRun() throws Exception {
        // test doAction failed
        Whitebox.setInternalState(testBaseActionRunnable, "doActionResult", false);
        Mockito.doReturn("SOME HOST NAME").when(testBaseActionRunnable).getHostInfo(Mockito.anyString());
        testBaseActionRunnable.run();
        Assert.assertFalse("isWaiting should still be false",
            Whitebox.getInternalState(testBaseActionRunnable, "isWaiting"));

        // test doAction success
        Whitebox.setInternalState(testBaseActionRunnable, "doActionResult", true);

        // with checkState return true
        Mockito.doReturn(true).when(testBaseActionRunnable).checkState();
        testBaseActionRunnable.run();
        Assert.assertFalse("isWaiting should still be false",
            Whitebox.getInternalState(testBaseActionRunnable, "isWaiting"));

        // with checkState return false
        Mockito.doReturn(false).when(testBaseActionRunnable).checkState();
        testBaseActionRunnable.run();
        Assert.assertTrue("isWaiting should still be true",
            Whitebox.getInternalState(testBaseActionRunnable, "isWaiting"));

        // should stay
        testBaseActionRunnable.run();
        Mockito.verify(testBaseActionRunnable, times(1)).keepWaiting();
    }

    @Test
    public void testSetAbortStatus() throws Exception {
        testBaseActionRunnable.setAbortStatus();
        Assert.assertEquals("Should return abort code", OAMCommandStatus.ABORT.getResponseCode(),
            testBaseActionRunnable.status.getCode().intValue());
        Assert.assertTrue("Should set abort due to execution error message",
            testBaseActionRunnable.status.getMessage().endsWith(
                String.format(testBaseActionRunnable.ABORT_MESSAGE_FORMAT,
                    testRpc.name(), testBaseActionRunnable.DUE_TO_EXECUTION_ERROR)));
    }

    @Test
    public void testCheckState() throws Exception {
        // 1. with isTimeout true
        Mockito.doReturn(true).when(testBaseActionRunnable).isTimeout("checkState");
        Assert.assertTrue("Should return true", testBaseActionRunnable.checkState());

        // 2. with isTimeout false and
        Mockito.doReturn(false).when(testBaseActionRunnable).isTimeout("checkState");

        // 2.1 with task not all done
        Mockito.doReturn(false).when(mockBundleHelper).isAllTaskDone(any());
        Assert.assertFalse("Should return false", testBaseActionRunnable.checkState());

        // 2. 2 with task all done
        Mockito.doReturn(true).when(mockBundleHelper).isAllTaskDone(any());

        // 2.2.1 with has bundle failure
        Mockito.doReturn(true).when(testBaseActionRunnable).hasBundleOperationFailure();
        Assert.assertTrue("Should return true", testBaseActionRunnable.checkState());

        // 2.2.2 with no bundle failure
        Mockito.doReturn(false).when(testBaseActionRunnable).hasBundleOperationFailure();

        Mockito.doReturn(targetState).when(mockStateHelper).getBundlesState();
        Assert.assertTrue("Should return true", testBaseActionRunnable.checkState());

        Mockito.doReturn(AppcOamStates.Started).when(mockStateHelper).getBundlesState();
        Assert.assertFalse("Should return false", testBaseActionRunnable.checkState());
    }

    @Test
    public void testPostAction() throws Exception {
        Mockito.doReturn(AppcOamStates.Started).when(mockStateHelper).getCurrentOamState();
        // set status to avoid NPE when using status
        testBaseActionRunnable.setAbortStatus();

        // test no parameter
        testBaseActionRunnable.postAction(null);
        Mockito.verify(mockOperHelper, times(1)).sendNotificationMessage(any(), any(), any());
        Mockito.verify(mockStateHelper, times(0)).setState(any());
        Mockito.verify(testProcessor, times(1)).cancelAsyncTask();

        // test with parameter
        testBaseActionRunnable.postAction(AppcOamStates.Error);
        Mockito.verify(mockOperHelper, times(2)).sendNotificationMessage(any(), any(), any());
        Mockito.verify(mockStateHelper, times(1)).setState(any());
        Mockito.verify(testProcessor, times(2)).cancelAsyncTask();
    }

    @Test
    public void testIsTimeout() throws Exception {
        String parentName = "testing";
        Whitebox.setInternalState(testBaseActionRunnable, "doTimeoutChecking", false);
        Assert.assertFalse("Should not be timeout", testBaseActionRunnable.isTimeout(parentName));

        Mockito.doReturn(AppcOamStates.Started).when(mockStateHelper).getCurrentOamState();
        Whitebox.setInternalState(testBaseActionRunnable, "doTimeoutChecking", true);
        Whitebox.setInternalState(testBaseActionRunnable, "timeoutMs", System.currentTimeMillis() + 100);
        Whitebox.setInternalState(testBaseActionRunnable, "startTimeMs", 2);
        Assert.assertFalse("Should not be timeout", testBaseActionRunnable.isTimeout(parentName));

        long timeoutMs = 1;
        Whitebox.setInternalState(testBaseActionRunnable, "timeoutMs", timeoutMs);
        Whitebox.setInternalState(testBaseActionRunnable, "startTimeMs", 2);
        Assert.assertTrue("Should be timeout", testBaseActionRunnable.isTimeout(parentName));
        Mockito.verify(testBaseActionRunnable, times(1)).postAction(any());
        Assert.assertEquals("Should return timeout code", OAMCommandStatus.TIMEOUT.getResponseCode(),
            testBaseActionRunnable.status.getCode().intValue());
        Assert.assertTrue("Should set timeout message",
            testBaseActionRunnable.status.getMessage().endsWith(
                String.format(testBaseActionRunnable.TIMEOUT_MESSAGE_FORMAT, testRpc.name(), timeoutMs)));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testHasBundleOperationFailure() throws Exception {
        Mockito.when(mockBundleHelper.getFailedMetrics(anyMap())).thenReturn(Long.valueOf("0"));
        Assert.assertFalse("should return false", testBaseActionRunnable.hasBundleOperationFailure());

        Mockito.when(mockStateHelper.getCurrentOamState()).thenReturn(AppcOamStates.Restarting);
        long failedNumber = 1;
        Mockito.doReturn(failedNumber).when(mockBundleHelper).getFailedMetrics(anyMap());
        Assert.assertTrue("should return true", testBaseActionRunnable.hasBundleOperationFailure());
        Mockito.verify(testBaseActionRunnable, times(1)).setStatus(OAMCommandStatus.UNEXPECTED_ERROR,
            String.format(testBaseActionRunnable.BUNDLE_OPERATION_FAILED_FORMAT, failedNumber));
        Mockito.verify(testBaseActionRunnable, times(1)).postAction(AppcOamStates.Error);
    }

    @Test
    public void testAbortRunnable() throws Exception {
        Mockito.doReturn(AppcOamStates.Restarting).when(mockStateHelper).getCurrentOamState();
        Mockito.doReturn("SOME HOST NAME").when(testBaseActionRunnable).getHostInfo(Mockito.anyString());
        AppcOam.RPC newRpc = AppcOam.RPC.restart;
        testBaseActionRunnable.abortRunnable(newRpc);
        Assert.assertEquals("Should return abort code", OAMCommandStatus.ABORT.getResponseCode(),
            testBaseActionRunnable.status.getCode().intValue());
        Assert.assertTrue("Should set abort due to new request message",
            testBaseActionRunnable.status.getMessage().endsWith(
                String.format(testBaseActionRunnable.ABORT_MESSAGE_FORMAT, testRpc.name(),
                    String.format(testBaseActionRunnable.NEW_RPC_OPERATION_REQUEST, newRpc.name()))));
        Mockito.verify(mockOperHelper, times(1)).sendNotificationMessage(any(), any(), any());
        Mockito.verify(testBaseActionRunnable, times(1)).resetLogProperties(false);
        Mockito.verify(testBaseActionRunnable, times(1)).resetLogProperties(true);
    }
}
