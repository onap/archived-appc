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

package org.openecomp.appc.executor.impl;

import org.openecomp.appc.domainmodel.lcm.RuntimeContext;
import org.openecomp.appc.domainmodel.lcm.ActionLevel;
import org.openecomp.appc.executionqueue.MessageExpirationListener;
import org.openecomp.appc.requesthandler.RequestHandler;


public class ExpiredMessageHandler<M> implements MessageExpirationListener<M>{
    private RequestHandler vnfRequestHandler;

    private RequestHandler vmRequestHandler;

    public ExpiredMessageHandler(){

    }

    public void setVnfRequestHandler(RequestHandler vnfRequestHandler) {
        this.vnfRequestHandler = vnfRequestHandler;
    }

    public void setVmRequestHandler(RequestHandler vmRequestHandler) {
        this.vmRequestHandler = vmRequestHandler;
    }

    @Override
    public void onMessageExpiration(M message) {
        RuntimeContext commandRequest = (RuntimeContext)message;
        RequestHandler requestHandler = readRequestHandler(commandRequest);
        requestHandler.onRequestTTLEnd(commandRequest, true);
    }

    private RequestHandler readRequestHandler(RuntimeContext runtimeContext) {
        if(ActionLevel.VM.equals(runtimeContext.getRequestContext().getActionLevel())){
            return vmRequestHandler;
        }
        return vnfRequestHandler;
    }
}
