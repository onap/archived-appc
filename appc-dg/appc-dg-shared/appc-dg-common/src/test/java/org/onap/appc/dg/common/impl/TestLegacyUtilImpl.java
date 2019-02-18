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

package org.onap.appc.dg.common.impl;

import static org.junit.Assert.assertEquals;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.onap.appc.exceptions.APPCException;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

public class TestLegacyUtilImpl {

    private LegacyUtilImpl legacyUtilImpl;
    private Map<String, String> params;
    private SvcLogicContext ctx;

    @Before
    public void setUp() {
        legacyUtilImpl = new LegacyUtilImpl();
        ctx = new SvcLogicContext();
        ctx.setAttribute(Constants.LCMAttributes.PAYLOAD.getValue(),
                "{\"vm-id\":\"12\",\"identity-url\":\"localhost\",\"tenant.id\":\"\",\"skip-hypervisor-check\":\"\"}");
        ctx.setAttribute(Constants.LCMAttributes.ACTION.getValue(), "");
    }

    @Test
    public void testParseRequest() throws APPCException {
        legacyUtilImpl.prepareRequest(params, ctx);
        assertEquals("12", ctx.getAttribute(Constants.LegacyAttributes.VMID.getValue()));
    }

}
