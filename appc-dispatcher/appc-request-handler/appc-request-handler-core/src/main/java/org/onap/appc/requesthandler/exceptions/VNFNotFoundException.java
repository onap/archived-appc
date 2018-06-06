/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.requesthandler.exceptions;


import com.att.eelf.i18n.EELFResourceManager;
import org.onap.appc.executor.objects.LCMCommandStatus;
import org.onap.appc.executor.objects.Params;
import org.onap.appc.i18n.Msg;
import org.onap.appc.logging.LoggingConstants;

public class VNFNotFoundException extends RequestValidationException {
    public VNFNotFoundException(String message, String vnfId){
        super(message);
        super.setLcmCommandStatus(LCMCommandStatus.VNF_NOT_FOUND);
        Params params = new Params().addParam("vnfId", vnfId);
        super.setLogMessage(EELFResourceManager.format(Msg.APPC_NO_RESOURCE_FOUND, vnfId));
        super.setParams(params);
        super.setTargetEntity(LoggingConstants.TargetNames.AAI);
        super.setTargetService("");
    }
}
