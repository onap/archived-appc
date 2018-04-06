package org.onap.appc.adapter.messaging.dmaap.impl;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;

import org.junit.Ignore;
import org.junit.Test;

public class TestDmaapConsumerImpl {
	String[] hostList = {"192.168.1.1"};	
	Collection<String> hosts = new HashSet<String>(Arrays.asList(hostList));
	
	String topic = "JunitTopicOne";
	String group = "junit-client";
	String id = "junit-consumer-one";
	String key = "key";
	String secret = "secret";
	String filter = null;

	@Test
	public void testDmaapConsumerImplNoFilter() {
	
		DmaapConsumerImpl consumer = new DmaapConsumerImpl(hosts, topic, group, id, key, secret);
		
		assertNotNull(consumer);
		
		Properties props = consumer.getProperties();
		
		assertEquals("192.168.1.1",props.getProperty("host"));
		assertEquals("key",props.getProperty("username"));
		assertEquals("secret",props.getProperty("password"));
	}

	@Test
	public void testDmaapConsumerImplwithFilter() {	
		
		DmaapConsumerImpl consumer = new DmaapConsumerImpl(hosts, topic, group, id, key, secret, filter);
		
		assertNotNull(consumer);

	}

	@Test
	public void testDmaapConsumerImplNoUserPassword() {
	
		DmaapConsumerImpl consumer = new DmaapConsumerImpl(hosts, topic, group, id, null, null);
		
		assertNotNull(consumer);
		
		Properties props = consumer.getProperties();
		
		assertEquals("192.168.1.1",props.getProperty("host"));
		assertNull(props.getProperty("username"));
		assertNull(props.getProperty("password"));
		assertEquals("HTTPNOAUTH",props.getProperty("TransportType"));
	}
	
	@Test
	public void testUpdateCredentials() {
		DmaapConsumerImpl consumer = new DmaapConsumerImpl(hosts, topic, group, id, null, null);
		
		assertNotNull(consumer);
		
		Properties props = consumer.getProperties();
		
		assertEquals("192.168.1.1",props.getProperty("host"));
		assertNull(props.getProperty("username"));
		assertNull(props.getProperty("password"));
		
		consumer.updateCredentials(key, secret);
		
		props = consumer.getProperties();
		assertEquals("192.168.1.1",props.getProperty("host"));
		assertEquals("key",props.getProperty("username"));
		assertEquals("secret",props.getProperty("password"));		
	}

	@Ignore
	@Test
	public void testFetch() {
		fail("Not yet implemented");
	}

	@Ignore
	@Test
	public void testFetchIntInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testCloseNoClient() {
		DmaapConsumerImpl consumer = new DmaapConsumerImpl(hosts, topic, group, id, key, secret);
		
		assertNotNull(consumer);
		
		consumer.close();
	}
	
	@Ignore
	@Test
	public void testCloseWithClient() {
		fail("Not yet implemented");
	}

	@Test
	public void testToString() {
		DmaapConsumerImpl consumer = new DmaapConsumerImpl(hosts, topic, group, id, null, null);
		
		assertNotNull(consumer);
		
		assertEquals("Consumer junit-client/junit-consumer-one listening to JunitTopicOne on [192.168.1.1]",consumer.toString());
	}

	@Test
	public void testUseHttps() {
		DmaapConsumerImpl consumer = new DmaapConsumerImpl(hosts, topic, group, id, key, secret);
		
		assertNotNull(consumer);
		
		assertEquals(false,consumer.isHttps());
		
		consumer.useHttps(true);
		
		assertEquals(true,consumer.isHttps());

		
	}

}
