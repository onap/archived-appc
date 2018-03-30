/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 Nokia Intellectual Property. All rights reserved.
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
package org.onap.appc.flow.controller.data;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TransactionsTest {

    private Transactions transactions;

    @Before
    public void setUp() {
        transactions = new Transactions();
    }

    @Test
    public void get_set_transactions() {
        List<Transaction> transactionsList = new ArrayList<>();
        Transaction transaction = mock(Transaction.class);
        transactionsList.add(transaction);

        transactions.setTransactions(transactionsList);

        Assert.assertEquals(transactionsList, this.transactions.getTransactions());
    }

    @Test
    public void to_string() {
        Transaction mock = mock(Transaction.class);
        when(mock.toString()).thenReturn("some_transactions");

        List<Transaction> transactionsList = new ArrayList<>();
        transactionsList.add(mock);

        transactions.setTransactions(transactionsList);

        Assert.assertEquals("Transactions [transactions=[some_transactions]]",
            transactions.toString());
    }

}