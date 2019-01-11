/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications (C) 2019 Ericsson
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

package org.onap.appc.provider.lcm.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.*;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.action.identifiers.ActionIdentifiers;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.common.header.CommonHeader;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.status.Status;
import org.onap.appc.executor.objects.LCMCommandStatus;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;


public class QuiesceTrafficServiceTest {
    private CommonHeader mockCommonHeader = mock(CommonHeader.class);
    private ActionIdentifiers mockAI = mock(ActionIdentifiers.class);
    private Payload mockPayload = mock(Payload.class);

    private QuiesceTrafficService quiesceServiceAction;
    @Before
    public void setUp() throws Exception {
        quiesceServiceAction = spy(new QuiesceTrafficService());
    }

    @Test
    public void testConstructor() throws Exception {
        Action expectedAction = Action.QuiesceTraffic;
        Assert.assertEquals("Should have proper ACTION", expectedAction,
                (Action) org.powermock.reflect.Whitebox.getInternalState(quiesceServiceAction, "expectedAction"));
        Assert.assertEquals("Should have quiesce-traffic RPC name", "quiesce-traffic",
                (org.powermock.reflect.Whitebox.getInternalState(quiesceServiceAction, "rpcName")).toString());
    }

    @Test
    public void testValidate() throws Exception {
        quiesceServiceAction.validate(mockCommonHeader, Action.QuiesceTraffic, mockAI,mockPayload);
        Status status = (Status) Whitebox.getInternalState(quiesceServiceAction, "status");
        Assert.assertEquals("should return missing parameter",
                Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()), status.getCode());
        Mockito.verify(quiesceServiceAction, times(0)).buildStatusForParamName(any(), any());
        Mockito.verify(quiesceServiceAction, times(0)).buildStatusForErrorMsg(any(), any());

        ZULU mockTimeStamp = mock(ZULU.class);
        Mockito.doReturn(mockTimeStamp).when(mockCommonHeader).getTimestamp();
        Mockito.doReturn("api ver").when(mockCommonHeader).getApiVer();
        Mockito.doReturn("orignator Id").when(mockCommonHeader).getOriginatorId();
        Mockito.doReturn("request Id").when(mockCommonHeader).getRequestId();

        // test empty action
        quiesceServiceAction.validate(mockCommonHeader, Action.QuiesceTraffic, mockAI,mockPayload);
        status = (Status) Whitebox.getInternalState(quiesceServiceAction, "status");
        Assert.assertEquals("Should return missing parameter for action",
                Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()), status.getCode());

        // test empty ActionIdentifier
        quiesceServiceAction.validate(mockCommonHeader, Action.QuiesceTraffic, mockAI,mockPayload);
        status = (Status) Whitebox.getInternalState(quiesceServiceAction, "status");
        Assert.assertEquals("should return missing parameter",
                Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()), status.getCode());

        // test Invalid VNF_ID
        Mockito.doReturn("").when(mockAI).getVnfId();
        quiesceServiceAction.validate(mockCommonHeader, Action.QuiesceTraffic, mockAI,mockPayload);
        status = (Status) Whitebox.getInternalState(quiesceServiceAction, "status");
        Assert.assertEquals("should return invalid parameter",
                Integer.valueOf(LCMCommandStatus.INVALID_INPUT_PARAMETER.getResponseCode()), status.getCode());

        // test null payload
        Mockito.doReturn("vnfId").when(mockAI).getVnfId();
        quiesceServiceAction.validate(mockCommonHeader, Action.QuiesceTraffic, mockAI, null);
        Mockito.verify(quiesceServiceAction, times(1)).validateExcludedActIds(any(), any());
        status = (Status) Whitebox.getInternalState(quiesceServiceAction, "status");
        Assert.assertEquals("should return missing parameter",
                Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()), status.getCode());

        // test empty payload
        Mockito.doReturn("").when(mockPayload).getValue();
        quiesceServiceAction.validate(mockCommonHeader, Action.QuiesceTraffic, mockAI, mockPayload);
        status = (Status) Whitebox.getInternalState(quiesceServiceAction, "status");
        Assert.assertEquals("should return invalid parameter",
                Integer.valueOf(LCMCommandStatus.INVALID_INPUT_PARAMETER.getResponseCode()), status.getCode());

        // test space payload
        Mockito.doReturn(null).when(quiesceServiceAction).validateMustHaveParamValue(Mockito.anyString(),
                Mockito.anyString());
        Mockito.doReturn(" ").when(mockPayload).getValue();
        quiesceServiceAction.validate(mockCommonHeader, Action.QuiesceTraffic, mockAI, mockPayload);
        status = (Status) Whitebox.getInternalState(quiesceServiceAction, "status");
        Assert.assertEquals("should return invalid parameter",
                Integer.valueOf(LCMCommandStatus.UNEXPECTED_ERROR.getResponseCode()), status.getCode());
    }
}
