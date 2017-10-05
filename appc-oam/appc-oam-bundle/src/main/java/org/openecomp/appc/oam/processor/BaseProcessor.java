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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Base processor for OAM APIs, such as maintenance mode, restart, start and stop API.
 *
 * <p>This class holds the general API request sync handling methods for all OAM APIs.
 * <p>Specific API processor will overwrite the general methods to add specific behaviors.
 */
public abstract class BaseProcessor extends BaseCommon {
    /** lock to serialize incoming OAM operations.  */
    private static final Object LOCK = new Object();

    final AsyncTaskHelper asyncTaskHelper;
    final BundleHelper bundleHelper;

    /** the requestTimeoutSeconds to use for this OAM operation */
    private Integer requestTimeoutSeconds;
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
            scheduleAsyncTask();
        } catch (Exception e) {
            setErrorStatus(e);
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
        throws InvalidInputException, APPCException, InvalidStateException,InterruptedException,TimeoutException {
        setInitialLogProperties();
        operationHelper.isInputValid(requestInput);

        //The OAM request may specify timeout value
        requestTimeoutSeconds = operationHelper.getParamRequestTimeout(requestInput);

        //All OAM operation pass through here first to validate if an OAM state change is allowed.
        //If a state change is allowed cancel the occurring OAM (if any) before starting this one.
        //we will synchronized so that only one can do this at any given time.
        synchronized(LOCK) {
            AppcOamStates currentOamState = stateHelper.getCurrentOamState();

            //make sure this OAM operation can transition to the desired OAM operation
            AppcOamStates nextState = operationHelper.getNextState(
                    rpc.getAppcOperation(), currentOamState);

            stateHelper.setState(nextState);


            try {
                //cancel the  BaseActionRunnable currently executing
                //it got to be completely terminated before proceeding
                asyncTaskHelper.cancelBaseActionRunnable(
                        rpc,
                        currentOamState,
                        getTimeoutMilliseconds(),
                        TimeUnit.MILLISECONDS
                );
            } catch (TimeoutException e) {
                stateHelper.setState(AppcOamStates.Error);
                throw e;
            }


        }
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

        scheduledRunnable = asyncTaskHelper.scheduleBaseRunnable(
            runnable, runnable::abortRunnable, getInitialDelayMillis(), getDelayMillis());
    }


    /**
     * The timeout for this OAM operation. The timeout source is chosen in the following order:
     * request, config file, default value
     * @return  - the timeout for this OAM operation.
     */
    long getTimeoutMilliseconds() {
        return configurationHelper.getOAMOperationTimeoutValue(this.requestTimeoutSeconds);
    }


    /**
     * @return initialDelayMillis - the time to delay first execution of {@link BaseActionRunnable}
     */
    protected long getInitialDelayMillis(){
        return 0L;
    }

    /**
     * @return delayMillis the delay between the consecutive executions of  {@link BaseActionRunnable}
     */
    private long getDelayMillis(){
        return 1000L;
    }

    /**
     * Cancel the scheduled {@link BaseActionRunnable}  through AsyncTaskHelper
     */
    void cancelAsyncTask() {
        if (scheduledRunnable == null) {
            logger.error(String.format(
                "Skipped cancel schedule async task for rpc(%s) due to scheduledRunnable is null", rpc.name()));
            return;
        }
        scheduledRunnable.cancel(true);
    }

}
