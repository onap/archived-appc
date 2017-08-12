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
import org.openecomp.appc.statemachine.impl.readers.AppcOamStates;

/**
 * Processor to handle stop OAM API.
 */
public class OamStopProcessor extends BaseProcessor {
    /**
     * Constructor
     *
     * @param eelfLogger          for logging
     * @param configurationHelper for property reading
     * @param stateHelper         for APP-C OAM state checking
     * @param asyncTaskHelper     for scheduling async task
     * @param operationHelper     for operational helper
     */
    public OamStopProcessor(EELFLogger eelfLogger,
                            ConfigurationHelper configurationHelper,
                            StateHelper stateHelper,
                            AsyncTaskHelper asyncTaskHelper,
                            OperationHelper operationHelper) {
        super(eelfLogger, configurationHelper, stateHelper, asyncTaskHelper, operationHelper);

        rpc = AppcOam.RPC.stop;
        auditMsg = Msg.OAM_OPERATION_STOPPING;
    }


    @Override
    protected void scheduleAsyncTask() {
        runnable = new MyRunnable(this);
        super.scheduleAsyncTask();
    }

    /**
     * This runnable does the async handling for the stop REST API. And it will be scheduled to run one time.
     *
     * <p>This runnable will the following operations: <br>
     *     - do APP-C OAM bundle stop and then refresh through BundlerHelper<br>
     * <p>Once above operations are done, the runnale will <br>
     *     - post message through operatonHelper <br>
     *     - set APP-C OAM state to started <br>
     *     - audit log the state <br>
     */
    class MyRunnable  extends BaseActionRunnable {

        MyRunnable(BaseProcessor parent) {
            super(parent);
            actionName = "OAM Stop";
            auditMsg = Msg.OAM_OPERATION_STOPPED;
            finalState = AppcOamStates.Stopped;
        }

        /**
         * Do stop action, include stop bundle .
         * @return true if action is successful, false when aciton is failed.
         */
        @Override
        boolean doAction() {
            logDebug(String.format("Executing %s task", actionName));

            try {
                boolean isBundleOperationCompleted = bundleHelper.bundleOperations(
                        rpc, bundleNameToFuture, myParent.asyncTaskHelper);
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
         * Final handling
         * @param setState boolean to indicate if set OAM state or not
         */
        @Override
        void postDoAction(boolean setState) {
            postAction(setState ? finalState : null);
            super.postDoAction(setState);
        }
    }
}
