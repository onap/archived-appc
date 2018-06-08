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
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.status.Status;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.status.StatusBuilder;
import org.onap.appc.executor.objects.LCMCommandStatus;
import org.onap.appc.executor.objects.Params;
import org.onap.appc.requesthandler.objects.RequestHandlerOutput;

import java.text.ParseException;

public class AbstractBaseUtils {
    protected final String COMMON_ERROR_MESSAGE_TEMPLATE =  "Error processing %s input : %s";
    protected final String DELIMITER_COMMA = ",";

	protected final EELFLogger logger = EELFManager.getInstance().getLogger(AbstractBaseService.class);

    /**
     * Build a Status without parameter
     *
     * @param lcmCommandStatus for the Status code and message format
     * @return the newly built Status
     */
    protected Status buildStatusWithoutParams(LCMCommandStatus lcmCommandStatus) {
        return buildStatus(lcmCommandStatus, null, null);
    }

    /**
     * Build a Status with "errorMsg" param key.
     *
     * @param lcmCommandStatus for the Status code and message format
     * @param message          String for the Status message variable
     * @return the newly built Status
     */
    protected Status buildStatusForErrorMsg(LCMCommandStatus lcmCommandStatus, String message) {
        return buildStatus(lcmCommandStatus, message, "errorMsg");
    }

    /**
     * Build a Status with "vnfId" param key.
     *
     * @param lcmCommandStatus for the Status code and message format
     * @param message          String for the Status message variable
     * @return the newly build Status
     */
    protected Status buildStatusForVnfId(LCMCommandStatus lcmCommandStatus, String message) {
        return buildStatus(lcmCommandStatus, message, "vnfId");
    }

    /**
     * Build a Status with "paramName" param key.
     *
     * @param lcmCommandStatus for the Status code and message format
     * @param message          String for the Status message variable
     * @return the newly built Status
     */
    protected Status buildStatusForParamName(LCMCommandStatus lcmCommandStatus, String message) {
        return buildStatus(lcmCommandStatus, message, "paramName");
    }

    /**
     * Build a Status with "id" param key.
     *
     * @param lcmCommandStatus for the Status code and message format
     * @param message          String for the Status message variable
     * @return the newly build Status
     */
    protected Status buildStatusForId(LCMCommandStatus lcmCommandStatus, String message) {
        return buildStatus(lcmCommandStatus, message, "id");
    }

    /**
     * Build a Status.
     *
     * @param lcmCommandStatus for the Status code and message format
     * @param message          String for the Status message variable
     * @param key              String for the LCMcommandStatus format
     * @return the newly built Status
     */
    Status buildStatus(LCMCommandStatus lcmCommandStatus, String message, String key) {
        Params params = new Params().addParam(key, message);
        String statusMsg = lcmCommandStatus.getFormattedMessage(params);

        return buildStatusWithCode(lcmCommandStatus.getResponseCode(), statusMsg);
    }

    /**
     * Build a Status with passed in code
     * @param statusCode Integer of the status code
     * @param statusMsg String of the status description
     * @return the newly build Status
     */
    private Status buildStatusWithCode(Integer statusCode, String statusMsg) {
        StatusBuilder status = new StatusBuilder();
        status.setCode(statusCode);
        status.setMessage(statusMsg);
        return status.build();
    }

    /**
     * Build a Status using ParseException
     * @param e of the ParseException
     * @return the newly built Status
     */
    protected Status buildStatusWithParseException(ParseException e) {
        String errorMessage = e.getMessage() != null ? e.getMessage() : e.toString();
        return buildStatusForErrorMsg(LCMCommandStatus.REQUEST_PARSING_FAILED, errorMessage);
    }

    /**
     * Build a Status using RequestHandlerOutput
     * @param requestHandlerOutput object which contains the status code and message for building the new status
     * @return the newly built Status
     */
    protected Status buildStatusWithDispatcherOutput(RequestHandlerOutput requestHandlerOutput){
        Integer statusCode = requestHandlerOutput.getResponseContext().getStatus().getCode();
        String statusMessage = requestHandlerOutput.getResponseContext().getStatus().getMessage();
        return  buildStatusWithCode(statusCode, statusMessage);
    }

    /**
     * Get RPC name from Action. When there 2 words in the Action, RPC name will be dash separated string.
     * @param action of Action object
     * @return RPC name of the Action
     */
    protected String getRpcName(Action action) {
        String regex = "([a-z])([A-Z]+)";
        String replacement = "$1-$2";
        return action.name().replaceAll(regex, replacement).toLowerCase();
    }
}
