/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2018 Nokia Solutions and Networks
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
package org.onap.appc.listener.LCM.operation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;

import com.att.aft.dme2.internal.jersey.core.util.Base64;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.listener.LCM.operation.ProviderOperations.MySSLSocketFactory;

public class ProviderOperationsTest {

    private ProviderOperations providerOperations;
    private MySSLSocketFactory socketFactory;

    @Mock
    private KeyStore mockKeyStore;


    @Before
    public void setup()
        throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        providerOperations =
            new ProviderOperations("http://127.0.0.1", "test_user", "test_password");
        socketFactory = new MySSLSocketFactory(mockKeyStore);
    }

    @Test
    public void setAuthentication_should_return_null_given_null_arguments() {
        String newAuthentication = providerOperations.setAuthentication(null, null);
        assertNull(newAuthentication);
    }

    @Test
    public void should_set_properties() {
        providerOperations.setUrl("hp://123.1.2.3");
        assertEquals("http://127.0.0.1", providerOperations.getUrl());
        providerOperations.setUrl("http://123.1.2.3");
        assertEquals("http://123.1.2.3", providerOperations.getUrl());

        String newAuthentication = providerOperations.setAuthentication("new_user", "new_password");
        assertEquals("new_user:new_password", Base64.base64Decode(newAuthentication));
    }

    @Test
    public void isSucceeded_should_resolve_status_codes() {

        assertFalse(ProviderOperations.isSucceeded(null));
        assertFalse(ProviderOperations.isSucceeded(200));
        assertTrue(ProviderOperations.isSucceeded(100));
        assertTrue(ProviderOperations.isSucceeded(400));
    }

    @Test(expected = APPCException.class)
    public void topologyDG_should_throw_given_null_message() throws APPCException {

        providerOperations.topologyDG("test-rpc-name", null);
    }

    @Test(expected = SocketException.class)
    public void sslSocketFactory_should_throw_when_socket_not_connected() throws IOException {
        Socket socket = socketFactory.createSocket();
        assertNotNull(socket);

        socketFactory.createSocket(socket, "127.0.0.1", 123, true);
    }

    //TODO write some test cases for topologyDG method
}
