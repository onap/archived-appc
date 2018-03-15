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
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
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
public class ChefAdapterImplJobPusherTest {

    private static final String CLIENT_PRIVATE_KEY_PATH = "/opt/onap/appc/chef/localhost/onap/testclient.pem";
    private static final String RESULT_CODE_ATTR_KEY = "chefServerResult.code";
    private static final String RESULT_MESSAGE_ATTR_KEY = "chefServerResult.message";
    private static final String EXPECTED_RESPONSE_MSG = "jobs/{666}/";

    private static final String USERNAME = "testclient";
    private static final String SERVER_ADDRESS = "localhost";
    private static final String ORGANIZATIONS = "onap";
    private static final String ACTION_PARAM = "/pushy/jobs";
    private static final String REQUEST_BODY_DATA = "requestBodyData";
    private static final Map<String, String> PARAMS = ImmutableMap
        .of("username", USERNAME,
            "serverAddress", SERVER_ADDRESS,
            "organizations", ORGANIZATIONS,
            "chefAction", ACTION_PARAM,
            "pushRequest", REQUEST_BODY_DATA);
    private static final String JOB_ID = "jobID";

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
    public void pushJob_shouldSuccessfullyMakePostCall_andUpdateSvcLogicContext_whenReturnedStatusIsDifferentThan_201()
        throws SvcLogicException {
        assertSuccessfulPostCallForStatus(HttpStatus.SC_OK);
        assertThat(svcLogicContext.getAttribute(JOB_ID)).isBlank();
    }

    @Test
    public void pushJob_shouldSuccessfullyMakePostCall_andUpdateSvcLogicContext_withReturnedStatusIs_201()
        throws SvcLogicException {
        assertSuccessfulPostCallForStatus(HttpStatus.SC_CREATED);
        assertThat(svcLogicContext.getAttribute(JOB_ID)).isEqualTo("666");
    }

    public void assertSuccessfulPostCallForStatus(int expectedHttpStatus) throws SvcLogicException {
        // GIVEN
        given(chefApiClientFactory.create("https://localhost/organizations/onap", ORGANIZATIONS, USERNAME,
            CLIENT_PRIVATE_KEY_PATH)).willReturn(chefApiClient);
        given(chefApiClient.post(ACTION_PARAM, REQUEST_BODY_DATA))
            .willReturn(ChefResponse.create(expectedHttpStatus, EXPECTED_RESPONSE_MSG));

        // WHEN
        chefAdapterFactory.create().pushJob(PARAMS, svcLogicContext);

        // THEN
        assertThat(svcLogicContext.getStatus()).isEqualTo("success");
        assertThat(svcLogicContext.getAttribute(RESULT_CODE_ATTR_KEY))
            .isEqualTo(Integer.toString(expectedHttpStatus));
        assertThat(svcLogicContext.getAttribute(RESULT_MESSAGE_ATTR_KEY)).isEqualTo(EXPECTED_RESPONSE_MSG);
    }

    @Test
    public void pushJob_shouldHandleAllOccurringExceptions_duringMethodExecution() {
        // GIVEN
        String EXPECTED_ERROR_MSG = "Something went wrong";
        given(chefApiClientFactory.create("https://localhost/organizations/onap", ORGANIZATIONS, USERNAME,
            CLIENT_PRIVATE_KEY_PATH)).willThrow(new NullPointerException(EXPECTED_ERROR_MSG));

        // WHEN // THEN
        assertThatExceptionOfType(SvcLogicException.class)
            .isThrownBy(() -> chefAdapterFactory.create().pushJob(PARAMS, svcLogicContext))
            .withMessage("Chef Adapter error:" + EXPECTED_ERROR_MSG);

        assertThat(svcLogicContext.getStatus()).isEqualTo("failure");
        assertThat(svcLogicContext.getAttribute(RESULT_CODE_ATTR_KEY))
            .isEqualTo(Integer.toString(HttpStatus.SC_UNAUTHORIZED));
        assertThat(svcLogicContext.getAttribute(RESULT_MESSAGE_ATTR_KEY)).isEqualTo(EXPECTED_ERROR_MSG);
        assertThat(svcLogicContext.getAttribute(JOB_ID)).isBlank();
    }
}
