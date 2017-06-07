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

package org.openecomp.appc.adapter.iaas.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
import org.junit.experimental.categories.Category;
import org.openecomp.appc.Constants;
import org.openecomp.appc.adapter.iaas.ProviderAdapter;
import org.openecomp.appc.adapter.iaas.impl.ProviderAdapterImpl;
import org.openecomp.appc.adapter.iaas.impl.ProviderCache;
import org.openecomp.appc.adapter.iaas.impl.ServiceCatalog;
import org.openecomp.appc.adapter.iaas.impl.TenantCache;
import org.openecomp.appc.adapter.iaas.impl.VMURL;
import org.openecomp.appc.configuration.ConfigurationFactory;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.exceptions.UnknownProviderException;
import com.att.cdp.exceptions.ZoneException;
import com.att.cdp.zones.ComputeService;
import com.att.cdp.zones.Context;
import com.att.cdp.zones.ContextFactory;
import com.att.cdp.zones.model.Image;
import com.att.cdp.zones.model.Server;
import com.att.cdp.zones.model.Server.Status;
import org.openecomp.sdnc.sli.SvcLogicContext;

import com.woorea.openstack.keystone.model.Access.Service.Endpoint;

/**
 * Test the ProviderAdapter implementation.
 */
@Category(org.openecomp.appc.adapter.iaas.impl.TestProviderAdapterImpl.class)
public class TestProviderAdapterImpl {

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

    private ProviderAdapterImpl adapter;

    /**
     * Use reflection to locate fields and methods so that they can be manipulated during the test to change the
     * internal state accordingly.
     * 
     * @throws NoSuchFieldException
     *             if the field(s) dont exist
     * @throws SecurityException
     *             if reflective access is not allowed
     * @throws NoSuchMethodException
     *             If the method(s) dont exist
     */
    @SuppressWarnings("nls")
    @BeforeClass
    public static void once() throws NoSuchFieldException, SecurityException, NoSuchMethodException {
        providerAdapterImplClass = ProviderAdapterImpl.class;
        configurationFactoryClass = ConfigurationFactory.class;

        providerCacheField = providerAdapterImplClass.getDeclaredField("providerCache");
        providerCacheField.setAccessible(true);

        configField = configurationFactoryClass.getDeclaredField("config");
        configField.setAccessible(true);

        Properties props = ConfigurationFactory.getConfiguration().getProperties();
        IDENTITY_URL = props.getProperty("provider1.identity");
        PRINCIPAL = props.getProperty("provider1.tenant1.userid", "appc");
        CREDENTIAL = props.getProperty("provider1.tenant1.password", "appc");
        TENANT_NAME = props.getProperty("provider1.tenant1.name", "appc");
        TENANT_ID = props.getProperty("provider1.tenant1.id", "abcde12345fghijk6789lmnopq123rst");
        REGION_NAME = props.getProperty("provider1.tenant1.region", "RegionOne");
        SERVER_URL = props.getProperty("test.url");
    }

    /**
     * Setup the test environment.
     * 
     * @throws IllegalAccessException
     *             if this Field object is enforcing Java language access control and the underlying field is either
     *             inaccessible or final.
     * @throws IllegalArgumentException
     *             if the specified object is not an instance of the class or interface declaring the underlying field
     *             (or a subclass or implementor thereof), or if an unwrapping conversion fails.
     * @throws NullPointerException
     *             if the specified object is null and the field is an instance field.
     * @throws ExceptionInInitializerError
     *             if the initialization provoked by this method fails.
     */
    @Before
    public void setup() throws IllegalArgumentException, IllegalAccessException {
        configField.set(null, null);
        Properties properties = new Properties();
        adapter = new ProviderAdapterImpl(properties);
    }

    /**
     * This method inspects the provider adapter implementation to make sure that the cache of providers and tenants, as
     * well as the service catalog, and all pools of contexts have been set up correctly.
     * 
     * @throws IllegalAccessException
     *             if this Field object is enforcing Java language access control and the underlying field is
     *             inaccessible.
     * @throws IllegalArgumentException
     *             if the specified object is not an instance of the class or interface declaring the underlying field
     *             (or a subclass or implementor thereof).
     */
    @SuppressWarnings({
        "unchecked"
    })
    @Ignore
    @Test
    public void validateCacheIsCreatedCorrectly() throws IllegalArgumentException, IllegalAccessException {
        Map<String, ProviderCache> providerCaches = (Map<String, ProviderCache>) providerCacheField.get(adapter);

        assertNotNull(providerCaches);
        assertEquals(1, providerCaches.size());
        assertTrue(providerCaches.containsKey(PROVIDER_NAME));

        ProviderCache providerCache = providerCaches.get(PROVIDER_NAME);
        assertEquals(PROVIDER_NAME, providerCache.getProviderName());
        assertEquals(PROVIDER_TYPE, providerCache.getProviderType());

        Map<String, TenantCache> tenantCaches = providerCache.getTenants();
        assertNotNull(tenantCaches);
        assertEquals(1, tenantCaches.size());
        assertTrue(tenantCaches.containsKey(TENANT_NAME));

        TenantCache tenantCache = tenantCaches.get(TENANT_NAME);

        assertEquals(TENANT_ID, tenantCache.getTenantId());
        assertEquals(TENANT_NAME, tenantCache.getTenantName());
        assertEquals(USER_ID, tenantCache.getUserid());

        ServiceCatalog catalog = tenantCache.getServiceCatalog();
        assertNotNull(catalog);

        System.out.println(catalog.toString());
        List<String> serviceTypes = catalog.getServiceTypes();
        assertNotNull(serviceTypes);
        assertEquals(12, serviceTypes.size());

        assertEquals(TENANT_NAME, catalog.getTenantName());
        assertEquals(TENANT_ID, catalog.getTenantId());

        Set<String> regionNames = catalog.getRegions();
        assertNotNull(regionNames);
        assertEquals(1, regionNames.size());
        assertTrue(regionNames.contains(REGION_NAME));

        List<Endpoint> endpoints = catalog.getEndpoints(ServiceCatalog.IDENTITY_SERVICE);
        assertNotNull(endpoints);
        assertEquals(1, endpoints.size());
        Endpoint endpoint = endpoints.get(0);
        assertNotNull(endpoint);
        assertEquals(REGION_NAME, endpoint.getRegion());
        assertEquals(IDENTITY_URL, endpoint.getPublicURL());

        endpoints = catalog.getEndpoints(ServiceCatalog.COMPUTE_SERVICE);
        assertNotNull(endpoints);
        assertEquals(1, endpoints.size());
        endpoint = endpoints.get(0);
        assertNotNull(endpoint);
        assertEquals(REGION_NAME, endpoint.getRegion());

        endpoints = catalog.getEndpoints(ServiceCatalog.VOLUME_SERVICE);
        assertNotNull(endpoints);
        assertEquals(1, endpoints.size());
        endpoint = endpoints.get(0);
        assertNotNull(endpoint);
        assertEquals(REGION_NAME, endpoint.getRegion());

        endpoints = catalog.getEndpoints(ServiceCatalog.IMAGE_SERVICE);
        assertNotNull(endpoints);
        assertEquals(1, endpoints.size());
        endpoint = endpoints.get(0);
        assertNotNull(endpoint);
        assertEquals(REGION_NAME, endpoint.getRegion());

        endpoints = catalog.getEndpoints(ServiceCatalog.NETWORK_SERVICE);
        assertNotNull(endpoints);
        assertEquals(1, endpoints.size());
        endpoint = endpoints.get(0);
        assertNotNull(endpoint);
        assertEquals(REGION_NAME, endpoint.getRegion());

        assertTrue(catalog.isServicePublished(ServiceCatalog.IDENTITY_SERVICE));
        assertTrue(catalog.isServicePublished(ServiceCatalog.COMPUTE_SERVICE));
        assertTrue(catalog.isServicePublished(ServiceCatalog.VOLUME_SERVICE));
        assertTrue(catalog.isServicePublished(ServiceCatalog.IMAGE_SERVICE));
        assertTrue(catalog.isServicePublished(ServiceCatalog.NETWORK_SERVICE));
    }

    /**
     * This test case is used to actually validate that a server has been restarted from an already running state
     * 
     * @throws ZoneException
     *             If the login cannot be performed because the principal and/or credentials are invalid.
     * @throws IllegalArgumentException
     *             If the principal and/or credential are null or empty, or if the expected argument(s) are not defined
     *             or are invalid
     * @throws IllegalStateException
     *             If the identity service is not available or cannot be created
     * @throws IOException
     *             if an I/O error occurs
     * @throws APPCException 
     */
    // @Ignore
    @Test
    public void testRestartRunningServer()
        throws IllegalStateException, IllegalArgumentException, ZoneException, IOException, APPCException {
        Properties properties = new Properties();
        properties.setProperty(ContextFactory.PROPERTY_IDENTITY_URL, IDENTITY_URL);
        properties.setProperty(ContextFactory.PROPERTY_REGION, REGION_NAME);
        properties.setProperty(ContextFactory.PROPERTY_TENANT, TENANT_NAME);
        properties.setProperty(ContextFactory.PROPERTY_TRUSTED_HOSTS, "*");
        properties.setProperty(ContextFactory.PROPERTY_DISABLE_PROXY, "true");

        try (Context context = ContextFactory.getContext(PROVIDER_TYPE, properties)) {
            context.login(PRINCIPAL, CREDENTIAL);
            VMURL vm = VMURL.parseURL(SERVER_URL);

            ComputeService computeService = context.getComputeService();
            Server server = computeService.getServer(vm.getServerId());
            if (!server.getStatus().equals(Status.RUNNING)) {
                server.start();
                assertTrue(waitForStateChange(server, Status.RUNNING));
            }

            Map<String, String> params = new HashMap<>();
            params.put(ProviderAdapter.PROPERTY_INSTANCE_URL, SERVER_URL);
            params.put(ProviderAdapter.PROPERTY_PROVIDER_NAME, PROVIDER_NAME);
            SvcLogicContext svcContext = new SvcLogicContext();

            server = adapter.restartServer(params, svcContext);

            assertEquals(Server.Status.RUNNING, server.getStatus());
        }
    }

    
    /****************************************/
    /**
     * Tests that the vmStatuschecker method works and returns the correct status of the VM requested
     * 
     * @throws ZoneException
     *             If the login cannot be performed because the principal and/or credentials are invalid.
     * @throws IllegalArgumentException
     *             If the principal and/or credential are null or empty, or if the expected argument(s) are not defined
     *             or are invalid
     * @throws IllegalStateException
     *             If the identity service is not available or cannot be created
     * @throws IOException
     *             if an I/O error occurs
     * @throws UnknownProviderException
     *             If the provider cannot be found
     */
    // @Ignore
    @Test
    public void testVmStatuschecker() throws IllegalStateException, IllegalArgumentException, ZoneException,
        UnknownProviderException, IOException {
        Properties properties = new Properties();
        properties.setProperty(ContextFactory.PROPERTY_IDENTITY_URL, IDENTITY_URL);
        properties.setProperty(ContextFactory.PROPERTY_REGION, REGION_NAME);
        properties.setProperty(ContextFactory.PROPERTY_TENANT, TENANT_NAME);
        properties.setProperty(ContextFactory.PROPERTY_TRUSTED_HOSTS, "*");
        properties.setProperty(ContextFactory.PROPERTY_DISABLE_PROXY, "true");

        try (Context context = ContextFactory.getContext(PROVIDER_TYPE, properties)) {
            context.login(PRINCIPAL, CREDENTIAL);
            VMURL vm = VMURL.parseURL(SERVER_URL);

            ComputeService computeService = context.getComputeService();
            Server server = computeService.getServer(vm.getServerId());
            if (!server.getStatus().equals(Status.RUNNING)) {
                server.start();
                assertTrue(waitForStateChange(server, Status.RUNNING));}
            //or instead of the if-block, can ensureRunning(server) be used?
            ensureRunning(server);
            assertEquals(Server.Status.RUNNING, server.getStatus());
        }   
    }
    /****************************************/
    
    
    /**
     * Tests that we can restart a server that is already stopped
     * 
     * @throws ZoneException
     *             If the login cannot be performed because the principal and/or credentials are invalid.
     * @throws IllegalArgumentException
     *             If the principal and/or credential are null or empty, or if the expected argument(s) are not defined
     *             or are invalid.
     * @throws IllegalStateException
     *             If the identity service is not available or cannot be created
     * @throws IOException
     *             if an I/O error occurs
     * @throws APPCException 
     */
    // @Ignore
    @Test
    public void testRestartStoppedServer()
        throws IllegalStateException, IllegalArgumentException, ZoneException, IOException, APPCException {
        Properties properties = new Properties();
        properties.setProperty(ContextFactory.PROPERTY_IDENTITY_URL, IDENTITY_URL);
        properties.setProperty(ContextFactory.PROPERTY_REGION, REGION_NAME);
        properties.setProperty(ContextFactory.PROPERTY_TENANT, TENANT_NAME);
        properties.setProperty(ContextFactory.PROPERTY_TRUSTED_HOSTS, "*");

        try (Context context = ContextFactory.getContext(PROVIDER_TYPE, properties)) {
            context.login(PRINCIPAL, CREDENTIAL);
            VMURL vm = VMURL.parseURL(SERVER_URL);

            ComputeService computeService = context.getComputeService();
            Server server = computeService.getServer(vm.getServerId());
            if (!server.getStatus().equals(Status.READY)) {
                server.stop();
                assertTrue(waitForStateChange(server, Status.READY));
            }

            Map<String, String> params = new HashMap<>();
            params.put(ProviderAdapter.PROPERTY_INSTANCE_URL, SERVER_URL);
            params.put(ProviderAdapter.PROPERTY_PROVIDER_NAME, PROVIDER_NAME);
            SvcLogicContext svcContext = new SvcLogicContext();

            server = adapter.restartServer(params, svcContext);

            assertEquals(Server.Status.RUNNING, server.getStatus());

        }
    }

    /**
     * Tests that we can rebuild a running server (not created from a bootable volume)
     * 
     * @throws ZoneException
     *             If the login cannot be performed because the principal and/or credentials are invalid.
     * @throws IllegalArgumentException
     *             If the principal and/or credential are null or empty, or if the expected argument(s) are not defined
     *             or are invalid.
     * @throws IllegalStateException
     *             If the identity service is not available or cannot be created
     * @throws UnknownProviderException
     *             If the provider cannot be found
     * @throws IOException
     *             if an I/O error occurs
     * @throws APPCException
     *             If the server cannot be rebuilt for some reason
     */
    // @Ignore
    @Test
    public void testRebuildRunningServer()
        throws IOException, IllegalStateException, IllegalArgumentException, ZoneException, APPCException {
        Properties properties = new Properties();
        properties.setProperty(ContextFactory.PROPERTY_IDENTITY_URL, IDENTITY_URL);
        properties.setProperty(ContextFactory.PROPERTY_REGION, REGION_NAME);
        properties.setProperty(ContextFactory.PROPERTY_TENANT, TENANT_NAME);
        properties.setProperty(ContextFactory.PROPERTY_TRUSTED_HOSTS, "*");

        try (Context context = ContextFactory.getContext(PROVIDER_TYPE, properties)) {
            context.login(PRINCIPAL, CREDENTIAL);
            VMURL vm = VMURL.parseURL(SERVER_URL);

            ComputeService computeService = context.getComputeService();
            Server server = computeService.getServer(vm.getServerId());
            ensureRunning(server);

            Map<String, String> params = new HashMap<>();
            params.put(ProviderAdapter.PROPERTY_INSTANCE_URL, SERVER_URL);
            params.put(ProviderAdapter.PROPERTY_PROVIDER_NAME, PROVIDER_NAME);
            SvcLogicContext svcContext = new SvcLogicContext();

            server = adapter.rebuildServer(params, svcContext);
            assertTrue(waitForStateChange(server, Status.RUNNING));

        }
    }

    /**
     * Tests that we can rebuild a paused server (not created from a bootable volume)
     * 
     * @throws ZoneException
     *             If the login cannot be performed because the principal and/or credentials are invalid.
     * @throws IllegalArgumentException
     *             If the principal and/or credential are null or empty, or if the expected argument(s) are not defined
     *             or are invalid.
     * @throws IllegalStateException
     *             If the identity service is not available or cannot be created
     * @throws UnknownProviderException
     *             If the provider cannot be found
     * @throws IOException
     *             if an I/O error occurs
     * @throws APPCException
     *             If the server cannot be rebuilt for some reason
     */
    // @Ignore
    @Test
    public void testRebuildPausedServer()
        throws IOException, IllegalStateException, IllegalArgumentException, ZoneException, APPCException {
        Properties properties = new Properties();
        properties.setProperty(ContextFactory.PROPERTY_IDENTITY_URL, IDENTITY_URL);
        properties.setProperty(ContextFactory.PROPERTY_REGION, REGION_NAME);
        properties.setProperty(ContextFactory.PROPERTY_TENANT, TENANT_NAME);
        properties.setProperty(ContextFactory.PROPERTY_TRUSTED_HOSTS, "*");

        try (Context context = ContextFactory.getContext(PROVIDER_TYPE, properties)) {
            context.login(PRINCIPAL, CREDENTIAL);
            VMURL vm = VMURL.parseURL(SERVER_URL);

            ComputeService computeService = context.getComputeService();
            Server server = computeService.getServer(vm.getServerId());
            ensurePaused(server);

            Map<String, String> params = new HashMap<>();
            params.put(ProviderAdapter.PROPERTY_INSTANCE_URL, SERVER_URL);
            params.put(ProviderAdapter.PROPERTY_PROVIDER_NAME, PROVIDER_NAME);
            SvcLogicContext svcContext = new SvcLogicContext();

            server = adapter.rebuildServer(params, svcContext);
            assertTrue(waitForStateChange(server, Status.RUNNING));
        }
    }

    /**
     * Tests that we can rebuild a paused server (not created from a bootable volume)
     * 
     * @throws ZoneException
     *             If the login cannot be performed because the principal and/or credentials are invalid.
     * @throws IllegalArgumentException
     *             If the principal and/or credential are null or empty, or if the expected argument(s) are not defined
     *             or are invalid.
     * @throws IllegalStateException
     *             If the identity service is not available or cannot be created
     * @throws UnknownProviderException
     *             If the provider cannot be found
     * @throws IOException
     *             if an I/O error occurs
     * @throws APPCException
     *             If the server cannot be rebuilt for some reason
     */
    // @Ignore
    @Test
    public void testRebuildSuspendedServer()
        throws IOException, IllegalStateException, IllegalArgumentException, ZoneException, APPCException {
        Properties properties = new Properties();
        properties.setProperty(ContextFactory.PROPERTY_IDENTITY_URL, IDENTITY_URL);
        properties.setProperty(ContextFactory.PROPERTY_REGION, REGION_NAME);
        properties.setProperty(ContextFactory.PROPERTY_TENANT, TENANT_NAME);
        properties.setProperty(ContextFactory.PROPERTY_TRUSTED_HOSTS, "*");

        try (Context context = ContextFactory.getContext(PROVIDER_TYPE, properties)) {
            context.login(PRINCIPAL, CREDENTIAL);
            VMURL vm = VMURL.parseURL(SERVER_URL);

            ComputeService computeService = context.getComputeService();
            Server server = computeService.getServer(vm.getServerId());
            ensureSuspended(server);

            Map<String, String> params = new HashMap<>();
            params.put(ProviderAdapter.PROPERTY_INSTANCE_URL, SERVER_URL);
            params.put(ProviderAdapter.PROPERTY_PROVIDER_NAME, PROVIDER_NAME);
            SvcLogicContext svcContext = new SvcLogicContext();

            server = adapter.rebuildServer(params, svcContext);
            assertTrue(waitForStateChange(server, Status.RUNNING));
        }
    }

    /**
     * Tests that we can rebuild a paused server (not created from a bootable volume)
     * 
     * @throws ZoneException
     *             If the login cannot be performed because the principal and/or credentials are invalid.
     * @throws IllegalArgumentException
     *             If the principal and/or credential are null or empty, or if the expected argument(s) are not defined
     *             or are invalid.
     * @throws IllegalStateException
     *             If the identity service is not available or cannot be created
     * @throws UnknownProviderException
     *             If the provider cannot be found
     * @throws IOException
     *             if an I/O error occurs
     * @throws APPCException
     *             If the server cannot be rebuilt for some reason
     */
    // @Ignore
    @Test
    public void testRebuildStoppedServer()
        throws IOException, IllegalStateException, IllegalArgumentException, ZoneException, APPCException {
        Properties properties = new Properties();
        properties.setProperty(ContextFactory.PROPERTY_IDENTITY_URL, IDENTITY_URL);
        properties.setProperty(ContextFactory.PROPERTY_REGION, REGION_NAME);
        properties.setProperty(ContextFactory.PROPERTY_TENANT, TENANT_NAME);
        properties.setProperty(ContextFactory.PROPERTY_TRUSTED_HOSTS, "*");

        try (Context context = ContextFactory.getContext(PROVIDER_TYPE, properties)) {
            context.login(PRINCIPAL, CREDENTIAL);
            VMURL vm = VMURL.parseURL(SERVER_URL);

            ComputeService computeService = context.getComputeService();
            Server server = computeService.getServer(vm.getServerId());
            ensureStopped(server);

            Map<String, String> params = new HashMap<>();
            params.put(ProviderAdapter.PROPERTY_INSTANCE_URL, SERVER_URL);
            params.put(ProviderAdapter.PROPERTY_PROVIDER_NAME, PROVIDER_NAME);
            SvcLogicContext svcContext = new SvcLogicContext();

            server = adapter.rebuildServer(params, svcContext);
            assertTrue(waitForStateChange(server, Status.RUNNING));
        }
    }

    /**
     * Test subsequent action on second vm in different Tenant resulting in {"itemNotFound": {"message": "Instance could not be found", "code": 404}}
     * 
     * @throws ZoneException
     *             If the login cannot be performed because the principal and/or credentials are invalid.
     * @throws IllegalArgumentException
     *             If the principal and/or credential are null or empty, or if the expected argument(s) are not defined
     *             or are invalid
     * @throws IllegalStateException
     *             If the identity service is not available or cannot be created
     * @throws IOException
     *             if an I/O error occurs
     * @throws APPCException 
     */

    @Test
    public void testTenantVerification() throws IllegalStateException, IllegalArgumentException, ZoneException,
        IOException, APPCException {
        
        Properties properties = new Properties();
        properties.setProperty(ContextFactory.PROPERTY_IDENTITY_URL, "http://example.com:5000");
        properties.setProperty(ContextFactory.PROPERTY_TENANT, "APP-C");
        properties.setProperty(ContextFactory.PROPERTY_TRUSTED_HOSTS, "*");

        String vmUrl =
            "http://192.168.1.2:8774/v2/abcde12345fghijk6789lmnopq123rst/servers/abc12345-1234-5678-890a-abcdefg12345";

        //try (Context context = ContextFactory.getContext(PROVIDER_TYPE, properties)) {
        //    context.login("AppC", "AppC");
            
            // call lookupServer on vm in defined tenant "APP-C_TLV"
            VMURL vm = VMURL.parseURL(vmUrl);

            Map<String, String> params = new HashMap<>();
            params.put(ProviderAdapter.PROPERTY_INSTANCE_URL, vmUrl);
            params.put(ProviderAdapter.PROPERTY_IDENTITY_URL, "http://example.com:5000/v2.0");
            params.put(ProviderAdapter.PROPERTY_PROVIDER_NAME, "http://example.com:5000/v2.0");
            SvcLogicContext svcContext = new SvcLogicContext();
            
            long start, end = 0;

            System.out.println("\n--------------------Begin lookupServer on tenant 1--------------------");          
            start = System.currentTimeMillis();
            Server server = adapter.lookupServer(params, svcContext);
            end = System.currentTimeMillis();
            
            System.out.println(String.format("lookupServer on tenant 1 took %ds", (end - start) / 1000));
            System.out.println("----------------------------------------------------------------------\n");
            assertNotNull(server);
            
            //repeat to show that context is reused for second request
            System.out.println("\n-----------------Begin repeat lookupServer on tenant 1----------------");               
            start = System.currentTimeMillis();
            server = adapter.lookupServer(params, svcContext);
            end = System.currentTimeMillis();
            
            System.out.println(String.format("Repeat lookupServer on tenant 1 took %ds", (end - start) / 1000));
            System.out.println("----------------------------------------------------------------------\n");
            assertNotNull(server);
            
            // call lookupServer on vm in second tenant "Play"
            // This is where we would fail due to using the previous
            // tenants context
            vmUrl = "http://192.168.1.2:8774/v2/abcde12345fghijk6789lmnopq123rst/servers/abc12345-1234-5678-890a-abcdefg12345";
            vm = VMURL.parseURL(vmUrl);
            params.put(ProviderAdapter.PROPERTY_INSTANCE_URL, vmUrl);
            
            System.out.println("\n--------------------Begin lookupServer on tenant 2--------------------");
            start = System.currentTimeMillis();
            server = adapter.lookupServer(params, svcContext);
            end = System.currentTimeMillis();
            System.out.println(String.format("\nlookupServer on tenant 2 took %ds", (end - start) / 1000));
            System.out.println("----------------------------------------------------------------------\n");
            assertNotNull(server);
            
            // call lookupServer on vm in non-existing tenant
            vmUrl = "http://192.168.1.2:8774/v2/abcde12345fghijk6789lmnopq123rst/servers/abc12345-1234-5678-890a-abcdefg12345";
            vm = VMURL.parseURL(vmUrl);
            params.put(ProviderAdapter.PROPERTY_INSTANCE_URL, vmUrl);
            
            System.out.println("\n--------------Begin lookupServer on non-existant tenant--------------");
            start = System.currentTimeMillis();
            server = adapter.lookupServer(params, svcContext);
            end = System.currentTimeMillis();            
            System.out.println(String.format("\nlookupServer on tenant 3 took %ds", (end - start) / 1000));
            System.out.println("----------------------------------------------------------------------\n");
            assertNull(server);
            
        //}
    }
    /****************************************/

    
    @Test
    public void testSnapshotServer() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(ContextFactory.PROPERTY_IDENTITY_URL, "http://example.com:5000");
        // properties.setProperty(ContextFactory.PROPERTY_REGION, "");
        properties.setProperty(ContextFactory.PROPERTY_TENANT, "Play");
        properties.setProperty(ContextFactory.PROPERTY_TRUSTED_HOSTS, "*");

        String vmUrl =
            "http://192.168.1.2:8774/v2/abcde12345fghijk6789lmnopq123rst/servers/abc12345-1234-5678-890a-abcdefg12345";

        try (Context context = ContextFactory.getContext(PROVIDER_TYPE, properties)) {
            context.login("AppC", "AppC");
            VMURL vm = VMURL.parseURL(vmUrl);

            Map<String, String> params = new HashMap<>();
            params.put(ProviderAdapter.PROPERTY_INSTANCE_URL, vmUrl);
            params.put(ProviderAdapter.PROPERTY_IDENTITY_URL, "http://example.com:5000/v2.0");
            params.put(ProviderAdapter.PROPERTY_PROVIDER_NAME, "http://example.com:5000/v2.0");
            SvcLogicContext svcContext = new SvcLogicContext();

            long start, end = 0;

            start = System.currentTimeMillis();
            Image image = adapter.createSnapshot(params, svcContext);
            end = System.currentTimeMillis();

            System.out.println(String.format("Image ID: %s", image.getId()));
            System.out.println(String.format("Snapshot took %ds", (end - start) / 1000));

            start = System.currentTimeMillis();
            adapter.rebuildServer(params, svcContext);
            end = System.currentTimeMillis();
            System.out.println(String.format("Rebuild took %ds", (end - start) / 1000));
        }

    }

    /**
     * Ensures that the server is in stopped (shutdown) state prior to test
     * 
     * @param server
     *            The server to ensure is stopped
     * @throws ZoneException
     *             If the server can't be operated upon for some reason
     */
    @SuppressWarnings("nls")
    private static void ensureStopped(Server server) throws ZoneException {
        switch (server.getStatus()) {
            case READY:
                break;

            case PENDING:
                waitForStateChange(server, Server.Status.READY, Server.Status.RUNNING, Server.Status.PAUSED,
                    Server.Status.SUSPENDED, Server.Status.ERROR);
                ensureSuspended(server);
                break;

            case PAUSED:
                server.unpause();
                waitForStateChange(server, Server.Status.RUNNING);
                server.stop();
                waitForStateChange(server, Server.Status.READY);
                break;

            case SUSPENDED:
                server.resume();
                waitForStateChange(server, Server.Status.RUNNING);
                server.stop();
                waitForStateChange(server, Server.Status.READY);
                break;

            case RUNNING:
                server.stop();
                waitForStateChange(server, Server.Status.READY);
                break;

            case DELETED:
            case ERROR:
            default:
                fail("Server state is not valid for test - " + server.getStatus().name());
        }
    }

    /**
     * Ensures that the server is in suspended state prior to test
     * 
     * @param server
     *            The server to ensure is suspended
     * @throws ZoneException
     *             If the server can't be operated upon for some reason
     */
    @SuppressWarnings("nls")
    private static void ensureSuspended(Server server) throws ZoneException {
        switch (server.getStatus()) {
            case SUSPENDED:
                break;

            case PENDING:
                waitForStateChange(server, Server.Status.READY, Server.Status.RUNNING, Server.Status.PAUSED,
                    Server.Status.SUSPENDED, Server.Status.ERROR);
                ensureSuspended(server);
                break;

            case PAUSED:
                server.unpause();
                waitForStateChange(server, Server.Status.RUNNING);
                server.suspend();
                waitForStateChange(server, Server.Status.SUSPENDED);
                break;

            case READY:
                server.start();
                waitForStateChange(server, Server.Status.RUNNING);
                server.suspend();
                waitForStateChange(server, Server.Status.SUSPENDED);
                break;

            case RUNNING:
                server.suspend();
                waitForStateChange(server, Server.Status.SUSPENDED);
                break;

            case DELETED:
            case ERROR:
            default:
                fail("Server state is not valid for test - " + server.getStatus().name());
        }
    }

    /**
     * This method makes sure that the indicated server is running before performing a test
     * 
     * @param server
     *            The server to ensure is running
     * @throws ZoneException
     *             If the server can't be operated upon
     */
    @SuppressWarnings("nls")
    private static void ensureRunning(Server server) throws ZoneException {
        switch (server.getStatus()) {
            case RUNNING:
                break;

            case PENDING:
                waitForStateChange(server, Server.Status.READY, Server.Status.RUNNING, Server.Status.PAUSED,
                    Server.Status.SUSPENDED, Server.Status.ERROR);
                ensureRunning(server);
                break;

            case PAUSED:
                server.unpause();
                waitForStateChange(server, Server.Status.RUNNING);
                break;

            case SUSPENDED:
                server.resume();
                waitForStateChange(server, Server.Status.RUNNING);
                break;

            case READY:
                server.start();
                waitForStateChange(server, Server.Status.RUNNING);
                break;

            case DELETED:
            case ERROR:
            default:
                fail("Server state is not valid for test - " + server.getStatus().name());
        }
    }

    /**
     * This method will make sure that the server we are testing is paused
     * 
     * @param server
     *            The server to make sure is paused for the test
     * @throws ZoneException
     *             If anything fails
     */
    @SuppressWarnings("nls")
    private static void ensurePaused(Server server) throws ZoneException {
        switch (server.getStatus()) {
            case PAUSED:
                break;

            case PENDING:
                waitForStateChange(server, Server.Status.READY, Server.Status.RUNNING, Server.Status.PAUSED,
                    Server.Status.SUSPENDED, Server.Status.ERROR);
                ensurePaused(server);
                break;

            case READY:
                server.start();
                waitForStateChange(server, Server.Status.RUNNING);
                server.pause();
                waitForStateChange(server, Server.Status.PAUSED);
                break;

            case RUNNING:
                server.pause();
                waitForStateChange(server, Server.Status.PAUSED);
                break;

            case SUSPENDED:
                server.resume();
                waitForStateChange(server, Server.Status.RUNNING);
                server.pause();
                waitForStateChange(server, Server.Status.PAUSED);
                break;

            case ERROR:
            case DELETED:
            default:
                fail("Server state is not valid for test - " + server.getStatus().name());
        }
    }

    /**
     * Enter a pool-wait loop checking the server state to see if it has entered one of the desired states or not.
     * <p>
     * This method checks the state of the server periodically for one of the desired states. When the server enters one
     * of the desired states, the method returns a successful indication (true). If the server never enters one of the
     * desired states within the alloted timeout period, then the method returns a failed response (false). No
     * exceptions are thrown from this method.
     * </p>
     * 
     * @param server
     *            The server to wait on
     * @param desiredStates
     *            A variable list of desired states, any one of which is allowed.
     * @return True if the server entered one of the desired states, and false if not and the wait loop timed out.
     */
    private static boolean waitForStateChange(Server server, Server.Status... desiredStates) {
        int timeout =
            ConfigurationFactory.getConfiguration().getIntegerProperty(Constants.PROPERTY_SERVER_STATE_CHANGE_TIMEOUT);
        long limit = System.currentTimeMillis() + (timeout * 1000);
        Server vm = server;

        try {
            while (limit > System.currentTimeMillis()) {
                vm.refresh();
                for (Server.Status desiredState : desiredStates) {
                    if (server.getStatus().equals(desiredState)) {
                        return true;
                    }
                }

                try {
                    Thread.sleep(10000L);
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        } catch (ZoneException e) {
            e.printStackTrace();
        }

        return false;
    }

    /*
     * @Test public void testTerminateStack() throws IllegalStateException, IllegalArgumentException, ZoneException,
     * UnknownProviderException, IOException { Properties properties = new Properties();
     * properties.setProperty(ContextFactory.PROPERTY_IDENTITY_URL, IDENTITY_URL);
     * properties.setProperty(ContextFactory.PROPERTY_REGION, REGION_NAME);
     * properties.setProperty(ContextFactory.PROPERTY_TENANT, TENANT_NAME);
     * properties.setProperty(ContextFactory.PROPERTY_TRUSTED_HOSTS, "*");
     * properties.setProperty(ContextFactory.PROPERTY_DISABLE_PROXY, "true"); try (Context context =
     * ContextFactory.getContext(PROVIDER_TYPE, properties)) { context.login(PRINCIPAL, CREDENTIAL); VMURL vm =
     * VMURL.parseURL(SERVER_URL); ComputeService computeService = context.getComputeService(); Server server =
     * computeService.getServer(vm.getServerId()); if (!server.getStatus().equals(Status.RUNNING)) { server.start();
     * assertTrue(waitForStateChange(server, Status.RUNNING)); } Map<String, String> params = new HashMap<>();
     * params.put(ProviderAdapter.PROPERTY_INSTANCE_URL, SERVER_URL); params.put(ProviderAdapter.PROPERTY_PROVIDER_NAME,
     * PROVIDER_NAME); SvcLogicContext svcContext = new SvcLogicContext(); Stack stack = adapter.terminateStack(params,
     * svcContext); assertNotNull(stack); } }
     */

}
