/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
 * Copyright (C) 2018 Nokia
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

package org.onap.appc.dg.util.impl;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.appc.dg.util.UpgradeStubNode;
import org.onap.appc.exceptions.APPCException;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

@RunWith(MockitoJUnitRunner.class)
public class UpgradeStubNodeImplTest {

    @Mock
    private SvcLogicContext svcLogicContext;

    private UpgradeStubNode upgradeStubNode;

    @Before
    public void setUp() {
        upgradeStubNode = new UpgradeStubNodeImpl();
    }

    @Test
    public void handleUpgradeStub_shouldCompleteSuccessfully_whenFailureIndicatorIsNull() throws APPCException {
        Map<String, String> params = new HashMap<>();
        upgradeStubNode.handleUpgradeStub(params, svcLogicContext);
        verifyZeroInteractions(svcLogicContext);
    }

    @Test
    public void handleUpgradeStub_shouldCompleteSuccessfully_whenFailureIndicatorIsFalse() throws APPCException {
        // GIVEN
        Map<String, String> params = new HashMap<>();
        params.put("failureIndicator", "false");
        // WHEN
        upgradeStubNode.handleUpgradeStub(params, svcLogicContext);
        // THEN
        verifyZeroInteractions(svcLogicContext);
    }

    @Test
    public void handleUpgradeStub_shouldThrowAPPCException_whenFailureIndicatorIsTrue() throws APPCException {
        // GIVEN
        Map<String, String> params = new HashMap<>();
        params.put("failureIndicator", "true");
        // WHEN // THEN
        assertThatExceptionOfType(APPCException.class)
            .isThrownBy(() -> upgradeStubNode.handleUpgradeStub(params, svcLogicContext));
        verifyZeroInteractions(svcLogicContext);
    }
}