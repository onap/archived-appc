/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 Ericsson. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.provider;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.provider.SvcLogicService;

public class TestAppcProviderClient {

    private AppcProviderClient appcProviderClient;
    private SvcLogicService svcLogicService;

    @Before
    public void setUp() {
        svcLogicService = Mockito.mock(SvcLogicService.class);
        appcProviderClient = new AppcProviderClient(svcLogicService);
    }

    @Test
    public void testHasGraph() throws SvcLogicException {
        when(svcLogicService.hasGraph("APPC", "healthcheck", "1.0", "active")).thenReturn(true);
        assertTrue(appcProviderClient.hasGraph("APPC", "healthcheck", "1.0", "active"));
    }

    @Test
    public void testExecute() throws SvcLogicException {
        Properties respProps = new Properties();
        when(svcLogicService.execute("APPC", "healthcheck", "1.0", "active", null)).thenReturn(respProps);
        assertSame(respProps, appcProviderClient.execute("APPC", "healthcheck", "1.0", "active", null));
    }
}
