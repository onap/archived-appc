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

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.onap.appc.domainmodel.lcm.RequestContext;
import org.onap.appc.domainmodel.lcm.RuntimeContext;
import org.onap.appc.domainmodel.lcm.TransactionRecord;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.requesthandler.constant.Constants;
import org.onap.appc.configuration.Configuration;
import org.onap.appc.configuration.ConfigurationFactory;
import org.onap.appc.domainmodel.lcm.CommonHeader;
import org.onap.appc.exceptions.InvalidInputException;
import org.onap.appc.i18n.Msg;
import org.onap.appc.logging.LoggingConstants;
import org.onap.appc.logging.LoggingUtils;
import org.onap.appc.requesthandler.LCMStateManager;
import org.onap.appc.requesthandler.exceptions.*;
import org.onap.appc.requesthandler.helper.RequestValidator;
import org.onap.appc.transactionrecorder.TransactionRecorder;

import java.util.Calendar;
import java.util.Date;

public abstract class AbstractRequestValidatorImpl implements RequestValidator {

    protected final EELFLogger logger = EELFManager.getInstance().getLogger(RequestValidatorImpl.class);
    protected final Configuration configuration = ConfigurationFactory.getConfiguration();

    LCMStateManager lcmStateManager;
    TransactionRecorder transactionRecorder;

    protected static Calendar DateToCalendar(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    public void setTransactionRecorder(TransactionRecorder transactionRecorder) {
        this.transactionRecorder = transactionRecorder;
    }

    public void setLcmStateManager(LCMStateManager lcmStateManager) {
        this.lcmStateManager = lcmStateManager;
    }

    protected void validateInput(RuntimeContext runtimeContext)
        throws RequestExpiredException, InvalidInputException, DuplicateRequestException {
        RequestContext requestContext = runtimeContext.getRequestContext();
        if (logger.isTraceEnabled()){
            logger.trace("Entering to validateInput with RequestHandlerInput = "+ ObjectUtils.toString(requestContext));
        }
        if (StringUtils.isEmpty(requestContext.getActionIdentifiers().getVnfId()) || requestContext.getAction() == null
                || StringUtils.isEmpty(requestContext.getAction().name()) || StringUtils.isEmpty(requestContext.getCommonHeader().getApiVer())){
            if (logger.isDebugEnabled()) {
                logger.debug("vnfID = " + requestContext.getActionIdentifiers().getVnfId() + ", action = " + requestContext.getAction().name());
            }

            LoggingUtils.logErrorMessage(
                    LoggingConstants.TargetNames.REQUEST_VALIDATOR,
                    EELFResourceManager.format(Msg.APPC_INVALID_INPUT),
                    this.getClass().getCanonicalName());

            throw new InvalidInputException("vnfID or command is null");
        }
        CommonHeader commonHeader = requestContext.getCommonHeader();
        try {
            if(transactionRecorder.isTransactionDuplicate(runtimeContext.getTransactionRecord()))
                throw new DuplicateRequestException("Duplicate Request with");
        } catch (APPCException e) {
            logger.error("Error accessing database for transaction data",e);
        }

        Calendar inputTimeStamp = DateToCalendar(commonHeader.getTimeStamp());
        Calendar currentTime = Calendar.getInstance();

        // If input timestamp is of future, we reject the request
        if (inputTimeStamp.getTime().getTime() > currentTime.getTime().getTime()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Input Timestamp is of future = " + inputTimeStamp.getTime());
            }
            throw new InvalidInputException("Input Timestamp is of future = " + inputTimeStamp.getTime());
        }

        // Set ttl value from commonHeader. If not available set it to default
        Integer ttl = (commonHeader.getFlags()== null || commonHeader.getFlags().getTtl() <= 0 ) ?
                Integer.parseInt(configuration.getProperty(Constants.DEFAULT_TTL_KEY, String.valueOf(Constants.DEFAULT_TTL))):
                commonHeader.getFlags().getTtl();

        logger.debug("TTL value set to (seconds) : " + ttl);

        inputTimeStamp.add(Calendar.SECOND, ttl);

        if (currentTime.getTime().getTime() >= inputTimeStamp.getTime().getTime()) {

            LoggingUtils.logErrorMessage(
                    LoggingConstants.TargetNames.REQUEST_VALIDATOR,
                    "TTL Expired: Current time - " + currentTime.getTime().getTime() + " Request time: " + inputTimeStamp.getTime().getTime() + " with TTL value: " + ttl,
                    this.getClass().getCanonicalName());

            throw new RequestExpiredException("TTL Expired");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Validation of the request is successful");
        }
    }
}
