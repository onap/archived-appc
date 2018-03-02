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

package org.onap.appc.flow.controller.node;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.onap.appc.flow.controller.dbervices.FlowControlDBService;
import org.onap.appc.flow.controller.interfaceData.Capabilities;
import org.onap.appc.flow.controller.interfaceData.DependencyInfo;
import org.onap.appc.flow.controller.interfaceData.Vnfcs;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

public class FlowControlNodeTest {

  private SvcLogicContext ctx;
  private FlowControlDBService dbService;

  @Before
  public void setUp() {
    ctx = mock(SvcLogicContext.class);
    dbService = mock(FlowControlDBService.class);
  }

  @Test
  public void should_handle_capabilities_full_config() throws Exception {

    String jsonPayload = "{'vnf':['vnf-1', 'vnf-2'],'vf-module':['vf-module-1', 'vf-module-2'],'vnfc':['vnfc-1', 'vnfc-2'],'vm':['vm-1', 'vm-2']}";
    when(dbService.getCapabilitiesData(ctx)).thenReturn(jsonPayload.replaceAll("'","\""));

    FlowControlNode flowControlNode = new FlowControlNode(null, dbService);
    Capabilities capabilitiesData = flowControlNode.getCapabilitiesData(ctx);

    Assert.assertEquals("Capabilities [vnf=[vnf-1, vnf-2], vfModule=[vf-module-1, vf-module-2], vm=[vm-1, vm-2], vnfc=[vnfc-1, vnfc-2]]", capabilitiesData.toString());
  }

  @Test
  public void should_handle_capabilities_config_with_missing_params() throws Exception {

    // vm is empty, vnfc is absent
    String jsonPayload = "{'vnf':['vnf-1', 'vnf-2'],'vf-module':['vf-module-1'],'vm':[]}";
    when(dbService.getCapabilitiesData(ctx)).thenReturn(jsonPayload.replaceAll("'","\""));

    FlowControlNode flowControlNode = new FlowControlNode(null, dbService);
    Capabilities capabilitiesData = flowControlNode.getCapabilitiesData(ctx);

    Assert.assertEquals("Capabilities [vnf=[vnf-1, vnf-2], vfModule=[vf-module-1], vm=[], vnfc=[]]", capabilitiesData.toString());
  }

  @Test
  public void should_handle_dependency_config() throws Exception {

    Vnfcs vnfcs = new Vnfcs();
    vnfcs.setVnfcType("some-type");
    vnfcs.setResilience("some-resilence");
    vnfcs.setMandatory("some-mandatory");
    Map<String, List<Vnfcs>> input = new HashMap<>();
    List<Vnfcs> list = new ArrayList<>();
    list.add(vnfcs);
    list.add(vnfcs);
    input.put("vnfcs", list);

    String jsonPayload = new ObjectMapper().writeValueAsString(input);

    when(dbService.getDependencyInfo(ctx)).thenReturn(jsonPayload);

    FlowControlNode flowControlNode = new FlowControlNode(null, dbService);
    DependencyInfo dependencyInfo = flowControlNode.getDependencyInfo(ctx);

    Assert.assertEquals("DependencyInfo [vnfcs=[Vnfcs [vnfcType=some-type, mandatory=some-mandatory, resilience=some-resilence, parents=[]], Vnfcs [vnfcType=some-type, mandatory=some-mandatory, resilience=some-resilence, parents=[]]]]", dependencyInfo.toString());
  }
}
