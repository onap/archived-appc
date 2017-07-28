/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.appc.requesthandler.impl;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import org.openecomp.appc.common.constant.Constants;
import org.openecomp.appc.domainmodel.lcm.RuntimeContext;
import org.openecomp.appc.domainmodel.lcm.Status;
import org.openecomp.appc.domainmodel.lcm.VNFOperation;
import org.openecomp.appc.executor.UnstableVNFException;
import org.openecomp.appc.executor.objects.LCMCommandStatus;
import org.openecomp.appc.executor.objects.Params;
import org.openecomp.appc.executor.objects.UniqueRequestIdentifier;
import org.openecomp.appc.i18n.Msg;
import org.openecomp.appc.lockmanager.api.LockException;
import org.openecomp.appc.lockmanager.api.LockManager;
import org.openecomp.appc.logging.LoggingConstants;
import org.openecomp.appc.workingstatemanager.WorkingStateManager;
import org.openecomp.appc.workingstatemanager.objects.VNFWorkingState;

/**
 * This class provides application logic for the Request/Response Handler Component.
 *
 */
public class RequestHandlerImpl extends AbstractRequestHandlerImpl {

    /**
     * APP-C VNF lock idle timeout in milliseconds. Applied only when locking VNF using northbound API "lock"
     */
    private static final String PROP_IDLE_TIMEOUT = "org.openecomp.appc.lock.idleTimeout";

    private LockManager lockManager;

    private WorkingStateManager workingStateManager;

    public void setLockManager(LockManager lockManager) {
        this.lockManager = lockManager;
    }

    public void setWorkingStateManager(WorkingStateManager workingStateManager) {
        this.workingStateManager = workingStateManager;
    }

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(RequestHandlerImpl.class);

    protected void handleRequest(RuntimeContext runtimeContext) {

        switch (runtimeContext.getRequestContext().getAction()) {
            case Lock:
                try {
                    lockWithTimeout(runtimeContext.getVnfContext().getId(), runtimeContext.getRequestContext().getCommonHeader().getRequestId());
                    fillStatus(runtimeContext, LCMCommandStatus.SUCCESS, null);
                } catch (LockException e) {
                    Params params = new Params().addParam("errorMsg", e.getMessage());
                    fillStatus(runtimeContext, LCMCommandStatus.LOCKING_FAILURE, params);
                    storeErrorMessageToLog(runtimeContext,
                            LoggingConstants.TargetNames.APPC,
                            LoggingConstants.TargetNames.LOCK_MANAGER,
                            EELFResourceManager.format(Msg.VF_SERVER_BUSY, runtimeContext.getVnfContext().getId()));
                }
                break;

            case Unlock:
                try {
                    releaseVNFLock(runtimeContext.getVnfContext().getId(), runtimeContext.getRequestContext().getCommonHeader().getRequestId());
                    fillStatus(runtimeContext,LCMCommandStatus.SUCCESS, null);
                } catch (LockException e) {
                    //TODO add proper error code and message
                    //  logger.error(EELFResourceManager.format(Msg.VF_SERVER_BUSY, runtimeContext.getVnfContext().getId()));
                    Params params = new Params().addParam("errorMsg", e.getMessage());
                    fillStatus(runtimeContext, LCMCommandStatus.LOCKING_FAILURE, params);
                }
                break;

            case CheckLock:
                boolean isLocked = lockManager.isLocked(runtimeContext.getVnfContext().getId());
                fillStatus(runtimeContext,LCMCommandStatus.SUCCESS, null);
                runtimeContext.getResponseContext().addKeyValueToAdditionalContext("locked", String.valueOf(isLocked).toUpperCase());
                break;
            default:
                try {
                    boolean lockAcquired = acquireVNFLock(runtimeContext.getVnfContext().getId(), runtimeContext.getRequestContext().getCommonHeader().getRequestId(), 0);
                    runtimeContext.setIsLockAcquired(lockAcquired);
                    callWfOperation(runtimeContext);
                } catch (LockException e) {
                    Params params = new Params().addParam("errorMsg", e.getMessage());
                    fillStatus(runtimeContext, LCMCommandStatus.LOCKING_FAILURE, params);
                } finally {
                    if (runtimeContext.isLockAcquired()) {
                        final int statusCode = runtimeContext.getResponseContext().getStatus().getCode();
                        if (statusCode % 100 == 2 || statusCode % 100 == 3) {
                            try {
                                releaseVNFLock(runtimeContext.getVnfContext().getId(), runtimeContext.getRequestContext().getCommonHeader().getRequestId());
                            } catch (LockException e) {
                                logger.error("Error releasing the lock",e);
                            }
                        }
                    }
                }
        }
    }

    private void releaseVNFLock(String vnfId, String transactionId) throws LockException {
        lockManager.releaseLock(vnfId, transactionId);
        logger.info("Lock released for vnfID = " + vnfId);
    }

    protected void lockWithTimeout(String vnfId, String requestId) throws LockException {
        long timeout = configuration.getLongProperty(PROP_IDLE_TIMEOUT, Constants.DEFAULT_IDLE_TIMEOUT);
        acquireVNFLock(vnfId, requestId, timeout);
    }

    private boolean acquireVNFLock(String vnfID, String requestId, long timeout) throws LockException {
        if (logger.isTraceEnabled())
            logger.trace("Entering to acquireVNFLock with vnfID = " + vnfID);
        boolean lockAcquired = lockManager.acquireLock(vnfID, requestId, timeout);
        if (lockAcquired) {
            logger.info("Lock acquired for vnfID = " + vnfID);
        } else {
            logger.info("vnfID = " + vnfID + " was already locked");
        }
        return lockAcquired;
    }

    /**
     * This method perform operations required before execution of workflow starts. It retrieves next state for current operation from Lifecycle manager and update it in AAI.
     *
     * @param vnfId String of VNF ID
     * @param readOnlyActivity boolean indicator
     * @param  requestIdentifierString - string contains id uniquely represents the request
     * @param forceFlag boolean indicator
     * @throws UnstableVNFException when failed
     */
    @Override
    public void onRequestExecutionStart(String vnfId, boolean readOnlyActivity, String requestIdentifierString, boolean forceFlag) throws UnstableVNFException {
        if (logger.isTraceEnabled()) {
            logger.trace("Entering to onRequestExecutionStart with vnfId = " + vnfId + "and requestIdentifierString = " + requestIdentifierString);
        }

        if(!readOnlyActivity || !forceFlag || workingStateManager.isVNFStable(vnfId)) {
            boolean updated = false;
            try {
                updated = workingStateManager.setWorkingState(vnfId, VNFWorkingState.UNSTABLE, requestIdentifierString, forceFlag);
            }  catch (Exception e) {
                logger.error("Error updating working state for vnf " + vnfId + e);
                throw new RuntimeException(e);
            }
            if (!updated) {
                throw new UnstableVNFException("VNF is not stable for vnfID = " + vnfId);
            }
        }

        if (logger.isTraceEnabled())
            logger.trace("Exiting from onRequestExecutionStart ");
    }

    private boolean isReadOnlyAction(VNFOperation action) {
        if (VNFOperation.Sync.toString().equals(action) ||
                VNFOperation.Audit.toString().equals(action) ||
                VNFOperation.ConfigBackup.toString().equals(action) ||
                VNFOperation.ConfigBackupDelete.toString().equals(action) ||
                VNFOperation.ConfigExport.toString().equals(action)){
            return true;
        }
        return false;
    }

    @Override
    public void onRequestExecutionEnd(RuntimeContext runtimeContext, boolean isAAIUpdated) {
        super.onRequestExecutionEnd(runtimeContext,isAAIUpdated);
        VNFWorkingState workingState;
        Status status = runtimeContext.getResponseContext().getStatus();
        if (status.getCode() == LCMCommandStatus.SUCCESS.getResponseCode() || isReadOnlyAction(runtimeContext.getRequestContext().getAction())) {
            workingState = VNFWorkingState.STABLE;
        } else {
            workingState = VNFWorkingState.UNKNOWN;
        }

        UniqueRequestIdentifier requestIdentifier = new UniqueRequestIdentifier(runtimeContext.getResponseContext().getCommonHeader().getOriginatorId(),
                runtimeContext.getResponseContext().getCommonHeader().getRequestId(),
                runtimeContext.getResponseContext().getCommonHeader().getSubRequestId());

        String requestIdentifierString = requestIdentifier.toIdentifierString();
        workingStateManager.setWorkingState(runtimeContext.getVnfContext().getId(), workingState, requestIdentifierString, false);
        logger.debug("Reset lock for vnfId " + runtimeContext.getVnfContext().getId());
        resetLock(runtimeContext.getVnfContext().getId(), runtimeContext.getResponseContext().getCommonHeader().getRequestId(), runtimeContext.isLockAcquired(), true);
    }

    private void resetLock(String vnfId, String requestId, boolean lockAcquired, boolean resetLockTimeout) {
        if (lockAcquired) {
            try {
                releaseVNFLock(vnfId, requestId);
            } catch (LockException e) {
                logger.error("Unlock VNF [" + vnfId + "] failed. Request id: [" + requestId + "]", e);
            }
        } else if (resetLockTimeout) {
            try {
                // reset timeout to previous value
                lockWithTimeout(vnfId, requestId);
            } catch (LockException e) {
                logger.error("Reset lock idle timeout for VNF [" + vnfId + "] failed. Request id: [" + requestId + "]", e);
            }
        }
    }

    @Override
    public void onRequestTTLEnd(RuntimeContext runtimeContext, boolean updateAAI) {
        super.onRequestTTLEnd(runtimeContext,updateAAI);
        resetLock(runtimeContext.getVnfContext().getId(), runtimeContext.getResponseContext().getCommonHeader().getRequestId(), runtimeContext.isLockAcquired(), true);
    }
}
