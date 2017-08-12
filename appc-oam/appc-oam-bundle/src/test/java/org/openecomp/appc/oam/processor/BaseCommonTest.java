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
import org.opendaylight.yang.gen.v1.org.openecomp.appc.oam.rev170303.status.Status;
import org.openecomp.appc.exceptions.InvalidInputException;
import org.openecomp.appc.exceptions.InvalidStateException;
import org.openecomp.appc.oam.AppcOam;
import org.openecomp.appc.oam.OAMCommandStatus;
import org.openecomp.appc.oam.util.ConfigurationHelper;
import org.openecomp.appc.oam.util.OperationHelper;
import org.openecomp.appc.oam.util.StateHelper;
import org.openecomp.appc.statemachine.impl.readers.AppcOamStates;
import org.powermock.reflect.Whitebox;

import static org.mockito.Mockito.mock;

public class BaseCommonTest {
    private class TestAbc extends BaseCommon {

        /**
         * Constructor
         *
         * @param eelfLogger            for logging
         * @param configurationHelperIn for property reading
         * @param stateHelperIn         for APP-C OAM state checking
         * @param operationHelperIn     for operational helper
         */
        TestAbc(EELFLogger eelfLogger,
                ConfigurationHelper configurationHelperIn,
                StateHelper stateHelperIn,
                OperationHelper operationHelperIn) {
            super(eelfLogger, configurationHelperIn, stateHelperIn, operationHelperIn);
        }
    }

    private TestAbc testBaseCommon;
    private ConfigurationHelper mockConfigHelper = mock(ConfigurationHelper.class);
    private StateHelper mockStateHelper = mock(StateHelper.class);

    @Before
    public void setUp() throws Exception {
        testBaseCommon = new TestAbc(null, mockConfigHelper, mockStateHelper, null);

        // to avoid operation on logger fail, mock up the logger
        EELFLogger mockLogger = mock(EELFLogger.class);
        Whitebox.setInternalState(testBaseCommon, "logger", mockLogger);
    }

    @Test
    public void testSetStatus() throws Exception {
        OAMCommandStatus oamCommandStatus = OAMCommandStatus.ACCEPTED;
        testBaseCommon.setStatus(oamCommandStatus);
        Status status = testBaseCommon.status;
        Assert.assertEquals("Should have code", oamCommandStatus.getResponseCode(), status.getCode().intValue());
        Assert.assertEquals("Should have message", oamCommandStatus.getResponseMessage(), status.getMessage());
    }

    @Test
    public void testSetStatusWithParams() throws Exception {
        String message = "testing";
        OAMCommandStatus oamCommandStatus = OAMCommandStatus.REJECTED;
        testBaseCommon.setStatus(oamCommandStatus, message);
        Status status = testBaseCommon.status;
        Assert.assertEquals("Should have code", oamCommandStatus.getResponseCode(), status.getCode().intValue());
        Assert.assertTrue("Should have message", status.getMessage().endsWith(message));
    }

    @Test
    public void testSetErrorStatus() throws Exception {
        Mockito.doReturn("testName").when(mockConfigHelper).getAppcName();
        Mockito.doReturn(AppcOamStates.Started).when(mockStateHelper).getCurrentOamState();
        Whitebox.setInternalState(testBaseCommon, "rpc", AppcOam.RPC.maintenance_mode);

        String exceptionMessage = "testing";

        OAMCommandStatus oamCommandStatus = OAMCommandStatus.INVALID_PARAMETER;
        Throwable t = new InvalidInputException(exceptionMessage);
        testBaseCommon.setErrorStatus(t);
        Status status = testBaseCommon.status;
        Assert.assertEquals("Should have code", oamCommandStatus.getResponseCode(), status.getCode().intValue());

        oamCommandStatus = OAMCommandStatus.REJECTED;
        t = new InvalidStateException(exceptionMessage);
        testBaseCommon.setErrorStatus(t);
        status = testBaseCommon.status;
        Assert.assertEquals("Should have code", oamCommandStatus.getResponseCode(), status.getCode().intValue());

        oamCommandStatus = OAMCommandStatus.UNEXPECTED_ERROR;
        t = new NullPointerException(exceptionMessage);
        testBaseCommon.setErrorStatus(t);
        status = testBaseCommon.status;
        Assert.assertEquals("Should have code", oamCommandStatus.getResponseCode(), status.getCode().intValue());
    }

}
