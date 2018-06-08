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

package org.onap.appc.provider.lcm.service;

import org.apache.commons.lang.StringUtils;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.Action;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.ActionStatusInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.ActionStatusOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.Payload;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.action.identifiers.ActionIdentifiers;
import org.onap.appc.domainmodel.lcm.ActionLevel;
import org.onap.appc.executor.objects.LCMCommandStatus;
import org.onap.appc.requesthandler.objects.RequestHandlerInput;
import org.onap.appc.requesthandler.objects.RequestHandlerOutput;

/**
 * Provide LCM command service for Query action status of a previously issue LCM command
 */
public class ActionStatusService extends AbstractBaseService {

    /**
     * Constructor
     */
    public ActionStatusService() {
        super(Action.ActionStatus);
        logger.debug("ActionStatusService starts");
    }

    /**
     * Query action status
     * @param input of the ActionStatusInput which contains the information about the previous LCM command
     * @return ActionStatusOuputBuilder containing query results
     */
    public ActionStatusOutputBuilder queryStatus(ActionStatusInput input) {
        Payload outputPayload = null;

        validate(input);
        ActionIdentifiers actionIdentifiers = input.getActionIdentifiers();
        if (null == status) {
            RequestHandlerInput requestHandlerInput = getRequestHandlerInput(
                input.getCommonHeader(), actionIdentifiers, input.getPayload(), this.getClass().getName());
            if (requestHandlerInput != null) {
                updateToMgmtActionLevel(requestHandlerInput);

                RequestHandlerOutput reqHandlerOutput = executeAction(requestHandlerInput);

                outputPayload = new RequestExecutor().getPayload(reqHandlerOutput);
            }
        }

        logger.info(String.format("ActionStatus execute of '%s' finished with status %s. Reason: %s",
            actionIdentifiers, status == null ? "null" : status.getCode().toString(),
            status == null ? "null" : status.getMessage()));

        ActionStatusOutputBuilder outputBuilder = new ActionStatusOutputBuilder();
        outputBuilder.setPayload(outputPayload);
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        return outputBuilder;
    }

    /**
     * Validate input for
     *   - commonHeader
     *   - Action in the input
     *   - ActionIdentifier and only has VNF ID
     *   - and payload exists and is not empty string
     * @param input of the ActionStatusInput from the REST API
     */
    void validate(ActionStatusInput input) {
        status = validateVnfId(input.getCommonHeader(), input.getAction(), input.getActionIdentifiers());
        if (status != null) {
            return;
        }

        Payload  payload = input.getPayload();
        if (payload == null) {
            status = buildStatusForParamName(LCMCommandStatus.MISSING_MANDATORY_PARAMETER, "payload");
        } else if (StringUtils.isEmpty(payload.getValue())) {
            status = buildStatusForParamName(LCMCommandStatus.INVALID_INPUT_PARAMETER, "payload");
        }
    }

    /**
     * Update request to MGMT action level
     * @param request of the RequestHandlerInput
     */
    void updateToMgmtActionLevel(RequestHandlerInput request) {
        request.getRequestContext().setActionLevel(ActionLevel.MGMT);
    }

}
