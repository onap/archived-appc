/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 Samsung
 * ================================================================================
 * Modifications Copyright (C) 2018 Ericsson
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

package org.onap.appc.adapter.netconf.jsch;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static org.junit.Assert.assertEquals;
import com.jcraft.jsch.ChannelSubsystem;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.onap.appc.adapter.netconf.NetconfConnectionDetails;
import org.onap.appc.adapter.netconf.internal.NetconfAdapter;
import org.onap.appc.exceptions.APPCException;
import org.powermock.reflect.Whitebox;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class TestNetconfClientJsch {

    NetconfClientJsch netconfClientJsch;
    private Session mockSession;
    private JSch mockJSch;
    private ChannelSubsystem mockChannel;
    private InputStream mockInputStream;
    private OutputStream mockOutputStream;
    private NetconfAdapter mockNetconfAdapter;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void SetUp() {
        netconfClientJsch = Mockito.spy(new NetconfClientJsch());
    }

    private void setupForConnectTests() throws JSchException, IOException {
        mockSession = Mockito.mock(Session.class);
        mockJSch = Mockito.mock(JSch.class);
        mockChannel = Mockito.mock(ChannelSubsystem.class);
        mockInputStream = Mockito.mock(InputStream.class);
        mockOutputStream = Mockito.mock(OutputStream.class);
        mockNetconfAdapter = Mockito.mock(NetconfAdapter.class);
        Mockito.doReturn(mockJSch).when(netconfClientJsch).getJSch();
        Mockito.doReturn(mockSession).when(mockJSch).getSession(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyInt());
        Mockito.doReturn(mockChannel).when(mockSession).openChannel("subsystem");
        Mockito.doReturn(mockInputStream).when(mockChannel).getInputStream();
        Mockito.doReturn(mockOutputStream).when(mockChannel).getOutputStream();
        Mockito.doReturn(mockNetconfAdapter).when(netconfClientJsch)
                .getNetconfAdapter(Mockito.any(InputStream.class), Mockito.any(OutputStream.class));
    }

    @Test
    public void testConnect() throws APPCException, IOException, JSchException {
        setupForConnectTests();
        Mockito.doReturn("<hello>").when(mockNetconfAdapter).receiveMessage();
        NetconfConnectionDetails connectionDetails = new NetconfConnectionDetails();
        connectionDetails.setHost("test");
        connectionDetails.setPort(8080);
        connectionDetails.setUsername("test");
        connectionDetails.setPassword("test");
        List<String> capabilities = Arrays.asList(new String[] {
            "<capability>urn:ietf:params:netconf:base:1.1</capability>\r\n"});
        connectionDetails.setCapabilities(capabilities);
        Properties additionalProperties = new Properties();
        additionalProperties.setProperty("testKey1", "testParam1");
        connectionDetails.setAdditionalProperties(additionalProperties);
        netconfClientJsch.connect(connectionDetails);
        Mockito.verify(mockNetconfAdapter).sendMessage(
                Mockito.contains("<capability>urn:ietf:params:netconf:base:1.1</capability>"));
    }

    @Test
    public void testConnectNullMessage() throws JSchException, IOException, APPCException {
        setupForConnectTests();
        NetconfConnectionDetails connectionDetails = new NetconfConnectionDetails();
        expectedEx.expect(APPCException.class);
        expectedEx.expectMessage("Cannot establish connection to server");
        netconfClientJsch.connect(connectionDetails);
    }

    @Test
    public void testConnectNullMessageNonNullResponse()
            throws JSchException, IOException, APPCException {
        setupForConnectTests();
        Mockito.doReturn("NOT NULL RESPONSE").when(mockNetconfAdapter).receiveMessage();
        Mockito.doThrow(new JSchException()).when(mockChannel).connect(10000);
        NetconfConnectionDetails connectionDetails = new NetconfConnectionDetails();
        expectedEx.expect(APPCException.class);
        expectedEx.expectCause(allOf(isA(RuntimeException.class),
                hasProperty("message", is("Error closing netconf device"))));
        netconfClientJsch.connect(connectionDetails);
    }

    @Test
    public void testConnectErrorMessage() throws JSchException, IOException, APPCException {
        setupForConnectTests();
        Mockito.doReturn("<rpc-error>").when(mockNetconfAdapter).receiveMessage();
        NetconfConnectionDetails connectionDetails = new NetconfConnectionDetails();
        expectedEx.expect(APPCException.class);
        expectedEx
                .expectCause(allOf(isA(RuntimeException.class),
                        hasProperty("cause", allOf(isA(IOException.class),
                                hasProperty("message",
                                        containsString("Error response from netconf device:")),
                                hasProperty("message", containsString("<rpc-error>"))
                        ))));
        netconfClientJsch.connect(connectionDetails);
    }

    @Test
    public void testConnectWithSuccessfulDisconnect()
            throws JSchException, IOException, APPCException {
        setupForConnectTests();
        Mockito.doThrow(new JSchException()).when(mockChannel).connect(10000);
        Mockito.doReturn("<ok/>").when(mockNetconfAdapter).receiveMessage();
        NetconfConnectionDetails connectionDetails = new NetconfConnectionDetails();
        expectedEx.expect(APPCException.class);
        expectedEx.expectCause(allOf(isA(APPCException.class),
                hasProperty("message", is(JSchException.class.getName()))));
        netconfClientJsch.connect(connectionDetails);
    }

    @Test
    public void testGetConfiguration() throws IOException, APPCException {
        mockNetconfAdapter = Mockito.mock(NetconfAdapter.class);
        Whitebox.setInternalState(netconfClientJsch, "netconfAdapter", mockNetconfAdapter);
        Mockito.doReturn("TEST RETURN VALUE").when(mockNetconfAdapter).receiveMessage();
        assertEquals("TEST RETURN VALUE", netconfClientJsch.getConfiguration());
    }

    @Test
    public void testGetConfigurationExceptionFlow() throws IOException, APPCException {
        mockNetconfAdapter = Mockito.mock(NetconfAdapter.class);
        Whitebox.setInternalState(netconfClientJsch, "netconfAdapter", mockNetconfAdapter);
        Mockito.doThrow(new IOException()).when(mockNetconfAdapter).receiveMessage();
        expectedEx.expect(APPCException.class);
        expectedEx.expectMessage(IOException.class.getName());
        netconfClientJsch.getConfiguration();
    }

    @Test
    public void testConfigure() throws IOException, APPCException {
        mockNetconfAdapter = Mockito.mock(NetconfAdapter.class);
        Whitebox.setInternalState(netconfClientJsch, "netconfAdapter", mockNetconfAdapter);
        Mockito.doReturn("<ok/>").when(mockNetconfAdapter).receiveMessage();
        netconfClientJsch.configure(null);
        Mockito.verify(netconfClientJsch).exchangeMessage(null);
    }

    @Test
    public void testConfigureExceptionFlow() throws IOException, APPCException {
        mockNetconfAdapter = Mockito.mock(NetconfAdapter.class);
        Whitebox.setInternalState(netconfClientJsch, "netconfAdapter", mockNetconfAdapter);
        Mockito.doThrow(new IOException()).when(mockNetconfAdapter).receiveMessage();
        expectedEx.expect(APPCException.class);
        expectedEx.expectMessage("java.io.IOException");
        netconfClientJsch.configure(null);
    }
}
