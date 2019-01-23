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

package org.onap.appc.interfaces.service.executorImpl;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.onap.appc.aai.client.aai.AaiService;
import org.onap.appc.interfaces.service.data.Request;
import org.onap.appc.interfaces.service.data.ScopeOverlap;
import org.onap.ccsdk.sli.adaptors.aai.AAIClient;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ServiceExecutorImplTest {

    private ServiceExecutorImpl executor;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void testScopeOverlapEmpty() throws Exception {
        executor = Mockito.spy(new ServiceExecutorImpl());
        ScopeOverlap scopeOverlap = Mockito.spy(new ScopeOverlap());
        List<Request> emptyList = new ArrayList<Request>();
        Mockito.doReturn(emptyList).when(scopeOverlap).getInProgressRequest();
        ObjectMapper objectMapper = Mockito.spy(new ObjectMapper());
        Mockito.doReturn(objectMapper).when(executor).getObjectMapper();
        Mockito.doReturn(scopeOverlap).when(objectMapper).readValue("{}", ScopeOverlap.class);
        assertEquals("\"requestOverlap\"  : false", executor.isRequestOverLap("{}"));
    }

    @Test
    public void testScopeOverlapWithVnfIdInCurrentRequest() throws Exception {
        executor = Mockito.spy(new ServiceExecutorImpl());
        String requestData = "{\"current-request\" :{\"action\" : \"Audit\",\"action-identifiers\" : {\"service-instance-id\" :"
                + " \"service-instance-id\",\"vnf-id\" : \"vnf-id\",\"vnfc-name\" : \"vnfc-name\",\"vf-module-id\" : \"vf-module-id\","
                + "\"vserver-id\": \"vserver-id\"}},\"in-progress-requests\" :[{\"action\" : \"HealthCheck\",\"action-identifiers\" :"
                + " {\"service-instance-id\" : \"service-instance-id1\",\"vnf-id\" : \"vnf-id1\",\"vnfc-name\" : \"vnfc-name1\",\"vf-module-id\" :"
                + " \"vf-module-id\",\"vserver-id\": \"vserver-id1\"}},{\"action\" : \"CheckLock\",\"action-identifiers\" : {\"service-instance-id\" :"
                + " \"service-instance-id2\",\"vnf-id\" : \"vnf-id2\",\"vnfc-name\" : \"vnfc-name2\",\"vf-module-id\" : \"vf-module-id2\",\"vserver-id\":"
                + " \"vserver-id2\"}}]}";
        assertEquals("\"requestOverlap\"  : true", executor.isRequestOverLap(requestData));
    }

    @Test
    public void testScopeOverlapWithoutVnfIdButWithVfModuleIdCurrentRequest() throws Exception {
        executor = Mockito.spy(new ServiceExecutorImpl());
        String requestData = "{\"current-request\" :{\"action\" : \"Audit\",\"action-identifiers\" : {\"service-instance-id\" :"
                + " \"service-instance-id\",\"vnfc-name\" : \"vnfc-name\",\"vf-module-id\" : \"vf-module-id\","
                + "\"vserver-id\": \"vserver-id\"}},\"in-progress-requests\" :[{\"action\" : \"HealthCheck\",\"action-identifiers\" :"
                + " {\"service-instance-id\" : \"service-instance-id1\",\"vnfc-name\" : \"vnfc-name1\",\"vf-module-id\" :"
                + " \"vf-module-id\",\"vserver-id\": \"vserver-id1\"}},{\"action\" : \"CheckLock\",\"action-identifiers\" : {\"service-instance-id\" :"
                + " \"service-instance-id2\",\"vnfc-name\" : \"vnfc-name2\",\"vf-module-id\" : \"vf-module-id2\",\"vserver-id\":"
                + " \"vserver-id2\"}}]}";
        assertEquals("\"requestOverlap\"  : true", executor.isRequestOverLap(requestData));
    }

    @Test
    public void testScopeOverlapWithVnfIdAndInProgressRequest() throws Exception {
        executor = Mockito.spy(new ServiceExecutorImpl());
        String requestData = "{\"vnf-id\":\"vnf-id1\",\"current-request\" :{\"action\" : \"Audit\",\"action-identifiers\" : {\"service-instance-id\" :"
                + " \"service-instance-id\",\"vnfc-name\" : \"vnfc-name\",\"vf-module-id\" : \"vf-module-id\","
                + "\"vserver-id\": \"vserver-id\"}},\"in-progress-requests\" : [{\"target-id\": \"vnf-id1\",\"action\" : \"HealthCheck\",\"action-identifiers\" :"
                + " {\"service-instance-id\" : \"service-instance-id1\",\"vnfc-name\" : \"vnfc-name1\",\"vf-module-id\" :"
                + " \"vf-module-id\",\"vserver-id\": \"vserver-id1\"}},{\"action\" : \"CheckLock\",\"action-identifiers\" : {\"service-instance-id\" :"
                + " \"service-instance-id2\",\"vnfc-name\" : \"vnfc-name2\",\"vf-module-id\" : \"vf-module-id2\",\"vserver-id\":"
                + " \"vserver-id2\"}}]}";
        assertEquals("\"requestOverlap\"  : true", executor.isRequestOverLap(requestData));
    }

    @Test
    public void testScopeOverlapWithVserverIdAndInProgressRequest() throws Exception {
        executor = Mockito.spy(new ServiceExecutorImpl());
        String requestData = "{\"vnf-id\":\"\",\"current-request\" :{\"action\" : \"Audit\",\"action-identifiers\" : {\"service-instance-id\" :"
                + " \"service-instance-id\",\"vnfc-name\" : \"vnfc-name\",\"vserver-id\": \"vserver-id\"}},\"in-progress-requests\" :"
                + " [{\"target-id\": \"vnf-id1\",\"action\" : \"HealthCheck\",\"action-identifiers\" : {\"service-instance-id\" :"
                + " \"service-instance-id1\",\"vnfc-name\" : \"vnfc-name1\",\"vserver-id\": \"vserver-id1\"}},{\"action\" :"
                + " \"CheckLock\",\"action-identifiers\" : {\"service-instance-id\" : \"service-instance-id2\",\"vnfc-name\" :"
                + " \"vnfc-name2\",\"vserver-id\": \"vserver-id2\"}}]}";
        AaiService aaiServiceMock = Mockito.mock(AaiService.class);
        Mockito.doReturn(aaiServiceMock).when(executor).getAaiService(Mockito.any(AAIClient.class));
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vm-count", "1");
        Mockito.doReturn(ctx).when(executor).getSvcLogicContext();
        assertEquals("\"requestOverlap\"  : false", executor.isRequestOverLap(requestData));
    }

    @Test
    public void testScopeOverlapWithNoVnfcNameInCurrentRequest() throws Exception {
        executor = Mockito.spy(new ServiceExecutorImpl());
        String requestData = "{\"vnf-id\":\"\",\"current-request\" :{\"action\" : \"Audit\",\"action-identifiers\" : {\"service-instance-id\" :"
                + " \"service-instance-id\",\"vserver-id\": \"vserver-id\"}},\"in-progress-requests\" :"
                + " [{\"target-id\": \"vnf-id1\",\"action\" : \"HealthCheck\",\"action-identifiers\" : {\"service-instance-id\" :"
                + " \"service-instance-id1\",\"vnfc-name\" : \"vnfc-name1\",\"vserver-id\": \"vserver-id1\"}},{\"action\" :"
                + " \"CheckLock\",\"action-identifiers\" : {\"service-instance-id\" : \"service-instance-id2\",\"vnfc-name\" :"
                + " \"vnfc-name2\",\"vserver-id\": \"vserver-id2\"}}]}";
        AaiService aaiServiceMock = Mockito.mock(AaiService.class);
        Mockito.doReturn(aaiServiceMock).when(executor).getAaiService(Mockito.any(AAIClient.class));
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vm-count", "1");
        Mockito.doReturn(ctx).when(executor).getSvcLogicContext();
        assertEquals("\"requestOverlap\"  : false", executor.isRequestOverLap(requestData));
    }

    @Test
    public void testScopeOverlapWithVnfcNameAndInProgressRequest() throws Exception {
        executor = Mockito.spy(new ServiceExecutorImpl());
        String requestData = "{\"vnf-id\":\"\",\"current-request\" :{\"action\" : \"Audit\",\"action-identifiers\" : {\"service-instance-id\" :"
                + " \"service-instance-id\",\"vnfc-name\" : \"vnfc-name\"}},\"in-progress-requests\" :"
                + " [{\"target-id\": \"vnf-id1\",\"action\" : \"HealthCheck\",\"action-identifiers\" : {\"service-instance-id\" :"
                + " \"service-instance-id1\",\"vnfc-name\" : \"vnfc-name1\",\"vserver-id\": \"vserver-id1\"}},{\"action\" :"
                + " \"CheckLock\",\"action-identifiers\" : {\"service-instance-id\" : \"service-instance-id2\",\"vnfc-name\" :"
                + " \"vnfc-name2\",\"vserver-id\": \"vserver-id2\"}}]}";
        AaiService aaiServiceMock = Mockito.mock(AaiService.class);
        Mockito.doReturn(aaiServiceMock).when(executor).getAaiService(Mockito.any(AAIClient.class));
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vm-count", "1");
        Mockito.doReturn(ctx).when(executor).getSvcLogicContext();
        assertEquals("\"requestOverlap\"  : false", executor.isRequestOverLap(requestData));
    }

    @Test
    public void testScopeOverlapExceptionFlow() throws Exception {
        executor = Mockito.spy(new ServiceExecutorImpl());
        String requestData = "{\"vnf-id\":\"\",\"current-request\" :{\"action\" : \"Audit\",\"action-identifiers\" : {\"service-instance-id\" :"
                + " \"service-instance-id\"}},\"in-progress-requests\" :"
                + " [{\"target-id\": \"vnf-id1\",\"action\" : \"HealthCheck\",\"action-identifiers\" : {\"service-instance-id\" :"
                + " \"service-instance-id1\",\"vnfc-name\" : \"vnfc-name1\",\"vserver-id\": \"vserver-id1\"}},{\"action\" :"
                + " \"CheckLock\",\"action-identifiers\" : {\"service-instance-id\" : \"service-instance-id2\",\"vnfc-name\" :"
                + " \"vnfc-name2\",\"vserver-id\": \"vserver-id2\"}}]}";
        AaiService aaiServiceMock = Mockito.mock(AaiService.class);
        Mockito.doReturn(aaiServiceMock).when(executor).getAaiService(Mockito.any(AAIClient.class));
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vm-count", "1");
        Mockito.doReturn(ctx).when(executor).getSvcLogicContext();
        expectedEx.expect(Exception.class);
        expectedEx.expectMessage(" Action Identifier doesn't have VnfId, VfModuleId, VServerId, VnfcName ");
        executor.isRequestOverLap(requestData);
    }
}
