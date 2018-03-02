package org.onap.appc.flow.controller.node;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.onap.appc.flow.controller.interfaceData.InventoryInfo;
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

    String vnfId = "some-vnf-id";
    InventoryInfo inventoryInfo = inventoryInfoExtractor.getInventoryInfo(ctx, vnfId);

    Assert.assertEquals(
        "InventoryInfo [vnfInfo=VnfInfo [vnfId=some-vnf-id, vnfName=some-vnf-name, vnfType=some-vnf-type, vm=[Vm [vserverId=some-id-0, vnfc=Vnfcslist [vnfcType=some-vnfc-type-0, vnfcName=some-vnfc-name-0]], Vm [vserverId=some-id-1, vnfc=Vnfcslist [vnfcType=some-vnfc-type-1, vnfcName=some-vnfc-name-1]]]]]",
        inventoryInfo.toString());
  }

  @Test
  public void full_config__with_zero__vnfc_count() throws Exception {
    when(ctx.getAttribute("tmp.vnfInfo.vnf.vnf-name")).thenReturn("some-vnf-name");
    when(ctx.getAttribute("tmp.vnfInfo.vnf.vnf-type")).thenReturn("some-vnf-type");
    when(ctx.getAttribute("tmp.vnfInfo.vm-count")).thenReturn("2");

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
        "InventoryInfo [vnfInfo=VnfInfo [vnfId=some-vnf-id, vnfName=some-vnf-name, vnfType=some-vnf-type, vm=[Vm [vserverId=some-id-0, vnfc=Vnfcslist [vnfcType=some-vnfc-type-0, vnfcName=some-vnfc-name-0]], Vm [vserverId=some-id-1, vnfc=null]]]]",
        inventoryInfo.toString());
  }

  @Test
  public void full_config__with_zero__vm_count() throws Exception {
    when(ctx.getAttribute("tmp.vnfInfo.vnf.vnf-name")).thenReturn("some-vnf-name");
    when(ctx.getAttribute("tmp.vnfInfo.vnf.vnf-type")).thenReturn("some-vnf-type");
    when(ctx.getAttribute("tmp.vnfInfo.vm-count")).thenReturn("0");

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
        "InventoryInfo [vnfInfo=VnfInfo [vnfId=some-vnf-id, vnfName=some-vnf-name, vnfType=some-vnf-type, vm=null]]",
        inventoryInfo.toString());
  }

  @Test
  public void full_config__with_empty__vm_count() throws Exception {
    when(ctx.getAttribute("tmp.vnfInfo.vnf.vnf-name")).thenReturn("some-vnf-name");
    when(ctx.getAttribute("tmp.vnfInfo.vnf.vnf-type")).thenReturn("some-vnf-type");
    when(ctx.getAttribute("tmp.vnfInfo.vm-count")).thenReturn("");

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
        "InventoryInfo [vnfInfo=VnfInfo [vnfId=some-vnf-id, vnfName=some-vnf-name, vnfType=some-vnf-type, vm=null]]",
        inventoryInfo.toString());
  }

  @Test
  public void full_config__with_null__vm_count() throws Exception {
    when(ctx.getAttribute("tmp.vnfInfo.vnf.vnf-name")).thenReturn("some-vnf-name");
    when(ctx.getAttribute("tmp.vnfInfo.vnf.vnf-type")).thenReturn("some-vnf-type");
    when(ctx.getAttribute("tmp.vnfInfo.vm-count")).thenReturn(null);

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
        "InventoryInfo [vnfInfo=VnfInfo [vnfId=some-vnf-id, vnfName=some-vnf-name, vnfType=some-vnf-type, vm=null]]",
        inventoryInfo.toString());
  }

}