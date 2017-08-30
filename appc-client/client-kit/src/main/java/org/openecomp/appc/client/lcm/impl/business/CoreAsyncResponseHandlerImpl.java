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

import org.openecomp.appc.client.impl.core.CoreException;
import org.openecomp.appc.client.impl.core.ICoreAsyncResponseHandler;
import org.openecomp.appc.client.lcm.api.ResponseHandler;
import org.openecomp.appc.client.lcm.exceptions.AppcClientException;
import com.fasterxml.jackson.databind.ObjectMapper;

class CoreAsyncResponseHandlerImpl<T> extends CoreResponseHandler implements ICoreAsyncResponseHandler {

    private final ResponseHandler<T> responseHandler;

    CoreAsyncResponseHandlerImpl(ResponseHandler<T> responseHandler, Class<T> rpcOutput, ObjectMapper mapper) {
        super(rpcOutput, mapper);
        this.responseHandler = responseHandler;
    }

    public boolean onResponse(String message, String type) {
        Boolean[] isFinal = new Boolean[1];
        isFinal[0] = false;
        try {
            T responseObject = (T) super.getResponse(message, type, isFinal);
            responseHandler.onResponse(responseObject);
            return isFinal[0];
        } catch (Exception e) {
            this.onException(e);
            isFinal[0] = true;
        }
        return isFinal[0];
    }

    public void onException(Exception e) {
        AppcClientException ex = new AppcClientException(e);
        responseHandler.onException(ex);
    }
}
