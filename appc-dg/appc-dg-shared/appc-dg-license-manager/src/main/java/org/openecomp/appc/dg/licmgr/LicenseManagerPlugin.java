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

package org.openecomp.appc.dg.licmgr;

import java.util.Map;

import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.openecomp.sdnc.sli.SvcLogicJavaPlugin;


public interface LicenseManagerPlugin extends SvcLogicJavaPlugin {
    /**
     * Retrieves license model from APPC database and populate flags into svc context
     * @param params map with parameters:
     *               org.openecomp.appc.vftype - the vnf type / service type;
     *               org.openecomp.appc.resource-version - the vnf version / service version
     * @param ctx service logic context
     *            1. supposed properties already in context:
     *            aai.input.data.entitlement-assignment-group-uuid - entitlement-group-uuid asset tag already stored in AAI
     *            aai.input.data.license-assignment-group-uuid - license-key-uuid asset tag already stored in AAI
     *            2. properties and flags stored in context after bean execution:
     *            model.entitlement.pool.uuid - entitlement-group-uuid from license model
     *            model.license.key.uuid - license-key-uuid from license model
     *            is.acquire-entitlement.require
     *            is.release-entitlement.require
     *            is.acquire-license.require
     *            is.release-license.require
     *
     * @throws APPCException throws in case of any error
     */
    void retrieveLicenseModel(Map<String, String> params, SvcLogicContext ctx) throws APPCException;
}
