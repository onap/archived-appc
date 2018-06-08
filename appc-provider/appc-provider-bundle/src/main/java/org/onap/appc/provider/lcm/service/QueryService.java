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

package org.onap.appc.provider.lcm.service;

import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.Action;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.QueryInput;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.QueryOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.appc.lcm.rev160108.query.output.QueryResults;
import org.onap.appc.requesthandler.objects.RequestHandlerInput;
import org.onap.appc.requesthandler.objects.RequestHandlerOutput;

import java.util.List;

/**
 * Provide LCM command service for Query of VNF state/status
 */
public class QueryService extends AbstractBaseService {
    private List<QueryResults> queryResultList;

    /**
     * Constructor
     */
    public QueryService() {
        super(Action.Query);
        logger.debug("QueryService starts");
    }

    /**
     * Process the input for the query service
     * @param input of QueryInput from the REST API input
     * @return QueryOutputBuilder which has the query results
     */
    public QueryOutputBuilder process(QueryInput input) {
        validate(input);
        if (status == null) {
            proceedAction(input);
        }

        QueryOutputBuilder outputBuilder = new QueryOutputBuilder();
        outputBuilder.setStatus(status);
        outputBuilder.setQueryResults(queryResultList);
        outputBuilder.setCommonHeader(input.getCommonHeader());
        return outputBuilder;
    }

    /**
     * Validate the input.
     * Set Status if any error occurs.
     *
     * @param input of QueryInput from the REST API input
     */
    void validate(QueryInput input) {
        status = validateVnfId(input.getCommonHeader(), input.getAction(), input.getActionIdentifiers());
    }

    /**
     * Proceed to action for the query service.
     *
     * @param input of QueryInput from the REST API input
     */
    void proceedAction(QueryInput input) {
        RequestHandlerInput requestHandlerInput = getRequestHandlerInput(
            input.getCommonHeader(), input.getActionIdentifiers(), null, this.getClass().getName());
        if (requestHandlerInput != null) {
            RequestHandlerOutput requestHandlerOutput = executeAction(requestHandlerInput);
            queryResultList = (List<QueryResults>) requestHandlerOutput.getResponseContext().getPayloadObject();
        }
    }
}
