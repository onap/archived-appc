/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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
 * * ============LICENSE_END=========================================================
 */

package org.onap.appc.flow.controller.node;

import static org.onap.appc.flow.controller.utils.FlowControllerConstants.APPC_SOUTHBOUND;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.INPUT_PARAM_RESPONSE_PREFIX;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.OUTPUT_PARAM_ERROR_MESSAGE;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.OUTPUT_PARAM_STATUS;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.OUTPUT_STATUS_FAILURE;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.OUTPUT_STATUS_MESSAGE;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.OUTPUT_STATUS_SUCCESS;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.onap.appc.flow.controller.data.Transaction;
import org.onap.appc.flow.controller.executorImpl.RestExecutor;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;

public class RestServiceNode implements SvcLogicJavaPlugin {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(RestServiceNode.class);

    static final String REST_RESPONSE = "restResponse";

    private final TransactionHandler transactionHandler;
    private final RestExecutor restExecutor;
    private final ResourceUriExtractor resourceUriExtractor;
    private final EnvVariables envVariables;

    public RestServiceNode() {
        this.transactionHandler = new TransactionHandler();
        this.restExecutor = new RestExecutor();
        this.resourceUriExtractor = new ResourceUriExtractor();
        this.envVariables = new EnvVariables();
    }

    /**
     * Constructor for tests, prefer to use no arg constructor
     */
    RestServiceNode(TransactionHandler transactionHandler, RestExecutor restExecutor, ResourceUriExtractor uriExtractor,
            EnvVariables envVariables) {
        this.transactionHandler = transactionHandler;
        this.restExecutor = restExecutor;
        this.resourceUriExtractor = uriExtractor;
        this.envVariables = envVariables;
    }

    public void sendRequest(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {
        String fn = "RestServiceNode.sendRequest";
        log.info("Received processParamKeys call with params : " + inParams);
        String responsePrefix = inParams.get(INPUT_PARAM_RESPONSE_PREFIX);
        try {
            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix + ".") : "";
            // Remove below for Block
            for (String key : ctx.getAttributeKeySet()) {
                log.info(fn + "Getting Key = " + key + "and Value = " + ctx.getAttribute(key));
            }

            String resourceUri = resourceUriExtractor.extractResourceUri(ctx);

            log.info("Rest Constructed URL : " + resourceUri);

            Transaction transaction = transactionHandler.buildTransaction(ctx, resourceUri);
            Map<String, String> output = restExecutor.execute(transaction, ctx);

            String json = output.get(REST_RESPONSE);
            log.info("Received response from Interface " + json);

            JsonNode validatedJson = JsonValidator.validate(json);

            if (validatedJson != null) {
                log.info("state is " + validatedJson.findValue("state"));
                ctx.setAttribute(responsePrefix + OUTPUT_STATUS_MESSAGE, output.get(REST_RESPONSE));
            }

            ctx.setAttribute(responsePrefix + OUTPUT_PARAM_STATUS, OUTPUT_STATUS_SUCCESS);

        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + OUTPUT_PARAM_STATUS, OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
            log.error("Error Message : " + e.getMessage(), e);
            throw new SvcLogicException(e.getMessage());
        }
    }


}
