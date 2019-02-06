/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2019 Ericsson. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.aai.interfaceImpl;

import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.sdnc.config.params.data.Parameter;
import org.onap.sdnc.config.params.data.ResponseKey;

public class TestAaiInterfaceRulesHandler {

  private AaiInterfaceRulesHandler aaiInterfaceRulesHandler;
  private Parameter parameters;
  private SvcLogicContext context;
  ResponseKey responseKey;
  private List<ResponseKey> responseKeys;
  private Set<String> params = new HashSet<>();

  @Before
  public void setUp() {
    parameters = Mockito.mock(Parameter.class);
    context = Mockito.mock(SvcLogicContext.class);
    responseKey = new ResponseKey();
    responseKey.setFieldKeyName("ipaddress-v4-oam-vip");
    responseKey.setFilterByValue("vnfc-function-code");
    responseKey.setFilterByField("vnfc-function-code");
    responseKeys = new ArrayList<>();
    responseKeys.add(responseKey);
    params.add("param1");
    when(context.getAttributeKeySet()).thenReturn(params);
    when(context.getAttribute("tmp.vnfInfo.vm-count")).thenReturn("1");
    when(context.getAttribute("tmp.vnfInfo.vm[" + 0 + "].vnfc-count")).thenReturn("1");
    when(context.getAttribute("tmp.vnfInfo.vm[" + 0 + "].vnfc-function-code"))
        .thenReturn("vnfc-function-code");
    when(context.getAttribute("tmp.vnfInfo.vm[" + 0 + "].vnfc-ipaddress-v4-oam-vip"))
        .thenReturn("0.0.0.0");
    when(context.getAttribute("tmp.vnfInfo.vm[" + 0 + "].vnfc-name")).thenReturn("vnfc-name");
    when(context.getAttribute("tmp.vnfInfo.vm[" + 0 + "].vserver-name")).thenReturn("vserver");
    aaiInterfaceRulesHandler = new AaiInterfaceRulesHandler(parameters, context);
  }

  @Test
  public void testProcessRuleVnf() {
    responseKey.setUniqueKeyValue("vnf");
    when(parameters.getName()).thenReturn("aaikey");
    when(parameters.getResponseKeys()).thenReturn(responseKeys);
    when(context.getAttribute("tmp.vnfInfo.vnf.ipv4-oam-address")).thenReturn("0.0.0.0");
    aaiInterfaceRulesHandler.processRule();
  }

  @Test
  public void testProcessRuleVnfc() {
    responseKey.setUniqueKeyValue("vnfc");
    when(parameters.getName()).thenReturn("aaikey");
    when(parameters.getResponseKeys()).thenReturn(responseKeys);
    when(context.getAttribute("tmp.vnfInfo.vnf.ipv4-oam-address")).thenReturn("0.0.0.0");
    aaiInterfaceRulesHandler.processRule();
  }

  @Test
  public void testProcessRuleVserver() {
    responseKey.setUniqueKeyValue("vserver");
    responseKey.setFieldKeyName("vserver-name");
    responseKey.setFilterByValue("1");
    responseKey.setFilterByField("vm-number");
    when(parameters.getName()).thenReturn("aaikey");
    when(parameters.getResponseKeys()).thenReturn(responseKeys);
    when(context.getAttribute("tmp.vnfInfo.vnf.ipv4-oam-address")).thenReturn("0.0.0.0");
    aaiInterfaceRulesHandler.processRule();
  }

  @Test
  public void testProcessRuleVserverElseCase() {
    responseKey.setUniqueKeyValue("vserver");
    responseKey.setFieldKeyName("vserver-name");
    responseKey.setFilterByValue(null);
    responseKey.setFilterByField("vm-number");
    when(parameters.getName()).thenReturn("aaikey");
    when(parameters.getResponseKeys()).thenReturn(responseKeys);
    when(context.getAttribute("tmp.vnfInfo.vnf.ipv4-oam-address")).thenReturn("0.0.0.0");
    aaiInterfaceRulesHandler.processRule();
  }

  @Test
  public void testProcessRuleWithVnfcname() {
    responseKey.setFieldKeyName("vnfc-name");
    responseKey.setUniqueKeyValue("vnfc");
    when(parameters.getName()).thenReturn("aaikey");
    when(parameters.getResponseKeys()).thenReturn(responseKeys);
    when(context.getAttribute("tmp.vnfInfo.vnf.ipv4-oam-address")).thenReturn("0.0.0.0");
    aaiInterfaceRulesHandler.processRule();
  }

  @Test
  public void testProcessRuleWithNoVnfcname() {
    responseKey.setFieldKeyName("vnfc-name");
    responseKey.setUniqueKeyValue("vnfc");
    responseKey.setFilterByValue(null);
    when(parameters.getName()).thenReturn("aaikey");
    when(parameters.getResponseKeys()).thenReturn(responseKeys);
    when(context.getAttribute("tmp.vnfInfo.vnf.ipv4-oam-address")).thenReturn("0.0.0.0");
    aaiInterfaceRulesHandler.processRule();
  }

  @Test
  public void testProcessRuleWithIp() {
    responseKey.setFieldKeyName("ipaddress-v4-oam-vip");
    responseKey.setUniqueKeyValue("vnfc");
    responseKey.setFilterByValue(null);
    when(parameters.getName()).thenReturn("aaikey");
    when(parameters.getResponseKeys()).thenReturn(responseKeys);
    when(context.getAttribute("tmp.vnfInfo.vnf.ipv4-oam-address")).thenReturn("0.0.0.0");
    aaiInterfaceRulesHandler.processRule();
  }

}
