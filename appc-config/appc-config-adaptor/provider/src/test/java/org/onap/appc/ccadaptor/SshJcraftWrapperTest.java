/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
 * Modifications Copyright (C) 2018 IBM.
 * ================================================================================
 * Modifications Copyright (C) 2018 Ericsson
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

package org.onap.appc.ccadaptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import com.google.common.base.Charsets;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.ChannelSubsystem;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.onap.appc.ccadaptor.SshJcraftWrapper.MyUserInfo;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DataOutputStream.class)
public class SshJcraftWrapperTest {

    private SshJcraftWrapper wrapper;
    private File debugFile;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void setupForTests() throws IOException, InterruptedException {
        wrapper = Mockito.spy(new SshJcraftWrapper());
        Mockito.doNothing().when(wrapper).delay(Mockito.anyInt());
        debugFile = new File("src/test/resources/sshJcraftWrapperDebug");
        File debugFile2 = new File("src/test/resources/sshJcraftWrapperDEBUG");
        File configFile = new File("src/test/resources/jcraftReadSwConfigFileFromDisk");
        File proxyRouterLogFile = new File("src/test/resources/proxyRouterLogFile");
        debugFile.getParentFile().mkdirs();
        debugFile.createNewFile();
        debugFile2.createNewFile();
        configFile.createNewFile();
        proxyRouterLogFile.createNewFile();
        Whitebox.setInternalState(wrapper, "debugLogFileName",
                "src/test/resources/sshJcraftWrapperDebug");
        Whitebox.setInternalState(wrapper, "extraDebugFile", debugFile2);
        Whitebox.setInternalState(wrapper, "jcraftReadSwConfigFileFromDisk", configFile);
    }

    @Test
    public void testConnect() throws IOException, JSchException, InterruptedException {
        JSch mockJSch = Mockito.mock(JSch.class);
        Session mockSession = Mockito.mock(Session.class);
        ChannelShell mockChannel = Mockito.mock(ChannelShell.class);
        InputStream stubInputStream = IOUtils.toInputStream("hello\n]]>]]>", Charsets.UTF_8);
        Mockito.doReturn(stubInputStream).when(mockChannel).getInputStream();
        Mockito.doReturn(mockChannel).when(mockSession).openChannel("shell");
        Mockito.doReturn(mockSession).when(mockJSch).getSession("testUser", "testHost", 22);
        Mockito.doReturn(mockJSch).when(wrapper).getJSch();
        wrapper.connect("testHost", "testUser", "testPswd", "]]>]]>", 1000);
        Mockito.verify(wrapper, Mockito.times(9)).appendToFile(Mockito.anyString(),
                Mockito.anyString());
    }

    @Test
    public void testConnectExceptionFlow() throws IOException, JSchException {
        JSch mockJSch = Mockito.mock(JSch.class);
        Mockito.doThrow(new JSchException()).when(mockJSch).getSession("testUser", "testHost", 22);
        Mockito.doReturn(mockJSch).when(wrapper).getJSch();
        expectedEx.expect(IOException.class);
        expectedEx.expectMessage("com.jcraft.jsch.JSchException");
        wrapper.connect("testHost", "testUser", "testPswd", "]]>]]>", 1000);
    }

    @Test
    public void testConnectExceptionFlow2() throws IOException, JSchException {
        JSch mockJSch = Mockito.mock(JSch.class);
        Session mockSession = Mockito.mock(Session.class);
        ChannelShell mockChannel = Mockito.mock(ChannelShell.class);
        Mockito.doReturn(mockChannel).when(mockSession).openChannel("shell");
        Mockito.doThrow(new IOException()).when(wrapper).receiveUntil("]]>]]>", 3000,
                "No cmd was sent, just waiting");
        Mockito.doReturn(mockSession).when(mockJSch).getSession("testUser", "testHost", 22);
        Mockito.doReturn(mockJSch).when(wrapper).getJSch();
        wrapper.connect("testHost", "testUser", "testPswd", "]]>]]>", 1000);
        Mockito.verify(wrapper, Mockito.times(1)).receiveUntil("]]>]]>", 3000,
                "No cmd was sent, just waiting");
    }

    @Test
    public void testConnectWithPortNumber() throws IOException, JSchException {
        JSch mockJSch = Mockito.mock(JSch.class);
        Session mockSession = Mockito.mock(Session.class);
        ChannelShell mockChannel = Mockito.mock(ChannelShell.class);
        InputStream stubInputStream = IOUtils.toInputStream("hello\n:~#", Charsets.UTF_8);
        Mockito.doReturn(stubInputStream).when(mockChannel).getInputStream();
        Mockito.doReturn(mockChannel).when(mockSession).openChannel("shell");
        Mockito.doReturn(mockSession).when(mockJSch).getSession("testUser", "testHost", 22);
        Mockito.doReturn(mockJSch).when(wrapper).getJSch();
        wrapper.connect("testHost", "testUser", "testPswd", ":~#", 1000, 22);
        Mockito.verify(wrapper, Mockito.times(9)).appendToFile(Mockito.anyString(),
                Mockito.anyString());
    }

    @Test
    public void testConnectWithPortNumberSuccessFlow2()
            throws IOException, JSchException, InterruptedException {
        JSch mockJSch = Mockito.mock(JSch.class);
        Session mockSession = Mockito.mock(Session.class);
        ChannelShell mockChannel = Mockito.mock(ChannelShell.class);
        InputStream stubInputStream = IOUtils.toInputStream("hello\n]]>]]>", Charsets.UTF_8);
        Mockito.doReturn(stubInputStream).when(mockChannel).getInputStream();
        Mockito.doReturn(mockChannel).when(mockSession).openChannel("shell");
        Mockito.doReturn(mockSession).when(mockJSch).getSession("testUser", "testHost", 22);
        Mockito.doReturn(mockJSch).when(wrapper).getJSch();
        wrapper.connect("testHost", "testUser", "testPswd", "]]>]]>", 1000, 22);
        Mockito.verify(wrapper, Mockito.times(9)).appendToFile(Mockito.anyString(),
                Mockito.anyString());
    }

    @Test
    public void testConnectWithPortNumberExceptionFlow() throws IOException, JSchException {
        JSch mockJSch = Mockito.mock(JSch.class);
        Mockito.doThrow(new JSchException()).when(mockJSch).getSession("testUser", "testHost", 22);
        Mockito.doReturn(mockJSch).when(wrapper).getJSch();
        expectedEx.expect(IOException.class);
        expectedEx.expectMessage("com.jcraft.jsch.JSchException");
        wrapper.connect("testHost", "testUser", "testPswd", "]]>]]>", 1000, 22);
    }

    @Test
    public void testConnectWithPortNumberExceptionFlow2() throws IOException, JSchException {
        JSch mockJSch = Mockito.mock(JSch.class);
        Session mockSession = Mockito.mock(Session.class);
        ChannelShell mockChannel = Mockito.mock(ChannelShell.class);
        InputStream stubInputStream = IOUtils.toInputStream("", Charsets.UTF_8);
        Mockito.doReturn(stubInputStream).when(mockChannel).getInputStream();
        Mockito.doReturn(mockChannel).when(mockSession).openChannel("shell");
        Mockito.doReturn(mockSession).when(mockJSch).getSession("testUser", "testHost", 22);
        Mockito.doReturn(mockJSch).when(wrapper).getJSch();
        wrapper.connect("testHost", "testUser", "testPswd", "]]>]]>", 1000, 22);
        Mockito.verify(wrapper, Mockito.times(1)).receiveUntil("]]>]]>", 10000,
                "No cmd was sent, just waiting");
    }

    @Test
    public void testReceiveUntilTimeout() throws TimedOutException, IOException, JSchException {
        Session mockSession = Mockito.mock(Session.class);
        Whitebox.setInternalState(wrapper, "session", mockSession);
        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws InterruptedException {
                Thread.sleep(1);
                return null;
            }
        }).when(mockSession).setTimeout(0);
        expectedEx.expect(IOException.class);
        expectedEx.expectMessage("Timeout: time in routine has exceed our deadline");
        wrapper.receiveUntil("", 0, "");
    }

    @Test
    public void testReceiveUntilReaderTimeout()
            throws TimedOutException, IOException, JSchException {
        Session mockSession = Mockito.mock(Session.class);
        Whitebox.setInternalState(wrapper, "session", mockSession);
        BufferedReader mockReader = Mockito.mock(BufferedReader.class);
        Whitebox.setInternalState(wrapper, "reader", mockReader);
        expectedEx.expect(IOException.class);
        expectedEx.expectMessage("Received a SocketTimeoutException router=");
        wrapper.receiveUntil("", 3000, "");
    }

    @Test
    public void testReceiveUntilIosXr() throws TimedOutException, IOException, JSchException {
        Session mockSession = Mockito.mock(Session.class);
        Whitebox.setInternalState(wrapper, "session", mockSession);
        BufferedReader mockReader = Mockito.mock(BufferedReader.class);
        Mockito.doReturn(3).when(mockReader).read(Mockito.anyObject(), Mockito.anyInt(),
                Mockito.anyInt());
        Whitebox.setInternalState(wrapper, "reader", mockReader);
        Mockito.doReturn(false).when(wrapper).jcraftReadSwConfigFileFromDisk();
        Mockito.doReturn("\nXML>").when(wrapper).getLastFewLinesOfFile(Mockito.anyObject(),
                Mockito.anyInt());

        assertNull(wrapper.receiveUntil("]]>]]>", 3000, "IOS_XR_uploadedSwConfigCmd\nOTHER\nXML>"));
    }

    @Test
    public void testReceiveStringDelimiters() {
        assertEquals(false, wrapper.checkIfReceivedStringMatchesDelimeter("#$", "", ""));
    }

    @Test
    public void testReceiveStringDelimitersShowConfig() {
        assertEquals(true, wrapper.checkIfReceivedStringMatchesDelimeter("]]>]]>", "]]>]]>\n #",
                "show config"));
    }

    @Test
    public void testReceiveStringDelimitersTwoArg() throws IOException {
        SshJcraftWrapper localWrapper = Mockito.spy(new SshJcraftWrapper());
        Mockito.doReturn(true).when(localWrapper).jcraftReadSwConfigFileFromDisk();
        Mockito.doThrow(new IOException()).when(localWrapper)
                .getLastFewLinesOfFile(Mockito.anyObject(), Mockito.anyInt());
        Whitebox.setInternalState(localWrapper, "routerFileName", "DUMMY_FILE_NAME");
        assertEquals(false, localWrapper.checkIfReceivedStringMatchesDelimeter(3, "]]>]]>\n #"));
    }

    @Test
    public void testCloseConnection() {
        Session mockSession = Mockito.mock(Session.class);
        Whitebox.setInternalState(wrapper, "session", mockSession);
        wrapper.closeConnection();
        Mockito.verify(mockSession, Mockito.times(1)).disconnect();
    }

    @Test
    public void testSend() throws IOException {
        ChannelShell mockChannel = Mockito.mock(ChannelShell.class);
        OutputStream mockOutputStream = Mockito.mock(BufferedOutputStream.class);
        Mockito.doReturn(mockOutputStream).when(mockChannel).getOutputStream();
        DataOutputStream mockDos = PowerMockito.spy(new DataOutputStream(mockOutputStream));
        PowerMockito.doReturn(mockDos).when(wrapper).getDataOutputStream(Mockito.anyObject());
        Whitebox.setInternalState(wrapper, "channel", mockChannel);
        wrapper.send("TEST COMMAND\n");
        Mockito.verify(wrapper, Mockito.times(2)).appendToFile(Mockito.anyString(),
                Mockito.anyString());
    }

    @Test
    public void testSendExceptionFlow() throws IOException {
        ChannelShell mockChannel = Mockito.mock(ChannelShell.class);
        OutputStream mockOutputStream = Mockito.mock(BufferedOutputStream.class);
        Mockito.doReturn(mockOutputStream).when(mockChannel).getOutputStream();
        DataOutputStream mockDos = PowerMockito.spy(new DataOutputStream(mockOutputStream));
        PowerMockito.doThrow(new IOException()).when(mockDos).flush();
        PowerMockito.doReturn(mockDos).when(wrapper).getDataOutputStream(Mockito.anyObject());
        Whitebox.setInternalState(wrapper, "channel", mockChannel);
        expectedEx.expect(IOException.class);
        expectedEx.expectMessage("java.io.IOException");
        wrapper.send("TEST COMMAND");
    }

    @Test
    public void testSendChar() throws IOException {
        ChannelShell mockChannel = Mockito.mock(ChannelShell.class);
        OutputStream mockOutputStream = Mockito.mock(BufferedOutputStream.class);
        Mockito.doReturn(mockOutputStream).when(mockChannel).getOutputStream();
        DataOutputStream mockDos = PowerMockito.spy(new DataOutputStream(mockOutputStream));
        PowerMockito.doReturn(mockDos).when(wrapper).getDataOutputStream(Mockito.anyObject());
        Whitebox.setInternalState(wrapper, "channel", mockChannel);
        wrapper.sendChar(74);
        Mockito.verify(mockDos).flush();
    }

    @Test
    public void testSendCharExceptionFlow() throws IOException {
        ChannelShell mockChannel = Mockito.mock(ChannelShell.class);
        OutputStream mockOutputStream = Mockito.mock(BufferedOutputStream.class);
        Mockito.doReturn(mockOutputStream).when(mockChannel).getOutputStream();
        DataOutputStream mockDos = PowerMockito.spy(new DataOutputStream(mockOutputStream));
        PowerMockito.doThrow(new IOException()).when(mockDos).flush();
        PowerMockito.doReturn(mockDos).when(wrapper).getDataOutputStream(Mockito.anyObject());
        Whitebox.setInternalState(wrapper, "channel", mockChannel);
        expectedEx.expect(IOException.class);
        expectedEx.expectMessage("java.io.IOException");
        wrapper.sendChar(65);
    }

    @Test
    public void testSendByteArrayExceptionFlow() throws IOException {
        ChannelShell mockChannel = Mockito.mock(ChannelShell.class);
        OutputStream mockOutputStream = Mockito.mock(BufferedOutputStream.class);
        Mockito.doReturn(mockOutputStream).when(mockChannel).getOutputStream();
        DataOutputStream mockDos = PowerMockito.spy(new DataOutputStream(mockOutputStream));
        PowerMockito.doThrow(new IOException()).when(mockDos).flush();
        PowerMockito.doReturn(mockDos).when(wrapper).getDataOutputStream(Mockito.anyObject());
        Whitebox.setInternalState(wrapper, "channel", mockChannel);
        expectedEx.expect(IOException.class);
        expectedEx.expectMessage("java.io.IOException");
        byte[] byteArray = new byte[] {65, 74};
        wrapper.send(byteArray, 0, 2);
    }

    @Test
    public void testGetLastFewLinesOfFile() throws FileNotFoundException, IOException {
        File file = new File("src/test/resources/TEST_FILE.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(file.getPath()));
        writer.write("line1\nline2");
        writer.flush();
        writer.close();
        assertEquals("\nline2", wrapper.getLastFewLinesOfFile(file, 2));
    }

    @Test
    public void testReceiveUntilBufferFlush() throws TimedOutException, IOException {
        Session mockSession = Mockito.mock(Session.class);
        Whitebox.setInternalState(wrapper, "session", mockSession);
        BufferedReader mockReader = Mockito.mock(BufferedReader.class);
        Mockito.doReturn(12).when(mockReader).read(Mockito.anyObject(), Mockito.anyInt(),
                Mockito.anyInt());
        Whitebox.setInternalState(wrapper, "reader", mockReader);

        wrapper.receiveUntilBufferFlush(12, 100, "TEST_MESSAGE");
        Mockito.verify(wrapper, Mockito.times(2)).logMemoryUsage();
    }

    @Test
    public void testReceiveUntilBufferFlushTimeout()
            throws TimedOutException, IOException, JSchException {
        Session mockSession = Mockito.mock(Session.class);
        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) throws InterruptedException {
                Thread.sleep(1);
                return null;
            }
        }).when(mockSession).setTimeout(0);
        Whitebox.setInternalState(wrapper, "session", mockSession);
        expectedEx.expect(IOException.class);
        expectedEx.expectMessage("Timeout: time in routine has exceed our deadline");
        wrapper.receiveUntilBufferFlush(10, 0, "TEST_MESSAGE");
    }

    @Test
    public void testReceiveUntilBufferFlushJSchException()
            throws TimedOutException, IOException, JSchException {
        Session mockSession = Mockito.mock(Session.class);
        Mockito.doThrow(new JSchException()).when(mockSession).setTimeout(0);
        Whitebox.setInternalState(wrapper, "session", mockSession);
        expectedEx.expect(TimedOutException.class);
        expectedEx.expectMessage("com.jcraft.jsch.JSchException");
        wrapper.receiveUntilBufferFlush(10, 0, "TEST_MESSAGE");
    }

    @Test
    public void testSftpPutSourceToDest() throws JSchException, IOException {
        Whitebox.setInternalState(wrapper, "hostName", "testHost");
        Whitebox.setInternalState(wrapper, "userName", "testUser");
        Whitebox.setInternalState(wrapper, "passWord", "testPwd");
        ChannelSftp mockChannel = Mockito.mock(ChannelSftp.class);
        JSch mockJsch = Mockito.mock(JSch.class);
        Session mockSession = Mockito.mock(Session.class);
        Mockito.doReturn(mockSession).when(mockJsch).getSession("testUser", "testHost", 22);
        Mockito.doNothing().when(mockSession).setPassword(Mockito.anyString());
        Mockito.doNothing().when(mockSession).connect();
        Mockito.doReturn(mockChannel).when(mockSession).openChannel("sftp");
        Whitebox.setInternalState(wrapper, "jsch", mockJsch);
        wrapper.sftpPut("DUMMY_SRC_PATH", "DUMMY_DEST_DIRECTORY");
        Mockito.verify(mockSession).disconnect();
    }

    @Test
    public void testSftpPutSourceToDestExceptionFlow() throws JSchException, IOException {
        JSch mockJsch = Mockito.mock(JSch.class);
        Mockito.doThrow(new JSchException()).when(mockJsch).getSession(null, null, 22);
        Whitebox.setInternalState(wrapper, "jsch", mockJsch);
        expectedEx.expect(IOException.class);
        expectedEx.expectMessage("com.jcraft.jsch.JSchException");
        wrapper.sftpPut("DUMMY_SRC_PATH", "DUMMY_DEST_DIRECTORY");
    }

    @Test
    public void testSftpPutStringToDest() throws JSchException, IOException {
        Whitebox.setInternalState(wrapper, "hostName", "testHost");
        Whitebox.setInternalState(wrapper, "userName", "testUser");
        Whitebox.setInternalState(wrapper, "passWord", "testPwd");
        ChannelSftp mockChannel = Mockito.mock(ChannelSftp.class);
        JSch mockJsch = Mockito.mock(JSch.class);
        Session mockSession = Mockito.mock(Session.class);
        Mockito.doReturn(mockSession).when(mockJsch).getSession("testUser", "testHost", 22);
        Mockito.doNothing().when(mockSession).setPassword(Mockito.anyString());
        Mockito.doNothing().when(mockSession).connect();
        Mockito.doReturn(mockChannel).when(mockSession).openChannel("sftp");
        Whitebox.setInternalState(wrapper, "jsch", mockJsch);
        wrapper.SftpPut("DUMMY_STRING", "DUMMY_DEST_DIRECTORY");
        Mockito.verify(mockSession).disconnect();
    }

    @Test
    public void testSftpPutStringToDestExceptionFlow() throws JSchException, IOException {
        JSch mockJsch = Mockito.mock(JSch.class);
        Mockito.doThrow(new JSchException()).when(mockJsch).getSession(null, null, 22);
        Whitebox.setInternalState(wrapper, "jsch", mockJsch);
        expectedEx.expect(IOException.class);
        expectedEx.expectMessage("com.jcraft.jsch.JSchException");
        wrapper.SftpPut("DUMMY_STRING", "DUMMY_DEST_DIRECTORY");
    }

    @Test
    public void testSftpGet() throws JSchException, IOException, SftpException {
        File file = new File("src/test/resources/TEST_FILE.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(file.getPath()));
        writer.write("line1\nline2");
        writer.flush();
        Whitebox.setInternalState(wrapper, "hostName", "testHost");
        Whitebox.setInternalState(wrapper, "userName", "testUser");
        Whitebox.setInternalState(wrapper, "passWord", "testPwd");
        ChannelSftp mockChannel = Mockito.mock(ChannelSftp.class);
        JSch mockJsch = Mockito.mock(JSch.class);
        Session mockSession = Mockito.mock(Session.class);
        Mockito.doReturn(mockSession).when(mockJsch).getSession("testUser", "testHost", 22);
        Mockito.doNothing().when(mockSession).setPassword(Mockito.anyString());
        Mockito.doNothing().when(mockSession).connect();
        Mockito.doReturn(mockChannel).when(mockSession).openChannel("sftp");
        Mockito.doReturn(new FileInputStream(file)).when(mockChannel)
                .get("src/test/resources/TEST_FILE.txt");
        Whitebox.setInternalState(wrapper, "jsch", mockJsch);;
        assertEquals("line1\nline2", wrapper.sftpGet("src/test/resources/TEST_FILE.txt"));
    }

    @Test
    public void testSftpGetExceptionFlow() throws JSchException, IOException {
        JSch mockJsch = Mockito.mock(JSch.class);
        Mockito.doThrow(new JSchException()).when(mockJsch).getSession(null, null, 22);
        Whitebox.setInternalState(wrapper, "jsch", mockJsch);
        expectedEx.expect(IOException.class);
        expectedEx.expectMessage("com.jcraft.jsch.JSchException");
        wrapper.sftpGet("DUMMY_FILE_PATH");
    }

    @Test
    public void testConnectWithSubsystem() throws IOException, JSchException, InterruptedException {
        JSch mockJSch = Mockito.mock(JSch.class);
        Session mockSession = Mockito.mock(Session.class);
        ChannelSubsystem mockChannel = Mockito.mock(ChannelSubsystem.class);
        InputStream stubInputStream = IOUtils.toInputStream("hello\n:~#", Charsets.UTF_8);
        Mockito.doReturn(stubInputStream).when(mockChannel).getInputStream();
        Mockito.doReturn(mockChannel).when(mockSession).openChannel("subsystem");
        Mockito.doReturn(mockSession).when(mockJSch).getSession("testUser", "testHost", 22);
        Mockito.doReturn(mockJSch).when(wrapper).getJSch();
        wrapper.connect("testHost", "testUser", "testPswd", ":~#", 1000, 22, "testSubsystem");
        Mockito.verify(mockChannel).connect();
    }

    @Test
    public void testConnectWithSubsystemExceptionFlow() throws IOException, JSchException {
        JSch mockJSch = Mockito.mock(JSch.class);
        Mockito.doThrow(new JSchException()).when(mockJSch).getSession("testUser", "testHost", 22);
        Mockito.doReturn(mockJSch).when(wrapper).getJSch();
        expectedEx.expect(IOException.class);
        expectedEx.expectMessage("com.jcraft.jsch.JSchException");
        wrapper.connect("testHost", "testUser", "testPswd", "]]>]]>", 1000, 22, "testSubsystem");
    }

    @Test
    public void testConnectShellFourParameters() throws IOException, JSchException {
        JSch mockJSch = Mockito.mock(JSch.class);
        Session mockSession = Mockito.mock(Session.class);
        ChannelShell mockChannel = Mockito.mock(ChannelShell.class);
        InputStream stubInputStream = IOUtils.toInputStream("hello\n]]>]]>", Charsets.UTF_8);
        Mockito.doReturn(stubInputStream).when(mockChannel).getInputStream();
        Mockito.doReturn(mockChannel).when(mockSession).openChannel("shell");
        Mockito.doReturn(mockSession).when(mockJSch).getSession("testUser", "testHost", 22);
        Mockito.doReturn(null).when(wrapper).receiveUntil(":~#", 9000,
                "No cmd was sent, just waiting, but we can stop on a '~#'");
        Mockito.doReturn(mockJSch).when(wrapper).getJSch();
        wrapper.connect("testHost", "testUser", "testPswd", 22);
        Mockito.verify(mockChannel).connect();
    }

    @Test
    public void testConnectShellFourParametersExceptionFlow() throws IOException, JSchException {
        JSch mockJSch = Mockito.mock(JSch.class);
        Mockito.doThrow(new JSchException()).when(mockJSch).getSession("testUser", "testHost", 22);
        Mockito.doReturn(mockJSch).when(wrapper).getJSch();
        expectedEx.expect(IOException.class);
        expectedEx.expectMessage("com.jcraft.jsch.JSchException");
        wrapper.connect("testHost", "testUser", "testPswd", 22);
    }

    @Test
    public void testPutInputStreamToDest() throws JSchException, IOException {
        ChannelSftp mockChannel = Mockito.mock(ChannelSftp.class);
        JSch mockJsch = Mockito.mock(JSch.class);
        Session mockSession = Mockito.mock(Session.class);
        Mockito.doReturn(mockSession).when(mockJsch).getSession("testUser", "testHost", 22);
        Mockito.doNothing().when(mockSession).setPassword(Mockito.anyString());
        Mockito.doNothing().when(mockSession).connect(30 * 1000);
        Mockito.doReturn(mockChannel).when(mockSession).openChannel("sftp");
        Mockito.doReturn(mockJsch).when(wrapper).getJSch();
        InputStream inputStream = Mockito.mock(InputStream.class);
        wrapper.put(inputStream, "DUMMY_DEST_PATH/", "testHost", "testUser", "testPswd");
        Mockito.verify(mockSession).disconnect();
    }

    @Test
    public void testPutInputStreamToDestExceptionFlow()
            throws JSchException, IOException, SftpException {
        ChannelSftp mockChannel = Mockito.mock(ChannelSftp.class);
        Mockito.doThrow(new SftpException(0, null)).when(mockChannel).rm("DUMMY_DEST_PATH/*");
        JSch mockJsch = Mockito.mock(JSch.class);
        Session mockSession = Mockito.mock(Session.class);
        Mockito.doReturn(mockSession).when(mockJsch).getSession("testUser", "testHost", 22);
        Mockito.doNothing().when(mockSession).setPassword(Mockito.anyString());
        Mockito.doNothing().when(mockSession).connect(30 * 1000);
        Mockito.doReturn(mockChannel).when(mockSession).openChannel("sftp");
        Mockito.doReturn(mockJsch).when(wrapper).getJSch();
        InputStream inputStream = Mockito.mock(InputStream.class);
        expectedEx.expect(IOException.class);
        expectedEx.expectMessage("0: null");
        wrapper.put(inputStream, "DUMMY_DEST_PATH/", "testHost", "testUser", "testPswd");
    }

    @Test
    public void testPutInputStreamToDestExceptionFlow2()
            throws JSchException, IOException, SftpException {
        ChannelSftp mockChannel = Mockito.mock(ChannelSftp.class);
        Mockito.doThrow(new SftpException(0, "No such file")).when(mockChannel)
                .rm("DUMMY_DEST_PATH/*");
        JSch mockJsch = Mockito.mock(JSch.class);
        Session mockSession = Mockito.mock(Session.class);
        Mockito.doReturn(mockSession).when(mockJsch).getSession("testUser", "testHost", 22);
        Mockito.doNothing().when(mockSession).setPassword(Mockito.anyString());
        Mockito.doNothing().when(mockSession).connect(30 * 1000);
        Mockito.doReturn(mockChannel).when(mockSession).openChannel("sftp");
        Mockito.doReturn(mockJsch).when(wrapper).getJSch();
        InputStream inputStream = Mockito.mock(InputStream.class);
        wrapper.put(inputStream, "DUMMY_DEST_PATH/", "testHost", "testUser", "testPswd");
        Mockito.verify(mockSession).disconnect();
    }

    @Test
    public void testGet() throws JSchException, IOException, SftpException {
        File file = new File("src/test/resources/TEST_FILE.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(file.getPath()));
        writer.write("line1\nline2");
        writer.flush();
        Whitebox.setInternalState(wrapper, "hostName", "testHost");
        Whitebox.setInternalState(wrapper, "userName", "testUser");
        Whitebox.setInternalState(wrapper, "passWord", "testPwd");
        ChannelSftp mockChannel = Mockito.mock(ChannelSftp.class);
        JSch mockJsch = Mockito.mock(JSch.class);
        Session mockSession = Mockito.mock(Session.class);
        Mockito.doReturn(mockSession).when(mockJsch).getSession("testUser", "testHost", 22);
        Mockito.doNothing().when(mockSession).setPassword(Mockito.anyString());
        Mockito.doNothing().when(mockSession).connect();
        Mockito.doReturn(mockChannel).when(mockSession).openChannel("sftp");
        Mockito.doReturn(new FileInputStream(file)).when(mockChannel)
                .get("src/test/resources/TEST_FILE.txt");
        Mockito.doReturn(mockJsch).when(wrapper).getJSch();
        assertEquals("line1\nline2",
                wrapper.get("src/test/resources/TEST_FILE.txt", "testHost", "testUser", "testPwd"));
    }

    @Test
    public void testGetExceptionFlow() throws JSchException, IOException {
        JSch mockJsch = Mockito.mock(JSch.class);
        Mockito.doThrow(new JSchException()).when(mockJsch).getSession("testUser", "testHost", 22);
        Mockito.doReturn(mockJsch).when(wrapper).getJSch();
        expectedEx.expect(IOException.class);
        expectedEx.expectMessage("com.jcraft.jsch.JSchException");
        wrapper.get("src/test/resources/TEST_FILE.txt", "testHost", "testUser", "testPwd");
    }

    @Test
    public void testSendWithDelimiter() throws IOException {
        ChannelShell mockChannel = Mockito.mock(ChannelShell.class);
        OutputStream mockOutputStream = Mockito.mock(BufferedOutputStream.class);
        Mockito.doReturn(mockOutputStream).when(mockChannel).getOutputStream();
        DataOutputStream mockDos = PowerMockito.spy(new DataOutputStream(mockOutputStream));
        PowerMockito.doReturn("TEST RESPONSE").when(wrapper).receiveUntil("#$", 300000,
                "TEST COMMAND\n");
        PowerMockito.doReturn(mockDos).when(wrapper).getDataOutputStream(Mockito.anyObject());
        Whitebox.setInternalState(wrapper, "channel", mockChannel);
        assertEquals("TEST RESPONSE", wrapper.send("TEST COMMAND\n", "#$"));
    }

    @Test
    public void testSendWithDelimiterExceptionFlow() throws IOException {
        ChannelShell mockChannel = Mockito.mock(ChannelShell.class);
        OutputStream mockOutputStream = Mockito.mock(BufferedOutputStream.class);
        Mockito.doReturn(mockOutputStream).when(mockChannel).getOutputStream();
        DataOutputStream mockDos = PowerMockito.spy(new DataOutputStream(mockOutputStream));
        PowerMockito.doThrow(new IOException()).when(mockDos).flush();
        PowerMockito.doReturn(mockDos).when(wrapper).getDataOutputStream(Mockito.anyObject());
        Whitebox.setInternalState(wrapper, "channel", mockChannel);
        expectedEx.expect(IOException.class);
        expectedEx.expectMessage("java.io.IOException");
        wrapper.send("TEST COMMAND", "]]>]]>");
    }

    @Test
    public void testValues() throws IOException {
        SshJcraftWrapper wrapper = new SshJcraftWrapper();
        wrapper.setEquipNameCode("testcode");
        wrapper.setRouterCommandType("testcommand");
        String equipName = wrapper.getEquipNameCode();
        assertNull(wrapper.getHostName());
        assertNull(wrapper.getPassWord());
        assertNull(wrapper.getRouterName());
        assertNull(wrapper.getUserName());
        assertTrue(
                wrapper.getTheDate().indexOf('/') > -1 && wrapper.getTheDate().indexOf(':') > -1);
        Assert.assertEquals("testcode", equipName);
    }

    @Test(expected = Exception.class)
    public void testSetRouterCommandType2() throws IOException {
        SshJcraftWrapper wrapper = new SshJcraftWrapper();
        wrapper.appendToRouterFile("test", 2);
        StringBuffer buffer = new StringBuffer();
        buffer.append("test");
        wrapper.appendToRouterFile("Test.txt", buffer);
        wrapper.receiveUntilBufferFlush(3, 4, "test");
    }

    @Test(expected = Exception.class)
    public void testSetRouterCommandType3() throws IOException {
        SshJcraftWrapper wrapper = new SshJcraftWrapper();
        wrapper.checkIfReceivedStringMatchesDelimeter(3, "test");
    }

    @Test
    public void testMyUserInfoGetPassword() {
        MyUserInfo myUserInfo = new MyUserInfo();
        assertNull(myUserInfo.getPassword());
    }

    @Test
    public void testMyUserInfoPromptYesNo() {
        MyUserInfo myUserInfo = new MyUserInfo();
        assertFalse(myUserInfo.promptYesNo(""));
    }
}
