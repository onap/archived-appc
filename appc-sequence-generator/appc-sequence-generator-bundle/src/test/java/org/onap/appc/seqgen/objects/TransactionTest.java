/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 IBM.
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
 * ============LICENSE_END=========================================================
 */
package org.onap.appc.seqgen.objects;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TransactionTest {

    private Transaction transaction;

    @Before
    public void setUp() {
        transaction = new Transaction();
    }

    @Test
    public void get_set_transaction_id() {
        Integer id = 133;
        transaction.setTransactionId(id);
        Assert.assertEquals(id, transaction.getTransactionId());
    }

    @Test
    public void get_set_action() {
        String action = "some_action";
        transaction.setAction(action);
        Assert.assertEquals(action, transaction.getAction());
    }

    @Test
    public void get_set_action_level() {
        String actionLevel = "some_action_level";
        transaction.setActionLevel(actionLevel);
        Assert.assertEquals(actionLevel, transaction.getActionLevel());
    }

    @Test
    public void get_set_action_identifier() {
        ActionIdentifier actionIdentifier = mock(ActionIdentifier.class);
        transaction.setActionIdentifier(actionIdentifier);
        Assert.assertEquals(actionIdentifier, transaction.getActionIdentifier());
    }

    @Test
    public void get_set_payload() {
        String payload = "some_payload";
        transaction.setPayload(payload);
        Assert.assertEquals(payload, transaction.getPayload());
    }

    	@Test
    public void get_setPreCheckOperator() {
        String preCheckOperator = "some_preCheckOperator";
        transaction.setPreCheckOperator(preCheckOperator);
        Assert.assertEquals(preCheckOperator, transaction.getPreCheckOperator());
    }

    @Test
    public void get_set_responses() {
        ArrayList<Response> responses = new ArrayList<>();
        responses.add(mock(Response.class));
        transaction.setResponses(responses);
        Assert.assertEquals(responses, transaction.getResponses());
    }

    @Test
    public void get_set_precheckOptions() {
        ArrayList<PreCheckOption> precheckOptions = new ArrayList<>();
        precheckOptions.add(mock(PreCheckOption.class));
        transaction.setPrecheckOptions(precheckOptions);
        Assert.assertEquals(precheckOptions, transaction.getPrecheckOptions());
    }

}