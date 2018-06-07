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

import org.onap.appc.interfaces.service.executorImpl.ServiceExecutorImpl;
import org.onap.appc.interfaces.service.utils.ServiceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceExecutor {

    private static final Logger log = LoggerFactory.getLogger(ServiceExecutor.class);

    public String execute(String action, String requestData, String requestDataType) throws Exception {
        String response;
        log.info("Received execute request for action : " + action + "  with Payload : " + requestData);
        try {
            RequestValidator.validate(action, requestData, requestData);
            switch (action) {
                case ServiceConstants.REQUESTOVERLAP:
                    response = isRequestOverLap(requestData);
                    break;
                case ServiceConstants.GEDATABYMODEL:
                    response = getDataByModel(action, requestData, requestDataType);
                    break;
                default:
                    throw new ExecutorException(" Action " + action + " not found while processing request ");
            }
        } catch (Exception e) {
            log.info("Error while checking for ScopeOverlap", e);
            throw e;
        }
        return response;
    }

    private String getDataByModel(String action, String requestData, String requestDataType) {

        return null;
    }

    private String isRequestOverLap(String requestData) throws Exception {

        ServiceExecutorImpl serviceExecutor = new ServiceExecutorImpl();
        try {
            return serviceExecutor.isRequestOverLap(requestData);
        } catch (Exception e) {
            log.error("Error while checking for request overlap", e);
            throw e;
        }
    }
}
