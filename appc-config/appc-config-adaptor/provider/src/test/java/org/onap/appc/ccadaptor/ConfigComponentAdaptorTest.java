/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
 * Modification Copyright (C) 2018 IBM.
 * =============================================================================
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
import static org.junit.Assert.assertThat;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.ccsdk.sli.core.sli.SvcLogicAdaptor.ConfigStatus;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

@RunWith(MockitoJUnitRunner.class)
public class ConfigComponentAdaptorTest {

    private static final String TERMINATE_COMMAND = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "\n    <rpc message-id=\"terminateConnection\" xmlns:netconf=\"urn:ietf:params:xml:ns:netconf:base:1.0\" "
            + "xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n <close-session/> \n </rpc>\n ]]>]]>";
    private SshJcraftWrapper mockWrapper;

    @Before
    public void setupForTests() {
        mockWrapper = Mockito.mock(SshJcraftWrapper.class);
    }

    @Test
    public void testGet() throws TimedOutException, IOException {
        Properties props = null;
        ConfigComponentAdaptor cca = Mockito.spy(new ConfigComponentAdaptor(props));
        Mockito.doReturn("TEST\nDATA").when(mockWrapper).receiveUntil(Mockito.anyString(),
                Mockito.anyInt(), Mockito.anyString());
        Mockito.doReturn(mockWrapper).when(cca).getSshJcraftWrapper();
        String key = "get";
        Map<String, String> parameters = new HashMap<>();
        parameters.put("Host_ip_address", "test");
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute(
                "service-data.vnf-config-parameters-list.vnf-config-parameters[0].update-configuration[0].block-key-name",
                "test");
        assertEquals(ConfigStatus.SUCCESS, cca.configure(key, parameters, ctx));
    }

    @Test
    public void testPutExceptionFlow() throws TimedOutException, IOException {
        Properties props = null;
        ConfigComponentAdaptor cca = Mockito.spy(new ConfigComponentAdaptor(props));
        Mockito.doThrow(new IOException()).when(mockWrapper).put(Mockito.anyObject(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(mockWrapper).when(cca).getSshJcraftWrapper();
        String key = "put";
        Map<String, String> parameters = new HashMap<>();
        parameters.put("data", "test");
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute(
                "service-data.vnf-config-parameters-list.vnf-config-parameters[0].update-configuration[0].block-key-name",
                "test");
        assertEquals(ConfigStatus.FAILURE, cca.configure(key, parameters, ctx));
    }

    @Test
    public void testCli() throws TimedOutException, IOException {
        Properties props = null;
        ConfigComponentAdaptor cca = Mockito.spy(new ConfigComponentAdaptor(props));
        Mockito.doReturn("TEST\nDATA").when(mockWrapper).receiveUntil(Mockito.anyString(),
                Mockito.anyInt(), Mockito.anyString());
        Mockito.doReturn(mockWrapper).when(cca).getSshJcraftWrapper();
        String Get_config_template =
                ("get_config_template\nRequest: \"show config\"\nResponse: Ends_With \"RESPONSE\"");
        String key = "cli";
        Map<String, String> parameters = new HashMap<>();
        loadSshParameters(parameters);
        parameters.put("Get_config_template", Get_config_template);
        parameters.put("config-component-configUrl", "testUrl");
        parameters.put("config-component-configPassword", "testPassword");
        parameters.put("config-component-configUser", "testUser");
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute(
                "service-data.vnf-config-parameters-list.vnf-config-parameters[0].update-configuration[0].block-key-name",
                "test");
        assertEquals(ConfigStatus.SUCCESS, cca.configure(key, parameters, ctx));
    }

    @Test
    public void testCliExceptionFlow() throws TimedOutException, IOException {
        Properties props = null;
        ConfigComponentAdaptor cca = Mockito.spy(new ConfigComponentAdaptor(props));
        Mockito.doThrow(new IOException()).when(mockWrapper).connect(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyInt());
        Mockito.doReturn(mockWrapper).when(cca).getSshJcraftWrapper();
        String Get_config_template =
                ("get_config_template\nRequest: \"show config\"\nResponse: Ends_With \"RESPONSE\"");
        String key = "cli";
        Map<String, String> parameters = new HashMap<>();
        loadSshParameters(parameters);
        parameters.put("Get_config_template", Get_config_template);
        parameters.put("config-component-configUrl", "testUrl");
        parameters.put("config-component-configPassword", "testPassword");
        parameters.put("config-component-configUser", "testUser");
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute(
                "service-data.vnf-config-parameters-list.vnf-config-parameters[0].update-configuration[0].block-key-name",
                "test");
        assertEquals(ConfigStatus.FAILURE, cca.configure(key, parameters, ctx));
    }

    @Test
    public void testEscapeSql() {
        Properties props = null;
        ConfigComponentAdaptor cca = new ConfigComponentAdaptor(props);
        String testArtifactContents = ("\\ \\\\");
        String key = "escapeSql";
        Map<String, String> parameters = new HashMap<>();
        loadSshParameters(parameters);
        parameters.put("artifactContents", testArtifactContents);
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute(
                "service-data.vnf-config-parameters-list.vnf-config-parameters[0].update-configuration[0].block-key-name",
                "test");
        assertEquals(ConfigStatus.SUCCESS, cca.configure(key, parameters, ctx));
        assertEquals("\\" + testArtifactContents + "\\\\", ctx.getAttribute("escapedData"));
    }

    @Test
    public void testGetCliRunningConfig() throws TimedOutException, IOException {
        Properties props = null;
        ConfigComponentAdaptor cca = Mockito.spy(new ConfigComponentAdaptor(props));
        Mockito.doReturn("TEST\nDATA").when(mockWrapper).receiveUntil(Mockito.anyString(),
                Mockito.anyInt(), Mockito.anyString());
        Mockito.doReturn(mockWrapper).when(cca).getSshJcraftWrapper();
        String Get_config_template =
                ("get_config_template\nRequest: \"show config\"\nResponse: Ends_With \"RESPONSE\"");
        String key = "GetCliRunningConfig";
        Map<String, String> parameters = new HashMap<>();
        loadSshParameters(parameters);
        parameters.put("Get_config_template", Get_config_template);
        parameters.put("config-component-configUrl", "testUrl");
        parameters.put("config-component-configPassword", "testPassword");
        parameters.put("config-component-configUser", "testUser");
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute(
                "service-data.vnf-config-parameters-list.vnf-config-parameters[0].update-configuration[0].block-key-name",
                "test");
        assertEquals(ConfigStatus.SUCCESS, cca.configure(key, parameters, ctx));
    }

    @Test
    public void testGetCliRunningConfigExceptionFlow() throws TimedOutException, IOException {
        Properties props = null;
        ConfigComponentAdaptor cca = Mockito.spy(new ConfigComponentAdaptor(props));
        Mockito.doThrow(new IOException()).when(mockWrapper).receiveUntil(Mockito.anyString(),
                Mockito.anyInt(), Mockito.anyString());
        Mockito.doReturn(mockWrapper).when(cca).getSshJcraftWrapper();
        String Get_config_template =
                ("get_config_template\nRequest: \"show config\"\nResponse: Ends_With \"RESPONSE\"");
        String key = "GetCliRunningConfig";
        Map<String, String> parameters = new HashMap<>();
        loadSshParameters(parameters);
        parameters.put("Get_config_template", Get_config_template);
        parameters.put("config-component-configUrl", "testUrl");
        parameters.put("config-component-configPassword", "testPassword");
        parameters.put("config-component-configUser", "testUser");
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute(
                "service-data.vnf-config-parameters-list.vnf-config-parameters[0].update-configuration[0].block-key-name",
                "test");
        assertEquals(ConfigStatus.FAILURE, cca.configure(key, parameters, ctx));
    }

    @Test
    public void testXmlDownload() throws TimedOutException, IOException {
        Properties props = null;
        ConfigComponentAdaptor cca = Mockito.spy(new ConfigComponentAdaptor(props));
        Mockito.doReturn("TEST\nDATA").when(mockWrapper).receiveUntil(Mockito.anyString(),
                Mockito.anyInt(), Mockito.anyString());
        Mockito.doReturn(mockWrapper).when(cca).getSshJcraftWrapper();
        String Get_config_template = ("get_config_template");
        String key = "xml-download";
        Map<String, String> parameters = new HashMap<>();
        loadSshParameters(parameters);
        parameters.put("Get_config_template", Get_config_template);

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute(
                "service-data.vnf-config-parameters-list.vnf-config-parameters[0].update-configuration[0].block-key-name",
                "test");
        assertEquals(ConfigStatus.SUCCESS, cca.configure(key, parameters, ctx));
    }

    @Test
    public void testXmlDownloadExceptionFlow() throws TimedOutException, IOException {
        Properties props = null;
        ConfigComponentAdaptor cca = Mockito.spy(new ConfigComponentAdaptor(props));
        Mockito.doReturn("rpc-error").when(mockWrapper).receiveUntil("</rpc-reply>", 600000, "");
        Mockito.doThrow(new IOException()).when(mockWrapper).send(TERMINATE_COMMAND);
        Mockito.doReturn(mockWrapper).when(cca).getSshJcraftWrapper();
        String Get_config_template = ("get_config_template");
        String key = "xml-download";
        Map<String, String> parameters = new HashMap<>();
        loadSshParameters(parameters);
        parameters.put("Get_config_template", Get_config_template);

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute(
                "service-data.vnf-config-parameters-list.vnf-config-parameters[0].update-configuration[0].block-key-name",
                "test");
        assertEquals(ConfigStatus.FAILURE, cca.configure(key, parameters, ctx));
    }

    @Test
    public void testXmlGetrunningconfig() throws TimedOutException, IOException {
        Properties props = null;
        ConfigComponentAdaptor cca = Mockito.spy(new ConfigComponentAdaptor(props));
        Mockito.doReturn("<configuration xmlns=\"\n<data>\n</data>\n</configuration>")
                .when(mockWrapper)
                .receiveUntil(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString());
        Mockito.doReturn(mockWrapper).when(cca).getSshJcraftWrapper();
        String key = "xml-getrunningconfig";
        Map<String, String> parameters = new HashMap<>();
        loadSshParameters(parameters);
        SvcLogicContext ctx = new SvcLogicContext();
        assertEquals(ConfigStatus.SUCCESS, cca.configure(key, parameters, ctx));
    }

    @Test
    public void testXmlGetrunningconfigExceptionFlow() throws TimedOutException, IOException {
        Properties props = new Properties();
        ConfigComponentAdaptor cca = Mockito.spy(new ConfigComponentAdaptor(props));
        Mockito.doThrow(new IOException()).when(mockWrapper).connect(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyInt(),
                Mockito.anyInt(), Mockito.anyString());
        Mockito.doReturn(mockWrapper).when(cca).getSshJcraftWrapper();
        String key = "xml-getrunningconfig";
        Map<String, String> parameters = new HashMap<>();
        loadSshParameters(parameters);
        SvcLogicContext ctx = new SvcLogicContext();
        assertEquals(ConfigStatus.FAILURE, cca.configure(key, parameters, ctx));
    }

    @Test
    public void testDownloadCliConfig() throws TimedOutException, IOException {
        Properties props = null;
        ConfigComponentAdaptor cca = Mockito.spy(new ConfigComponentAdaptor(props));
        Mockito.doReturn("TEST\nDATA").when(mockWrapper).receiveUntil(Mockito.anyString(),
                Mockito.anyInt(), Mockito.anyString());
        Mockito.doReturn(mockWrapper).when(cca).getSshJcraftWrapper();
        String Download_config_template =
                ("get_config_template\nRequest: \"show config\"\nResponse: Ends_With \"RESPONSE\"");
        String key = "DownloadCliConfig";
        Map<String, String> parameters = new HashMap<>();
        loadSshParameters(parameters);
        parameters.put("Download_config_template", Download_config_template);
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute(
                "service-data.vnf-config-parameters-list.vnf-config-parameters[0].update-configuration[0].block-key-name",
                "test");
        assertEquals(ConfigStatus.SUCCESS, cca.configure(key, parameters, ctx));
    }

    @Test
    public void testDownloadCliConfigExceptionFlow() throws TimedOutException, IOException {
        Properties props = null;
        ConfigComponentAdaptor cca = Mockito.spy(new ConfigComponentAdaptor(props));
        Mockito.doThrow(new IOException("ExceptionFromDownloadCli")).when(mockWrapper)
                .receiveUntil(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString());
        Mockito.doReturn(mockWrapper).when(cca).getSshJcraftWrapper();
        String Download_config_template = ("get_config_template\nRequest: \"show config\""
                + "\n    Execute_config_contents Response: Ends_With\" \"RESPONSE\"\n");
        String key = "DownloadCliConfig";
        Map<String, String> parameters = new HashMap<>();
        loadSshParameters(parameters);
        parameters.put("Config_contents", "TEST\nDATA");
        parameters.put("Download_config_template", Download_config_template);
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute(
                "service-data.vnf-config-parameters-list.vnf-config-parameters[0].update-configuration[0].block-key-name",
                "test");
        assertEquals(ConfigStatus.FAILURE, cca.configure(key, parameters, ctx));
    }

    @Test
    public void testPrepare() {
        Client mockClient = Mockito.mock(Client.class);
        WebResource mockWebResource = Mockito.mock(WebResource.class);
        ClientResponse mockClientResponse = Mockito.mock(ClientResponse.class);
        ConfigComponentAdaptor cca = Mockito.spy(new ConfigComponentAdaptor(null));
        Mockito.doReturn(mockClientResponse).when(cca).getClientResponse(Mockito.anyObject(),
                Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(mockWebResource).when(mockClient).resource(Mockito.anyString());
        Mockito.doReturn(mockClient).when(cca).getClient();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("action", "prepare");
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute(
                "service-data.vnf-config-parameters-list.vnf-config-parameters[0].update-configuration[0].block-key-name",
                "test");
        assertEquals(ConfigStatus.SUCCESS, cca.configure("", parameters, ctx));
    }

    @Test
    public void testPrepareExceptionFlow() {
        ConfigComponentAdaptor cca = new ConfigComponentAdaptor(null);
        Map<String, String> parameters = new HashMap<>();
        parameters.put("action", "prepare");
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute(
                "service-data.vnf-config-parameters-list.vnf-config-parameters[0].scale-configuration[0].network-type",
                "test");
        assertEquals(ConfigStatus.FAILURE, cca.configure("", parameters, ctx));
        assertEquals("500", ctx.getAttribute("error-code"));
    }

    @Test
    public void testAudit() {
        Client mockClient = Mockito.mock(Client.class);
        WebResource mockWebResource = Mockito.mock(WebResource.class);
        ClientResponse mockClientResponse = Mockito.mock(ClientResponse.class);
        ConfigComponentAdaptor cca = Mockito.spy(new ConfigComponentAdaptor(null));
        Mockito.doReturn(mockClientResponse).when(cca).getClientResponse(Mockito.anyObject(),
                Mockito.anyString(), Mockito.anyString());
        Mockito.doReturn(mockWebResource).when(mockClient).resource(Mockito.anyString());
        Mockito.doReturn(mockClient).when(cca).getClient();
        Map<String, String> parameters = new HashMap<>();
        parameters.put("action", "audit");
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute(
                "service-data.vnf-config-parameters-list.vnf-config-parameters[0].update-configuration[0].block-key-name",
                "test");
        assertEquals(ConfigStatus.SUCCESS, cca.configure("", parameters, ctx));
    }

    @Test
    public void testActivate() {
        ClientResponse mockClientResponse = Mockito.mock(ClientResponse.class);
        ConfigComponentAdaptor cca = Mockito.spy(new ConfigComponentAdaptor(null));
        Mockito.doReturn(mockClientResponse).when(cca).getClientResponse(Mockito.anyObject(),
                Mockito.anyString(), Mockito.anyString());
        Map<String, String> parameters = new HashMap<>();
        parameters.put("action", "activate");
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute(
                "service-data.vnf-config-parameters-list.vnf-config-parameters[0].update-configuration[0].block-key-name",
                "test");
        assertEquals(ConfigStatus.SUCCESS, cca.configure("", parameters, ctx));
    }

    @Test
    public void testConstructorForNonNullProperties() throws Exception {
        Properties props = new Properties();
        props.setProperty("configComponent.url", "testConfigUrl");
        props.setProperty("configComponent.user", "testConfigUser");
        props.setProperty("configComponent.passwd", "testConfigPwd");
        props.setProperty("auditComponent.url", "testAuditUrl");
        props.setProperty("auditComponent.user", "testAuditUser");
        props.setProperty("auditComponent.passwd", "testAuditPwd");
        props.setProperty("service-configuration-notification-url", "testServiceNotificationUrl");
        props.setProperty("audit-configuration-notification-url", "testAuditNotificationUrl");

        ConfigComponentAdaptor cca = new ConfigComponentAdaptor(props);
        assertEquals("testConfigUrl", cca.getConfigUrl());
        assertEquals("testConfigUser", cca.getConfigUser());
        assertEquals("testConfigPwd", cca.getConfigPassword());
        assertEquals("testAuditUrl", cca.getAuditUrl());
        assertEquals("testAuditUser", cca.getAuditUser());
        assertEquals("testAuditPwd", cca.getAuditPassword());
        assertEquals("testServiceNotificationUrl", cca.getConfigCallbackUrl());
        assertEquals("testAuditNotificationUrl", cca.getAuditCallbackUrl());
    }

    @Test
    public void testStaticReadFile() {
        assertThat(ConfigComponentAdaptor._readFile("src/main/resources/config-base.xml"),
                CoreMatchers.containsString("<configure>"));
    }

    @Test
    public void testStaticReadFileExceptionFlow() {
        assertEquals("", ConfigComponentAdaptor._readFile("NON_EXISTENT_FILE"));
    }

    private void loadSshParameters(Map<String, String> map) {
        map.put("Host_ip_address", "test");
        map.put("User_name", "test");
        map.put("Password", "password");
        map.put("Port_number", "22");
        map.put("portNumber", "22");
    }
}
