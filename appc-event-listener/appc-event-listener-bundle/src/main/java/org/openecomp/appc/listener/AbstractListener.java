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

package org.openecomp.appc.listener;

import java.security.SecureRandom;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.openecomp.appc.listener.impl.EventHandlerImpl;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public abstract class AbstractListener implements Listener {

    private final EELFLogger LOG = EELFManager.getInstance().getLogger(AbstractListener.class);

    protected AtomicBoolean run = new AtomicBoolean(false);

    protected int QUEUED_MIN = 1;
    protected int QUEUED_MAX = 10;

    protected int THREAD_MIN = 4;
    protected int THREAD_MAX = THREAD_MIN; // Fixed thread pool
    protected int THREAD_SCALE_DOWN_SEC = 10; // Number of seconds to wait until we remove idle threads

    protected ThreadPoolExecutor executor;

    protected EventHandler dmaap;

    protected ListenerProperties props;

    private String listenerId;

    public AbstractListener(ListenerProperties props) {
        updateProperties(props);

        dmaap = new EventHandlerImpl(props);
        if (dmaap.getClientId().equals("0")) {
        	dmaap.setClientId(String.valueOf(new SecureRandom().nextInt(1000)));
        }

        BlockingQueue<Runnable> threadQueue = new ArrayBlockingQueue<Runnable>(QUEUED_MAX + QUEUED_MIN + 1);
        executor = new ThreadPoolExecutor(THREAD_MIN, THREAD_MAX, THREAD_SCALE_DOWN_SEC, TimeUnit.SECONDS, threadQueue,
            new JobRejectionHandler());

        // Custom Named thread factory
        BasicThreadFactory threadFactory = new BasicThreadFactory.Builder().namingPattern("DMaaP-Worker-%d").build();
        executor.setThreadFactory(threadFactory);

        run.set(true);
    }

    /**
     * Starts a loop that will only end after stop() or stopNow() are called. The loop will read messages off the DMaaP
     * topic and perform some action on them while writing messages back to DMaaP at critical points in the execution.
     * Inherited from Runnable.
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        LOG.error("Listener.run() has not been implemented");
    }

    @Override
    public void stop() {
        run.set(false);
        LOG.info(String.format("Stopping with %d messages in queue", executor.getQueue().size()));
        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOG.error("Listener graceful stop() failed", e);
        }
        
        // close DMaaP clients
        if (dmaap != null) {
        	dmaap.closeClients();
        }
        LOG.info("Listener Thread Pool Finished");
    }

    @Override
    public void stopNow() {
        run.set(false);
        LOG.info(String.format("StopNow called. Orphaning %d messages in the queue", executor.getQueue().size()));
        executor.getQueue().clear();
        stop();
    }

    @Override
    public String getBenchmark() {
        return String.format("%s - No benchmarking implemented.", getListenerId());
    }

    @Override
    public String getListenerId() {
        return listenerId;
    }

    // Sets the id of the listener in
    @Override
    public void setListenerId(String id) {
        listenerId = id;
    }

    private void updateProperties(ListenerProperties properties) {
        this.props = properties;
        QUEUED_MIN =
            Integer.valueOf(props.getProperty(ListenerProperties.KEYS.THREADS_MIN_QUEUE, String.valueOf(QUEUED_MIN)));
        QUEUED_MAX =
            Integer.valueOf(props.getProperty(ListenerProperties.KEYS.THREADS_MAX_QUEUE, String.valueOf(QUEUED_MAX)));
        THREAD_MIN =
            Integer.valueOf(props.getProperty(ListenerProperties.KEYS.THREADS_MIN_POOL, String.valueOf(THREAD_MIN)));
        THREAD_MAX =
            Integer.valueOf(props.getProperty(ListenerProperties.KEYS.THREADS_MAX_POOL, String.valueOf(THREAD_MAX)));

        listenerId = props.getPrefix();
    }

    /**
     * This class will be used to handle what happens when we cannot add a job because of a ThreadPool issue. It does
     * not get invoked if there is any fault with the job. NOTE: So far, this has only been seen when doing a
     * {@link Listener#stopNow}
     *
     */
    class JobRejectionHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            LOG.error(String.format("A job was rejected. [%s]", r));
        }

    }
}
