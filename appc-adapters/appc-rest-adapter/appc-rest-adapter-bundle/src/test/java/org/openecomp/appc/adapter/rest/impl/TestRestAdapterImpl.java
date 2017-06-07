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

package org.openecomp.appc.adapter.rest.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import org.slf4j.MDC;

import org.openecomp.appc.Constants;
import org.openecomp.appc.adapter.rest.RestAdapter;
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


/**
 * Test the ProviderAdapter implementation.
 */

@Ignore
public class TestRestAdapterImpl {

    @SuppressWarnings("nls")
    private static final String PROVIDER_NAME = "APPC";

    @SuppressWarnings("nls")
    private static final String PROVIDER_TYPE = "OpenStackProvider";

    private static String IDENTITY_URL;

    private static String PRINCIPAL;

    private static String CREDENTIAL;

    private static String TENANT_NAME;

    private static String TENANT_ID;

    private static String USER_ID;

    private static String REGION_NAME;

    private static String SERVER_URL;

    private static Class<?> providerAdapterImplClass;
    private static Class<?> configurationFactoryClass;
    private static Field providerCacheField;
    private static Field configField;

    private RestAdapterImpl adapter;


    @SuppressWarnings("nls")
    @BeforeClass
    public static void once() throws NoSuchFieldException, SecurityException, NoSuchMethodException {

    }

    @Before
    public void setup() throws IllegalArgumentException, IllegalAccessException {

        adapter = new RestAdapterImpl();
    }
    
    @Test
    public void testCommonGet() throws IOException, IllegalStateException, IllegalArgumentException,
        ZoneException, APPCException {    
         
            Map<String, String> params = new HashMap<>();
            params.put("org.openecomp.appc.instance.URI", "http://example.com:8080/about/health");
            params.put("org.openecomp.appc.instance.haveHeader","false");
            SvcLogicContext svcContext = new SvcLogicContext();          
            adapter.commonGet(params, svcContext);
            String statusCode=svcContext.getAttribute("org.openecomp.rest.agent.result.code");
            assertEquals("200",statusCode);
    }
    
    @Test
    public void testCommonPost() throws IOException, IllegalStateException, IllegalArgumentException,
        ZoneException, APPCException {    
         
            Map<String, String> params = new HashMap<>();
            params.put("org.openecomp.appc.instance.URI", "http://example.com:8081/posttest");
            params.put("org.openecomp.appc.instance.haveHeader","false");
            params.put("org.openecomp.appc.instance.requestBody", "{\"name\":\"MyNode\", \"width\":200, \"height\":100}");
            SvcLogicContext svcContext = new SvcLogicContext();          
            adapter.commonPost(params, svcContext);
            String statusCode=svcContext.getAttribute("org.openecomp.rest.agent.result.code");
            assertEquals("200",statusCode);
    }
    
    @Test
    public void testCommonPut() throws IOException, IllegalStateException, IllegalArgumentException,
        ZoneException, APPCException {    
         
            Map<String, String> params = new HashMap<>();
            params.put("org.openecomp.appc.instance.URI", "http://example.com:8081/puttest");
            params.put("org.openecomp.appc.instance.haveHeader","false");
            params.put("org.openecomp.appc.instance.requestBody", "{\"name\":\"MyNode2\", \"width\":300, \"height\":300}");
            SvcLogicContext svcContext = new SvcLogicContext();          
            adapter.commonPut(params, svcContext);
            String statusCode=svcContext.getAttribute("org.openecomp.rest.agent.result.code");
            assertEquals("200",statusCode);
    }
    
    @Test
    public void testCommonDelete() throws IOException, IllegalStateException, IllegalArgumentException,
        ZoneException, APPCException {    
         
            Map<String, String> params = new HashMap<>();
            params.put("org.openecomp.appc.instance.URI", "http://example.com:8081/deletetest");
            params.put("org.openecomp.appc.instance.haveHeader","false");
            SvcLogicContext svcContext = new SvcLogicContext();          
            adapter.commonDelete(params, svcContext);
            String statusCode=svcContext.getAttribute("org.openecomp.rest.agent.result.code");
            assertEquals("200",statusCode);
    }


}
