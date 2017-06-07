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

package org.openecomp.appc.dg.util.impl;

import java.util.Map;

import org.openecomp.appc.dg.util.UpgradeStubNode;
import org.openecomp.appc.exceptions.APPCException;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.openecomp.sdnc.sli.SvcLogicContext;


public class UpgradeStubNodeImpl implements UpgradeStubNode {

    public static final String FAILURE_INDICATOR_FIELD_NAME = "failureIndicator";
    private static final EELFLogger logger = EELFManager.getInstance().getLogger(UpgradeStubNodeImpl.class);

    @Override
    public void handleUpgradeStub(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        logger.debug("Entering in handleUpgradeStub : "+ params.toString());
        String failureInd = params.get(FAILURE_INDICATOR_FIELD_NAME);
        if (null != failureInd &&  Boolean.valueOf(failureInd)){
            throw new APPCException("Simulating exception...");
        }
        logger.info("Simulating was successful");
    }
}
