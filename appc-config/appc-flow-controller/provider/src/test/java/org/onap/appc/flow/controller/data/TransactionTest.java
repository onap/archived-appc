/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 Nokia Intellectual Property. All rights reserved.
 * ================================================================================
 * Modifications Copyright (C) 2018 IBM.
 * ================================================================================
 * Modifications Copyright (C) 2019 Ericsson
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
package org.onap.appc.flow.controller.data;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import org.junit.Before;
import org.junit.Test;

public class TransactionTest {

    private Transaction transaction;

    @Before
    public void setUp() {
        transaction = new Transaction();
    }

    @Test
    public void get_set_pass() {
        String somePwsd = "some_pass";
        transaction.setPswd(somePwsd);
        assertEquals(somePwsd, transaction.getPswd());
    }

    @Test
    public void get_set_precheck() {
        PreCheck precheck = mock(PreCheck.class);
        transaction.setPrecheck(precheck);
        assertEquals(precheck, transaction.getPrecheck());
    }

    @Test
    public void get_set_state() {
        String state = "some_state";
        transaction.setState(state);
        assertEquals(state, transaction.getState());
    }

    @Test
    public void get_set_status_code() {
        String statusCode = "status_code";
        transaction.setStatusCode(statusCode);
        assertEquals(statusCode, transaction.getStatusCode());
    }

    @Test
    public void get_set_transaction_id() {
        int id = 133;
        transaction.setTransactionId(id);
        assertEquals(id, transaction.getTransactionId());
    }

    @Test
    public void get_set_responses() {
        ArrayList<Response> responses = new ArrayList<>();
        responses.add(mock(Response.class));
        transaction.setResponses(responses);
        assertEquals(responses, transaction.getResponses());
    }

    @Test
    public void get_set_parameters() {
        ArrayList<Parameters> parameters = new ArrayList<>();
        parameters.add(mock(Parameters.class));
        transaction.setParameters(parameters);
        assertEquals(parameters, transaction.getParameters());
    }

    @Test
    public void get_set_payload() {
        String payload = "some_payload";
        transaction.setPayload(payload);
        assertEquals(payload, transaction.getPayload());
    }

    @Test
    public void get_set_execution_rpc() {
        String executionRPC = "some_exec_rpc";
        transaction.setExecutionRPC(executionRPC);
        assertEquals(executionRPC, transaction.getExecutionRPC());
    }

    @Test
    public void get_set_execution_module() {
        String executionModule = "some_exec_module";
        transaction.setExecutionModule(executionModule);
        assertEquals(executionModule, transaction.getExecutionModule());
    }

    @Test
    public void get_set_execution_type() {
        String executionType = "some_exec_type";
        transaction.setExecutionType(executionType);
        assertEquals(executionType, transaction.getExecutionType());
    }

    @Test
    public void get_set_execution_endpoint() {
        String executionEndpoint = "some_exec_endpoint";
        transaction.setExecutionEndPoint(executionEndpoint);
        assertEquals(executionEndpoint, transaction.getExecutionEndPoint());
    }

    @Test
    public void get_set_uid() {
        String uid = "some_uid";
        transaction.setuId(uid);
        assertEquals(uid, transaction.getuId());
    }

    @Test
    public void get_set_action_level() {
        String actionLevel = "some_action_level";
        transaction.setActionLevel(actionLevel);
        assertEquals(actionLevel, transaction.getActionLevel());
    }

    @Test
    public void get_set_action_identifier() {
        ActionIdentifier actionIdentifier = mock(ActionIdentifier.class);
        transaction.setActionIdentifier(actionIdentifier);
        assertEquals(actionIdentifier, transaction.getActionIdentifier());
    }

    @Test
    public void get_set_action() {
        String action = "some_action";
        transaction.setAction(action);
        assertEquals(action, transaction.getAction());
    }

    @Test
    public void get_set_status() {
        String status = "some_status";
        transaction.setStatus(status);
        assertEquals(status, transaction.getStatus());
    }

    @Test
    public void to_string() {

        ActionIdentifier actionIdentifier = mock(ActionIdentifier.class);
        when(actionIdentifier.toString()).thenReturn("some_action_identifier");

        PreCheck precheck = mock(PreCheck.class);
        when(precheck.toString()).thenReturn("some_precheck");

        Response response = mock(Response.class);
        when(response.toString()).thenReturn("some_response");
        ArrayList<Response> responses = new ArrayList<>();
        responses.add(response);

        Parameters parameters = mock(Parameters.class);
        when(parameters.toString()).thenReturn("some_parameters");
        ArrayList<Parameters> parametersList = new ArrayList<>();
        parametersList.add(parameters);

        transaction.setAction("some_action");
        transaction.setActionIdentifier(actionIdentifier);
        transaction.setActionLevel("some_action_level");
        transaction.setExecutionRPC("some_execution_rpc");
        transaction.setExecutionType("some_execution_type");
        transaction.setExecutionModule("some_execution_module");
        transaction.setExecutionEndPoint("some_execution_endpoint");
        transaction.setState("some_state");
        transaction.setStatus("some_status");
        transaction.setStatusCode("some_status_code");
        transaction.setPswd("some_pass");
        transaction.setPayload("some_payload");
        transaction.setPrecheck(precheck);
        transaction.setParameters(parametersList);
        transaction.setResponses(responses);
        transaction.setTransactionId(133);
        transaction.setuId("some_uid");

        assertEquals(
            "Transaction [transactionId=133, action=some_action, actionLevel=some_action_level, actionIdentifier=some_action_identifier, parameters=[some_parameters], executionType=some_execution_type, uId=some_uid, statusCode=some_status_code, pswd=some_pass, executionEndPoint=some_execution_endpoint, executionModule=some_execution_module, executionRPC=some_execution_rpc, state=some_state, precheck=some_precheck, payload=some_payload, responses=[some_response], status=some_status]",
            transaction.toString());
    }

    @Test
    public void equals() {
        ActionIdentifier actionIdentifier = mock(ActionIdentifier.class);
        PreCheck precheck = mock(PreCheck.class);
        ArrayList<Response> responses = new ArrayList<>();
        responses.add(new Response());
        Parameters parameters = mock(Parameters.class);
        ArrayList<Parameters> parametersList = new ArrayList<>();
        parametersList.add(parameters);
        assertTrue(transaction.equals(transaction));
        assertFalse(transaction.equals(null));
        assertFalse(transaction.equals(""));
        Transaction other = new Transaction();
        other.setAction("different_action");
        assertFalse(transaction.equals(other));
        transaction.setAction("action");
        assertFalse(transaction.equals(other));
        other.setAction("action");
        other.setActionIdentifier(actionIdentifier);
        assertFalse(transaction.equals(other));
        transaction.setActionIdentifier(new ActionIdentifier());
        assertFalse(transaction.equals(other));
        transaction.setActionIdentifier(actionIdentifier);
        other.setActionLevel("different_action_level");
        assertFalse(transaction.equals(other));
        transaction.setActionLevel("some_action_level");
        assertFalse(transaction.equals(other));
        other.setActionLevel("some_action_level");
        other.setExecutionEndPoint("different_execution_endpoint");
        assertFalse(transaction.equals(other));
        transaction.setExecutionEndPoint("some_execution_endpoint");
        assertFalse(transaction.equals(other));
        other.setExecutionEndPoint("some_execution_endpoint");

        other.setExecutionModule("different_execution_module");
        assertFalse(transaction.equals(other));
        transaction.setExecutionModule("some_execution_module");
        assertFalse(transaction.equals(other));
        other.setExecutionModule("some_execution_module");

        other.setExecutionRPC("different_execution_rpc");
        assertFalse(transaction.equals(other));
        transaction.setExecutionRPC("some_execution_rpc");
        assertFalse(transaction.equals(other));
        other.setExecutionRPC("some_execution_rpc");

        other.setExecutionType("different_execution_type");
        assertFalse(transaction.equals(other));
        transaction.setExecutionType("some_execution_type");
        assertFalse(transaction.equals(other));
        other.setExecutionType("some_execution_type");
        
        other.setParameters(new ArrayList<Parameters>(0));
        assertFalse(transaction.equals(other));
        transaction.setParameters(parametersList);
        assertFalse(transaction.equals(other));
        other.setParameters(parametersList);
        
        other.setPayload("different_payload");
        assertFalse(transaction.equals(other));
        transaction.setPayload("some_payload");
        assertFalse(transaction.equals(other));
        other.setPayload("some_payload");
        
        other.setPrecheck(new PreCheck());
        assertFalse(transaction.equals(other));
        transaction.setPrecheck(precheck);
        assertFalse(transaction.equals(other));
        other.setPrecheck(precheck);
        
        other.setPswd("different_pass");
        assertFalse(transaction.equals(other));
        transaction.setPswd("some_pass");
        assertFalse(transaction.equals(other));
        other.setPswd("some_pass");
        
        other.setPswd("different_pass");
        assertFalse(transaction.equals(other));
        transaction.setPswd("some_pass");
        assertFalse(transaction.equals(other));
        other.setPswd("some_pass");
        
        other.setResponses(new ArrayList<Response>(0));
        assertFalse(transaction.equals(other));
        transaction.setResponses(responses);
        assertFalse(transaction.equals(other));
        other.setResponses(responses);
        
        other.setState("different_state");
        assertFalse(transaction.equals(other));
        transaction.setState("some_state");
        assertFalse(transaction.equals(other));
        other.setState("some_state");
        
        other.setStatus("different_status");
        assertFalse(transaction.equals(other));
        transaction.setStatus("some_status");
        assertFalse(transaction.equals(other));
        other.setStatus("some_status");
        
        other.setStatusCode("different_status_code");
        assertFalse(transaction.equals(other));
        transaction.setStatusCode("some_status_code");
        assertFalse(transaction.equals(other));
        other.setStatusCode("some_status_code");
        
        other.setTransactionId(0);
        transaction.setTransactionId(1);
        assertFalse(transaction.equals(other));
        other.setTransactionId(1);
        other.setuId("different_uid");
        assertFalse(transaction.equals(other));
        transaction.setuId("some_uid");
        assertFalse(transaction.equals(other));
        other.setuId("some_uid");
        assertTrue(transaction.equals(other));
    }
    
    @Test
    public void testHashCode()
    {
        transaction.setAction("some_action");
        transaction.setActionLevel("some_action_level");
        transaction.setExecutionRPC("some_execution_rpc");
        transaction.setExecutionType("some_execution_type");
        transaction.setExecutionModule("some_execution_module");
        transaction.setExecutionEndPoint("some_execution_endpoint");
        transaction.setState("some_state");
        transaction.setStatus("some_status");
        transaction.setStatusCode("some_status_code");
        transaction.setPswd("some_pass");
        transaction.setPayload("some_payload");
        transaction.setTransactionId(133);
        transaction.setuId("some_uid");
        int hashcode= transaction.hashCode();
        assertEquals(-955260883,hashcode);
    }

}