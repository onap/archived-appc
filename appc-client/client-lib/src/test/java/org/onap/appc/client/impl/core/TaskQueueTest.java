package org.onap.appc.client.impl.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

public class TaskQueueTest {

	private ExecutorService executor = Executors.newFixedThreadPool(1);
	private TaskQueue queue;
	private CountDownLatch latch;
	
	@Before
	public void setUp() throws Exception {
		executor.execute(queue = new TaskQueue());
	}

	@Test
	public void testAddTask() throws InterruptedException {
		latch = new CountDownLatch(1);
		queue.addTask(new RunTask());
		assertTrue(latch.await(3, TimeUnit.SECONDS));
	}

	@Test
	public void testAddManyTasks() throws InterruptedException {
		int count = 5;
		latch = new CountDownLatch(count);
		for (int i = 0; i < count; i++) {
			queue.addTask(new RunTask());
		}
		assertTrue(latch.await(3, TimeUnit.SECONDS));
	}

	@Test
	public void testStopQueue() throws InterruptedException {
		latch = new CountDownLatch(1);
		queue.stopQueue();
		queue.addTask(new RunTask());
		assertFalse(latch.await(1, TimeUnit.SECONDS));
		
	}

	private class RunTask implements Runnable {
		@Override
		public void run() {
			latch.countDown();
		}
	}
}
