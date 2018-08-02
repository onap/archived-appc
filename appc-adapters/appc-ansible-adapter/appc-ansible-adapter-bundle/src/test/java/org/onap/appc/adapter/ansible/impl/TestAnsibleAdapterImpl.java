/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
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

package org.onap.appc.adapter.ansible.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.appc.adapter.ansible.model.AnsibleMessageParser;
import org.onap.appc.adapter.ansible.model.AnsibleResult;
import org.onap.appc.configuration.Configuration;
import org.onap.appc.configuration.ConfigurationFactory;
import org.onap.appc.exceptions.APPCException;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.powermock.reflect.Whitebox;

@RunWith(MockitoJUnitRunner.class)
public class TestAnsibleAdapterImpl {

    private static String KEYSTORE_PASSWORD;
    private static Configuration configuration;
    private static final String RESULT_CODE_ATTRIBUTE_NAME = "org.onap.appc.adapter.ansible.result.code";
    private static final String LOG_ATTRIBUTE_NAME = "org.onap.appc.adapter.ansible.log";
    private static final String STATUS_CODE = "StatusCode";
    private static final String STATUS_MESSAGE = "StatusMessage";
    private static final String PENDING = "100";
    private static final String SUCCESS = "200";

    private AnsibleAdapterImpl adapter;
    private boolean testMode = true;
    private Map<String, String> params;
    private SvcLogicContext svcContext;
    private JSONObject jsonPayload;
    private AnsibleResult result;
    private String agentUrl = "https://192.168.1.1";
    private AnsibleAdapterImpl spyAdapter;

    @Mock
    private AnsibleMessageParser messageProcessor;

    @Mock
    private ConnectionBuilder httpClient;

    /**
     * Load the configuration properties
     */
    @BeforeClass
    public static void once() {
        configuration = ConfigurationFactory.getConfiguration();
        KEYSTORE_PASSWORD = configuration.getProperty("org.onap.appc.adapter.ansible.trustStore.trustPasswd");

    }

    /**
     * Use reflection to locate fields and methods so that they can be manipulated during the test
     * to change the internal state accordingly.
     *
     */
    @Before
    public void setup() {
        testMode = true;
        svcContext = new SvcLogicContext();
        adapter = new AnsibleAdapterImpl(testMode);
        params = new HashMap<>();
        params.put("AgentUrl", agentUrl);
        jsonPayload = new JSONObject();
        jsonPayload.put("Id", "100");
        jsonPayload.put("User", "test");
        jsonPayload.put("Password", "test");
        jsonPayload.put("PlaybookName", "test_playbook.yaml");
        jsonPayload.put("AgentUrl", agentUrl);
        result = new AnsibleResult();
        result.setStatusMessage("Success");
        result.setResults("Success");
        Whitebox.setInternalState(adapter, "messageProcessor", messageProcessor);
        spyAdapter = Mockito.spy(adapter);
    }

    @After
    public void tearDown() {
        testMode = false;
        adapter = null;
        params = null;
        svcContext = null;
    }

    /**
     * This test case is used to test the request is submitted and the status is marked to pending
     *
     * @throws SvcLogicException If the request cannot be process due to Number format or JSON
     *         Exception
     * @throws APPCException If the request cannot be processed for some reason
     */
    @Test
    public void reqExec_shouldSetPending() throws SvcLogicException, APPCException {
        result.setStatusCode(Integer.valueOf(PENDING));
        when(messageProcessor.reqMessage(params)).thenReturn(jsonPayload);
        when(messageProcessor.parsePostResponse(anyString())).thenReturn(result);
        spyAdapter.reqExec(params, svcContext);
        assertEquals(PENDING, svcContext.getAttribute(RESULT_CODE_ATTRIBUTE_NAME));
    }

    /**
     * This test case is used to test the request is process and the status is marked to success
     *
     * @throws SvcLogicException If the request cannot be process due to Number format or JSON
     *         Exception
     * @throws APPCException If the request cannot be processed for some reason
     */
    @Test
    public void reqExecResult_shouldSetSuccess() throws SvcLogicException, APPCException {
        params.put("Id", "100");
        result.setStatusCode(Integer.valueOf(SUCCESS));
        when(messageProcessor.reqUriResult(params)).thenReturn(agentUrl);
        when(messageProcessor.parseGetResponse(anyString())).thenReturn(result);
        spyAdapter.reqExecResult(params, svcContext);
        assertEquals("400", svcContext.getAttribute(RESULT_CODE_ATTRIBUTE_NAME));
    }

    /**
     * This test case is used to test the Failure of the request
     *
     * @throws SvcLogicException If the request cannot be process due to Number format or JSON
     *         Exception
     * @throws APPCException If the request cannot be processed for some reason
     */
    @Test(expected = SvcLogicException.class)
    public void reqExecResult_Failure() throws SvcLogicException, APPCException {
        params.put("Id", "100");
        result.setStatusCode(100);
        result.setStatusMessage("Failed");
        when(messageProcessor.reqUriResult(params)).thenReturn(agentUrl);
        when(messageProcessor.parseGetResponse(anyString())).thenReturn(result);
        adapter.reqExecResult(params, svcContext);
    }

    /**
     * This test case is used to test the APPC Exception
     *
     * @throws SvcLogicException If the request cannot be process due to Number format or JSON
     *         Exception
     * @throws APPCException If the request cannot be processed for some reason
     */
    @Test(expected = SvcLogicException.class)
    public void reqExecResult_appcException() throws APPCException, SvcLogicException {
        when(messageProcessor.reqUriResult(params)).thenThrow(new APPCException());
        adapter.reqExecResult(params, svcContext);
    }

    /**
     * This test case is used to test the Number Format Exception
     *
     * @throws SvcLogicException If the request cannot be process due to Number format or JSON
     *         Exception
     * @throws APPCException If the request cannot be processed for some reason
     */
    @Test(expected = SvcLogicException.class)
    public void reqExecResult_numberFormatException()
            throws IllegalStateException, IllegalArgumentException, APPCException, SvcLogicException {
        when(messageProcessor.reqUriResult(params)).thenThrow(new NumberFormatException());
        adapter.reqExecResult(params, svcContext);
    }

    /**
     * This test case is used to test the logs executed for the specific request
     *
     * @throws SvcLogicException If the request cannot be process due to Number format or JSON
     *         Exception
     * @throws APPCException If the request cannot be processed for some reason
     */
    @Test
    public void reqExecLog_shouldSetMessage() throws SvcLogicException, APPCException {
        params.put("Id", "101");
        when(messageProcessor.reqUriLog(params)).thenReturn(agentUrl);
        adapter.reqExecLog(params, svcContext);
        String message = getResponseMessage();
        assertEquals(message, svcContext.getAttribute(LOG_ATTRIBUTE_NAME));
    }

    private String getResponseMessage() {
        JSONObject response = new JSONObject();
        response.put(STATUS_CODE, 200);
        response.put(STATUS_MESSAGE, "FINISHED");
        JSONObject results = new JSONObject();

        JSONObject vmResults = new JSONObject();
        vmResults.put(STATUS_CODE, 200);
        vmResults.put(STATUS_MESSAGE, "SUCCESS");
        vmResults.put("Id", "");
        results.put("192.168.1.10", vmResults);

        response.put("Results", results);
        String message = response.toString();
        return message;
    }

    /**
     * This test case is used to test the APPC Exception
     *
     * @throws SvcLogicException If the request cannot be process due to Number format or JSON
     *         Exception
     * @throws APPCException If the request cannot be processed for some reason
     */
    @Test(expected = SvcLogicException.class)
    public void reqExecException()
            throws IllegalStateException, IllegalArgumentException, APPCException, SvcLogicException {
        when(messageProcessor.reqUriLog(params)).thenThrow(new APPCException("Appc Exception"));
        adapter.reqExecLog(params, svcContext);
    }

    /**
     * This test case is used to test the APPC Exception
     *
     * @throws SvcLogicException If the request cannot be process due to Number format or JSON
     *         Exception
     * @throws APPCException If the request cannot be processed for some reason
     */
    @Test(expected = SvcLogicException.class)
    public void reqExec_AppcException()
            throws IllegalStateException, IllegalArgumentException, SvcLogicException, APPCException {
        when(messageProcessor.reqMessage(params)).thenThrow(new APPCException());
        adapter.reqExec(params, svcContext);
    }

    /**
     * This test case is used to test the JSON Exception
     *
     * @throws SvcLogicException If the request cannot be process due to Number format or JSON
     *         Exception
     * @throws APPCException If the request cannot be processed for some reason
     */
    @Test(expected = SvcLogicException.class)
    public void reqExec_JsonException()
            throws IllegalStateException, IllegalArgumentException, SvcLogicException, APPCException {
        when(messageProcessor.reqMessage(params)).thenThrow(new JSONException("Json Exception"));
        adapter.reqExec(params, svcContext);
    }

    /**
     * This test case is used to test the Number Format Exception
     *
     * @throws SvcLogicException If the request cannot be process due to Number format or JSON
     *         Exception
     * @throws APPCException If the request cannot be processed for some reason
     */
    @Test(expected = SvcLogicException.class)
    public void reqExec_NumberFormatException()
            throws IllegalStateException, IllegalArgumentException, SvcLogicException, APPCException {
        when(messageProcessor.reqMessage(params)).thenThrow(new NumberFormatException("Numbre Format Exception"));
        adapter.reqExec(params, svcContext);
    }

    /**
     * This test case is used to test the constructor with no client type
     *
     */
    @Test
    public void testInitializeWithDefault() {
        configuration.setProperty("org.onap.appc.adapter.ansible.clientType", "");
        adapter = new AnsibleAdapterImpl();
        assertNotNull(adapter);
    }

    /**
     * This test case is used to test the constructor with client type as TRUST_ALL
     *
     */
    @Test
    public void testInitializeWithTrustAll() {
        configuration.setProperty("org.onap.appc.adapter.ansible.clientType", "TRUST_ALL");
        adapter = new AnsibleAdapterImpl();
        assertNotNull(adapter);
    }

    /**
     * This test case is used to test the constructor with client type as TRUST_CERT
     *
     */
    @Test
    public void testInitializeWithTrustCert() {
        configuration.setProperty("org.onap.appc.adapter.ansible.clientType", "TRUST_CERT");
        configuration.setProperty("org.onap.appc.adapter.ansible.trustStore.trustPasswd", KEYSTORE_PASSWORD);
        adapter = new AnsibleAdapterImpl();
        assertNotNull(adapter);
    }

    /**
     * This test case is used to test the constructor with exception
     *
     */
    @Test
    public void testInitializeWithException() {
        configuration.setProperty("org.onap.appc.adapter.ansible.clientType", "TRUST_CERT");
        configuration.setProperty("org.onap.appc.adapter.ansible.trustStore.trustPasswd", "appc");
        adapter = new AnsibleAdapterImpl();
    }

}
