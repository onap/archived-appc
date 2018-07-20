/*
* ============LICENSE_START=======================================================
* ONAP : APPC
* ================================================================================
* Copyright 2018 TechMahindra
*=================================================================================
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* ============LICENSE_END=========================================================
*/
package org.onap.appc.adapter.ssh;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class TestSshConnectionMock {

    private SshConnectionMock sshConnectionMock;
    @Before
    public void setUp() {
        sshConnectionMock=new SshConnectionMock("localhost", 8080, "myUser", "myPassword");
    }
    
    @Test
    public void testGetHost() {
        assertNotNull(sshConnectionMock.getHost());
        assertEquals(sshConnectionMock.getHost(), "localhost");
    }

    @Test
    public void testGetPort() {
        assertNotNull(sshConnectionMock.getPort());
        assertEquals(sshConnectionMock.getPort(), 8080);
    }

    @Test
    public void testGetUsername() {
        assertNotNull(sshConnectionMock.getUsername());
        assertEquals(sshConnectionMock.getUsername(), "myUser");
    }

    @Test
    public void testGetPassword() {
        assertNotNull(sshConnectionMock.getPassword());
        assertEquals(sshConnectionMock.getPassword(), "myPassword");
    }

    @Test
    public void testKeyFile() {
        sshConnectionMock=new SshConnectionMock("localhost", 8080, "sampleKeyFile");
        assertNotNull(sshConnectionMock.getKeyFile());
        assertEquals(sshConnectionMock.getKeyFile(), "sampleKeyFile");
    }

    @Test
    public void testGetReturnStderr() {
        sshConnectionMock.setReturnStderr("returnStderr");
        assertNotNull(sshConnectionMock.getReturnStderr());
        assertEquals(sshConnectionMock.getReturnStderr(), "returnStderr");
    }
    @Test
    public void testGetReturnStdout() {
        sshConnectionMock.setReturnStdout("returnStdout");
        assertNotNull(sshConnectionMock.getReturnStdout());
        assertEquals(sshConnectionMock.getReturnStdout(), "returnStdout");
    }
    @Test
    public void testGetReturnStatus() {
        sshConnectionMock.setReturnStatus(200);
        assertNotNull(sshConnectionMock.getReturnStatus());
        assertEquals(sshConnectionMock.getReturnStatus(), 200);
    }
    @Test
    public void testGetExecutedCommands() {
        sshConnectionMock.getExecutedCommands().add("cls");
        sshConnectionMock.getExecutedCommands().add("pwd");
        assertNotNull(sshConnectionMock.getExecutedCommands());
        assertEquals(false, sshConnectionMock.getExecutedCommands().isEmpty());
    }
}
