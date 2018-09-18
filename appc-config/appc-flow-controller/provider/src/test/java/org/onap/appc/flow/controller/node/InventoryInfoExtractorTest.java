/*
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 Nokia. All rights reserved.
 * Copyright (C) 2018 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */
package org.onap.appc.flow.controller.node;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.onap.appc.flow.controller.interfaceData.InventoryInfo;
import org.onap.appc.flow.controller.interfaceData.VnfInfo;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

public class InventoryInfoExtractorTest {

  private SvcLogicContext ctx;
  private InventoryInfoExtractor inventoryInfoExtractor;

  @Before
  public void setUp() {
    inventoryInfoExtractor = new InventoryInfoExtractor();
    ctx = mock(SvcLogicContext.class);
  }

  @Test
  public void full_config() throws Exception {
    when(ctx.getAttribute("tmp.vnfInfo.vnf.vnf-name")).thenReturn("some-vnf-name");
    when(ctx.getAttribute("tmp.vnfInfo.vnf.vnf-type")).thenReturn("some-vnf-type");
    when(ctx.getAttribute("tmp.vnfInfo.vm-count")).thenReturn("2");

    when(ctx.getAttribute("tmp.vnfInfo.vm[0].vserver-id")).thenReturn("some-id-0");
    when(ctx.getAttribute("tmp.vnfInfo.vm[0].vnfc-count")).thenReturn("2");
    when(ctx.getAttribute("tmp.vnfInfo.vm[0].vnfc-name")).thenReturn("some-vnfc-name-0");
    when(ctx.getAttribute("tmp.vnfInfo.vm[0].vnfc-type")).thenReturn("some-vnfc-type-0");

    when(ctx.getAttribute("tmp.vnfInfo.vm[1].vserver-id")).thenReturn("some-id-1");
    when(ctx.getAttribute("tmp.vnfInfo.vm[1].vnfc-count")).thenReturn("1");
    when(ctx.getAttribute("tmp.vnfInfo.vm[1].vnfc-name")).thenReturn("some-vnfc-name-1");
    when(ctx.getAttribute("tmp.vnfInfo.vm[1].vnfc-type")).thenReturn("some-vnfc-type-1");

    when(ctx.getAttribute("tmp.vnfInfo.cloud-region.identity-url")).thenReturn("some-url");

    String vnfId = "some-vnf-id";
    InventoryInfo inventoryInfo = inventoryInfoExtractor.getInventoryInfo(ctx, vnfId);

    Assert.assertEquals(
        "InventoryInfo [vnfInfo=VnfInfo [vnfId=some-vnf-id, vnfName=some-vnf-name, vnfType=some-vnf-type, " +
        "identityUrl=some-url, vm=[Vm [vserverId=some-id-0, vmId=null, " +
        "vnfc=Vnfcslist [vnfcType=some-vnfc-type-0, vnfcName=some-vnfc-name-0]], Vm [vserverId=some-id-1, vmId=null, vnfc=Vnfcslist [vnfcType=some-vnfc-type-1, vnfcName=some-vnfc-name-1]]]]]",
        inventoryInfo.toString());
  }

  @Test
  public void full_config__with_zero__vnfc_count() throws Exception {
    when(ctx.getAttribute("tmp.vnfInfo.vnf.vnf-name")).thenReturn("some-vnf-name");
    when(ctx.getAttribute("tmp.vnfInfo.vnf.vnf-type")).thenReturn("some-vnf-type");
    when(ctx.getAttribute("tmp.vnfInfo.vm-count")).thenReturn("2");
    when(ctx.getAttribute("tmp.vnfInfo.cloud-region.identity-url")).thenReturn("some-url");

    when(ctx.getAttribute("tmp.vnfInfo.vm[0].vserver-id")).thenReturn("some-id-0");
    when(ctx.getAttribute("tmp.vnfInfo.vm[0].vnfc-count")).thenReturn("2");
    when(ctx.getAttribute("tmp.vnfInfo.vm[0].vnfc-name")).thenReturn("some-vnfc-name-0");
    when(ctx.getAttribute("tmp.vnfInfo.vm[0].vnfc-type")).thenReturn("some-vnfc-type-0");

    when(ctx.getAttribute("tmp.vnfInfo.vm[1].vserver-id")).thenReturn("some-id-1");
    when(ctx.getAttribute("tmp.vnfInfo.vm[1].vnfc-count")).thenReturn("0");
    when(ctx.getAttribute("tmp.vnfInfo.vm[1].vnfc-name")).thenReturn("some-vnfc-name-1");
    when(ctx.getAttribute("tmp.vnfInfo.vm[1].vnfc-type")).thenReturn("some-vnfc-type-1");

    String vnfId = "some-vnf-id";
    InventoryInfo inventoryInfo = inventoryInfoExtractor.getInventoryInfo(ctx, vnfId);

    Assert.assertEquals(
            "InventoryInfo [vnfInfo=VnfInfo [vnfId=some-vnf-id, vnfName=some-vnf-name, "
            + "vnfType=some-vnf-type, identityUrl=some-url, vm=[Vm [vserverId=some-id-0, "
            + "vmId=null, vnfc=Vnfcslist [vnfcType=some-vnfc-type-0, vnfcName=some-vnfc-name-0]], "
            + "Vm [vserverId=some-id-1, vmId=null, vnfc=null]]]]",
            inventoryInfo.toString());
  }

  @Test
  public void full_config__with_zero__vm_count() throws Exception {
    when(ctx.getAttribute("tmp.vnfInfo.vnf.vnf-name")).thenReturn("some-vnf-name");
    when(ctx.getAttribute("tmp.vnfInfo.vnf.vnf-type")).thenReturn("some-vnf-type");
    when(ctx.getAttribute("tmp.vnfInfo.vm-count")).thenReturn("0");
    when(ctx.getAttribute("tmp.vnfInfo.cloud-region.identity-url")).thenReturn("some-url");


    when(ctx.getAttribute("tmp.vnfInfo.vm[0].vserver-id")).thenReturn("some-id-0");
    when(ctx.getAttribute("tmp.vnfInfo.vm[0].vnfc-count")).thenReturn("2");
    when(ctx.getAttribute("tmp.vnfInfo.vm[0].vnfc-name")).thenReturn("some-vnfc-name-0");
    when(ctx.getAttribute("tmp.vnfInfo.vm[0].vnfc-type")).thenReturn("some-vnfc-type-0");

    when(ctx.getAttribute("tmp.vnfInfo.vm[1].vserver-id")).thenReturn("some-id-1");
    when(ctx.getAttribute("tmp.vnfInfo.vm[1].vnfc-count")).thenReturn("0");
    when(ctx.getAttribute("tmp.vnfInfo.vm[1].vnfc-name")).thenReturn("some-vnfc-name-1");
    when(ctx.getAttribute("tmp.vnfInfo.vm[1].vnfc-type")).thenReturn("some-vnfc-type-1");

    String vnfId = "some-vnf-id";
    InventoryInfo inventoryInfo = inventoryInfoExtractor.getInventoryInfo(ctx, vnfId);

    Assert.assertEquals("InventoryInfo [vnfInfo=VnfInfo [vnfId=some-vnf-id, vnfName=some-vnf-name, "
            + "vnfType=some-vnf-type, identityUrl=some-url, vm=null]]",
            inventoryInfo.toString());
  }

  @Test
  public void full_config__with_empty__vm_count() throws Exception {
    when(ctx.getAttribute("tmp.vnfInfo.vnf.vnf-name")).thenReturn("some-vnf-name");
    when(ctx.getAttribute("tmp.vnfInfo.vnf.vnf-type")).thenReturn("some-vnf-type");
    when(ctx.getAttribute("tmp.vnfInfo.vm-count")).thenReturn("");
    when(ctx.getAttribute("tmp.vnfInfo.cloud-region.identity-url")).thenReturn("some-url");

    when(ctx.getAttribute("tmp.vnfInfo.vm[0].vserver-id")).thenReturn("some-id-0");
    when(ctx.getAttribute("tmp.vnfInfo.vm[0].vnfc-count")).thenReturn("2");
    when(ctx.getAttribute("tmp.vnfInfo.vm[0].vnfc-name")).thenReturn("some-vnfc-name-0");
    when(ctx.getAttribute("tmp.vnfInfo.vm[0].vnfc-type")).thenReturn("some-vnfc-type-0");

    when(ctx.getAttribute("tmp.vnfInfo.vm[1].vserver-id")).thenReturn("some-id-1");
    when(ctx.getAttribute("tmp.vnfInfo.vm[1].vnfc-count")).thenReturn("0");
    when(ctx.getAttribute("tmp.vnfInfo.vm[1].vnfc-name")).thenReturn("some-vnfc-name-1");
    when(ctx.getAttribute("tmp.vnfInfo.vm[1].vnfc-type")).thenReturn("some-vnfc-type-1");

    String vnfId = "some-vnf-id";
    InventoryInfo inventoryInfo = inventoryInfoExtractor.getInventoryInfo(ctx, vnfId);

    Assert.assertEquals(
            "InventoryInfo [vnfInfo=VnfInfo [vnfId=some-vnf-id, vnfName=some-vnf-name, "
            + "vnfType=some-vnf-type, identityUrl=some-url, vm=null]]",
            inventoryInfo.toString());
  }

  @Test
  public void full_config__with_null__vm_count() throws Exception {
    when(ctx.getAttribute("tmp.vnfInfo.vnf.vnf-name")).thenReturn("some-vnf-name");
    when(ctx.getAttribute("tmp.vnfInfo.vnf.vnf-type")).thenReturn("some-vnf-type");
    when(ctx.getAttribute("tmp.vnfInfo.vm-count")).thenReturn(null);
    when(ctx.getAttribute("tmp.vnfInfo.cloud-region.identity-url")).thenReturn("some-url");

    when(ctx.getAttribute("tmp.vnfInfo.vm[0].vserver-id")).thenReturn("some-id-0");
    when(ctx.getAttribute("tmp.vnfInfo.vm[0].vnfc-count")).thenReturn("2");
    when(ctx.getAttribute("tmp.vnfInfo.vm[0].vnfc-name")).thenReturn("some-vnfc-name-0");
    when(ctx.getAttribute("tmp.vnfInfo.vm[0].vnfc-type")).thenReturn("some-vnfc-type-0");

    when(ctx.getAttribute("tmp.vnfInfo.vm[1].vserver-id")).thenReturn("some-id-1");
    when(ctx.getAttribute("tmp.vnfInfo.vm[1].vnfc-count")).thenReturn("0");
    when(ctx.getAttribute("tmp.vnfInfo.vm[1].vnfc-name")).thenReturn("some-vnfc-name-1");
    when(ctx.getAttribute("tmp.vnfInfo.vm[1].vnfc-type")).thenReturn("some-vnfc-type-1");

    String vnfId = "some-vnf-id";
    InventoryInfo inventoryInfo = inventoryInfoExtractor.getInventoryInfo(ctx, vnfId);

    Assert.assertEquals(
            "InventoryInfo [vnfInfo=VnfInfo [vnfId=some-vnf-id, vnfName=some-vnf-name, "
            + "vnfType=some-vnf-type, identityUrl=some-url, vm=null]]",
            inventoryInfo.toString());
  }

  @Test
  public void testGetIdentityUrl_from_payload() throws Exception{
      InventoryInfoExtractor info = new InventoryInfoExtractor();
      when(ctx.getAttribute("identity-url")).thenReturn("some_url");
      VnfInfo vnfInfo = new VnfInfo();
      String url=info.getIdentityUrl(ctx, vnfInfo, "123");
      System.out.println(info.toString());
      Assert.assertEquals(url, "some_url");
  }

  @Test
  public void testGetIdentityUrl_from_Inventory() throws Exception{
      InventoryInfoExtractor info = new InventoryInfoExtractor();
      when(ctx.getAttribute("tmp.vnfInfo.cloud-region.identity-url")).thenReturn("some_url_from_inventory");
      VnfInfo vnfInfo = new VnfInfo();
      String url=info.getIdentityUrl(ctx, vnfInfo, "123");
      System.out.println(info.toString());
      Assert.assertEquals(url, "some_url_from_inventory");
  }

}
