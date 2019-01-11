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
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.Action;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.ActionStatusInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.ActionStatusOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.Payload;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.ZULU;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.action.identifiers.ActionIdentifiers;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.common.header.CommonHeader;
import org.onap.appc.domainmodel.lcm.ActionLevel;
import org.onap.appc.domainmodel.lcm.RequestContext;
import org.onap.appc.domainmodel.lcm.ResponseContext;
import org.onap.appc.domainmodel.lcm.Status;
import org.onap.appc.executor.objects.LCMCommandStatus;
import org.onap.appc.provider.lcm.util.RequestInputBuilder;
import org.onap.appc.requesthandler.objects.RequestHandlerInput;
import org.onap.appc.requesthandler.objects.RequestHandlerOutput;
import org.powermock.reflect.Whitebox;

import java.text.ParseException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.powermock.api.mockito.PowerMockito.whenNew;


public class ActionStatusServiceTest {
    private RequestHandlerInput mockRequestHandlerInput = mock(RequestHandlerInput.class);
    private RequestContext mockRequestContext = mock(RequestContext.class);
    private RequestHandlerOutput mockOutput = mock(RequestHandlerOutput.class);
    private ResponseContext mockResponseContext = mock(ResponseContext.class);

    private ActionStatusService actionStatusService;

    @Before
    public void setUp() throws Exception {
        Mockito.doReturn(mockRequestContext).when(mockRequestHandlerInput).getRequestContext();
        actionStatusService = spy(new ActionStatusService());
    }

    @Test
    public void testConstructor() throws Exception {
        Assert.assertEquals("Should have proper ACTION", Action.ActionStatus,
            (Action) Whitebox.getInternalState(actionStatusService, "expectedAction"));
        Assert.assertEquals("Should have action-status RPC name", "action-status",
            (Whitebox.getInternalState(actionStatusService, "rpcName")).toString());
    }

    @Test
    public void testQueryStatus() throws Exception {
        // ===========   test input validation ============
        CommonHeader mockCommonHeader = mock(CommonHeader.class);
        ActionStatusInput mockInput = mock(ActionStatusInput.class);

        Mockito.doReturn(mockCommonHeader).when(mockInput).getCommonHeader();
        // test commonHeader error
        ActionStatusOutputBuilder output = actionStatusService.queryStatus(mockInput);
        Assert.assertEquals("Should have commonHeader", mockCommonHeader, output.getCommonHeader());
        Assert.assertEquals("should return missing parameter",
            Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()),
            output.getStatus().getCode());

        ZULU zuluTimeStamp = new ZULU("2017-06-29T21:44:00.35Z");
        Mockito.doReturn(zuluTimeStamp).when(mockCommonHeader).getTimestamp();
        Mockito.doReturn("api ver").when(mockCommonHeader).getApiVer();
        Mockito.doReturn("originator Id").when(mockCommonHeader).getOriginatorId();
        Mockito.doReturn("request Id").when(mockCommonHeader).getRequestId();

        // test invalid action
        Mockito.doReturn(Action.Query).when(mockInput).getAction();
        output = actionStatusService.queryStatus(mockInput);
        Assert.assertEquals("Should have commonHeader", mockCommonHeader, output.getCommonHeader());
        Assert.assertEquals("Should return invalid parameter for action",
            Integer.valueOf(LCMCommandStatus.INVALID_INPUT_PARAMETER.getResponseCode()),
            output.getStatus().getCode());

        // test null actionIdentifier
        Mockito.doReturn(Action.ActionStatus).when(mockInput).getAction();
        output = actionStatusService.queryStatus(mockInput);
        Assert.assertEquals("Should have commonHeader", mockCommonHeader, output.getCommonHeader());
        Assert.assertEquals("should return missing parameter",
            Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()),
            output.getStatus().getCode());

        // test missing VNF ID
        ActionIdentifiers mockAI = mock(ActionIdentifiers.class);
        Mockito.doReturn(mockAI).when(mockInput).getActionIdentifiers();
        output = actionStatusService.queryStatus(mockInput);
        Assert.assertEquals("Should have commonHeader", mockCommonHeader, output.getCommonHeader());
        Assert.assertEquals("should return missing parameter",
            Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()),
            output.getStatus().getCode());

        // test invalid VNF ID
        Mockito.doReturn("").when(mockAI).getVnfId();
        output = actionStatusService.queryStatus(mockInput);
        Assert.assertEquals("Should have commonHeader", mockCommonHeader, output.getCommonHeader());
        Assert.assertEquals("Should return invalid parameter for action",
            Integer.valueOf(LCMCommandStatus.INVALID_INPUT_PARAMETER.getResponseCode()),
            output.getStatus().getCode());

        // test null payload
        Mockito.doReturn("test VNF ID").when(mockAI).getVnfId();
        output = actionStatusService.queryStatus(mockInput);
        Assert.assertEquals("Should have commonHeader", mockCommonHeader, output.getCommonHeader());
        Assert.assertEquals("should return missing parameter",
            Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()),
            output.getStatus().getCode());

        // test payload with empty string
        Payload mockPayload = mock(Payload.class);
        Mockito.doReturn(mockPayload).when(mockInput).getPayload();
        output = actionStatusService.queryStatus(mockInput);
        Assert.assertEquals("Should have commonHeader", mockCommonHeader, output.getCommonHeader());
        Assert.assertEquals("should return invalid parameter",
            Integer.valueOf(LCMCommandStatus.INVALID_INPUT_PARAMETER.getResponseCode()),
            output.getStatus().getCode());

        // test validation passed
        Mockito.doReturn("testing payload").when(mockPayload).getValue();

        // ===========   test success ============
        RequestExecutor mockExecutor = mock(RequestExecutor.class);
        whenNew(RequestExecutor.class).withNoArguments().thenReturn(mockExecutor);
        Mockito.doReturn(mockOutput).when(mockExecutor).executeRequest(any());
        Mockito.doReturn(mockPayload).when(mockExecutor).getPayload(mockOutput);

        Mockito.doReturn(mockResponseContext).when(mockOutput).getResponseContext();

        Integer statusCode = 400;
        Status mockStatus = mock(Status.class);
        Mockito.doReturn(statusCode).when(mockStatus).getCode();
        Mockito.doReturn(mockStatus).when(mockResponseContext).getStatus();
    }

    @Test
    public void testUpdateToMgmtActionLevel() throws Exception {
        actionStatusService.updateToMgmtActionLevel(mockRequestHandlerInput);
        Mockito.verify(mockRequestContext, times(1)).setActionLevel(ActionLevel.MGMT);
    }
}
