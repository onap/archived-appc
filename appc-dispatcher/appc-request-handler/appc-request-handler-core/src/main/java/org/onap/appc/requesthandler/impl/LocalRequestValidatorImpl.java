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

package org.onap.appc.requesthandler.impl;

import com.att.eelf.i18n.EELFResourceManager;
import org.onap.appc.domainmodel.lcm.ActionIdentifiers;
import org.onap.appc.domainmodel.lcm.RequestContext;
import org.onap.appc.domainmodel.lcm.RuntimeContext;
import org.onap.appc.exceptions.InvalidInputException;
import org.onap.appc.i18n.Msg;
import org.onap.appc.logging.LoggingConstants;
import org.onap.appc.logging.LoggingUtils;
import org.onap.appc.requesthandler.exceptions.DuplicateRequestException;
import org.onap.appc.requesthandler.exceptions.LCMOperationsDisabledException;
import org.onap.appc.requesthandler.exceptions.RequestExpiredException;
import org.onap.appc.util.JsonUtil;

import java.io.IOException;
import java.util.Map;

/**
 * * This class validates the LCM-Requests that don't need communication with the VNF.
 */
public class LocalRequestValidatorImpl extends AbstractRequestValidatorImpl {
    @Override
    public void validateRequest(RuntimeContext runtimeContext) throws LCMOperationsDisabledException,
        DuplicateRequestException, RequestExpiredException, InvalidInputException {

        if (!lcmStateManager.isLCMOperationEnabled()) {
            LoggingUtils.logErrorMessage(
                LoggingConstants.TargetNames.REQUEST_VALIDATOR,
                EELFResourceManager.format(Msg.LCM_OPERATIONS_DISABLED),
                this.getClass().getCanonicalName());
            throw new LCMOperationsDisabledException("APPC LCM operations have been administratively disabled");
        }

        validateInput(runtimeContext);
    }

    @Override
    public void validateInput(RuntimeContext runtimeContext) throws DuplicateRequestException,
        RequestExpiredException, InvalidInputException {
        RequestContext requestContext = runtimeContext.getRequestContext();
        super.validateInput(runtimeContext);
        switch (requestContext.getAction()) {
            case ActionStatus:
                validateActionStatusPayload(requestContext.getPayload());
                validateActionStatusActionIdentifiers(requestContext.getActionIdentifiers());
                break;
            default:
                logger.warn(String.format("Action %s not supported!", requestContext.getAction()));
        }
    }

    private void validateActionStatusPayload(String payload) throws InvalidInputException {
        Map<String, String> map;
        try {
            map = JsonUtil.convertJsonStringToFlatMap(payload);
        } catch (IOException e) {
            logger.error(String.format("Error encountered when converting JSON payload '%s' to map", payload), e);
            throw new InvalidInputException("Search criteria cannot be determined from Payload due to format issue");
        }
        if ((map == null) || !map.containsKey("request-id")) {
            throw new InvalidInputException("request-id is absent in the Payload");
        }
    }

    private void validateActionStatusActionIdentifiers(ActionIdentifiers identifiers) throws InvalidInputException {
        if (identifiers == null || identifiers.getVnfId() == null) {
            throw new InvalidInputException("VNF-Id is absent in Action Identifiers");
        }
    }
}
