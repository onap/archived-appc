/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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

package org.onap.appc.provider.lcm.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.*;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.action.identifiers.ActionIdentifiers;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.common.header.CommonHeader;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.status.Status;
import org.onap.appc.domainmodel.lcm.ResponseContext;
import org.onap.appc.executor.objects.LCMCommandStatus;
import org.onap.appc.requesthandler.objects.RequestHandlerOutput;
import org.onap.appc.requesthandler.objects.RequestHandlerInput;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest({StopTraffic.class, RequestExecutor.class})
public class StopTrafficTest {
    private final Action myAction = Action.StopTraffic;
    private final String PAYLOAD_STRING = "{\"A\":\"A-value\",\"B\":{\"C\":\"B.C-value\",\"D\":\"B.D-value\"}}";
    private StopTrafficInput mockInput = mock(StopTrafficInput.class);
    private CommonHeader mockCommonHeader = mock(CommonHeader.class);
    private ActionIdentifiers mockAI = mock(ActionIdentifiers.class);
    private Payload mockPayload = mock(Payload.class);

    private StopTraffic stopTrafficAction;

    @Before
    public void setUp() throws Exception {
        stopTrafficAction = spy(new StopTraffic());
    }

    @Test
    public void testProcess() throws Exception {
        // test error occurs in validation
        StopTrafficOutputBuilder outputBuilder = stopTrafficAction.process(mockInput);
        Mockito.verify(stopTrafficAction, times(0)).proceedAction(any(), any(), any());
        Assert.assertTrue("Should not have commonHeader as we did not mock it",
                outputBuilder.getCommonHeader() == null);
        Assert.assertEquals("should return missing parameter status",
                Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()),
                outputBuilder.getStatus().getCode());

        // make validation pass
        Mockito.doReturn(mockCommonHeader).when(mockInput).getCommonHeader();
        Mockito.doReturn(mockPayload).when(mockInput).getPayload();
        Mockito.doReturn(PAYLOAD_STRING).when(mockPayload).getValue();
        // to make validation pass
        ZULU zuluTimeStamp = new ZULU("2017-06-29T21:44:00.35Z");
        Mockito.doReturn(zuluTimeStamp).when(mockCommonHeader).getTimestamp();
        Mockito.doReturn("2.34").when(mockCommonHeader).getApiVer();
        Mockito.doReturn("originator Id").when(mockCommonHeader).getOriginatorId();
        Mockito.doReturn("request Id").when(mockCommonHeader).getRequestId();

        Mockito.doReturn(myAction).when(mockInput).getAction();
        Mockito.doReturn(mockAI).when(mockInput).getActionIdentifiers();
        Mockito.doReturn("vnfId").when(mockAI).getVnfId();

        // test processAction return without error
        RequestExecutor mockExecutor = mock(RequestExecutor.class);
        whenNew(RequestExecutor.class).withNoArguments().thenReturn(mockExecutor);

        RequestHandlerOutput mockOutput = mock(RequestHandlerOutput.class);
        Mockito.doReturn(mockOutput).when(mockExecutor).executeRequest(any());

        ResponseContext mockResponseContext = mock(ResponseContext.class);
        Mockito.doReturn(mockResponseContext).when(mockOutput).getResponseContext();
        org.onap.appc.domainmodel.lcm.Status mockStatus = mock(org.onap.appc.domainmodel.lcm.Status.class);
        Integer successCode = Integer.valueOf(LCMCommandStatus.SUCCESS.getResponseCode());
        String successMsg = "Simulated SUCCESS";
        Mockito.doReturn(successCode).when(mockStatus).getCode();
        Mockito.doReturn(successMsg).when(mockStatus).getMessage();
        Mockito.doReturn(mockStatus).when(mockResponseContext).getStatus();

        try {
            outputBuilder = stopTrafficAction.process(mockInput);
        } catch(Exception e) {
            Assert.assertTrue("Encountered Exception " + e.toString() + " processing action", false);
        }
        Assert.assertTrue("Should have commonHeader",
                outputBuilder != null && outputBuilder.getCommonHeader() != null);
        Assert.assertEquals("should return success status", successCode, outputBuilder.getStatus().getCode());
    }

    @Test
    public void testValidate() throws Exception {
        stopTrafficAction.validate(mockCommonHeader, Action.StopTraffic, mockAI, mockPayload);
        Status status = (Status) Whitebox.getInternalState(stopTrafficAction, "status");
        Assert.assertEquals("should return missing parameter",
                Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()), status.getCode());
        Mockito.verify(stopTrafficAction, times(0)).buildStatusForParamName(any(), any());
        Mockito.verify(stopTrafficAction, times(0)).buildStatusForErrorMsg(any(), any());

        Mockito.doReturn("mocked AI").when(mockAI).toString();
        Mockito.doReturn("mocked CommonHeader").when(mockCommonHeader).toString();
        Mockito.doReturn("mocked Input").when(mockInput).toString();
        Mockito.doReturn("mocked Payload").when(mockPayload).toString();
        ZULU zuluTimeStamp = new ZULU("2017-06-29T21:44:00.35Z");
        Mockito.doReturn(zuluTimeStamp).when(mockCommonHeader).getTimestamp();
        Mockito.doReturn("2.34").when(mockCommonHeader).getApiVer();
        Mockito.doReturn("originator Id").when(mockCommonHeader).getOriginatorId();
        Mockito.doReturn("request Id").when(mockCommonHeader).getRequestId();

        // test empty action
        stopTrafficAction.validate(mockCommonHeader, Action.StopTraffic, mockAI, mockPayload);
        status = (Status) Whitebox.getInternalState(stopTrafficAction, "status");
        Assert.assertEquals("Should return missing parameter for action",
                Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()),
                status.getCode());

        // test empty ActionIdentifier
        stopTrafficAction.validate(mockCommonHeader, Action.StopTraffic, mockAI, mockPayload);
        status = (Status) Whitebox.getInternalState(stopTrafficAction, "status");
        Assert.assertEquals("should return missing parameter",
                Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()),
                status.getCode());

        // test Invalid VNF_ID
        Mockito.doReturn("").when(mockAI).getVnfId();
        stopTrafficAction.validate(mockCommonHeader, Action.StopTraffic, mockAI, mockPayload);
        status = (Status) Whitebox.getInternalState(stopTrafficAction, "status");
        Assert.assertEquals("should return invalid parameter",
                Integer.valueOf(LCMCommandStatus.INVALID_INPUT_PARAMETER.getResponseCode()),
                status.getCode());

        // test null payload
        Mockito.doReturn("vnfId").when(mockAI).getVnfId();
        stopTrafficAction.validate(mockCommonHeader, Action.StopTraffic, mockAI, null);
        Mockito.verify(stopTrafficAction, times(1)).validateExcludedActIds(any(), any());
        status = (Status) Whitebox.getInternalState(stopTrafficAction, "status");
        if ((StopTraffic.payloadConfig & StopTraffic.PAYLOAD_ACCEPT_NULL) != 0) {
            Assert.assertTrue("Configuration indicates should accept null Payload", status == null);
        } else {
            Assert.assertEquals("Configuration indicates should reject null Payload",
                    Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()),
                    status.getCode());
        }

        // test empty payload
        Mockito.doReturn("").when(mockPayload).getValue();
        stopTrafficAction.validate(mockCommonHeader, Action.StopTraffic, mockAI, mockPayload);
        status = (Status) Whitebox.getInternalState(stopTrafficAction, "status");
        if ((StopTraffic.payloadConfig & StopTraffic.PAYLOAD_ACCEPT_NULL) != 0
            && (StopTraffic.payloadConfig & StopTraffic.PAYLOAD_TREAT_EMPTY_AS_NULL) != 0) {
            Assert.assertTrue("Configuration indicates should accept empty Payload", status == null);
        } else if ((StopTraffic.payloadConfig & StopTraffic.PAYLOAD_TREAT_EMPTY_AS_NULL) != 0) {
            Assert.assertEquals("Configuration indicates should reject empty Payload",
                    Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()),
                    status.getCode());
        } else {
            Assert.assertEquals("Configuration indicates should reject empty Payload",
                    Integer.valueOf(LCMCommandStatus.INVALID_INPUT_PARAMETER.getResponseCode()),
                    status.getCode());
        }

        // test space payload
        Mockito.doReturn(" ").when(mockPayload).getValue();
        stopTrafficAction.validate(mockCommonHeader, Action.StopTraffic, mockAI, mockPayload);
        status = (Status) Whitebox.getInternalState(stopTrafficAction, "status");
        if ((StopTraffic.payloadConfig & StopTraffic.PAYLOAD_ACCEPT_NULL) != 0
            && (StopTraffic.payloadConfig & StopTraffic.PAYLOAD_AUTO_TRIM) != 0
            && (StopTraffic.payloadConfig & StopTraffic.PAYLOAD_TREAT_EMPTY_AS_NULL) != 0) {
            Assert.assertTrue("Configuration indicates should accept blank Payload", status == null);
        } else if ((StopTraffic.payloadConfig & StopTraffic.PAYLOAD_AUTO_TRIM) != 0
                   && (StopTraffic.payloadConfig & StopTraffic.PAYLOAD_TREAT_EMPTY_AS_NULL) != 0) {
            Assert.assertEquals("Configuration indicates should reject blank Payload",
                    Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()),
                    status.getCode());
        } else {
            Assert.assertEquals("Configuration indicates should reject blank Payload",
                    Integer.valueOf(LCMCommandStatus.INVALID_INPUT_PARAMETER.getResponseCode()),
                    status.getCode());
        }
    }
}
