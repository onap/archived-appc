package org.openecomp.appc.flow.executor.node;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.ws.rs.core.MediaType;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openecomp.appc.flow.controller.executorImpl.RestExecutor;
import org.powermock.api.mockito.PowerMockito;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

public class RestExecutorTest {
	
    
    private static final String URL = null;

	@Mock
	private DefaultClientConfig clientConfig;

	@Mock
	private com.sun.jersey.api.client.WebResource webResource;
	
	@InjectMocks
	private Client client;
	@Mock
	private ClientResponse res;
	@Mock
	URI resourceUri;
@Mock
RestExecutor restEx;
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		clientConfig = Mockito.mock(DefaultClientConfig.class);
		 Client mockClient = Client.create();
		 client = Client.create(clientConfig);
	        doReturn(mockClient).when(client).create();
	        webResource= mockClient.resource(URL);
	        doReturn(webResource).when(mockClient).resource(URL);
	        when(webResource.get((Class<String>) any())).thenReturn("OK")	;        
	}

	@After
	public void tearDown() throws Exception {
	}

	


	
}
