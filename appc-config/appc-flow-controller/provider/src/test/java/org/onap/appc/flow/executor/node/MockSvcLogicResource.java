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

import java.util.Map;

import org.onap.appc.flow.controller.utils.FlowControllerConstants;
import org.onap.ccsdk.sli.adaptors.resource.sql.SqlResource;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;

public class MockSvcLogicResource extends SqlResource {

    @Override
    public QueryStatus query(String resource, boolean localOnly, String select, String key, String prefix,
            String orderBy, SvcLogicContext ctx) throws SvcLogicException {
        ctx.setAttribute("artifact-content", "TestArtifactContent");
        ctx.setAttribute(FlowControllerConstants.EXECUTION_TYPE,"TestRPC");
        ctx.setAttribute(FlowControllerConstants.EXECUTTION_MODULE,"TestModule");
        ctx.setAttribute(FlowControllerConstants.EXECUTION_RPC,"TestRPC");
        ctx.setAttribute("count(protocol)", "1");
        ctx.setAttribute("protocol", "TestProtocol");
        ctx.setAttribute("SEQUENCE_TYPE", "TestSequence");
        return QueryStatus.SUCCESS;
    }


    @Override
    public QueryStatus save(String resource, boolean force, boolean localOnly, String key, Map<String, String> parms,
            String prefix, SvcLogicContext ctx) throws SvcLogicException {
        return QueryStatus.SUCCESS;
    }
}
