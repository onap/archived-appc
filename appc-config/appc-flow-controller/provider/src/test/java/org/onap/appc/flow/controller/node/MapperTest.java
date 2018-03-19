/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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
 *  * ============LICENSE_END=========================================================
 */

package org.onap.appc.flow.controller.node;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.onap.appc.flow.controller.data.Transaction;
import org.onap.appc.flow.controller.data.Transactions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/*
 * Adding this test case to ensure that JSON mapping works as expected.
 * Add or modify test case if changes are made to class attributes.
 *
*/
public class MapperTest {

    @Test
    public void testsIfJsonGenerationisValid() throws JsonProcessingException {

        Transaction t = new Transaction();
        Transactions transactions = new Transactions();
        t.setAction("testAction");
        t.setTransactionId(100);
        t.setActionLevel("testActionLevel");
        t.setExecutionRPC("testMethod");
        List<Transaction> tList = new ArrayList<Transaction>();
        tList.add(t);
        transactions.setTransactions(tList);
        Transactions trans = transactions;
        ObjectMapper mapper = new ObjectMapper();
        String flowSequence = mapper.writeValueAsString(trans);
        String compareString = "{\"transactions\":[{\"executionType\":null,\"uId\":null,\"statusCode\":null,\"pswd\":null,"
                + "\"executionEndPoint\":null,\"executionModule\":null,\"executionRPC\":\"testMethod\","
                + "\"status\":\"PENDING\",\"transaction-id\":100,\"action\":\"testAction\","
                + "\"action-level\":\"testActionLevel\",\"action-identifier\":null,"
                + "\"parameters\":null,\"state\":null,\"precheck\":null,\"payload\":null,\"responses\":null}]}";
        assertEquals(flowSequence, compareString);

    }

}
