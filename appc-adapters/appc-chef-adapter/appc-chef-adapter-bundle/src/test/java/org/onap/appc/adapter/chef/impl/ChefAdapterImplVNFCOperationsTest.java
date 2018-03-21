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
package org.onap.appc.adapter.chef.impl;


import static com.google.common.collect.Maps.immutableEntry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.appc.adapter.chef.chefclient.ChefApiClientFactory;
import org.onap.appc.adapter.chef.chefclient.api.ChefApiClient;
import org.onap.appc.adapter.chef.chefclient.api.ChefResponse;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

@RunWith(MockitoJUnitRunner.class)
public class ChefAdapterImplVNFCOperationsTest {

    private static final String CLIENT_PRIVATE_KEY_PATH = "/opt/onap/appc/chef/localhost/onap/testclient.pem";
    private static final String RESULT_CODE_ATTR_KEY = "chefServerResult.code";
    private static final String RESULT_MESSAGE_ATTR_KEY = "chefServerResult.message";
    private static final String USERNAME = "testclient";
    private static final String SERVER_ADDRESS = "localhost";
    private static final String ORGANIZATIONS = "onap";
    private static final String ENV_PARAM_KEY = "Environment";
    private static final String ENV_JSON_VALUE = "{name:envName}";
    private static final String FAILURE_STATUS = "failure";
    private static final String SUCCESS_STATUS = "success";
    private static final String CHEF_ADAPTER_ERROR_PREFIX = "Chef Adapter error:";

    @Mock
    private PrivateKeyChecker privateKeyChecker;
    @Mock
    private ChefApiClientFactory chefApiClientFactory;
    @Mock
    private ChefApiClient chefApiClient;

    @InjectMocks
    private ChefAdapterFactory chefAdapterFactory;
    private SvcLogicContext svcLogicContext;

    @Before
    public void setUp() {
        svcLogicContext = new SvcLogicContext();
    }

    @Test
    public void vnfcEnvironment_shouldSkipEnvironmentCreation_whenEnvParamIsEmpty() throws SvcLogicException {
        // GIVEN
        Map<String, String> params = givenInputParams(immutableEntry(ENV_PARAM_KEY, ""));

        // WHEN
        chefAdapterFactory.create().vnfcEnvironment(params, svcLogicContext);

        // THEN
        assertThat(svcLogicContext.getStatus()).isEqualTo(SUCCESS_STATUS);
        assertThat(svcLogicContext.getAttribute(RESULT_CODE_ATTR_KEY))
            .isEqualTo(Integer.toString(HttpStatus.SC_OK));
        assertThat(svcLogicContext.getAttribute(RESULT_MESSAGE_ATTR_KEY)).isEqualTo("Skip Environment block ");
    }

    @Test
    public void vnfcEnvironment_shouldCreateNewEnvironment_forEnvParam_whenRequestedEnvDoesNotExist()
        throws SvcLogicException {
        // GIVEN
        String expectedErrorMessage = "New Environment Created";
        Map<String, String> params = givenInputParams(immutableEntry(ENV_PARAM_KEY, ENV_JSON_VALUE));
        given(privateKeyChecker.doesExist(CLIENT_PRIVATE_KEY_PATH)).willReturn(true);
        given(chefApiClientFactory.create("https://localhost/organizations/onap", ORGANIZATIONS, USERNAME,
            CLIENT_PRIVATE_KEY_PATH)).willReturn(chefApiClient);
        given(chefApiClient.put("/environments/" + "envName", ENV_JSON_VALUE))
            .willReturn(ChefResponse.create(HttpStatus.SC_NOT_FOUND, ""));
        given(chefApiClient.post("/environments", ENV_JSON_VALUE))
            .willReturn(ChefResponse.create(HttpStatus.SC_CREATED, expectedErrorMessage));

        // WHEN
        chefAdapterFactory.create().vnfcEnvironment(params, svcLogicContext);

        // THEN
        assertThat(svcLogicContext.getStatus()).isEqualTo(SUCCESS_STATUS);
        assertThat(svcLogicContext.getAttribute(RESULT_CODE_ATTR_KEY))
            .isEqualTo(Integer.toString(HttpStatus.SC_CREATED));
        assertThat(svcLogicContext.getAttribute(RESULT_MESSAGE_ATTR_KEY)).isEqualTo(expectedErrorMessage);
    }

    @Test
    public void vnfcEnvironment_shouldNotAttemptEnvCreation_andThrowException_whenPrivateKeyCheckFails() {
        // GIVEN
        String expectedErrorMsg = "Cannot find the private key in the APPC file system, please load the private key to ";
        Map<String, String> params = givenInputParams(immutableEntry(ENV_PARAM_KEY, ENV_JSON_VALUE));
        given(privateKeyChecker.doesExist(CLIENT_PRIVATE_KEY_PATH)).willReturn(false);

        // WHEN  // THEN
        assertThatExceptionOfType(SvcLogicException.class)
            .isThrownBy(() -> chefAdapterFactory.create().vnfcEnvironment(params, svcLogicContext))
            .withMessage(CHEF_ADAPTER_ERROR_PREFIX + expectedErrorMsg + CLIENT_PRIVATE_KEY_PATH);

        assertThat(svcLogicContext.getStatus()).isEqualTo(FAILURE_STATUS);
        assertThat(svcLogicContext.getAttribute(RESULT_CODE_ATTR_KEY))
            .isEqualTo(Integer.toString(HttpStatus.SC_INTERNAL_SERVER_ERROR));
        assertThat(svcLogicContext.getAttribute(RESULT_MESSAGE_ATTR_KEY))
            .isEqualTo(expectedErrorMsg + CLIENT_PRIVATE_KEY_PATH);
    }

    @Test
    public void vnfcEnvironment_shouldNotAttemptEnvCreation_andHandleJSONException_whenJSONParamsAreMalformed() {
        // GIVEN
        String expectedErrorMessage = "Error posting request due to invalid JSON block: ";
        Map<String, String> params = givenInputParams(immutableEntry(ENV_PARAM_KEY, "MALFORMED_JSON"));
        given(privateKeyChecker.doesExist(CLIENT_PRIVATE_KEY_PATH)).willReturn(true);

        // WHEN  // THEN
        assertThatExceptionOfType(SvcLogicException.class)
            .isThrownBy(() -> chefAdapterFactory.create().vnfcEnvironment(params, svcLogicContext))
            .withMessageStartingWith(CHEF_ADAPTER_ERROR_PREFIX + expectedErrorMessage);

        assertThat(svcLogicContext.getStatus()).isEqualTo(FAILURE_STATUS);
        assertThat(svcLogicContext.getAttribute(RESULT_CODE_ATTR_KEY))
            .isEqualTo(Integer.toString(HttpStatus.SC_UNAUTHORIZED));
        assertThat(svcLogicContext.getAttribute(RESULT_MESSAGE_ATTR_KEY))
            .startsWith(expectedErrorMessage);
    }

    @Test
    public void vnfcEnvironment_shouldNotAttemptEnvCreation_andHandleException_whenExceptionOccursDuringExecution() {
        // GIVEN
        String expectedErrorMessage = "Error posting request: ";
        Map<String, String> params = givenInputParams(immutableEntry(ENV_PARAM_KEY, ENV_JSON_VALUE));
        given(privateKeyChecker.doesExist(CLIENT_PRIVATE_KEY_PATH)).willReturn(true);
        given(chefApiClientFactory.create("https://localhost/organizations/onap", ORGANIZATIONS, USERNAME,
            CLIENT_PRIVATE_KEY_PATH)).willThrow(new NullPointerException("Null value encountered"));

        // WHEN  // THEN
        assertThatExceptionOfType(SvcLogicException.class)
            .isThrownBy(() -> chefAdapterFactory.create().vnfcEnvironment(params, svcLogicContext))
            .withMessage(CHEF_ADAPTER_ERROR_PREFIX + expectedErrorMessage + "Null value encountered");

        assertThat(svcLogicContext.getStatus()).isEqualTo(FAILURE_STATUS);
        assertThat(svcLogicContext.getAttribute(RESULT_CODE_ATTR_KEY))
            .isEqualTo(Integer.toString(HttpStatus.SC_UNAUTHORIZED));
        assertThat(svcLogicContext.getAttribute(RESULT_MESSAGE_ATTR_KEY)).startsWith(expectedErrorMessage);
    }

    private Map<String, String> givenInputParams(Entry<String, String>... entries) {
        Builder<String, String> paramsBuilder = ImmutableMap.builder();
        paramsBuilder.put("username", USERNAME)
            .put("serverAddress", SERVER_ADDRESS)
            .put("organizations", ORGANIZATIONS);

        for (Entry<String, String> entry : entries) {
            paramsBuilder.put(entry);
        }
        return paramsBuilder.build();
    }
}