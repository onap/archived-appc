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

package org.openecomp.appc.aai.client.node;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.openecomp.appc.aai.client.AppcAaiClientConstant;
import org.openecomp.appc.aai.client.aai.AaiService;
import org.openecomp.appc.aai.client.aai.TestAaiService;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.adaptors.aai.AAIClient;

import com.fasterxml.jackson.databind.ObjectMapper;


public class TestAAIResourceNode {
    
    //Removed for ONAP integration

    private static final EELFLogger log = EELFManager.getInstance().getLogger(TestAAIResourceNode.class);
    
    @Test
    public void sortVServer() throws Exception{
        
        //log.info("Test");
        
        ArrayList<Map<String, String>> vservers = new ArrayList<Map<String, String>>();
        HashMap<String, String> vserverMap = new HashMap<String, String>();
        vserverMap.put("vserver-id", "vserverId9");
        vserverMap.put("tenant-id", "tenantId9");
        vserverMap.put("cloud-owner", "cloudOwner9");
        vserverMap.put("cloud-region-id", "cloudRegionId9");
        vserverMap.put("vserver-name", "vServerName9");
        vservers.add(vserverMap);
        vserverMap = new HashMap<String, String>();
        vserverMap.put("vserver-id", "vserverId1");
        vserverMap.put("tenant-id", "tenantId1");
        vserverMap.put("cloud-owner", "cloudOwner1");
        vserverMap.put("cloud-region-id", "cloudRegionId1");
        vserverMap.put("vserver-name", "vServerName1");
        vservers.add(vserverMap);
        vserverMap = new HashMap<String, String>();
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
    }

    @Test
    public void testAllVServer() throws Exception{
        
        MockAAIResourceNode mrn = new MockAAIResourceNode();
        SvcLogicContext ctx = new SvcLogicContext();
        populateAllVServerInfo(ctx, "tmp.vnfInfo");
        Map<String, String> inParams  =new HashMap<String, String>();
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
        HashMap<String, String> vserverMap = new HashMap<String, String>();
        vserverMap = new HashMap<String, String>();
        vserverMap.put("vserver-id", "vserverId1");
        vserverMap.put("tenant-id", "tenantId1");
        vserverMap.put("cloud-owner", "cloudOwner1");
        vserverMap.put("cloud-region-id", "cloudRegionId1");
        vserverMap.put("vserver-name", "vServerName1");
        vserverMap.put("vnfc-name", "vnfcName1");
        vservers.add(vserverMap);
        vserverMap = new HashMap<String, String>();
        vserverMap.put("vserver-id", "vserverId3");
        vserverMap.put("tenant-id", "tenantId3");
        vserverMap.put("cloud-owner", "cloudOwner3");
        vserverMap.put("cloud-region-id", "cloudRegionId3");
        vserverMap.put("vserver-name", "vServerName3");
        vservers.add(vserverMap);
        vserverMap = new HashMap<String, String>();
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
    public final void testGetVnfInfo() {
        SvcLogicContext ctx = new SvcLogicContext();
        AAIResourceNode aai = new AAIResourceNode();
Map<String, String> inParams  =new HashMap<String, String>();
        inParams.put("responsePrefix", "tmp.vnfInfo");
        try {
            aai.getVnfInfo(inParams, ctx);
        } catch (SvcLogicException e) {
            e.printStackTrace();
        }
    
    }
    @Test
    public final void testaddVnfcs()
    {
        SvcLogicContext ctx = new SvcLogicContext();
        AAIResourceNode aai = new AAIResourceNode();
Map<String, String> inParams  =new HashMap<String, String>();
        inParams.put("responsePrefix", "tmp.vnfInfo");
        try {
            aai.addVnfcs(inParams, ctx);
        } catch (SvcLogicException e) {
            e.printStackTrace();
        }
        
    }
    @Test
    public final void  testupdateVnfAndVServerStatus(){
        SvcLogicContext ctx = new SvcLogicContext();
        AAIResourceNode aai = new AAIResourceNode();
Map<String, String> inParams  =new HashMap<String, String>();
        
        inParams.put("responsePrefix", "tmp.vnfInfo");
        try {
            aai.updateVnfAndVServerStatus(inParams, ctx);
        } catch (SvcLogicException e) {
            e.printStackTrace();
        }
    }
}
