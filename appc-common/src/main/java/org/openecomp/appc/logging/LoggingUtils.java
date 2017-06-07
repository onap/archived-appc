/*-
 * ============LICENSE_START=======================================================
 * APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Amdocs
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
 * ============LICENSE_END=========================================================
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */

package org.openecomp.appc.logging;

import org.openecomp.appc.i18n.Msg;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResolvableErrorEnum;
import com.att.eelf.i18n.EELFResourceManager;
import org.slf4j.MDC;

import static com.att.eelf.configuration.Configuration.MDC_KEY_REQUEST_ID;
import static com.att.eelf.configuration.Configuration.MDC_SERVICE_NAME;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.TimeZone;



public class LoggingUtils {

    private final static EELFLogger errorLogger = EELFManager.getInstance().getErrorLogger();
    private final static EELFLogger auditLogger = EELFManager.getInstance().getAuditLogger();
    private final static EELFLogger metricLogger = EELFManager.getInstance().getMetricsLogger();

    public static void logErrorMessage(String errorCode, String errorDescription, String targetEntity, String targetServiceName, String additionalMessage, String className) {
        logError(errorCode, errorDescription, targetEntity, targetServiceName, additionalMessage, className);
    }

    public static void logErrorMessage(String targetEntity, String targetServiceName, String additionalMessage, String className) {
        logError("", "", targetEntity, targetServiceName, additionalMessage, className);
    }

    public static void logErrorMessage(String targetServiceName, String additionalMessage, String className) {
        logError("", "", LoggingConstants.TargetNames.APPC, targetServiceName, additionalMessage, className);
    }

    private static void logError(String errorCode, String errorDescription, String targetEntity, String targetServiceName, String additionalMessage, String className) {
        populateErrorLogContext(errorCode, errorDescription, targetEntity, targetServiceName, className);
        errorLogger.error(additionalMessage == null ? "" : additionalMessage);
        cleanErrorLogContext();
    }

    public static void logAuditMessage(Instant beginTimeStamp, Instant endTimeStamp, String code, String responseDescription, String className) {
        populateAuditLogContext(beginTimeStamp, endTimeStamp, code, responseDescription, className);
        auditLogger.info(EELFResourceManager.format(Msg.APPC_AUDIT_MSG,
                MDC.get(MDC_SERVICE_NAME),
                MDC.get(LoggingConstants.MDCKeys.TARGET_VIRTUAL_ENTITY),
                MDC.get(LoggingConstants.MDCKeys.PARTNER_NAME),
                MDC.get(MDC_KEY_REQUEST_ID),
                MDC.get(LoggingConstants.MDCKeys.BEGIN_TIMESTAMP),
                MDC.get(LoggingConstants.MDCKeys.END_TIMESTAMP),
                MDC.get(LoggingConstants.MDCKeys.RESPONSE_CODE)));
        cleanAuditErrorContext();
    }

    public static void auditInfo(Instant beginTimeStamp, Instant endTimeStamp, String code, String responseDescription, String className,EELFResolvableErrorEnum resourceId, String... arguments) {
        populateAuditLogContext(beginTimeStamp, endTimeStamp, code, responseDescription, className);
        auditLogger.info(resourceId,arguments);
        cleanAuditErrorContext();
    }

    public static void auditWarn(Instant beginTimeStamp, Instant endTimeStamp, String code, String responseDescription, String className,EELFResolvableErrorEnum resourceId, String... arguments) {
        populateAuditLogContext(beginTimeStamp, endTimeStamp, code, responseDescription, className);
        auditLogger.warn(resourceId,arguments);
        cleanAuditErrorContext();
    }



    public static void logMetricsMessage(Instant beginTimeStamp, Instant endTimeStamp, String targetEntity, String targetServiceName, String statusCode, String responseCode, String responseDescription, String className) {
        populateMetricLogContext(beginTimeStamp, endTimeStamp, targetEntity, targetServiceName, statusCode, responseCode, responseDescription, className);
        metricLogger.info(EELFResourceManager.format(Msg.APPC_METRIC_MSG,
                MDC.get(MDC_SERVICE_NAME),
                MDC.get(LoggingConstants.MDCKeys.TARGET_VIRTUAL_ENTITY),
                MDC.get(LoggingConstants.MDCKeys.PARTNER_NAME),
                MDC.get(MDC_KEY_REQUEST_ID),
                MDC.get(LoggingConstants.MDCKeys.TARGET_ENTITY),
                MDC.get(LoggingConstants.MDCKeys.TARGET_SERVICE_NAME),
                MDC.get(LoggingConstants.MDCKeys.ELAPSED_TIME),
                MDC.get(LoggingConstants.MDCKeys.STATUS_CODE)));
        cleanMetricContext();
    }

    private static void populateAuditLogContext(Instant beginTimeStamp, Instant endTimeStamp, String code, String responseDescription, String className) {
        populateTimeContext(beginTimeStamp, endTimeStamp);
        MDC.put(LoggingConstants.MDCKeys.RESPONSE_CODE, code);
        MDC.put(LoggingConstants.MDCKeys.STATUS_CODE, code.equals("100") || code.equals("400") ?
                LoggingConstants.StatusCodes.COMPLETE :
                LoggingConstants.StatusCodes.ERROR);
        MDC.put(LoggingConstants.MDCKeys.RESPONSE_DESCRIPTION, responseDescription!=null?responseDescription:"");
        MDC.put(LoggingConstants.MDCKeys.CLASS_NAME, className!=null?className:"");
    }

    private static void cleanAuditErrorContext() {
        cleanTimeContext();
        MDC.remove(LoggingConstants.MDCKeys.STATUS_CODE);
        MDC.remove(LoggingConstants.MDCKeys.RESPONSE_CODE);
        MDC.remove(LoggingConstants.MDCKeys.RESPONSE_DESCRIPTION);
        MDC.remove(LoggingConstants.MDCKeys.CLASS_NAME);
    }

    private static void populateErrorLogContext(String errorCode, String errorDescription, String targetEntity, String targetServiceName, String className) {
        populateErrorContext(errorCode, errorDescription);
        populateTargetContext(targetEntity, targetServiceName!=null?targetServiceName:"");
        MDC.put(LoggingConstants.MDCKeys.CLASS_NAME, className!=null?className:"");
    }

    private static void cleanErrorLogContext() {
        cleanErrorContext();
        cleanTargetContext();
        MDC.remove(LoggingConstants.MDCKeys.CLASS_NAME);
    }

    private static void populateMetricLogContext(Instant beginTimeStamp, Instant endTimeStamp, String targetEntity, String targetServiceName, String statusCode, String responseCode, String responseDescription, String className) {
        populateTimeContext(beginTimeStamp, endTimeStamp);
        populateTargetContext(targetEntity, targetServiceName);
        populateResponseContext(statusCode, responseCode, responseDescription);
        MDC.put(LoggingConstants.MDCKeys.CLASS_NAME, className!=null?className:"");
    }

    private static void cleanMetricContext() {
        cleanTimeContext();
        cleanTargetContext();
        cleanResponseContext();
        MDC.remove(LoggingConstants.MDCKeys.CLASS_NAME);
    }

    private static void populateTargetContext(String targetEntity, String targetServiceName) {
        MDC.put(LoggingConstants.MDCKeys.TARGET_ENTITY, targetEntity!=null?targetEntity:"");
        MDC.put(LoggingConstants.MDCKeys.TARGET_SERVICE_NAME, targetServiceName!=null?targetServiceName:"");
    }

    private static void cleanTargetContext() {
        MDC.remove(LoggingConstants.MDCKeys.TARGET_ENTITY);
        MDC.remove(LoggingConstants.MDCKeys.TARGET_SERVICE_NAME);
    }

    private static void populateTimeContext(Instant beginTimeStamp, Instant endTimeStamp) {
        String beginTime = "";
        String endTime = "";
        String elapsedTime = "";

        if (beginTimeStamp != null && endTimeStamp != null) {
            elapsedTime = String.valueOf(ChronoUnit.MILLIS.between(beginTimeStamp,  endTimeStamp));
            beginTime = generateTimestampStr(beginTimeStamp);
            endTime = generateTimestampStr(endTimeStamp);
        }

        MDC.put(LoggingConstants.MDCKeys.BEGIN_TIMESTAMP, beginTime);
        MDC.put(LoggingConstants.MDCKeys.END_TIMESTAMP, endTime);
        MDC.put(LoggingConstants.MDCKeys.ELAPSED_TIME, elapsedTime);
    }

    private static String generateTimestampStr(Instant timeStamp) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
        TimeZone tz = TimeZone.getTimeZone("UTC");
        df.setTimeZone(tz);
        return df.format(Date.from(timeStamp));
    }

    private static void cleanTimeContext() {
        MDC.remove(LoggingConstants.MDCKeys.BEGIN_TIMESTAMP);
        MDC.remove(LoggingConstants.MDCKeys.END_TIMESTAMP);
        MDC.remove(LoggingConstants.MDCKeys.ELAPSED_TIME);
    }

    private static void populateResponseContext(String statusCode, String responseCode, String responseDescription) {
        MDC.put(LoggingConstants.MDCKeys.STATUS_CODE, statusCode!=null?statusCode:"");
        MDC.put(LoggingConstants.MDCKeys.RESPONSE_CODE, responseCode);
        MDC.put(LoggingConstants.MDCKeys.RESPONSE_DESCRIPTION, responseDescription!=null?responseDescription:"");
    }

    private static void cleanResponseContext() {
        MDC.remove(LoggingConstants.MDCKeys.STATUS_CODE);
        MDC.remove(LoggingConstants.MDCKeys.RESPONSE_CODE);
        MDC.remove(LoggingConstants.MDCKeys.RESPONSE_DESCRIPTION);
    }

    private static void populateErrorContext(String errorCode, String errorDescription) {
        MDC.put(LoggingConstants.MDCKeys.ERROR_CODE, errorCode);
        MDC.put(LoggingConstants.MDCKeys.ERROR_DESCRIPTION, errorDescription);
    }

    private static void cleanErrorContext() {
        MDC.remove(LoggingConstants.MDCKeys.ERROR_CODE);
        MDC.remove(LoggingConstants.MDCKeys.ERROR_DESCRIPTION);
    }

}
