/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 Orange
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.appc.domainmodel.lcm.ResponseContext;
import org.onap.appc.executor.objects.LCMCommandStatus;
import org.onap.appc.requesthandler.objects.RequestHandlerOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.*;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.action.identifiers.ActionIdentifiers;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.common.header.CommonHeader;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.status.Status;
import org.powermock.reflect.Whitebox;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(MockitoJUnitRunner.class)
public class DistributeTrafficCheckServiceTest {

    private final Action myAction = Action.DistributeTrafficCheck;
    private final String rpcName = "distribute-traffic-check";
    private  String PAYLOAD_STRING = "{\"test\":\"test\"}";

    private final DistributeTrafficCheckInput mockInput = mock(DistributeTrafficCheckInput.class);
    private final CommonHeader mockCommonHeader = mock(CommonHeader.class);
    private final ActionIdentifiers mockActionIdentifiers = mock(ActionIdentifiers.class);
    private final Payload mockPayload = mock(Payload.class);

    private DistributeTrafficCheckService distributeTrafficCheckService;

    @Before
    public void setUp() throws Exception {
        distributeTrafficCheckService = spy(new DistributeTrafficCheckService());
    }

    @Test
    public void testConstructor() throws Exception {
        assertEquals("Should have proper ACTION", myAction,
                (Action) Whitebox.getInternalState(distributeTrafficCheckService, "expectedAction"));
        assertEquals("Should have action-status RPC name", rpcName,
                Whitebox.getInternalState(distributeTrafficCheckService, "rpcName").toString());
    }

    private void helpInitializeRequestParameters() {
        Mockito.doReturn(mockCommonHeader).when(mockInput).getCommonHeader();
        Mockito.doReturn(mockPayload).when(mockInput).getPayload();
        Mockito.doReturn(myAction).when(mockInput).getAction();
        Mockito.doReturn(mockActionIdentifiers).when(mockInput).getActionIdentifiers();
        Mockito.doReturn(PAYLOAD_STRING).when(mockPayload).getValue();
        ZULU zuluTimeStamp = new ZULU("2017-06-29T21:44:00.35Z");
        Mockito.doReturn(zuluTimeStamp).when(mockCommonHeader).getTimestamp();
        Mockito.doReturn("api ver").when(mockCommonHeader).getApiVer();
        Mockito.doReturn("orignator Id").when(mockCommonHeader).getOriginatorId();
        Mockito.doReturn("request Id").when(mockCommonHeader).getRequestId();
        Mockito.doReturn("vnfId").when(mockActionIdentifiers).getVnfId();
    }

    @Test
    public void testProcess() throws Exception {
        helpInitializeRequestParameters();

        // test processAction return without error
        RequestExecutor mockExecutor = mock(RequestExecutor.class);
        whenNew(RequestExecutor.class).withNoArguments().thenReturn(mockExecutor);

        RequestHandlerOutput mockOutput = mock(RequestHandlerOutput.class);
        Mockito.doReturn(mockOutput).when(mockExecutor).executeRequest(any());

        ResponseContext mockResponseContext = mock(ResponseContext.class);
        Mockito.doReturn(mockResponseContext).when(mockOutput).getResponseContext();

        Mockito.when(distributeTrafficCheckService.executeAction(any())).thenReturn(mockOutput);

        DistributeTrafficCheckOutputBuilder outputBuilder = distributeTrafficCheckService.process(mockInput);

        Mockito.verify(distributeTrafficCheckService, times(1)).proceedAction(mockInput);

        assertNotNull("Should have commonHeader", outputBuilder.getCommonHeader());
    }

    @Test
    public void testValidateMissingParameters() throws Exception {
        DistributeTrafficCheckOutputBuilder outputBuilder = distributeTrafficCheckService.process(mockInput);
        Mockito.verify(distributeTrafficCheckService, times(0)).proceedAction(any());
        assertNull("Should not have commonHeader as we did not mock it", outputBuilder.getCommonHeader());
        assertEquals("should return missing parameter status",
                Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()),
                outputBuilder.getStatus().getCode());
    }

    @Test
    public void testValidateForMissingOrInvalidAction() throws Exception {
        helpInitializeRequestParameters();

        // check missing Action
        Mockito.doReturn(null).when(mockInput).getAction();

        distributeTrafficCheckService.validate(mockInput);
        Status status = (Status) Whitebox.getInternalState(distributeTrafficCheckService, "status");
        assertEquals("Should return missing parameter for action",
                Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()), status.getCode());

        // check invalid Action
        Mockito.doReturn(Action.Migrate).when(mockInput).getAction();
        distributeTrafficCheckService.validate(mockInput);
        status = (Status) Whitebox.getInternalState(distributeTrafficCheckService, "status");
        assertEquals("should return missing parameter",
                Integer.valueOf(LCMCommandStatus.INVALID_INPUT_PARAMETER.getResponseCode()), status.getCode());
    }

    @Test
    public void testValidateForMissingActionIdentifiers() throws Exception {
        helpInitializeRequestParameters();
        Mockito.doReturn(null).when(mockInput).getActionIdentifiers();

        // test missing ActionIdentifiers
        distributeTrafficCheckService.validate(mockInput);
        Status status = (Status) Whitebox.getInternalState(distributeTrafficCheckService, "status");
        assertEquals("should return missing parameter",
                Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()), status.getCode());
    }

    @Test
    public void testValidateEmptyOrMissingPayload() throws Exception {
        helpInitializeRequestParameters();

        // validate empty payload
        Mockito.doReturn("").when(mockPayload).getValue();
        distributeTrafficCheckService.validate(mockInput);
        Status status = (Status) Whitebox.getInternalState(distributeTrafficCheckService, "status");
        assertEquals("should return invalid parameter",
                Integer.valueOf(LCMCommandStatus.INVALID_INPUT_PARAMETER.getResponseCode()), status.getCode());

        // validate missing payload
        Mockito.doReturn(null).when(mockInput).getPayload();
        distributeTrafficCheckService.validate(mockInput);
        status = (Status) Whitebox.getInternalState(distributeTrafficCheckService, "status");
        assertEquals("should return missing parameter",
                Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()), status.getCode());

    }

}
