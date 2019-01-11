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
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.Action;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.QueryInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.QueryOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.ZULU;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.action.identifiers.ActionIdentifiers;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.common.header.CommonHeader;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.query.output.QueryResults;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.status.Status;
import org.onap.appc.domainmodel.lcm.ResponseContext;
import org.onap.appc.executor.objects.LCMCommandStatus;
import org.onap.appc.requesthandler.objects.RequestHandlerOutput;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.powermock.api.mockito.PowerMockito.whenNew;

public class QueryServiceTest {
    private final Action myAction = Action.Query;

    private QueryInput mockInput = mock(QueryInput.class);
    private CommonHeader mockCommonHeader = mock(CommonHeader.class);
    private ActionIdentifiers mockAI = mock(ActionIdentifiers.class);

    private QueryService queryService;

    @Before
    public void setUp() throws Exception {
        queryService = spy(new QueryService());

        Mockito.doReturn(mockCommonHeader).when(mockInput).getCommonHeader();
    }

    @Test
    public void testConstructor() throws Exception {
        Assert.assertEquals("Should have proper ACTION", myAction,
            (Action) org.powermock.reflect.Whitebox.getInternalState(queryService, "expectedAction"));
        Assert.assertEquals("Should have query RPC name", myAction.name().toLowerCase(),
            (org.powermock.reflect.Whitebox.getInternalState(queryService, "rpcName")).toString());
    }

    @Test
    public void testProcess() throws Exception {
        // test error occurs in validation
        QueryOutputBuilder queryOutputBuilder = queryService.process(mockInput);
        Mockito.verify(queryService, times(0)).proceedAction(mockInput);
        Assert.assertTrue("Should have commonHeader",queryOutputBuilder.getCommonHeader() != null);
        Assert.assertTrue("Should not have queryResults",queryOutputBuilder.getQueryResults() == null);
        Assert.assertEquals("should return missing parameter status",
            Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()),
            queryOutputBuilder.getStatus().getCode());

        // to make validation pass
        ZULU zuluTimeStamp = new ZULU("2017-06-29T21:44:00.35Z");
        Mockito.doReturn(zuluTimeStamp).when(mockCommonHeader).getTimestamp();
        Mockito.doReturn("api ver").when(mockCommonHeader).getApiVer();
        Mockito.doReturn("orignator Id").when(mockCommonHeader).getOriginatorId();
        Mockito.doReturn("request Id").when(mockCommonHeader).getRequestId();

        Mockito.doReturn(myAction).when(mockInput).getAction();
        Mockito.doReturn(mockAI).when(mockInput).getActionIdentifiers();
        Mockito.doReturn("vnfId").when(mockAI).getVnfId();

        // test processAction return with error
        queryOutputBuilder = queryService.process(mockInput);
        Mockito.verify(queryService, times(1)).proceedAction(mockInput);
        Assert.assertTrue("Should have commonHeader",queryOutputBuilder.getCommonHeader() != null);
        Assert.assertTrue("Should not have queryResults",queryOutputBuilder.getQueryResults() == null);
        Assert.assertEquals("should return rejected status",
            Integer.valueOf(LCMCommandStatus.REJECTED.getResponseCode()),
            queryOutputBuilder.getStatus().getCode());

        // test processAction return without error
        RequestExecutor mockExecutor = mock(RequestExecutor.class);
        whenNew(RequestExecutor.class).withNoArguments().thenReturn(mockExecutor);

        RequestHandlerOutput mockOutput = mock(RequestHandlerOutput.class);
        Mockito.doReturn(mockOutput).when(mockExecutor).executeRequest(any());

        ResponseContext mockResponseContext = mock(ResponseContext.class);
        Mockito.doReturn(mockResponseContext).when(mockOutput).getResponseContext();

        List<QueryResults> results = new ArrayList<>();
        QueryResults mockResult = mock(QueryResults.class);
        results.add(mockResult);
        Mockito.doReturn(results).when(mockResponseContext).getPayloadObject();

        org.onap.appc.domainmodel.lcm.Status mockStatus = mock(org.onap.appc.domainmodel.lcm.Status.class);
        Integer successCode = Integer.valueOf(LCMCommandStatus.SUCCESS.getResponseCode());
        Mockito.doReturn(successCode).when(mockStatus).getCode();
        Mockito.doReturn(mockStatus).when(mockResponseContext).getStatus();

        queryOutputBuilder = queryService.process(mockInput);
        Assert.assertTrue("Should have commonHeader",queryOutputBuilder.getCommonHeader() != null);
    }

    @Test
    public void testValidate() throws Exception {
        // test commonHeader error
        queryService.validate(mockInput);
        Status status = (Status) Whitebox.getInternalState(queryService, "status");
        Assert.assertEquals("should return missing parameter",
            Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()), status.getCode());
        Mockito.verify(queryService, times(0)).buildStatusForParamName(any(), any());
        Mockito.verify(queryService, times(0)).buildStatusForErrorMsg(any(), any());

        ZULU mockTimeStamp = mock(ZULU.class);
        Mockito.doReturn(mockTimeStamp).when(mockCommonHeader).getTimestamp();
        Mockito.doReturn("api ver").when(mockCommonHeader).getApiVer();
        Mockito.doReturn("orignator Id").when(mockCommonHeader).getOriginatorId();
        Mockito.doReturn("request Id").when(mockCommonHeader).getRequestId();

        // test empty ActionIdentifier
        Mockito.doReturn(mockAI).when(mockInput).getActionIdentifiers();
        Mockito.doReturn(myAction).when(mockInput).getAction();
        queryService.validate(mockInput);
        status = (Status) Whitebox.getInternalState(queryService, "status");
        Assert.assertEquals("should return missing parameter",
            Integer.valueOf(LCMCommandStatus.MISSING_MANDATORY_PARAMETER.getResponseCode()), status.getCode());

        // test empty VNF_ID
        Mockito.doReturn("").when(mockAI).getVnfId();
        queryService.validate(mockInput);
        status = (Status) Whitebox.getInternalState(queryService, "status");
        Assert.assertEquals("should return invalid parameter",
            Integer.valueOf(LCMCommandStatus.INVALID_INPUT_PARAMETER.getResponseCode()), status.getCode());

        // test calling validateExcludeActId
        Mockito.doReturn("vnfId").when(mockAI).getVnfId();
        queryService.validate(mockInput);
        Mockito.verify(queryService, times(1)).validateExcludedActIds(any(), any());
    }
}
