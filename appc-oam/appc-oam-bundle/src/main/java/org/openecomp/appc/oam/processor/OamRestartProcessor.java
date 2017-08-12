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

import com.att.eelf.configuration.EELFLogger;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.i18n.Msg;
import org.openecomp.appc.oam.AppcOam;
import org.openecomp.appc.oam.util.AsyncTaskHelper;
import org.openecomp.appc.oam.util.ConfigurationHelper;
import org.openecomp.appc.oam.util.OperationHelper;
import org.openecomp.appc.oam.util.StateHelper;
import org.openecomp.appc.requesthandler.LCMStateManager;
import org.openecomp.appc.statemachine.impl.readers.AppcOamStates;

/**
 * Processor to handle restart OAM API.
 */
public class OamRestartProcessor extends BaseProcessor {
    /**
     * Action phases:
     * <br> -ToStop: call bundles stop
     * <br> -Stopped: check if all bundle state reached stopped
     * <br> -ToStart: call bundles start
     * <br> -Started: action is full completed
     * <br> -Timeout: indication of timeout reached
     */
    private enum ActionPhases {
        ToStop,
        Stopped,
        ToStart,
        Started,
        Timeout
    }

    /**
     * Constructor
     *
     * @param eelfLogger          for logging
     * @param configurationHelper for property reading
     * @param stateHelper         for APP-C OAM state checking
     * @param asyncTaskHelper     for scheduling async task
     * @param operationHelper     for operational helper
     */
    public OamRestartProcessor(EELFLogger eelfLogger,
                               ConfigurationHelper configurationHelper,
                               StateHelper stateHelper,
                               AsyncTaskHelper asyncTaskHelper,
                               OperationHelper operationHelper) {
        super(eelfLogger, configurationHelper, stateHelper, asyncTaskHelper, operationHelper);

        rpc = AppcOam.RPC.restart;
        auditMsg = Msg.OAM_OPERATION_RESTARTING;
    }

    @Override
    protected void scheduleAsyncTask() {
        runnable = new MyRunnable(this);
        super.scheduleAsyncTask();
    }

    /**
     * This runnable does the async handling for the restart REST API. And it will be scheduled to run one time.
     *
     * <p>This runnable will the following operations: <br>
     *     - do APP-C OAM bundle stop and then start through BundlerHelper<br>
     *     - and always enable LCM operation handling (which can be disabled through maintenance mode API).<br>
     * <p>Once above operations are done, the runnale will <br>
     *     - post message through operatonHelper <br>
     *     - set APP-C OAM state to started <br>
     *     - audit log the state <br>
     */
    class MyRunnable extends BaseActionRunnable {

        ActionPhases currentPhase = ActionPhases.ToStop;
        private LCMStateManager lcmStateManager;

        MyRunnable(BaseProcessor parent) {
            super(parent);

            actionName = "OAM Restart";
            auditMsg = Msg.OAM_OPERATION_RESTARTED;
            finalState = AppcOamStates.Started;
        }

        /**
         * Do restart action, include stop then start and always enable LCM operation.
         * @return true if action is successful, false when aciton is failed or aborted
         */
        @Override
        boolean doAction() {
            logDebug(String.format("Executing %s task at phase (%s)",
                    actionName, currentPhase == null ? "null" : currentPhase.name()));

            boolean isBundleOperationCompleted = true;
            try {
                switch (currentPhase) {
                    case ToStop:
                        isBundleOperationCompleted = bundleHelper.bundleOperations(
                                AppcOam.RPC.stop, bundleNameToFuture, myParent.asyncTaskHelper);
                        currentPhase = ActionPhases.Stopped;
                        break;
                    case Stopped:
                        // check state
                        AppcOamStates currentState = stateHelper.getBundlesState();
                        if (currentState == AppcOamStates.Stopped) {
                            currentPhase = ActionPhases.ToStart;
                        } else {
                            logDebug(String.format("%s task is waiting in stopped phase, current state is %s",
                                    actionName, currentState));
                        }
                        break;
                    case ToStart:
                        isBundleOperationCompleted = bundleHelper.bundleOperations(
                                AppcOam.RPC.start, bundleNameToFuture, myParent.asyncTaskHelper);
                        currentPhase = ActionPhases.Started;
                        break;
                    case Timeout:
                        // do nothing
                        break;
                    default:
                        // Should not reach log it and return false;
                        logger.error("%s task doAction reached %s phase. not supported. return false.",
                                actionName, currentPhase.name());
                        stateHelper.setState(AppcOamStates.Error);
                        return false;
                }

                if (isTimeout("restart doAction")) {
                    currentPhase = ActionPhases.Timeout;
                    return true;
                }
                if (isBundleOperationCompleted) {
                    return true;
                }

                setAbortStatus();
            } catch (APPCException e) {
                setErrorStatus(e);
                stateHelper.setState(AppcOamStates.Error);
            }

            return false;
        }

        /**
         * With additional to get the LCMStateManager service
         * @see BaseActionRunnable#checkState()
         */
        @Override
        boolean checkState() {
            switch (currentPhase) {
                case Started:
                    try {
                        lcmStateManager = operationHelper.getService(LCMStateManager.class);
                        return super.checkState();
                    } catch (APPCException e) {
                        logDebug("LCMStateManager is not available.");
                    }
                    break;
                default:
                    // in all the other ActionPhase, we want the run go back to doAction
                    return true;
            }
            return false;
        }

        /**
         * Final handling. The thread is cancelled.
         * @param setState boolean to indicate if set OAM state or not
         */
        @Override
        void postDoAction(boolean setState) {
            AppcOamStates newState = null;
            if (setState) {
                logDebug("Always enable LCM operation");
                lcmStateManager.enableLCMOperations();
                newState = finalState;
            }
            postAction(newState);
            super.postDoAction(setState);
        }

    }
}
