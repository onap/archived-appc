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

package org.openecomp.appc.ccadaptor;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

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
}
