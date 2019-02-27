/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 Ericsson
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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.flow.controller.dbervices;

import static org.junit.Assert.assertNull;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.onap.appc.flow.controller.data.Transaction;
import org.onap.appc.flow.controller.utils.FlowControllerConstants;
import org.onap.ccsdk.sli.adaptors.resource.sql.SqlResource;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;

public class FlowControlDBServiceTest {

    private SqlResource sqlResource = Mockito.mock(SqlResource.class);
    private FlowControlDBService dbService;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void testGetFlowReferenceData() throws SvcLogicException {
        dbService = new FlowControlDBService(sqlResource);
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute(FlowControllerConstants.ACTION_LEVEL, "action_level");
        Mockito.when(sqlResource.query(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.any(SvcLogicContext.class))).thenReturn(QueryStatus.FAILURE);
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage(FlowControlDBService.GET_FLOW_REF_DATA_ERROR);
        dbService.getFlowReferenceData(ctx, null, new SvcLogicContext());
    }

    @Test
    public void testGetEndpointByAction() {
        dbService = new FlowControlDBService(sqlResource);
        assertNull(dbService.getEndPointByAction(null));
    }

    @Test
    public void testGetDesignTimeFlowModelFirstQueryException() throws SvcLogicException {
        dbService = new FlowControlDBService(sqlResource);
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute(FlowControllerConstants.ACTION_LEVEL, "action_level");
        Mockito.when(sqlResource.query(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.any(SvcLogicContext.class))).thenReturn(QueryStatus.FAILURE);
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage(FlowControlDBService.GET_FLOW_REF_DATA_ERROR);
        dbService.getDesignTimeFlowModel(ctx);
    }

    @Test
    public void testGetDesignTimeFlowModelSecondQueryException() throws SvcLogicException {
        dbService = new FlowControlDBService(sqlResource);
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute(FlowControllerConstants.ACTION_LEVEL, "action_level");
        Mockito.when(sqlResource.query(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.any(SvcLogicContext.class))).thenReturn(QueryStatus.SUCCESS).thenReturn(QueryStatus.FAILURE);
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage(FlowControlDBService.GET_FLOW_REF_DATA_ERROR);
        dbService.getDesignTimeFlowModel(ctx);
    }

    @Test
    public void testGetDesignTimeFlowModelNullLocalContext() throws SvcLogicException {
        dbService = new FlowControlDBService(sqlResource);
        assertNull(dbService.getDesignTimeFlowModel(null));
    }

    @Test
    public void testLoadSequenceIntoDb() throws SvcLogicException {
        dbService = new FlowControlDBService(sqlResource);
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute(FlowControllerConstants.ACTION_LEVEL, "action_level");
        Mockito.when(sqlResource.save(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyBoolean(), Mockito.anyString(),
                Mockito.any(), Mockito.anyString(), Mockito.any(SvcLogicContext.class)))
                .thenReturn(QueryStatus.FAILURE);
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("Error While processing storing Artifact: ");
        dbService.loadSequenceIntoDB(ctx);
    }

    @Test
    public void testGetProtocolTypeFirstException() throws SvcLogicException {
        dbService = new FlowControlDBService(sqlResource);
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute(FlowControllerConstants.ACTION_LEVEL, "action_level");
        Mockito.when(sqlResource.query(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.any(SvcLogicContext.class))).thenReturn(QueryStatus.FAILURE);
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage(FlowControlDBService.GET_FLOW_REF_DATA_ERROR);
        dbService.populateModuleAndRPC(new Transaction(), "vnf_type");
    }

    @Test
    public void testGetProtocolTypeSecondException() throws SvcLogicException {
        dbService = Mockito.spy(new FlowControlDBService(sqlResource));
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute(FlowControlDBService.COUNT_PROTOCOL_PARAM, "1");
        Mockito.when(sqlResource.query(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.any(SvcLogicContext.class))).thenReturn(QueryStatus.SUCCESS).thenReturn(QueryStatus.FAILURE);
        Mockito.when(dbService.getSvcLogicContext()).thenReturn(ctx);
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage(FlowControlDBService.GET_FLOW_REF_DATA_ERROR);
        dbService.populateModuleAndRPC(new Transaction(), "vnf_type");
    }

    @Test
    public void testHasSingleProtocolFirstException() throws SvcLogicException {
        dbService = Mockito.spy(new FlowControlDBService(sqlResource));
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute(FlowControlDBService.COUNT_PROTOCOL_PARAM, "2");
        Mockito.when(sqlResource.query(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.any(SvcLogicContext.class))).thenReturn(QueryStatus.SUCCESS).thenReturn(QueryStatus.FAILURE);
        Mockito.when(dbService.getSvcLogicContext()).thenReturn(ctx);
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage(FlowControlDBService.GET_FLOW_REF_DATA_ERROR);
        dbService.populateModuleAndRPC(new Transaction(), "vnf_type");
        Mockito.verify(dbService).getSvcLogicContext();
    }

    @Test
    public void testHasSingleProtocolSecondException() throws SvcLogicException {
        dbService = Mockito.spy(new FlowControlDBService(sqlResource));
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute(FlowControlDBService.COUNT_PROTOCOL_PARAM, "2");
        Mockito.when(sqlResource.query(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.any(SvcLogicContext.class))).thenReturn(QueryStatus.SUCCESS).thenReturn(QueryStatus.SUCCESS)
                .thenReturn(QueryStatus.FAILURE);
        Mockito.when(dbService.getSvcLogicContext()).thenReturn(ctx);
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("Got more than 2 values..");
        dbService.populateModuleAndRPC(new Transaction(), "vnf_type");
        Mockito.verify(dbService).getSvcLogicContext();
    }

    @Test
    public void testHasSingleProtocolThirdException() throws SvcLogicException {
        dbService = Mockito.spy(new FlowControlDBService(sqlResource));
        SvcLogicContext ctx = Mockito.spy(new SvcLogicContext());
        Mockito.when(ctx.getAttribute(FlowControlDBService.COUNT_PROTOCOL_PARAM)).thenReturn("2").thenReturn("1");
        Mockito.when(sqlResource.query(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.any(SvcLogicContext.class))).thenReturn(QueryStatus.SUCCESS).thenReturn(QueryStatus.SUCCESS)
                .thenReturn(QueryStatus.FAILURE);
        Mockito.when(dbService.getSvcLogicContext()).thenReturn(ctx);
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage(FlowControlDBService.GET_FLOW_REF_DATA_ERROR);
        dbService.populateModuleAndRPC(new Transaction(), "vnf_type");
        Mockito.verify(dbService).getSvcLogicContext();
    }

    @Test
    public void testHasSingleProtocolSuccessFlow() throws SvcLogicException {
        dbService = Mockito.spy(new FlowControlDBService(sqlResource));
        SvcLogicContext ctx = Mockito.spy(new SvcLogicContext());
        Mockito.when(ctx.getAttribute(FlowControlDBService.COUNT_PROTOCOL_PARAM)).thenReturn("2").thenReturn("1");
        Mockito.when(sqlResource.query(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.any(SvcLogicContext.class))).thenReturn(QueryStatus.SUCCESS).thenReturn(QueryStatus.SUCCESS)
                .thenReturn(QueryStatus.SUCCESS);
        Mockito.when(dbService.getSvcLogicContext()).thenReturn(ctx);
        Transaction transaction = Mockito.spy(new Transaction());
        dbService.populateModuleAndRPC(transaction, "vnf_type");
        Mockito.verify(transaction).setExecutionRPC(null);
    }

    @Test
    public void testGetDependencyInfoFirstException() throws SvcLogicException {
        dbService = new FlowControlDBService(sqlResource);
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute(FlowControllerConstants.ACTION_LEVEL, "action_level");
        Mockito.when(sqlResource.query(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.any(SvcLogicContext.class))).thenReturn(QueryStatus.FAILURE);
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("Error - while getting dependencydata ");
        dbService.getDependencyInfo(ctx);
    }

    @Test
    public void testGetDependencyInfoSecondException() throws SvcLogicException {
        dbService = new FlowControlDBService(sqlResource);
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute(FlowControllerConstants.ACTION_LEVEL, "action_level");
        Mockito.when(sqlResource.query(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.any(SvcLogicContext.class))).thenReturn(QueryStatus.SUCCESS).thenReturn(QueryStatus.FAILURE);
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("Error - while getting dependencyData ");
        dbService.getDependencyInfo(ctx);
    }

    @Test
    public void testGetCapabilitiesDataFirstException() throws SvcLogicException {
        dbService = new FlowControlDBService(sqlResource);
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute(FlowControllerConstants.ACTION_LEVEL, "action_level");
        Mockito.when(sqlResource.query(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.any(SvcLogicContext.class))).thenReturn(QueryStatus.FAILURE);
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("Error - while getting capabilitiesData ");
        dbService.getCapabilitiesData(ctx);
    }

    @Test
    public void testGetCapabilitiesDataSecondException() throws SvcLogicException {
        dbService = new FlowControlDBService(sqlResource);
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute(FlowControllerConstants.ACTION_LEVEL, "action_level");
        Mockito.when(sqlResource.query(Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.any(SvcLogicContext.class))).thenReturn(QueryStatus.SUCCESS).thenReturn(QueryStatus.FAILURE);
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("Error - while getting capabilitiesData ");
        dbService.getCapabilitiesData(ctx);
    }
}
