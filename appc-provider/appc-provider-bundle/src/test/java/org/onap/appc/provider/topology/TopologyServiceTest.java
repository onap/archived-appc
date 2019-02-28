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

package org.onap.appc.provider.topology;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.UUID;
import org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.common.request.header.CommonRequestHeader;
import org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.config.payload.ConfigPayload;
import org.opendaylight.yang.gen.v1.org.onap.appc.provider.rev160104.vnf.resource.VnfResource;
import org.onap.appc.Constants;
import org.onap.appc.configuration.Configuration;
import org.onap.appc.configuration.ConfigurationFactory;
import org.onap.appc.provider.AppcProvider;
import org.onap.appc.provider.AppcProviderClient;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;
import java.util.Properties;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ConfigurationFactory.class)
public class TopologyServiceTest {
    @Mock
    private AppcProvider appcProvider;
    private TopologyService topologyService;

    @Before
    public void setUp() throws Exception {
        mockStatic(ConfigurationFactory.class);
        Configuration configuration = mock(Configuration.class);
        when(ConfigurationFactory.getConfiguration()).thenReturn(configuration);
        doReturn("NODE_NAME").when(configuration).getProperty("appc.provider.vfodl.url");

        topologyService = spy(new TopologyService(appcProvider));
    }

    @Test
    public void modifyConfig() throws Exception {
        CommonRequestHeader commonRequestHeader = mock(CommonRequestHeader.class);
        doReturn("request-id").when(commonRequestHeader).getServiceRequestId();
        ConfigPayload configPayload = mock(ConfigPayload.class);
        doReturn("url").when(configPayload).getConfigUrl();
        doReturn("configJson").when(configPayload).getConfigJson();

        topologyService.modifyConfig(commonRequestHeader, configPayload);

        Mockito.verify(topologyService, times(1)).callGraph(Mockito.any(Properties.class));
    }

    @Test
    public void migrate() throws Exception {
        CommonRequestHeader commonRequestHeader = mock(CommonRequestHeader.class);
        doReturn("request-id").when(commonRequestHeader).getServiceRequestId();
        VnfResource vnfResource = mock(VnfResource.class);
        UUID uuid = mock(UUID.class);
        doReturn("uuid-value").when(uuid).getValue();
        doReturn(uuid).when(vnfResource).getVmId();

        topologyService.migrate(commonRequestHeader, vnfResource);

        Mockito.verify(topologyService, times(1)).callGraph(Mockito.any(Properties.class));
    }

    @Test
    public void restart() throws Exception {
        CommonRequestHeader commonRequestHeader = mock(CommonRequestHeader.class);
        doReturn("request-id").when(commonRequestHeader).getServiceRequestId();
        VnfResource vnfResource = mock(VnfResource.class);
        UUID uuid = mock(UUID.class);
        doReturn("uuid-value").when(uuid).getValue();
        doReturn(uuid).when(vnfResource).getVmId();

        topologyService.restart(commonRequestHeader, vnfResource);

        Mockito.verify(topologyService, times(1)).callGraph(Mockito.any(Properties.class));
    }

    @Test
    public void rebuild() throws Exception {
        CommonRequestHeader commonRequestHeader = mock(CommonRequestHeader.class);
        doReturn("request-id").when(commonRequestHeader).getServiceRequestId();
        VnfResource vnfResource = mock(VnfResource.class);
        UUID uuid = mock(UUID.class);
        doReturn("uuid-value").when(uuid).getValue();
        doReturn(uuid).when(vnfResource).getVmId();

        topologyService.rebuild(commonRequestHeader, vnfResource);

        Mockito.verify(topologyService, times(1)).callGraph(Mockito.any(Properties.class));
    }

    @Test
    public void snapshot() throws Exception {
        CommonRequestHeader commonRequestHeader = mock(CommonRequestHeader.class);
        doReturn("request-id").when(commonRequestHeader).getServiceRequestId();
        VnfResource vnfResource = mock(VnfResource.class);
        UUID uuid = mock(UUID.class);
        doReturn("uuid-value").when(uuid).getValue();
        doReturn(uuid).when(vnfResource).getVmId();

        topologyService.snapshot(commonRequestHeader, vnfResource);

        Mockito.verify(topologyService, times(1)).callGraph(Mockito.any(Properties.class));
    }

    @Test
    public void vmstatuscheck() throws Exception {
        CommonRequestHeader commonRequestHeader = mock(CommonRequestHeader.class);
        doReturn("request-id").when(commonRequestHeader).getServiceRequestId();
        VnfResource vnfResource = mock(VnfResource.class);
        UUID uuid = mock(UUID.class);
        doReturn("uuid-value").when(uuid).getValue();
        doReturn(uuid).when(vnfResource).getVmId();

        topologyService.vmstatuscheck(commonRequestHeader, vnfResource);

        Mockito.verify(topologyService, times(1)).callGraph(Mockito.any(Properties.class));
    }

    @Test
    public void testCallGraphSuccess() throws SvcLogicException {
        Properties properties = new Properties();
        properties.setProperty(Constants.PROPERTY_MODULE_NAME, "test_module_name");
        properties.setProperty(Constants.PROPERTY_TOPOLOGY_METHOD, "test_topology_method");
        properties.setProperty(Constants.PROPERTY_TOPOLOGY_VERSION, "test_topology_version");
        AppcProviderClient client = Mockito.mock(AppcProviderClient.class);
        Mockito.when(client.hasGraph(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true);
        Properties respProps = new Properties();
        respProps.setProperty(Constants.ATTRIBUTE_ERROR_CODE, "200");
        Mockito.when(client.execute(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.any(Properties.class))).thenReturn(respProps);
        AppcProvider provider = Mockito.mock(AppcProvider.class);
        Mockito.when(provider.getClient()).thenReturn(client);
        Whitebox.setInternalState(topologyService, "provider", provider);
        assertTrue(topologyService.callGraph(properties));
    }

    @Test
    public void testCallGraphNoGraph() throws SvcLogicException {
        Properties properties = new Properties();
        properties.setProperty(Constants.PROPERTY_MODULE_NAME, "test_module_name");
        properties.setProperty(Constants.PROPERTY_TOPOLOGY_METHOD, "test_topology_method");
        properties.setProperty(Constants.PROPERTY_TOPOLOGY_VERSION, "test_topology_version");
        AppcProviderClient client = Mockito.mock(AppcProviderClient.class);
        Mockito.when(client.hasGraph(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(false);
        Properties respProps = new Properties();
        respProps.setProperty(Constants.ATTRIBUTE_ERROR_CODE, "200");
        Mockito.when(client.execute(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.any(Properties.class))).thenReturn(respProps);
        AppcProvider provider = Mockito.mock(AppcProvider.class);
        Mockito.when(provider.getClient()).thenReturn(client);
        Whitebox.setInternalState(topologyService, "provider", provider);
        assertFalse(topologyService.callGraph(properties));
    }

    @Test
    public void testCallGraphException() throws SvcLogicException {
        Properties properties = new Properties();
        properties.setProperty(Constants.PROPERTY_MODULE_NAME, "test_module_name");
        properties.setProperty(Constants.PROPERTY_TOPOLOGY_METHOD, "test_topology_method");
        properties.setProperty(Constants.PROPERTY_TOPOLOGY_VERSION, "test_topology_version");
        AppcProviderClient client = Mockito.mock(AppcProviderClient.class);
        Mockito.when(client.hasGraph(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true);
        Properties respProps = new Properties();
        respProps.setProperty(Constants.ATTRIBUTE_ERROR_CODE, "200");
        Mockito.when(client.execute(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.any(Properties.class))).thenThrow(new SvcLogicException());
        AppcProvider provider = Mockito.mock(AppcProvider.class);
        Mockito.when(provider.getClient()).thenReturn(client);
        Whitebox.setInternalState(topologyService, "provider", provider);
        assertFalse(topologyService.callGraph(properties));
    }

    @Test
    public void testCallGraphNoAttributeErrorCode() throws SvcLogicException {
        Properties properties = new Properties();
        properties.setProperty(Constants.PROPERTY_MODULE_NAME, "test_module_name");
        properties.setProperty(Constants.PROPERTY_TOPOLOGY_METHOD, "test_topology_method");
        properties.setProperty(Constants.PROPERTY_TOPOLOGY_VERSION, "test_topology_version");
        AppcProviderClient client = Mockito.mock(AppcProviderClient.class);
        Mockito.when(client.hasGraph(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true);
        Properties respProps = new Properties();
        Mockito.when(client.execute(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.any(Properties.class))).thenReturn(respProps);
        AppcProvider provider = Mockito.mock(AppcProvider.class);
        Mockito.when(provider.getClient()).thenReturn(client);
        Whitebox.setInternalState(topologyService, "provider", provider);
        assertFalse(topologyService.callGraph(properties));
    }

    @Test
    public void testCallGraphErrorCodeGreaterThan300() throws SvcLogicException {
        Properties properties = new Properties();
        properties.setProperty(Constants.PROPERTY_MODULE_NAME, "test_module_name");
        properties.setProperty(Constants.PROPERTY_TOPOLOGY_METHOD, "test_topology_method");
        properties.setProperty(Constants.PROPERTY_TOPOLOGY_VERSION, "test_topology_version");
        AppcProviderClient client = Mockito.mock(AppcProviderClient.class);
        Mockito.when(client.hasGraph(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true);
        Properties respProps = new Properties();
        respProps.setProperty(Constants.ATTRIBUTE_ERROR_CODE, "400");
        Mockito.when(client.execute(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.any(Properties.class))).thenReturn(respProps);
        AppcProvider provider = Mockito.mock(AppcProvider.class);
        Mockito.when(provider.getClient()).thenReturn(client);
        Whitebox.setInternalState(topologyService, "provider", provider);
        assertFalse(topologyService.callGraph(properties));
    }

    @Test
    public void testCallGraphNumberFormatException() throws SvcLogicException {
        Properties properties = new Properties();
        properties.setProperty(Constants.PROPERTY_MODULE_NAME, "test_module_name");
        properties.setProperty(Constants.PROPERTY_TOPOLOGY_METHOD, "test_topology_method");
        properties.setProperty(Constants.PROPERTY_TOPOLOGY_VERSION, "test_topology_version");
        AppcProviderClient client = Mockito.mock(AppcProviderClient.class);
        Mockito.when(client.hasGraph(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(true);
        Properties respProps = new Properties();
        respProps.setProperty(Constants.ATTRIBUTE_ERROR_CODE, "NaN");
        Mockito.when(client.execute(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.any(Properties.class))).thenReturn(respProps);
        AppcProvider provider = Mockito.mock(AppcProvider.class);
        Mockito.when(provider.getClient()).thenReturn(client);
        Whitebox.setInternalState(topologyService, "provider", provider);
        assertFalse(topologyService.callGraph(properties));
    }
}