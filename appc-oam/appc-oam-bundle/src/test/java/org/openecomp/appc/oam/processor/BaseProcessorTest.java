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
import org.opendaylight.yang.gen.v1.org.openecomp.appc.oam.rev170303.StartInput;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.oam.rev170303.common.header.CommonHeader;
import org.opendaylight.yang.gen.v1.org.openecomp.appc.oam.rev170303.status.Status;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.exceptions.InvalidInputException;
import org.openecomp.appc.exceptions.InvalidStateException;
import org.openecomp.appc.i18n.Msg;
import org.openecomp.appc.oam.AppcOam;
import org.openecomp.appc.oam.OAMCommandStatus;
import org.openecomp.appc.oam.util.AsyncTaskHelper;
import org.openecomp.appc.oam.util.ConfigurationHelper;
import org.openecomp.appc.oam.util.OperationHelper;
import org.openecomp.appc.oam.util.StateHelper;
import org.openecomp.appc.statemachine.impl.readers.AppcOamStates;
import org.powermock.reflect.Whitebox;

import java.util.concurrent.Future;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class BaseProcessorTest {
    private AppcOam.RPC testRpc = AppcOam.RPC.start;
    private AppcOamStates currentState = AppcOamStates.Stopped;

    private class TestAbc extends BaseProcessor {

        /**
         * Constructor
         *
         * @param eelfLogger            for logging
         * @param configurationHelperIn for property reading
         * @param stateHelperIn         for APP-C OAM state checking
         * @param asyncTaskHelperIn     for scheduling async task
         * @param operationHelperIn     for operational helper
         */
        TestAbc(EELFLogger eelfLogger,
                ConfigurationHelper configurationHelperIn,
                StateHelper stateHelperIn,
                AsyncTaskHelper asyncTaskHelperIn,
                OperationHelper operationHelperIn) {
            super(eelfLogger, configurationHelperIn, stateHelperIn, asyncTaskHelperIn, operationHelperIn);

            // must set rpc and auditMsg
            rpc = testRpc;
            auditMsg = Msg.OAM_OPERATION_STARTING;
        }
    }

    private TestAbc testBaseProcessor;
    private ConfigurationHelper mockConfigHelper = mock(ConfigurationHelper.class);
    private StateHelper mockStateHelper = mock(StateHelper.class);
    private AsyncTaskHelper mockTaskHelper = mock(AsyncTaskHelper.class);
    private OperationHelper mockOperHelper = mock(OperationHelper.class);

    private StartInput mockInput = mock(StartInput.class);
    private CommonHeader mockCommonHeader = mock(CommonHeader.class);

    @Before
    public void setUp() throws Exception {
        Mockito.doReturn(mockCommonHeader).when(mockOperHelper).getCommonHeader(mockInput);
        Mockito.doReturn(mockCommonHeader).when(mockInput).getCommonHeader();

        testBaseProcessor = spy(
                new TestAbc(null, mockConfigHelper, mockStateHelper, mockTaskHelper, mockOperHelper));

        Whitebox.setInternalState(testBaseProcessor, "commonHeader", mockCommonHeader);

        // to avoid operation on logger fail, mock up the logger
        EELFLogger mockLogger = mock(EELFLogger.class);
        Whitebox.setInternalState(testBaseProcessor, "logger", mockLogger);
    }

    @Test
    public void testProcessRequestError() throws Exception {
        Mockito.doReturn(currentState).when(mockStateHelper).getCurrentOamState();
        Mockito.doThrow(new InvalidInputException("test")).when(mockOperHelper).isInputValid(mockInput);
        Status status = testBaseProcessor.processRequest(mockInput);
        Assert.assertEquals("Should return reject",
                OAMCommandStatus.INVALID_PARAMETER.getResponseCode(), status.getCode().intValue());
    }

    @Test
    public void testProcessRequest() throws Exception {
        Mockito.doReturn(currentState).when(mockStateHelper).getCurrentOamState();
        Mockito.doReturn(AppcOamStates.Starting).when(mockOperHelper).getNextState(any(), any());
        Mockito.doReturn(mockCommonHeader).when(mockInput).getCommonHeader();
        Status status = testBaseProcessor.processRequest(mockInput);
        Assert.assertEquals("Should return success",
                OAMCommandStatus.ACCEPTED.getResponseCode(), status.getCode().intValue());
    }

    @Test(expected = InvalidInputException.class)
    public void testPreProcessWithInvalidInput() throws Exception {
        Mockito.doThrow(new InvalidInputException("test")).when(mockOperHelper).isInputValid(mockInput);
        testBaseProcessor.preProcess(mockInput);
    }

    @Test(expected = InvalidStateException.class)
    public void testPreProcessWithInvalidState() throws Exception {
        Mockito.doReturn(currentState).when(mockStateHelper).getCurrentOamState();
        Mockito.doThrow(new InvalidStateException("test"))
                .when(mockOperHelper).getNextState(testRpc.getAppcOperation(), currentState);
        testBaseProcessor.preProcess(mockInput);
    }

    @Test(expected = APPCException.class)
    public void testPreProcessWithAppcException() throws Exception {
        Mockito.doReturn(currentState).when(mockStateHelper).getCurrentOamState();
        Mockito.doThrow(new APPCException("test"))
                .when(mockOperHelper).getNextState(testRpc.getAppcOperation(), currentState);
        testBaseProcessor.preProcess(mockInput);
    }

    @Test
    public void testPreProcess() throws Exception {
        Mockito.doReturn(currentState).when(mockStateHelper).getCurrentOamState();
        AppcOamStates nextState = AppcOamStates.Starting;
        Mockito.doReturn(nextState)
                .when(mockOperHelper).getNextState(testRpc.getAppcOperation(), currentState);
        testBaseProcessor.preProcess(mockInput);
        Mockito.verify(mockOperHelper, times(1)).isInputValid(mockInput);
        Mockito.verify(mockOperHelper, times(1)).getNextState(testRpc.getAppcOperation(), currentState);
        Mockito.verify(mockStateHelper, times(1)).setState(nextState);
    }

    @Test
    public void testScheduleAsyncTask() throws Exception {
        // test no runnable
        testBaseProcessor.scheduleAsyncTask();
        Mockito.verify(mockTaskHelper, times(0)).scheduleAsyncTask(any(), any());

        // test runnable
        Runnable runnable = () -> {
            // do nothing
        };
        Whitebox.setInternalState(testBaseProcessor, "runnable", runnable);
        testBaseProcessor.scheduleAsyncTask();
        Mockito.verify(mockTaskHelper, times(1)).scheduleAsyncTask(testRpc, runnable);
    }

    @Test
    public void isSameAsyncTask() throws Exception {
        Future<?> mockTask1 = mock(Future.class);
        Whitebox.setInternalState(testBaseProcessor, "scheduledRunnable", mockTask1);
        Mockito.doReturn(mockTask1).when(mockTaskHelper).getCurrentAsyncTask();
        Assert.assertTrue("Shoudl be the same", testBaseProcessor.isSameAsyncTask());

        Future<?> mockTask2 = mock(Future.class);
        Mockito.doReturn(mockTask2).when(mockTaskHelper).getCurrentAsyncTask();
        Assert.assertFalse("Shoudl not be the same", testBaseProcessor.isSameAsyncTask());
    }

    @Test
    public void cancleAsyncTask() throws Exception {
        Future<?> mockTask = mock(Future.class);
        Whitebox.setInternalState(testBaseProcessor, "scheduledRunnable", mockTask);
        testBaseProcessor.cancelAsyncTask();
        Mockito.verify(mockTaskHelper, times(1)).cancelAsyncTask(mockTask);
        Assert.assertTrue(Whitebox.getInternalState(testBaseProcessor, "scheduledRunnable") == null);
    }

}
