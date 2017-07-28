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
import org.openecomp.appc.domainmodel.lcm.RuntimeContext;
import org.openecomp.appc.domainmodel.lcm.VNFContext;
import org.openecomp.appc.domainmodel.lcm.VNFOperation;
import org.openecomp.appc.i18n.Msg;
import org.openecomp.appc.logging.LoggingConstants;
import org.openecomp.appc.logging.LoggingUtils;
import org.openecomp.appc.requesthandler.exceptions.*;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class VMRequestValidatorImpl extends AbstractRequestValidatorImpl {

    @Override
    public void validateRequest(RuntimeContext runtimeContext) throws VNFNotFoundException, RequestExpiredException, InvalidInputException, WorkflowNotFoundException, DGWorkflowNotFoundException, MissingVNFDataInAAIException, LCMOperationsDisabledException, DuplicateRequestException {
        if(!lcmStateManager.isLCMOperationEnabled()) {
            LoggingUtils.logErrorMessage(
                    LoggingConstants.TargetNames.REQUEST_VALIDATOR,
                    EELFResourceManager.format(Msg.LCM_OPERATIONS_DISABLED),
                    this.getClass().getCanonicalName());
            throw new LCMOperationsDisabledException("APPC LCM operations have been administratively disabled");
        }

        getAAIservice();
        super.validateInput(runtimeContext.getRequestContext());
        String vnfId = runtimeContext.getRequestContext().getActionIdentifiers().getVnfId();

        VNFContext vnfContext = queryAAI(vnfId);
        runtimeContext.setVnfContext(vnfContext);

        VNFOperation operation = runtimeContext.getRequestContext().getAction();
        if(supportedVMLevelAction().contains(operation)) {
            queryWFM(vnfContext, runtimeContext.getRequestContext());
        }
        else{
            throw new LCMOperationsDisabledException("Action "+ operation.name() + " is not supported on VM level");
        }
    }

    public Set<VNFOperation> supportedVMLevelAction(){
        Set<VNFOperation> vnfOperations = new HashSet<>();
        vnfOperations.add(VNFOperation.Start);
        vnfOperations.add(VNFOperation.Stop);
        vnfOperations.add(VNFOperation.Restart);
        vnfOperations.add(VNFOperation.Rebuild);
        vnfOperations.add(VNFOperation.Terminate);
        vnfOperations.add(VNFOperation.Migrate);
        vnfOperations.add(VNFOperation.Evacuate);
        vnfOperations.add(VNFOperation.Snapshot);
        return vnfOperations;
    }


}
