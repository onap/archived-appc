/*-
 * ============LICENSE_START=======================================================
 * APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Amdocs
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
 * ============LICENSE_END=========================================================
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */

package org.openecomp.appc.listener.CL.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Properties;

import org.eclipse.osgi.internal.signedcontent.Base64;
import org.junit.Before;
import org.junit.Test;
import org.openecomp.appc.configuration.ConfigurationFactory;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.listener.CL.impl.ProviderOperations;
import org.openecomp.appc.listener.CL.model.IncomingMessage;

public class TestProviderOperations {

    private String ACTIVE_ENDPOINT;
    
    @Before
    public void setup() {
        Properties props = ConfigurationFactory.getConfiguration().getProperties();
        ACTIVE_ENDPOINT = props.getProperty("appc.ClosedLoop.provider.url");
        assertNotNull(ACTIVE_ENDPOINT);
        ProviderOperations.setUrl(ACTIVE_ENDPOINT);

        props.getProperty("test.vm_url");
        assertNotNull("VM_URL");
    }

    @Test
    public void testTopologyOperation() {
        IncomingMessage msg = new IncomingMessage();
        // Client and Time are for ID
        msg.setRequestClient("APPC");
        msg.setRequestTime("TEST");
        msg.setRequest("Restart");

        // Null Input
        try {
            ProviderOperations.topologyDG(null);
            fail("Topology Operation with null input should fail");
        } catch (APPCException e) {
            assertNotNull(e.getMessage());
        }

        // Bad URL
        msg.setUrl("some bad url here");
        try {
            ProviderOperations.topologyDG(msg);
            // Could also be issue in IaaS Adapter
            fail("Topology Operation with bad url should fail");
        } catch (APPCException e) {
            assertNotNull(e.getMessage());
        }

        // Will be tested in worker
        // msg.setUrl(VM_URL);
        // System.out.println("Rebooting real VM. Test can take up to 90s");
        // try {
        // assertTrue(ProviderOperations.topologyDG(msg));
        // } catch (APPCException e) {
        // fail("Topology Operation with good url should succeed. Check url in gui first");
        // }

    }

    @Test
    public void testConfigurationOperation() {
        try {
            ProviderOperations.topologyDG(null);
            fail("Configuration Operation should throw execption. Not yet supported");
        } catch (APPCException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    public void testBasicAuthFormating() {
        String user = "user";
        String pass = "pass";

        String result = ProviderOperations.setAuthentication(user, pass);

        assertNotNull(result);
        String decode = new String(Base64.decode(result.getBytes()));
        assertEquals(user + ":" + pass, decode);
    }

    @Test
    public void testGetSet() {
        // Every test URL will get reset
        assertEquals(ACTIVE_ENDPOINT, ProviderOperations.getUrl());

        String newUrl = "http://example.com";
        ProviderOperations.setUrl(newUrl);
        assertEquals(newUrl, ProviderOperations.getUrl());
    }

}
