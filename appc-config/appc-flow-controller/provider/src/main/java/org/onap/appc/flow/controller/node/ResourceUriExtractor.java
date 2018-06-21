/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 Nokia. All rights reserved.
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
 * =============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.appc.flow.controller.node;

import static org.onap.appc.flow.controller.utils.FlowControllerConstants.HTTP;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.INPUT_CONTEXT;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.INPUT_HOST_IP_ADDRESS;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.INPUT_REQUEST_ACTION;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.INPUT_URL;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.REST_PORT;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.REST_CONTEXT_URL;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.apache.commons.lang.StringUtils;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

/**
 * Helper class for RestServiceNode
 */
class ResourceUriExtractor {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(RestServiceNode.class);

    String extractResourceUri(SvcLogicContext ctx) throws Exception {
        String resourceUri = ctx.getAttribute(INPUT_URL);

        if (StringUtils.isBlank(resourceUri)) {
            resourceUri = getAddress(ctx);
            log.info("resourceUri= " + resourceUri);
            resourceUri += getContext(ctx);
            log.info("resourceUri= " + resourceUri);

        }
        log.info("resourceUri= " + resourceUri);

        return resourceUri;
    }

    private String getAddress(SvcLogicContext ctx) {
        String address = ctx.getAttribute(INPUT_HOST_IP_ADDRESS);
        String port = ctx.getAttribute(REST_PORT);
        return HTTP + address + ":" + port;
    }

    private String getContext(SvcLogicContext ctx) throws Exception {
        String context;
        if (StringUtils.isNotBlank(ctx.getAttribute(INPUT_CONTEXT))) {
            context = "/" + ctx.getAttribute(INPUT_CONTEXT);
        } else if (ctx.getAttribute(REST_CONTEXT_URL) != null) {
            context = "/" + ctx.getAttribute(REST_CONTEXT_URL);
        } else {
            throw new Exception("Could not find the context for operation " + ctx.getAttribute(INPUT_REQUEST_ACTION));
        }
        return context;
    }

}
