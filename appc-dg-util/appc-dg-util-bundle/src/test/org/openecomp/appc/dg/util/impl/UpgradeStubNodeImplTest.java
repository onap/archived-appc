/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.appc.dg.util.impl;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openecomp.appc.exceptions.APPCException;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;
import org.onap.ccsdk.sli.adaptors.aai.AAIClient;
import org.onap.ccsdk.sli.adaptors.aai.AAIService;

import java.util.HashMap;
import java.util.Map;

public class UpgradeStubNodeImplTest {
    @Mock
    private SvcLogicContext svcLogicContext;

    private UpgradeStubNodeImpl upgradeStubNode;

    @Before
    public void setUp() throws Exception {
        upgradeStubNode = new UpgradeStubNodeImpl();
    }

    @Test
    public void testHandleUpgradeStubSuccess() throws APPCException {
        Map<String, String> params = new HashMap<>();
        upgradeStubNode.handleUpgradeStub(params, svcLogicContext);
    }

    @Test(expected = APPCException.class)
    public void testHandleUpgradeStubException() throws APPCException {
        Map<String, String> params = new HashMap<>();
        params.put("failureIndicator", "true");
        upgradeStubNode.handleUpgradeStub(params, svcLogicContext);
    }
}