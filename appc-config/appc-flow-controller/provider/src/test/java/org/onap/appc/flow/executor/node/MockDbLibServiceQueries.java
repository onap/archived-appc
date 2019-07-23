/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 IBM
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

package org.onap.appc.flow.executor.node;

import java.util.ArrayList;
import java.util.Map;

import org.onap.appc.flow.controller.dbervices.DbLibServiceQueries;
import org.onap.appc.flow.controller.utils.FlowControllerConstants;
import org.onap.ccsdk.sli.adaptors.resource.sql.SqlResource;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;

public class MockDbLibServiceQueries extends DbLibServiceQueries {

    public MockDbLibServiceQueries() {
        super(null,true);
    }

    @Override
    public QueryStatus query(String query, SvcLogicContext ctx, String prefix, ArrayList<String> arguments) {

        return QueryStatus.SUCCESS;
    }
    @Override
    public QueryStatus query(String query, SvcLogicContext ctx, ArrayList<String> arguments) {

        return QueryStatus.SUCCESS;
    }
    @Override
    public QueryStatus query(String query, String prefix, SvcLogicContext ctx) {

        return QueryStatus.SUCCESS;
    }
    @Override
    public QueryStatus query(String query, SvcLogicContext ctx) {

        return QueryStatus.SUCCESS;
    }

    @Override
    public QueryStatus save(String query, SvcLogicContext ctx, ArrayList<String> arguments) {

        return QueryStatus.SUCCESS;
    }
    @Override
    public QueryStatus save(String query, SvcLogicContext ctx) {

        return QueryStatus.SUCCESS;
    }


}
