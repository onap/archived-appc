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

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.Action;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.RebootInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.RebootOutputBuilder;
import org.onap.appc.executor.objects.LCMCommandStatus;
import org.onap.appc.requesthandler.objects.RequestHandlerInput;
import org.onap.appc.util.JsonUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Provide LCM command service for rebooting virtual machine (VM)
 */
public class RebootService extends AbstractBaseService {
    private final EELFLogger myLogger = EELFManager.getInstance().getLogger(RebootService.class);
    private static final String REBOOT_TYPE_PARAMETER = "reboot-type";
    private final List<String> rebootTypeList = Arrays.asList("hard", "soft");

    /**
     * Constructor
     */
    public RebootService() {
        super(Action.Reboot);
    }

    public RebootOutputBuilder process(RebootInput input) {
        validate(input);
        if (status == null) {
            proceedAction(input);
        }

        final RebootOutputBuilder outputBuilder = new RebootOutputBuilder();
        outputBuilder.setCommonHeader(input.getCommonHeader());
        outputBuilder.setStatus(status);
        return outputBuilder;
    }

    private void proceedAction(RebootInput input) {
        RequestHandlerInput requestHandlerInput = getRequestHandlerInput(
            input.getCommonHeader(), input.getActionIdentifiers(), input.getPayload(), this.getClass().getName());
        if (requestHandlerInput != null) {
            executeAction(requestHandlerInput);
        }
    }

    private String getRebootType(RebootInput input) {
        String rebootType = null;
        if (input.getPayload() != null) {
            Map<String, String> payloadMap;
            try {
                payloadMap = JsonUtil.convertJsonStringToFlatMap(input.getPayload().getValue());
                rebootType = payloadMap.get(REBOOT_TYPE_PARAMETER);
            } catch (IOException e) {
                myLogger.error("Error in converting payload of RebootInput", e.getMessage());
            }
        }

        return rebootType;
    }

    /**
     * Validate the input.
     *
     * @param input of RebootInput from the REST API input
     */
    private void validate(RebootInput input) {
        status = validateVserverId(input.getCommonHeader(), input.getAction(), input.getActionIdentifiers());
        if (status != null) {
            return;
        }

        //reboot-type validation
        final String rebootType = getRebootType(input);
        if (null == rebootType) {
            status = buildStatusForParamName(LCMCommandStatus.MISSING_MANDATORY_PARAMETER, REBOOT_TYPE_PARAMETER);
        } else if (!rebootTypeList.contains(rebootType)) {
            status = buildStatusForErrorMsg(LCMCommandStatus.INVALID_INPUT_PARAMETER, REBOOT_TYPE_PARAMETER);
        }
    }
}
