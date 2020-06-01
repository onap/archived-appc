/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications Copyright (C) 2019 Ericsson
 * Modifications Copyright (C) 2019 IBM
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

package org.onap.appc.interfaces.service;

import org.opendaylight.yang.gen.v1.org.onap.appc.interfaces.service.rev170818.ExecuteServiceInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.interfaces.service.rev170818.ExecuteServiceOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.interfaces.service.rev170818.ExecuteServiceOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.interfaces.service.rev170818.InterfacesServiceService;
import org.opendaylight.yang.gen.v1.org.onap.appc.interfaces.service.rev170818.response.info.ResponseInfoBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.interfaces.service.rev170818.status.StatusBuilder;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.onap.appc.interfaces.service.executor.ServiceExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

public class InterfacesServiceProviderImpl implements InterfacesServiceService{

    private static final Logger log = LoggerFactory.getLogger(InterfacesServiceProviderImpl.class);

    @Override
    public ListenableFuture<RpcResult<ExecuteServiceOutput>> executeService(ExecuteServiceInput input) {

        log.info("Received Request: " + input.getRequest().getRequestId() + " Action : " + 
                input.getRequest().getAction() + " with RequestData  :" + input.getRequest().getRequestData() + " and data-Type : " + input.getRequest().getRequestDataType());
        String requestId = input.getRequest().getRequestId();
        String action = input.getRequest().getAction();
        ResponseInfoBuilder responseInfoBuilder = new ResponseInfoBuilder();
        ExecuteServiceOutputBuilder executeServicebuilder = new ExecuteServiceOutputBuilder();
        ServiceExecutor serviceExecutor = getServiceExecutor();
        StatusBuilder statusBuilder = new StatusBuilder();
        try{
            String response = serviceExecutor.execute(action, input.getRequest().getRequestData(), input.getRequest().getRequestDataType());
            responseInfoBuilder.setBlock(response);
            responseInfoBuilder.setRequestId(requestId);
            statusBuilder.setCode("400");
            statusBuilder.setMessage("success");
        }
        catch(Exception e){
            log.error("Error" + e.getMessage());
            statusBuilder.setCode("401");
            statusBuilder.setMessage("failure");
        }
        executeServicebuilder.setResponseInfo(responseInfoBuilder.build());
        executeServicebuilder.setStatus(statusBuilder.build());
        RpcResult<ExecuteServiceOutput> result  = RpcResultBuilder.<ExecuteServiceOutput>status(true).withResult(executeServicebuilder.build()).build();
        return Futures.immediateFuture(result);
    }

    protected ServiceExecutor getServiceExecutor() {
        return new ServiceExecutor();
    }
}
