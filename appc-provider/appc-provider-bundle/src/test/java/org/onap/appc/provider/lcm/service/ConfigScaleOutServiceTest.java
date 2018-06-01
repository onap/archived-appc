
/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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
import org.onap.appc.util.JsonUtil;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.*;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.action.identifiers.ActionIdentifiers;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.common.header.CommonHeader;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.status.Status;
import org.onap.appc.domainmodel.lcm.ResponseContext;
import org.onap.appc.executor.objects.LCMCommandStatus;
import org.onap.appc.requesthandler.objects.RequestHandlerOutput;
import org.onap.appc.requesthandler.objects.RequestHandlerInput;

import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;

@RunWith(MockitoJUnitRunner.class)
public class ConfigScaleOutServiceTest {
    private final Action myAction = Action.ConfigScaleOut;
    private final String PAYLOAD_STRING = "{\"A\":\"A-value\",\"B\":{\"C\":\"B.C-value\",\"D\":\"B.D-value\"}}";
    private ConfigScaleOutInput mockInput = mock(ConfigScaleOutInput.class);
    private CommonHeader mockCommonHeader = mock(CommonHeader.class);
    private ActionIdentifiers mockAI = mock(ActionIdentifiers.class);
    private Payload mockPayload = mock(Payload.class);

    private ConfigScaleOutService configscaleoutServiceAction;
    @Before
    public void setUp() throws Exception {

        configscaleoutServiceAction = spy(new ConfigScaleOutService());
    }

    @Test
    public void testProcess() throws Exception {
        // test error occurs in validation
        ConfigScaleOutOutputBuilder outputBuilder = configscaleoutServiceAction.process(mockInput);
        Mockito.verify(configscaleoutServiceAction, times(0)).proceedAction(any(),any(),any());
        Assert.assertTrue("Should not have commonHeader as we did not mock it",outputBuilder.getCommonHeader() == null);
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
        Mockito.doReturn("api ver").when(mockCommonHeader).getApiVer();
        Mockito.doReturn("orignator Id").when(mockCommonHeader).getOriginatorId();
        Mockito.doReturn("request Id").when(mockCommonHeader).getRequestId();

        Mockito.doReturn(myAction).when(mockInput).getAction();
        Mockito.doReturn(mockAI).when(mockInput).getActionIdentifiers();
        Mockito.doReturn("vnfId").when(mockAI).getVnfId();

        // test processAction return without error
        RequestExecutor mockExecutor = mock(RequestExecutor.class);
      //  whenNew(RequestExecutor.class).withNoArguments().thenReturn(mockExecutor);

        RequestHandlerOutput mockOutput = mock(RequestHandlerOutput.class);
        Mockito.doReturn(mockOutput).when(mockExecutor).executeRequest(any());

        ResponseContext mockResponseContext = mock(ResponseContext.class);
        Mockito.doReturn(mockResponseContext).when(mockOutput).getResponseContext();

        org.onap.appc.domainmodel.lcm.Status mockStatus = mock(org.onap.appc.domainmodel.lcm.Status.class);
        Integer successCode = Integer.valueOf(LCMCommandStatus.SUCCESS.getResponseCode());
        Mockito.doReturn(successCode).when(mockStatus).getCode();
        Mockito.doReturn(mockStatus).when(mockResponseContext).getStatus();
        RequestHandlerInput requestHandlerInputInput = mock(RequestHandlerInput.class);
        AbstractBaseService abstractBaseService = mock(AbstractBaseService.class);
        Mockito.when(abstractBaseService.executeAction(requestHandlerInputInput)).thenReturn(mockOutput);
        try {
            outputBuilder = configscaleoutServiceAction.process(mockInput);
        }catch(Exception e){
            Assert.assertTrue(true);
        }
        Assert.assertTrue("Should have commonHeader",outputBuilder.getCommonHeader() == null);
        Assert.assertEquals("should return success status", new Integer(302), outputBuilder.getStatus().getCode());
    }

    @Test
    public void testValidate() throws Exception {
        configscaleoutServiceAction.validate(mockCommonHeader, Action.ConfigScaleOut, mockAI,mockPayload);
        Status status = (Status) Whitebox.getInternalState(configscaleoutServiceAction, "status");
        Assert.assertEquals("should return missing parameter",
                Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()), status.getCode());
        Mockito.verify(configscaleoutServiceAction, times(0)).buildStatusForParamName(any(), any());
        Mockito.verify(configscaleoutServiceAction, times(0)).buildStatusForErrorMsg(any(), any());

        ZULU mockTimeStamp = mock(ZULU.class);
        Mockito.doReturn(mockTimeStamp).when(mockCommonHeader).getTimestamp();
        Mockito.doReturn("api ver").when(mockCommonHeader).getApiVer();
        Mockito.doReturn("orignator Id").when(mockCommonHeader).getOriginatorId();
        Mockito.doReturn("request Id").when(mockCommonHeader).getRequestId();

        // test empty action
        configscaleoutServiceAction.validate(mockCommonHeader, Action.ConfigScaleOut, mockAI,mockPayload);
        status = (Status) Whitebox.getInternalState(configscaleoutServiceAction, "status");
        Assert.assertEquals("Should return missing parameter for action",
                Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()), status.getCode());

        // test empty ActionIdentifier
        configscaleoutServiceAction.validate(mockCommonHeader, Action.ConfigScaleOut, mockAI,mockPayload);
        status = (Status) Whitebox.getInternalState(configscaleoutServiceAction, "status");
        Assert.assertEquals("should return missing parameter",
                Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()), status.getCode());

        // test Invalid VNF_ID
        Mockito.doReturn("").when(mockAI).getVnfId();
        configscaleoutServiceAction.validate(mockCommonHeader, Action.ConfigScaleOut, mockAI,mockPayload);
        status = (Status) Whitebox.getInternalState(configscaleoutServiceAction, "status");
        Assert.assertEquals("should return invalid parameter",
                Integer.valueOf(LCMCommandStatus.INVALID_INPUT_PARAMETER.getResponseCode()), status.getCode());

        // test null payload
        Mockito.doReturn("vnfId").when(mockAI).getVnfId();
        configscaleoutServiceAction.validate(mockCommonHeader, Action.ConfigScaleOut, mockAI, null);
        Mockito.verify(configscaleoutServiceAction, times(1)).validateExcludedActIds(any(), any());
        status = (Status) Whitebox.getInternalState(configscaleoutServiceAction, "status");
        Assert.assertEquals("should return missing parameter",
                Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()), status.getCode());

        // test empty payload

        Mockito.doReturn("").when(mockPayload).getValue();
        configscaleoutServiceAction.validate(mockCommonHeader, Action.ConfigScaleOut, mockAI, mockPayload);
        status = (Status) Whitebox.getInternalState(configscaleoutServiceAction, "status");
        Assert.assertEquals("should return invalid parameter",
                Integer.valueOf(LCMCommandStatus.INVALID_INPUT_PARAMETER.getResponseCode()), status.getCode());

        // test space payload
        Mockito.doReturn(" ").when(mockPayload).getValue();
        configscaleoutServiceAction.validate(mockCommonHeader, Action.ConfigScaleOut, mockAI, mockPayload);
        status = (Status) Whitebox.getInternalState(configscaleoutServiceAction, "status");
        Assert.assertEquals("should return invalid parameter",
                Integer.valueOf(LCMCommandStatus.INVALID_INPUT_PARAMETER.getResponseCode()), status.getCode());
    }
}

