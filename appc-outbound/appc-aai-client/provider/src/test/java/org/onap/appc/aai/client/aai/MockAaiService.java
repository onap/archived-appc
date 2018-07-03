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
import static org.junit.Assert.fail;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import java.util.HashMap;
import java.util.Map;
import org.onap.ccsdk.sli.adaptors.aai.AAIClient;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

public class MockAaiService extends AaiService {

    //ONAP migration

    private static final EELFLogger log = EELFManager.getInstance().getLogger(MockAaiService.class);
    private AAIClient aaiClient;

    public MockAaiService(AAIClient aaic) {
        super(aaic);
    }

    public SvcLogicContext readResource(String query, String prefix, String resourceType)
        throws AaiServiceInternalException, SvcLogicException {
        log.info("In MockRead Resource");
        SvcLogicContext resourceContext = new SvcLogicContext();

        //prefix = StringUtils.isNotBlank(prefix) ? (prefix+".") : "";
        if ("generic-vnf".equals(resourceType)) {
            populateGenericVnfContext(resourceContext, prefix);
        } else if ("vserver".equals(resourceType)) {
            populateVmContext(resourceContext, prefix);
        } else if ("vnfc".equals(resourceType)) {
            populateVnfcContext(resourceContext, prefix);
        } else if ("cloud-region".equals(resourceType)) {
            resourceContext.setAttribute(prefix + ".identity-url", "TestUrl");
        }

        return resourceContext;


    }

    public void addVnfc(String vnfcName, Map<String, String> params, String prefix) {

        if (vnfcName.startsWith("ibcx")) {
            assertEquals("ibcxvm0002func0001", vnfcName);

            log.info("In AddVnfc " + vnfcName);
            Map<String, String> expectedParams = getExpectedParams();
                 
                 
                /*    for (Map.Entry<String, String> entry : params.entrySet()) {
                            String key = entry.getKey();
                            String value = entry.getValue();
        
        
                           log.info("key= " + key + "value = " + value );
                         
                    }*/
            assertEquals(params, expectedParams);
        }
    }


    public void updateResource(String resource, String resourceKey, Map<String, String> params) {

        Map<String, String> expectedParams = new HashMap<String, String>();

        expectedParams.put("prov-status", "NVTPROV");
        assertEquals(params, expectedParams);

        if ("vserver".equals(resource)) {

            String key = "vserver.vserver-id = 'ibcx00000'" +
                " AND tenant.tenant-id = 'tenantId'" +
                " AND cloud-region.cloud-owner = 'cloudOwner'" +
                " AND cloud-region.cloud-region-id = 'cloudRegionId'";

            assertEquals(key, resourceKey);
        } else if ("generic-vnf".equals(resource)) {
            assertEquals(resourceKey, "generic-vnf.vnf-id = 'ibcx000000'");
        } else {
            fail("Invalid resource " + resource);
        }


    }

    public Map<String, String> getExpectedParams() {
        Map<String, String> vnfcParams = new HashMap<String, String>();

        vnfcParams.put("vnfc-function-code", "func0");

        vnfcParams.put("vnfc-type", "ssc0");

        vnfcParams.put("ipaddress-v4-oam-vip", "000.00.00.00");

        vnfcParams.put("prov-status", "NVTPROV");
        vnfcParams.put("orchestration-status", "CONFIGURED");
        vnfcParams.put("in-maint", "false");
        vnfcParams.put("is-closed-loop", "false");
        vnfcParams.put("group-notation", "2");

        vnfcParams.put("relationship-list.relationship[0].related-to", "vserver");
        vnfcParams.put("relationship-list.relationship[0].relationship-data[0].relationship-key", "vserver.vserver-id");
        vnfcParams.put("relationship-list.relationship[0].relationship-data[0].relationship-value", "ibcx00000");

        vnfcParams.put("relationship-list.relationship[0].relationship-data[1].relationship-key", "tenant.tenant-id");
        vnfcParams.put("relationship-list.relationship[0].relationship-data[1].relationship-value", "tenantId");

        vnfcParams
            .put("relationship-list.relationship[0].relationship-data[2].relationship-key", "cloud-region.cloud-owner");
        vnfcParams.put("relationship-list.relationship[0].relationship-data[2].relationship-value", "cloudOwner");

        vnfcParams.put("relationship-list.relationship[0].relationship-data[3].relationship-key",
            "cloud-region.cloud-region-id");
        vnfcParams.put("relationship-list.relationship[0].relationship-data[3].relationship-value", "cloudRegionId");

        vnfcParams.put("relationship-list.relationship[1].related-to", "generic-vnf");
        vnfcParams.put("relationship-list.relationship[1].relationship-data[0].relationship-key", "generic-vnf.vnf-id");
        vnfcParams.put("relationship-list.relationship[1].relationship-data[0].relationship-value", "ibcx000000");

        vnfcParams.put("relationship-list.relationship[2].related-to", "vf-module");
        vnfcParams.put("relationship-list.relationship[2].relationship-data[0].relationship-key", "generic-vnf.vnf-id");
        vnfcParams.put("relationship-list.relationship[2].relationship-data[0].relationship-value", "ibcx000000");

        vnfcParams
            .put("relationship-list.relationship[2].relationship-data[1].relationship-key", "vf-module.vf-module-id");
        vnfcParams.put("relationship-list.relationship[2].relationship-data[1].relationship-value", "vfModuleId");

        return vnfcParams;
    }

    public void populateVnfcContext(SvcLogicContext ctx, String prefix) {
        log.info("In populateVnfcContext " + prefix);

        ctx.setAttribute(prefix + ".vnfc-type", "vnfctype1");
        ctx.setAttribute(prefix + ".vnfc-function-code", "funccode1");
        ctx.setAttribute(prefix + ".group-notation", "grpnot1");
    }

    public void populateVmContext(SvcLogicContext ctx, String prefix) {
        log.info("In populateVmContext " + prefix);

        ctx.setAttribute(prefix + ".vserver-name", "ibcx0000000");

        ctx.setAttribute(prefix + ".relationship-list.relationship_length", "3");

        // Junk
        ctx.setAttribute(prefix + ".relationship-list.relationship[0].related-to", "test");
        ctx.setAttribute(prefix + ".relationship-list.relationship[0].relationship-data_length", "1");
        ctx.setAttribute(prefix + ".relationship-list.relationship[0].relationship-data[0].relationship-key",
            "vnfc.vnfc-name");
        ctx.setAttribute(prefix + ".relationship-list.relationship[0].relationship-data[0].relationship-value", "test");

        // VNFC
        ctx.setAttribute(prefix + ".relationship-list.relationship[1].related-to", "vnfc");
        ctx.setAttribute(prefix + ".relationship-list.relationship[1].relationship-data_length", "1");
        ctx.setAttribute(prefix + ".relationship-list.relationship[1].relationship-data[0].relationship-key",
            "vnfc.vnfc-name");
        ctx.setAttribute(prefix + ".relationship-list.relationship[1].relationship-data[0].relationship-value",
            "ibcx0001vm001vnfc1");

        // VFModule

        ctx.setAttribute(prefix + ".relationship-list.relationship[2].related-to", "vf-module");
        ctx.setAttribute(prefix + ".relationship-list.relationship[2].relationship-data_length", "1");
        ctx.setAttribute(prefix + ".relationship-list.relationship[2].relationship-data[0].relationship-key",
            "vf-module.vf-module-id");
        ctx.setAttribute(prefix + ".relationship-list.relationship[2].relationship-data[0].relationship-value",
            "vfModule1");


    }

    public void populateGenericVnfContext(SvcLogicContext ctx, String prefix) {

        log.info("In populateGenericVnf " + prefix);
        ctx.setAttribute(prefix + ".vnf-name", "ibvcx0001");
        ctx.setAttribute(prefix + ".vnf-type", "vUSP-Metaswitch");

        ctx.setAttribute(prefix + ".relationship-list.relationship_length", "3");
             
            /* // VM1
             ctx.setAttribute(prefix + ".relationship-list.relationship[0].related-to", "vserver");
             ctx.setAttribute(prefix + ".relationship-list.relationship[0].relationship-data_length", "4");
             ctx.setAttribute(prefix + ".relationship-list.relationship[0].relationship-data[0].relationship-key", "vserver.vserver-id");
             ctx.setAttribute(prefix + ".relationship-list.relationship[0].relationship-data[0].relationship-value", "ibcx001vm001-id");
             ctx.setAttribute(prefix + ".relationship-list.relationship[0].relationship-data[1].relationship-key", "tenant.tenant-id");
             ctx.setAttribute(prefix + ".relationship-list.relationship[0].relationship-data[1].relationship-value", "sometenant");
             ctx.setAttribute(prefix + ".relationship-list.relationship[0].relationship-data[2].relationship-key", "cloud-region.cloud-owner");
             ctx.setAttribute(prefix + ".relationship-list.relationship[0].relationship-data[2].relationship-value", "ATTAIC");
             ctx.setAttribute(prefix + ".relationship-list.relationship[0].relationship-data[3].relationship-key", "cloud-region.cloud-region-id");
             ctx.setAttribute(prefix + ".relationship-list.relationship[0].relationship-data[3].relationship-value", "testcloudregionid");
           
             
             //ctx.setAttribute(prefix + ".relationship-list.relationship_length", "3");
*/
        // VM1
        ctx.setAttribute(prefix + ".relationship-list.relationship[0].related-to", "vserver");
        ctx.setAttribute(prefix + ".relationship-list.relationship[0].relationship-data_length", "4");
        ctx.setAttribute(prefix + ".relationship-list.relationship[0].relationship-data[0].relationship-key",
            "vserver.vserver-id");
        ctx.setAttribute(prefix + ".relationship-list.relationship[0].relationship-data[0].relationship-value",
            "ibcx001vm001-id");
        ctx.setAttribute(prefix + ".relationship-list.relationship[0].relationship-data[1].relationship-key",
            "tenant.tenant-id");
        ctx.setAttribute(prefix + ".relationship-list.relationship[0].relationship-data[1].relationship-value",
            "sometenant");
        ctx.setAttribute(prefix + ".relationship-list.relationship[0].relationship-data[2].relationship-key",
            "cloud-region.cloud-owner");
        ctx.setAttribute(prefix + ".relationship-list.relationship[0].relationship-data[2].relationship-value",
            "ATTAIC");
        ctx.setAttribute(prefix + ".relationship-list.relationship[0].relationship-data[3].relationship-key",
            "cloud-region.cloud-region-id");
        ctx.setAttribute(prefix + ".relationship-list.relationship[0].relationship-data[3].relationship-value",
            "testcloudregionid");

        // VM2
        ctx.setAttribute(prefix + ".relationship-list.relationship[1].related-to", "vserver");
        ctx.setAttribute(prefix + ".relationship-list.relationship[1].relationship-data_length", "4");
        ctx.setAttribute(prefix + ".relationship-list.relationship[1].relationship-data[0].relationship-key",
            "vserver.vserver-id");
        ctx.setAttribute(prefix + ".relationship-list.relationship[1].relationship-data[0].relationship-value",
            "ibcx000000");

        ctx.setAttribute(prefix + ".relationship-list.relationship[1].relationship-data[1].relationship-key",
            "tenant.tenant-id");
        ctx.setAttribute(prefix + ".relationship-list.relationship[1].relationship-data[1].relationship-value",
            "sometenant");

        ctx.setAttribute(prefix + ".relationship-list.relationship[1].relationship-data[2].relationship-key",
            "cloud-region.cloud-owner");
        ctx.setAttribute(prefix + ".relationship-list.relationship[0].relationship-data[2].relationship-value",
            "ATTAIC");

        ctx.setAttribute(prefix + ".relationship-list.relationship[1].relationship-data[3].relationship-key",
            "cloud-region.cloud-region-id");
        ctx.setAttribute(prefix + ".relationship-list.relationship[1].relationship-data[3].relationship-value",
            "testcloudregionid");

        // Unrelated
        ctx.setAttribute(prefix + ".relationship-list.relationship[2].related-to", "junk");

        ctx.setAttribute(prefix + ".relationship-list.relationship[2].relationship-data_length", "4");

        ctx.setAttribute(prefix + ".relationship-list.relationship[2].relationship-data[0].relationship-key", "test");
        ctx.setAttribute(prefix + ".relationship-list.relationship[2].relationship-data[0].relationship-value",
            "ibcx000000");

        ctx.setAttribute(prefix + ".relationship-list.relationship[2].relationship-data[1].relationship-key", "test1");
        ctx.setAttribute(prefix + ".relationship-list.relationship[2].relationship-data[1].relationship-value",
            "sometenant");

        ctx.setAttribute(prefix + ".relationship-list.relationship[2].relationship-data[2].relationship-key", "test2");
        ctx.setAttribute(prefix + ".relationship-list.relationship[2].relationship-data[2].relationship-value",
            "ATTAIC");

        ctx.setAttribute(prefix + ".relationship-list.relationship[2].relationship-data[3].relationship-key", "test3");
        ctx.setAttribute(prefix + ".relationship-list.relationship[2].relationship-data[3].relationship-value",
            "testcloudregionid");
    }


    public void populateFirstVnfcData(SvcLogicContext ctx, String prefix) throws Exception {

        ctx.setAttribute(prefix + "vnf.vm-count", "4");

        ctx.setAttribute(prefix + "vm[0].vnfc-type", "mmc");
        ctx.setAttribute(prefix + "vm[0].vnfc-name", "vnfcname1");

        ctx.setAttribute(prefix + "vm[1].vnfc-type", "mmc");
        ctx.setAttribute(prefix + "vm[1].vnfc-name", "vnfcname2");

        ctx.setAttribute(prefix + "vm[2].vnfc-type", "ssc");
        ctx.setAttribute(prefix + "vm[2].vnfc-name", "vnfcname3");

        ctx.setAttribute(prefix + "vm[3].vnfc-type", "ssc");
        ctx.setAttribute(prefix + "vm[3].vnfc-name", "vnfcname4");
    }


    public void populateGroupNotation(SvcLogicContext ctx, String prefix) throws Exception {

        ctx.setAttribute(prefix + "vnf.vm-count", "5");

        ctx.setAttribute(prefix + "vm[0].vserver-name", "ibcxvm0001");
        ctx.setAttribute(prefix + "vm[0].group-notation", "grpNot1");

        ctx.setAttribute(prefix + "vm[1].vserver-name", "ibcxvm0002");
        ctx.setAttribute(prefix + "vm[1].group-notation", "grpNot2");

        ctx.setAttribute(prefix + "vm[2].vserver-name", "ibcxvm0003");
        ctx.setAttribute(prefix + "vm[2].group-notation", "grpNot3");

        ctx.setAttribute(prefix + "vm[3].vserver-name", "ibcxvm0004");
        ctx.setAttribute(prefix + "vm[3].group-notation", "4");

        ctx.setAttribute(prefix + "vm[4].vserver-name", "ibcxvm0005");
        ctx.setAttribute(prefix + "vm[4].group-notation", "4");
    }


    public void populateVnfcRef(SvcLogicContext ctx) throws Exception {

        for (int i = 0; i < 2; i++) {

            String vnfcRefKey = "vnfcReference[" + i + "].";

            ctx.setAttribute(vnfcRefKey + "VM-INSTANCE", String.valueOf(i));
            ctx.setAttribute(vnfcRefKey + "VNFC-INSTANCE", "1");

            //if ( i == 0 || i == 1 ) {
            ctx.setAttribute(vnfcRefKey + "GROUP-NOTATION-TYPE", "fixed-value");
            ctx.setAttribute(vnfcRefKey + "GROUP-NOTATION-VALUE", "2");
            //}

            ctx.setAttribute(vnfcRefKey + "VNFC-TYPE", "ssc" + i);

            ctx.setAttribute(vnfcRefKey + "VNFC-FUNCTION-CODE", "func" + i);

            ctx.setAttribute(vnfcRefKey + "IPADDRESS-V4-OAM-VIP", "Y");
        }

    }

    public void populateAllVnfInfo(SvcLogicContext ctx, String prefix) throws Exception {

        ctx.setAttribute("vnf-id", "ibcx000000");

        ctx.setAttribute("vnf-host-ip-address", "000.00.00.00");
        ctx.setAttribute(prefix + ".vnf.vm-count", "2");

        ctx.setAttribute(prefix + ".vm[0].vserver-name", "ibcxvm0000");
        ctx.setAttribute(prefix + ".vm[0].vnfc-name", "VNFCNAME");

        ctx.setAttribute(prefix + ".vm[0].vserver-id", "ibcxvm0001id");
        ctx.setAttribute(prefix + ".vm[0].tenant-id", "tenantid");
        ctx.setAttribute(prefix + ".vm[0].cloud-owner", "cloudOwner");
        ctx.setAttribute(prefix + ".vm[0].cloud-region-id", "cloudRegionId");

        ctx.setAttribute(prefix + ".vm[0].vf-module-id", "vfModuleId");

        ctx.setAttribute(prefix + ".vm[1].vserver-name", "ibcxvm0002");

        ctx.setAttribute(prefix + ".vm[1].vserver-id", "ibcx00000");
        ctx.setAttribute(prefix + ".vm[1].tenant-id", "tenantId");
        ctx.setAttribute(prefix + ".vm[1].cloud-owner", "cloudOwner");
        ctx.setAttribute(prefix + ".vm[1].cloud-region-id", "cloudRegionId");

        ctx.setAttribute(prefix + ".vm[1].vf-module-id", "vfModuleId");

        //ctx.setAttribute(prefix+ ".vm[1].vserver-name", "ibcxvm0002");

    }


    public void populateAllVnfInfo1(SvcLogicContext ctx, String prefix) throws Exception {

        ctx.setAttribute("vnf-id", "dbjx0001v");

        ctx.setAttribute("vnf-host-ip-address", "000.00.00.00");
        ctx.setAttribute(prefix + ".vnf.vm-count", "2");

        ctx.setAttribute(prefix + ".vm[0].vserver-name", "dbjx0001vm001");

        ctx.setAttribute(prefix + ".vm[0].vserver-id", "dbjx0001vm0001id");
        ctx.setAttribute(prefix + ".vm[0].tenant-id", "tenantid1");
        ctx.setAttribute(prefix + ".vm[0].cloud-owner", "cloudOwner1");
        ctx.setAttribute(prefix + ".vm[0].cloud-region-id", "cloudRegionId1");

        ctx.setAttribute(prefix + ".vm[0].vf-module-id", "vfModuleId1");

        ctx.setAttribute(prefix + ".vm[1].vserver-name", "dbjx0001vm002");

        ctx.setAttribute(prefix + ".vm[1].vserver-id", "dbjx0001vm0002id");
        ctx.setAttribute(prefix + ".vm[1].tenant-id", "tenantId2");
        ctx.setAttribute(prefix + ".vm[1].cloud-owner", "cloudOwner2");
        ctx.setAttribute(prefix + ".vm[1].cloud-region-id", "cloudRegionId2");

        ctx.setAttribute(prefix + ".vm[1].vf-module-id", "vfModuleId2");


    }

    public void populateVnfcRefFirstVnfcName(SvcLogicContext ctx) throws Exception {

        for (int i = 0; i < 2; i++) {

            String vnfcRefKey = "vnfcReference[" + i + "].";

            ctx.setAttribute(vnfcRefKey + "VM-INSTANCE", String.valueOf(i));
            ctx.setAttribute(vnfcRefKey + "VNFC-INSTANCE", "1");

            ctx.setAttribute(vnfcRefKey + "GROUP-NOTATION-TYPE", "first-vnfc-name");
            ctx.setAttribute(vnfcRefKey + "GROUP-NOTATION-VALUE", "pair");

            ctx.setAttribute(vnfcRefKey + "VNFC-TYPE", "vDBE-I? - DBJX");

            ctx.setAttribute(vnfcRefKey + "VNFC-FUNCTION-CODE", "dbj");

            ctx.setAttribute(vnfcRefKey + "IPADDRESS-V4-OAM-VIP", "Y");
        }

    }

    public void populateVnfcRefRelValueSame(SvcLogicContext ctx) throws Exception {

        for (int i = 0; i < 2; i++) {

            String vnfcRefKey = "vnfcReference[" + i + "].";

            ctx.setAttribute(vnfcRefKey + "VM-INSTANCE", String.valueOf(i));
            ctx.setAttribute(vnfcRefKey + "VNFC-INSTANCE", "1");

            if (i == 0) {
                ctx.setAttribute(vnfcRefKey + "GROUP-NOTATION-TYPE", "fixed-value");
                ctx.setAttribute(vnfcRefKey + "GROUP-NOTATION-VALUE", "1");
            } else {
                ctx.setAttribute(vnfcRefKey + "GROUP-NOTATION-TYPE", "relative-value");
                ctx.setAttribute(vnfcRefKey + "GROUP-NOTATION-VALUE", "same");
            }

            ctx.setAttribute(vnfcRefKey + "VNFC-TYPE", "v-I? - DBJX");

            ctx.setAttribute(vnfcRefKey + "VNFC-FUNCTION-CODE", "dbj");

            ctx.setAttribute(vnfcRefKey + "IPADDRESS-V4-OAM-VIP", "Y");
        }

    }


    public void populateVnfcRefRelValueNext(SvcLogicContext ctx) throws Exception {

        for (int i = 0; i < 2; i++) {

            String vnfcRefKey = "vnfcReference[" + i + "].";

            ctx.setAttribute(vnfcRefKey + "VM-INSTANCE", String.valueOf(i));
            ctx.setAttribute(vnfcRefKey + "VNFC-INSTANCE", "1");

            if (i == 0) {
                ctx.setAttribute(vnfcRefKey + "GROUP-NOTATION-TYPE", "fixed-value");
                ctx.setAttribute(vnfcRefKey + "GROUP-NOTATION-VALUE", "1");
            } else {
                ctx.setAttribute(vnfcRefKey + "GROUP-NOTATION-TYPE", "relative-value");
                ctx.setAttribute(vnfcRefKey + "GROUP-NOTATION-VALUE", "next");
            }

            ctx.setAttribute(vnfcRefKey + "VNFC-TYPE", "v-I? - DBJX");

            ctx.setAttribute(vnfcRefKey + "VNFC-FUNCTION-CODE", "dbj");

            ctx.setAttribute(vnfcRefKey + "IPADDRESS-V4-OAM-VIP", "Y");
        }

    }

}
