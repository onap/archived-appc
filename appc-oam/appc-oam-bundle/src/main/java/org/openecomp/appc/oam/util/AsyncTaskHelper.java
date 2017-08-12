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

package org.openecomp.appc.oam.util;

import com.att.eelf.configuration.EELFLogger;
import org.openecomp.appc.oam.AppcOam;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Utility class provides general async task related help.
 */
@SuppressWarnings("unchecked")
public class AsyncTaskHelper {
    final int MMODE_TASK_DELAY = 10000;
    final int COMMON_INITIAL_DELAY = 0;
    final int COMMON_INTERVAL = 1000;

    private final EELFLogger logger;
    private final ScheduledExecutorService scheduledExecutorService;
    private final ThreadPoolExecutor bundleOperationService;

    /** Reference to the Async task */
    private volatile Future<?> backgroundOamTask;

    /**
     * Constructor
     * @param eelfLogger of the logger
     */
    public AsyncTaskHelper(EELFLogger eelfLogger) {
        logger = eelfLogger;

        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(
                (runnable) -> {
                    Bundle bundle = FrameworkUtil.getBundle(AppcOam.class);
                    return new Thread(runnable, bundle.getSymbolicName() + " scheduledExecutor");
                }
        );

        bundleOperationService = new ThreadPoolExecutor(
                0,
                10,
                10,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue(),// BlockingQueue<Runnable> workQueue
                (runnable) -> new Thread(runnable, "OAM bundler operation executor")//ThreadFactory
        );
    }

    void addThreadsToPool() {
        bundleOperationService.setCorePoolSize(bundleOperationService.getMaximumPoolSize());
    }

    void removeThreadsFromPoolWhenDone() {
        bundleOperationService.setCorePoolSize(0);
    }

    /**
     * Terminate the class <bS>ScheduledExecutorService</b>
     */
    public void close() {
        logDebug("Start shutdown scheduleExcutorService.");
        scheduledExecutorService.shutdown();
        bundleOperationService.shutdown();
        logDebug("Completed shutdown scheduleExcutorService.");
    }

    /**
     * Get current async task refernce
     * @return the class <b>backgroundOamTask</b>
     */
    public Future<?> getCurrentAsyncTask() {
        return backgroundOamTask;
    }

    /**
     * Schedule a service for async task with the passed in parameters
     * @param rpc of the REST API call, decides how to schedule the service
     * @param runnable of the to be scheduled service.
     * @return the refernce of the scheduled task
     */
    public Future<?> scheduleAsyncTask(final AppcOam.RPC rpc, final Runnable runnable) {
        int initialDelay, interval;
        switch (rpc) {
            case maintenance_mode:
                initialDelay = interval =MMODE_TASK_DELAY;
                break;
            case start:
            case stop:
            case restart:
                initialDelay = COMMON_INITIAL_DELAY;
                interval = COMMON_INTERVAL;
                break;
            default:
                // should not get here. Log it and return null
                logDebug(String.format("Cannot scheudle task for unsupported RPC(%s).", rpc.name()));
                return null;
        }

        // Always cancel existing  async task
        if (backgroundOamTask != null) {
            backgroundOamTask.cancel(true);
        }
        backgroundOamTask = scheduledExecutorService.scheduleWithFixedDelay(
                runnable, initialDelay, interval, TimeUnit.MILLISECONDS);

        return backgroundOamTask;
    }

    Future<?> submitBundleLcOperation(final Callable callable) {
        return bundleOperationService.submit(callable);
    }

    /**
     * Cancle a previously schedule task. If the task is the same as backgroundOamTask, set it to null.
     * @param task to be canceled
     */
    public void cancelAsyncTask(Future<?> task) {
        task.cancel(false);
        if (task == backgroundOamTask) {
            backgroundOamTask = null;
        }
    }

    /**
     * Genral debug log when debug logging level is enabled.
     * @param message of the log message format
     * @param args of the objects listed in the message format
     */
    private void logDebug(String message, Object... args) {
        if (logger.isDebugEnabled()) {
            logger.debug(String.format(message, args));
        }
    }
}
