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
package org.onap.appc.dg.common.objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;

public class TestConnectionDetails {

    private ConnectionDetails connectionDetails;

    @Before
    public void setUp() {
        connectionDetails = new ConnectionDetails();
    }

    @Test
    public void testGetHost() {
        connectionDetails.setHost("host");
        assertNotNull(connectionDetails.getHost());
        assertEquals(connectionDetails.getHost(), "host");
    }

    @Test
    public void testGetPort() {
        connectionDetails.setPort(8080);
        assertNotNull(connectionDetails.getPort());
        assertEquals(connectionDetails.getPort(), 8080);
    }

    @Test
    public void testGetUsername() {
        connectionDetails.setUsername("username");
        assertNotNull(connectionDetails.getUsername());
        assertEquals(connectionDetails.getUsername(), "username");
    }

    @Test
    public void testGetPassword() {
        connectionDetails.setPassword("password");
        assertNotNull(connectionDetails.getPassword());
        assertEquals(connectionDetails.getPassword(), "password");
    }
}
