/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications (C) 2019 Ericsson
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

package org.onap.appc.dg.netconf.impl;

import org.onap.appc.exceptions.APPCException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.onap.appc.adapter.netconf.util.Constants;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.appc.adapter.netconf.ConnectionDetails;
import org.onap.appc.adapter.netconf.NetconfClientFactory;
import org.onap.appc.adapter.netconf.NetconfClientType;
import org.onap.appc.adapter.netconf.NetconfConnectionDetails;
import org.onap.appc.adapter.netconf.NetconfDataAccessService;
import org.onap.appc.adapter.netconf.OperationalStateValidatorFactory;
import org.onap.appc.adapter.netconf.VnfType;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.powermock.api.mockito.PowerMockito.when;



@RunWith(PowerMockRunner.class)
@PrepareForTest({OperationalStateValidatorFactory.class, FrameworkUtil.class, ObjectMapper.class})

public class NetconfClientPluginImplTest {
    private NetconfClientPluginImpl netconfClientPlugin;
    private NetconfDataAccessService dao;
    private NetconfClientFactory clientFactory;
    private Map<String, String> params;

    private final BundleContext bundleContext = Mockito.mock(BundleContext.class);
    private final Bundle bundleService = Mockito.mock(Bundle.class);
    private final ServiceReference sref1 = Mockito.mock(ServiceReference.class);
    private final ServiceReference sref2 = Mockito.mock(ServiceReference.class);
    private final ServiceReference sref3 = Mockito.mock(ServiceReference.class);
    private static final String DG_OUTPUT_STATUS_MESSAGE = "output.status.message";


    String host = "http://www.test.com";
    String host1 = "http://www.test1.com";
    String vnfType = "VNF";
    int port = 8080;
    String username = "test";
    String password = "test";
    String connectionDetails = "{\"host\":\"" + host + "\",\"port\":" + port + ",\"username\":\"" + username + "\",\"password\":\"" + password + "\",\"capabilities\":null,\"additionalProperties\":null}";
    String fileContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "\t<get-config>\n" +
            "\t\t<source>\n" +
            "\t\t\t<running/>\n" +
            "\t\t </source>\n" +
            "\t</get-config>\n" +
            "</rpc>'";
    String operationalState = "<rpc message-id=\"101\" xmlns=\"urn:ietf:params:xml:ns:netconf:base:1.0\">\n" +
            "       <get>\n" +
            "              <filter>\n" +
            "                     <ManagedElement xmlns=\"urn:org:onap:appc:Test\">\n" +
            "                           <VnfFunction xmlns=\"urn:org:openecomop:appc:Test\">\n" +
            "                                  <ProcessorManagement>\n" +
            "                                         <MatedPair>\n" +
            "                                                <operationalState/>\n" +
            "                                                <PayloadProcessor>\n" +
            "                                                       <operationalState/>\n" +
            "                                                </PayloadProcessor>\n" +
            "                                         </MatedPair>\n" +
            "                                         <SystemController>\n" +
            "                                                <operationalState/>\n" +
            "                                         </SystemController>\n" +
            "                                  </ProcessorManagement>\n" +
            "                           </VnfFunction>\n" +
            "                     </ManagedElement>\n" +
            "              </filter>\n" +
            "       </get>\n" +
            "</rpc>\n";


    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        clientFactory = new NetconfClientFactoryMock();
    }


    @Test
    public void testConfigure() throws Exception {
        shortInit();
        SvcLogicContext ctx = new SvcLogicContext();

        params = new HashMap<>();
        params.put(Constants.CONNECTION_DETAILS_FIELD_NAME, connectionDetails);
        params.put(Constants.FILE_CONTENT_FIELD_NAME, fileContent);

        netconfClientPlugin.configure(params, ctx);

        NetconfClientJschMock client = (NetconfClientJschMock) clientFactory.getNetconfClient(NetconfClientType.SSH);

        try {
            Assert.assertEquals("wrong configuration", fileContent, client.getConf());
            Assert.assertEquals("wrong host", host, client.getLastConnectionDetails().getHost());
            Assert.assertEquals("wrong port", port, client.getLastConnectionDetails().getPort());
            Assert.assertEquals("wrong username", username, client.getLastConnectionDetails().getUsername());
            Assert.assertEquals("wrong password", password, client.getLastConnectionDetails().getPassword());
            Assert.assertFalse(client.isConnection());
        } catch (Exception e) {
            Assert.fail("failed with because of " + e.getCause());
        }
    }


    @Test
    public void testConfigureNegativeIOException() throws Exception {
        shortInit();
        SvcLogicContext ctx = new SvcLogicContext();

        params = new HashMap<>();
        params.put(Constants.CONNECTION_DETAILS_FIELD_NAME, "{" + connectionDetails);
        params.put(Constants.FILE_CONTENT_FIELD_NAME, fileContent);
        NetconfClientJschMock client = (NetconfClientJschMock) clientFactory.getNetconfClient(NetconfClientType.SSH);

        try {
            netconfClientPlugin.configure(params, ctx);
            Assert.assertTrue(false);
        } catch (APPCException e) {
            Assert.assertNull(client.getLastConnectionDetails());
            Assert.assertNull(client.getConf());
        }

    }

    @Test
    public void testOperationStateValidation() throws Exception {
        shortInit();
        SvcLogicContext ctx = new SvcLogicContext();
        DAOServiceMock daoServiceMock = (DAOServiceMock) dao;
        daoServiceMock.setConfigFile(fileContent);

        NetconfClientJschMock client = (NetconfClientJschMock) clientFactory.getNetconfClient(NetconfClientType.SSH);
        client.setAnswer(operationalState);

        params = new HashMap<>();
        params.put(Constants.VNF_TYPE_FIELD_NAME, vnfType);
        params.put(Constants.VNF_HOST_IP_ADDRESS_FIELD_NAME, host1);
        params.put(Constants.CONNECTION_DETAILS_FIELD_NAME, connectionDetails);
        MockOperationalStateValidatorImpl validatorMock = new MockOperationalStateValidatorImpl();
        validatorMock.setConfigurationFileName("VnfGetRunningConfig");

        PowerMockito.mockStatic(OperationalStateValidatorFactory.class);
        when(OperationalStateValidatorFactory.getOperationalStateValidator(Matchers.any(VnfType.class))).thenReturn(validatorMock);

        netconfClientPlugin.operationStateValidation(params, ctx);

        Assert.assertTrue("validation process failed", validatorMock.isValidated());
        Assert.assertEquals(fileContent, client.getLastMessage());
    }

    @Test
    public void testOperationStateValidationNegativeJsonProcessingNullIllegalStateException() throws Exception {
        shortInit();
        SvcLogicContext ctx = new SvcLogicContext();
        DAOServiceMock daoServiceMock = (DAOServiceMock) dao;
        daoServiceMock.setConfigFile(fileContent);

        NetconfClientJschMock client = (NetconfClientJschMock) clientFactory.getNetconfClient(NetconfClientType.SSH);
        client.setAnswer(operationalState);

        params = new HashMap<>();
        params.put(Constants.VNF_TYPE_FIELD_NAME, vnfType);
        params.put(Constants.VNF_HOST_IP_ADDRESS_FIELD_NAME, host1);
        params.put(Constants.CONNECTION_DETAILS_FIELD_NAME, connectionDetails);
        MockOperationalStateValidatorImpl validatorMock = new MockOperationalStateValidatorImpl();
        validatorMock.setConfigurationFileName("VnfGetRunningConfig");

        PowerMockito.mockStatic(OperationalStateValidatorFactory.class);
        when(OperationalStateValidatorFactory.getOperationalStateValidator(Matchers.any(VnfType.class))).thenReturn(validatorMock);
        substituteMapper(true);

        try {
            netconfClientPlugin.operationStateValidation(params, ctx);
            substituteMapper(false);
        } catch (APPCException e) {
            substituteMapper(false);
            Assert.assertNotNull(ctx.getAttribute(DG_OUTPUT_STATUS_MESSAGE));
            Assert.assertFalse(validatorMock.isValidated());
            Assert.assertNull(client.getLastMessage());
        }
    }

    @Test
    public void testOperationStateValidationNegativeConnectionDetailsAreNullNullPointerException() throws Exception {
        shortInit();
        SvcLogicContext ctx = new SvcLogicContext();
        DAOServiceMock daoServiceMock = (DAOServiceMock) dao;
        daoServiceMock.setConfigFile(fileContent);

        NetconfClientJschMock client = (NetconfClientJschMock) clientFactory.getNetconfClient(NetconfClientType.SSH);
        client.setAnswer(operationalState);

        params = new HashMap<>();
        params.put(Constants.VNF_TYPE_FIELD_NAME, vnfType);
        params.put(Constants.VNF_HOST_IP_ADDRESS_FIELD_NAME, host1);
        params.put(Constants.CONNECTION_DETAILS_FIELD_NAME, null);
        MockOperationalStateValidatorImpl validatorMock = new MockOperationalStateValidatorImpl();
        validatorMock.setConfigurationFileName("VnfGetRunningConfig");

        PowerMockito.mockStatic(OperationalStateValidatorFactory.class);
        when(OperationalStateValidatorFactory.getOperationalStateValidator(Matchers.any(VnfType.class))).thenReturn(validatorMock);
        ObjectMapper mapper = PowerMockito.mock(ObjectMapper.class);
        final NetconfConnectionDetails netconfConnectionDetails = null;
        when(mapper.readValue(Matchers.anyString(), Matchers.any(Class.class))).thenReturn(netconfConnectionDetails);

        try {
            netconfClientPlugin.operationStateValidation(params, ctx);
            Assert.assertTrue(false);
        } catch (APPCException e) {
            Assert.assertNotNull(ctx.getAttribute(DG_OUTPUT_STATUS_MESSAGE));
            Assert.assertFalse("validation process failed", validatorMock.isValidated());
        }
    }


    @Test
    public void testOperationStateValidationNegativeAppcException() throws Exception {
        shortInit();
        SvcLogicContext ctx = new SvcLogicContext();
        DAOServiceMock daoServiceMock = (DAOServiceMock) dao;
        daoServiceMock.setConfigFile(fileContent);

        NetconfClientJschMock client = (NetconfClientJschMock) clientFactory.getNetconfClient(NetconfClientType.SSH);
        client.setAnswer("wrong");

        params = new HashMap<>();
        params.put(Constants.VNF_TYPE_FIELD_NAME, vnfType);
        params.put(Constants.VNF_HOST_IP_ADDRESS_FIELD_NAME, host1);
        params.put(Constants.CONNECTION_DETAILS_FIELD_NAME, connectionDetails);
        MockOperationalStateValidatorImpl validatorMock = new MockOperationalStateValidatorImpl();
        validatorMock.setConfigurationFileName("VnfGetRunningConfig");

        PowerMockito.mockStatic(OperationalStateValidatorFactory.class);
        when(OperationalStateValidatorFactory.getOperationalStateValidator(Matchers.any(VnfType.class))).thenReturn(validatorMock);

        try {
            netconfClientPlugin.operationStateValidation(params, ctx);
            Assert.assertTrue(false);
        } catch (APPCException e) {
            Assert.assertNotNull(ctx.getAttribute(DG_OUTPUT_STATUS_MESSAGE));
            Assert.assertFalse("validation process failed", validatorMock.isValidated());
        }
    }


    @Test
    public void testOperationStateValidatioConnectionDetailsInParamsAreEmpty() throws Exception {
        shortInit();
        SvcLogicContext ctx = new SvcLogicContext();
        DAOServiceMock daoServiceMock = (DAOServiceMock) dao;
        daoServiceMock.setConfigFile(fileContent);

        NetconfClientJschMock client = (NetconfClientJschMock) clientFactory.getNetconfClient(NetconfClientType.SSH);
        client.setAnswer(operationalState);
        ((DAOServiceMock) dao).setConnection(getConnectionDetails());

        params = new HashMap<>();
        params.put(Constants.VNF_TYPE_FIELD_NAME, vnfType);
        params.put(Constants.VNF_HOST_IP_ADDRESS_FIELD_NAME, host1);
        params.put(Constants.CONNECTION_DETAILS_FIELD_NAME, "");
        MockOperationalStateValidatorImpl validatorMock = new MockOperationalStateValidatorImpl();
        validatorMock.setConfigurationFileName("VnfGetRunningConfig");

        PowerMockito.mockStatic(OperationalStateValidatorFactory.class);
        when(OperationalStateValidatorFactory.getOperationalStateValidator(Matchers.any(VnfType.class))).thenReturn(validatorMock);

        netconfClientPlugin.operationStateValidation(params, ctx);

        Assert.assertTrue("validation process failed", validatorMock.isValidated());
        Assert.assertEquals(fileContent, client.getLastMessage());
    }

    @Test
    public void testOperationStateValidatioConnectionDetailsInParamsAreNull() throws Exception {
        shortInit();
        SvcLogicContext ctx = new SvcLogicContext();
        DAOServiceMock daoServiceMock = (DAOServiceMock) dao;
        daoServiceMock.setConfigFile(fileContent);

        NetconfClientJschMock client = (NetconfClientJschMock) clientFactory.getNetconfClient(NetconfClientType.SSH);
        client.setAnswer(operationalState);
        ((DAOServiceMock) dao).setConnection(getConnectionDetails());

        params = new HashMap<>();
        params.put(Constants.VNF_TYPE_FIELD_NAME, vnfType);
        params.put(Constants.VNF_HOST_IP_ADDRESS_FIELD_NAME, host1);
        params.put(Constants.CONNECTION_DETAILS_FIELD_NAME, null);
        MockOperationalStateValidatorImpl validatorMock = new MockOperationalStateValidatorImpl();
        validatorMock.setConfigurationFileName("VnfGetRunningConfig");

        PowerMockito.mockStatic(OperationalStateValidatorFactory.class);
        when(OperationalStateValidatorFactory.getOperationalStateValidator(Matchers.any(VnfType.class))).thenReturn(validatorMock);

        netconfClientPlugin.operationStateValidation(params, ctx);

        Assert.assertTrue("validation process failed", validatorMock.isValidated());
        Assert.assertEquals(fileContent, client.getLastMessage());
    }


    @Test
    public void testBackupConfiguration() throws Exception {
        shortInit();
        SvcLogicContext ctx = new SvcLogicContext();
        params = new HashMap<>();
        params.put(Constants.CONNECTION_DETAILS_FIELD_NAME, connectionDetails);
        NetconfClientJschMock client = (NetconfClientJschMock) clientFactory.getNetconfClient(NetconfClientType.SSH);
        client.setConf(fileContent);
        netconfClientPlugin.backupConfiguration(params, ctx);

        DAOServiceMock mockdao = (DAOServiceMock) dao;
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        Date date = new Date();
        String creationDateExpected = dateFormat.format(date);
        String creationDateActual = mockdao.getBackupConf().get("creationDate").substring(0, 10);

        Assert.assertEquals("wrong configuration in db", fileContent, mockdao.getBackupConf().get("logText"));
        Assert.assertEquals(creationDateExpected, creationDateActual);
    }

    @Test
    public void testBackupConfigurationNegativeDgErrorFieldName() throws Exception {
        shortInit();
        SvcLogicContext ctx = new SvcLogicContext();
        params = new HashMap<>();
        params.put(Constants.CONNECTION_DETAILS_FIELD_NAME, "{" + connectionDetails);
        NetconfClientJschMock client = (NetconfClientJschMock) clientFactory.getNetconfClient(NetconfClientType.SSH);
        client.setConf(fileContent);
        try {
            netconfClientPlugin.backupConfiguration(params, ctx);
            Assert.assertTrue(false);
        } catch (APPCException e) {
            Assert.assertNotNull(ctx.getAttribute(DG_OUTPUT_STATUS_MESSAGE));
            DAOServiceMock mockdao = (DAOServiceMock) dao;
            Assert.assertNull(mockdao.getBackupConf());
        }
    }

    @Test
    public void testGetConfig() throws Exception {
        fullInit();
        String entity = "123";

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("entity", entity);

        params = new HashMap<>();
        params.put("conf-id", "current");
        params.put(Constants.CONNECTION_DETAILS_FIELD_NAME, connectionDetails);
        NetconfClientJschMock client = (NetconfClientJschMock) clientFactory.getNetconfClient(NetconfClientType.SSH);
        client.setConf(fileContent);

        netconfClientPlugin.getConfig(params, ctx);

        Assert.assertEquals("Success", ctx.getAttribute("getConfig_Result"));
        Assert.assertEquals(fileContent, ctx.getAttribute("fullConfig"));
        Assert.assertNotNull(ctx.getAttribute(entity + ".Configuration"));
        Assert.assertEquals(fileContent, ctx.getAttribute(entity + ".Configuration"));
    }


    @Test
    public void testGetConfigNegativeConfigurationNull() throws Exception {
        fullInit();
        String entity = "123";

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("entity", entity);

        params = new HashMap<>();
        params.put("conf-id", "current");
        params.put(Constants.CONNECTION_DETAILS_FIELD_NAME, connectionDetails);

        netconfClientPlugin.getConfig(params, ctx);

        Assert.assertEquals("failure", ctx.getAttribute("getConfig_Result"));
        Assert.assertNull(ctx.getAttribute("fullConfig"));
        Assert.assertNull(ctx.getAttribute(entity + ".Configuration"));
        Assert.assertNull(ctx.getAttribute(entity + ".Configuration"));
    }


    @Test
    public void testGetConfigNegativeNotSupportedConfId() throws Exception {
        fullInit();
        String entity = "123";
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("entity", entity);

        params = new HashMap<>();
        params.put("conf-id", "current1");
        params.put(Constants.CONNECTION_DETAILS_FIELD_NAME, connectionDetails);

        netconfClientPlugin.getConfig(params, ctx);

        Assert.assertNull(ctx.getAttribute("getConfig_Result"));
        Assert.assertNull(ctx.getAttribute("fullConfig"));
        Assert.assertNull(ctx.getAttribute(entity + ".Configuration"));
        Assert.assertNull(ctx.getAttribute(entity + ".Configuration"));
    }

    @Test
    public void testGetConfigNegativeWronjJsonConnectionDetailsException() throws Exception {
        fullInit();
        String entity = "123";

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("entity", entity);

        params = new HashMap<>();
        params.put("conf-id", "current");
        params.put(Constants.CONNECTION_DETAILS_FIELD_NAME, "{" + connectionDetails);

        try {
            netconfClientPlugin.getConfig(params, ctx);
            Assert.assertTrue(false);
        } catch (APPCException e) {
            Assert.assertEquals("failure", ctx.getAttribute("getConfig_Result"));
            Assert.assertNull(ctx.getAttribute("fullConfig"));
            Assert.assertNull(ctx.getAttribute(entity + ".Configuration"));
            Assert.assertNull(ctx.getAttribute(entity + ".Configuration"));
            Assert.assertNotNull(ctx.getAttribute(DG_OUTPUT_STATUS_MESSAGE));
        }
    }

    @Test
    public void testGetRunningConfig() throws Exception {
        fullInit();
        SvcLogicContext ctx = new SvcLogicContext();
        params = new HashMap<>();
        params.put("host-ip-address", host);
        params.put("user-name", username);
        params.put("password", password);
        params.put("port-number", String.valueOf(port));

        NetconfClientJschMock client = (NetconfClientJschMock) clientFactory.getNetconfClient(NetconfClientType.SSH);
        client.setConf(fileContent);

        netconfClientPlugin.getRunningConfig(params, ctx);

        Assert.assertEquals("Success", ctx.getAttribute("getRunningConfig_Result"));
        Assert.assertEquals(fileContent, ctx.getAttribute("running-config"));
        Assert.assertEquals("success", ctx.getStatus());
    }

    @Test
    public void testGetRunningConfigWithoutPortNumberDgErrorFieldNameException() throws Exception {
        fullInit();
        SvcLogicContext ctx = new SvcLogicContext();
        params = new HashMap<>();
        params.put("host-ip-address", host);
        params.put("user-name", username);
        params.put("password", password);

        NetconfClientJschMock client = (NetconfClientJschMock) clientFactory.getNetconfClient(NetconfClientType.SSH);
        client.setConf(fileContent);

        try {
            netconfClientPlugin.getRunningConfig(params, ctx);
            Assert.assertTrue(false);
        } catch (APPCException e) {
            Assert.assertEquals("failure", ctx.getAttribute("getRunningConfig_Result"));
            Assert.assertNull(ctx.getAttribute("running-config"));
            Assert.assertNotNull(ctx.getAttribute(DG_OUTPUT_STATUS_MESSAGE));
        }
    }

    @Test
    public void testGetRunningConfigNegativeConfigurationNull() throws Exception {
        fullInit();
        SvcLogicContext ctx = new SvcLogicContext();
        params = new HashMap<>();
        params.put("host-ip-address", host);
        params.put("user-name", username);
        params.put("password", password);
        params.put("port-number", String.valueOf(port));

        netconfClientPlugin.getRunningConfig(params, ctx);

        Assert.assertEquals("failure", ctx.getAttribute("getRunningConfig_Result"));
        Assert.assertNull(ctx.getAttribute("running-config"));
    }

    @Test
    public void testValidateMandatoryParamNegativeEmptyParamValue() throws Exception {
        shortInit();
        String paramName = "test";
        String paramValue = "";

        try {
            netconfClientPlugin.validateMandatoryParam(paramName, paramValue);
            Assert.assertTrue(false);
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testRetrieveConnectionDetails() throws Exception {
        shortInit();
        DAOServiceMock daoServiceMock = (DAOServiceMock) dao;
        daoServiceMock.setConfigFile(fileContent);
        ConnectionDetails connectionDetails1 = getConnectionDetails();
        daoServiceMock.setConnection(connectionDetails1);

        NetconfConnectionDetails connectionDetailsActual = netconfClientPlugin.retrieveConnectionDetails(VnfType.VNF);

        Assert.assertEquals("wrong host", connectionDetails1.getHost(), connectionDetailsActual.getHost());
        Assert.assertEquals("wrong password", connectionDetails1.getPassword(), connectionDetailsActual.getPassword());
        Assert.assertEquals("wrong port", connectionDetails1.getPort(), connectionDetailsActual.getPort());
        Assert.assertEquals("wrong usename", connectionDetails1.getUsername(), connectionDetailsActual.getUsername());
    }


    @Test
    public void testRetrieveConnectionDetailsNegativeMissingConfiguration() throws Exception {
        shortInit();
        DAOServiceMock daoServiceMock = (DAOServiceMock) dao;
        daoServiceMock.setConfigFile(fileContent);
        ConnectionDetails connectionDetails1 = getConnectionDetails();
        daoServiceMock.setConnection(connectionDetails1);

        NetconfConnectionDetails connectionDetailsActual = null;
        try {
            connectionDetailsActual = netconfClientPlugin.retrieveConnectionDetails(VnfType.MOCK);
            Assert.assertTrue(false);
        } catch (APPCException e) {
            Assert.assertNull(connectionDetailsActual);
        }
    }

    @Test
    public void testRetrieveConfigurationFileContent() throws Exception {
        shortInit();

        DAOServiceMock daoServiceMock = (DAOServiceMock) dao;
        daoServiceMock.setConfigFile(fileContent);

        Assert.assertEquals("wrong config in a database", fileContent, netconfClientPlugin.retrieveConfigurationFileContent("VnfGetRunningConfig"));
    }

    private ConnectionDetails getConnectionDetails() {
        ConnectionDetails connectionDetails = new ConnectionDetails();
        connectionDetails.setPassword(password);
        connectionDetails.setPort(port);
        connectionDetails.setUsername(username);
        connectionDetails.setHost(host);
        return connectionDetails;
    }


    private void initDao() throws NoSuchFieldException, IllegalAccessException {
        dao = new DAOServiceMock();
        PowerMockito.mockStatic(FrameworkUtil.class);
        when(FrameworkUtil.getBundle(Matchers.any(Class.class))).thenReturn(bundleService);
        when(bundleService.getBundleContext()).thenReturn(bundleContext);
        when(bundleContext.getServiceReference(NetconfDataAccessService.class)).thenReturn(sref1);
        when(bundleContext.getService(sref1)).thenReturn(dao);
    }

    private void fullInit() throws NoSuchFieldException, IllegalAccessException {
        initClientFactory();
        initClientFactory2();
        initDao();
        netconfClientPlugin = new NetconfClientPluginImpl();
        netconfClientPlugin.setDao(this.dao);
    }

    private void shortInit() throws NoSuchFieldException, IllegalAccessException {
        initClientFactory();
        initDao();
        netconfClientPlugin = new NetconfClientPluginImpl();
        netconfClientPlugin.setDao(this.dao);
    }

    private void initClientFactory() throws NoSuchFieldException, IllegalAccessException {
        PowerMockito.mockStatic(FrameworkUtil.class);
        when(FrameworkUtil.getBundle(Matchers.any(Class.class))).thenReturn(bundleService);
        when(bundleService.getBundleContext()).thenReturn(bundleContext);
        when(bundleContext.getServiceReference(NetconfClientFactory.class)).thenReturn(sref2);
        when(bundleContext.getService(sref2)).thenReturn(clientFactory);
    }

    private void initClientFactory2() {
        PowerMockito.mockStatic(FrameworkUtil.class);
        when(FrameworkUtil.getBundle(Matchers.any(Class.class))).thenReturn(bundleService);
        when(bundleService.getBundleContext()).thenReturn(bundleContext);
        when(bundleContext.getServiceReference(Matchers.anyString())).thenReturn(sref3);
        when(bundleContext.getService(sref3)).thenReturn(clientFactory);
    }

    private void substituteMapper(boolean command) throws NoSuchFieldException, IllegalAccessException {
        ObjectMapper mapper = new ObjectMapperMock();
        ObjectMapper mapper2 = new ObjectMapper();
        Field field = NetconfClientPluginImpl.class.getDeclaredField("mapper");
        field.setAccessible(true);
        if (command) {
            field.set(netconfClientPlugin, mapper);
        } else {
            field.set(netconfClientPlugin, mapper2);
        }
    }

}
