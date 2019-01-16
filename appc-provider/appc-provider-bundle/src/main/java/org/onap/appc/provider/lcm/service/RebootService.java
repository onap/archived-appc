/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications (C) 2019 Ericsson
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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.provider.lcm.service;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.Action;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.RebootInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.RebootOutputBuilder;
import org.onap.appc.executor.objects.LCMCommandStatus;
import org.onap.appc.requesthandler.objects.RequestHandlerInput;
import org.onap.appc.util.JsonUtil;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.action.identifiers.ActionIdentifiers;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.common.header.CommonHeader;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.status.Status;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/**
 * Provide LCM command service for rebooting virtual machine (VM)
 */
public class RebootService extends AbstractBaseService {
    private final EELFLogger myLogger = EELFManager.getInstance().getLogger(RebootService.class);
    private static final String REBOOT_TYPE_PARAMETER = "type";
    private final List<String> rebootTypeList = Arrays.asList("HARD", "SOFT");

    /**
     * Constructor
     */
    public RebootService() {
        super(Action.Reboot);
    }

    public RebootOutputBuilder reboot(RebootInput input) {

        validate(input);
        if (status == null) {
            proceedAction(input);
        }

        RebootOutputBuilder outputBuilder = new RebootOutputBuilder();
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        return outputBuilder;
    }

    void proceedAction(RebootInput input) {
        RequestHandlerInput requestHandlerInput = getRequestHandlerInput(
                input.getCommonHeader(), input.getActionIdentifiers(), input.getPayload(), this.getClass().getName());
        if (requestHandlerInput != null) {
            executeAction(requestHandlerInput);
        }
    }

    /**
     * Validate the input.
     *
     * @param input of RebootInput from the REST API input
     */
    void validate(RebootInput input) {
        status = validateVserverIdVnfId(input.getCommonHeader(), input.getAction(), input.getActionIdentifiers());
        if (status != null) {
            return;
        }
        // validate payload
        String keyName = "payload";
        if (input.getPayload() == null) {
            status = buildStatusForParamName(LCMCommandStatus.MISSING_MANDATORY_PARAMETER, keyName);
            return;
        }
        String payloadString = input.getPayload().getValue();
        status = validateMustHaveParamValue(
                payloadString == null ? payloadString : payloadString.trim(), "payload");
        if (status != null) {
            return;
        }

        try {
            Map<String, String> payloadMap = JsonUtil.convertJsonStringToFlatMap(payloadString);
            //reboot-type validation
            final String rebootType = payloadMap.get(REBOOT_TYPE_PARAMETER);
            if (rebootType == null) {
                return;
            } else if (!rebootTypeList.contains(rebootType)) {
                status = buildStatusForErrorMsg(LCMCommandStatus.INVALID_INPUT_PARAMETER, REBOOT_TYPE_PARAMETER);
                return;
            }
        } catch (IOException e) {
            logger.error(String.format("VolumeService (%s) got IOException when converting payload", rpcName), e);
            status = buildStatusForErrorMsg(LCMCommandStatus.UNEXPECTED_ERROR, e.getMessage());
        }
    }

    Status validateVserverIdVnfId(CommonHeader commonHeader, Action action, ActionIdentifiers actionIdentifiers ) {
        Status validatedStatus = validateInput(commonHeader, action, actionIdentifiers);

        if (validatedStatus != null) {
            return validatedStatus;
        }

        validatedStatus = validateMustHaveParamValue(actionIdentifiers.getVserverId(), "vserver-id");
        if (validatedStatus == null) {
            validatedStatus = validateMustHaveParamValue(actionIdentifiers.getVnfId(), "vnf-id");
            logger.debug("check for vnf-id");
        }
        if (validatedStatus == null) {
            validatedStatus = validateExcludedActIds(actionIdentifiers, EnumSet.of(ACTID_KEYS.VSERVER_ID, ACTID_KEYS.VNF_ID));
        }
        logger.debug("check for ActIds");
        return validatedStatus;
    }
}
