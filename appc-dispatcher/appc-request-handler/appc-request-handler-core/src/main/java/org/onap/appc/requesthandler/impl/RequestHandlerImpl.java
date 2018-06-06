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
import org.onap.appc.domainmodel.lcm.*;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.executor.CommandExecutor;
import org.onap.appc.executor.objects.CommandExecutorInput;
import org.onap.appc.metricservice.metric.DispatchingFuntionMetric;
import org.onap.appc.requesthandler.constant.Constants;
import org.onap.appc.executor.objects.LCMCommandStatus;
import org.onap.appc.executor.objects.Params;
import org.onap.appc.i18n.Msg;
import org.onap.appc.lockmanager.api.LockException;
import org.onap.appc.lockmanager.api.LockManager;
import org.onap.appc.logging.LoggingConstants;

/**
 * This class provides application logic for the Request/Response Handler Component.
 */
public class RequestHandlerImpl extends AbstractRequestHandlerImpl {

    /**
     * APP-C VNF lock idle timeout in milliseconds. Applied only when locking VNF using northbound API "lock"
     */
    private static final String PROP_IDLE_TIMEOUT = "org.onap.appc.lock.idleTimeout";

    private final EELFLogger logger = EELFManager.getInstance().getLogger(RequestHandlerImpl.class);

    private LockManager lockManager;
    private CommandExecutor commandExecutor;
    private boolean isMetricEnabled = false;

    public RequestHandlerImpl() {
        super();
    }

    public void setLockManager(LockManager lockManager) {
        this.lockManager = lockManager;
    }

    public void setCommandExecutor(CommandExecutor commandExecutor) {
        this.commandExecutor = commandExecutor;
    }

    public void handleRequest(RuntimeContext runtimeContext) {

        switch (runtimeContext.getRequestContext().getAction()) {
            case Lock:
                try {
                    long timeout = configuration.getLongProperty(PROP_IDLE_TIMEOUT, Constants.DEFAULT_IDLE_TIMEOUT);
                    boolean lockAcquired = lockManager.acquireLock(runtimeContext.getVnfContext().getId(), runtimeContext.getRequestContext().getCommonHeader().getRequestId(), timeout);
                    logger.info(String.format(lockAcquired ? "Lock acquired for vnfID = %s" : " VnfId  : %s was already locked.", runtimeContext.getVnfContext().getId()));
                    fillStatus(runtimeContext, LCMCommandStatus.SUCCESS, null);
                } catch (LockException e) {
                    logger.error("Error during Lock operation: " + e.getMessage(), e);
                    Params params = new Params().addParam("errorMsg", e.getMessage());
                    fillStatus(runtimeContext, LCMCommandStatus.LOCKED_VNF_ID, params);
                    storeErrorMessageToLog(runtimeContext,
                            LoggingConstants.TargetNames.APPC,
                            LoggingConstants.TargetNames.LOCK_MANAGER,
                            EELFResourceManager.format(Msg.VF_SERVER_BUSY, runtimeContext.getVnfContext().getId()));
                }
                break;

            case Unlock:
                try {
                    lockManager.releaseLock(runtimeContext.getVnfContext().getId(), runtimeContext.getRequestContext().getCommonHeader().getRequestId());
                    logger.info("Lock released for vnfID = " + runtimeContext.getVnfContext().getId());
                    fillStatus(runtimeContext, LCMCommandStatus.SUCCESS, null);
                } catch (LockException e) {
                    logger.error("Error during Unlock operation: " + e.getMessage(), e);
                    Params params = new Params().addParam("errorMsg", e.getMessage());
                    fillStatus(runtimeContext, LCMCommandStatus.LOCKED_VNF_ID, params);
                }
                break;

            case CheckLock:
                boolean isLocked = lockManager.isLocked(runtimeContext.getVnfContext().getId());
                fillStatus(runtimeContext, LCMCommandStatus.SUCCESS, null);
                runtimeContext.getResponseContext().addKeyValueToAdditionalContext("locked", String.valueOf(isLocked).toUpperCase());
                break;
            default:
                callWfOperation(runtimeContext);
        }
    }


    private void callWfOperation(RuntimeContext runtimeContext) {
        int remainingTTL = calculateRemainingTTL(runtimeContext.getRequestContext().getCommonHeader());
        if (remainingTTL > 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("Calling command Executor with remaining TTL value: " + remainingTTL);
            }

            RuntimeContext clonedContext = cloneContext(runtimeContext);

            CommandExecutorInput commandExecutorInput = new CommandExecutorInput();
            commandExecutorInput.setRuntimeContext(clonedContext);
            commandExecutorInput.setTtl(remainingTTL);

            try {
                commandExecutor.executeCommand(commandExecutorInput);
                if (logger.isTraceEnabled()) {
                    logger.trace("Command was added to queue successfully for vnfID = " + ObjectUtils.toString(runtimeContext.getRequestContext().getActionIdentifiers().getVnfId()));
                }
                fillStatus(runtimeContext, LCMCommandStatus.ACCEPTED, null);
                if (isMetricEnabled) {
                    ((DispatchingFuntionMetric) metricRegistry.metric("DISPATCH_FUNCTION")).incrementAcceptedRequest();
                }
            } catch (APPCException e) {
                logger.error("Unexpected Error : " + e.getMessage(), e);
                String errorMessage = e.getMessage() != null ? e.getMessage() : e.toString();
                Params params = new Params().addParam("errorMsg", errorMessage);
                fillStatus(runtimeContext, LCMCommandStatus.UNEXPECTED_ERROR, params);
            }

        } else {
            fillStatus(runtimeContext, LCMCommandStatus.EXPIRED_REQUEST, null);
            storeErrorMessageToLog(runtimeContext,
                    LoggingConstants.TargetNames.APPC,
                    LoggingConstants.TargetNames.REQUEST_HANDLER,
                    EELFResourceManager.format(Msg.APPC_EXPIRED_REQUEST,
                            runtimeContext.getRequestContext().getCommonHeader().getOriginatorId(),
                            runtimeContext.getRequestContext().getActionIdentifiers().getVnfId(),
                            String.valueOf(runtimeContext.getRequestContext().getCommonHeader().getFlags().getTtl())));
        }
    }


    private int calculateRemainingTTL(CommonHeader commonHeader) {
        if (logger.isTraceEnabled()) {
            logger.trace("Entering to calculateRemainingTTL with RequestHeader = " + ObjectUtils.toString(commonHeader));
        }
        long usedTimeInMillis = (System.currentTimeMillis() - commonHeader.getTimeStamp().getTime());
        int usedTimeInSeconds = (int) (usedTimeInMillis / 1000);
        logger.debug("usedTimeInSeconds = " + usedTimeInSeconds + "usedTimeInMillis = " + usedTimeInMillis);
        // Set ttl value from commonHeader. If not available set it to default
        Integer inputTTL = (commonHeader.getFlags() == null || commonHeader.getFlags().getTtl() <= 0) ?
                Integer.parseInt(configuration.getProperty(Constants.DEFAULT_TTL_KEY, String.valueOf(Constants.DEFAULT_TTL))) :
                commonHeader.getFlags().getTtl();
        logger.debug("inputTTL = " + inputTTL);
        Integer remainingTTL = inputTTL - usedTimeInSeconds;
        logger.debug("Remaining TTL = " + remainingTTL);
        if (logger.isTraceEnabled())
            logger.trace("Exiting from calculateRemainingTTL with (remainingTTL = " + ObjectUtils.toString(remainingTTL) + ")");
        return remainingTTL;
    }

    /*
    * Workaround to clone context in order to prevent sharing of ResponseContext by two threads (one to set Accepted
    * status code and other - depending on DG status). Other properties should not be a problem
    */
    private RuntimeContext cloneContext(RuntimeContext runtimeContext) {
        RuntimeContext other = new RuntimeContext();
        other.setRequestContext(runtimeContext.getRequestContext());
        other.setResponseContext(new ResponseContext());
        other.getResponseContext().setStatus(new Status());
        other.getResponseContext().setCommonHeader(runtimeContext.getRequestContext().getCommonHeader());
        other.setVnfContext(runtimeContext.getVnfContext());
        other.setRpcName(runtimeContext.getRpcName());
        other.setTimeStart(runtimeContext.getTimeStart());
        other.setTransactionRecord(runtimeContext.getTransactionRecord());
        return other;
    }


    /**
     * This method perform operations required before execution of workflow starts. It retrieves next state for current operation from Lifecycle manager and update it in AAI.
     *
     * @param vnfId                   String of VNF ID
     * @param readOnlyActivity        boolean indicator
     * @param forceFlag               boolean indicator
     */
    @Override
    public void onRequestExecutionStart(String vnfId, boolean readOnlyActivity, boolean forceFlag) {

    }
}
