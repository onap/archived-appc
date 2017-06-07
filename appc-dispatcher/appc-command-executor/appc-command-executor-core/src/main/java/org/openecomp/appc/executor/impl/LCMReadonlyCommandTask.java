/*-
 * ============LICENSE_START=======================================================
 * APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Amdocs
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */

package org.openecomp.appc.executor.impl;


import org.apache.commons.lang3.StringUtils;
import org.openecomp.appc.domainmodel.lcm.CommonHeader;
import org.openecomp.appc.domainmodel.lcm.RuntimeContext;
import org.openecomp.appc.executor.UnstableVNFException;
import org.openecomp.appc.executor.objects.CommandResponse;
import org.openecomp.appc.executor.objects.LCMCommandStatus;
import org.openecomp.appc.executor.objects.Params;
import org.openecomp.appc.executor.objects.UniqueRequestIdentifier;
import org.openecomp.appc.requesthandler.RequestHandler;
import org.openecomp.appc.workflow.WorkFlowManager;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class LCMReadonlyCommandTask extends CommandTask  {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(LCMReadonlyCommandTask.class);

    public LCMReadonlyCommandTask(RuntimeContext commandRequest, RequestHandler requestHandler,
            WorkFlowManager workflowManager) {
        super(commandRequest, requestHandler, workflowManager);
    }

    @Override
    public void onRequestCompletion(CommandResponse response) {
        super.onRequestCompletion(response, true);
    }

    @Override
    public void run() {
        RuntimeContext request = commandRequest;
        final CommonHeader commonHeader = request.getRequestContext().getCommonHeader();
        final boolean forceFlag = commonHeader.getFlags().isForce();
        UniqueRequestIdentifier requestIdentifier = new UniqueRequestIdentifier(commonHeader.getOriginatorId(), commonHeader.getRequestId(), commonHeader.getSubRequestId());
        String requestIdentifierString = requestIdentifier.toIdentifierString();
        final String vnfId = request.getVnfContext().getId();
        try {
            requestHandler.onRequestExecutionStart(vnfId,true, requestIdentifierString, forceFlag);
            super.execute();
        } catch (UnstableVNFException e) {
            logger.error(e.getMessage(), e);
            Params params = new Params().addParam("vnfId",vnfId);
            request.getResponseContext().setStatus(LCMCommandStatus.UNSTABLE_VNF_FAILURE.toStatus(params));
        }catch (Exception e) {
            logger.error("Error during runing LCMReadonlyCommandTask.", e);
            String errorMsg = StringUtils.isEmpty(e.getMessage()) ? e.toString() : e.getMessage();
            Params params = new Params().addParam("errorMsg",errorMsg);
            request.getResponseContext().setStatus(LCMCommandStatus.UNEXPECTED_FAILURE.toStatus(params));
        }
    }
}
