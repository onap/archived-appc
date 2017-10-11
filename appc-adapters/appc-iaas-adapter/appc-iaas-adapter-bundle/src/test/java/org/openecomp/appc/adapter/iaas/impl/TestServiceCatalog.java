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



package org.openecomp.appc.adapter.iaas.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.openecomp.appc.configuration.ConfigurationFactory;
import com.att.cdp.exceptions.ZoneException;
import com.woorea.openstack.keystone.model.Access.Service;

/**
 * This class tests the service catalog against a known provider.
 */
public class TestServiceCatalog {

    // Number
    private static int EXPECTED_REGIONS = 2;
    private static int EXPECTED_ENDPOINTS = 1;

    private static String PRINCIPAL;
    private static String CREDENTIAL;
    private static String TENANT_NAME;
    private static String TENANT_ID;
    private static String IDENTITY_URL;
    private static String REGION_NAME;

    private ServiceCatalog catalog;

    private Properties properties;

    @BeforeClass
    public static void before() {
        Properties props = ConfigurationFactory.getConfiguration().getProperties();
        IDENTITY_URL = props.getProperty("provider1.identity");
        PRINCIPAL = props.getProperty("provider1.tenant1.userid", "appc");
        CREDENTIAL = props.getProperty("provider1.tenant1.password", "appc");
        TENANT_NAME = props.getProperty("provider1.tenant1.name", "appc");
        TENANT_ID = props.getProperty("provider1.tenant1.id",
                props.getProperty("test.tenantid", "abcde12345fghijk6789lmnopq123rst"));
        REGION_NAME = props.getProperty("provider1.tenant1.region", "RegionOne");

        EXPECTED_REGIONS = Integer.valueOf(props.getProperty("test.expected-regions", "2"));
        EXPECTED_ENDPOINTS = Integer.valueOf(props.getProperty("test.expected-endpoints", "0"));
    }

    /**
     * Setup the test environment by loading a new service catalog for each test
     * 
     * @throws ZoneException
     */
    @Before
    public void setup() throws ZoneException {
        properties = new Properties();
        catalog = Mockito.mock(ServiceCatalog.class, Mockito.CALLS_REAL_METHODS);
        catalog.rwLock = new ReentrantReadWriteLock();

        Set<String> testdata = new HashSet<>();
        testdata.add("RegionOne");
        testdata.add("RegionTwo");
        catalog.regions = testdata;
    }

    /**
     * Test that we find all of the expected region(s)
     */
    @Test
    public void testKnownRegions() {
        assertEquals(EXPECTED_REGIONS, catalog.getRegions().size());
    }
}
