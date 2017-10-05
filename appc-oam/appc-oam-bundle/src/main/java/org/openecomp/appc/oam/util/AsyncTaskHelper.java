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
import org.openecomp.appc.oam.processor.BaseActionRunnable;
import org.openecomp.appc.statemachine.impl.readers.AppcOamStates;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 * The AsyncTaskHelper class manages an internal parent child data structure.   The parent is a transient singleton,
 * meaning only one can exist at any given time.     The parent is scheduled with the
 * {@link #scheduleBaseRunnable(Runnable, Consumer, long, long)} and is executed at configured interval.   It can be
 * terminated by using the {@link Future#cancel(boolean)} or the {@link Future#cancel(boolean)} returned from \
 * {@link #scheduleBaseRunnable(Runnable, Consumer, long, long)}.
 * <p>
 * The children are scheduled using {@link #submitBaseSubCallable(Callable)}} and can only be scheduled if a parent
 * is scheduled.   Children only execute once, but can be terminated preemptively by the {@link Future#cancel(boolean)}
 * returned from {@link #submitBaseSubCallable(Callable)} or indirectly by terminating the parent via the method
 * described above.
 * <p>
 * This class augments the meaning of {@link Future#isDone()} in that it guarantees that this method only returns true
 * if the scheduled {@link Runnable} or {@link Callable}  is not currently executing and is not going to execute in the
 * future.   This is different than the Java core implementation of {@link Future#isDone()} in which it will return
 * true immediately after the {@link Future#cancel(boolean)} is called. Even if a Thread is actively executing the
 * {@link Runnable} or {@link Callable} and has not return yet. See Java BUG JDK-8073704
 * <p>
 * The parent {@link Future#isDone()} has an additional augmentation in that it will not return true until all of its
 * children's {@link Future#isDone()} also return true.
 *
 */
@SuppressWarnings("unchecked")
public class AsyncTaskHelper {

    private final EELFLogger logger;
    private final ScheduledExecutorService scheduledExecutorService;
    private final ThreadPoolExecutor bundleOperationService;

    /** Reference to {@link MyFuture} return from {@link #scheduleBaseRunnable(Runnable, Consumer, long, long)} */
    private MyFuture backgroundBaseRunnableFuture;

    /** The cancel Callback from {@link #scheduleBaseRunnable(Runnable, Consumer, long, long)}   */
    private Consumer<AppcOam.RPC> cancelCallBackForBaseRunnable;

    /** All Futures created by thus calls which have not completed -- {@link Future#isDone()} equals false  */
    private Set<MyFuture> myFutureSet = new HashSet<>();

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
            new LinkedBlockingQueue(), //BlockingQueue<Runnable> workQueue
            (runnable) -> {
                Bundle bundle = FrameworkUtil.getBundle(AppcOam.class);
                return new Thread(runnable, bundle.getSymbolicName() + " bundle operation executor");
            }
        );
    }

    /**
     * Terminate the class <bS>ScheduledExecutorService</b>
     */
    public void close() {
        logDebug("Start shutdown scheduleExcutorService.");
        bundleOperationService.shutdownNow();
        scheduledExecutorService.shutdownNow();
        logDebug("Completed shutdown scheduleExcutorService.");
    }


    /**
     * Cancel currently executing {@link BaseActionRunnable} if any.
     * This method returns immediately if there is currently no {@link BaseActionRunnable} actively executing.
     * @param rpcCausingAbort - The RPC causing the abort
     * @param stateBeingAbborted - The current state being canceled
     * @param timeout - The amount of time to wait for a cancel to complete
     * @param timeUnit - The unit of time of timeout
     * @throws TimeoutException - If {@link BaseActionRunnable} has not completely cancelled within the timeout period
     * @throws InterruptedException - If the Thread waiting for the abort
     */
    public synchronized void cancelBaseActionRunnable(final AppcOam.RPC rpcCausingAbort,
                                                      AppcOamStates stateBeingAbborted,
                                                      long timeout, TimeUnit timeUnit)
        throws TimeoutException,InterruptedException {

        final MyFuture localBackgroundBaseRunnableFuture = backgroundBaseRunnableFuture;
        final Consumer<AppcOam.RPC> localCancelCallBackForBaseRunnable = cancelCallBackForBaseRunnable;

        if (localBackgroundBaseRunnableFuture == null || localBackgroundBaseRunnableFuture.isDone()) {
          return;
        }

        if (localCancelCallBackForBaseRunnable != null) {
            localCancelCallBackForBaseRunnable.accept(rpcCausingAbort);
        }
        localBackgroundBaseRunnableFuture.cancel(true);

        long timeoutMillis = timeUnit.toMillis(timeout);
        long expiryTime = System.currentTimeMillis() + timeoutMillis;
        while (!(localBackgroundBaseRunnableFuture.isDone())) {
            long sleepTime = expiryTime - System.currentTimeMillis();
            if (sleepTime < 1) {
                break;
            }
            this.wait(sleepTime);
        }

        if (!localBackgroundBaseRunnableFuture.isDone()) {
            throw new TimeoutException(String.format("Unable to abort %s in timely manner.",stateBeingAbborted));
        }
    }

    /**
     * Schedule a {@link BaseActionRunnable} to begin async execution.   This is the Parent  {@link Runnable} for the
     * children that are submitted by {@link #submitBaseSubCallable(Callable)}
     *
     * The currently executing {@link BaseActionRunnable} must fully be terminated before the next can be scheduled.
     * This means all Tasks' {@link MyFuture#isDone()} must equal true and all threads must return to their respective
     * thread pools.
     *
     * @param runnable of the to be scheduled service.
     * @param cancelCallBack to be invoked when
     *        {@link #cancelBaseActionRunnable(AppcOam.RPC, AppcOamStates, long, TimeUnit)} is invoked.
     * @param initialDelayMillis the time to delay first execution
     * @param delayMillis the delay between the termination of one
     * execution and the commencement of the next
     * @return The {@link BaseActionRunnable}'s {@link Future}
     * @throws IllegalStateException if there is currently executing Task
     */
    public synchronized Future<?> scheduleBaseRunnable(final Runnable runnable,
                                                       final Consumer<AppcOam.RPC> cancelCallBack,
                                                       long initialDelayMillis,
                                                       long delayMillis)
        throws IllegalStateException {

        if (backgroundBaseRunnableFuture != null && !backgroundBaseRunnableFuture.isDone()) {
            throw new IllegalStateException("Unable to schedule background task when one is already running.  All task must fully terminated before another can be scheduled. ");
        }

        this.cancelCallBackForBaseRunnable = cancelCallBack;

        backgroundBaseRunnableFuture = new MyFuture(runnable) {
            /**
             * augments the cancel operation to cancel all subTack too,
             */
            @Override
            public boolean cancel(final boolean mayInterruptIfRunning) {
                boolean cancel;
                synchronized (AsyncTaskHelper.this) {
                    cancel = super.cancel(mayInterruptIfRunning);
                    //clone the set to prevent java.util.ConcurrentModificationException.  The  synchronized prevents
                    //other threads from modifying this set, but not itself.  The  f->f.cancel may modify myFutureSet by
                    //removing an entry which breaks the iteration in the forEach.
                    (new HashSet<MyFuture>(myFutureSet))
                            .stream().filter(f->!this.equals(f)).forEach(f->f.cancel(mayInterruptIfRunning));
                }
                return cancel;
            }

            /**
             * augments the isDone operation to return false until all subTask have completed too.
             */
            @Override
            public boolean isDone() {
                synchronized (AsyncTaskHelper.this) {
                    return myFutureSet.isEmpty();
                }
            }
        };
        backgroundBaseRunnableFuture.setFuture(
            scheduledExecutorService.scheduleWithFixedDelay(
                backgroundBaseRunnableFuture, initialDelayMillis, delayMillis, TimeUnit.MILLISECONDS)
        );
        return backgroundBaseRunnableFuture;
    }

    /**
     * Submits children {@link Callable} to be executed as soon as possible,  A parent must have been scheduled
     * previously via {@link #scheduleBaseRunnable(Runnable, Consumer, long, long)}
     * @param callable the Callable to be submitted
     * @return The {@link Callable}'s {@link Future}
     */
    synchronized Future<?> submitBaseSubCallable(final Callable callable) {

        if (backgroundBaseRunnableFuture == null
            || backgroundBaseRunnableFuture.isCancelled()
            || backgroundBaseRunnableFuture.isDone()){
            throw new IllegalStateException("Unable to schedule subCallable when a base Runnable is not running.");
        }

        //Make sure the pool is ready to go
        if(bundleOperationService.getPoolSize() != bundleOperationService.getMaximumPoolSize()){
            bundleOperationService.setCorePoolSize(bundleOperationService.getMaximumPoolSize());
            bundleOperationService.prestartAllCoreThreads();
            bundleOperationService.setCorePoolSize(0);
        }

        MyFuture<?> myFuture = new MyFuture(callable);
        myFuture.setFuture(bundleOperationService.submit((Callable)myFuture));
        return myFuture;
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

    /**
     * This class has two purposes.  First it insures  {@link #isDone()} only returns true if the deligate is not
     * currently running and will not be running in the future: See Java BUG JDK-8073704 Second this class maintains
     * the {@link #myFutureSet } by insurring that itself is removed when  {@link #isDone()} returns true.
     *
     * See {@link #scheduleBaseRunnable(Runnable, Consumer, long, long)} and {@link #submitBaseSubCallable(Callable)}
     * for usage of this class
     */
    private class MyFuture<T> implements Future<T>, Runnable, Callable<T> {

        private Future<T> future;
        private final Runnable runnable;
        private final Callable<T> callable;
        private boolean isRunning;

        MyFuture(Runnable runnable) {
            this.runnable = runnable;
            this.callable = null;
            myFutureSet.add(this);
        }

        MyFuture(Callable<T> callable) {
            this.runnable = null;
            this.callable = callable;
            myFutureSet.add(this);
        }

        void setFuture(Future<T> future) {
            this.future = future;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            synchronized (AsyncTaskHelper.this) {
                if (!isRunning) {
                    myFutureSetRemove();
                }

                return future.cancel(mayInterruptIfRunning);
            }
        }

        @Override
        public boolean isCancelled() {
            synchronized (AsyncTaskHelper.this) {
                return future.isCancelled();
            }
        }

        @Override
        public boolean isDone() {
            synchronized (AsyncTaskHelper.this) {
                return future.isDone() && !isRunning;
            }
        }

        @Override
        public T get() throws InterruptedException, ExecutionException {
                return future.get();
        }

        @Override
        public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return future.get(timeout, unit);
        }

        @Override
        public void run() {
            synchronized (AsyncTaskHelper.this) {
                if(future.isCancelled()){
                    return;
                }
                isRunning = true;
            }
            try {
                runnable.run();
            } finally {
                synchronized (AsyncTaskHelper.this) {
                    isRunning = false;

                    //The Base Runnable is expected to run again.
                    //unless it has been canceled.
                    //so only removed if it is canceled.
                    if (future.isCancelled()) {
                        myFutureSetRemove();
                    }
                }
            }
        }

        @Override
        public T call() throws Exception {
            synchronized (AsyncTaskHelper.this) {
                if(future.isCancelled()){
                    throw new CancellationException();
                }
                isRunning = true;
            }
            try {
                return callable.call();
            } finally {
                synchronized (AsyncTaskHelper.this){
                    isRunning = false;
                    myFutureSetRemove();
                }
            }
        }


        /**
         * Removes this from the the myFutureSet.
         * When all the BaseActionRunnable is Done notify any thread waiting in
         * {@link AsyncTaskHelper#cancelBaseActionRunnable(AppcOam.RPC, AppcOamStates, long, TimeUnit)}
         */
        void myFutureSetRemove(){
            synchronized (AsyncTaskHelper.this) {
                myFutureSet.remove(this);
                if(myFutureSet.isEmpty()){
                    backgroundBaseRunnableFuture = null;
                    cancelCallBackForBaseRunnable = null;
                    AsyncTaskHelper.this.notifyAll();

                }
            }
        }

    }
}
