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

package org.onap.appc.dg.common.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.onap.appc.exceptions.APPCException;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ResolverFactory.class)
public class TestDgResolverPluginImpl {

  private DgResolverPluginImpl dgResolverPluginImpl;
  private Map<String, String> params;
  private SvcLogicContext ctx;
  private AbstractResolver abstractResolver;
  private FlowKey flowKey;

  @Before
  public void setUp() {
    params = new HashMap<>();
    ctx = new SvcLogicContext();
    PowerMockito.mockStatic(ResolverFactory.class);
    flowKey = Mockito.mock(FlowKey.class);
    abstractResolver = Mockito.mock(AbstractResolver.class);
    PowerMockito.when(ResolverFactory.createResolver(anyString())).thenReturn(abstractResolver);
    dgResolverPluginImpl = new DgResolverPluginImpl();
    PowerMockito.when(flowKey.name()).thenReturn("flowName");
  }

  @Test
  public void testResolveDgVNF() throws APPCException {
    params.put("action", "healthcheck");
    params.put("vnfVersion", "1");
    params.put("api-ver", "1.0");
    params.put("DGResolutionType", "VNF");
    params.put("vnfType", "vnfType");
    PowerMockito.when(abstractResolver.resolve("healthcheck", "vnfType", "1", "1.0"))
        .thenReturn(flowKey);
    dgResolverPluginImpl.resolveDg(params, ctx);
    assertEquals("flowName", ctx.getAttribute("dg_name"));
  }

  @Test
  public void testResolveDgVNFC() throws APPCException {
    params.put("action", "healthcheck");
    params.put("vnfcType", "vnfcType");
    params.put("api-ver", "1.0");
    params.put("DGResolutionType", "VNFC");
    params.put("vnfType", "vnfType");
    PowerMockito.when(abstractResolver.resolve("healthcheck", "vnfType", "vnfcType", "1.0"))
        .thenReturn(flowKey);
    dgResolverPluginImpl.resolveDg(params, ctx);
    assertEquals("flowName", ctx.getAttribute("dg_name"));
  }

  @Test(expected = DgResolverException.class)
  public void testResolveDgWithException() throws APPCException {
    params.put("action", "healthcheck");
    params.put("vnfcType", "vnfcType");
    params.put("api-ver", "1.0");
    params.put("DGResolutionType", "VNFC");
    params.put("vnfType", "vnfType1");
    PowerMockito.when(abstractResolver.resolve("healthcheck", "vnfType", "vnfcType", "1.0"))
        .thenReturn(flowKey);
    dgResolverPluginImpl.resolveDg(params, ctx);
  }

  @Test(expected = DgResolverException.class)
  public void testResolveDgResolverNull() throws APPCException {
    PowerMockito.when(ResolverFactory.createResolver(anyString())).thenReturn(null);
    dgResolverPluginImpl.resolveDg(params, ctx);
  }
}
