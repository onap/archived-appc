/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 Ericsson
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
 *
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.adapter.netconf.odlconnector;

import static org.junit.Assert.assertEquals;
import java.util.Properties;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.util.httpClient;
import org.onap.appc.adapter.netconf.NetconfConnectionDetails;
import org.onap.appc.adapter.netconf.util.Constants;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

@RunWith(PowerMockRunner.class)
@PrepareForTest(httpClient.class)
public class NetconfClientRestconfImplTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void setup() {
        PowerMockito.mockStatic(httpClient.class);
    }
    @Test
    public void testConfigureNullDetails() throws APPCException {
        NetconfClientRestconfImpl client = new NetconfClientRestconfImpl();
        expectedEx.expect(APPCException.class);
        expectedEx.expectMessage("Invalid connection details - null value");
        client.configure(null);
    }

    @Test
    public void testConfigureNullProperties() throws APPCException {
        NetconfClientRestconfImpl client = Mockito.spy(new NetconfClientRestconfImpl());
        expectedEx.expect(APPCException.class);
        expectedEx.expectMessage("Invalid properties!");
        Whitebox.setInternalState(client, "connectionDetails", Mockito.mock(NetconfConnectionDetails.class));
        client.configure(null);
    }

    @Test
    public void testConfigureWithError() throws APPCException {
        PowerMockito.when(httpClient.putMethod(Constants.PROTOCOL, Constants.CONTROLLER_IP, Constants.CONTROLLER_PORT,
                Constants.CONFIGURE_PATH + "null/yang-ext:mount/MODULE_NAME:NODE_NAME", null, "application/xml"))
                .thenReturn(HttpStatus.SC_ACCEPTED);
        NetconfClientRestconfImpl client = Mockito.spy(new NetconfClientRestconfImpl());
        NetconfConnectionDetails details = new NetconfConnectionDetails();
        Properties properties = new Properties();
        properties.setProperty("module.name", "MODULE_NAME");
        properties.setProperty("node.name", "NODE_NAME");
        details.setAdditionalProperties(properties);
        Whitebox.setInternalState(client, "connectionDetails", details);
        expectedEx.expect(APPCException.class);
        expectedEx.expectMessage("Error configuring node :NODE_NAME, of Module :MODULE_NAME, in device :null");
        client.configure(null);
    }

    @Test
    public void testConfigure4ArgWithError() throws APPCException {
        PowerMockito.when(httpClient.putMethod(Constants.PROTOCOL, Constants.CONTROLLER_IP, Constants.CONTROLLER_PORT,
                Constants.CONFIGURE_PATH + "null/yang-ext:mount/MODULE_NAME:NODE_NAME", null, "application/xml"))
                .thenReturn(HttpStatus.SC_ACCEPTED);
        NetconfClientRestconfImpl client = Mockito.spy(new NetconfClientRestconfImpl());
        expectedEx.expect(APPCException.class);
        expectedEx.expectMessage("Error configuring node :NODE_NAME, of Module :MODULE_NAME, in device :null");
        client.configure(null, null, "MODULE_NAME", "NODE_NAME");
    }

    @Test
    public void testConnect() throws APPCException {
        PowerMockito.when(httpClient.putMethod(Constants.PROTOCOL, Constants.CONTROLLER_IP, Constants.CONTROLLER_PORT,
                Constants.CONFIGURE_PATH + "null/yang-ext:mount/MODULE_NAME:NODE_NAME", null, "application/xml"))
                .thenReturn(HttpStatus.SC_ACCEPTED);
        NetconfClientRestconfImpl client = Mockito.spy(new NetconfClientRestconfImpl());
        NetconfConnectionDetails details = new NetconfConnectionDetails();
        Properties properties = new Properties();
        properties.setProperty("module.name", "MODULE_NAME");
        properties.setProperty("node.name", "NODE_NAME");
        details.setAdditionalProperties(properties);
        Whitebox.setInternalState(client, "connectionDetails", details);
        expectedEx.expect(APPCException.class);
        expectedEx.expectMessage("Error connecting device :null");
        client.connect(details);
    }

    @Test
    public void testConnectWithNullDetails() throws APPCException {
        PowerMockito.when(httpClient.putMethod(Constants.PROTOCOL, Constants.CONTROLLER_IP, Constants.CONTROLLER_PORT,
                Constants.CONFIGURE_PATH + "null/yang-ext:mount/MODULE_NAME:NODE_NAME", null, "application/xml"))
                .thenReturn(HttpStatus.SC_ACCEPTED);
        NetconfClientRestconfImpl client = Mockito.spy(new NetconfClientRestconfImpl());
        expectedEx.expect(APPCException.class);
        expectedEx.expectMessage("Invalid connection details - null value");
        client.connect(null);
    }

    @Test
    public void testDisconnectNullDetails() throws APPCException {
        NetconfClientRestconfImpl client = Mockito.spy(new NetconfClientRestconfImpl());
        expectedEx.expect(APPCException.class);
        expectedEx.expectMessage("Invalid connection details - null value");
        client.disconnect();
    }

    @Test
    public void testDisconnect() throws APPCException {
        NetconfClientRestconfImpl client = Mockito.spy(new NetconfClientRestconfImpl());
        NetconfConnectionDetails details = new NetconfConnectionDetails();
        Properties properties = new Properties();
        properties.setProperty("module.name", "MODULE_NAME");
        properties.setProperty("node.name", "NODE_NAME");
        details.setAdditionalProperties(properties);
        Whitebox.setInternalState(client, "connectionDetails", details);
        expectedEx.expect(APPCException.class);
        expectedEx.expectMessage("Disconnection of device null failed!");
        client.disconnect();
    }

    @Test
    public void testGetConfigurationNullDetails() throws APPCException {
        NetconfClientRestconfImpl client = Mockito.spy(new NetconfClientRestconfImpl());
        expectedEx.expect(APPCException.class);
        expectedEx.expectMessage("Invalid connection details - null value");
        client.getConfiguration();
    }

    @Test
    public void testGetConfigurationNullProperties() throws APPCException {
        NetconfClientRestconfImpl client = Mockito.spy(new NetconfClientRestconfImpl());
        expectedEx.expect(APPCException.class);
        expectedEx.expectMessage("Invalid properties!");
        Whitebox.setInternalState(client, "connectionDetails", Mockito.mock(NetconfConnectionDetails.class));
        client.getConfiguration();
    }

    @Test
    public void testGetConfigurationWithError() throws APPCException {
        PowerMockito.when(httpClient.putMethod(Constants.PROTOCOL, Constants.CONTROLLER_IP, Constants.CONTROLLER_PORT,
                Constants.CONFIGURE_PATH + "null/yang-ext:mount/MODULE_NAME:NODE_NAME", null, "application/xml"))
                .thenReturn(HttpStatus.SC_ACCEPTED);
        NetconfClientRestconfImpl client = Mockito.spy(new NetconfClientRestconfImpl());
        NetconfConnectionDetails details = new NetconfConnectionDetails();
        Properties properties = new Properties();
        properties.setProperty("module.name", "MODULE_NAME");
        properties.setProperty("node.name", "NODE_NAME");
        details.setAdditionalProperties(properties);
        Whitebox.setInternalState(client, "connectionDetails", details);
        expectedEx.expect(APPCException.class);
        expectedEx.expectMessage("Error getting configuration of node :NODE_NAME, of Module :MODULE_NAME, in device :null");
        client.getConfiguration();
    }

    @Test
    public void testGetConfigurationSuccess() throws APPCException {
        PowerMockito.when(httpClient.getMethod(Constants.PROTOCOL, Constants.CONTROLLER_IP, Constants.CONTROLLER_PORT,
                Constants.CONFIGURE_PATH + "null/yang-ext:mount/MODULE_NAME:NODE_NAME", "application/json"))
                .thenReturn("TEST");
        NetconfClientRestconfImpl client = Mockito.spy(new NetconfClientRestconfImpl());
        NetconfConnectionDetails details = new NetconfConnectionDetails();
        Properties properties = new Properties();
        properties.setProperty("module.name", "MODULE_NAME");
        properties.setProperty("node.name", "NODE_NAME");
        details.setAdditionalProperties(properties);
        Whitebox.setInternalState(client, "connectionDetails", details);
        assertEquals("TEST", client.getConfiguration());
    }
    
    @Test
    public void testCheckConnection() throws APPCException {
        NetconfClientRestconfImpl client = new NetconfClientRestconfImpl();
        assertEquals(false, client.checkConnection(null));

    }
}
