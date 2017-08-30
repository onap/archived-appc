/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.appc.client.lcm.impl.business;

import org.openecomp.appc.client.impl.core.*;
import org.openecomp.appc.client.lcm.api.ApplicationContext;
import org.openecomp.appc.client.lcm.api.ResponseHandler;
import org.openecomp.appc.client.lcm.exceptions.AppcClientBusinessException;
import org.openecomp.appc.client.lcm.exceptions.AppcClientInternalException;
import org.openecomp.appc.client.lcm.model.CommonHeader;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

class LCMRequestProcessor {

    private final IInvocationManager invocationManager;
    private final ObjectMapper mapper;

    LCMRequestProcessor(ApplicationContext context, Properties properties) throws AppcClientBusinessException {
        try{
            invocationManager = InvocationManagerFactory.getInstance();
            invocationManager.init(properties);
            mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        } catch (CoreException e) {
            throw new AppcClientBusinessException(e);
        }
    }

    <T> void processAsync(Object rpcInput, String rpcName, Class<T> rpcOutputType, ResponseHandler<T> handler) throws AppcClientInternalException {
        try {
            String correlationID = createCorrelationID(rpcInput);
            String rpcStr = marshallRPCInput(rpcInput);
            ICoreAsyncResponseHandler asyncResponseHandler = new CoreAsyncResponseHandlerImpl<T>(handler, rpcOutputType, mapper);
            invocationManager.asyncRequest(rpcStr, asyncResponseHandler, correlationID, rpcName);
        } catch (CoreException e) {
            throw new AppcClientInternalException(e);
        }
    }

    <T> T processSync(Object rpcInput, String rpcName, Class<T> rpcOutputType) throws AppcClientInternalException, AppcClientBusinessException {
        T response = null;
        try {
            String correlationID = createCorrelationID(rpcInput);
            String rpcStr = marshallRPCInput(rpcInput);
            ICoreSyncResponseHandler syncResponseHandler = new CoreSyncResponseHandlerImpl<T>(rpcOutputType, mapper);
            response = invocationManager.syncRequest(rpcStr, syncResponseHandler, correlationID, rpcName);
        }catch (CoreException e){
            if (e.getCause() instanceof AppcClientInternalException) {
                throw (AppcClientInternalException) e.getCause();
            }
            else {
                throw new AppcClientInternalException(e);
            }
        }catch (TimeoutException e){
            throw new AppcClientBusinessException(e);
        }
        return response;
    }

    private CommonHeader getCommonHeader(Object rpcInput) throws AppcClientInternalException {
        try {
            Class<?> clazz = rpcInput.getClass();
            Method method = clazz.getMethod("getCommonHeader");
            return CommonHeader.class.cast(method.invoke(rpcInput));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new AppcClientInternalException("can't get commonHeader");
        }
    }

    private String createCorrelationID(Object rpcInput) throws AppcClientInternalException {
        CommonHeader commonHeader = getCommonHeader(rpcInput);
        return commonHeader.getRequestId() + "-" + commonHeader.getSubRequestId();
    }

    private String marshallRPCInput(Object rpcInput) throws AppcClientInternalException {
        try {
            JsonNode body =  mapper.valueToTree(rpcInput);
            ObjectNode message = mapper.createObjectNode();
            message.set("input", body);
            return message.toString();
        } catch (RuntimeException e) {
            throw new AppcClientInternalException("can't marshall input", e);
        }
    }

    void shutdown(boolean isForceShutdown){
        invocationManager.shutdown(isForceShutdown);
    }
}
