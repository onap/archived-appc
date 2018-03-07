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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.appc.adapter.chef.ChefAdapter;
import org.onap.appc.adapter.chef.chefclient.ChefApiClientFactory;
import org.onap.appc.adapter.chef.chefclient.api.ChefApiClient;
import org.onap.appc.adapter.chef.chefclient.api.ChefResponse;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

@RunWith(MockitoJUnitRunner.class)
public class ChefAdapterImplHttpMethodTest {

    private static final String CLIENT_PRIVATE_KEY_PATH = "/opt/onap/appc/chef/localhost/onap/testclient.pem";
    private static final String RESULT_CODE_ATTR_KEY = "chefServerResult.code";
    private static final String RESULT_MESSAGE_ATTR_KEY = "chefServerResult.message";
    @Mock
    private PrivateKeyChecker privateKeyChecker;
    @Mock
    private ChefApiClientFactory chefApiClientFactory;
    @Mock
    private ChefApiClient chefApiClient;
    private static final Map<String, String> PARAMS = ImmutableMap
        .of("username", "testclient",
            "serverAddress", "localhost",
            "organizations", "onap",
            "chefAction", "action");

    @Test
    public void chefGet_shouldExecuteHttpClient_andSetChefResponseInContext_whenPrivateKeyFileExists() throws SvcLogicException {
        // GIVEN
        SvcLogicContext svcLogicContext = new SvcLogicContext();
        given(privateKeyChecker.doesExist(CLIENT_PRIVATE_KEY_PATH)).willReturn(true);
        given(chefApiClientFactory.create("https://localhost/organizations/onap",
            "onap",
            "testclient",
            CLIENT_PRIVATE_KEY_PATH)).willReturn(chefApiClient);
        given(chefApiClient.get("action")).willReturn(ChefResponse.create(HttpStatus.SC_OK, "chefResponseMessage"));

        // WHEN
        ChefAdapter chefAdapter = new ChefAdapterImpl(chefApiClientFactory, privateKeyChecker);
        chefAdapter.chefGet(PARAMS, svcLogicContext);

        // THEN
        assertThat(svcLogicContext.getStatus()).isEqualTo("success");
        assertThat(svcLogicContext.getAttribute(RESULT_CODE_ATTR_KEY)).isEqualTo(Integer.toString(HttpStatus.SC_OK));
        assertThat(svcLogicContext.getAttribute(RESULT_MESSAGE_ATTR_KEY)).isEqualTo("chefResponseMessage");
    }

    @Test
    public void chefGet_shouldNotExecuteHttpClient_andSetErrorResponseInContext_whenPrivateKeyFileDoesNotExist() throws SvcLogicException {
        // GIVEN
        SvcLogicContext svcLogicContext = new SvcLogicContext();
        given(privateKeyChecker.doesExist(CLIENT_PRIVATE_KEY_PATH)).willReturn(false);

        // WHEN
        ChefAdapter chefAdapter = new ChefAdapterImpl(chefApiClientFactory, privateKeyChecker);
        chefAdapter.chefGet(PARAMS, svcLogicContext);

        // THEN
        assertThat(svcLogicContext.getStatus()).isEqualTo("success");
        assertThat(svcLogicContext.getAttribute(RESULT_CODE_ATTR_KEY))
            .isEqualTo(Integer.toString(HttpStatus.SC_INTERNAL_SERVER_ERROR));
        assertThat(svcLogicContext.getAttribute(RESULT_MESSAGE_ATTR_KEY)).isEqualTo(
            "Cannot find the private key in the APPC file system, please load the private key to "
                + CLIENT_PRIVATE_KEY_PATH);
    }
}