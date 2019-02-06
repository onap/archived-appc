/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.client.impl.core;

import org.onap.appc.client.impl.protocol.*;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Consolidates all services(Registry, Timeout and Task Queue) for handling of requests/responses events.
 */
class CoreManager{

    private final EELFLogger LOG = EELFManager.getInstance().getLogger(CoreManager.class);
    private final ProtocolFactory protocolFactory;
    protected  AsyncProtocol protocol;
    private final RetrieveMessageCallback protocolCallback = null;
    private final CoreRegistry registry;
    private final ITimerService timerService;
    private final TaskQueueManager queueManager;
    private String DEFAULT_TIMEOUT = "300000";
    private final static String RESPONSE_TIMEOUT = "client.response.timeout";
    private final static String GRACEFUL_SHUTDOWN_TIMEOUT = "client.graceful.shutdown.timeout";
    private boolean isForceShutdown = false;
    private AtomicBoolean isGracefulShutdown = new AtomicBoolean(false);
    private long shutdownTimeout;

    CoreManager(Properties prop) throws CoreException {
        protocolFactory = ProtocolFactory.getInstance();
        try {
            initProtocol(prop);
        }catch (ProtocolException e){
            throw new CoreException(e);
        }
        registry = new CoreRegistry<RequestResponseHandler>(new EmptyRegistryCallbackImpl());
        String timeoutProp = prop.getProperty(RESPONSE_TIMEOUT, DEFAULT_TIMEOUT);
        long responseTimeout = Long.parseLong(timeoutProp);
        String gracefulTimeout = prop.getProperty(GRACEFUL_SHUTDOWN_TIMEOUT, DEFAULT_TIMEOUT);
        shutdownTimeout = Long.parseLong(gracefulTimeout);
        timerService = new TimerServiceImpl(responseTimeout);
        queueManager = new TaskQueueManager(prop);
        listenShutdown();
    }

    /**
     * initiates protocol layer services.
     * @param prop - Properties
     */
    private void initProtocol(Properties prop) throws ProtocolException {
        protocol = (AsyncProtocol) protocolFactory.getProtocolObject(ProtocolType.ASYNC);
        protocol.init(prop, getProtocolCallback());
    }

    /**
     * Creates protocol response callback
     * @return - @{@link ProtocolResponseCallbackImpl}
     */
    RetrieveMessageCallback getProtocolCallback(){
        return new ProtocolResponseCallbackImpl();
    }

    /**
     * Registers a new handler in registry
     * @param corrID - Correlation ID
     * @param requestResponseHandler handler to be called when response arrives
     */
    void registerHandler(String corrID, RequestResponseHandler requestResponseHandler){
        registry.register(corrID, requestResponseHandler);
    }

    /**
     * Remove a handler from registry service by correlation ID.
     * @param corrID - Correlation ID
     * @return - @{@link RequestResponseHandler}
     */
    RequestResponseHandler unregisterHandler(String corrID){
        return (RequestResponseHandler) registry.unregister(corrID);
    }

    /**
     * Checks in registry service if a handler is existing.
     * @param corrID - Correlation ID
     * @return - boolean
     */
    boolean isExistHandler(String corrID) {
        return registry.isExist(corrID);
    }

    /**
     * Starts timer for timeout event when a request was send successfully.
     * @param corrID - Correlation ID
     */
    void startTimer(String corrID){
        timerService.add(corrID, new TimeoutHandlerImpl(corrID));
    }

    /**
     * Cancels timer for fimeout event, in case when complete response was received
     * @param corrID
     */
    void cancelTimer(String corrID){
        timerService.cancel(corrID);
    }

    /**
     * Submits a new task to Queue manager. it is using for both response and timeout tasks
     * @param corrID - Correlation ID
     * @param task - @{@link Runnable} task.
     * @throws InterruptedException
     */
    void submitTask(String corrID, Runnable task) throws InterruptedException {
        queueManager.submit(corrID, task);
    }

    /**
     * Sends request to protocol.
     * @param request - Request
     * @param corrId - Correlation ID
     * @param rpcName - RPC name
     * @throws CoreException - @{@link CoreException}
     */
    void sendRequest(String request, String corrId, String rpcName) throws CoreException {
        MessageContext ctx = getMessageContext(corrId, rpcName);
        try {
            protocol.sendRequest(request, ctx);
        } catch (ProtocolException e) {
            unregisterHandler(corrId);
            throw new CoreException(e);
        }
    }

    /**
     * Creates @{@link MessageContext}
     * @param correlationId - Correlation ID
     * @param rpcName - RPC Name
     * @return - @{@link MessageContext}
     */
    private MessageContext getMessageContext(String correlationId, String rpcName){
        MessageContext msgCtx = new MessageContext();
        msgCtx.setCorrelationID(correlationId);
        msgCtx.setRpc(rpcName);
        return msgCtx;
    }

    /**
     * Implements response callback from protocol and filters responses by correlation ID.
     * Only registered events(by correlation ID) will be handled.
     */
    private class ProtocolResponseCallbackImpl implements RetrieveMessageCallback {
        @Override
        public void onResponse(String response, MessageContext context) {
            String corrID = context.getCorrelationID();
            if (corrID != null) {
                RequestResponseHandler messageHandler = (RequestResponseHandler) registry.get(corrID);
                if (messageHandler != null) {
                    LOG.info("On response callback corrID <" + corrID + "> handler " + messageHandler + " response " + response);
                    messageHandler.handleResponse(context, response);
                }
            }
        }
    }


    /**
     * listens to @{@link Runtime} shutdown event
     */
    private void listenShutdown() {
        Runtime.getRuntime().addShutdownHook(new Thread(){
            public void run(){
                gracefulShutdown();
            }
        });
    }

    /**
     * Implements shutdown for client library.
     * @param isForceShutdown - true force shutdown, false graceful shutdown
     */
    void shutdown(boolean isForceShutdown){
        if(isForceShutdown){
            forceShutdown();
        }else{
            gracefulShutdown();
        }
    }

    /**
     * Graceful shutdown. in case of all requests already were handled, calls to force shutdown. another goes to force
     * shutdown only when either all request will be handled or graceful shutdown will be time out.
     */
    synchronized void gracefulShutdown(){
        isGracefulShutdown.set(true);
        if(registry.isEmpty()){
            forceShutdown();
        }
        else{
            try {
                LOG.info("Core manager::graceful shutdown is starting... this <" + this + ">");
                wait(shutdownTimeout);
                LOG.info("Core manager::graceful shutdown is continue... this <" + this + ">");
                forceShutdown();
            } catch (InterruptedException e) {
            	LOG.error("Error occured in gracefulShutdown "+e);
            }

        }
    }

    /**
     * Closes Protocol, stops Queue Manager and shutdowns Time Service.
     */
    private void forceShutdown(){
        isForceShutdown = true;
        try {
            LOG.info("Starting shutdown process.");
            protocol.shutdown();
            queueManager.stopQueueManager();
            timerService.shutdown();
        } catch (InterruptedException e) {
            LOG.info("Client library shutdown in progress ", e);
        }
    }

    /**
     *
     * @return - true when shutdown is in process
     */
    boolean isShutdownInProgress(){
        return isForceShutdown || isGracefulShutdown.get();
    }

    /**
     * Timeout handler implementation.
     * This handler is responsible to assign a task for handling of timeout events.
     *
     */
    private class TimeoutHandlerImpl implements ITimeoutHandler {

        private final String corrID;

        TimeoutHandlerImpl(String corrID) {
            this.corrID = corrID;
        }

        /**
         * When a timeout event is occurring, the new Timeout task will be assigned into a queue,
         * this queue is shared between both timeout and handlers which belong to same correlation ID.
         */
        @Override
        public void onTimeout() {
            try {
                submitTask(corrID, new Runnable() {
                    @Override
                    public void run() {
                        RequestResponseHandler requestResponseHandler = unregisterHandler(corrID);
                        if (requestResponseHandler != null) {
                            requestResponseHandler.onTimeOut();
                        }
                    }
                });
            } catch (InterruptedException e) {
                LOG.warn("could not submit timeout task for correlation ID <" + corrID + "> ", e);
            }
        }
    }


    /**
     * Wakes Up graceful shutdown.
     */
    class EmptyRegistryCallbackImpl implements CoreRegistry.EmptyRegistryCallback {
        @Override
        public synchronized void emptyCallback() {
            LOG.info("Registry is empty, wake up the shutdown!, isGraceful flag <" + isGracefulShutdown + ">");
            if(isGracefulShutdown.get()){
                wakeUpShutdown();
            }
        }
    }

    /**
     * wakes up waiting shutdown.
     */
    private synchronized void wakeUpShutdown(){
        notifyAll();
    }

}

