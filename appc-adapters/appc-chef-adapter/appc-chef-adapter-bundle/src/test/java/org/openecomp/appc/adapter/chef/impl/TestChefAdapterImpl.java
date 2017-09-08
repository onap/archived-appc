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

package org.openecomp.appc.adapter.chef.impl;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openecomp.appc.exceptions.APPCException;
import com.att.cdp.exceptions.ZoneException;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

public class TestChefAdapterImpl {
    private SvcLogicContext svcContext;

    private ChefAdapterImpl adapter;

    private Map<String, String> params;
    private String getAttribute;

    @Before
    public void setup() {
        adapter = new ChefAdapterImpl(Boolean.TRUE);
        params = new HashMap<>();
        params.put("org.openecomp.appc.instance.pemPath",
                "/src/test/resources/testclient.pem");
    }

    @After
    public void tearDown() {
        params = null;
        svcContext = null;
        getAttribute = null;
    }

    @Test
    public void testChefGetFail() throws IOException, IllegalStateException, IllegalArgumentException,
            ZoneException, APPCException {
        params.put("org.openecomp.appc.instance.chefAction", "/nodes");

        givenParams(params, "chefGet");
        thenResponseShouldFail();
    }

    @Test
    public void testChefPutFail() throws IOException, IllegalStateException, IllegalArgumentException,
            ZoneException, APPCException {
        params.put("org.openecomp.appc.instance.chefAction", "/nodes/testnode");
        params.put("org.openecomp.appc.instance.runList", "recipe[commandtest]");
        params.put("org.openecomp.appc.instance.attributes", "");
        params.put("org.openecomp.appc.instance.chefRequestBody", "Test Body");

        givenParams(params, "chefPut");
        thenResponseShouldFail();
    }

    @Test
    public void testTriggerFail() throws IOException, IllegalStateException, IllegalArgumentException,
            ZoneException, APPCException {
        params.put("org.openecomp.appc.instance.ip", "");

        givenParams(params, "trigger");
        thenResponseShouldFail();
    }

    private void givenParams(Map<String, String> adapterParams, String method) {
        svcContext = new SvcLogicContext();
        if (method == "chefGet"){
            adapter.chefGet(adapterParams, svcContext);
            getAttribute = "org.openecomp.appc.chefServerResult.code";
        }
        if (method == "chefPut"){
            adapter.chefPut(adapterParams, svcContext);
            getAttribute = "org.openecomp.appc.chefServerResult.code";
        }
        if (method == "trigger"){
            adapter.trigger(adapterParams, svcContext);
            getAttribute = "org.openecomp.appc.chefAgent.code";
        }
    }

    private void thenResponseShouldFail(){
        String status = svcContext.getAttribute(this.getAttribute);
        assertEquals("500", status);
    }
}
