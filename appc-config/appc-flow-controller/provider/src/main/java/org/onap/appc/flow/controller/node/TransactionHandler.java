/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 Nokia. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */
package org.onap.appc.flow.controller.node;

import static org.onap.appc.flow.controller.utils.FlowControllerConstants.INPUT_REQUEST_ACTION;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.INPUT_REQUEST_ACTION_TYPE;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.VNF_TYPE;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.REST_PROTOCOL;

import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.onap.appc.flow.controller.data.Transaction;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

/**
 * Helper class for RestServiceNode
 */
class TransactionHandler {

  Transaction buildTransaction(SvcLogicContext ctx, Properties prop, String resourceUri)
      throws Exception {

    String inputRequestAction = ctx.getAttribute(INPUT_REQUEST_ACTION);
    String inputRequestActionType = ctx.getAttribute(INPUT_REQUEST_ACTION_TYPE);
    String vnfType = ctx.getAttribute(VNF_TYPE);

    if (StringUtils.isBlank(vnfType)) {
        throw new Exception("Don't know vnf type to send REST request for  " + INPUT_REQUEST_ACTION + " - " +vnfType);
    }
    if (StringUtils.isBlank(inputRequestActionType)) {
      throw new Exception("Don't know REST operation for Action " + inputRequestActionType);
    }
    if (StringUtils.isBlank(inputRequestAction)) {
      throw new Exception("Don't know request-action " + INPUT_REQUEST_ACTION);
    }

    Transaction transaction = new Transaction();
    transaction.setExecutionEndPoint(resourceUri);
    transaction.setExecutionRPC(inputRequestActionType);
    transaction.setAction(INPUT_REQUEST_ACTION);

    String userKey = vnfType + "." + REST_PROTOCOL + "." + inputRequestAction + ".user";
    String passwordKey = vnfType + "." +  REST_PROTOCOL + "." + inputRequestAction + ".password";
    transaction.setuId(prop.getProperty(userKey));
    transaction.setPswd(prop.getProperty(passwordKey));
    if (StringUtils.isBlank(transaction.getuId()) || StringUtils.isBlank(transaction.getPswd())) {
        throw new Exception ("User Id or Password is not set !!!");
    }
    return transaction;
  }

}
