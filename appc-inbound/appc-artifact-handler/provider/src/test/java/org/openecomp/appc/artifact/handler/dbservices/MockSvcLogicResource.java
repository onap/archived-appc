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

package org.openecomp.appc.artifact.handler.dbservices;

import java.util.Map;

import org.openecomp.sdnc.sli.SvcLogicContext;
import org.openecomp.sdnc.sli.SvcLogicException;
import org.openecomp.sdnc.sli.resource.sql.SqlResource;

public class MockSvcLogicResource extends SqlResource {

    @Override
    public QueryStatus query(String resource, boolean localOnly, String select, String key, String prefix,
            String orderBy, SvcLogicContext ctx) throws SvcLogicException {
        QueryStatus status = QueryStatus.SUCCESS;
        ctx.setAttribute("id", "testId");
        ctx.setAttribute("VNF_TYPE", "testvnf");
        ctx.setAttribute("maximum", "1");
        ctx.setAttribute("COUNT(*)", "1");
        return status;
    }


    @Override
    public QueryStatus save(String resource, boolean force, boolean localOnly, String key, Map<String, String> parms,
            String prefix, SvcLogicContext ctx) throws SvcLogicException {
        return QueryStatus.SUCCESS;
    }


}
