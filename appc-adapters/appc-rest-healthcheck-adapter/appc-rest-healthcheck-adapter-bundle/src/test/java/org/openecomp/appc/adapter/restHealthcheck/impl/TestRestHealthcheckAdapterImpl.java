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
package org.openecomp.appc.adapter.restHealthcheck.impl;

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
import org.openecomp.appc.adapter.restHealthcheck.*;
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



public class TestRestHealthcheckAdapterImpl {

    @SuppressWarnings("nls")
    private static final String PROVIDER_NAME = "ILAB";

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

    private RestHealthcheckAdapterImpl adapter;


    @SuppressWarnings("nls")
    @BeforeClass
    public static void once() throws NoSuchFieldException, SecurityException, NoSuchMethodException {

    }

    @Before
    public void setup() throws IllegalArgumentException, IllegalAccessException {

        adapter = new RestHealthcheckAdapterImpl();
    }

    @Test
    public void testCheckHealth() throws IOException, IllegalStateException, IllegalArgumentException,
        ZoneException, APPCException {

            Map<String, String> params = new HashMap<>();
            params.put("VNF.URI", "http://restHalthCheck.test");
            params.put("VNF.endpoint", "health");
            SvcLogicContext svcContext = new SvcLogicContext();
            adapter.checkHealth(params, svcContext);
            String statusCode=svcContext.getAttribute("healthcheck.result.code");
            assertEquals("200",statusCode);
    }



}
