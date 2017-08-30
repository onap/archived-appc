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

import org.openecomp.appc.RPC;
import org.openecomp.appc.client.lcm.api.ResponseHandler;
import org.openecomp.appc.client.lcm.exceptions.AppcClientBusinessException;
import org.openecomp.appc.client.lcm.exceptions.AppcClientException;
import org.openecomp.appc.client.lcm.exceptions.AppcClientInternalException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

class RPCInvocator implements InvocationHandler {

    private final LCMRequestProcessor lcmRequestProcessor;

    RPCInvocator(LCMRequestProcessor lcmRequestProcessor) {
        this.lcmRequestProcessor = lcmRequestProcessor;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (isLCMRequest(method)) {
            try {
                return invokeImpl(method, args);
            }
            catch (AppcClientInternalException | AppcClientBusinessException e) {
                throw new AppcClientException(e);
            }
        } else {
            // Delegate non-RPC Object's methods (hashCode/equals/etc) to the proxy instance itself
            return method.invoke(proxy, args);
        }
    }

    private <T> T invokeImpl(Method method, Object[] args) throws AppcClientInternalException, AppcClientBusinessException {
        Object rpcInput = args[0];
        RPC annotation = method.getAnnotation(RPC.class);
        String rpcName = annotation.name();
        @SuppressWarnings("unchecked")
        Class<T> rpcOutputType = (Class<T>) annotation.outputType();

        T result = null;
        if (isAsync(method)) {
            @SuppressWarnings("unchecked")
            ResponseHandler<T> handler = (ResponseHandler<T>) args[1];
            lcmRequestProcessor.processAsync(rpcInput, rpcName, rpcOutputType, handler);
        }
        else {
            result = lcmRequestProcessor.processSync(rpcInput, rpcName, rpcOutputType);
        }
        return result;
    }

    private boolean isLCMRequest(Method method) {
        return method.isAnnotationPresent(RPC.class);
    }

    private boolean isAsync(Method method) {
        return method.getReturnType().equals(Void.TYPE);
    }
}
