/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.interfaces.service.executor;

import java.util.ArrayList;
import java.util.List;

import org.onap.appc.interfaces.service.utils.ServiceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RequestValidator {

    private static final Logger log = LoggerFactory.getLogger(RequestValidator.class);

    public static void validate(String action, String requestData, String requestDataType) throws Exception {
        log.debug("Received validation for action= " + action + " Data :" + requestData);
        try {
            if (requestData.isEmpty()) {
                throw new Exception("Request Data is Empty");
            }
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode payloadObject = objectMapper.readTree(requestData);
            log.info("payloadObject" + payloadObject);
            if (payloadObject.get(ServiceConstants.VNF) == null)
                throw new Exception("VNF-ID is null");
            String vnfId = payloadObject.get(ServiceConstants.VNF).toString();
            if (vnfId.isEmpty())
                throw new Exception("VNF-ID is blank");
            if (payloadObject.get(ServiceConstants.CURRENTREQUEST) == null)
                throw new Exception("Current request is null");
            String cRequest = payloadObject.get(ServiceConstants.CURRENTREQUEST).toString();
            if (cRequest.isEmpty())
                throw new Exception("Current Request is blank");
            JsonNode currentRequest = payloadObject.get(ServiceConstants.CURRENTREQUEST);
            if (currentRequest.get(ServiceConstants.ACTION) == null)
                throw new Exception("Action is null in Current Request");
            String cRequestAction = currentRequest.get(ServiceConstants.ACTION).toString();
            if (cRequestAction.isEmpty())
                throw new Exception("Action is blank in Current Request");
            if (currentRequest.get(ServiceConstants.ACTIONIDENTIFIER) == null)
                throw new Exception("Action Identifier is null in Current Request");
            String cRequestActionIdentifier = currentRequest.get(ServiceConstants.ACTIONIDENTIFIER).toString();
            if (cRequestActionIdentifier.isEmpty())
                throw new Exception("Action Identifier is blank in Current Request");
            } catch (Exception e) {
            e.printStackTrace();
            log.debug("Error while validating: " + e.getMessage());
            throw e;
        }
    }

}
