/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications Copyright (C) 2019 Ericsson
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

package org.onap.appc.aai.client.node;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.onap.appc.aai.client.AppcAaiClientConstant;
import org.onap.appc.aai.client.aai.AaiService;
import org.onap.ccsdk.sli.adaptors.aai.AAIClient;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;



public class TestAAIResourceNode {

    //Removed for ONAP integration

    private static final EELFLogger log = EELFManager.getInstance().getLogger(TestAAIResourceNode.class);

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void testGetVnfInfo() throws Exception {
        AAIResourceNode aai = Mockito.spy(new AAIResourceNode());
        Map<String, String> inParams = new HashMap<>();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vm[0].cloud-owner", "TEST");
        ctx.setAttribute("vm[0].cloud-region-id", "TEST");
        AaiService aaiService = Mockito.mock(AaiService.class);
        Mockito.doReturn(aaiService).when(aai).getAaiService();
        aai.getVnfInfo(inParams, ctx);
        Mockito.verify(aaiService).getIdentityUrl(Mockito.anyMap(), Mockito.any(SvcLogicContext.class));
    }

    @Test
    public void sortVServer() throws Exception{
        ArrayList<Map<String, String>> vservers = new ArrayList<Map<String, String>>();
        Map<String, String> vserverMap = new HashMap<>();
        vserverMap.put("vserver-id", "vserverId9");
        vserverMap.put("tenant-id", "tenantId9");
        vserverMap.put("cloud-owner", "cloudOwner9");
        vserverMap.put("cloud-region-id", "cloudRegionId9");
        vserverMap.put("vserver-name", "vServerName9");
        vservers.add(vserverMap);
        vserverMap = new HashMap<>();
        vserverMap.put("vserver-id", "vserverId1");
        vserverMap.put("tenant-id", "tenantId1");
        vserverMap.put("cloud-owner", "cloudOwner1");
        vserverMap.put("cloud-region-id", "cloudRegionId1");
        vserverMap.put("vserver-name", "vServerName1");
        vservers.add(vserverMap);
        vserverMap = new HashMap<>();
        vserverMap.put("vserver-id", "vserverId3");
        vserverMap.put("tenant-id", "tenantId3");
        vserverMap.put("cloud-owner", "cloudOwner3");
        vserverMap.put("cloud-region-id", "cloudRegionId3");
        vserverMap.put("vserver-name", "vServerName3");
        vservers.add(vserverMap);
        Collections.sort(vservers, new Comparator<Map<String, String>>() {
            @Override
            public int compare(Map<String, String> o1, Map<String, String> o2) {
                return o1.get("vserver-name").compareTo(o2.get("vserver-name"));
            }
        });

        SvcLogicContext ctx = new SvcLogicContext();
        AAIResourceNode aai = new AAIResourceNode();
        aai.populateContext(vservers, ctx, "vserver.");
        log.info(ctx.getAttribute("vserver.vm[0].vserver-name"));
        assertNotNull(vserverMap);
    }

    @Test
    public void testAllVServer() throws Exception{

        MockAAIResourceNode mrn = new MockAAIResourceNode();
        SvcLogicContext ctx = new SvcLogicContext();
        populateAllVServerInfo(ctx, "tmp.vnfInfo");
        Map<String, String> inParams = new HashMap<>();
        inParams.put("responsePrefix", "tmp.vnfInfo");
        mrn.getAllVServersVnfcsInfo(inParams, ctx);
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vm-count"), "2");
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vnf.vm-count"), "2");
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vnf.vm-with-no-vnfcs-count"), "0");
        // VM1
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vm[0].vserver-id"), "ibcsm0002id");
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vm[0].tenant-id"), "tenantid2");
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vm[0].cloud-owner"), "cloudOwner2");
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vm[0].cloud-region-id"), "cloudRegionId2");
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vm[0].vserver-name"), "vserverName2");
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vm[0].vf-module-id"), "vfModule2");
        //assertNull(ctx.getAttribute("tmp.vnfInfo.vm[0].vnfc-name"));

        // VM2
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vm[1].vserver-id"), "ibcxvm0001id");
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vm[1].tenant-id"), "tenantid1");
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vm[1].cloud-owner"), "cloudOwner1");
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vm[1].cloud-region-id"), "cloudRegionId1");
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vm[1].vserver-name"), "vserverName2");
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vm[1].vf-module-id"), "vfModule2");
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vm[1].vnfc-name"), "vnfcName2");
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vm[1].vnfc-type"), "vnfcType2");
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vm[1].vnfc-function-code"), "vnfcFuncCode2");
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vm[1].group-notation"), "vnfcGrpNot2");

        ctx.setAttribute("tmp.vnfInfo.vm[0].vserver-id","ibcm0001id");
        ctx.setAttribute("req-vf-module-id","vfModule1");
        mrn.getAllVServersVnfcsInfo(inParams, ctx);
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vnf.vm-with-no-vnfcs-count-vf-module"),"1");
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vnf.vm-count-for-vf-module"),"1");
    }

    @Test
    public void testAllVServerExceptionFlow() throws Exception{
        AAIResourceNode aai = Mockito.spy(new AAIResourceNode());
        Map<String, String> inParams = new HashMap<>();
        SvcLogicContext ctx = new SvcLogicContext();
        AaiService aaiService = Mockito.mock(AaiService.class);
        Mockito.doReturn(aaiService).when(aai).getAaiService();
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("Unable to get VServers for the VNF");
        aai.getAllVServersVnfcsInfo(inParams, ctx);
    }

    public void populateAllVServerInfo(SvcLogicContext ctx, String prefix) throws Exception {
         ctx.setAttribute("vnf-id", "ibcx0001v");
         ctx.setAttribute("vnf-host-ip-address", "000.00.00.00");
         ctx.setAttribute(prefix + ".vm-count", "2");
         ctx.setAttribute(prefix+ ".vm[0].vserver-id", "ibcsm0002id");
         ctx.setAttribute(prefix+ ".vm[0].tenant-id", "tenantid2");
         ctx.setAttribute(prefix+ ".vm[0].cloud-owner", "cloudOwner2");
         ctx.setAttribute(prefix+ ".vm[0].cloud-region-id", "cloudRegionId2");
         ctx.setAttribute(prefix+ ".vm[1].vserver-id", "ibcxvm0001id");
         ctx.setAttribute(prefix+ ".vm[1].tenant-id", "tenantid1");
         ctx.setAttribute(prefix+ ".vm[1].cloud-owner", "cloudOwner1");
         ctx.setAttribute(prefix+ ".vm[1].cloud-region-id", "cloudRegionId1");
    }

    public static class MockAAIResourceNode extends AAIResourceNode {
        private static final EELFLogger log = EELFManager.getInstance().getLogger(MockAAIResourceNode.class);
         private AAIClient aaiClient;

        public AaiService getAaiService() {
            log.info("In MockAAI");
            return new MockAaiService(aaiClient);
        }
    }

    @Test
    public void testPopulateContext() throws Exception{
        ArrayList<Map<String, String>> vservers = new ArrayList<Map<String, String>>();
        Map<String, String> vserverMap = new HashMap<>();
        vserverMap = new HashMap<>();
        vserverMap.put("vserver-id", "vserverId1");
        vserverMap.put("tenant-id", "tenantId1");
        vserverMap.put("cloud-owner", "cloudOwner1");
        vserverMap.put("cloud-region-id", "cloudRegionId1");
        vserverMap.put("vserver-name", "vServerName1");
        vserverMap.put("vnfc-name", "vnfcName1");
        vservers.add(vserverMap);
        vserverMap = new HashMap<>();
        vserverMap.put("vserver-id", "vserverId3");
        vserverMap.put("tenant-id", "tenantId3");
        vserverMap.put("cloud-owner", "cloudOwner3");
        vserverMap.put("cloud-region-id", "cloudRegionId3");
        vserverMap.put("vserver-name", "vServerName3");
        vservers.add(vserverMap);
        vserverMap = new HashMap<>();
        vserverMap.put("vserver-id", "vserverId9");
        vserverMap.put("tenant-id", "tenantId9");
        vserverMap.put("cloud-owner", "cloudOwner9");
        vserverMap.put("cloud-region-id", "cloudRegionId9");
        vserverMap.put("vserver-name", "vServerName9");
        vservers.add(vserverMap);
        SvcLogicContext ctx = new SvcLogicContext();
        AAIResourceNode aai = new AAIResourceNode();
        aai.populateContext(vservers, ctx, "tmp.vnfInfo.");
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vm[0].vserver-name"), "vServerName1");
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vm[0].tenant-id"), "tenantId1");
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vm[0].cloud-owner"), "cloudOwner1");
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vm[0].cloud-region-id"), "cloudRegionId1");
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vm[0].vserver-id"), "vserverId1");
        assertEquals(ctx.getAttribute("vm-name"), "vServerName3");
    }

    @Test
    public final void testGetVnfInfoExceptionFlow() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        AAIResourceNode aai = Mockito.spy(new AAIResourceNode());
        Map<String, String> inParams = new HashMap<>();
        ctx.setAttribute("vm[0].cloud-owner", "TEST");
        ctx.setAttribute("vm[0].cloud-region-id", "TEST");
        AaiService aaiService = Mockito.mock(AaiService.class);
        Mockito.doThrow(new SvcLogicException("TEST")).when(aaiService).getIdentityUrl(Mockito.anyMap(), Mockito.any(SvcLogicContext.class));
        Mockito.doReturn(aaiService).when(aai).getAaiService();
        expectedEx.expect(SvcLogicException.class);
        expectedEx.expectMessage("TEST");
        aai.getVnfInfo(inParams, ctx);
    }

    @Test
    public final void testaddVnfcs() throws SvcLogicException
    {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vnfcReference_length", "1");
        ctx.setAttribute("vnf.vm-count", "1");
        AAIResourceNode aai = Mockito.spy(new AAIResourceNode());
        Map<String, String> inParams = new HashMap<>();
        AaiService aaiService = Mockito.mock(AaiService.class);
        Mockito.doReturn(aaiService).when(aai).getAaiService();
        aai.addVnfcs(inParams, ctx);
        assertNotNull(ctx);
    }

    @Test
    public final void testaddVnfcsExceptionFlow() throws Exception
    {
        SvcLogicContext ctx = new SvcLogicContext();
        AAIResourceNode aai = Mockito.spy(new AAIResourceNode());
        Map<String, String> inParams = new HashMap<>();
        inParams.put("responsePrefix", "tmp.vnfInfo");
        AaiService aaiService = Mockito.mock(AaiService.class);
        Mockito.doThrow(new SvcLogicException("TEST")).when(aaiService).getIdentityUrl(Mockito.anyMap(), Mockito.any(SvcLogicContext.class));
        Mockito.doReturn(aaiService).when(aai).getAaiService();
        expectedEx.expect(SvcLogicException.class);
        aai.addVnfcs(inParams, ctx);
    }

    @Test
    public final void  testupdateVnfAndVServerStatus() throws SvcLogicException{
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("vnf.vm-count", "1");
        AAIResourceNode aai = Mockito.spy(new AAIResourceNode());
        Map<String, String> inParams = new HashMap<>();
        AaiService aaiService = Mockito.mock(AaiService.class);
        Mockito.doReturn(aaiService).when(aai).getAaiService();
        aai.updateVnfAndVServerStatus(inParams, ctx);
        assertEquals(AppcAaiClientConstant.OUTPUT_STATUS_SUCCESS, ctx.getAttribute(AppcAaiClientConstant.OUTPUT_PARAM_STATUS));
    }

    @Test
    public final void  testupdateVnfAndVServerStatusExceptionFlow() throws SvcLogicException{
        SvcLogicContext ctx = new SvcLogicContext();
        AAIResourceNode aai = new AAIResourceNode();
        Map<String, String> inParams = new HashMap<>();
        expectedEx.expect(SvcLogicException.class);
        aai.updateVnfAndVServerStatus(inParams, ctx);
    }

    @Test
    public void testgetVfModduleModelInfo() throws Exception{
        SvcLogicContext ctx = new SvcLogicContext();
        AAIResourceNode aai = new AAIResourceNode();
        AAIClient aaic = null;
        MockAaiService aaiService = new MockAaiService(aaic);
        Map<String, String> inParams = new HashMap<>();
        inParams.put("responsePrefix", "tmp.vnfInfo");
        aai.processForVfModuleModelInfo(aaiService, inParams, ctx);
        assertEquals(ctx.getAttribute("template-model-id"), "model0001");
    }

    @Test
    public final void testSetVmParams() {
        SvcLogicContext ctx = new SvcLogicContext();
        String vServerId = "vserver02";
        ctx.setAttribute("tmp.vnfInfo.vm-count","3");
        ctx.setAttribute("tmp.vnfInfo.vm[0].vserver-id", "vserver01");
        ctx.setAttribute("tmp.vnfInfo.vm[1].vserver-id", "vserver02");
        ctx.setAttribute("tmp.vnfInfo.vm[1].tenant-id", "ten01");
        ctx.setAttribute("tmp.vnfInfo.vm[1].cloud-region-id", "cr01");
        ctx.setAttribute("tmp.vnfInfo.vm[1].cloud-owner", "co01");
        AAIResourceNode aairn= new AAIResourceNode();
        Map <String, String> params = aairn.setVmParams(ctx, vServerId);
        assertNotNull(params);
    }

    @Test
    public final void testGetVnfcInformationForVserver() throws Exception{
        MockAAIResourceNode aairn = new MockAAIResourceNode();
        SvcLogicContext ctx = new SvcLogicContext();
        SvcLogicContext newVnfcCtx = new SvcLogicContext();
        Map<String, String> inParams = new HashMap<>();
        Map<String, String> vnfcParams = new HashMap<>();
        String responsePrefix = "test.";
        inParams.put(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX, "test");
        vnfcParams.put("vnfcName", "vnfcName2");
        aairn.getVnfcInformationForVserver(vnfcParams, newVnfcCtx, inParams, ctx, aairn.getAaiService(), responsePrefix);
        assertEquals(ctx.getAttribute("test.vm.vnfc.vnfc-name"), "vnfcName2");
        assertEquals(ctx.getAttribute("test.vm.vnfc.vnfc-type"), "vnfcType2");
        assertEquals(ctx.getAttribute("test.vm.vnfc.vnfc-function-code"), "vnfcFuncCode2");
        assertEquals(ctx.getAttribute("test.vm.vnfc.vnfc-group-notation"), "vnfcGrpNot2");
    }

    @Test
    public final void testGetFormattedValue() throws Exception{
        MockAAIResourceNode aairn = new MockAAIResourceNode();
        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> inParams = new HashMap<>();
        inParams.put(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX, "test");
        inParams.put("inputParameter", "Some/Value/With/ Many Spaces");
        aairn.getFormattedValue(inParams, ctx);
        assertEquals(ctx.getAttribute("template-model-id"), "Some_Value_With_ManySpaces");
    }

    @Test
    public final void testProcessCheckForVfModule() throws Exception{
        MockAAIResourceNode aairn = new MockAAIResourceNode();
        SvcLogicContext ctx = new SvcLogicContext();
        Map<String,String> inParams = new HashMap<String, String>();
        inParams.put(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX, "test");
        inParams.put("inputParameter", "Some/Value/With/ Many Spaces");
        ctx.setAttribute("test.vnf.vm-with-no-vnfcs-count-vf-module", "0");
        ctx.setAttribute("test.vnf.vm-count-for-vf-module", "2");
        aairn.processCheckForVfModule("vfmoduleId1", ctx, "test.", 2);
        assertNotNull(aairn);
    }

    @Test
    public void testGetVserverInfo() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.vnfInfo.vm-count", "1");
        AAIResourceNode aai = Mockito.spy(new AAIResourceNode());
        Map<String, String> inParams = new HashMap<>();
        AaiService aaiService = Mockito.mock(AaiService.class);
        Mockito.doReturn(aaiService).when(aai).getAaiService();
        aai.getVserverInfo(inParams, ctx);
        Mockito.verify(aaiService).getVMInfo(Mockito.anyMap(), Mockito.any(SvcLogicContext.class));
    }

    @Test
    public void testGetVserverInfoExceptionFlow() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.vnfInfo.vm-count", "1");
        Map<String, String> inParams = new HashMap<>();
        AaiService aaiService = Mockito.mock(AaiService.class);
        AAIResourceNode aai = Mockito.spy(new AAIResourceNode());
        Mockito.doThrow(new SvcLogicException("TEST")).when(aaiService).getVMInfo(Mockito.anyMap(), Mockito.any(SvcLogicContext.class));
        Mockito.doReturn(aaiService).when(aai).getAaiService();
        aai.getVserverInfo(inParams, ctx);
        assertEquals("TEST", ctx.getAttribute(AppcAaiClientConstant.OUTPUT_PARAM_ERROR_MESSAGE));
    }
}
