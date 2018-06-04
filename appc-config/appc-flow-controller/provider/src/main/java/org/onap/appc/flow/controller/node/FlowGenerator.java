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
 *
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.flow.controller.node;

import static org.onap.appc.flow.controller.utils.FlowControllerConstants.ACTION_LEVEL;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.PAYLOAD;
import static org.onap.appc.flow.controller.utils.FlowControllerConstants.REQUEST_ACTION;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.onap.appc.flow.controller.data.Response;
import org.onap.appc.flow.controller.data.ResponseAction;
import org.onap.appc.flow.controller.data.Transaction;
import org.onap.appc.flow.controller.data.Transactions;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

public class FlowGenerator {

  private static final EELFLogger log = EELFManager.getInstance().getLogger(FlowGenerator.class);

  public Transactions createSingleStepModel(Map<String, String> inParams, SvcLogicContext ctx) {

    log.debug("Starting generating single Step flow");
    log.debug("Data in context" + ctx.getAttributeKeySet());

    Transactions transactions = new Transactions();
    transactions.setTransactions(getTransactions(ctx));

    log.debug("FlowGenerator.createSingleStepModel Sequence String" + transactions.toString());

    return transactions;
  }

  private List<Transaction> getTransactions(SvcLogicContext ctx) {
    Transaction singleTransaction = new Transaction();
    singleTransaction.setTransactionId(1);
    singleTransaction.setAction(ctx.getAttribute(REQUEST_ACTION));
    //Need to discuss how to get action level if not in request
    singleTransaction.setPayload(ctx.getAttribute(PAYLOAD));
    singleTransaction.setActionLevel(ctx.getAttribute(ACTION_LEVEL));

    singleTransaction.setResponses(getResponses());

    List<Transaction> transactionList = new ArrayList<>();
    transactionList.add(singleTransaction);

    return transactionList;
  }

  private List<Response> getResponses() {

    ResponseAction ra = new ResponseAction();
    ra.setStop(true);

    Response response = new Response();
    response.setResponseAction(ra);

    List<Response> responseList = new ArrayList<>();
    responseList.add(response);

    return responseList;
  }
}
