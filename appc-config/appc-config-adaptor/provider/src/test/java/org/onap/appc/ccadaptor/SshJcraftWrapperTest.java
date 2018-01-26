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
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.ChannelSubsystem;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import java.util.Properties;
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

    private SshJcraftWrapper cut;
    @Mock
    private JSch jSchMock;
    @Mock
    private Session session;
    @Mock
    private ChannelShell channelShell;
    @Mock
    private ChannelSubsystem channelSubsystem;

    @Before
    public void setUpTest() throws Exception {
        given(channelShell.getInputStream()).willReturn(IOUtils.toInputStream("test shell stream", "UTF-8"));
        given(channelSubsystem.getInputStream()).willReturn(IOUtils.toInputStream("test subsystem stream", "UTF-8"));
        given(session.openChannel(SshJcraftWrapper.CHANNEL_SHELL_TYPE)).willReturn(channelShell);
        given(session.openChannel(SshJcraftWrapper.CHANNEL_SUBSYSTEM_TYPE)).willReturn(channelSubsystem);
        given(jSchMock.getSession(anyString(), anyString(), anyInt())).willReturn(session);
        cut = new SshJcraftWrapper(jSchMock);
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
        String equipName =wrapper.getEquipNameCode();
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
        verify(jSchMock).getSession(USER, HOST, SshJcraftWrapper.DEFAULT_PORT);
        inOrder.verify(session).setConfig(any(Properties.class));
        inOrder.verify(session).setPassword(PASS);
        inOrder.verify(session).setUserInfo(any(UserInfo.class));
        inOrder.verify(session).connect(anyInt());
        inOrder.verify(session).openChannel(SshJcraftWrapper.CHANNEL_SHELL_TYPE);
        inOrder.verify(session).setServerAliveCountMax(0);
        //inOrder.verify(channelShell).setPtyType(SshJcraftWrapper.TERMINAL_BASIC_MODE); cast makes it unable to verify here
        inOrder.verify(channelShell).getInputStream();
        inOrder.verify(channelShell).connect();
        inOrder.verify(session).setTimeout(anyInt());
        verifyNoMoreInteractions(jSchMock, session, channelShell);
    }

    @Test
    public void connect_shouldFinishSuccessfully_whenExceptionThrownDuringReceivingPhase() throws Exception {
        //given
        doThrow(new JSchException()).when(session).setTimeout(anyInt());

        //when
        cut.connect(HOST, USER, PASS);

        //then should end silently
    }

}
