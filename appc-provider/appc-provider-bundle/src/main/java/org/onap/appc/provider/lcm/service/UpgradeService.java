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

import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.Action;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.UpgradePreCheckInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.UpgradePreCheckOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.UpgradeSoftwareInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.UpgradeSoftwareOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.UpgradePostCheckInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.UpgradePostCheckOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.UpgradeBackupInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.UpgradeBackupOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.UpgradeBackoutInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.UpgradeBackoutOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.Payload;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.action.identifiers.ActionIdentifiers;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.common.header.CommonHeader;
import org.onap.appc.executor.objects.LCMCommandStatus;
import org.onap.appc.requesthandler.objects.RequestHandlerInput;
import org.onap.appc.util.JsonUtil;

import java.io.IOException;
import java.util.Map;

/**
 * Provide LCM command service for attach/detach a cinder to a VM
 */
public class UpgradeService extends AbstractBaseService {
	/**
     * Constructor
     *
     */
    public UpgradeService(String upgrade) {
    	switch(upgrade){
    		case "upgradePre":
                rpcName = getRpcName(Action.UpgradePreCheck);
                expectedAction = Action.UpgradePreCheck;
                break;
    		case "upgradePost":
                rpcName = getRpcName(Action.UpgradePostCheck);
                expectedAction = Action.UpgradePostCheck;
                break;
    		case "upgradeSoft":
                rpcName = getRpcName(Action.UpgradeSoftware);
                expectedAction = Action.UpgradeSoftware;
                break;
    		case "upgradeBackup":
                rpcName = getRpcName(Action.UpgradeBackup);
                expectedAction =  Action.UpgradeBackup;
                break;
    		case "upgradeBackout":
                rpcName = getRpcName(Action.UpgradeBackout);
                expectedAction = Action.UpgradeBackout;
                break;
    	}
        logger.debug("UpgradeService starts ", upgrade);
    }
    
    public UpgradePreCheckOutputBuilder upgradePreCheck(UpgradePreCheckInput input) {
        CommonHeader commonHeader = input.getCommonHeader();
        ActionIdentifiers actionIdentifiers = input.getActionIdentifiers();
        Payload payload = input.getPayload();

        validate(commonHeader, input.getAction(), actionIdentifiers, payload);
        if (status == null) {
            proceedAction(commonHeader, actionIdentifiers, payload);
        }

        UpgradePreCheckOutputBuilder outputBuilder = new UpgradePreCheckOutputBuilder();
        outputBuilder.setStatus(status);
        outputBuilder.setCommonHeader(input.getCommonHeader());
        return outputBuilder;
    }

    
    public UpgradeSoftwareOutputBuilder upgradeSoftware(UpgradeSoftwareInput input) {
        CommonHeader commonHeader = input.getCommonHeader();
        ActionIdentifiers actionIdentifiers = input.getActionIdentifiers();
        Payload payload = input.getPayload();

        validate(commonHeader, input.getAction(), actionIdentifiers, payload);
        if (status == null) {
            proceedAction(commonHeader, actionIdentifiers, payload);
        }

        UpgradeSoftwareOutputBuilder outputBuilder = new UpgradeSoftwareOutputBuilder();
        outputBuilder.setStatus(status);
        outputBuilder.setCommonHeader(input.getCommonHeader());
        return outputBuilder;
    }
    public UpgradePostCheckOutputBuilder upgradePostCheck(UpgradePostCheckInput input) {
        CommonHeader commonHeader = input.getCommonHeader();
        ActionIdentifiers actionIdentifiers = input.getActionIdentifiers();
        Payload payload = input.getPayload();

        validate(commonHeader, input.getAction(), actionIdentifiers, payload);
        if (status == null) {
            proceedAction(commonHeader, actionIdentifiers, payload);
        }

        UpgradePostCheckOutputBuilder outputBuilder = new UpgradePostCheckOutputBuilder();
        outputBuilder.setStatus(status);
        outputBuilder.setCommonHeader(input.getCommonHeader());
        return outputBuilder;
    }
    
    public UpgradeBackupOutputBuilder upgradeBackup(UpgradeBackupInput input) {
        CommonHeader commonHeader = input.getCommonHeader();
        ActionIdentifiers actionIdentifiers = input.getActionIdentifiers();
        Payload payload = input.getPayload();

        validate(commonHeader, input.getAction(), actionIdentifiers, payload);
        if (status == null) {
            proceedAction(commonHeader, actionIdentifiers, payload);
        }

        UpgradeBackupOutputBuilder outputBuilder = new UpgradeBackupOutputBuilder();
        outputBuilder.setStatus(status);
        outputBuilder.setCommonHeader(input.getCommonHeader());
        return outputBuilder;
    }
    public UpgradeBackoutOutputBuilder upgradeBackout(UpgradeBackoutInput input) {
        CommonHeader commonHeader = input.getCommonHeader();
        ActionIdentifiers actionIdentifiers = input.getActionIdentifiers();
        Payload payload = input.getPayload();

        validate(commonHeader, input.getAction(), actionIdentifiers, payload);
        if (status == null) {
            proceedAction(commonHeader, actionIdentifiers, payload);
        }

        UpgradeBackoutOutputBuilder outputBuilder = new UpgradeBackoutOutputBuilder();
        outputBuilder.setStatus(status);
        outputBuilder.setCommonHeader(input.getCommonHeader());
        return outputBuilder;
    }
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
            status = buildStatusForParamName(LCMCommandStatus.MISSING_MANDATORY_PARAMETER, keyName);
            return;
        }
        String payloadString = payload.getValue();
        status = validateMustHaveParamValue(
            payloadString == null ? payloadString : payloadString.trim(), "payload");
        if (status != null) {
            return;
        }

        try {
            Map<String, String> payloadMap = JsonUtil.convertJsonStringToFlatMap(payloadString);
            validateMustHaveParamValue(payloadMap.get(keyName), keyName);
        } catch (IOException e) {
            logger.error(String.format("UpgradeService (%s) got IOException when converting payload", rpcName), e);
            status = buildStatusForErrorMsg(LCMCommandStatus.UNEXPECTED_ERROR, e.getMessage());
        }
    }

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
