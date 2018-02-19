package org.onap.appc.flow.controller.node;

import static org.onap.appc.flow.controller.utils.FlowControllerConstants.INPUT_REQUEST_ACTION;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.INPUT_REQUEST_ACTION_TYPE;

import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.onap.appc.flow.controller.data.Transaction;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

/**
 * Helper class for RestServiceNode
 */
class TransactionHandler {

  private TransactionHandler() {}

  static Transaction buildTransaction(SvcLogicContext ctx, Properties prop,
      String resourceUri) throws Exception {

    String inputRequestAction = ctx.getAttribute(INPUT_REQUEST_ACTION);
    String inputRequestActionType = ctx.getAttribute(INPUT_REQUEST_ACTION_TYPE);

    if (StringUtils.isBlank(inputRequestActionType)) {
      throw new IllegalArgumentException(
          "Don't know REST operation for Action " + inputRequestActionType);
    }
    if (StringUtils.isBlank(inputRequestAction)) {
      throw new IllegalArgumentException("Don't know request-action " + INPUT_REQUEST_ACTION);
    }

    Transaction transaction = new Transaction();
    transaction.setExecutionEndPoint(resourceUri);
    transaction.setExecutionRPC(inputRequestActionType);
    transaction.setAction(INPUT_REQUEST_ACTION);

    //This code need to get changed to get the UserID and pass from a common place.
    transaction.setuId(prop.getProperty(inputRequestAction.concat(".default-rest-user")));
    transaction.setPswd(prop.getProperty(inputRequestAction.concat(".default-rest-pass")));

    return transaction;
  }

}