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

package org.onap.appc.instar.interfaceImpl;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.onap.appc.instar.interfaces.ResponseHandlerInterface;
import org.onap.appc.instar.interfaces.RestClientInterface;
import org.onap.appc.system.interfaces.RuleHandlerInterface;
import org.onap.appc.instar.utils.InstarClientConstant;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.sdnc.config.params.data.Parameter;
import org.onap.sdnc.config.params.data.ResponseKey;

public class InterfaceIpAddressImpl implements RuleHandlerInterface {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(InterfaceIpAddressImpl.class);
    private Parameter parameters;
    private SvcLogicContext context;

    public InterfaceIpAddressImpl(Parameter params, SvcLogicContext ctx) {
        this.parameters = params;
        this.context = ctx;
    }

    @Override
    public void processRule() throws InstarResponseException, IOException {

        String fn = "InterfaceIpAddressHandler.processRule";
        log.info(fn + "Processing rule :" + parameters.getRuleType());
        String operationName;

        RestClientInterface restClient;
        ResponseHandlerInterface responseHandler;

        List<ResponseKey> responseKeyList = parameters.getResponseKeys();
        if (responseKeyList != null && !responseKeyList.isEmpty()) {
            for (ResponseKey filterKeys : responseKeyList) {
                if (parameters.getSource().equals(InstarClientConstant.SOURCE_SYSTEM_INSTAR)) {
                    restClient = new InstarRestClientImpl(createInstarRequestData(context));
                    responseHandler = new InstarResponseHandlerImpl(filterKeys, context);
                    operationName = "getIpAddressByVnf";

                } else {
                    throw new InstarResponseException("No Client registered for : " + parameters.getSource());
                }
                responseHandler.processResponse(restClient.sendRequest(operationName), parameters.getName());
            }
        } else {
            throw new InstarResponseException("NO response Keys set  for : " + parameters.getRuleType());
        }
    }

    private Map<String, String> createInstarRequestData(SvcLogicContext ctxt) {
        HashMap<String, String> requestParams = new HashMap<>();
        requestParams.put(InstarClientConstant.VNF_NAME, ctxt.getAttribute(InstarClientConstant.VNF_NAME));
        return requestParams;
    }
}
