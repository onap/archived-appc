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

import org.openecomp.appc.client.impl.core.AsyncRequestResponseHandler;
import org.openecomp.appc.client.impl.core.CoreException;
import org.openecomp.appc.client.impl.core.CoreManager;
import org.openecomp.appc.client.impl.core.ICoreAsyncResponseHandler;
import org.openecomp.appc.client.impl.core.MessageContext;
import org.openecomp.appc.client.impl.protocol.AsyncProtocol;
import org.openecomp.appc.client.impl.protocol.ProtocolException;
import org.openecomp.appc.client.impl.protocol.RetrieveMessageCallback;
import org.junit.Before;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.mockito.Mockito.mock;

public class ResponseManagerTest {

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    ICoreAsyncResponseHandler listener1 = new ListenerImpl();
    ICoreAsyncResponseHandler listener2 = new SleeepListenerImpl();
    ICoreAsyncResponseHandler listener3 = new ListenerImpl();
    CoreManager coreManager = null;

    public void initialize() throws CoreException {
        Properties prop = new Properties();
        prop.setProperty("client.pool.size", "10");
        prop.setProperty("client.response.timeout", "7000");
        coreManager = new ResponseManagerTest.CoreManagerTest(prop);
    }

    void asyncRequest(String request, ICoreAsyncResponseHandler businessCallback, String correlationId, String rpcName) throws CoreException {
        AsyncRequestResponseHandler requestResponseHandler = new AsyncRequestResponseHandler(correlationId, businessCallback, coreManager);
        requestResponseHandler.sendRequest(request, correlationId, rpcName);
    }

    public void simpleResponseTest() throws Exception {
        System.out.println("simpleResponseTest");
        asyncRequest("request 1", listener1,"vasia1", "test");
        MessageContext msgCtx = new MessageContext();
        msgCtx.setCorrelationID("vasia1");
        msgCtx.setType("response");
        coreManager.getProtocolCallback().onResponse("vasia1 response",msgCtx);
        coreManager.getProtocolCallback().onResponse("vasia2 response",msgCtx);
        Thread.sleep(10);
    }

    public void twoResponseTest() throws Exception {
        System.out.println("twoResponseTest");
        asyncRequest("twoResponseTest request 1", listener2,"vasia2", "test");
        MessageContext msgCtx = new MessageContext();
        msgCtx.setCorrelationID("vasia2");
        msgCtx.setType("response");
        coreManager.getProtocolCallback().onResponse("second of vasia2",msgCtx);
        Thread.sleep(100);
        asyncRequest("twoResponseTest request 2", listener1,"vasia1", "test");
        MessageContext msgCtx2 = new MessageContext();
        msgCtx2.setCorrelationID("vasia1");
        msgCtx2.setType("response");
        coreManager.getProtocolCallback().onResponse("first of vasia1",msgCtx2);
        Thread.sleep(150);
    }

    public void threeResponseTest() throws Exception {
        System.out.println("treeResponseTest");
        asyncRequest("threeResponseTest request 2", listener1,"vasia4", "test");
        asyncRequest("threeResponseTest request 1", listener2,"vasia2", "test");
        MessageContext msgCtx2 = new MessageContext();
        msgCtx2.setCorrelationID("vasia2");
        msgCtx2.setType("response");
        coreManager.getProtocolCallback().onResponse("second of vasia2",msgCtx2);

        asyncRequest("threeResponseTest request 2", listener1,"vasia1", "test");
        MessageContext msgCtx1 = new MessageContext();
        msgCtx1.setCorrelationID("vasia1");
        msgCtx1.setType("response");
        coreManager.getProtocolCallback().onResponse("vasia1",msgCtx1);

        asyncRequest("threeResponseTest request 3", listener3,"vasia3", "test");
        MessageContext msgCtx3 = new MessageContext();
        msgCtx3.setCorrelationID("vasia3");
        msgCtx3.setType("response");
        coreManager.getProtocolCallback().onResponse("three1",msgCtx3);

        coreManager.getProtocolCallback().onResponse("three2", msgCtx3);

        coreManager.getProtocolCallback().onResponse("first1", msgCtx1);
        Thread.sleep(250);

        coreManager.getProtocolCallback().onResponse("first2", msgCtx1);
        Thread.sleep(10000);
    }

    private class ListenerImpl implements ICoreAsyncResponseHandler{

        public boolean onResponse(String message, String type) {
            System.out.println("callback " + message);
            return message != null;
        }

        @Override
        public void onException(Exception e) {
            e.printStackTrace();
        }
    }

    private class SleeepListenerImpl implements ICoreAsyncResponseHandler{

        public boolean onResponse(String message, String type) {
            try {
                Thread.sleep(150);
                System.out.println("sleep callback " + message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return message != null;
        }

        @Override
        public void onException(Exception e) {
            e.printStackTrace();
        }
    }

    class CoreManagerTest extends CoreManager{
        CoreManagerTest(Properties properties) throws CoreException {
            super(properties);
            protocol = mock(AsyncProtocol.class);
        }
        protected void sendRequest2Protocol(String request, String corrId, String rpcName) throws CoreException {
        }

        protected void initProtocol(Properties properties, RetrieveMessageCallback protocolCallback) throws ProtocolException {

        }
    }
}
