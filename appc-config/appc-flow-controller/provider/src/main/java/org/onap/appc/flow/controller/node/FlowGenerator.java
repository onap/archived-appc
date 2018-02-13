/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.flow.controller.node;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.onap.appc.flow.controller.data.Response;
import org.onap.appc.flow.controller.data.ResponseAction;
import org.onap.appc.flow.controller.data.Transaction;
import org.onap.appc.flow.controller.data.Transactions;
import org.onap.appc.flow.controller.utils.FlowControllerConstants;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

public class FlowGenerator {
    
    private static final  EELFLogger log = EELFManager.getInstance().getLogger(FlowGenerator.class);

    public Transactions createSingleStepModel(Map<String, String> inParams, SvcLogicContext ctx) {

        log.debug("Starting generating single Step flow" );
        log.debug("Data in context"  + ctx.getAttributeKeySet() );

        Transaction singleTransaction = new Transaction();
        singleTransaction.setTransactionId(1);
        singleTransaction.setAction(ctx.getAttribute(FlowControllerConstants.REQUEST_ACTION));
        //Need to discuss how to get action level if not in request
        singleTransaction.setActionLevel(FlowControllerConstants.VNF);
        singleTransaction.setPayload(ctx.getAttribute(FlowControllerConstants.PAYLOAD));
        singleTransaction.setActionLevel(ctx.getAttribute(FlowControllerConstants.ACTION_LEVEL));

        List<Response> responseList  = new ArrayList<>();
        Response response = new Response();
                
        ResponseAction ra = new ResponseAction();                    
        ra.setStop(true);
        response.setResponseAction(ra);
        
        responseList.add(response);
        singleTransaction.setResponses(responseList);

        List<Transaction> transactionList = new ArrayList<>();
        transactionList.add(singleTransaction);

        Transactions transactions = new Transactions();
        transactions.setTransactions(transactionList);

        log.debug("FlowGenerator.createSingleStepModel Sequence String" + transactions.toString());
        
        return transactions;
    }
}
