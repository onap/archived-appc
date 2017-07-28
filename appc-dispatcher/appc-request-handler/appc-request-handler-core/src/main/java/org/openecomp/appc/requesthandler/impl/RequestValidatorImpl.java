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

import com.att.eelf.i18n.EELFResourceManager;
import org.apache.commons.lang.ObjectUtils;
import org.openecomp.appc.domainmodel.lcm.RuntimeContext;
import org.openecomp.appc.domainmodel.lcm.VNFContext;
import org.openecomp.appc.domainmodel.lcm.VNFOperation;
import org.openecomp.appc.executor.UnstableVNFException;
import org.openecomp.appc.i18n.Msg;
import org.openecomp.appc.lifecyclemanager.LifecycleManager;
import org.openecomp.appc.lifecyclemanager.objects.LifecycleException;
import org.openecomp.appc.lifecyclemanager.objects.NoTransitionDefinedException;
import org.openecomp.appc.logging.LoggingConstants;
import org.openecomp.appc.logging.LoggingUtils;
import org.openecomp.appc.requesthandler.LCMStateManager;
import org.openecomp.appc.requesthandler.exceptions.*;
import org.openecomp.appc.workingstatemanager.WorkingStateManager;


public class RequestValidatorImpl extends AbstractRequestValidatorImpl {

    private WorkingStateManager workingStateManager;
    private LCMStateManager lcmStateManager;

    public void setLifecyclemanager(LifecycleManager lifecyclemanager) {
        this.lifecyclemanager = lifecyclemanager;
    }

    public void setWorkingStateManager(WorkingStateManager workingStateManager) {
        this.workingStateManager = workingStateManager;
    }

    public void setLcmStateManager(LCMStateManager lcmStateManager) {
        this.lcmStateManager = lcmStateManager;
    }

    public RequestValidatorImpl() {
    }

    @Override
    public void validateRequest(RuntimeContext runtimeContext)
            throws VNFNotFoundException, RequestExpiredException, UnstableVNFException, InvalidInputException,
            DuplicateRequestException, NoTransitionDefinedException, LifecycleException, WorkflowNotFoundException,
            DGWorkflowNotFoundException, MissingVNFDataInAAIException, LCMOperationsDisabledException {
        if (logger.isTraceEnabled()){
            logger.trace("Entering to validateRequest with RequestHandlerInput = "+ ObjectUtils.toString(runtimeContext));
        }
        if(!lcmStateManager.isLCMOperationEnabled()) {
            LoggingUtils.logErrorMessage(
                    LoggingConstants.TargetNames.REQUEST_VALIDATOR,
                    EELFResourceManager.format(Msg.LCM_OPERATIONS_DISABLED),
                    this.getClass().getCanonicalName());
            throw new LCMOperationsDisabledException("APPC LCM operations have been administratively disabled");
        }

        getAAIservice();
        validateInput(runtimeContext.getRequestContext());
        checkVNFWorkingState(runtimeContext);
        String vnfId = runtimeContext.getRequestContext().getActionIdentifiers().getVnfId();
        VNFContext vnfContext = queryAAI(vnfId);
        runtimeContext.setVnfContext(vnfContext);

        queryLCM(runtimeContext.getVnfContext().getStatus(), runtimeContext.getRequestContext().getAction());
        VNFOperation operation = runtimeContext.getRequestContext().getAction();
        if(!operation.isBuiltIn()) {
            // for built-in operations skip WF presence check
            queryWFM(vnfContext, runtimeContext.getRequestContext());
        }
    }


    private String queryLCM(String orchestrationStatus, VNFOperation action) throws LifecycleException, NoTransitionDefinedException {
        if (logger.isTraceEnabled()) {
            logger.trace("Entering to queryLCM  with Orchestration Status = "+ ObjectUtils.toString(orchestrationStatus)+
                    ", command = "+ ObjectUtils.toString(action));
        }

        String nextState = lifecyclemanager.getNextState(null, orchestrationStatus, action.name());
        if (logger.isDebugEnabled()) {
            logger.trace("Exiting from queryLCM with (LCMResponse = "+ ObjectUtils.toString(nextState)+")");
        }
        return nextState;
    }


    private void checkVNFWorkingState(RuntimeContext runtimeContext) throws UnstableVNFException {

        if (logger.isTraceEnabled()) {
            logger.trace("Entering to checkVNFWorkingState with RequestHandlerInput = "+ ObjectUtils.toString(runtimeContext.getRequestContext()));
        }
        boolean forceFlag = runtimeContext.getRequestContext().getCommonHeader().getFlags() != null && runtimeContext.getRequestContext().getCommonHeader().getFlags().isForce();
        String vnfId = runtimeContext.getRequestContext().getActionIdentifiers().getVnfId();

        if (logger.isDebugEnabled()) {
            logger.debug("forceFlag = " + forceFlag);
        }
        boolean isVNFStable = workingStateManager.isVNFStable(vnfId);
        if (!isVNFStable && !forceFlag) {
            if (logger.isDebugEnabled()) {
                logger.debug("VNF is not stable for VNF ID = " + vnfId);
            }
            throw new UnstableVNFException("VNF is not stable for vnfID = " + vnfId);
        }

    }


}
