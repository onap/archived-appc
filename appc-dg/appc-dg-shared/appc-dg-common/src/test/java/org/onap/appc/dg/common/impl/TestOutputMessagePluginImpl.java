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
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.onap.appc.exceptions.APPCException;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

public class TestOutputMessagePluginImpl {

    private OutputMessagePluginImpl outputMessagePluginImpl;
    private Map<String, String> params;
    private SvcLogicContext ctx;

    @Before
    public void setUp() {
        outputMessagePluginImpl = new OutputMessagePluginImpl();
        ctx = new SvcLogicContext();
        params = new HashMap<>();
        params.put(Constants.ATTRIBUTE_ERROR_MESSAGE, "Error Message");
        params.put(Constants.EVENT_MESSAGE, "Error description");
    }

    @Test
    public void testOutputMessageBuilder() throws APPCException {
        outputMessagePluginImpl.outputMessageBuilder(params, ctx);
        assertEquals("Error Message | Error description", ctx.getAttribute(Constants.DG_OUTPUT_STATUS_MESSAGE));
    }

    @Test
    public void testOutputMessageBuilderWithoutEventMessage() throws APPCException {
        params.put(Constants.EVENT_MESSAGE, "");
        outputMessagePluginImpl.outputMessageBuilder(params, ctx);
        assertEquals("Error Message", ctx.getAttribute(Constants.DG_OUTPUT_STATUS_MESSAGE));
    }

}
