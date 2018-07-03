/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.aai.client.aai;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.adaptors.aai.AAIClient;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class TestAaiService {
    // ONAP merging

    private static final EELFLogger log = EELFManager.getInstance().getLogger(TestAaiService.class);
    private AAIClient aaiClient;

    @Test
    public void testGetGenericVnfInfo() throws Exception {

        MockAaiService mockAai = new MockAaiService(aaiClient);

        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put("vnfId", "ibcxvm0000");
        inParams.put("responsePrefix", "tmp.vnfInfo");

        SvcLogicContext ctx = new SvcLogicContext();

        mockAai.getGenericVnfInfo(inParams, ctx);

        assertEquals(ctx.getAttribute("tmp.vnfInfo.vnf.vnf-type"), "vUSP-Metaswitch");
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vm-count"), "2");
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vm[0].vserver-id"), "ibcx001vm001-id");
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vm[1].vserver-id"), "ibcx000000");
    }

    @Test
    public void testGetVmInfo() throws Exception {

        MockAaiService mockAai = new MockAaiService(aaiClient);

        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put("vserverId", "vserverId1");
        inParams.put("tenantId", "tenantId1");
        inParams.put("cloudOwner", "cloudOwner1");
        inParams.put("cloudRegionId", "cloudRegionId1");
        inParams.put("responsePrefix", "tmp.vnfInfo");

        SvcLogicContext ctx = new SvcLogicContext();

        mockAai.getVMInfo(inParams, ctx);

        printContext(ctx);

        assertEquals(ctx.getAttribute("tmp.vnfInfo.vm.vserver-name"), "ibcx0000000");
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vm.vnfc-count"), "1");
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vm.vf-module-id"), "vfModule1");
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vm.vnfc[0].vnfc-name"), "ibcx0001vm001vnfc1");

    }

    @Test
    public void testGetVnfcInfo() throws Exception {

        MockAaiService mockAai = new MockAaiService(aaiClient);

        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put("vnfcName", "vnfcName1");

        inParams.put("responsePrefix", "tmp.vnfInfo");

        SvcLogicContext ctx = new SvcLogicContext();

        mockAai.getVnfcInfo(inParams, ctx);

        printContext(ctx);

        assertEquals(ctx.getAttribute("tmp.vnfInfo.vnfc.vnfc-type"), null);
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vnfc.vnfc-function-code"), null);
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vnfc.group-notation"), "grpnot1");

    }

    @Test
    public void testGetFirstVnfcNameForVnfcType() throws Exception {

        MockAaiService mockAai = new MockAaiService(aaiClient);

        String prefix = "tmp.vnfInfo.";

        SvcLogicContext ctx = new SvcLogicContext(); // VNFC with specified vnfc
                                                        // type found
        mockAai.populateFirstVnfcData(ctx, prefix);
        String firstVnfcName = mockAai.getFirstVnfcNameForVnfcType(ctx, prefix, "ssc");
        assertEquals(firstVnfcName, "vnfcname3");

        ctx = new SvcLogicContext(); // no VMS found
        firstVnfcName = mockAai.getFirstVnfcNameForVnfcType(ctx, prefix, "ssc");
        assertEquals(firstVnfcName, null);

        ctx = new SvcLogicContext(); // no VMS found with specified type
        mockAai.populateFirstVnfcData(ctx, prefix);
        firstVnfcName = mockAai.getFirstVnfcNameForVnfcType(ctx, prefix, "test");
        assertEquals(firstVnfcName, null);

    }

    @Test
    public void testGroupNotation() throws Exception {

        MockAaiService mockAai = new MockAaiService(aaiClient);

        String prefix = "tmp.vnfInfo.";

        SvcLogicContext ctx = new SvcLogicContext();
        mockAai.populateGroupNotation(ctx, prefix);

        // printContext(ctx);
        String grpNotation = mockAai.getGroupNotationForVServer(ctx, prefix, "ibcxvm0002");
        assertEquals(grpNotation, "grpNot2");

        ctx = new SvcLogicContext(); // no VMS found
        grpNotation = mockAai.getGroupNotationForVServer(ctx, prefix, "ibcxvm0002");
        assertEquals(grpNotation, null);

        ctx = new SvcLogicContext(); // no VMS found with specified name
        mockAai.populateGroupNotation(ctx, prefix);
        grpNotation = mockAai.getGroupNotationForVServer(ctx, prefix, "test");
        assertEquals(grpNotation, null);

    }

    @Test
    public void testGetGroupNotation() throws Exception {

        MockAaiService mockAai = new MockAaiService(aaiClient);

        String prefix = "tmp.vnfInfo.";

        SvcLogicContext ctx = new SvcLogicContext();

        String grpNotation = mockAai.getGroupNotation("fixed-value", "2", null, null, null, null, null,null, 0);
        assertEquals(grpNotation, "2");

        mockAai.populateFirstVnfcData(ctx, prefix); // Existing VNFC Found
        grpNotation = mockAai.getGroupNotation("first-vnfc-name", "2", "currentVnfcName", "currentVServerName", prefix,
                ctx, "ssc", null, 0);
        assertEquals(grpNotation, "vnfcname32");

        ctx = new SvcLogicContext(); // no vnfcs exist in A&AI- Use current
                                        // vnfcName
        grpNotation = mockAai.getGroupNotation("first-vnfc-name", "2", "currentVnfcName", "currentVServerName", prefix,
                ctx, "ssc", null, 0);
        assertEquals(grpNotation, "currentVnfcName2");

        ctx = new SvcLogicContext();
        mockAai.populateGroupNotation(ctx, prefix);
        grpNotation = mockAai.getGroupNotation("relative-value", "same", "currentVnfcName", "ibcxvm0003", prefix, ctx,
                "ssc", null, 0);
        assertEquals(grpNotation, "grpNot2");

        ctx = new SvcLogicContext();
        mockAai.populateGroupNotation(ctx, prefix);
        grpNotation = mockAai.getGroupNotation("relative-value", "next", "currentVnfcName", "ibcxvm0006", prefix, ctx,
                "ssc",null,0);
        assertEquals(grpNotation, "5");

        ctx = new SvcLogicContext();
        mockAai.populateGroupNotation(ctx, prefix);
        grpNotation = mockAai.getGroupNotation("relative-value", "next", "currentVnfcName", "ibcxvm0003", prefix, ctx,
                "ssc",null,0);
        assertEquals(grpNotation, null); // Null if grpNotation is not numeric

    }

@Test
    public void testInsertVnfcs() throws Exception {

        MockAaiService mockAai = new MockAaiService(aaiClient);

        Map<String, String> inParams = new HashMap<String, String>();

        inParams.put("responsePrefix", "tmp.vnfInfo");

        SvcLogicContext ctx = new SvcLogicContext();

        mockAai.populateVnfcRef(ctx);

        mockAai.populateAllVnfInfo(ctx, "tmp.vnfInfo");

        // mockAai.insertVnfcs(inParams,ctx,2, 2) ;
    }

    @Test
    public void testUpdateVServerStatus() throws Exception {

        MockAaiService mockAai = new MockAaiService(aaiClient);

        Map<String, String> inParams = new HashMap<String, String>();

        inParams.put("responsePrefix", "tmp.vnfInfo");

        SvcLogicContext ctx = new SvcLogicContext();

        mockAai.populateAllVnfInfo(ctx, "tmp.vnfInfo");

        mockAai.updateVServerStatus(inParams, ctx, 2);
    }

    @Test
    public void testInsertVnfcsForFirstVnfc() throws Exception {

        MockAaiService mockAai = new MockAaiService(aaiClient);

        Map<String, String> inParams = new HashMap<String, String>();

        inParams.put("responsePrefix", "tmp.vnfInfo");

        SvcLogicContext ctx = new SvcLogicContext();

        mockAai.populateVnfcRefFirstVnfcName(ctx);

        mockAai.populateAllVnfInfo1(ctx, "tmp.vnfInfo");

        mockAai.insertVnfcs(inParams, ctx, 2, 2,"vfModuleId1");


    }
    @Test
    public void testInsertVnfcsForRelativeValueSame() throws Exception {

        MockAaiService mockAai = new MockAaiService(aaiClient);

        Map<String, String> inParams = new HashMap<String, String>();

        inParams.put("responsePrefix", "tmp.vnfInfo");

        SvcLogicContext ctx = new SvcLogicContext();

        mockAai.populateVnfcRefRelValueSame(ctx);

        mockAai.populateAllVnfInfo1(ctx, "tmp.vnfInfo");

        mockAai.insertVnfcs(inParams, ctx, 2, 2, null);

        assertEquals(ctx.getAttribute("tmp.vnfInfo.vm[0].vnfc-name"), "dbjx0001vm001dbj001");
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vm[0].vnfc-function-code"), "dbj");
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vm[0].vnfc-type"), "v-I? - DBJX");
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vm[0].group-notation"), "1");
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vm[1].vnfc-name"), "dbjx0001vm002dbj001");
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vm[1].vnfc-function-code"), "dbj");
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vm[1].vnfc-type"), "v-I? - DBJX");
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vm[1].group-notation"), "1");
    }

    @Test
    public void testInsertVnfcsForRelativeValueNext() throws Exception {

        MockAaiService mockAai = new MockAaiService(aaiClient);

        Map<String, String> inParams = new HashMap<String, String>();

        inParams.put("responsePrefix", "tmp.vnfInfo");

        SvcLogicContext ctx = new SvcLogicContext();

        mockAai.populateVnfcRefRelValueNext(ctx);

        mockAai.populateAllVnfInfo1(ctx, "tmp.vnfInfo");

        mockAai.insertVnfcs(inParams, ctx, 2, 2,null);



        assertEquals(ctx.getAttribute("tmp.vnfInfo.vm[0].vnfc-name"), "dbjx0001vm001dbj001");
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vm[0].vnfc-function-code"), "dbj");
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vm[0].vnfc-type"), "v-I? - DBJX");
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vm[0].group-notation"), "1");
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vm[1].vnfc-name"), "dbjx0001vm002dbj001");
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vm[1].vnfc-function-code"), "dbj");
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vm[1].vnfc-type"), "v-I? - DBJX");
        assertEquals(ctx.getAttribute("tmp.vnfInfo.vm[1].group-notation"), "2");
    }

    @Test
    public void testUpdateVnfStatus() throws Exception {

        MockAaiService mockAai = new MockAaiService(aaiClient);

        Map<String, String> inParams = new HashMap<String, String>();

        inParams.put("responsePrefix", "tmp.vnfInfo");

        SvcLogicContext ctx = new SvcLogicContext();

        mockAai.populateAllVnfInfo(ctx, "tmp.vnfInfo");

        mockAai.updateVnfStatus(inParams, ctx);
    }

    @Test
    public void testReadResource() throws Exception {

        MockAaiService mockAai = new MockAaiService(aaiClient);
        // AaiService mockAai = new AaiService(new AAIClientMock());

        String vnfId = "ibcx0001v";
        String resourceKey = "generic-vnf.vnf-id = '" + vnfId + "'";
        String resourceType = "generic-vnf";
        String queryPrefix = "vnfInfo";
        SvcLogicContext ctx = mockAai.readResource(resourceKey, queryPrefix, resourceType);

        // System.out.println("VNF TYPE " + queryPrefix + ".vnf.vnf-type");

        assertEquals(ctx.getAttribute("vnfInfo.vnf-type"), "vUSP-Metaswitch");

    }
    private void printContext(SvcLogicContext ctx) throws Exception {
        for (String key : ctx.getAttributeKeySet()) {
            log.info(" KEY " + key);
            log.info(" VALUE " + ctx.getAttribute(key));
        }
    }

    @Test
    public void testGetGroupNotationForExistingValue() throws Exception {

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("req-vf-module-id",  "vfmodule01");
        ctx.setAttribute("tmp.vnfInfo.vm[0].vf-module-id", "vfmodule01");
        ctx.setAttribute("tmp.vnfInfo.vm[0].vnfc-function-code", "fc1");
        ctx.setAttribute("tmp.vnfInfo.vm[0].group-notation", "gn1");
        ctx.setAttribute("tmp.vnfInfo.vm[1].vf-module-id", "vfmodule01");
        ctx.setAttribute("tmp.vnfInfo.vm[1].vnfc-function-code", "fc1");
        ctx.setAttribute("tmp.vnfInfo.vm[0].vnfc-function-code", "fc1");
        ctx.setAttribute("tmp.vnfInfo.vm[0].group-notation", "gn2");
        MockAaiService aai=new MockAaiService(aaiClient);
        String groupNotationValue1 = aai.getGroupNotationForExistigValue(ctx, "tmp.vnfInfo", "fc1", 2);
        assertEquals (groupNotationValue1,null);

        ctx.setAttribute("tmp.vnfInfo.vm[0].group-notation", "gn1");
        ctx.setAttribute("tmp.vnfInfo.vm[1].group-notation", "gn1");
        String groupNotationValue2 = aai.getGroupNotationForExistigValue(ctx, "tmp.vnfInfo", "fc1", 2);
        assertEquals (groupNotationValue2,"gn1");

        ctx.setAttribute("tmp.vnfInfo.vm[2].vf-module-id", "vfmodule01");
        ctx.setAttribute("tmp.vnfInfo.vm[2].vnfc-function-code", "fc1");
        ctx.setAttribute("tmp.vnfInfo.vm[2].group-notation", "gn2");
        String groupNotationValue3 = aai.getGroupNotationForExistigValue(ctx, "tmp.vnfInfo", "fc1", 3);
        assertEquals (groupNotationValue3,null);

        ctx.setAttribute("tmp.vnfInfo.vm[2].group-notation", "gn1");
        String groupNotationValue4 = aai.getGroupNotationForExistigValue(ctx, "tmp.vnfInfo", "fc1", 3);
        assertEquals (groupNotationValue4,"gn1");

    }

    @Test
    public void testgetIdentityUrl() throws Exception {

        MockAaiService mockAai = new MockAaiService(aaiClient);

        Map<String, String> inParams = new HashMap<String, String>();
        inParams.put("responsePrefix", "tmp.vnfInfo");
        inParams.put("cloudOwner", "testCloudOwner");
        inParams.put("cloudRegionId", "testCloudRegionId");

        SvcLogicContext ctx = new SvcLogicContext();
        mockAai.getIdentityUrl(inParams, ctx);
        assertEquals(ctx.getAttribute("tmp.vnfInfo.cloud-region.identity-url"), "TestUrl");

    }

}
