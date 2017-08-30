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

package org.openecomp.appc.client.impl.core;

import org.openecomp.appc.client.impl.core.CoreException;
import org.openecomp.appc.client.impl.core.CoreManager;
import org.openecomp.appc.client.impl.core.ICoreSyncResponseHandler;
import org.openecomp.appc.client.impl.core.MessageContext;
import org.openecomp.appc.client.impl.core.SyncRequestResponseHandler;
import org.openecomp.appc.client.impl.protocol.AsyncProtocol;
import org.openecomp.appc.client.impl.protocol.ProtocolException;
import org.openecomp.appc.client.impl.protocol.RetrieveMessageCallback;
import org.junit.Assert;
import org.junit.Before;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import static org.mockito.Mockito.mock;

public class SyncFlowTest {
    CoreManager coreManager = null;

    public void initialize() throws CoreException {
        Properties prop = new Properties();
        prop.setProperty("client.pool.size", "10");
        prop.setProperty("client.response.timeout", "7000");
        coreManager = new CoreManagerTest(prop);
    }

    <T> T syncRequest(String request, ICoreSyncResponseHandler businessCallback, String correlationId, String rpcName ) throws CoreException, TimeoutException {
        SyncRequestResponseHandler requestResponseHandler = new SyncRequestResponseHandler(correlationId, businessCallback, coreManager);
        requestResponseHandler.sendRequest(request, correlationId, rpcName);
        T responseObject = (T) requestResponseHandler.getResponse();
        return responseObject;
    }

    public void blockRequestTest(){
        ICoreSyncResponseHandler handler = new ICoreSyncResponseHandlerImpl1();
        try {
            syncRequest("request 1", handler, "vasia1", "test");
        }catch (Throwable e){
            e.printStackTrace();
            Assert.assertTrue(e != null);
        }

    }

    public <T> void blockRequestSucceedTest() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        final ICoreSyncResponseHandler handler = new ICoreSyncResponseHandlerImpl1();
        try {
            executorService.submit(new Runnable() {
                public void run() {
                    System.out.println("Send request");
                    T response;
                    try {
                        response = syncRequest("request 1", handler, "vasia1", "test");
                        System.out.println("=======" + response.toString());
                    } catch (CoreException e) {
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        e.printStackTrace();
                    }
                }
            });
        }catch (Throwable e){
            Assert.assertTrue((RuntimeException)e != null);
        }
        Thread.sleep(2000);
        executorService.submit(new Runnable() {
            public void run() {
                MessageContext ctx = new MessageContext();
                ctx.setCorrelationID("vasia1");
                ctx.setType("response");
                try {
                    System.out.println("Send response 1");
                    coreManager.getProtocolCallback().onResponse("response for request 1", ctx);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Thread.sleep(2000);
        executorService.submit(new Runnable() {
            public void run() {
                MessageContext ctx = new MessageContext();
                ctx.setCorrelationID("vasia1");
                ctx.setType("response");
                try {
                    System.out.println("Send response 2");
                    coreManager.getProtocolCallback().onResponse("response for request 1 final", ctx);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        Thread.sleep(1000);

    }

    class ICoreSyncResponseHandlerImpl1 implements ICoreSyncResponseHandler{


        public <T> T onResponse(String message, String type) {
            System.out.println("Received message = " + message) ;
            if(message.contains("final")){
                return (T) new String(message);
            }
            return null;
        }
    }

    class CoreManagerTest extends CoreManager{
        CoreManagerTest(Properties properties) throws CoreException {
            super(properties);
            protocol = mock(AsyncProtocol.class);
        }
        protected void sendRequest2Protocol(String request, String corrId, String rpcName) throws CoreException {
        }

        protected void initProtocol(Properties properties, RetrieveMessageCallback protocolCallback) throws ProtocolException{

        }
    }
}
