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

package org.onap.appc.interfaces.service;

import static org.junit.Assert.assertEquals;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.appc.interfaces.service.executor.ServiceExecutor;
import org.opendaylight.yang.gen.v1.org.onap.appc.interfaces.service.rev170818.ExecuteServiceInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.interfaces.service.rev170818.ExecuteServiceInputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.interfaces.service.rev170818.ExecuteServiceOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.interfaces.service.rev170818.request.info.Request;
import org.opendaylight.yang.gen.v1.org.onap.appc.interfaces.service.rev170818.request.info.RequestBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;

public class InterfacesServiceProviderImplTest {

    @Test
    public void testExecuteService() throws InterruptedException, ExecutionException {
        InterfacesServiceProviderImpl impl = new InterfacesServiceProviderImpl();
        RequestBuilder requestBuilder = new RequestBuilder();
        String requestData = "{\"vnf-id\": \"vnf-id\",\"current-request\" :{\"action\" : \"Audit\",\"action-identifiers\" : {\"service-instance-id\" :"
                + " \"service-instance-id\",\"vnf-id\" : \"vnf-id\",\"vnfc-name\" : \"vnfc-name\",\"vf-module-id\" : \"vf-module-id\","
                + "\"vserver-id\": \"vserver-id\"}},\"in-progress-requests\" :[{\"action\" : \"HealthCheck\",\"action-identifiers\" :"
                + " {\"service-instance-id\" : \"service-instance-id1\",\"vnf-id\" : \"vnf-id1\",\"vnfc-name\" : \"vnfc-name1\",\"vf-module-id\" :"
                + " \"vf-module-id\",\"vserver-id\": \"vserver-id1\"}},{\"action\" : \"CheckLock\",\"action-identifiers\" : {\"service-instance-id\" :"
                + " \"service-instance-id2\",\"vnf-id\" : \"vnf-id2\",\"vnfc-name\" : \"vnfc-name2\",\"vf-module-id\" : \"vf-module-id2\",\"vserver-id\":"
                + " \"vserver-id2\"}}]}";
        requestBuilder.setRequestData(requestData);
        requestBuilder.setAction("isScopeOverlap");
        Request request = requestBuilder.build();
        ExecuteServiceInputBuilder inputBuilder = new ExecuteServiceInputBuilder();
        inputBuilder.setRequest(request);
        ExecuteServiceInput input = inputBuilder.build();
        Future<RpcResult<ExecuteServiceOutput>> future = impl.executeService(input);
        assertEquals("400", future.get().getResult().getStatus().getCode());
    }

    @Test
    public void testExecuteServiceExceptionFlow() throws Exception {
        InterfacesServiceProviderImpl impl = Mockito.spy(new InterfacesServiceProviderImpl());
        RequestBuilder requestBuilder = new RequestBuilder();
        String requestData = "{\"vnf-id\": \"vnf-id\",\"current-request\" :{\"action\" : \"Audit\",\"action-identifiers\" : {\"service-instance-id\" :"
                + " \"service-instance-id\",\"vnf-id\" : \"vnf-id\",\"vnfc-name\" : \"vnfc-name\",\"vf-module-id\" : \"vf-module-id\","
                + "\"vserver-id\": \"vserver-id\"}},\"in-progress-requests\" :[{\"action\" : \"HealthCheck\",\"action-identifiers\" :"
                + " {\"service-instance-id\" : \"service-instance-id1\",\"vnf-id\" : \"vnf-id1\",\"vnfc-name\" : \"vnfc-name1\",\"vf-module-id\" :"
                + " \"vf-module-id\",\"vserver-id\": \"vserver-id1\"}},{\"action\" : \"CheckLock\",\"action-identifiers\" : {\"service-instance-id\" :"
                + " \"service-instance-id2\",\"vnf-id\" : \"vnf-id2\",\"vnfc-name\" : \"vnfc-name2\",\"vf-module-id\" : \"vf-module-id2\",\"vserver-id\":"
                + " \"vserver-id2\"}}]}";
        requestBuilder.setRequestData(requestData);
        requestBuilder.setAction("isScopeOverlap");
        Request request = requestBuilder.build();
        ExecuteServiceInputBuilder inputBuilder = new ExecuteServiceInputBuilder();
        inputBuilder.setRequest(request);
        ExecuteServiceInput input = inputBuilder.build();
        ServiceExecutor executorMock = Mockito.mock(ServiceExecutor.class);
        Mockito.doThrow(new Exception()).when(executorMock).execute(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(executorMock).when(impl).getServiceExecutor();
        Future<RpcResult<ExecuteServiceOutput>> future = impl.executeService(input);
        assertEquals("401", future.get().getResult().getStatus().getCode());
    }

}
