/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright 2018 AT&T
 * =================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

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
