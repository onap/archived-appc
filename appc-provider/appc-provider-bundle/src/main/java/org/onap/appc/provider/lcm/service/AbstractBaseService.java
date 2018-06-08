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
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.Payload;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.action.identifiers.ActionIdentifiers;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.common.header.CommonHeader;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.status.Status;
import org.onap.appc.executor.objects.LCMCommandStatus;
import org.onap.appc.logging.LoggingConstants;
import org.onap.appc.logging.LoggingUtils;
import org.onap.appc.provider.lcm.util.RequestInputBuilder;
import org.onap.appc.provider.lcm.util.ValidationService;
import org.onap.appc.requesthandler.objects.RequestHandlerInput;
import org.onap.appc.requesthandler.objects.RequestHandlerOutput;

import java.text.ParseException;
import java.util.EnumSet;

public abstract class AbstractBaseService extends AbstractBaseUtils {
    /**
     * The list of ActionIdentifier keys.<br>
     * The extra space in the front of the keyName is for better REST API response output.
     */
    enum ACTID_KEYS {
        SERVICE_INSTANCE_ID(" service-instance-id"),
        VF_MODULE_ID(" vf-module-id"),
        VNF_ID(" vnf-id"),
        VNFC_NAME(" vnfc-name"),
        VSERVER_ID(" vserver-id");

        String keyName;

        ACTID_KEYS(String keyName) {
            this.keyName = keyName;
        }

        String getKeyName() {
            return keyName;
        }
    }

      String rpcName;
      Action expectedAction;

    Status status;

    protected AbstractBaseService(){};
    /**
     * Constructor
     *
     * @param theAction of the expected Action for this service
     */
    protected AbstractBaseService(Action theAction) {
        expectedAction = theAction;
        rpcName = getRpcName(theAction);
    }


    /**
     * Validate Input: <br>
     *    - using ValidationService to do common validation <br>
     *    - validate Action matches the expected Action <br>
     *    - validate existence of ActionIdentifier <br>
     *
     * @param commonHeader      of the input
     * @param action            of the input
     * @param actionIdentifiers of the input
     * @return null if validation passed, otherwise, return Status with validation failure details.
     */
    Status validateInput(CommonHeader commonHeader, Action action, ActionIdentifiers actionIdentifiers) {
        // common validation
        Status validatedStatus = ValidationService.getInstance().validateInput(commonHeader, action, rpcName);
        if (validatedStatus != null) {
            return validatedStatus;
        }

        // action validation
        if (action != expectedAction) {
            validatedStatus = buildStatusForErrorMsg(LCMCommandStatus.INVALID_INPUT_PARAMETER, "action");
            return validatedStatus;
        }

        // action identifier
        if (actionIdentifiers == null) {
            validatedStatus = buildStatusForParamName(
                LCMCommandStatus.MISSING_MANDATORY_PARAMETER, "action-identifiers");
        }

        return validatedStatus;
    }

    /**
     * Validate input as well as VNF ID in actionIdentifier
     *
     * @param commonHeader      of the input
     * @param action            of the input
     * @param actionIdentifiers of the input
     * @return null if validation passed, otherwise, return Status with validation failure details.
     */
    Status validateVnfId(CommonHeader commonHeader, Action action, ActionIdentifiers actionIdentifiers) {
        Status validatedStatus = validateInput(commonHeader, action, actionIdentifiers);
        if (validatedStatus != null) {
            return validatedStatus;
        }

        validatedStatus = validateMustHaveParamValue(actionIdentifiers.getVnfId(), "vnf-id");
        if (validatedStatus == null) {
            validatedStatus = validateExcludedActIds(actionIdentifiers, EnumSet.of(ACTID_KEYS.VNF_ID));
        }

        return validatedStatus;
    }

    /**
     * Validate input as well as VSERVER ID in actionIdentifier
     *
     * @param commonHeader      of the input
     * @param action            of the input
     * @param actionIdentifiers of the input
     * @return null if validation passed, otherwise, return Status with validation failure details.
     */
    Status validateVserverId(CommonHeader commonHeader, Action action, ActionIdentifiers actionIdentifiers) {
        Status validatedStatus = validateInput(commonHeader, action, actionIdentifiers);
        if (validatedStatus != null) {
            return validatedStatus;
        }

        validatedStatus = validateMustHaveParamValue(actionIdentifiers.getVserverId(), "vserver-id");
        if (validatedStatus == null) {
            validatedStatus = validateExcludedActIds(actionIdentifiers, EnumSet.of(ACTID_KEYS.VSERVER_ID));
        }

        return validatedStatus;
    }

    /**
     * Validate a value of the must have parameter
     * @param value   the value of the parameter
     * @param keyName the key name of the parameter
     * @return null if validation passed, otherwise, return Status with validation failure details.
     */
    Status validateMustHaveParamValue(String value, String keyName) {
        Status validatedStatus = null;
        if (StringUtils.isEmpty(value)) {
            if (value == null) {
                validatedStatus = buildStatusForParamName(LCMCommandStatus.MISSING_MANDATORY_PARAMETER, keyName);
            } else {
                validatedStatus = buildStatusForErrorMsg(LCMCommandStatus.INVALID_INPUT_PARAMETER, keyName);
            }
        }
        return validatedStatus;
    }

    /**
     * Validate the excluded Action Identifier to ensure they do not exist.
     * Set Status if any error occurs.
     *
     * @param actionIdentifiers of the to be validated object
     * @param exclusionKeys of a list of ACTID_KEYS should be ignored in this validation
     * @return null if validation passed, otherwise, return Status with validation failure details.
     */
    Status validateExcludedActIds(ActionIdentifiers actionIdentifiers, EnumSet<ACTID_KEYS> exclusionKeys) {
        StringBuilder names = new StringBuilder();
        boolean append = false;
        for (ACTID_KEYS key : ACTID_KEYS.values()) {
            if (exclusionKeys.contains(key)) {
                continue;
            }

            switch (key) {
                case SERVICE_INSTANCE_ID:
                    append = actionIdentifiers.getServiceInstanceId() != null;
                    break;
                case VF_MODULE_ID:
                    append = actionIdentifiers.getVfModuleId() != null;
                    break;
                case VSERVER_ID:
                    append = actionIdentifiers.getVserverId() != null;
                    break;
                case VNFC_NAME:
                    append = actionIdentifiers.getVnfcName() != null;
                    break;
                case VNF_ID:
                    append = actionIdentifiers.getVnfId() != null;
                    break;
                default:
                    append = false;
            }

            if (append) {
                names.append(key.getKeyName()).append(DELIMITER_COMMA);
            }
        }

        Status validatedStatus = null;
        int namesLength = names.length();
        if (namesLength != 0) {
            names.setLength(namesLength - 1);
            validatedStatus = buildStatusForErrorMsg(LCMCommandStatus.INVALID_INPUT_PARAMETER, names.toString());
        }

        return validatedStatus;
    }

    /**
     * Get RequestHandlerInput
     * @param commonHeader of the input
     * @param actionIdentifiers of the input
     * @param payload of the input
     * @param callerClassName String of this.getClass().getName() of the call class
     * @return the newly built RequestHandlerInput if no error occured, otherwise, return null.
     */
    RequestHandlerInput getRequestHandlerInput(CommonHeader commonHeader,
                                               ActionIdentifiers actionIdentifiers,
                                               Payload payload,
                                               String callerClassName) {

        try {
            RequestInputBuilder requestInputBuilder = new RequestInputBuilder().requestContext()
                .commonHeader(commonHeader)
                .actionIdentifiers(actionIdentifiers)
                .action(expectedAction.name())
                .rpcName(rpcName);
            if (payload != null) {
                requestInputBuilder = requestInputBuilder.payload(payload);
            }
            return requestInputBuilder.build();
        } catch (ParseException e) {
            status = buildStatusWithParseException(e);

            LoggingUtils.logErrorMessage(
                LoggingConstants.TargetNames.APPC_PROVIDER,
                String.format(COMMON_ERROR_MESSAGE_TEMPLATE, expectedAction, e.getMessage()),
                callerClassName);
        }
        return null;
    }

    /**
     * Execute the action through RequestExecutor
     * @param requestHandlerInput contains everything about the action
     */
    RequestHandlerOutput executeAction(RequestHandlerInput requestHandlerInput) {
        RequestHandlerOutput requestHandlerOutput = null;
        if (requestHandlerInput == null) {
            status = buildStatusForErrorMsg(LCMCommandStatus.UNEXPECTED_ERROR,
                "executeAction with null RequestHandlerInput");
        } else {
            RequestExecutor requestExecutor = new RequestExecutor();
            requestHandlerOutput = requestExecutor.executeRequest(requestHandlerInput);
            status = buildStatusWithDispatcherOutput(requestHandlerOutput);
        }
        return requestHandlerOutput;
    }
}
