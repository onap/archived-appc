/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017-2018 Amdocs
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

package org.onap.appc.adapter.iaas.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.appc.adapter.iaas.ProviderAdapter;
import org.onap.appc.adapter.iaas.provider.operation.api.IProviderOperation;
import org.onap.appc.adapter.iaas.provider.operation.api.ProviderOperationFactory;
import org.onap.appc.adapter.iaas.provider.operation.common.enums.Operation;
import org.onap.appc.adapter.iaas.provider.operation.impl.EvacuateServer;
import org.onap.appc.configuration.ConfigurationFactory;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.exceptions.UnknownProviderException;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

import com.att.cdp.exceptions.ZoneException;
import com.att.cdp.zones.model.Image;
import com.att.cdp.zones.model.Server;
import com.att.cdp.zones.model.Stack;
import com.google.common.collect.ImmutableMap;

/**
 * This class is used to test methods and functions of the adapter implementation that do not
 * require and do not set up connections to any providers.
 */
@RunWith(MockitoJUnitRunner.class)
@Category(org.onap.appc.adapter.iaas.impl.TestProviderAdapterImpl.class)
public class TestProviderAdapterImpl {

    @SuppressWarnings("nls")
    private static final String PROVIDER_NAME = "ILAB";

    @SuppressWarnings("nls")
    private static final String PROVIDER_TYPE = "OpenStackProvider";

    private static String IDENTITY_URL;

    private static String SERVER_URL;
    
    private static String SERVER_ID;

    private static Class<?> providerAdapterImplClass;
    private static Class<?> configurationFactoryClass;
    private static Field providerCacheField;
    private static Field configField;

    private static ProviderAdapterImpl adapter;
    
    @Mock
    ProviderOperationFactory factory;
    
    @Mock
    IProviderOperation providerOperation;

    @Mock
    EvacuateServer evacuateServer;
    
    private Map<String,ProviderCache> providerCache;
    
    /**
     * Use reflection to locate fields and methods so that they can be manipulated during the test
     * to change the internal state accordingly.
     * 
     * @throws NoSuchFieldException if the field(s) dont exist
     * @throws SecurityException if reflective access is not allowed
     * @throws NoSuchMethodException If the method(s) dont exist
     */
    @SuppressWarnings("nls")
    @BeforeClass
    public static void once() throws NoSuchFieldException, SecurityException, NoSuchMethodException {
        providerAdapterImplClass = ProviderAdapterImpl.class;
        configurationFactoryClass = ConfigurationFactory.class;

        providerCacheField = providerAdapterImplClass.getDeclaredField("providerCache");
        providerCacheField.setAccessible(true);
        Properties props = ConfigurationFactory.getConfiguration().getProperties();
        IDENTITY_URL = props.getProperty("provider1.identity");
        SERVER_URL = props.getProperty("test.url");
        configField = configurationFactoryClass.getDeclaredField("config");
        configField.setAccessible(true);
		SERVER_ID = "server1";
    }

    /**
     * Use reflection to locate fields and methods so that they can be manipulated during the test
     * to change the internal state accordingly.
     * 
     * @throws IllegalAccessException if this Field object is enforcing Java language access control
     *         and the underlying field is either inaccessible or final.
     * @throws IllegalArgumentException if the specified object is not an instance of the class or
     *         interface declaring the underlying field (or a subclass or implementor thereof), or
     *         if an unwrapping conversion fails.
     */
    @Before
    public void setup() throws IllegalArgumentException, IllegalAccessException {
        configField.set(null, null);
        Properties properties = new Properties();
        adapter = new ProviderAdapterImpl(properties);
		Map<String, Object> privateFields = ImmutableMap.<String, Object>builder().put("factory", factory)
				.put("providerCache",getProviderCache()).build();
		injectMockObjects(privateFields);
    }
    
	private static void injectMockObjects(Map<String, Object> privateFields) {
		final Object catalogObject = (Object) adapter;
		privateFields.forEach((fieldName, fieldInstance) -> {
			try {
				Field privateField = catalogObject.getClass().getDeclaredField(fieldName);
				privateField.setAccessible(true);
				privateField.set(catalogObject, fieldInstance);
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				// Exception occurred while accessing the private fields
			}
		});
	}
	
	private Map<String, ProviderCache> getProviderCache() {
		providerCache = new HashMap<String, ProviderCache>();
		ProviderCache cache = new ProviderCache();
		cache.setIdentityURL(IDENTITY_URL);
		cache.setProviderName(PROVIDER_NAME);
		cache.setProviderType(PROVIDER_TYPE);
		providerCache.put(cache.getIdentityURL(), cache);
		return providerCache;
	}
	
    /**
     * This test case is used to invoke the default constructor
     * 
     */
	@Test
	public void testDefaultConstructor() {
		ProviderAdapter adapter = new ProviderAdapterImpl();
		assertNotNull(adapter);
	}
	
    /**
     * This test case is used to invoke the argument constructor
     * 
     */
	@Test
	public void testArgumentedConstructor(){
		ProviderAdapter adapter = new ProviderAdapterImpl(true);
		assertNotNull(adapter);
	}
	
	/**
	 * Tests that we get the Adapter name
	 */
	@Test
	public void testAdapterName() {
		assertNotNull(adapter.getAdapterName());
	}

    /**
     * This test case is used to restart the server
     * 
     * @throws ZoneException If the login cannot be performed because the principal and/or
     *         credentials are invalid.
     * @throws IllegalArgumentException If the principal and/or credential are null or empty, or if
     *         the expected argument(s) are not defined or are invalid
     * @throws IllegalStateException If the identity service is not available or cannot be created
     * @throws IOException if an I/O error occurs
     * @throws APPCException If the server cannot be restarted for some reason
     */
	@Test
	public void testRestartServer()
			throws IllegalStateException, IllegalArgumentException, ZoneException, IOException, APPCException {
		when(factory.getOperationObject(Operation.RESTART_SERVICE)).thenReturn(providerOperation);
		Server expectedServer = new Server();
		expectedServer.setStatus(Server.Status.RUNNING);
		when(providerOperation.doOperation(anyObject(), anyObject())).thenReturn(expectedServer);
		Map<String, String> params = new HashMap<>();
		params.put(ProviderAdapter.PROPERTY_INSTANCE_URL, SERVER_URL);
		params.put(ProviderAdapter.PROPERTY_PROVIDER_NAME, PROVIDER_NAME);
		SvcLogicContext svcContext = new SvcLogicContext();
		Server actualServer = adapter.restartServer(params, svcContext);
		assertEquals(Server.Status.RUNNING, actualServer.getStatus());
	}
	
    /**
     * This test case is used to start the server
     * 
     * @throws ZoneException If the login cannot be performed because the principal and/or
     *         credentials are invalid.
     * @throws IllegalArgumentException If the principal and/or credential are null or empty, or if
     *         the expected argument(s) are not defined or are invalid
     * @throws IllegalStateException If the identity service is not available or cannot be created
     * @throws IOException if an I/O error occurs
     * @throws APPCException If the server cannot be started for some reason
     */
	@Test
	public void testStartServer()
			throws IllegalStateException, IllegalArgumentException, ZoneException, IOException, APPCException {
		when(factory.getOperationObject(Operation.START_SERVICE)).thenReturn(providerOperation);
		Server actualServer = new Server();
		actualServer.setStatus(Server.Status.RUNNING);
		when(providerOperation.doOperation(anyObject(), anyObject())).thenReturn(actualServer);
		Map<String, String> params = new HashMap<>();
		params.put(ProviderAdapter.PROPERTY_INSTANCE_URL, SERVER_URL);
		params.put(ProviderAdapter.PROPERTY_PROVIDER_NAME, PROVIDER_NAME);
		SvcLogicContext svcContext = new SvcLogicContext();
		Server expectedServer = adapter.startServer(params, svcContext);
		assertEquals(Server.Status.RUNNING, expectedServer.getStatus());
	}
	
    /**
     * This test case is used to stop the server
     * 
     * @throws ZoneException If the login cannot be performed because the principal and/or
     *         credentials are invalid.
     * @throws IllegalArgumentException If the principal and/or credential are null or empty, or if
     *         the expected argument(s) are not defined or are invalid
     * @throws IllegalStateException If the identity service is not available or cannot be created
     * @throws IOException if an I/O error occurs
     * @throws APPCException If the server cannot be stopped for some reason
     */
	@Test
	public void testStopServer()
			throws IllegalStateException, IllegalArgumentException, ZoneException, IOException, APPCException {
		when(factory.getOperationObject(Operation.STOP_SERVICE)).thenReturn(providerOperation);
		Server expectedServer = new Server();
		expectedServer.setStatus(Server.Status.READY);
		Map<String, String> params = new HashMap<>();
		params.put(ProviderAdapter.PROPERTY_INSTANCE_URL, SERVER_URL);
		params.put(ProviderAdapter.PROPERTY_PROVIDER_NAME, PROVIDER_NAME);
		SvcLogicContext svcContext = new SvcLogicContext();
		when(providerOperation.doOperation(params, svcContext)).thenReturn(expectedServer);
		Server actualServer = adapter.stopServer(params, svcContext);
		assertEquals(Server.Status.READY, actualServer.getStatus());
	}

    /**
     * Tests that the vmStatuschecker method works and returns the correct status of the VM
     * requested
     * 
     * @throws ZoneException If the login cannot be performed because the principal and/or
     *         credentials are invalid.
     * @throws IllegalArgumentException If the principal and/or credential are null or empty, or if
     *         the expected argument(s) are not defined or are invalid
     * @throws IllegalStateException If the identity service is not available or cannot be created
     * @throws IOException if an I/O error occurs
     * @throws APPCException If the vm status can not be verified
     */
	@Test
	public void testVmStatuschecker()
			throws IllegalStateException, IllegalArgumentException, ZoneException, IOException, APPCException {
		when(factory.getOperationObject(Operation.VMSTATUSCHECK_SERVICE)).thenReturn(providerOperation);
		Server actualServer = new Server();
		actualServer.setStatus(Server.Status.READY);
		when(providerOperation.doOperation(anyObject(), anyObject())).thenReturn(actualServer);
		Map<String, String> params = new HashMap<>();
		params.put(ProviderAdapter.PROPERTY_INSTANCE_URL, SERVER_URL);
		params.put(ProviderAdapter.PROPERTY_PROVIDER_NAME, PROVIDER_NAME);
		SvcLogicContext svcContext = new SvcLogicContext();
		Server expectedServer = adapter.vmStatuschecker(params, svcContext);
		assertEquals(Server.Status.READY, expectedServer.getStatus());
	}
    
    /**
     * Tests that the terminate stack method works
     * 
     * @throws ZoneException If the login cannot be performed because the principal and/or
     *         credentials are invalid.
     * @throws IllegalArgumentException If the principal and/or credential are null or empty, or if
     *         the expected argument(s) are not defined or are invalid
     * @throws IllegalStateException If the identity service is not available or cannot be created
     * @throws IOException if an I/O error occurs
     * @throws APPCException If the stack cannot be terminated for some reason
     */
	@Test
	public void testTerminateStack()
			throws IllegalStateException, IllegalArgumentException, ZoneException, IOException, APPCException {
		Stack stack = new Stack();
		stack.setStatus(Stack.Status.DELETED);
		when(factory.getOperationObject(Operation.TERMINATE_STACK)).thenReturn(providerOperation);
		when(providerOperation.doOperation(anyObject(), anyObject())).thenReturn(stack);
		Map<String, String> params = new HashMap<>();
		params.put(ProviderAdapter.PROPERTY_INSTANCE_URL, SERVER_URL);
		params.put(ProviderAdapter.PROPERTY_PROVIDER_NAME, PROVIDER_NAME);
		SvcLogicContext svcContext = new SvcLogicContext();
		Stack actualStack = adapter.terminateStack(params, svcContext);
		assertEquals(Stack.Status.DELETED, actualStack.getStatus());
	}
    
    /**
     * Tests that the snapshot method works and returns snapshot of the stack
     * 
     * @throws ZoneException If the login cannot be performed because the principal and/or
     *         credentials are invalid.
     * @throws IllegalArgumentException If the principal and/or credential are null or empty, or if
     *         the expected argument(s) are not defined or are invalid
     * @throws IllegalStateException If the identity service is not available or cannot be created
     * @throws IOException if an I/O error occurs
     * @throws APPCException If the stack snapshot can not be taken for some reason
     */
	@Test
	public void testSnapshotStack()
			throws IllegalStateException, IllegalArgumentException, ZoneException, IOException, APPCException {
		Stack stack = new Stack();
		stack.setStatus(Stack.Status.ACTIVE);
		when(factory.getOperationObject(Operation.SNAPSHOT_STACK)).thenReturn(providerOperation);
		when(providerOperation.doOperation(anyObject(), anyObject())).thenReturn(stack);
		Map<String, String> params = new HashMap<>();
		params.put(ProviderAdapter.PROPERTY_INSTANCE_URL, SERVER_URL);
		params.put(ProviderAdapter.PROPERTY_PROVIDER_NAME, PROVIDER_NAME);
		SvcLogicContext svcContext = new SvcLogicContext();
		Stack actualStack = adapter.snapshotStack(params, svcContext);
		assertEquals(Stack.Status.ACTIVE, actualStack.getStatus());
	}
    
    /**
     * Tests that the restore method works and returns restored stack
     * 
     * @throws ZoneException If the login cannot be performed because the principal and/or
     *         credentials are invalid.
     * @throws IllegalArgumentException If the principal and/or credential are null or empty, or if
     *         the expected argument(s) are not defined or are invalid
     * @throws IllegalStateException If the identity service is not available or cannot be created
     * @throws IOException if an I/O error occurs
     * @throws APPCException If the stack cannot be restored for some reason
     */
	@Test
	public void testRestoreStack()
			throws IllegalStateException, IllegalArgumentException, ZoneException, IOException, APPCException {
		Stack stack = new Stack();
		stack.setStatus(Stack.Status.ACTIVE);
		when(factory.getOperationObject(Operation.RESTORE_STACK)).thenReturn(providerOperation);
		when(providerOperation.doOperation(anyObject(), anyObject())).thenReturn(stack);
		Map<String, String> params = new HashMap<>();
		params.put(ProviderAdapter.PROPERTY_INSTANCE_URL, SERVER_URL);
		params.put(ProviderAdapter.PROPERTY_PROVIDER_NAME, PROVIDER_NAME);
		SvcLogicContext svcContext = new SvcLogicContext();
		Stack actualStack = adapter.restoreStack(params, svcContext);
		assertEquals(Stack.Status.ACTIVE, actualStack.getStatus());
	}
    
    /**
     * Tests that the lookup server will lookup for the server with server id
     * 
     * @throws ZoneException If the login cannot be performed because the principal and/or
     *         credentials are invalid.
     * @throws IllegalArgumentException If the principal and/or credential are null or empty, or if
     *         the expected argument(s) are not defined or are invalid
     * @throws IllegalStateException If the identity service is not available or cannot be created
     * @throws IOException if an I/O error occurs
     * @throws APPCException If the server cannot be found for some reason
     */
	@Test
	public void testLookupServer()
			throws IllegalStateException, IllegalArgumentException, ZoneException, IOException, APPCException {
		when(factory.getOperationObject(Operation.LOOKUP_SERVICE)).thenReturn(providerOperation);
		Server expectedServer = new Server();
		expectedServer.setStatus(Server.Status.READY);
		expectedServer.setId(SERVER_ID);
		Map<String, String> params = new HashMap<>();
		params.put(ProviderAdapter.PROPERTY_INSTANCE_URL, SERVER_URL);
		params.put(ProviderAdapter.PROPERTY_PROVIDER_NAME, PROVIDER_NAME);
		SvcLogicContext svcContext = new SvcLogicContext();
		when(providerOperation.doOperation(params, svcContext)).thenReturn(expectedServer);
		Server actualServer = adapter.lookupServer(params, svcContext);
		assertEquals(expectedServer.getId(), actualServer.getId());
	}
    
    /**
     * Tests that the to create a snapshot and return a image 
     * 
     * @throws ZoneException If the login cannot be performed because the principal and/or
     *         credentials are invalid.
     * @throws IllegalArgumentException If the principal and/or credential are null or empty, or if
     *         the expected argument(s) are not defined or are invalid
     * @throws IllegalStateException If the identity service is not available or cannot be created
     * @throws IOException if an I/O error occurs
     * @throws APPCException If the image snapshot can not be taken for some reason
     */
	@Test
	public void testCreateSnapshot()
			throws IllegalStateException, IllegalArgumentException, ZoneException, IOException, APPCException {
		when(factory.getOperationObject(Operation.SNAPSHOT_SERVICE)).thenReturn(providerOperation);
		Image expectedImage = new Image();
		expectedImage.setStatus(Image.Status.ACTIVE);
		Map<String, String> params = new HashMap<>();
		params.put(ProviderAdapter.PROPERTY_INSTANCE_URL, SERVER_URL);
		params.put(ProviderAdapter.PROPERTY_PROVIDER_NAME, PROVIDER_NAME);
		SvcLogicContext svcContext = new SvcLogicContext();
		when(providerOperation.doOperation(params, svcContext)).thenReturn(expectedImage);
		Image actualImage = adapter.createSnapshot(params, svcContext);
		assertEquals(expectedImage.getStatus(), actualImage.getStatus());
	}
    
    /**
     * Tests that the to calculate the server volume and attach it to the server
     * 
     * @throws ZoneException If the login cannot be performed because the principal and/or
     *         credentials are invalid.
     * @throws IllegalArgumentException If the principal and/or credential are null or empty, or if
     *         the expected argument(s) are not defined or are invalid
     * @throws IllegalStateException If the identity service is not available or cannot be created
     * @throws IOException if an I/O error occurs
     * @throws APPCException If the Server volume can not be calculated for some reason
     */
	@Test
	public void testAttachVolume()
			throws IllegalStateException, IllegalArgumentException, ZoneException, IOException, APPCException {
		when(factory.getOperationObject(Operation.ATTACHVOLUME_SERVICE)).thenReturn(providerOperation);
		Server expectedServer = new Server();
		expectedServer.setStatus(Server.Status.READY);
		expectedServer.setId(SERVER_ID);
		Map<String, String> params = new HashMap<>();
		params.put(ProviderAdapter.PROPERTY_INSTANCE_URL, SERVER_URL);
		params.put(ProviderAdapter.PROPERTY_PROVIDER_NAME, PROVIDER_NAME);
		SvcLogicContext svcContext = new SvcLogicContext();
		when(providerOperation.doOperation(params, svcContext)).thenReturn(expectedServer);
		Server actualServer = adapter.attachVolume(params, svcContext);
		assertEquals(expectedServer.getId(), actualServer.getId());
	}
    
    /**
     * Tests that the to detach the calculated volume from the server
     * 
     * @throws ZoneException If the login cannot be performed because the principal and/or
     *         credentials are invalid.
     * @throws IllegalArgumentException If the principal and/or credential are null or empty, or if
     *         the expected argument(s) are not defined or are invalid
     * @throws IllegalStateException If the identity service is not available or cannot be created
     * @throws IOException if an I/O error occurs
     * @throws APPCException If the Server volume can not be detached for some reason
     */
	@Test
	public void testDettachVolume()
			throws IllegalStateException, IllegalArgumentException, ZoneException, IOException, APPCException {
		when(factory.getOperationObject(Operation.DETACHVOLUME_SERVICE)).thenReturn(providerOperation);
		Server expectedServer = new Server();
		expectedServer.setStatus(Server.Status.READY);
		expectedServer.setId(SERVER_ID);
		Map<String, String> params = new HashMap<>();
		params.put(ProviderAdapter.PROPERTY_INSTANCE_URL, SERVER_URL);
		params.put(ProviderAdapter.PROPERTY_PROVIDER_NAME, PROVIDER_NAME);
		SvcLogicContext svcContext = new SvcLogicContext();
		when(providerOperation.doOperation(params, svcContext)).thenReturn(expectedServer);
		Server actualServer = adapter.dettachVolume(params, svcContext);
		assertEquals(expectedServer.getId(), actualServer.getId());
	}

    /****************************************/


    /**
     * Tests that we can restart a server that is already stopped
     * 
     * @throws ZoneException If the login cannot be performed because the principal and/or
     *         credentials are invalid.
     * @throws IllegalArgumentException If the principal and/or credential are null or empty, or if
     *         the expected argument(s) are not defined or are invalid.
     * @throws IllegalStateException If the identity service is not available or cannot be created
     * @throws IOException if an I/O error occurs
     * @throws APPCException If the server cannot be restarted for some reason
     */
    @Test
    public void testRestartStoppedServer()
            throws IllegalStateException, IllegalArgumentException, ZoneException, IOException, APPCException {
		when(factory.getOperationObject(Operation.RESTART_SERVICE)).thenReturn(providerOperation);
		Server expectedServer = new Server();
		expectedServer.setStatus(Server.Status.RUNNING);
		expectedServer.setId(SERVER_ID);
		Map<String, String> params = new HashMap<>();
		params.put(ProviderAdapter.PROPERTY_INSTANCE_URL, SERVER_URL);
		params.put(ProviderAdapter.PROPERTY_PROVIDER_NAME, PROVIDER_NAME);
		SvcLogicContext svcContext = new SvcLogicContext();
		when(providerOperation.doOperation(params, svcContext)).thenReturn(expectedServer);
		Server actualServer = adapter.restartServer(params, svcContext);
		assertEquals(Server.Status.RUNNING, actualServer.getStatus());
        
    }

    /**
     * Tests that we can rebuild a server (not created from a bootable volume)
     * 
     * @throws ZoneException If the login cannot be performed because the principal and/or
     *         credentials are invalid.
     * @throws IllegalArgumentException If the principal and/or credential are null or empty, or if
     *         the expected argument(s) are not defined or are invalid.
     * @throws IllegalStateException If the identity service is not available or cannot be created
     * @throws UnknownProviderException If the provider cannot be found
     * @throws IOException if an I/O error occurs
     * @throws APPCException If the server cannot be rebuilt for some reason
     */
	@Test
	public void testRebuildServer()
			throws IOException, IllegalStateException, IllegalArgumentException, ZoneException, APPCException {
		when(factory.getOperationObject(Operation.REBUILD_SERVICE)).thenReturn(providerOperation);
		Server actualServer = new Server();
		actualServer.setStatus(Server.Status.READY);
		when(providerOperation.doOperation(anyObject(), anyObject())).thenReturn(actualServer);
		Map<String, String> params = new HashMap<>();
		params.put(ProviderAdapter.PROPERTY_INSTANCE_URL, SERVER_URL);
		params.put(ProviderAdapter.PROPERTY_PROVIDER_NAME, PROVIDER_NAME);
		SvcLogicContext svcContext = new SvcLogicContext();
		Server expectedServer = adapter.rebuildServer(params, svcContext);
		assertEquals(Server.Status.READY, expectedServer.getStatus());
	}
	
    /**
     * Tests that we can terminate a running server
     * 
     * @throws ZoneException If the login cannot be performed because the principal and/or
     *         credentials are invalid.
     * @throws IllegalArgumentException If the principal and/or credential are null or empty, or if
     *         the expected argument(s) are not defined or are invalid.
     * @throws IllegalStateException If the identity service is not available or cannot be created
     * @throws UnknownProviderException If the provider cannot be found
     * @throws IOException if an I/O error occurs
     * @throws APPCException If the server cannot be terminated for some reason
     */
	@Test
	public void testTerminateServer()
			throws IOException, IllegalStateException, IllegalArgumentException, ZoneException, APPCException {
		when(factory.getOperationObject(Operation.TERMINATE_SERVICE)).thenReturn(providerOperation);
		Server actualServer = new Server();
		actualServer.setStatus(Server.Status.DELETED);
		when(providerOperation.doOperation(anyObject(), anyObject())).thenReturn(actualServer);
		Map<String, String> params = new HashMap<>();
		params.put(ProviderAdapter.PROPERTY_INSTANCE_URL, SERVER_URL);
		params.put(ProviderAdapter.PROPERTY_PROVIDER_NAME, PROVIDER_NAME);
		SvcLogicContext svcContext = new SvcLogicContext();
		Server expectedServer = adapter.terminateServer(params, svcContext);
		assertEquals(Server.Status.DELETED, expectedServer.getStatus());
	}
	
    /**
     * Tests that we can evacuate a server to move it to non-pending state 
     * 
     * @throws ZoneException If the login cannot be performed because the principal and/or
     *         credentials are invalid.
     * @throws IllegalArgumentException If the principal and/or credential are null or empty, or if
     *         the expected argument(s) are not defined or are invalid.
     * @throws IllegalStateException If the identity service is not available or cannot be created
     * @throws UnknownProviderException If the provider cannot be found
     * @throws IOException if an I/O error occurs
     * @throws APPCException If the server cannot be evacuated for some reason
     */
	@Test
	public void testEvacuateServer()
			throws IOException, IllegalStateException, IllegalArgumentException, ZoneException, APPCException {
		when(factory.getOperationObject(Operation.EVACUATE_SERVICE)).thenReturn(evacuateServer);
		Server actualServer = new Server();
		actualServer.setStatus(Server.Status.READY);
		when(evacuateServer.doOperation(anyObject(), anyObject())).thenReturn(actualServer);
		Map<String, String> params = new HashMap<>();
		params.put(ProviderAdapter.PROPERTY_INSTANCE_URL, SERVER_URL);
		params.put(ProviderAdapter.PROPERTY_PROVIDER_NAME, PROVIDER_NAME);
		SvcLogicContext svcContext = new SvcLogicContext();
		Server expectedServer = adapter.evacuateServer(params, svcContext);
		assertEquals(Server.Status.READY, expectedServer.getStatus());
	}
	
    /**
     * Tests that we can migrate a server. Migration can be done only on certain statuses like 
     * READY, RUNNING & SUSPENDED 
     * 
     * @throws ZoneException If the login cannot be performed because the principal and/or
     *         credentials are invalid.
     * @throws IllegalArgumentException If the principal and/or credential are null or empty, or if
     *         the expected argument(s) are not defined or are invalid.
     * @throws IllegalStateException If the identity service is not available or cannot be created
     * @throws UnknownProviderException If the provider cannot be found
     * @throws IOException if an I/O error occurs
     * @throws APPCException If the server cannot be migrated for some reason
     */
	@Test
	public void testMigrateServer()
			throws IOException, IllegalStateException, IllegalArgumentException, ZoneException, APPCException {
		when(factory.getOperationObject(Operation.MIGRATE_SERVICE)).thenReturn(providerOperation);
		Server actualServer = new Server();
		actualServer.setStatus(Server.Status.READY);
		when(providerOperation.doOperation(anyObject(), anyObject())).thenReturn(actualServer);
		Map<String, String> params = new HashMap<>();
		params.put(ProviderAdapter.PROPERTY_INSTANCE_URL, SERVER_URL);
		params.put(ProviderAdapter.PROPERTY_PROVIDER_NAME, PROVIDER_NAME);
		SvcLogicContext svcContext = new SvcLogicContext();
		Server expectedServer = adapter.migrateServer(params, svcContext);
		assertEquals(Server.Status.READY, expectedServer.getStatus());
	}
}
