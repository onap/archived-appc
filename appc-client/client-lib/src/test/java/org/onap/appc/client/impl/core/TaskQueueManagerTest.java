package org.onap.appc.client.impl.core;

import static org.junit.Assert.*;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class TaskQueueManagerTest {
	
	private TaskQueueManager qm;
	private CountDownLatch latch;
	private static Properties props = new Properties();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		props.setProperty("client.pool.size", "3");
	}

	@Before
	public void setUp() throws Exception {
		qm = new TaskQueueManager(props);
	}

	@Test
	public void testSubmit() throws InterruptedException {
		int count = 10;
		latch = new CountDownLatch(count);
		for (int i = 0; i < count; i++) {
			qm.submit(i + "", new RunTask());
		}
		assertTrue(latch.await(3, TimeUnit.SECONDS));
	}

	@Test
	public void testStopQueueManager() throws InterruptedException {
		latch = new CountDownLatch(1);
		qm.stopQueueManager();
		qm.submit("1", new RunTask());
		assertFalse(latch.await(1, TimeUnit.SECONDS));
	}
	
	private class RunTask implements Runnable {
		@Override
		public void run() {
			latch.countDown();
		}
	}
}
