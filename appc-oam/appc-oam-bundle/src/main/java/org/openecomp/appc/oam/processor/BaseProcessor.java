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
import org.opendaylight.yang.gen.v1.org.openecomp.appc.oam.rev170303.status.Status;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.exceptions.InvalidInputException;
import org.openecomp.appc.exceptions.InvalidStateException;
import org.openecomp.appc.i18n.Msg;
import org.openecomp.appc.oam.OAMCommandStatus;
import org.openecomp.appc.oam.util.AsyncTaskHelper;
import org.openecomp.appc.oam.util.BundleHelper;
import org.openecomp.appc.oam.util.ConfigurationHelper;
import org.openecomp.appc.oam.util.OperationHelper;
import org.openecomp.appc.oam.util.StateHelper;
import org.openecomp.appc.statemachine.impl.readers.AppcOamStates;

import java.util.Date;
import java.util.concurrent.Future;

/**
 * Base processor for OAM APIs, such as maintenance mode, restart, start and stop API.
 *
 * <p>This class holds the general API request sync handling methods for all OAM APIs.
 * <p>Specific API processor will overwrite the general methods to add specific behaviors.
 */
public abstract class BaseProcessor extends BaseCommon {
    final AsyncTaskHelper asyncTaskHelper;
    final BundleHelper bundleHelper;


    Integer timeoutSeconds;
    Msg auditMsg;
    BaseActionRunnable runnable;
    private Future<?> scheduledRunnable = null;

    /**
     * Constructor
     *
     * @param eelfLogger for logging
     * @param configurationHelperIn for property reading
     * @param stateHelperIn for APP-C OAM state checking
     * @param asyncTaskHelperIn for scheduling async task
     * @param operationHelperIn for operational helper
     */
    BaseProcessor(EELFLogger eelfLogger,
                  ConfigurationHelper configurationHelperIn,
                  StateHelper stateHelperIn,
                  AsyncTaskHelper asyncTaskHelperIn,
                  OperationHelper operationHelperIn) {
        super(eelfLogger, configurationHelperIn, stateHelperIn, operationHelperIn);

        asyncTaskHelper = asyncTaskHelperIn;
        bundleHelper = new BundleHelper(eelfLogger, configurationHelper, stateHelper);
    }

    /**
     * Process synch handling and schedule asynch task
     *
     * @param requestInput of REST API request
     * @return Status of new APP-C OAM state
     */
    public Status processRequest(final Object requestInput) {
        startTime = new Date();
        commonHeader = operationHelper.getCommonHeader(requestInput);
        setStatus(OAMCommandStatus.ACCEPTED);

        try {
            preProcess(requestInput);
            timeoutSeconds = operationHelper.getParamRequestTimeout(requestInput);
            scheduleAsyncTask();
        } catch (Exception t) {
            setErrorStatus(t);
        } finally {
            postProcess();
        }

        return status;
    }

    /**
     * Preprocess before actual handling of the REST API call. Does:
     * <p> - commonHeader validation
     * <p> - get NextState as well as validate if next state is valid
     * <p> - set logging properties
     * <p> - set appcCurrentState to next state
     *
     * @throws InvalidInputException when commonHeader validation failed
     * @throws APPCException         when state validation failed
     */
    protected void preProcess(final Object requestInput)
            throws InvalidInputException, APPCException, InvalidStateException {
        operationHelper.isInputValid(requestInput);

        AppcOamStates nextState = operationHelper.getNextState(
                rpc.getAppcOperation(), stateHelper.getCurrentOamState());
        setInitialLogProperties();
        stateHelper.setState(nextState);
    }

    /**
     * Post process includes audit logging as well as clear MDC properties.
     */
    private void postProcess() {
        auditInfoLog(auditMsg);
        clearRequestLogProperties();
    }

    /**
     * Schedule async task through AsyncTaskHelper.
     */
    protected void scheduleAsyncTask() {
        if (runnable == null) {
            logger.error(String.format(
                    "Skipped schedule async task for rpc(%s) due to runnable is null", rpc.name()));
            return;
        }

        scheduledRunnable = asyncTaskHelper.scheduleAsyncTask(rpc, runnable);
    }

    /**
     * Check if current running task is the same as schedule task
     * @return true if they are the same, otherwise false.
     */
    boolean isSameAsyncTask() {
        return asyncTaskHelper.getCurrentAsyncTask() == scheduledRunnable;
    }

    /**
     * Cancel schedueled async task through AsyncTaskHelper
     */
    void cancelAsyncTask() {
        if (scheduledRunnable == null) {
            logger.error(String.format(
                    "Skipped cancel schedule async task for rpc(%s) due to scheduledRunnable is null", rpc.name()));
            return;
        }

        asyncTaskHelper.cancelAsyncTask(scheduledRunnable);
        scheduledRunnable = null;
    }
}
