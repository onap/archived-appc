/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications (C) 2018 Ericsson
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

package org.onap.appc.oam.processor;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.i18n.EELFResourceManager;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.common.header.CommonHeader;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.status.Status;
import org.opendaylight.yang.gen.v1.org.onap.appc.oam.rev170303.status.StatusBuilder;
import org.onap.appc.exceptions.InvalidInputException;
import org.onap.appc.exceptions.InvalidStateException;
import org.onap.appc.executor.objects.Params;
import org.onap.appc.i18n.Msg;
import org.onap.appc.logging.LoggingConstants;
import org.onap.appc.logging.LoggingUtils;
import org.onap.appc.oam.AppcOam;
import org.onap.appc.oam.OAMCommandStatus;
import org.onap.appc.oam.util.ConfigurationHelper;
import org.onap.appc.oam.util.OperationHelper;
import org.onap.appc.oam.util.StateHelper;
import org.slf4j.MDC;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static com.att.eelf.configuration.Configuration.MDC_INSTANCE_UUID;
import static com.att.eelf.configuration.Configuration.MDC_KEY_REQUEST_ID;
import static com.att.eelf.configuration.Configuration.MDC_SERVER_FQDN;
import static com.att.eelf.configuration.Configuration.MDC_SERVER_IP_ADDRESS;
import static com.att.eelf.configuration.Configuration.MDC_SERVICE_NAME;

/**
 * Common handling methods of <br>
 *     - BaseProcessor (for REST sync handling) <br>
 *     - BaseActionRunnable (for REST async handling)
 */
public abstract class BaseCommon {
    final EELFLogger logger;
    final ConfigurationHelper configurationHelper;
    final StateHelper stateHelper;
    final OperationHelper operationHelper;

    Status status;
    Date startTime;

    AppcOam.RPC rpc;
    CommonHeader commonHeader;

    private final List<String> MDC_KEYS = Arrays.asList(
        LoggingConstants.MDCKeys.PARTNER_NAME,
        LoggingConstants.MDCKeys.SERVER_NAME,
        MDC_INSTANCE_UUID,
        MDC_KEY_REQUEST_ID,
        MDC_SERVER_FQDN,
        MDC_SERVER_IP_ADDRESS,
        MDC_SERVICE_NAME
    );
    private Map<String, String> oldMdcContent = new HashMap<>();

    /**
     * Constructor
     *
     * @param eelfLogger            for logging
     * @param configurationHelperIn for property reading
     * @param stateHelperIn         for APP-C OAM state checking
     * @param operationHelperIn for operational helper
     */
    BaseCommon(EELFLogger eelfLogger,
               ConfigurationHelper configurationHelperIn,
               StateHelper stateHelperIn,
               OperationHelper operationHelperIn) {
        logger = eelfLogger;
        configurationHelper = configurationHelperIn;
        stateHelper = stateHelperIn;
        operationHelper = operationHelperIn;
    }

    /**
     * Audit log the passed in message at INFO level.
     * @param msg the Msg to be audit logged.
     */
    void auditInfoLog(Msg msg) {
        LoggingUtils.auditInfo(startTime.toInstant(),
            Instant.now(),
            String.valueOf(status.getCode()),
            status.getMessage(),
            getClass().getCanonicalName(),
            msg,
            configurationHelper.getAppcName(),
            stateHelper.getCurrentOamState().toString()
        );
    }

    /**
     * Set MDC properties.
     */
    public final void setInitialLogProperties() {
        MDC.put(MDC_KEY_REQUEST_ID, commonHeader.getRequestId());
        MDC.put(LoggingConstants.MDCKeys.PARTNER_NAME, commonHeader.getOriginatorId());
        MDC.put(MDC_INSTANCE_UUID, ""); // value should be created in the future
        MDC.put(MDC_SERVICE_NAME, rpc.name());
        try {
            //!!!Don't change the following to a .getHostName() again please. It's wrong!MDC.put(MDC_SERVER_FQDN,
            // InetAddress.getLocalHost().getCanonicalHostName());
            MDC.put(MDC_SERVER_FQDN, getHostInfo("canonicalHostName"));
            MDC.put(MDC_SERVER_IP_ADDRESS, getHostInfo("hostName"));
            MDC.put(LoggingConstants.MDCKeys.SERVER_NAME, getHostInfo("hostName"));
        } catch (Exception e) {
            logger.error("MDC constant error", e);
        }
    }

    /**
     * Clear MDC properties.
     */
    public final void clearRequestLogProperties() {
        for (String key : MDC_KEYS) {
            try {
                MDC.remove(key);
            } catch (Exception e) {
                logger.error(
                    String.format("Unable to clear the Log properties (%s) due to exception: %s", key, e.getMessage()));
            }
        }
    }

    /**
     * Reset MDC log properties based on passed in condition. does:<br>
     *   - persist existing MDC setting and set my MDC log properties <br>
     *   - or re-apply persisted MDC log properties
     * @param useMdcMap boolean to indicate whether to persist the existing MDC setting and set my MDC log properties,
     *                  or to re-apply the persisted MDC log properties.
     */
    void resetLogProperties(boolean useMdcMap) {
        if (useMdcMap) {
            for (Map.Entry<String, String> aEntry : oldMdcContent.entrySet()) {
                MDC.put(aEntry.getKey(), aEntry.getValue());
            }
            return;
        }

        // persist existing log properties and set my log properties
        oldMdcContent.clear();
        for (String key : MDC_KEYS) {
            String value = MDC.get(key);
            if (value != null) {
                oldMdcContent.put(key, value);
            }
        }
        setInitialLogProperties();
    }

    /**
     * Set class <b>status</b> by calling setStatus(OAMCommandStatus, Params) with null paramter.
     * @see #setStatus(OAMCommandStatus, String)
     *
     * @param oamCommandStatus of the to be set new state
     */
    void setStatus(OAMCommandStatus oamCommandStatus) {
        setStatus(oamCommandStatus, null);
    }

    /**
     * Create Status based on the passed in parameter, then set the class <b>status</b> with it.
     *
     * @param oamCommandStatus of the current OAM command status
     * @param message to be set in the new status
     */
    void setStatus(OAMCommandStatus oamCommandStatus, String message) {
        Params params = new Params().addParam("errorMsg", message);

        StatusBuilder statusBuilder = new StatusBuilder();
        statusBuilder.setCode(oamCommandStatus.getResponseCode());
        if (params != null) {
            statusBuilder.setMessage(oamCommandStatus.getFormattedMessage(params));
        } else {
            statusBuilder.setMessage(oamCommandStatus.getResponseMessage());
        }

        status = statusBuilder.build();
    }

    /**
     * Set class <b>status</b> with error status calculated from the passed in paremeter
     * and audit log the error message.
     * @param t of the error Throwable.
     */
    void setErrorStatus(Throwable t) {
        resetLogProperties(false);

        final String appName = configurationHelper.getAppcName();
        String exceptionMessage = t.getMessage() != null ? t.getMessage() : t.toString();

        OAMCommandStatus oamCommandStatus;
        String errorMessage;
        if (t instanceof InvalidInputException) {
            oamCommandStatus = OAMCommandStatus.INVALID_PARAMETER;
            errorMessage = EELFResourceManager.format(Msg.OAM_OPERATION_INVALID_INPUT, t.getMessage());
        } else if (t instanceof InvalidStateException) {
            exceptionMessage = String.format(AppcOam.INVALID_STATE_MESSAGE_FORMAT,
                rpc.getAppcOperation(), appName, stateHelper.getCurrentOamState());
            oamCommandStatus = OAMCommandStatus.REJECTED;
            errorMessage = EELFResourceManager.format(Msg.INVALID_STATE_TRANSITION, exceptionMessage);
        } else if (t instanceof TimeoutException) {
            oamCommandStatus = OAMCommandStatus.TIMEOUT;
            errorMessage = EELFResourceManager.format(Msg.OAM_OPERATION_EXCEPTION, t,
                    appName, t.getClass().getSimpleName(), rpc.name(), exceptionMessage);
        } else {
            oamCommandStatus = OAMCommandStatus.UNEXPECTED_ERROR;
            errorMessage = EELFResourceManager.format(Msg.OAM_OPERATION_EXCEPTION, t,
                appName, t.getClass().getSimpleName(), rpc.name(), exceptionMessage);
        }

        setStatus(oamCommandStatus, exceptionMessage);

        LoggingUtils.logErrorMessage(
            String.valueOf(status.getCode()),
            status.getMessage(),
            LoggingConstants.TargetNames.APPC,
            LoggingConstants.TargetNames.APPC_OAM_PROVIDER,
            errorMessage,
            AppcOam.class.getCanonicalName());

        resetLogProperties(true);
    }

    /**
     * Genral debug log when debug logging level is enabled.
     * @param message of the log message format
     * @param args of the objects listed in the message format
     */
    void logDebug(String message, Object... args) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format(message, args));
        }
    }

    protected String getHostInfo(String type) throws UnknownHostException {
        InetAddress inetAddress = InetAddress.getLocalHost();
        String returnValue = "";
        switch(type) {
            case "canonicalHostName": returnValue = inetAddress.getCanonicalHostName();
                break;
            case "hostName": returnValue = inetAddress.getHostName();
                break;
            case "hostAddress": returnValue = inetAddress.getHostAddress();
                break;
            default: returnValue = "Invalid operation";
                break;
        }
        return returnValue;
    }
}
