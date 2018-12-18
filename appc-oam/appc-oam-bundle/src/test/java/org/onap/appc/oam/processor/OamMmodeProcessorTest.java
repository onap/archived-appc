/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 Ericsson
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

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.exceptions.InvalidInputException;
import org.onap.appc.exceptions.InvalidStateException;
import org.onap.appc.i18n.Msg;
import org.onap.appc.oam.AppcOam;
import org.onap.appc.oam.util.AsyncTaskHelper;
import org.onap.appc.oam.util.ConfigurationHelper;
import org.onap.appc.oam.util.OperationHelper;
import org.onap.appc.oam.util.StateHelper;
import org.onap.appc.statemachine.impl.readers.AppcOamStates;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.StartInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.common.header.CommonHeader;
import org.powermock.reflect.Whitebox;
import org.onap.appc.requesthandler.LCMStateManager;
import com.att.eelf.configuration.EELFLogger;

public class OamMmodeProcessorTest {
    
    private class TestAbc extends OamMmodeProcessor {

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

    private ConfigurationHelper mockConfigHelper = mock(ConfigurationHelper.class);
    private StateHelper mockStateHelper = mock(StateHelper.class);
    private AsyncTaskHelper mockTaskHelper = mock(AsyncTaskHelper.class);
    private OperationHelper mockOperHelper = mock(OperationHelper.class);
    private AppcOam.RPC testRpc = AppcOam.RPC.start;
    private AppcOamStates currentState = AppcOamStates.Stopped;
    private StartInput mockInput = mock(StartInput.class);
    private CommonHeader mockCommonHeader = mock(CommonHeader.class);
    private LCMStateManager mockLCMStateManager = mock(LCMStateManager.class);


    // to avoid operation on logger fail, mock up the logger
    EELFLogger mockLogger = mock(EELFLogger.class);
    private OamMmodeProcessor oamMmodeProcessor = Mockito.spy(new TestAbc(mockLogger, mockConfigHelper, mockStateHelper, mockTaskHelper, mockOperHelper));

    @Before
    public void setup() throws UnknownHostException, APPCException {
        Mockito.doReturn("SOME HOST NAME").when(oamMmodeProcessor).getHostInfo(Mockito.anyString());
        Mockito.doReturn(mockCommonHeader).when(mockOperHelper).getCommonHeader(mockInput);
        Mockito.doReturn(mockCommonHeader).when(mockInput).getCommonHeader();
        Mockito.doReturn(mockLCMStateManager).when(mockOperHelper).getService(LCMStateManager.class);
        Whitebox.setInternalState(oamMmodeProcessor, "commonHeader", mockCommonHeader);
    }

    @Test
    public void testPreProcess() throws InvalidInputException, InvalidStateException, APPCException, InterruptedException, TimeoutException {
        Mockito.doReturn(currentState).when(mockStateHelper).getCurrentOamState();
        AppcOamStates nextState = AppcOamStates.Starting;
        Mockito.doReturn(nextState)
            .when(mockOperHelper).getNextState(testRpc.getAppcOperation(), currentState);
        oamMmodeProcessor.preProcess(mockInput);
        Mockito.verify(mockOperHelper, times(1)).isInputValid(mockInput);
        Mockito.verify(mockOperHelper, times(1)).getNextState(testRpc.getAppcOperation(), currentState);
        Mockito.verify(mockStateHelper, times(1)).setState(nextState);
    }

    @Test
    public void testScheduleAsyncTask() throws Exception {
        oamMmodeProcessor.scheduleAsyncTask();
        BaseActionRunnable runnable = Whitebox.getInternalState(oamMmodeProcessor, "runnable");
        assertTrue(runnable != null);
    }
}
