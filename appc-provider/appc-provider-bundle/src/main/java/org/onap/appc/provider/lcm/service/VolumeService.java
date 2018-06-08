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
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.AttachVolumeInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.AttachVolumeOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.DetachVolumeInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.DetachVolumeOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.Payload;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.action.identifiers.ActionIdentifiers;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.common.header.CommonHeader;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.status.Status;
import org.onap.appc.executor.objects.LCMCommandStatus;
import org.onap.appc.requesthandler.objects.RequestHandlerInput;
import org.onap.appc.util.JsonUtil;
import java.util.EnumSet;
import java.io.IOException;
import java.util.Map;

/**
 * Provide LCM command service for attach/detach a cinder to a VM
 */
public class VolumeService extends AbstractBaseService {
    /**
     * Constructor
     *
     * @param isAttachVolume boolean to indicate if this VolumeSerivce is created for attaching or detaching cinder
     */
    public VolumeService(boolean isAttachVolume) {
        super(isAttachVolume ? Action.AttachVolume : Action.DetachVolume);
        logger.debug("VolumeService starts ", isAttachVolume);
    }

    /**
     * Attach a cinder to the VM volume
     *
     * @param input of AttachVolumeInput from the REST API input
     * @return AttachVolumeOutputBuilder which has the details of the request results
     */
    public AttachVolumeOutputBuilder attachVolume(AttachVolumeInput input) {
        CommonHeader commonHeader = input.getCommonHeader();
        ActionIdentifiers actionIdentifiers = input.getActionIdentifiers();
        Payload payload = input.getPayload();

        validate(commonHeader, input.getAction(), actionIdentifiers,payload);
        if (status == null) {
            proceedAction(commonHeader, actionIdentifiers, payload);
        }

        AttachVolumeOutputBuilder outputBuilder = new AttachVolumeOutputBuilder();
        outputBuilder.setStatus(status);
        outputBuilder.setCommonHeader(input.getCommonHeader());
        return outputBuilder;
    }

    /**
     * Detach a cinder to the VM volume
     *
     * @param input of DetachVolumeInput from the REST API input
     * @return DetachVolumeOutputBuilder which has the details of the request results
     */
    public DetachVolumeOutputBuilder detachVolume(DetachVolumeInput input) {
        CommonHeader commonHeader = input.getCommonHeader();
        ActionIdentifiers actionIdentifiers = input.getActionIdentifiers();
        Payload payload = input.getPayload();

        validate(commonHeader, input.getAction(), actionIdentifiers,payload);
        if (status == null) {
            proceedAction(commonHeader, actionIdentifiers, payload);
        }

        DetachVolumeOutputBuilder outputBuilder = new DetachVolumeOutputBuilder();
        outputBuilder.setStatus(status);
        outputBuilder.setCommonHeader(input.getCommonHeader());
        return outputBuilder;
    }
    
    void validate(CommonHeader commonHeader,
                  Action action,
                  ActionIdentifiers actionIdentifiers,
                  Payload payload) {
        status = validateVserverIdVnfId(commonHeader, action, actionIdentifiers);
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
    	validatedStatus = validateMustHaveParamValue(actionIdentifiers.getVserverId(), "vnf-id");
    	logger.debug("check for vnf-id");
    	}
    	if (validatedStatus == null) {
    	validatedStatus = validateExcludedActIds(actionIdentifiers, EnumSet.of(ACTID_KEYS.VSERVER_ID, ACTID_KEYS.VNF_ID));
    	}
    	logger.debug("check for ActIds");
    	return validatedStatus;
    	}
    /**
     * Proceed to action for the attach or detach volume.
     * @param commonHeader of the input
     * @param actionIdentifiers of the input
     * @param payload of the input
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
