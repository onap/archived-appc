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

package org.openecomp.appc.dg.common;

import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.openecomp.sdnc.sli.SvcLogicJavaPlugin;

import java.util.Map;
/**
 * DG plugin created for VNF configuration operation to store data in MD-SAL store
 **/
public interface VNFConfigurator extends SvcLogicJavaPlugin{
    /**
     * it is invoked from the DG, and it performs following operations
     * 1. checks whether given yang module is present in the MD-SAL store
     * 2. if it is absent, loads it into MD-SAL store
     * 3. Stores the VNF configuration into MD-SAL store
     * @param params should have 1. uniqueId, 2. yang, 3.configJSON, 4.requestId
     * @param context - DG context
     * @throws APPCException
     */
    void storeConfig(Map<String, String> params, SvcLogicContext context) throws APPCException;
}
