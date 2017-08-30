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

import org.openecomp.appc.client.lcm.exceptions.AppcClientInternalException;

import org.openecomp.appc.client.lcm.model.Status;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class CoreResponseHandler<T> {
    private final Class<T> rpcOutput;
    private final ObjectMapper mapper;

    private final EELFLogger LOG = EELFManager.getInstance().getLogger(CoreResponseHandler.class);

    CoreResponseHandler(Class<T> rpcOutput, ObjectMapper mapper) {
        this.rpcOutput = rpcOutput;
        this.mapper = mapper;
    }

    T getResponse(String message, String type, Boolean[] isFinal) throws AppcClientInternalException {
        if (type.equals("error")) {
            try {
                String correlationId = getCorrelationID(message);
                LOG.error("Received response with error on correlation id: " + correlationId);
            } catch (IOException e) {
                LOG.error("Received response with error and couldn't extract correlationID");
            }
            throw new AppcClientInternalException(message);
        }
        T responseObject = jsonToResponseObject(message);
        int code = getStatusCode(responseObject);

        int family = code / 100;

        switch (family) {
            case 1:
            case 5:
                // not final
                break;
            case 2:
            case 3:
            case 4:
                isFinal[0] = true;
                // final
                break;
            default: // Should never happen
                throw new AppcClientInternalException(new IllegalStateException("Unsupported status code " + code + ". message: " + message));
        }
        return responseObject;

    }

    private T jsonToResponseObject(String message) throws AppcClientInternalException {
        try {
            JsonNode jsonOutput = mapper.readTree(message).get("output");
            return rpcOutput.cast(mapper.treeToValue(jsonOutput, rpcOutput));
        } catch (IOException e) {
            throw new AppcClientInternalException("failed to read message: " + message, e);
        }
    }

    private int getStatusCode(Object response) throws AppcClientInternalException {
        try {
            Method method = response.getClass().getMethod("getStatus");
            Status status = (Status) method.invoke(response);
            return status.getCode();
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new AppcClientInternalException("failed to get status code from response: " + response, e);
        }
    }

    private String getCorrelationID(String message) throws IOException {
        JsonNode common = mapper.readTree(message).get("output").get("common-header");
        String correlationId = common.get("request-id").asText() + "-" + common.get("sub-request-id").asText();
        return correlationId;
    }

}
