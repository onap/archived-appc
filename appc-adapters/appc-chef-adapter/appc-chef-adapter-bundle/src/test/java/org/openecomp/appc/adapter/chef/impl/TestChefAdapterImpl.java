/*-
 * ============LICENSE_START=======================================================
 * APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Amdocs
 * ================================================================================
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */

package org.openecomp.appc.adapter.chef.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openecomp.appc.Constants;
import org.openecomp.appc.adapter.chef.ChefAdapter;
import org.openecomp.appc.adapter.chef.impl.ChefAdapterImpl;
import org.openecomp.appc.configuration.ConfigurationFactory;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.exceptions.UnknownProviderException;
import com.att.cdp.exceptions.ZoneException;
import com.att.cdp.zones.ComputeService;
import com.att.cdp.zones.Context;
import com.att.cdp.zones.ContextFactory;
import com.att.cdp.zones.model.Server;
import com.att.cdp.zones.model.Server.Status;
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.slf4j.MDC;

@Ignore
public class TestChefAdapterImpl {


    private ChefAdapterImpl adapter;

    @SuppressWarnings("nls")
    @BeforeClass
    public static void once() throws NoSuchFieldException, SecurityException, NoSuchMethodException {

    }

    @Before
    public void setup() throws IllegalArgumentException, IllegalAccessException {

        adapter = new ChefAdapterImpl(System.getProperty("user.dir")+"/src/main/resources/client.pem");
    }
    
    @Test
    public void testChefGet() throws IOException, IllegalStateException, IllegalArgumentException,
      ZoneException, APPCException {

            Map<String, String> params = new HashMap<>();
            params.put("org.openecomp.appc.instance.chefAction", "/nodes");
            
            
            SvcLogicContext svcContext = new SvcLogicContext();          
            adapter.chefGet(params, svcContext);
            String status=svcContext.getAttribute("org.openecomp.appc.chefServerResult.code");
            assertEquals("200",status);

    }

    @Test
    public void testChefPut() throws IOException, IllegalStateException, IllegalArgumentException,
        ZoneException, APPCException {

            Map<String, String> params = new HashMap<>();
            params.put("org.openecomp.appc.instance.chefAction", "/nodes/testnode");
            params.put("org.openecomp.appc.instance.runList", "recipe[commandtest]");
            params.put("org.openecomp.appc.instance.attributes", "");
            SvcLogicContext svcContext = new SvcLogicContext();          
            adapter.chefPut(params, svcContext);
            String status=svcContext.getAttribute("org.openecomp.appc.chefServerResult.code");
            assertEquals("200",status);

    }

    @Test
    public void testTrigger() throws IOException, IllegalStateException, IllegalArgumentException,
    ZoneException, APPCException {

            Map<String, String> params = new HashMap<>();
            params.put("org.openecomp.appc.instance.ip", "http://example.com/test");
            SvcLogicContext svcContext = new SvcLogicContext();          
            adapter.trigger(params, svcContext);
            String status=svcContext.getAttribute("org.openecomp.appc.chefAgent.code");
            assertEquals("200",status);

    }


}
