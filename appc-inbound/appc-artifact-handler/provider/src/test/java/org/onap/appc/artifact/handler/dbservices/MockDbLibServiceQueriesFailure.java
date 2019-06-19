/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 Ericsson
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
 * 
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.artifact.handler.dbservices;

import java.util.ArrayList;
import java.util.Map;

import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;
import org.onap.ccsdk.sli.adaptors.resource.sql.SqlResource;

public class MockDbLibServiceQueriesFailure extends DbLibServiceQueries {

    @Override
    public QueryStatus query(String key, SvcLogicContext ctx) {
        QueryStatus status = QueryStatus.FAILURE;
        ctx.setAttribute("keys",key);
        ctx.setAttribute("id", "testId");
        ctx.setAttribute("VNF_TYPE", "testvnf");
        ctx.setAttribute("maximum", "1");
        ctx.setAttribute("COUNT(*)", "1");
        ctx.setAttribute("download-config-dg", "TestDG");
        return status;
    }
    
    @Override
    public QueryStatus query(String key, SvcLogicContext ctx, ArrayList<String> arguments) {
        QueryStatus status = QueryStatus.FAILURE;
        ctx.setAttribute("keys",key);
        ctx.setAttribute("id", "testId");
        ctx.setAttribute("VNF_TYPE", "testvnf");
        ctx.setAttribute("maximum", "1");
        ctx.setAttribute("COUNT(*)", "1");
        ctx.setAttribute("download-config-dg", "TestDG");
        return status;
    }

    @Override
    public QueryStatus save(String key, SvcLogicContext ctx) {
        ctx.setAttribute("keys", key);
        return QueryStatus.FAILURE;
    }
    
    @Override
    public QueryStatus save(String key, SvcLogicContext ctx, ArrayList<String> arguments) {
        ctx.setAttribute("keys", key);
        return QueryStatus.FAILURE;
    }
}
