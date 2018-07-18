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
