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

package org.openecomp.appc.dg.netconf.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.*;
import org.openecomp.appc.adapter.netconf.ConnectionDetails;
import org.openecomp.appc.adapter.netconf.NetconfConnectionDetails;
import org.openecomp.appc.adapter.netconf.NetconfDataAccessService;
import org.openecomp.appc.adapter.netconf.exception.DataAccessException;
import org.openecomp.appc.dg.netconf.impl.NetconfDBPluginImpl;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.sdnc.sli.SvcLogicContext;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.powermock.api.support.SuppressCode.suppressConstructor;

public class NetconfDBPluginImplTest {
    private NetconfDBPluginImpl netconfDBPlugin;
    private NetconfDataAccessService daoService;
    private DAOServiceMock daoMock;
    private Map<String, String> params;
    private static final String DG_OUTPUT_STATUS_MESSAGE = "output.status.message";
    String host = "http://www.test.com";
    String host1 = "http://www.test1.com";
    int port = 8080;
    String username = "test";
    String password = "test";
    String configContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "\t<get-config>\n" +
            "\t\t<source>\n" +
            "\t\t\t<running/>\n" +
            "\t\t </source>\n" +
            "\t</get-config>\n" +
            "</rpc>'";


    @Test
    public void testRetrieveDSConfiguration() throws Exception {
        init();
        params = new HashMap<>();
        params.put("org.openecomp.appc.vftype", "VNF");
        params.put("configuration-file-name", "VnfGetRunningConfig");
        SvcLogicContext ctx = new SvcLogicContext();
        netconfDBPlugin.retrieveDSConfiguration(params, ctx);

        Assert.assertEquals("lack of success of status", "success", ctx.getStatus());
        Assert.assertEquals("wrong config file content", configContent, ctx.getAttribute("file-content"));
    }


    @Test
    public void testRetrieveDSConfigurationNegativeErrorFieldNameDaoException() throws Exception {
        init();
        SvcLogicContext ctx = new SvcLogicContext();
        params = new HashMap<>();
        params.put("configuration-file-name", "wrong");

        try {
            netconfDBPlugin.retrieveDSConfiguration(params, ctx);
        } catch (DataAccessException e) {
            //Assert.assertNotNull(ctx.getAttribute("org.openecomp.appc.dg.error"));
            Assert.assertNull(ctx.getAttribute("file-content"));
        }


    }

    @Test
    public void testRetrieveVMDSConfiguration() throws Exception {
        init();
        params = new HashMap<>();
        params.put("resourceKey", "VNF");
        SvcLogicContext ctx = new SvcLogicContext();
        netconfDBPlugin.retrieveVMDSConfiguration(params, ctx);

        Assert.assertEquals("lack of success of retrieveVMDSConfiguration_Result", "success", ctx.getAttribute("retrieveVMDSConfiguration_Result"));
        Assert.assertEquals("wrong entity", "VNF", ctx.getAttribute("entity"));
        assertConnectionDetails(ctx, host);
    }

    @Test
    public void testRetrieveVMDSConfigurationNegativeMissingConfiguration() throws Exception {
        init();
        SvcLogicContext ctx = new SvcLogicContext();
        params = new HashMap<>();
        params.put("resourceKey", "MOCK");

        try {
            netconfDBPlugin.retrieveVMDSConfiguration(params, ctx);
            Assert.assertTrue(false);
        } catch (APPCException e) {

            Assert.assertEquals("failure", ctx.getAttribute("retrieveVMDSConfiguration_Result"));
        }
    }


    @Test
    public void testRetrieveVMDSConfigurationNegativeJsonProcessingException() throws Exception {

        SvcLogicContext ctx = new SvcLogicContext();
        params = new HashMap<>();
        params.put("resourceKey", "VNF");

        init();
        substituteMapper(true);
        try {
            netconfDBPlugin.retrieveVMDSConfiguration(params, ctx);
            substituteMapper(false);
            Assert.assertTrue(false);

        } catch (APPCException e) {
            substituteMapper(false);
            Assert.assertNotNull(ctx.getAttribute(DG_OUTPUT_STATUS_MESSAGE));

        }

    }

    @Test
    public void testRetrieveConfigFile() throws Exception {
        init();
        SvcLogicContext ctx = new SvcLogicContext();
        params = new HashMap<>();
        params.put("configuration-file-name", "VnfGetRunningConfig");
        netconfDBPlugin.retrieveConfigFile(params, ctx);

        Assert.assertEquals("lack of success of status", "success", ctx.getStatus());
        Assert.assertEquals("wrong config file content", configContent, ctx.getAttribute("file-content"));
    }

    @Test
    public void testRetrieveConnectionDetails() throws Exception {
        init();
        params = new HashMap<>();
        params.put("org.openecomp.appc.vftype", "VNF");
        params.put("vnf-host-ip-address", host1);
        SvcLogicContext ctx = new SvcLogicContext();
        netconfDBPlugin.retrieveConnectionDetails(params, ctx);

        assertConnectionDetails(ctx, host1);
    }

    @Test
    public void testRetrieveConnectionDetailsNegativeJsonProcessingException() throws Exception {
        init();
        params = new HashMap<>();
        params.put("org.openecomp.appc.vftype", "MOCK");
        params.put("vnf-host-ip-address", host1);
        SvcLogicContext ctx = new SvcLogicContext();

        try {
            netconfDBPlugin.retrieveConnectionDetails(params, ctx);
            Assert.assertTrue(false);
        } catch (APPCException e) {
            Assert.assertNull(ctx.getAttribute("connection-details"));
            Assert.assertNotNull(ctx.getAttribute(DG_OUTPUT_STATUS_MESSAGE));
        }

    }


    @Test
    public void testRetrieveConnectionDetailsNegativeMissingConfiguration() throws Exception {
        init();
        params = new HashMap<>();
        params.put("org.openecomp.appc.vftype", "VNF");
        params.put("vnf-host-ip-address", host1);
        SvcLogicContext ctx = new SvcLogicContext();
        substituteMapper(true);

        try {
            netconfDBPlugin.retrieveConnectionDetails(params, ctx);
            substituteMapper(false);
            Assert.assertTrue(false);
        } catch (APPCException e) {
            substituteMapper(false);
            Assert.assertNull(ctx.getAttribute("connection-details"));
            Assert.assertNotNull(ctx.getAttribute(DG_OUTPUT_STATUS_MESSAGE));
        }

    }

    private void assertConnectionDetails(SvcLogicContext ctx, String host) throws IOException {
        String sConnectionDetails = ctx.getAttribute("connection-details");
        NetconfConnectionDetails connectionDetails = new ObjectMapper().readValue(sConnectionDetails, NetconfConnectionDetails.class);
        Assert.assertEquals(host, connectionDetails.getHost());
        Assert.assertEquals(port, connectionDetails.getPort());
        Assert.assertEquals(username, connectionDetails.getUsername());
        Assert.assertEquals(password, connectionDetails.getPassword());
        Assert.assertNull(connectionDetails.getCapabilities());
        Assert.assertNull(connectionDetails.getAdditionalProperties());
    }

    private void init() {
        netconfDBPlugin = new NetconfDBPluginImpl();
        daoService = new DAOServiceMock();
        netconfDBPlugin.setDaoService(daoService);
        daoMock = (DAOServiceMock) daoService;
        daoMock.setConfigFile(configContent);
        daoMock.setConnection(getConnectionDetails());

    }

    private ConnectionDetails getConnectionDetails() {
        ConnectionDetails connectionDetails = new ConnectionDetails();
        connectionDetails.setHost(host);
        connectionDetails.setUsername(username);
        connectionDetails.setPort(port);
        connectionDetails.setPassword(password);
        return connectionDetails;
    }

    private void substituteMapper(boolean command) throws NoSuchFieldException, IllegalAccessException {
        ObjectMapper mapper = new ObjectMapperMock();
        ObjectMapper mapper2 = new ObjectMapper();
        Field field = NetconfDBPluginImpl.class.getDeclaredField("mapper");
        field.setAccessible(true);
        if (command) {
            field.set(netconfDBPlugin, mapper);
        } else {
            field.set(netconfDBPlugin, mapper2);
        }
    }
}
