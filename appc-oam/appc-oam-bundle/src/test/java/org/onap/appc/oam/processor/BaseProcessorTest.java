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
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.StartInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.common.header.CommonHeader;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.status.Status;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.exceptions.InvalidInputException;
import org.onap.appc.exceptions.InvalidStateException;
import org.onap.appc.i18n.Msg;
import org.onap.appc.oam.AppcOam;
import org.onap.appc.oam.OAMCommandStatus;
import org.onap.appc.oam.util.AsyncTaskHelper;
import org.onap.appc.oam.util.ConfigurationHelper;
import org.onap.appc.oam.util.OperationHelper;
import org.onap.appc.oam.util.StateHelper;
import org.onap.appc.statemachine.impl.readers.AppcOamStates;
import org.powermock.reflect.Whitebox;

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
        Mockito.doReturn("SOME HOST NAME").when(testBaseProcessor).getHostInfo(Mockito.anyString());
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
        Assert.assertTrue(Whitebox.getInternalState(testBaseProcessor, "runnable") == null);
        Assert.assertTrue(Whitebox.getInternalState(testBaseProcessor, "scheduledRunnable") == null);

        BaseActionRunnable mockRunnable = mock(BaseActionRunnable.class);
        Whitebox.setInternalState(testBaseProcessor, "runnable", mockRunnable);
        testBaseProcessor.scheduleAsyncTask();
        // scheduledRunnable should still be null, there's no mock done
        // as I have trouble to make mockTaskHelper.scheduleBaseRunnable to return a proper Future
        Assert.assertTrue(Whitebox.getInternalState(testBaseProcessor, "scheduledRunnable") == null);
    }

}
