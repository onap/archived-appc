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

package org.openecomp.appc.oam.processor;

import org.openecomp.appc.i18n.Msg;
import org.openecomp.appc.oam.OAMCommandStatus;
import org.openecomp.appc.statemachine.impl.readers.AppcOamStates;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Base runnable actions for OAM APIs, such as maintenance mode, restart, start and stop API.
 *
 * <p>This class holds the general action async handling methods for all OAM APIs.
 * <p>Specific API action runnable will overwrite the general methods to add specific behaviors.
 *
 * <p>Subclass constructor must set the following class variables:
 *   <br>  - actionName
 *   <br>  - auditMsg
 *   <br>  - finalState
 */
abstract class BaseActionRunnable extends BaseCommon implements Runnable {
    final String OAM_OPERATION_TIMEOUT_SECOND = "appc.OAM.api.timeout";
    /** Default operation tiemout set to 1 minute */
    final int DEFAULT_OAM_OPERATION_TIMEOUT = 60;
    /** Abort message format with flexible operation name */
    final String ABORT_MESSAGE_FORMAT = "Aborting %s operation.";
    /** Timeout message format with flexible operation name */
    final String TIMEOUT_MESSAGE_FORMAT = "%s operation has reached timeout %d milliseconds.";

    private boolean isWaiting = false;
    private AppcOamStates currentState;
    long startTimeMs = 0;
    long timeoutMs = 0;
    boolean doTimeoutChecking = false;

    String actionName = "Need to be reset";
    Msg auditMsg;
    AppcOamStates finalState;

    BaseProcessor myParent;
    Map<String, Future<?>> bundleNameToFuture = new HashMap<>();

    BaseActionRunnable(BaseProcessor parent) {
        super(parent.logger, parent.configurationHelper, parent.stateHelper, parent.operationHelper);

        rpc = parent.rpc;
        commonHeader = parent.commonHeader;
        startTime = parent.startTime;
        myParent = parent;

        setTimeoutValues();
    }

    void setTimeoutValues() {
        Integer timeoutSeconds = myParent.timeoutSeconds;
        if (timeoutSeconds == null) {
            timeoutMs = configurationHelper.getConfig().getIntegerProperty(
                    OAM_OPERATION_TIMEOUT_SECOND, DEFAULT_OAM_OPERATION_TIMEOUT) * 1000;
        } else {
            timeoutMs = timeoutSeconds.longValue() * 1000;
        }

        doTimeoutChecking = timeoutMs != 0;
        if (doTimeoutChecking) {
            startTimeMs = startTime.getTime();
        }
        logDebug("%s action runnable check timeout (%s) with timeout (%d)ms, and startMs (%d)",
                rpc.name(), Boolean.toString(doTimeoutChecking), timeoutMs, startTimeMs);
    }

    @Override
    public void run() {
        try {
            setInitialLogProperties();
            logDebug(String.format("===========in %s run (waiting: %s)=======",
                    actionName, Boolean.toString(isWaiting)));

            if (isWaiting) {
                if (!checkState()) {
                    keepWaiting();
                }
            } else {
                if (doAction()) {
                    isWaiting = !checkState();
                } else {
                    postDoAction(false);
                }
            }
        } catch (Exception e) {
            logDebug(String.format("%s got exception %s", actionName, e.getMessage()));
            logger.error(actionName + " exception", e);

        } finally {
            clearRequestLogProperties();
        }
    }

    void keepWaiting() {
        logDebug(String.format("%s runnable waiting, current state is %s.",
                actionName, currentState == null ? "null" : currentState.toString()));

        isTimeout("keepWaiting");
    }

    boolean isTimeout(String parentName) {
        logDebug(String.format("%s task isTimeout called from %s", actionName, parentName));
        if (doTimeoutChecking
                && System.currentTimeMillis() - startTimeMs > timeoutMs) {
            logger.error(String.format("%s operation timeout (%d) ms has reached, abort with error state.",
                    actionName, timeoutMs));

            setStatus(OAMCommandStatus.TIMEOUT, String.format(TIMEOUT_MESSAGE_FORMAT, rpc.name(), timeoutMs));
            postAction(AppcOamStates.Error);
            return true;
        }
        return false;
    }

    /**
     * Set class <b>status</b> to REJECTED with abort message.
     */
    void setAbortStatus() {
        setStatus(OAMCommandStatus.REJECTED, String.format(ABORT_MESSAGE_FORMAT, rpc.name()));
    }

    /**
     * Final handling. The thread is cancelled.
     * @param setState boolean to indicate if set OAM state or not
     */
    void postDoAction(boolean setState) {
        logDebug(String.format("Finished %s task", actionName));
    }

    /**
     * Handling for after doAction. does post notification,  issue audit log and set OAM state based on input
     * @param state of AppcOamState to be set as OAM state when it is not null.
     */
    void postAction(AppcOamStates state) {
        operationHelper.sendNotificationMessage(rpc, commonHeader, status);

        if (state != null) {
            stateHelper.setState(state);
        }

        auditInfoLog(auditMsg);

        myParent.cancelAsyncTask();
    }

    /**
     * Check state
     * @return true if final state reached, otherwise return false
     */
    boolean checkState() {
        if (isTimeout("checkState")) {
            myParent.bundleHelper.cancelUnfinished(bundleNameToFuture);
            return true;
        }

        if (!myParent.bundleHelper.isAllTaskDone(bundleNameToFuture)) {
            return false;
        }

        long failedTask = myParent.bundleHelper.getFailedMetrics(bundleNameToFuture);
        if (failedTask != 0) {
            String errorMsg = failedTask + " bundle(s) failed, see logs for details.";
            setStatus(OAMCommandStatus.UNEXPECTED_ERROR, errorMsg);
            postAction(AppcOamStates.Error);
            return true;
        }

        currentState = stateHelper.getBundlesState();
        if (currentState == finalState) {
            setStatus(OAMCommandStatus.SUCCESS);
            postDoAction(true);
            return true;
        }
        return false;
    }

    abstract boolean doAction();
}
