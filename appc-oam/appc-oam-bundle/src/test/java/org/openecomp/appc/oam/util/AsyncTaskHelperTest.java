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
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openecomp.appc.oam.AppcOam;
import org.openecomp.appc.statemachine.impl.readers.AppcOamStates;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.LinkedList;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;


@RunWith(PowerMockRunner.class)
@PrepareForTest({FrameworkUtil.class})
public class AsyncTaskHelperTest {
    private AsyncTaskHelper asyncTaskHelper;

    private long initialDelayMillis = 0;
    private long delayMillis = 10;


    @Before
    public void setUp() throws Exception {

        // to avoid operation on logger fail, mock up the logger
        EELFLogger mockLogger = mock(EELFLogger.class);


        mockStatic(FrameworkUtil.class);
        Bundle myBundle = mock(Bundle.class);
        Mockito.doReturn("TestBundle").when(myBundle).getSymbolicName();
        PowerMockito.when(FrameworkUtil.getBundle(any())).thenReturn(myBundle);

        asyncTaskHelper = new AsyncTaskHelper(mockLogger);


    }


    @After
    public void shutdown(){
        asyncTaskHelper.close();
    }


    /**
     * Test that Base Runnable
     *
     * Runs at a fix rate;
     * Only one Base Runnable  can be scheduled at time;
     * Future.cancle stops the Base Runnable;
     * That another Base Runnable  can be scheduled once the previous isDone.
     */
    @Test
    public void test_scheduleBaseRunnable_Base_isDone() throws Exception{



        //loop is to test we can run consecutive Base Runnable
        for(int testIteration = 0; testIteration < 3;testIteration++){
            final ExecuteTest et = new ExecuteTest();

            Future<?> future = asyncTaskHelper.scheduleBaseRunnable(
                    et::test
                    , s -> { }
                    ,initialDelayMillis
                    ,delayMillis
            );

            //make sure it is running at a fix rate
            Assert.assertTrue("It should be iterating", et.waitForTestExec(5000));
            Assert.assertFalse("It Should not be Done", future.isDone());
            Assert.assertTrue("It should be iterating", et.waitForTestExec(5000));
            Assert.assertFalse("It Should not be Done", future.isDone());


            //make sure a seconds Runnable cannot be scheduled when one is already running
            try {
                asyncTaskHelper.scheduleBaseRunnable(et::test
                        , s -> {}
                        ,initialDelayMillis
                        ,delayMillis
                );
                Assert.fail("scheduling should have been prevented.  ");
            } catch (IllegalStateException e) {
                //IllegalStateException means the second scheduling was not allowed.
            }


            //let it cancel itself
            et.cancelSelfOnNextExecution(future);

            //it should be done after it executes itself one more time.
            Assert.assertTrue("it should be done", waitFor(future::isDone, 5000));
            Assert.assertTrue("The test failed to execute", et.isExecuted);
        }


    }


    /**
     * Makes sure the Future.isDone one only returns true if its runnable is not currently executing and will not
     * execute in the future.  Default implementation of isDone() returns true immediately after the future is
     * canceled -- Even if is there is still a thread actively executing the runnable
     */
    @Test
    public void test_scheduleBaseRunnable_Base_isDone_Ignore_Interrupt() throws Exception{


        final ExecuteTest et = new ExecuteTest();

        //configure test to run long and ignore interrupt
        et.isContinuous = true;
        et.isIgnoreInterrupt = true;



        Future<?> future = asyncTaskHelper.scheduleBaseRunnable(
                et::test
                , s->{}
                ,initialDelayMillis
                ,delayMillis
        );

        //make sure it is running
        Assert.assertTrue("It should be running",waitFor(et::isExecuting,1000));
        Assert.assertTrue("It should be running",et.waitForTestExec(1000));
        Assert.assertFalse("It Should not be Done", future.isDone());

        //cancel it and make sure it is still running
        future.cancel(true);
        Assert.assertTrue("It should be running",waitFor(et::isExecuting,1000));
        Assert.assertTrue("It should be running",et.waitForTestExec(1000));
        Assert.assertFalse("It Should not be Done", future.isDone());

        //let the thread die and then make sure its done
        et.isContinuous = false;
        Assert.assertTrue("It should not be running",waitForNot(et::isExecuting,1000));
        Assert.assertTrue("It Should be Done", future.isDone());

    }




    /**
     * Make sure the base Future.isDone returns false until the sub callable has completed execution.
     */
    @Test
    public void test_scheduleBaseRunnable_SubTask_isDone_Ignore_Interrupt() throws Exception{


        final ExecuteTest baseET = new ExecuteTest();
        final ExecuteTest subET = new ExecuteTest();

        //configure sub test to run long and ignore interrupt
        subET.isContinuous = true;
        subET.isIgnoreInterrupt = true;


        //schedule the Base test to run and make sure it is running.
        Future<?> baseFuture = asyncTaskHelper.scheduleBaseRunnable(
                baseET::test
                ,s->{}
                ,initialDelayMillis
                ,delayMillis
                );
        Assert.assertTrue("baseET should be running",waitFor(baseET::isExecuted,1000));
        Assert.assertFalse("baseET Should not be Done because it runs at a fix rate", baseFuture.isDone());


        //schedule the sub task and make sure it is  running
        Future<?> subFuture = asyncTaskHelper.submitBaseSubCallable(subET::test);
        Assert.assertTrue("subET should be running",waitFor(subET::isExecuting,1000));
        Assert.assertTrue("subET should be running",subET.waitForTestExec(1000));
        Assert.assertFalse("subET Should not be Done", subFuture.isDone());
        Assert.assertFalse("baseET Should not be Done", baseFuture.isDone());

        //cancel the base task and make sure isDone is still false
        baseFuture.cancel(true);
        Assert.assertTrue("subET should be running",waitFor(subET::isExecuting,1000));
        Assert.assertTrue("subET should be running",subET.waitForTestExec(1000));
        Assert.assertFalse("subET Should not be Done",subFuture.isDone());
        Assert.assertFalse("baseET Should not be Done", baseFuture.isDone());


        //let the sub task die and and make sure the base is now finally done
        subET.isContinuous = false;
        Assert.assertTrue("subET should not be running",waitForNot(subET::isExecuting,1000));
        Assert.assertTrue("subET Should be Done", subFuture.isDone());
        Assert.assertTrue("baseET Should be Done", baseFuture.isDone());

    }


    /**
     * Make sure the base Future.isDone returns false until the 3 sub callable has completed execution.
     * Each sub callable will be shutdown one at a time.
     */
    @Test
    public void test_scheduleBaseRunnable_SubTasks_isDone() throws Exception {


        //loop is to test we can run consecutive Base Runnable
        for (int testIteration = 0; testIteration < 3; testIteration++) {
            final ExecuteTest baseET = new ExecuteTest();
            final LinkedList<Sub> subList = new LinkedList<>();
            for (int i = 0; i < 3; i++) {
                Sub sub = new Sub();
                sub.et.isContinuous = true;
                subList.add(sub);
            }


            //schedule the base runnable and make sure it is running
            Future<?> baseFuture = asyncTaskHelper.scheduleBaseRunnable(
                    baseET::test
                    , s -> {
                    }
                    , initialDelayMillis
                    , delayMillis
            );
            Assert.assertTrue("baseET should be running", waitFor(baseET::isExecuted, 1000));
            Assert.assertFalse("baseET Should not be Done because it runs at a fix rate", baseFuture.isDone());


            //schedule the sub Callables and make sure these are running
            subList.forEach(sub -> sub.future = asyncTaskHelper.submitBaseSubCallable(sub.et::test));
            for (Sub sub : subList) {
                Assert.assertTrue("subET should be running", waitFor(sub.et::isExecuting, 100));
                Assert.assertTrue("subET should be running", sub.et.waitForTestExec(1000));
                Assert.assertFalse("subET Should not be Done", sub.future.isDone());
            }
            Assert.assertFalse("baseET Should not be Done", baseFuture.isDone());


            //On each iteration shut down a sub callable.  Make sure it stops, the others are still running and the
            // //base is still running.
            while (!subList.isEmpty()) {

                //stop one sub and make sure it stopped
                {
                    Sub sub = subList.removeFirst();
                    Assert.assertTrue("subET should be running", waitFor(sub.et::isExecuting, 1000));
                    sub.et.isContinuous = false;
                    Assert.assertTrue("subET should not be running", waitForNot(sub.et::isExecuting,1000));
                    Assert.assertTrue("subET Should not be Done", sub.future.isDone());
                }

                //make sure the other are still running
                for (Sub sub : subList) {
                    Assert.assertTrue("subET should be running", waitFor(sub.et::isExecuting, 1000));
                    Assert.assertTrue("subET should be running", sub.et.waitForTestExec(1000));
                    Assert.assertFalse("subET Should not be Done", sub.future.isDone());
                }

                //Make sure the Base is still running
                Assert.assertFalse("baseET Should not be Done", baseFuture.isDone());
            }

            //let the base cancel itself and make sure it stops
            baseET.cancelSelfOnNextExecution(baseFuture);
            Assert.assertTrue("baseET should be done", waitFor(baseFuture::isDone, 1000));
        }
    }


    /**
     * Make sure SubCallable cannot be scheduled when there is not BaseRunnable
     */
    @Test(expected=IllegalStateException.class)
    public void test_SubTasksScheduleFailWhenNoBase() throws Exception {
        asyncTaskHelper.submitBaseSubCallable(()->null);
    }



    /**
     * Make sure SubCallable cannot be scheduled when BaseRunnable is cancelled but is still actively running.
     */
    @Test(expected=IllegalStateException.class)
    public void test_SubTasksScheduleFailWhenBaseCanceledBeforeisDone() throws Exception {

        final ExecuteTest et = new ExecuteTest();
        et.isContinuous = true;

        Future<?> future = asyncTaskHelper.scheduleBaseRunnable(
                et::test
                , s -> { }
                ,initialDelayMillis
                ,delayMillis
        );

        Assert.assertTrue("It should be running",waitFor(et::isExecuting,1000));
        future.cancel(false);
        Assert.assertTrue("It should be running",waitFor(et::isExecuting,1000));

        try {
            asyncTaskHelper.submitBaseSubCallable(() -> null);
        } finally {
            et.isContinuous = false;
        }



    }


    /**
     * Make sure SubCallable cannot be scheduled after a BaseRunnable has completed
     */
    @Test(expected=IllegalStateException.class)
    public void test_SubTasksScheduleFailAfterBaseDone() throws Exception {

        final ExecuteTest et = new ExecuteTest();

        Future<?> future = asyncTaskHelper.scheduleBaseRunnable(
                et::test
                , s -> { }
                ,initialDelayMillis
                ,delayMillis
        );


        future.cancel(false);
        Assert.assertTrue("It should not be running",waitFor(future::isDone,1000));

        try {
            asyncTaskHelper.submitBaseSubCallable(() -> null);
        } finally {
            et.isContinuous = false;
        }

    }


    /**
     * Test {@link AsyncTaskHelper#cancelBaseActionRunnable(AppcOam.RPC, AppcOamStates, long, TimeUnit)}}
     * Test cancel does not block when BaseRunnable is not scheduled
     */
    @Test
    public void test_cancel_noBlockingWhenBaseRunnableNotScheduled() throws Exception{
        //nothing is running so this should return immediately without TimeoutException
        asyncTaskHelper.cancelBaseActionRunnable(AppcOam.RPC.stop , AppcOamStates.Started , 1, TimeUnit.MILLISECONDS);
    }



    /**
     * Test {@link AsyncTaskHelper#cancelBaseActionRunnable(AppcOam.RPC, AppcOamStates, long, TimeUnit)}}
     * Test cancel does blocks until BaseRunnable is done scheduled
     */
    @Test()
    public void test_cancel_BlockingWhenBaseRunnableNotDone() throws Exception {


        final ExecuteTest et = new ExecuteTest();
        et.isContinuous = true;
        et.isIgnoreInterrupt = true;
        asyncTaskHelper.scheduleBaseRunnable(
                et::test
                , s -> {
                }
                , initialDelayMillis
                , delayMillis
        );

        Assert.assertTrue("It should be running", waitFor(et::isExecuting, 1000));


        //we should get a timeout
        try {
            asyncTaskHelper.cancelBaseActionRunnable(
                    AppcOam.RPC.stop,
                    AppcOamStates.Started,
                    1,
                    TimeUnit.MILLISECONDS);
            Assert.fail("Should have gotten TimeoutException");
        } catch (TimeoutException e) {
            //just ignore as it is expected
        }


        //release the test thread
        et.isContinuous = false;


        //we should not get a timeout
        asyncTaskHelper.cancelBaseActionRunnable(
                AppcOam.RPC.stop,
                AppcOamStates.Started,
                1000,
                TimeUnit.MILLISECONDS);

    }



    /**
     * Test {@link AsyncTaskHelper#cancelBaseActionRunnable(AppcOam.RPC, AppcOamStates, long, TimeUnit)}}
     * Test cancel does not block when BaseRunnable is not scheduled
     */
    @Test
    public void test_BaseRunnableCancelCallback() throws Exception{

        AtomicReference<AppcOam.RPC> cancelCallback = new AtomicReference<>(null);

        final ExecuteTest et = new ExecuteTest();
        et.isContinuous = true;
        Future<?> future = asyncTaskHelper.scheduleBaseRunnable(
                et::test
                , cancelCallback::set
                , initialDelayMillis
                , delayMillis
        );

        Assert.assertTrue("It should be running", waitFor(et::isExecuting, 1000));
        Assert.assertTrue("It should be running", waitForNot(future::isDone, 1000));


        try {
            asyncTaskHelper.cancelBaseActionRunnable(
                    AppcOam.RPC.stop,
                    AppcOamStates.Started,
                    1,
                    TimeUnit.MILLISECONDS);
            Assert.fail("Should have gotten TimeoutException");
        } catch (TimeoutException e) {
           //just ignore as it is expected
        }


        Assert.assertEquals("Unexpected rpc in call back",AppcOam.RPC.stop,cancelCallback.get());
    }








    /**
     * @return true if the negation of the expected value is returned from the supplier within the specified
     * amount of time
     */
    private static boolean waitForNot(Supplier<Boolean> s,long timeoutMillis)throws Exception{
        return waitFor(()->!s.get(),timeoutMillis);
    }


    /**
     * @return true if the expected value is returned from the supplier within the specified
     * amount of time
     */
    private static boolean waitFor(Supplier<Boolean> s,long timeoutMillis) throws Exception {
        long timeout = TimeUnit.MILLISECONDS.toMillis(timeoutMillis);
        long expiryTime = System.currentTimeMillis() + timeout;
        long elapsedTime;
        while(!s.get()){
            elapsedTime = expiryTime - System.currentTimeMillis();
            if(elapsedTime < 1) {
                break;
            }
            Thread.sleep(10);
        }
        return s.get();
    }


    /**
     * This class is used control a thread  executed in th {@link #test()}
     */
    @SuppressWarnings("unused")
    private static class ExecuteTest {


        /** A fail safe to insure this TEst does not run indefinitely */
        private final long EXPIRY_TIME = System.currentTimeMillis() + 10000;



        /** A thread sets this value to true when it has completed the execution the of executes {@link #test()}   */
        private  volatile boolean isExecuted = false;

        /**
         * A thread sets this value to true when it is actively executing {@link #test()} and back to false when
         * it is not
         */
        private  volatile boolean isExecuting = false;

        /**
         * While this value is true, a thread will not be allowed to return from {@link #test()} It will simulate  a
         * long execution.
         */
        private  volatile boolean isContinuous = false;

        /**
         * When this value is set to true, an ongoing simulation of a long execution of {@link #test()} cannot be force
         * to abort via a  {@link Thread#interrupt()}
         */
        private  volatile boolean isIgnoreInterrupt = false;



        /** Use to send a signal to the thread executing {@link #notifyTestExcuted(long)} */
        private  Semaphore inner = new Semaphore(0);

        /** Use to send a signal to the thread executing {@link #waitForTestExec(long)} */
        private  Semaphore outer = new Semaphore(0);

        /** The {@link Future} of the Thread executing {@link #test()}*/
        private volatile Future<?> future;

        /**
         * When set the Thread executing {@link #test()} will cancel itself
         * @param future - The {@link Future} of the Thread executing {@link #test()}
         */
        private void cancelSelfOnNextExecution(Future<?> future) {
            this.future = future;
        }


        private boolean isExecuted() {
            return isExecuted;
        }

        private boolean isExecuting() {
            return isExecuting;
        }


        private boolean isContinuous() {
            return isContinuous;
        }


        private boolean isIgnoreInterrupt() {
            return isIgnoreInterrupt;
        }



        /**
         * The thread executing this method if blocked from returning until the thread executing
         * {@link #test()}  invokes  {@link #notifyTestExcuted(long)} or the specified time elapses
         * @param timeoutMillis - the amount of time to wait for a execution iteration.
         * @return true if the Thread is released because of an invocation of {@link #notifyTestExcuted(long)}
         * @throws InterruptedException - If the Caller thread is interrupted.
         */
        private boolean waitForTestExec(long timeoutMillis) throws InterruptedException {
            inner.release();
            return outer.tryAcquire(timeoutMillis,TimeUnit.MILLISECONDS);
        }


        /**
         * Test simulator
         * @return  Always returns true.
         */
        private Boolean test() {
            isTestExpired();
            System.out.println("started");
             isExecuting = true;
             try {
                 if (future != null) {
                     future.cancel(false);
                 }
                 if(!isContinuous){
                     notifyTestExcuted(1);
                 }

                 while(isContinuous){
                     notifyTestExcuted(100);
                     isTestExpired();
                 }

             } finally {
                 isExecuting = false;
                 isExecuted = true;
             }
             return true;
        }


        /** @throws RuntimeException if the test  has bee running too long */
        private void isTestExpired(){
            if(System.currentTimeMillis() > EXPIRY_TIME){
                throw new RuntimeException("Something went wrong the test expired.");
            }
        }


        /**
         * The thread executing {@link #test()}  if blocked from returning until another thread invokes
         * {@link #waitForTestExec(long)} or the specified time elapses
         * @param timeoutMillis - the amount of time to wait for a execution iteration.
         * @return true if the Thread is released because of an invocation of {@link #waitForTestExec(long)}
         */
        private boolean notifyTestExcuted(long timeoutMillis){
            try {
                boolean acquire = inner.tryAcquire(timeoutMillis,TimeUnit.MILLISECONDS);
                if(acquire){
                    outer.release();
                    System.out.println("release");
                }
            } catch (InterruptedException e) {
                if(!isIgnoreInterrupt){
                    return false;
                }
            }
            return true;
        }
    }


    static class Sub {
        ExecuteTest et = new ExecuteTest();
        Future<?> future = null;
    }

}
