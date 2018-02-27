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

package org.onap.appc.adapter.chef.impl;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.appc.exceptions.APPCException;
import com.att.cdp.exceptions.ZoneException;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

public class TestChefAdapterImpl {
    private SvcLogicContext svcContext;

    private ChefAdapterImpl adapter;

    private Map<String, String> params;
    private String getAttribute;

    @Before
    public void setup() {
        adapter = new ChefAdapterImpl();
        params = new HashMap<>();
        params.put("pemPath",
                "/src/test/resources/testclient.pem");
    }

    @After
    public void tearDown() {
        params = null;
        svcContext = null;
        getAttribute = null;
    }
    
        @Test(expected=Exception.class)
    public void testChefGetFail() throws IOException, IllegalStateException, IllegalArgumentException,
            ZoneException, APPCException,SvcLogicException {
        params.put("chefAction", "/nodes");

        givenParams(params, "chefGet");
        thenResponseShouldFail();
    }

      @Test(expected=Exception.class)
    public void testChefPutFail() throws IOException, IllegalStateException, IllegalArgumentException,
            ZoneException, APPCException,SvcLogicException {
        params.put("chefAction", "/nodes/testnode");
        params.put("runList", "recipe[commandtest]");
        params.put("attributes", "");
        params.put("chefRequestBody", "Test Body");

        givenParams(params, "chefPut");
        thenResponseShouldFail();
    }

    @Test
    public void testTriggerFail() throws IOException, IllegalStateException, IllegalArgumentException,
            ZoneException, APPCException,SvcLogicException {
        params.put("ip", "");

        givenParams(params, "trigger");
        thenResponseShouldFail();
    }

    private void givenParams(Map<String, String> adapterParams, String method) throws SvcLogicException {
        svcContext = new SvcLogicContext();
        if (method == "chefGet"){
            adapter.chefGet(adapterParams, svcContext);
            getAttribute = "chefServerResult.code";
        }
        if (method == "chefPut"){
            adapter.chefPut(adapterParams, svcContext);
            getAttribute = "chefServerResult.code";
        }
        if (method == "trigger"){
            adapter.trigger(adapterParams, svcContext);
            getAttribute = "chefAgent.code";
        }
    }

    private void thenResponseShouldFail(){
        String status = svcContext.getAttribute(this.getAttribute);
        assertEquals("500", status);
    }
}
