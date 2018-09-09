/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
 * Modifications Copyright (C) 2018 IBM.
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
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.ChannelSubsystem;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
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
import org.onap.appc.ccadaptor.SshJcraftWrapper.MyUserInfo;
import org.apache.commons.io.IOUtils;

@RunWith(MockitoJUnitRunner.class)
public class SshJcraftWrapperTest {
    @Test
    public void TestCheckIfReceivedStringMatchesDelimeter(){
        SshJcraftWrapper wrapper = new SshJcraftWrapper();
        wrapper.getTheDate();
        boolean result = wrapper.checkIfReceivedStringMatchesDelimeter("#", "test#", "test#");
        Assert.assertEquals(true, result);
    }

    @Test
    public void testRemoveWhiteSpaceAndNewLineCharactersAroundString(){
        SshJcraftWrapper wrapper = new SshJcraftWrapper();
        String nameSpace = wrapper.removeWhiteSpaceAndNewLineCharactersAroundString("namespace ");
        Assert.assertEquals("namespace", nameSpace);
    }

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

    @Test(expected=Exception.class)
    public void testSetRouterCommandType() throws IOException{
        SshJcraftWrapper wrapper = new SshJcraftWrapper();
        wrapper.setRouterCommandType("test");
        wrapper.receiveUntil("test", 2, "test");
    }

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

    @Test(expected=Exception.class)
    public void testSetRouterCommandType2() throws IOException{
        SshJcraftWrapper wrapper = new SshJcraftWrapper();
        wrapper.appendToRouterFile("test", 2);
        StringBuffer buffer = new StringBuffer();
        buffer.append("test");
        wrapper.appendToRouterFile("Test.txt", buffer);
        wrapper.receiveUntilBufferFlush(3, 4, "test");
    }

    @Test(expected=Exception.class)
    public void testSetRouterCommandType3() throws IOException{
        SshJcraftWrapper wrapper = new SshJcraftWrapper();
        wrapper.checkIfReceivedStringMatchesDelimeter(3, "test");
    }
    
    @Test(expected=IOException.class)
    public void testConnect() throws IOException{
        SshJcraftWrapper wrapper = new SshJcraftWrapper();
        wrapper.connect("testHost", "testUser", "testPswd", "3000", 1000);
    }
    
    @Test
    public void testMyUserInfoGetPassword() {
        MyUserInfo myUserInfo=new MyUserInfo();
        assertNull(myUserInfo.getPassword());
    }
    
    @Test
    public void testMyUserInfoPromptYesNo() {
        MyUserInfo myUserInfo=new MyUserInfo();
        assertFalse(myUserInfo.promptYesNo(""));
    }
}
