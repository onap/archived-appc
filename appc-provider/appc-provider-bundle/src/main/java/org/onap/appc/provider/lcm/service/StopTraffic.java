/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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

package org.onap.appc.provider.lcm.service;

import org.apache.commons.lang.StringUtils;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.Action;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.StopTrafficInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.StopTrafficOutput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.StopTrafficOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.Payload;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.action.identifiers.ActionIdentifiers;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.common.header.CommonHeader;
import org.onap.appc.requesthandler.objects.RequestHandlerInput;
import org.onap.appc.executor.objects.LCMCommandStatus;
import org.onap.appc.util.JsonUtil;

import java.io.IOException;
import java.util.Map;
/**
 * Provide LCM command service for StopTraffic VNF
 */
public class StopTraffic extends AbstractBaseService {

    /**
     * Constructor
     */
    public StopTraffic() {
        super(Action.StopTraffic);
        logger.debug("StopTraffic starts");
    }

    /**
     * Constants for characterizing payload handling
     */
    static final byte PAYLOAD_ACCEPT_NULL = 1;
    static final byte PAYLOAD_AUTO_TRIM = 2;
    static final byte PAYLOAD_TREAT_EMPTY_AS_NULL = 4;

    /**
     * Payload handling configuration for all object instances
     */
    static final byte payloadConfig = PAYLOAD_ACCEPT_NULL | PAYLOAD_AUTO_TRIM | PAYLOAD_TREAT_EMPTY_AS_NULL;

    /**
     * Process the StopTraffic request
     * @param input of StopTrafficInput from the REST API input
     * @return StopTrafficOutputBuilder which has the process results
     */
    public StopTrafficOutputBuilder process(StopTrafficInput input) {
        CommonHeader commonHeader = input.getCommonHeader();
        ActionIdentifiers actionIdentifiers = input.getActionIdentifiers();
        Payload payload = input.getPayload();

        validate(commonHeader, input.getAction(), actionIdentifiers, payload);
        if (status == null) {
            if (payload != null) {
                String payloadStr = payload.getValue();
                if (StringUtils.isEmpty(payloadStr)) {
                    if ((payloadConfig & PAYLOAD_TREAT_EMPTY_AS_NULL) != 0) {
                        payload = null;
                    }
                } else if ((payloadConfig & PAYLOAD_AUTO_TRIM) != 0) {
                    payloadStr = payloadStr.trim();
                    if (StringUtils.isEmpty(payloadStr) && (payloadConfig & PAYLOAD_TREAT_EMPTY_AS_NULL) != 0) {
                        payload = null;
                    } else {
                        payload = new Payload(payloadStr);
                    }                    
                }
            }
            proceedAction(commonHeader, actionIdentifiers, payload);
        }

        StopTrafficOutputBuilder outputBuilder = new StopTrafficOutputBuilder();
        outputBuilder.setStatus(status);
        outputBuilder.setCommonHeader(commonHeader);
        return outputBuilder;
    }

    /**
     * Validate the input.
     * Set Status if any error occurs.
     *
     * @param input of StopTrafficInput from the REST API input
     */
    void validate(CommonHeader commonHeader,
            Action action,
            ActionIdentifiers actionIdentifiers,
            Payload payload) {
        status = validateVnfId(commonHeader, action, actionIdentifiers);
        if (status != null) {
            return;
        }

        // validate payload
        String keyName = "payload";
        if (payload == null) {
            if ((payloadConfig & PAYLOAD_ACCEPT_NULL) == 0) {
                status = buildStatusForParamName(LCMCommandStatus.MISSING_MANDATORY_PARAMETER, keyName);
            }
            return;
        }
        String payloadString = payload.getValue();
        if (payloadString != null && (payloadConfig & PAYLOAD_AUTO_TRIM) != 0) {
            payloadString = payloadString.trim();
        }
        if ((payloadConfig & PAYLOAD_TREAT_EMPTY_AS_NULL) == 0) {
            status = validateMustHaveParamValue(payloadString, "payload");
            if (status != null) {
                return;
            }
        } else if (StringUtils.isEmpty(payloadString)) {
            if ((payloadConfig & PAYLOAD_ACCEPT_NULL) == 0) {
                status = buildStatusForParamName(LCMCommandStatus.MISSING_MANDATORY_PARAMETER, keyName);
            }
            return;
        }

        try {
            Map<String, String> payloadMap = JsonUtil.convertJsonStringToFlatMap(payloadString);
            // validateMustHaveParamValue(payloadMap.get(keyName), keyName);
        } catch (IOException e) {
            logger.error(String.format("StopTraffic (%s) got IOException when converting payload", rpcName), e);
            status = buildStatusForErrorMsg(LCMCommandStatus.UNEXPECTED_ERROR, e.getMessage());
        }
    }

    /**
     * Proceed to action for the StopTraffic VNF traffic.
     *
     * @param input of StopTrafficInput from the REST API input
     */
    void proceedAction(CommonHeader commonHeader,
            ActionIdentifiers actionIdentifiers,
            Payload payload) {
        RequestHandlerInput requestHandlerInput =
                getRequestHandlerInput(commonHeader, actionIdentifiers, payload, this.getClass().getName());
        if (requestHandlerInput != null) {
            executeAction(requestHandlerInput);
        }
    }
}
