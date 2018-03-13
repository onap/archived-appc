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

public class TestSshAdapterMock {
    private SshAdapterMock sshAdapterMock;

    @Before
    public void setUp() {
        sshAdapterMock = new SshAdapterMock();
    }

    @Test
    public void testGetReturnStatus() {
        sshAdapterMock.setReturnStatus(200);
        assertNotNull(sshAdapterMock.getReturnStatus());
        assertEquals(sshAdapterMock.getReturnStatus(), 200);
    }

    @Test
    public void testGetReturnStdout() {
        sshAdapterMock.setReturnStdout("success");
        assertNotNull(sshAdapterMock.getReturnStdout());
        assertEquals(sshAdapterMock.getReturnStdout(), "success");
    }

    @Test
    public void testGetReturnStderr() {
        sshAdapterMock.setReturnStderr("error");
        assertNotNull(sshAdapterMock.getReturnStderr());
        assertEquals(sshAdapterMock.getReturnStderr(), "error");
    }

    @Test
    public void testGetConnectionMocks() {
        sshAdapterMock.setReturnStatus(200);
        sshAdapterMock.setReturnStdout("success");
        sshAdapterMock.setReturnStderr("error");
        sshAdapterMock.getConnection("localhost", 8080, "myUser", "myPassword");
        assertEquals(false, sshAdapterMock.getConnectionMocks().isEmpty());
        assertNotNull(sshAdapterMock.getConnectionMocks());
    }
}
