/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 Orange
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

package org.onap.appc.provider.lcm.service;

import org.onap.appc.executor.objects.LCMCommandStatus;
import org.onap.appc.requesthandler.objects.RequestHandlerInput;
import org.onap.appc.util.JsonUtil;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.Action;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.DistributeTrafficCheckInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.DistributeTrafficCheckOutputBuilder;

import java.io.IOException;
import java.util.Map;


/**
 * Provide LCM command service to check conditions and the result of DistributeTraffic.
 */
public class DistributeTrafficCheckService extends AbstractBaseService {

    private static final String PAYLOAD = "payload";

    /**
     * Constructor.
     */
    public DistributeTrafficCheckService() {
        super(Action.DistributeTrafficCheck);
        logger.debug("DistributeTrafficCheckService starts");
    }

    /**
     * Process the DistributeTrafficCheck request
     * @param input of DistributeTrafficCheckInput from the REST API input
     * @return DistributeTrafficCheckOutputBuilder which has the process results
     */
    public DistributeTrafficCheckOutputBuilder process(DistributeTrafficCheckInput input) {

        validate(input);
        if (status == null) {
            proceedAction(input);
        }

        DistributeTrafficCheckOutputBuilder outputBuilder = new DistributeTrafficCheckOutputBuilder();
        outputBuilder.setStatus(status);
        outputBuilder.setCommonHeader(input.getCommonHeader());
        return outputBuilder;

    }

    /**
     * Validate input.
     * Set status if any error detected. Otherwise, status == null.
     * @param input of DistributeTrafficCheckInput from the REST API input
     */
    void validate(DistributeTrafficCheckInput input) {
        status = validateVnfId(input.getCommonHeader(), input.getAction(), input.getActionIdentifiers());
        if (status != null) {
            return;
        }

        if (input.getPayload() == null) {
            status = buildStatusForParamName(LCMCommandStatus.MISSING_MANDATORY_PARAMETER, PAYLOAD);
            return;
        }
        String payloadString = input.getPayload().getValue();
        status = validateMustHaveParamValue(payloadString == null ? payloadString : payloadString.trim(), PAYLOAD);
        if (status != null) {
            return;
        }

        try {
            Map<String, String> payloadMap = JsonUtil.convertJsonStringToFlatMap(payloadString);
            validateMustHaveParamValue(payloadMap.get(PAYLOAD), PAYLOAD);
        } catch(IOException e) {
            logger.error(String.format("DistributeTrafficCheckService (%s) got IOException when converting payload", rpcName), e);
            status = buildStatusForErrorMsg(LCMCommandStatus.UNEXPECTED_ERROR, e.getMessage());
        }
    }

    /**
     * Execute the distribute-traffic-check action.
     * @param input of DistributeTrafficCheckInput from the REST API input
     */
    void proceedAction(DistributeTrafficCheckInput input) {
        RequestHandlerInput requestHandlerInput = getRequestHandlerInput(
                input.getCommonHeader(), input.getActionIdentifiers(), input.getPayload(), this.getClass().getName());
        if (requestHandlerInput != null) {
            executeAction(requestHandlerInput);
        }
    }


}
