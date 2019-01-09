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
import org.onap.appc.configuration.Configuration;
import org.onap.appc.configuration.ConfigurationFactory;
import org.onap.appc.provider.AppcProvider;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

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

}