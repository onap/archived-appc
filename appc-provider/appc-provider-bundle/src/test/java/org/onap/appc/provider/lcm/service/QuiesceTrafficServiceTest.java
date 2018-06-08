/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.provider.lcm.service;

import org.junit.Assert;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.*;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.action.identifiers.ActionIdentifiers;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.common.header.CommonHeader;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.status.Status;
import org.onap.appc.domainmodel.lcm.ResponseContext;
import org.onap.appc.executor.objects.LCMCommandStatus;
import org.onap.appc.requesthandler.objects.RequestHandlerOutput;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({QuiesceTrafficService.class, RequestExecutor.class})
public class QuiesceTrafficServiceTest {
    private final Action myAction = Action.QuiesceTraffic;
    private final String PAYLOAD_STRING = "{\"A\":\"A-value\",\"B\":{\"C\":\"B.C-value\",\"D\":\"B.D-value\"}}";
    private QuiesceTrafficInput mockInput = mock(QuiesceTrafficInput.class);
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

//    @Test
//    public void testProcess() throws Exception {
//        // test error occurs in validation
//        QuiesceTrafficOutputBuilder outputBuilder = quiesceServiceAction.process(mockInput);
//        Mockito.verify(quiesceServiceAction, times(0)).proceedAction(any(),any(),any());
//        Assert.assertTrue("Should not have commonHeader as we did not mock it",outputBuilder.getCommonHeader() == null);
//        Assert.assertEquals("should return missing parameter status",
//                Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()),
//                outputBuilder.getStatus().getCode());
//
//        // make validation pass
//        Mockito.doReturn(mockCommonHeader).when(mockInput).getCommonHeader();
//        Mockito.doReturn(mockPayload).when(mockInput).getPayload();
//        Mockito.doReturn(PAYLOAD_STRING).when(mockPayload).getValue();
//        // to make validation pass
//        ZULU zuluTimeStamp = new ZULU("2017-06-29T21:44:00.35Z");
//        Mockito.doReturn(zuluTimeStamp).when(mockCommonHeader).getTimestamp();
//        Mockito.doReturn("api ver").when(mockCommonHeader).getApiVer();
//        Mockito.doReturn("orignator Id").when(mockCommonHeader).getOriginatorId();
//        Mockito.doReturn("request Id").when(mockCommonHeader).getRequestId();
//
//        Mockito.doReturn(myAction).when(mockInput).getAction();
//        Mockito.doReturn(mockAI).when(mockInput).getActionIdentifiers();
//        Mockito.doReturn("vnfId").when(mockAI).getVnfId();
//
//        // test processAction return with error
//        outputBuilder = quiesceServiceAction.process(mockInput);
//        Mockito.verify(quiesceServiceAction, times(1)).proceedAction(any(),any(),any());
//        Assert.assertTrue("Should have commonHeader",outputBuilder.getCommonHeader() != null);
//        Assert.assertEquals("should return rejected status",
//                Integer.valueOf(LCMCommandStatus.REJECTED.getResponseCode()),
//                outputBuilder.getStatus().getCode());
//
//        // test processAction return without error
//        RequestExecutor mockExecutor = mock(RequestExecutor.class);
//        whenNew(RequestExecutor.class).withNoArguments().thenReturn(mockExecutor);
//
//        RequestHandlerOutput mockOutput = mock(RequestHandlerOutput.class);
//        Mockito.doReturn(mockOutput).when(mockExecutor).executeRequest(any());
//
//        ResponseContext mockResponseContext = mock(ResponseContext.class);
//        Mockito.doReturn(mockResponseContext).when(mockOutput).getResponseContext();
//
//        org.onap.appc.domainmodel.lcm.Status mockStatus = mock(org.onap.appc.domainmodel.lcm.Status.class);
//        Integer successCode = Integer.valueOf(LCMCommandStatus.SUCCESS.getResponseCode());
//        Mockito.doReturn(successCode).when(mockStatus).getCode();
//        Mockito.doReturn(mockStatus).when(mockResponseContext).getStatus();
//
//        outputBuilder = quiesceServiceAction.process(mockInput);
//        Assert.assertTrue("Should have commonHeader",outputBuilder.getCommonHeader() != null);
//        Assert.assertEquals("should return success status", successCode, outputBuilder.getStatus().getCode());
//    }

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
        Mockito.doReturn(" ").when(mockPayload).getValue();
        quiesceServiceAction.validate(mockCommonHeader, Action.QuiesceTraffic, mockAI, mockPayload);
        status = (Status) Whitebox.getInternalState(quiesceServiceAction, "status");
        Assert.assertEquals("should return invalid parameter",
                Integer.valueOf(LCMCommandStatus.INVALID_INPUT_PARAMETER.getResponseCode()), status.getCode());
    }
}
