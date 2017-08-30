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

package org.openecomp.appc.client.impl.protocol;

import org.openecomp.appc.client.impl.core.MessageContext;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

class AsyncProtocolImpl implements AsyncProtocol {

    /**
     * message bus listener thread handler
     */
    private Future listenerHandler;
    /**
     * called when messages are fetched - called for a single message
     */
    private RetrieveMessageCallback callback;
    /**
     * message bus client used to send/fetch
     */
    private MessagingService messageService;
    /**
     * Message reader used to extract body and context from reponse message
     */
    private MessageReader messageReader;
    /**
     * Message writer used to construct meesage from body and context
     */
    private MessageWriter messageWriter;

    /**
     * shutdown indicator
     */
    private boolean isShutdown = false;

    /**
     * executor service for listener usage
     */
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final EELFLogger LOG = EELFManager.getInstance().getLogger(AsyncProtocolImpl.class);


    AsyncProtocolImpl() {

        messageService = new UEBMessagingService();
        messageReader = new APPCMessageReaderWriter();
        messageWriter = (MessageWriter) messageReader;
    }

    public void init(Properties props, RetrieveMessageCallback callback) throws ProtocolException {

        if (callback == null) {
            throw new ProtocolException("Callback param should not be null!");
        }
        this.callback = callback;

        try {
            messageService.init(props);
            //get message bus listener thread
            //start the thread after initializing services
            listenerHandler = executorService.submit(new Listener());
        } catch (GeneralSecurityException | IllegalAccessException | NoSuchFieldException | IOException e) {
            throw new ProtocolException(e);
        }
    }

    public void sendRequest(String payload, MessageContext context) throws ProtocolException {

        //get message to be sent to appc from payload and context
        String message = messageWriter.write(payload, context);
        try {
            messageService.send(context.getPartiton(), message);
            LOG.debug("Successfully send message: " + message);
        } catch (IOException e) {
            throw new ProtocolException(e);
        }
    }

    @Override
    public void shutdown() {
        isShutdown = true;
        messageService.close();
        LOG.warn("The protocol layer in shutdown stage.");
        executorService.shutdownNow();
    }

    public class Listener implements Runnable {


        public void run() {

            while (!isShutdown) {
                List<String> messages = new ArrayList<>();
                try {
                    messages = messageService.fetch();
                    LOG.debug("Successfully fetched " + messages.size() + " messages");
                } catch (IOException e) {
                    LOG.error("Fetching " + messages.size() + " messages failed");
                }
                for (String message : messages) {

                    MessageContext context = new MessageContext();
                    String payload = null;

                    try {
                        //get payload and context from message to be sent to core layer
                        payload = messageReader.read(message, context);
                        LOG.debug("Got body: " + payload);
                        //call core layer response handler
                        if(!isShutdown) {
                            callback.onResponse(payload, context);
                        }else{
                            LOG.warn("Shutdown in progress, response will not receive. Correlation ID <" +
                                    context.getCorrelationID() + "> response ", message);
                        }
                    } catch (ProtocolException e) {
                        LOG.error("Failed to read message from UEB. message is: " + message);
                    }
                }
            }
        }
    }

}
