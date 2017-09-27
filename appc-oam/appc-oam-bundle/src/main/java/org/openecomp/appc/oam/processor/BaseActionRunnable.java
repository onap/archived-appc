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
import org.openecomp.appc.oam.AppcOam;
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
public abstract class BaseActionRunnable extends BaseCommon implements Runnable {
    /** Abort due to rejection message format with flexible operation name */
    final String ABORT_MESSAGE_FORMAT = "Aborting %s operation due to %s.";
    /** Timeout message format with flexible operation name */
    final String TIMEOUT_MESSAGE_FORMAT = "%s operation has reached timeout %d milliseconds.";
    /** Failure message format with flexible number of bundles */
    final String BUNDLE_OPERATION_FAILED_FORMAT = "%d bundle(s) failed, see logs for details.";
    final String NEW_RPC_OPERATION_REQUEST = "new %s operation request";
    final String DUE_TO_EXECUTION_ERROR = "due to execution error.";

    private boolean isWaiting = false;
    long startTimeMs = 0;
    long timeoutMs = 0;
    boolean doTimeoutChecking = false;

    String actionName = "Need to be reset";
    Msg auditMsg;
    AppcOamStates finalState;

    BaseProcessor myParent;
    Map<String, Future<?>> bundleNameToFuture = new HashMap<>();

    /**
     * Constructor
     *
     * @param parent BaseProcessor who has called this constructor.
     */
    BaseActionRunnable(BaseProcessor parent) {
        super(parent.logger, parent.configurationHelper, parent.stateHelper, parent.operationHelper);

        rpc = parent.rpc;
        commonHeader = parent.commonHeader;
        myParent = parent;
        setTimeoutValues();
    }

    /**
     * Collect the timeout value for this {@link BaseActionRunnable}
     */
    void setTimeoutValues() {
        startTime = myParent.startTime;
        timeoutMs = myParent.getTimeoutMilliseconds();
        doTimeoutChecking = timeoutMs != 0;
        if (doTimeoutChecking) {
            startTimeMs = startTime.getTime();
        }
        logDebug("%s action runnable check timeout (%s) with timeout (%d)ms, and startMs (%d)",
            rpc.name(), Boolean.toString(doTimeoutChecking), timeoutMs, startTimeMs);
    }


    /**
     * Abort operation handling due to outside interruption, does<br>
     *     - set ABORT status<br>
     *     - send notification message<br>
     *     - add audit log
     *
     * @param newRpc of the new AppcOam.RPC operation.
     */
    void abortRunnable(final AppcOam.RPC newRpc) {
        resetLogProperties(false);

        String additionalMsg = String.format(NEW_RPC_OPERATION_REQUEST, newRpc);
        logDebug("%s action aborted due to %s", rpc.name(), additionalMsg);
        setStatus(OAMCommandStatus.ABORT, String.format(ABORT_MESSAGE_FORMAT, rpc.name(), additionalMsg));
        operationHelper.sendNotificationMessage(rpc, commonHeader, status);
        auditInfoLog(auditMsg);

        resetLogProperties(true);
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

    /**
     * Keep waiting to be override by children classes for different behaviors.
     * Timeout is validated here.
     */
    void keepWaiting() {
        logDebug(String.format("%s runnable waiting, current state is %s.",
            actionName, stateHelper.getCurrentOamState()));

        isTimeout("keepWaiting");
    }

    /**
     * Check if the timeout milliseconds has reached.
     *
     * @param parentName String of the caller, for logging purpose.
     * @return true if the timeout has reached, otherwise false.
     */
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
     * Check if all bundle operations are successful through BundleHelper.
     * If there's failed bundler operation, set error status and trigger postAction with Error state.
     *
     * @return true if bundler operations have failure, otherwise false.
     */
    boolean hasBundleOperationFailure() {
        long failedTask = myParent.bundleHelper.getFailedMetrics(bundleNameToFuture);
        if (failedTask == 0) {
            return false;
        }

        setStatus(OAMCommandStatus.UNEXPECTED_ERROR, String.format(BUNDLE_OPERATION_FAILED_FORMAT, failedTask));
        postAction(AppcOamStates.Error);
        return true;
    }

    /**
     * Set class <b>status</b> to ABORT with abort message.
     */
    void setAbortStatus() {
        setStatus(OAMCommandStatus.ABORT, String.format(ABORT_MESSAGE_FORMAT, rpc.name(), DUE_TO_EXECUTION_ERROR));
    }

    /**
     * Final handling. The thread is cancelled.
     *
     * @param setState boolean to indicate if set OAM state or not
     */
    void postDoAction(boolean setState) {
        logDebug(String.format("Finished %s task", actionName));
    }

    /**
     * Handling for after doAction. does post notification, issue audit log and set OAM state based on input.
     *
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
     *
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

        if (hasBundleOperationFailure()) {
            return true;
        }

        if (stateHelper.getBundlesState() == finalState) {
            setStatus(OAMCommandStatus.SUCCESS);
            postDoAction(true);
            return true;
        }
        return false;
    }

    abstract boolean doAction();
}
