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

package org.onap.appc.ccadaptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.ChannelSubsystem;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.apache.commons.io.IOUtils;

@RunWith(MockitoJUnitRunner.class)
public class SshJcraftWrapperTest {

    private static final String USER = "username";
    private static final String PASS = "pass";
    private static final String HOST = "hostname";
    private static final String SUBSYSTEM = "netconf";
    private static final String PROMPT = "]]>]]>";
    private static final String SEARCH_STR = "</rpc-reply>";
    private static final int READ_TIMEOUT = 180_000;
    private static final int PORT_NUM = 23;
    private static final int SESSION_TIMEOUT = 30_000;
    private static final int READ_INTERVAL_MS = 1;
    private static final int READ_BUFFER_SIZE = 10;

    private SshJcraftWrapper cut;
    @Mock
    private JSch jSchMock;
    @Mock
    private Session session;
    @Mock
    private ChannelShell channelShell;
    @Mock
    private ChannelSubsystem channelSubsystem;
    @Mock
    private InputStream channelIs;
    @Mock
    private OutputStream channelOs;

    @Before
    public void setUpTest() throws Exception {
        InputStream is = IOUtils.toInputStream("test input stream:~#", "UTF-8");
        given(channelShell.getInputStream()).willReturn(is);
        given(channelSubsystem.getInputStream()).willReturn(is);
        given(session.openChannel(SshJcraftWrapper.CHANNEL_SHELL_TYPE)).willReturn(channelShell);
        given(session.openChannel(SshJcraftWrapper.CHANNEL_SUBSYSTEM_TYPE)).willReturn(channelSubsystem);
        given(jSchMock.getSession(anyString(), anyString(), anyInt())).willReturn(session);
        cut = new SshJcraftWrapper(jSchMock, READ_INTERVAL_MS, READ_BUFFER_SIZE);
    }

    @Ignore
    @Test
    public void TestCheckIfReceivedStringMatchesDelimeter(){
        SshJcraftWrapper wrapper = new SshJcraftWrapper();
        wrapper.getTheDate();
        boolean result = wrapper.checkIfReceivedStringMatchesDelimeter("#", "test#", "test#");
        Assert.assertEquals(true, result);
    }

    @Ignore
    @Test
    public void testRemoveWhiteSpaceAndNewLineCharactersAroundString(){
        SshJcraftWrapper wrapper = new SshJcraftWrapper();
        String nameSpace = wrapper.removeWhiteSpaceAndNewLineCharactersAroundString("namespace ");
        Assert.assertEquals("namespace", nameSpace);
    }

    @Ignore
    @Test
    public void testStripOffCmdFromRouterResponse(){
        SshJcraftWrapper wrapper = new SshJcraftWrapper();
        String result = wrapper.stripOffCmdFromRouterResponse("test\nsuccess");
        Assert.assertEquals("success\n", result);
    }

    //@Test
    public void testGetLastFewLinesOfFile() throws FileNotFoundException, IOException{
        SshJcraftWrapper wrapper = new SshJcraftWrapper();
        URL path = SshJcraftWrapperTest.class.getResource("Test");
        File file = new File(path.getFile());
        String value = wrapper.getLastFewLinesOfFile(file,1);
        Assert.assertEquals("\nTest data 3", value);
    }

    @Ignore
    @Test(expected=Exception.class)
    public void testSetRouterCommandType() throws IOException{
        SshJcraftWrapper wrapper = new SshJcraftWrapper();
        wrapper.setRouterCommandType("test");
        wrapper.receiveUntil("test", 2, "test");
    }

    @Ignore
    @Test
    public void testValues() throws IOException{
        SshJcraftWrapper wrapper = new SshJcraftWrapper();
        wrapper.setEquipNameCode("testcode");
        wrapper.setRouterCommandType("testcommand");
        String equipName = wrapper.getEquipNameCode();
        wrapper.getHostName();
        wrapper.getPassWord();
        wrapper.getRouterName();
        wrapper.getUserName();
        wrapper.getTheDate();
        Assert.assertEquals("testcode", equipName);
    }

    @Ignore
    @Test(expected=Exception.class)
    public void testSetRouterCommandType2() throws IOException{
        SshJcraftWrapper wrapper = new SshJcraftWrapper();
        wrapper.appendToRouterFile("test", 2);
        StringBuilder sb = new StringBuilder();
        sb.append("test");
        wrapper.appendToRouterFile("Test.txt", sb);
        wrapper.receiveUntilBufferFlush(3, 4, "test");
    }

    @Ignore
    @Test(expected=Exception.class)
    public void testSetRouterCommandType3() throws IOException{
        SshJcraftWrapper wrapper = new SshJcraftWrapper();
        wrapper.checkIfReceivedStringMatchesDelimeter(3, "test");
    }

    //real jUnits
    @Test(expected = IOException.class)
    public void connect_shouldThrowIOException_whenJSchFails() throws Exception {
        //given
        given(jSchMock.getSession(anyString(), anyString(), anyInt())).willThrow(new JSchException());

        //when
        cut.connect(HOST, USER, PASS);

        //then
        fail("IOException should be thrown");
    }

    @Test
    public void connect_shouldSetVariables() throws Exception {
        //when
        cut.connect(HOST, USER, PASS);

        //then
        assertEquals(HOST, cut.getHostName());
        assertEquals(HOST, cut.getRouterName());
        assertEquals(USER, cut.getUserName());
        assertEquals(PASS, cut.getPassWord());
    }

    @Test
    public void connect_shouldSetUpSessionWithProperInvocationOrder() throws Exception {
        //given
        InOrder inOrder =  inOrder(session, channelShell);

        //when
        cut.connect(HOST, USER, PASS);

        //then
        verifySessionConfigurationOrderForChannelShellOpenning(
            inOrder, USER, HOST, PASS, SshJcraftWrapper.DEFAULT_PORT, SESSION_TIMEOUT);
    }

    @Test
    public void connect_shouldFinishSuccessfully_whenExceptionThrownDuringReceivingPhase() throws Exception {
        //given
        doThrow(new JSchException()).when(session).setTimeout(anyInt());

        //when
        cut.connect(HOST, USER, PASS);

        //then
        verify(session).setTimeout(anyInt());
    }

    @Test(expected = IOException.class)
    public void connect_withSubsystem_shouldThrowIOException_whenJSchFails() throws Exception {
        //given
        given(jSchMock.getSession(anyString(), anyString(), anyInt())).willThrow(new JSchException());

        //when
        cut.connect(HOST, USER, PASS, SESSION_TIMEOUT, PORT_NUM, SUBSYSTEM);

        //then
        fail("IOException should be thrown");
    }

    @Test
    public void connect_withSubsystem_shouldSetRouterName() throws Exception {
        //when
        cut.connect(HOST, USER, PASS, SESSION_TIMEOUT, PORT_NUM, SUBSYSTEM);

        //then
        assertEquals(HOST, cut.getRouterName());
    }

    @Test
    public void connect_withSubsystem_shouldSetUpSessionWithProperInvocationOrder() throws Exception {
        //given
        InOrder inOrder =  inOrder(session, channelSubsystem);

        //when
        cut.connect(HOST, USER, PASS, SESSION_TIMEOUT, PORT_NUM, SUBSYSTEM);

        //then
        verify(jSchMock).getSession(USER, HOST, PORT_NUM);
        inOrder.verify(session).setPassword(PASS);
        inOrder.verify(session).setUserInfo(any(UserInfo.class));
        inOrder.verify(session).setConfig(SshJcraftWrapper.STRICT_HOST_CHECK_KEY, SshJcraftWrapper.STRICT_HOST_CHECK_VALUE);
        inOrder.verify(session).connect(SESSION_TIMEOUT);
        inOrder.verify(session).setServerAliveCountMax(0);
        inOrder.verify(session).openChannel(SshJcraftWrapper.CHANNEL_SUBSYSTEM_TYPE);
        inOrder.verify(channelSubsystem).getInputStream();
        inOrder.verify(channelSubsystem).connect(anyInt());
        inOrder.verifyNoMoreInteractions();
        verifyNoMoreInteractions(jSchMock);
    }

    @Test(expected = IOException.class)
    public void connect_withPrompt_shouldThrowIOException_whenJSchFails() throws Exception {
        //given
        given(jSchMock.getSession(anyString(), anyString(), anyInt())).willThrow(new JSchException());

        //when
        cut.connect(HOST, USER, PASS, PROMPT, SESSION_TIMEOUT);

        //then
        fail("IOException should be thrown");
    }

    @Test
    public void connect_withPrompt_shouldSetVariables() throws Exception {
        //when
        cut.connect(HOST, USER, PASS, PROMPT, SESSION_TIMEOUT);

        //then
        assertEquals(HOST, cut.getHostName());
        assertEquals(HOST, cut.getRouterName());
        assertEquals(USER, cut.getUserName());
        assertEquals(PASS, cut.getPassWord());
    }

    @Test
    public void connect_withPrompt_shouldFinishSuccessfully_whenExceptionThrownDuringReceivingPhase() throws Exception {
        //given
        doThrow(new JSchException()).when(session).setTimeout(anyInt());

        //when
        cut.connect(HOST, USER, PASS, PROMPT, SESSION_TIMEOUT);

        //then
        verify(session).setTimeout(anyInt());
    }

    @Test
    public void connect_withPrompt_shouldSetUpSessionWithProperInvocationOrder() throws Exception {
        //given
        InOrder inOrder =  inOrder(session, channelShell);

        //when
        cut.connect(HOST, USER, PASS, PROMPT, SESSION_TIMEOUT);

        //then
        verifySessionConfigurationOrderForChannelShellOpenning(
            inOrder, USER, HOST, PASS, SshJcraftWrapper.DEFAULT_PORT, SESSION_TIMEOUT);
    }

    @Test(expected = IOException.class)
    public void connect_withPort_shouldThrowIOException_whenJSchFails() throws Exception {
        //given
        given(jSchMock.getSession(anyString(), anyString(), anyInt())).willThrow(new JSchException());

        //when
        cut.connect(HOST, USER, PASS, PROMPT, SESSION_TIMEOUT, PORT_NUM);

        //then
        fail("IOException should be thrown");
    }

    @Test
    public void connect_withPort_shouldSetVariables() throws Exception {
        //when
        cut.connect(HOST, USER, PASS, PROMPT, SESSION_TIMEOUT, PORT_NUM);

        //then
        assertEquals(HOST, cut.getHostName());
        assertEquals(HOST, cut.getRouterName());
        assertEquals(USER, cut.getUserName());
        assertEquals(PASS, cut.getPassWord());
    }

    @Test
    public void connect_withPort_shouldFinishSuccessfully_whenExceptionThrownDuringReceivingPhase() throws Exception {
        //given
        doThrow(new JSchException()).when(session).setTimeout(anyInt());

        //when
        cut.connect(HOST, USER, PASS, PROMPT, SESSION_TIMEOUT, PORT_NUM);

        //then
        verify(session).setTimeout(anyInt());
    }

    @Test
    public void connect_withPort_shouldSetUpSessionWithProperInvocationOrder() throws Exception {
        //given
        InOrder inOrder =  inOrder(session, channelShell);

        //when
        cut.connect(HOST, USER, PASS, PROMPT, SESSION_TIMEOUT, PORT_NUM);

        //then
        verifySessionConfigurationOrderForChannelShellOpenning(inOrder, USER, HOST, PASS, PORT_NUM, SESSION_TIMEOUT);
    }

    private void verifySessionConfigurationOrderForChannelShellOpenning(InOrder inOrder, String user, String host, String pass, int port, int sessionTimeout) throws Exception {
        verify(jSchMock).getSession(user, host, port);
        inOrder.verify(session).setPassword(pass);
        inOrder.verify(session).setUserInfo(any(UserInfo.class));
        inOrder.verify(session).setConfig(SshJcraftWrapper.STRICT_HOST_CHECK_KEY, SshJcraftWrapper.STRICT_HOST_CHECK_VALUE);
        inOrder.verify(session).connect(sessionTimeout);
        inOrder.verify(session).setServerAliveCountMax(0);
        inOrder.verify(session).openChannel(SshJcraftWrapper.CHANNEL_SHELL_TYPE);
        inOrder.verify(channelShell).getInputStream();
        inOrder.verify(channelShell).connect();
        inOrder.verify(session).setTimeout(anyInt());
        inOrder.verifyNoMoreInteractions();
        verifyNoMoreInteractions(jSchMock);
    }

    @Test
    public void closeConnection_shouldCloseReaderChannelAndSession_inAGivenOrder() throws Exception {
        //given
        provideConnectedSubsystemInstance();
        InOrder inOrder = inOrder(channelIs, channelSubsystem, session);

        //when
        cut.closeConnection();

        //then
        inOrder.verify(channelIs).close();
        inOrder.verify(channelSubsystem).disconnect();
        inOrder.verify(session).disconnect();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void closeConnection_shouldCloseChannelAndSession_whenClosingReaderFails() throws Exception {
        //given
        doThrow(new IOException("failed to close reader")).when(channelIs).close();
        provideConnectedSubsystemInstance();

        //when
        cut.closeConnection();

        //then
        verify(channelIs).close();
        verify(channelSubsystem).disconnect();
        verify(session).disconnect();
    }

    @Test
    public void closeConnection_shouldBeIdempotent_whenRunOnNewInstance() throws Exception {
        //given
        assertFalse(cut.isConnected());

        //when
        cut.closeConnection();

        //then
        assertFalse(cut.isConnected());
    }

    @Test
    public void closeConnection_shouldBeIdempotent_whenRunTwiceOnConnectedInstance() throws Exception {
        //given
        provideConnectedSubsystemInstance();

        //when
        cut.closeConnection();
        cut.closeConnection();

        //then
        assertFalse(cut.isConnected());
    }

    @Test
    public void closeConnection_shouldCloseResourcesOnce_whenRunTwiceOnConnectedInstance() throws Exception {
        //given
        provideConnectedSubsystemInstance();

        //when
        cut.closeConnection();
        cut.closeConnection();

        //then
        verify(channelIs, times(1)).close();
        verify(channelSubsystem, times(1)).disconnect();
        verify(session, times(1)).disconnect();
    }

    private void provideConnectedSubsystemInstance() throws Exception {
        given(channelSubsystem.getInputStream()).willReturn(channelIs);
        cut.connect(HOST, USER, PASS, SESSION_TIMEOUT, PORT_NUM, SUBSYSTEM);
        assertTrue(cut.isConnected());
    }

    //receiveUntil tests begin
    @Test(expected = IllegalStateException.class)
    public void receiveUntil_shouldThrowIllegalStateException_whenInstanceIsNotConnected() throws Exception {
        //given
        assertFalse(cut.isConnected());

        //when
        cut.receiveUntil(SEARCH_STR, READ_TIMEOUT, "");

        //then
        fail("IllegalStateException should be thrown");
    }

    @Test(expected = IllegalStateException.class)
    public void receiveUntil_shouldThrowIllegalStateException_whenJschReaderStreamIsNotAvailable() throws Exception {
        //given
        provideConnectedSubsystemInstance();
        given(channelIs.available()).willReturn(0);

        //when
        cut.receiveUntil(SEARCH_STR, READ_TIMEOUT, "");

        //then
        fail("IllegalStateException should be thrown");
    }

    @Test(expected = TimedOutException.class)
    public void receiveUntil_shouldThrowTimedOutException_whenSessionFails() throws Exception {
        //given
        given(channelSubsystem.getInputStream()).willReturn(IOUtils.toInputStream("test input stream:~#", "UTF-8"));
        cut.connect(HOST, USER, PASS, SESSION_TIMEOUT, PORT_NUM, SUBSYSTEM);
        assertTrue(cut.isConnected());
        doThrow(new JSchException("Session is not available")).when(session).setTimeout(anyInt());

        //when
        cut.receiveUntil(SEARCH_STR, READ_TIMEOUT, "");

        //then
        fail("TimedOutException should be thrown");
    }

    @Test(expected = TimedOutException.class)
    public void receiveUntil_shouldThrowTimedOutException_whenReadFails() throws Exception {
        //given
        provideConnectedSubsystemInstance();
        given(channelIs.available()).willReturn(1);
        given(channelIs.read(any(), anyInt(), anyInt())).willThrow(new IOException("Could not read stream"));

        //when
        cut.receiveUntil(SEARCH_STR, READ_TIMEOUT, "");

        //then
        fail("TimedOutException should be thrown");
    }

    @Test(expected = TimedOutException.class)
    public void receiveUntil_shouldThrowException_whenTimeoutIsReached() throws Exception {
        //given
        String streamContent = "test input stream:~#";
        provideConnectedSubsystemInstanceWithStreamContent(streamContent);

        //when
        cut.receiveUntil(SEARCH_STR, -1000, " Some fake command\n");

        //then
        fail("TimedOutException should be thrown");
    }

    @Test(expected = TimedOutException.class)
    public void receiveUntil_shouldThrowException_whenReachedEndOfStream_andCouldNotReadMoreBytes() throws Exception {
        //given
        provideConnectedSubsystemInstance();
        given(channelIs.available()).willReturn(1);
        given(channelIs.read(any(), anyInt(), anyInt())).willReturn(-1);

        //when
        cut.receiveUntil(SEARCH_STR, READ_TIMEOUT, "");

        //then
        fail("TimedOutException should be thrown");
    }

    @Test
    public void receiveUntil_shouldReadUnderlyingStream_andStripOffFirstLine() throws Exception {
        //given
        String command = "Command"+SshJcraftWrapper.EOL;
        String reply = "Reply"+SshJcraftWrapper.EOL;
        String streamContent = command+reply+PROMPT;
        provideConnectedSubsystemInstanceWithStreamContent(streamContent);

        //when
        String result = cut.receiveUntil(PROMPT, SESSION_TIMEOUT, command);

        //then
        assertEquals(reply+PROMPT, result);
    }

    @Test
    public void receiveUntil_shouldReadUnderlyingStream_andReturnWholeReadString() throws Exception {
        //given
        String streamContent = "Command and Reply in just one line"+PROMPT;
        provideConnectedSubsystemInstanceWithStreamContent(streamContent);

        //when
        String result = cut.receiveUntil(PROMPT, SESSION_TIMEOUT, streamContent);

        //then
        assertEquals(streamContent, result);
    }

    @Test
    public void receiveUntil_shouldCutOffSpecialCharactersFromStream() throws Exception {
        //given
        char special1 = Character.UNASSIGNED;
        char special2 = Character.ENCLOSING_MARK;
        char special3 = Character.LINE_SEPARATOR;
        char special4 = Character.MODIFIER_SYMBOL;
        StringBuilder sb = new StringBuilder("Command");
        sb.append(special1).append("With").append(special2).append("Special")
            .append(special3).append("Characters").append(special4).append("Set").append(PROMPT);

        provideConnectedSubsystemInstanceWithStreamContent(sb.toString());

        //when
        String result = cut.receiveUntil(PROMPT, SESSION_TIMEOUT, "");

        //then
        assertEquals("CommandWithSpecialCharactersSet"+PROMPT, result);
    }

    @Test
    public void receiveUntil_shouldReadUnderlyingStream_untilCLIDelimiterFound_whenProperDelimiterSet() throws Exception {
        //given
        String cliDelimiter = "#$";
        String delimiters = PROMPT+SshJcraftWrapper.DELIMITERS_SEPARATOR+cliDelimiter;
        String streamContent = "Command for CLI invocation #";
        provideConnectedSubsystemInstanceWithStreamContent(streamContent);

        //when
        String result = cut.receiveUntil(delimiters, SESSION_TIMEOUT, streamContent);

        //then
        assertEquals(streamContent, result);
    }

    @Test
    public void receiveUntil_shouldReadUnderlyingStream_untilCLIDelimiterFound_whenCLICommandSet() throws Exception {
        //given
        String streamContent = "Command for CLI invocation #";
        provideConnectedSubsystemInstanceWithStreamContent(streamContent);
        cut.setRouterCommandType("CLI");

        //when
        String result = cut.receiveUntil("", SESSION_TIMEOUT, streamContent);

        //then
        assertEquals(streamContent, result);
    }

    @Test
    public void receiveUntil_shouldReadUnderlyingStream_untilCLIDelimiterFound_forShowConfigCommand() throws Exception {
        //given
        String streamContent = "show config\nconfig content#";
        provideConnectedSubsystemInstanceWithStreamContent(streamContent);

        //when
        String result = cut.receiveUntil("#", SESSION_TIMEOUT, streamContent);

        //then
        assertEquals("config content#", result);
    }

    @Test
    public void receiveUntil_shouldWriteOutputToRouterFile_whenReadingIOSXRswConfigFile_confirmFromFile() throws Exception {
        receiveUntil_shouldWriteOutputToRouterFile_whenReadingIOSXRswConfigFile();
    }

    @Test
    public void receiveUntil_shouldWriteOutputToRouterFile_whenReadingIOSXRswConfigFile_confirmFromBuffer() throws Exception {
        //given
        int biggerBufferSize = 32;
        cut = new SshJcraftWrapper(jSchMock, READ_INTERVAL_MS, biggerBufferSize);

        receiveUntil_shouldWriteOutputToRouterFile_whenReadingIOSXRswConfigFile();
    }

    private void receiveUntil_shouldWriteOutputToRouterFile_whenReadingIOSXRswConfigFile() throws Exception {
        //given
        String routerName = "router";
        String command = "RP/0/RP0/CPU0: "+routerName+" #IOS_XR_uploadedSwConfigCmd";
        String configFileEnding = "\nXML>";
        String streamContent = "Config file\ncontent"+configFileEnding;
        provideConnectedSubsystemInstanceWithStreamContent(streamContent);

        //when
        String result = cut.receiveUntil("", SESSION_TIMEOUT, command);

        //then
        assertNull(result); //TO-DO: it would be better to return empty string in this situation
        assertFileExist(routerName);

        //after
        teardownFile(routerName);
    }

    private void provideConnectedSubsystemInstanceWithStreamContent( String streamContent) throws Exception {
        given(channelSubsystem.getInputStream()).willReturn(IOUtils.toInputStream(streamContent, "UTF-8"));
        cut.connect(HOST, USER, PASS, SESSION_TIMEOUT, PORT_NUM, SUBSYSTEM);
        assertTrue(cut.isConnected());
    }

    private void teardownFile(String routerName) {
        File file = new File(routerName);
        if(file.exists() && file.isFile()) {
            file.delete();
        }
    }

    private void assertFileExist(String fileName) {
        File file = new File(fileName);
        assertTrue(file.exists());
        assertTrue(file.isFile());
    }

    @Test
    public void send_withReceive_shouldWriteCommandToChannelOutputStream_andReturnReceivedCommand() throws Exception {
        //given
        String command = "sdc";
        String delimiter = ":";
        InOrder inOrder = inOrder(channelOs);
        provideConnectedSubsystemInstanceWithStreamContent(command+delimiter);
        given(channelSubsystem.getOutputStream()).willReturn(channelOs);

        //when
        String result = cut.send(command, delimiter);

        //then
        verifySdcCommandSent(inOrder);
        assertEquals(command+delimiter, result);
    }

    @Test
    public void send_shouldWriteCommandToChannelOutputStream() throws Exception {
        //given
        String command = "sdc";
        InOrder inOrder = inOrder(channelOs);
        provideConnectedSubsystemInstance();
        given(channelSubsystem.getOutputStream()).willReturn(channelOs);

        //when
        cut.send(command);

        //then
        verifySdcCommandSent(inOrder);
    }

    private void verifySdcCommandSent(InOrder inOrder) throws IOException {
        inOrder.verify(channelOs).write('s');
        inOrder.verify(channelOs).write('d');
        inOrder.verify(channelOs).write('c');
        inOrder.verify(channelOs, atLeastOnce()).flush();
        inOrder.verify(channelOs, atLeastOnce()).close();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void send_withReceive_shouldWriteCommandInChunksToChannelOutputStream_andReturnReceivedCommand() throws Exception {
        //given
        cut = new SshJcraftWrapper(jSchMock, READ_INTERVAL_MS, 1);
        cut.setCharsChunkSize(1);
        String command = "sdc";
        String delimiter = ":";
        int timeout = 9000;
        provideConnectedSubsystemInstanceWithStreamContent(command+delimiter+SshJcraftWrapper.EOL);
        given(channelSubsystem.getOutputStream()).willReturn(channelOs);
        InOrder inOrder = inOrder(channelOs, session);

        //when
        String result = cut.send(command, delimiter);

        //then
        verifySdcCommandSentInChunk(inOrder, timeout);
        assertEquals(command+delimiter, result);
    }

    @Test
    public void send_shouldWriteCommandInChunksToChannelOutputStream() throws Exception {
        //given
        cut = new SshJcraftWrapper(jSchMock, READ_INTERVAL_MS, 1);
        cut.setCharsChunkSize(1);
        String command = "sdc";
        int timeout = 9000;
        provideConnectedSubsystemInstanceWithStreamContent(command+SshJcraftWrapper.EOL);
        given(channelSubsystem.getOutputStream()).willReturn(channelOs);
        InOrder inOrder = inOrder(channelOs, session);

        //when
        cut.send(command);

        //then
        verifySdcCommandSentInChunk(inOrder, timeout);
    }

    private void verifySdcCommandSentInChunk(InOrder inOrder, int timeout) throws Exception{
        inOrder.verify(channelOs).write('s');
        inOrder.verify(channelOs).flush();
        inOrder.verify(session).setTimeout(timeout);
        inOrder.verify(channelOs).write('d');
        inOrder.verify(channelOs).flush();
        inOrder.verify(session).setTimeout(timeout);
        inOrder.verify(channelOs).write('c');
        inOrder.verify(channelOs).flush();
        inOrder.verify(session).setTimeout(timeout);
        inOrder.verify(channelOs, atLeastOnce()).flush();
        inOrder.verify(channelOs, atLeastOnce()).close();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void send_withReceive_shouldReturnActualResult_whenTimeoutReached() throws Exception {
        //given
        cut = new SshJcraftWrapper(jSchMock, READ_INTERVAL_MS, 1);
        cut.setCharsChunkSize(1);
        cut.setSessionTimeoutMs(-1);
        String command = "sdc";
        String delimiter = ":";
        provideConnectedSubsystemInstanceWithStreamContent(command+SshJcraftWrapper.EOL);
        given(channelSubsystem.getOutputStream()).willReturn(channelOs);

        //when
        String result = cut.send(command, delimiter);

        //then
        assertEquals(StringUtils.EMPTY, result);
    }

    @Test
    public void send_withReceive_shouldReturnActualResult_whenCouldNotSetSessionTimeout() throws Exception {
        //given
        cut = new SshJcraftWrapper(jSchMock, READ_INTERVAL_MS, 1);
        cut.setCharsChunkSize(1);
        String command = "sdc";
        String delimiter = ":";
        provideConnectedSubsystemInstanceWithStreamContent(command+SshJcraftWrapper.EOL);
        given(channelSubsystem.getOutputStream()).willReturn(channelOs);
        doThrow(new JSchException("failed to set session timeout")).when(session).setTimeout(anyInt());

        //when
        String result = cut.send(command, delimiter);

        //then
        assertEquals(StringUtils.EMPTY, result);
    }

    @Test
    public void sendChar_shouldWriteCharacterToChannelOutputStream() throws Exception {
        //given
        int charNum = 100;
        provideConnectedSubsystemInstance();
        given(channelSubsystem.getOutputStream()).willReturn(channelOs);
        InOrder inOrder = inOrder(channelOs);

        //when
        cut.sendChar(charNum);

        //then
        inOrder.verify(channelOs).write(charNum);
        inOrder.verify(channelOs, atLeastOnce()).flush();
        inOrder.verify(channelOs, atLeastOnce()).close();
        inOrder.verifyNoMoreInteractions();
    }

    @Test(expected = IOException.class)
    public void sendChar_shouldRethrowIOException_whenOccurs() throws Exception {
        //given
        int charNum = 100;
        provideConnectedSubsystemInstance();
        given(channelSubsystem.getOutputStream()).willReturn(channelOs);
        doThrow(new IOException()).when(channelOs).write(charNum);

        //when
        cut.sendChar(charNum);

        //then
        fail("IOException should be thrown");
    }

    @Test
    public void send_withByteBuffer_shouldWriteBufferToChannelOutputStream() throws Exception {
        //given
        byte[] buffer = "Command".getBytes();
        int offset = 5;
        provideConnectedSubsystemInstance();
        given(channelSubsystem.getOutputStream()).willReturn(channelOs);
        InOrder inOrder = inOrder(channelOs);

        //when
        cut.send(buffer,offset,buffer.length);

        //then
        inOrder.verify(channelOs).write(buffer, offset, buffer.length);
        inOrder.verify(channelOs, atLeastOnce()).flush();
        inOrder.verify(channelOs, atLeastOnce()).close();
        inOrder.verifyNoMoreInteractions();
    }

    @Test(expected = IOException.class)
    public void send_withByteBuffer_shouldRethrowIOException_whenOccurs() throws Exception {
        //given
        byte[] buffer = "Command".getBytes();
        int offset = 5;
        provideConnectedSubsystemInstance();
        given(channelSubsystem.getOutputStream()).willReturn(channelOs);
        doThrow(new IOException()).when(channelOs).write(buffer, offset, buffer.length);

        //when
        cut.send(buffer,offset,buffer.length);

        //then
        fail("IOException should be thrown");
    }
}
