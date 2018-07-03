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

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.onap.appc.aai.client.AppcAaiClientConstant;
import org.onap.ccsdk.sli.adaptors.aai.AAIClient;
import org.onap.ccsdk.sli.adaptors.aai.AAIService;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

public class AaiService {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(AaiService.class);
    private static final String STR_VNF_ID = "generic-vnf.vnf-id = '";
    private static final String STR_VNFC_REF = "vnfcReference[";
    private static final String STR_VNFC_REF_KEY = "VNFCREFKEY ";
    private static final String STR_AAI_REF_KEY = "AAIREFKEY ";
    private static final String STR_RELATIONSHIP_LIST = ".relationship-list.relationship[";
    private static final String STR_VNFC_NAME = "vnfc.vnfc-name = '";
    private static final String QUERY_STR_VNFC_NAME = "VNFCNAME IN INSERTVNFCS ";

    private static final String PARAM_GENERIC_VNF = "generic-vnf";
    private static final String PARAM_VNF_INFO = "vnfInfo";
    private static final String PARAM_VSERVER = "vserver";
    private static final String PARAM_VM_INFO = "vmInfo";
    private static final String PARAM_PROV_STATUS = "prov-status";
    private static final String PARAM_VAL_NVTPROV = "NVTPROV";

    private static final String ATTR_VSERVER_ID = "vserver-id";
    private static final String ATTR_TENANT_ID = "tenant-id";
    private static final String ATTR_CLOUD_OWNER = "cloud-owner";
    private static final String ATTR_CLOUD_REGION_ID = "cloud-region-id";
    private static final String ATTR_VNFC_COUNT = "vm.vnfc-count";
    private static final String ATTR_VNFC_NAME = "vnfc-name";
    private static final String ATTR_VNFC_FUNC_CODE = "VNFC-FUNCTION-CODE";
    private static final String ATTR_VSERVER_NAME = "vserver-name";
    private static final String ATTR_VNF_ID = "vnf-id";

    private AAIClient aaiClient;

    public AaiService(AAIClient aaiClient) {
        this.aaiClient = aaiClient;
    }

    public AaiService() {
        BundleContext bctx = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        ServiceReference sref = bctx.getServiceReference(AAIService.class);
        aaiClient = (AAIClient) bctx.getService(sref);
    }

    public void getGenericVnfInfo(Map<String, String> params, SvcLogicContext ctx)
        throws AaiServiceInternalException, SvcLogicException {

        String vnfId = params.get("vnfId");
        if (StringUtils.isBlank(vnfId)) {
            throw new AaiServiceInternalException("VnfId is missing");
        }
        String prefix = params.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX);
        prefix = StringUtils.isNotBlank(prefix) ? (prefix + ".") : "";
        String resourceKey = STR_VNF_ID + vnfId + "'";
        SvcLogicContext vnfCtx = readResource(resourceKey, PARAM_VNF_INFO, PARAM_GENERIC_VNF);

        ctx.setAttribute(prefix + "vnf.vnf-name", vnfCtx.getAttribute("vnfInfo.vnf-name"));
        ctx.setAttribute(prefix + "vnf.vnf-type", vnfCtx.getAttribute("vnfInfo.vnf-type"));
        ctx.setAttribute(prefix + "vnf.prov-status", vnfCtx.getAttribute("vnfInfo.prov-status"));
        ctx.setAttribute(prefix + "vnf.orchestration-status", vnfCtx.getAttribute("vnfInfo.orchestration-status"));
        ctx.setAttribute(prefix + "vnf.ipv4-oam-address", vnfCtx.getAttribute("vnfInfo.ipv4-oam-address"));

        int vmCount = 0;
        String relLen = vnfCtx.getAttribute("vnfInfo.relationship-list.relationship_length");
        int relationshipLength = 0;
        if (relLen != null) {
            relationshipLength = Integer.parseInt(relLen);
        }
        log.info("RELLEN " + relationshipLength);
        for (int i = 0; i < relationshipLength; i++) {
            String vserverId = getRelationshipValue(i, vnfCtx, PARAM_VSERVER, "vserver.vserver-id", PARAM_VNF_INFO);
            String tenantId = getRelationshipValue(i, vnfCtx, PARAM_VSERVER, "tenant.tenant-id", PARAM_VNF_INFO);
            String cloudOwner = getRelationshipValue(i, vnfCtx, PARAM_VSERVER, "cloud-region.cloud-owner",
                PARAM_VNF_INFO);
            String cloudRegionId =
                getRelationshipValue(i, vnfCtx, PARAM_VSERVER, "cloud-region.cloud-region-id", PARAM_VNF_INFO);
            if (vserverId != null) {
                log.info("VSERVER KEYS " + vserverId + " " + tenantId + " " + cloudOwner + " " + cloudRegionId);
                String vnfPrefix = prefix + "vm[" + vmCount + "].";
                ctx.setAttribute(vnfPrefix + ATTR_VSERVER_ID, vserverId);
                ctx.setAttribute(vnfPrefix + ATTR_TENANT_ID, tenantId);
                ctx.setAttribute(vnfPrefix + ATTR_CLOUD_OWNER, cloudOwner);
                ctx.setAttribute(vnfPrefix + ATTR_CLOUD_REGION_ID, cloudRegionId);
                vmCount++;
            }
        }
        ctx.setAttribute(prefix + "vm-count", String.valueOf(vmCount));
        log.info("VMCOUNT FROM VNF INFO " + ctx.getAttribute(prefix + "vm-count"));
    }

    public void getVMInfo(Map<String, String> params, SvcLogicContext ctx)
        throws SvcLogicException {

        try {
            log.info("Received getVmInfo call with params : " + params);
            String prefix = params.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX);
            prefix = StringUtils.isNotBlank(prefix) ? (prefix + ".") : "";
            int vnfcCount = 0;
            ctx.setAttribute(prefix + ATTR_VNFC_COUNT, String.valueOf(vnfcCount)); // In case no vnfcs are found

            VServerInfo vServerInfo = new VServerInfo(params);

            String resourceKey =
                "vserver.vserver-id = '" + vServerInfo.getVserverId() + "' AND tenant.tenant-id = '" + vServerInfo
                    .getTenantId()
                    + "' AND cloud-region.cloud-owner = '" + vServerInfo.getCloudOwner()
                    + "' AND cloud-region.cloud-region-id = '"
                    + vServerInfo.getCloudRegionId() + "'";

            SvcLogicContext vmCtx = readResource(resourceKey, PARAM_VM_INFO, PARAM_VSERVER);
            ctx.setAttribute(prefix + "vm.prov-status", vmCtx.getAttribute("vmInfo.prov-status"));
            ctx.setAttribute(prefix + "vm.vserver-name", vmCtx.getAttribute("vmInfo.vserver-name"));
            ctx.setAttribute(prefix + "vm.vserver-selflink", vmCtx.getAttribute("vmInfo.vserver-selflink"));

            String relLen = vmCtx.getAttribute("vmInfo.relationship-list.relationship_length");
            int relationshipLength = 0;
            if (relLen != null) {
                relationshipLength = Integer.parseInt(relLen);
            }
            log.info("RELLEN" + relationshipLength);
            for (int i = 0; i < relationshipLength; i++) {
                String vfModuleId = getRelationshipValue(i, vmCtx, "vf-module", "vf-module.vf-module-id",
                    PARAM_VM_INFO);
                if (vfModuleId != null) {
                    ctx.setAttribute(prefix + "vm.vf-module-id", vfModuleId);
                }

                String vnfcName = getRelationshipValue(i, vmCtx, "vnfc", "vnfc.vnfc-name", PARAM_VM_INFO);
                if (vnfcName != null) {
                    ctx.setAttribute(prefix + "vm.vnfc[" + vnfcCount + "].vnfc-name", vnfcName);
                    vnfcCount++;
                }

            } // relationshipLength
            ctx.setAttribute(prefix + ATTR_VNFC_COUNT, String.valueOf(vnfcCount));
            log.info("VSERVERNAME " + ctx.getAttribute(prefix + "vm.vserver-name") + " HAS NUM VNFCS = "
                + ctx.getAttribute(prefix + ATTR_VNFC_COUNT));
        } catch (Exception e) {
            log.error("An error occurred when fetching Vm info", e);
            throw new SvcLogicException("Failed to fetch VM info", e);
        }
    }

    private String getRelationshipValue(int i, SvcLogicContext ctx, String relatedTo, String relationshipKey,
        String prefix) {

        if (relatedTo.equals(ctx.getAttribute(prefix + STR_RELATIONSHIP_LIST + i + "].related-to"))) {
            log.info("RELATEDTO " + relatedTo);
            int relationshipDataLength = 0;
            String relDataLen =
                ctx.getAttribute(prefix + STR_RELATIONSHIP_LIST + i + "].relationship-data_length");
            if (relDataLen != null) {
                relationshipDataLength = Integer.parseInt(relDataLen);
            }

            for (int j = 0; j < relationshipDataLength; j++) {

                String key = ctx.getAttribute(prefix + STR_RELATIONSHIP_LIST + i + "].relationship-data["
                    + j + "].relationship-key");

                String value = ctx.getAttribute(prefix + STR_RELATIONSHIP_LIST + i + "].relationship-data["
                    + j + "].relationship-value");

                log.info("GENERIC KEY " + key);
                log.info("GENERIC VALUE " + value);

                if (relationshipKey.equals(key)) {
                    return value;
                }

            } // relationshipDataLength
        } // if related-To

        return null;
    }

    public void getVnfcInfo(Map<String, String> params, SvcLogicContext ctx)
        throws AaiServiceInternalException, SvcLogicException {
        log.info("Received getVnfc call with params : " + params);

        String prefix = params.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX);
        prefix = StringUtils.isNotBlank(prefix) ? (prefix + ".") : "";

        String vnfcName = params.get("vnfcName");
        if (StringUtils.isBlank(vnfcName)) {
            throw new AaiServiceInternalException("Vnfc Name is missing");
        }

        String resourceKey = STR_VNFC_NAME + vnfcName + "'";
        SvcLogicContext vnfcCtx = readResource(resourceKey, "vnfcInfo", "vnfc");

        // Changes for US 315820 for 1710 vnfc-type renamed to nfc-function,vnfc-function-code renamed to
        // nfc-naming-code

        ctx.setAttribute(prefix + "vnfc.vnfc-type", vnfcCtx.getAttribute("vnfcInfo.nfc-function"));
        ctx.setAttribute(prefix + "vnfc.vnfc-function-code", vnfcCtx.getAttribute("vnfcInfo.nfc-naming-code"));
        ctx.setAttribute(prefix + "vnfc.group-notation", vnfcCtx.getAttribute("vnfcInfo.group-notation"));
        ctx.setAttribute(prefix + "vnfc.ipaddress-v4-oam-vip", vnfcCtx.getAttribute("vnfcInfo.ipaddress-v4-oam-vip"));

    }

    public void insertVnfcs(Map<String, String> params, SvcLogicContext ctx, int vnfcRefLen, int vmCount,
        String vfModuleIdFromRequest)
        throws AaiServiceInternalException, SvcLogicException {
        log.info("Received insertVnfcs call with params : " + params);

        String prefix = params.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX);

        prefix = StringUtils.isNotBlank(prefix) ? (prefix + ".") : "";
        int vnfcRefIndx = 0;
        log.debug("vnfcRefIndx" + vnfcRefIndx);
        for (int i = 0; i < vmCount; i++) {
            String aaiRefKey = prefix + "vm[" + i + "].";

            //ConfigScaleOut - Do not process vms that are not associated with vfmodule id if vfmodule id is present
            if (StringUtils.isNotBlank(vfModuleIdFromRequest)) {
                String vmVfModuleId = ctx.getAttribute(aaiRefKey + "vf-module-id");
                log.info("insertVnfcs():::vfModule for vm is=" + vmVfModuleId);
                if (StringUtils.isBlank(vmVfModuleId) || !StringUtils
                    .equalsIgnoreCase(vmVfModuleId, vfModuleIdFromRequest)) {
                    continue;
                }
            }

            log.info(QUERY_STR_VNFC_NAME + ctx.getAttribute(aaiRefKey + ATTR_VNFC_NAME));
            String vnfcNameAAI = ctx.getAttribute(aaiRefKey + ATTR_VNFC_NAME);

            // Get Vnfc_reference data from the table
            String vnfcRefKey = STR_VNFC_REF + vnfcRefIndx + "].";

            log.info(STR_VNFC_REF_KEY + vnfcRefKey);
            log.info(STR_AAI_REF_KEY + aaiRefKey);

            String groupNotationType = ctx.getAttribute(vnfcRefKey + "GROUP-NOTATION-TYPE");
            String groupNotationValue = ctx.getAttribute(vnfcRefKey + "GROUP-NOTATION-VALUE");
            String vnfcType = ctx.getAttribute(vnfcRefKey + "VNFC-TYPE");
            String vnfcFuncCode = ctx.getAttribute(vnfcRefKey + ATTR_VNFC_FUNC_CODE);
            String populateIpAddressV4OamVip = ctx.getAttribute(vnfcRefKey + "IPADDRESS-V4-OAM-VIP");

            // Get vnfc Data to be added
            String vserverName = ctx.getAttribute(aaiRefKey + ATTR_VSERVER_NAME);
            String vnfcName = vserverName + vnfcFuncCode + "001";
            String groupNotation = getGroupNotation(groupNotationType, groupNotationValue, vnfcName, vserverName,
                prefix, ctx, vnfcType, vnfcFuncCode, vmCount);

            String ipAddressV4OamVip = null;
            if ("Y".equals(populateIpAddressV4OamVip)) {
                ipAddressV4OamVip = ctx.getAttribute("vnf-host-ip-address"); // from input
            }
            Map<String, String> vnfcParams =
                populateVnfcParams(ctx, aaiRefKey, ipAddressV4OamVip, groupNotation, vnfcType, vnfcFuncCode);

            log.info("Vnfc name from AAI: " + vnfcNameAAI);
            log.info("Vnfc name generated: " + vnfcName);

            if (StringUtils.isNotBlank(vnfcNameAAI)) {
                if (vnfcName.equalsIgnoreCase(vnfcNameAAI)) {
                    updateVnfcStatus(vnfcNameAAI, params, prefix);
                    vnfcRefIndx++;
                }
                continue;
            }
            vnfcRefIndx++;
            addVnfc(vnfcName, vnfcParams, prefix);

            // Add VNFC Info to context for current added VNFC
            ctx.setAttribute(aaiRefKey + ATTR_VNFC_NAME, vnfcName);
            ctx.setAttribute(aaiRefKey + "vnfc-type", vnfcType);
            ctx.setAttribute(aaiRefKey + "vnfc-function-code", vnfcFuncCode);
            ctx.setAttribute(aaiRefKey + "group-notation", groupNotation);
        }
    }

    public List<String> getVnfcData(Map<String, String> params, SvcLogicContext ctx, int vnfcRefLen, int vmCount) {

        String prefix = params.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX);
        prefix = StringUtils.isNotBlank(prefix) ? (prefix + ".") : "";
        List<String> vnfcNames = new ArrayList<>();
        int vnfcRefIndx = -1;
        for (int i = 0; i < vmCount; i++) {
            String aaiRefKey = prefix + "vm[" + i + "].";
            log.info(QUERY_STR_VNFC_NAME + ctx.getAttribute(aaiRefKey + ATTR_VNFC_NAME));
            if (ctx.getAttribute(aaiRefKey + ATTR_VNFC_NAME) != null) {
                continue;
            } else {
                vnfcRefIndx++;
            }
            String vnfcRefKey = STR_VNFC_REF + vnfcRefIndx + "].";
            log.info(STR_VNFC_REF_KEY + vnfcRefKey);
            log.info(STR_AAI_REF_KEY + aaiRefKey);
            String vnfcFuncCode = ctx.getAttribute(vnfcRefKey + ATTR_VNFC_FUNC_CODE);

            // Get vnfc Data to be added
            String vserverName = ctx.getAttribute(aaiRefKey + ATTR_VSERVER_NAME);
            String vnfcName = vserverName + vnfcFuncCode + "001";

            vnfcNames.add(vnfcName);
        }

        return vnfcNames;
    }

    private Map<String, String> populateVnfcParams(SvcLogicContext ctx, String aaiRefKey, String ipAddressV4OamVip,
        String groupNotation, String vnfcType, String vnfcFuncCode) {

        Map<String, String> vnfcParams = new HashMap<>();

        // Changes for vnfc-type renamed to nfc-function,vnfc-function-code renamed to
        // nfc-naming-code
        vnfcParams.put("nfc-naming-code", vnfcFuncCode);
        vnfcParams.put("nfc-function", vnfcType);
        vnfcParams.put("ipaddress-v4-oam-vip", ipAddressV4OamVip);
        vnfcParams.put(PARAM_PROV_STATUS, PARAM_VAL_NVTPROV);
        vnfcParams.put("orchestration-status", "CONFIGURED");
        vnfcParams.put("in-maint", "false");
        vnfcParams.put("is-closed-loop", "false");
        vnfcParams.put("group-notation", groupNotation);
        vnfcParams.put("relationship-list.relationship[0].related-to", PARAM_VSERVER);
        vnfcParams.put("relationship-list.relationship[0].relationship-data[0].relationship-key", "vserver.vserver-id");
        vnfcParams.put("relationship-list.relationship[0].relationship-data[0].relationship-value",
            ctx.getAttribute(aaiRefKey + ATTR_VSERVER_ID));
        vnfcParams.put("relationship-list.relationship[0].relationship-data[1].relationship-key", "tenant.tenant-id");
        vnfcParams.put("relationship-list.relationship[0].relationship-data[1].relationship-value",
            ctx.getAttribute(aaiRefKey + ATTR_TENANT_ID));
        vnfcParams.put("relationship-list.relationship[0].relationship-data[2].relationship-key",
            "cloud-region.cloud-owner");
        vnfcParams.put("relationship-list.relationship[0].relationship-data[2].relationship-value",
            ctx.getAttribute(aaiRefKey + ATTR_CLOUD_OWNER));
        vnfcParams.put("relationship-list.relationship[0].relationship-data[3].relationship-key",
            "cloud-region.cloud-region-id");
        vnfcParams.put("relationship-list.relationship[0].relationship-data[3].relationship-value",
            ctx.getAttribute(aaiRefKey + ATTR_CLOUD_REGION_ID));
        vnfcParams.put("relationship-list.relationship[1].related-to", PARAM_GENERIC_VNF);
        vnfcParams.put("relationship-list.relationship[1].relationship-data[0].relationship-key", "generic-vnf.vnf-id");
        vnfcParams.put("relationship-list.relationship[1].relationship-data[0].relationship-value",
            ctx.getAttribute(ATTR_VNF_ID));
        vnfcParams.put("relationship-list.relationship[2].related-to", "vf-module");
        vnfcParams.put("relationship-list.relationship[2].relationship-data[0].relationship-key", "generic-vnf.vnf-id");
        vnfcParams.put("relationship-list.relationship[2].relationship-data[0].relationship-value",
            ctx.getAttribute(ATTR_VNF_ID));
        vnfcParams.put("relationship-list.relationship[2].relationship-data[1].relationship-key",
            "vf-module.vf-module-id");
        vnfcParams.put("relationship-list.relationship[2].relationship-data[1].relationship-value",
            ctx.getAttribute(aaiRefKey + "vf-module-id"));

        return vnfcParams;
    }

    public void addVnfc(String vnfcName, Map<String, String> params, String prefix)
        throws AaiServiceInternalException, SvcLogicException {

        log.info("Received addVnfc call with vnfcName : " + vnfcName);
        log.info("Received addVnfc call with params : " + params);
        String resourceKey = STR_VNFC_NAME + vnfcName + "'";
        log.info("Received addVnfc call with resourceKey : " + resourceKey);

        SvcLogicContext vnfcCtx = new SvcLogicContext();
        SvcLogicResource.QueryStatus response =
            aaiClient.save("vnfc", true, false, resourceKey, params, prefix, vnfcCtx);

        if (SvcLogicResource.QueryStatus.SUCCESS.equals(response)) {
            log.info("Added VNFC SUCCESSFULLY " + vnfcName);
        } else if (SvcLogicResource.QueryStatus.FAILURE.equals(response)) {
            throw new AaiServiceInternalException("VNFC Add failed for vnfc_name " + vnfcName);
        }
    }

    public String getGroupNotation(String groupNotationType, String groupNotationValue, String vnfcName,
        String vserverName, String prefix, SvcLogicContext ctx, String vnfcRefVnfcType, String vnfcFuncCode,
        int vmCount) {

        String groupNotation = null;

        if ("fixed-value".equals(groupNotationType)) {
            groupNotation = groupNotationValue;
        } else if ("first-vnfc-name".equals(groupNotationType)) {

            /*
             * If the group-notation-type value = ?first-vnfc-name?,
             * then populate the group-notation value with the concatenation of
             * [vnfc name associated with the first vnfc for the vnfc-type (e.g., *******)]
             * and [the value in group-notation-value (e.g., pair)].
             * There may be several vnfc-types associated with the VM?s.
             */
            /* Vnfc-type should be from refrence data */

            /* vDBE has 2 VNFCs with same VNFC type . The pair name should be same for both . */
            /*
             * When first VNFC is added details should be added to context so FirstVnfcName doesnt return null second
             * time.
             */
            String tmpVnfcName = getFirstVnfcNameForVnfcType(ctx, prefix, vnfcRefVnfcType);

            log.info("RETURNED FIRSTVNFCNAME" + tmpVnfcName);
            log.info("CURRENTVNFCNAME" + vnfcName);
            groupNotation = resolveGroupNotation(groupNotationValue, vnfcName, tmpVnfcName);
        } else if ("relative-value".equals(groupNotationType)) {

            /*
             * If the group-notation-type = ?relative-value?, then find the group-notation value
             * from the prior vnfc (where prior means the vnfc with where the last three digits of the
             * vm-name is one lower than the current one; note that this vnfc may have been previously configured.)
             * 1. If the group-notation-value = next, then add 1 to the group-notation value from the prior vnfc and use
             * this value
             * 2. If the group-notation-value = same, then use the group-notation-value from the prior vnfc record
             */

            // next and same cant be defined for first VM. if next will not generate grpNotation if Prior is not a
            // number
            String tmpVserverName;
            if (vserverName != null) {

                String vmNamePrefix = vserverName.substring(0, vserverName.length() - 3);
                String lastThreeChars = vserverName.substring(vserverName.length() - 3);

                if (NumberUtils.isDigits(lastThreeChars)) {
                    int vmNum = Integer.parseInt(lastThreeChars) - 1;
                    String formatted = String.format("%03d", vmNum);

                    log.info("FORMATTED " + formatted);

                    tmpVserverName = vmNamePrefix + formatted;

                    String priorGroupNotation = getGroupNotationForVServer(ctx, prefix, tmpVserverName);
                    groupNotation = resolveGroupNotation(groupNotationValue, priorGroupNotation);
                }
            }
        } else if ("existing-value".equals(groupNotationType)) {
         /* This is a new value being added.  Find the existing vnfc records in A&AI inventory with the same vnfc-function code as the value in vnfc_reference table.
          * Verify that the group-notation value is the same for all such records found in inventory.
          * if all records do not have the same group-notation value, write the new vnfc record to A&AI inventory without a group-notation value and continue to the next VM in the vnfc_reference table.  A 501 intermediate error message should be sent after all new VNFC records have been added to A&AI.
          * If all records match, use the same group-notation value for the new vnfc record as found in the existing vnfc records.
          */
            groupNotation = getGroupNotationForExistigValue(ctx, prefix, vnfcFuncCode, vmCount);
        }

        log.info("RETURNED GROUPNOTATION " + groupNotation);
        return groupNotation;
    }

    private String resolveGroupNotation(String groupNotationValue, String vnfcName, String tmpVnfcName) {
        if (tmpVnfcName == null) {
            log.info("CURRENTVNFCNAME" + vnfcName);
            // No Vnfcs currently exist. Use Current vnfcName
            return vnfcName + groupNotationValue;
        } else {
            return tmpVnfcName + groupNotationValue;
        }
    }

    private String resolveGroupNotation(String groupNotationValue, String priorGroupNotation) {
        if ("same".equals(groupNotationValue)) {
            return priorGroupNotation;
        } else if ("next".equals(groupNotationValue) && priorGroupNotation != null
            && NumberUtils.isDigits(priorGroupNotation)) {

            int nextGrpNotation = Integer.parseInt(priorGroupNotation) + 1;
            return String.valueOf(nextGrpNotation);
        }
        return null;
    }

    public String getGroupNotationForExistigValue(SvcLogicContext ctx, String prefix, String vnfcFuncCode,
        int vmCount) {
        String vfModuleId = ctx.getAttribute("req-vf-module-id"); //Coming from request-params
        boolean first = true;
        String aaiGroupNotationValue = null;
        for (int i = 0; i < vmCount; i++) {
            String ind = "tmp.vnfInfo.vm[" + i + "].";
            String aaiFuncCode = ctx.getAttribute(ind + "vnfc-function-code");
            String aaiGroupNotation = ctx.getAttribute(ind + "group-notation");
            String aaiVfModuleId = ctx.getAttribute(ind + "vf-module-id");

            log.info("getGroupNotationForExistigValue()::: vfModuleId=" + vfModuleId + ", aaiFuncCode=" + aaiFuncCode
                + ", aaiGroupNotation=" + aaiGroupNotation + ",aaiVfMOduleId=" + aaiVfModuleId);

            if (StringUtils.isNotBlank(aaiFuncCode) && aaiFuncCode.equals(vnfcFuncCode) &&
                (StringUtils.isNotBlank(vfModuleId) && StringUtils.isNotBlank(aaiVfModuleId) && aaiVfModuleId
                    .equals(vfModuleId))) {
                if (null == aaiGroupNotationValue && first) {
                    if (null == aaiGroupNotation) {//Return if null
                        return null;
                    }
                    aaiGroupNotationValue = ctx.getAttribute(ind + "group-notation");
                    first = false;
                } else {
                    if (!StringUtils.equals(aaiGroupNotationValue, ctx.getAttribute(ind + "group-notation"))) {
                        log.info("Values are different, returning null");
                        return null;
                    }
                }
            }
        }

        return aaiGroupNotationValue;
    }

    public String getGroupNotationForVServer(SvcLogicContext ctx, String prefix, String vserverName) {

        String vmCountStr = ctx.getAttribute(prefix + "vnf.vm-count");

        if (vmCountStr == null) {
            return null;
        }

        int vmCount = Integer.parseInt(vmCountStr);
        for (int i = 0; i < vmCount; i++) {

            String tmpVserver = ctx.getAttribute(prefix + "vm[" + i + "].vserver-name");

            if (vserverName.equals(tmpVserver)) {
                return ctx.getAttribute(prefix + "vm[" + i + "].group-notation");
            }
        } // vmCount
        return null;
    }

    public String getFirstVnfcNameForVnfcType(SvcLogicContext ctx, String prefix, String vnfcRefVnfcType) {

        String vmCountStr = ctx.getAttribute(prefix + "vnf.vm-count");
        if (vmCountStr == null) {
            return null;
        }
        int vmCount = Integer.parseInt(vmCountStr);
        for (int i = 0; i < vmCount; i++) {

            String tmpvnfcType = ctx.getAttribute(prefix + "vm[" + i + "].vnfc-type");

            if (vnfcRefVnfcType.equals(tmpvnfcType)) {
                return ctx.getAttribute(prefix + "vm[" + i + "].vnfc-name");
            }
        } // vmCount
        return null;
    }

    public void updateVServerStatus(Map<String, String> params, SvcLogicContext ctx, int vmCount)
        throws AaiServiceInternalException, SvcLogicException {
        log.info("Received updateVServerStatus call with params : " + params);

        String prefix = params.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX);

        prefix = StringUtils.isNotBlank(prefix) ? (prefix + ".") : "";
        Map<String, String> vServerParams = new HashMap<>();

        // TODO - Should this just update prov-status or both? What about generic-vnf status? Will that be updated by
        // Dispatcher?

        vServerParams.put(PARAM_PROV_STATUS, PARAM_VAL_NVTPROV);

        for (int i = 0; i < vmCount; i++) {
            String aaiRefKey = prefix + "vm[" + i + "].";

            log.info("VNFCNAME IN UpdateVServer " + ctx.getAttribute(aaiRefKey + ATTR_VNFC_NAME));

            if (ctx.getAttribute(aaiRefKey + ATTR_VNFC_NAME) != null) {
                continue;
            }

            String resourceKey = "vserver.vserver-id = '" + ctx.getAttribute(aaiRefKey + ATTR_VSERVER_ID) + "'"
                + " AND tenant.tenant-id = '" + ctx.getAttribute(aaiRefKey + ATTR_TENANT_ID) + "'"
                + " AND cloud-region.cloud-owner = '" + ctx.getAttribute(aaiRefKey + ATTR_CLOUD_OWNER) + "'"
                + " AND cloud-region.cloud-region-id = '" + ctx.getAttribute(aaiRefKey + ATTR_CLOUD_REGION_ID) + "'";

            updateResource(PARAM_VSERVER, resourceKey, vServerParams);
        }
    }

    public void updateVnfStatus(Map<String, String> params, SvcLogicContext ctx)
        throws AaiServiceInternalException, SvcLogicException {
        log.info("Received updateVnfStatus call with params : " + params);

        Map<String, String> vnfParams = new HashMap<>();

        // TODO - Should this just update prov-status or both? What about generic-vnf status? Will that be updated by
        // Dispatcher?

        vnfParams.put(PARAM_PROV_STATUS, PARAM_VAL_NVTPROV);

        String resourceKey = STR_VNF_ID + ctx.getAttribute(ATTR_VNF_ID) + "'";

        updateResource(PARAM_GENERIC_VNF, resourceKey, vnfParams);
    }

    public void updateResource(String resource, String resourceKey, Map<String, String> params)
        throws AaiServiceInternalException, SvcLogicException {

        log.info("Received updateResource call with Key : " + resourceKey);

        SvcLogicContext ctx = new SvcLogicContext();
        SvcLogicResource.QueryStatus response = aaiClient.update(resource, resourceKey, params, "tmp.update", ctx);

        if (SvcLogicResource.QueryStatus.SUCCESS.equals(response)) {
            log.info("Updated " + resource + " SUCCESSFULLY for " + resourceKey);

        } else if (SvcLogicResource.QueryStatus.FAILURE.equals(response)) {
            throw new AaiServiceInternalException(resource + " Update failed for " + resourceKey);
        }
    }

    public SvcLogicContext readResource(String query, String prefix, String resourceType)
        throws AaiServiceInternalException, SvcLogicException {
        SvcLogicContext resourceContext = new SvcLogicContext();

        SvcLogicResource.QueryStatus response =
            aaiClient.query(resourceType, false, null, query, prefix, null, resourceContext);
        log.info("AAIResponse: " + response.toString());
        if (!SvcLogicResource.QueryStatus.SUCCESS.equals(response)) {
            throw new AaiServiceInternalException("Error Retrieving " + resourceType + " from A&AI");
        }
        return resourceContext;
    }

    // Added for Backward Compatibility
    public void checkAndUpdateVnfc(Map<String, String> params, SvcLogicContext ctx, int vnfcRefLen, int vmCount)
        throws AaiServiceInternalException, SvcLogicException {
        log.info("Received checkAndUpdateVnfcStatus call with params : " + params);

        String prefix = params.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX);

        prefix = StringUtils.isNotBlank(prefix) ? (prefix + ".") : "";

        for (int i = 0; i < vmCount; i++) {
            String aaiRefKey = prefix + "vm[" + i + "].";

            log.info(QUERY_STR_VNFC_NAME + aaiRefKey + "vnfc-name:" + ctx.getAttribute(aaiRefKey + ATTR_VNFC_NAME));

            String vnfcNameAai = ctx.getAttribute(aaiRefKey + ATTR_VNFC_NAME);

            if (StringUtils.isNotBlank(vnfcNameAai)) {
                // Get Vnfc_reference data
                for (int vnfcRefIndx = 0; vnfcRefIndx < vnfcRefLen; vnfcRefIndx++) {

                    String vnfcRefKey = STR_VNFC_REF + vnfcRefIndx + "].";

                    log.info(STR_VNFC_REF_KEY + vnfcRefKey);
                    log.info(STR_AAI_REF_KEY + aaiRefKey);

                    String vnfcFuncCode = ctx.getAttribute(vnfcRefKey + ATTR_VNFC_FUNC_CODE);
                    String vserverName = ctx.getAttribute(aaiRefKey + ATTR_VSERVER_NAME);
                    String vnfcNameReference = vserverName + vnfcFuncCode + "001";
                    tryUpdateVnfcStatus(params, prefix, vnfcNameAai, vnfcNameReference);
                }
            }
        }
    }

    private void tryUpdateVnfcStatus(Map<String, String> params, String prefix, String vnfcNameAai,
        String vnfcNameReference) throws AaiServiceInternalException, SvcLogicException {
        if (vnfcNameAai.equals(vnfcNameReference)) {
            updateVnfcStatus(vnfcNameAai, params, prefix);
        }
    }

    public void updateVnfcStatus(String vnfcName, Map<String, String> params, String prefix)
        throws AaiServiceInternalException, SvcLogicException {

        log.info("Received updateVnfcStatus call with vnfcName : " + vnfcName);
        log.info("Received updateVnfcStatus call with params : " + params);

        String resourceKey = STR_VNFC_NAME + vnfcName + "'";
        log.info("Received updateVnfcStatus call with resourceKey : " + resourceKey);

        Map<String, String> vnfcParams = new HashMap<>();
        vnfcParams.put(PARAM_PROV_STATUS, PARAM_VAL_NVTPROV);
        vnfcParams.put("orchestration-status", "CONFIGURED");

        log.info("In updateVnfcStatus call with vnfcParams : " + vnfcParams);

        updateResource("vnfc", resourceKey, vnfcParams);

        log.info("End of updateVnfcStatus");
    }

    public void updateVnfStatusWithOAMAddress(Map<String, String> params, SvcLogicContext ctx)
        throws AaiServiceInternalException, SvcLogicException {
        log.info("Received updateVnfStatusWithOAMAddress call with params : " + params);

        String ipAddress = ctx.getAttribute("vnf-host-ip-address");
        log.debug("Vnf-host-ip-address" + ipAddress);

        Map<String, String> vnfParams = new HashMap<>();
        vnfParams.put("ipv4-oam-address", ipAddress);
        String resourceKey = STR_VNF_ID + ctx.getAttribute(ATTR_VNF_ID) + "'";
        updateResource(PARAM_GENERIC_VNF, resourceKey, vnfParams);
    }

    public void getVfModuleInfo(Map<String, String> params, SvcLogicContext vfModuleCtx) throws Exception {
        log.info("Received getVfModuleInfo call with params : " + params);
        String prefix = params.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX);
        prefix = StringUtils.isNotBlank(prefix) ? (prefix + ".") : "";

        String vnfId = params.get("vnfId");
        String vfModuleId = params.get("vfModuleId");
        String resourceKey = "generic-vnf.vnf-id = '" + vnfId +
            "' AND vf-module.vf-module-id = '" + vfModuleId + "'";
        String queryPrefix = "vfModuleInfo";
        String resourceType = "vf-module";
        SvcLogicContext vfmCtx = readResource(resourceKey, queryPrefix, resourceType);
        String modelInvariantId = vfmCtx.getAttribute("vfModuleInfo.model-invariant-id");
        log.info("getVfModuleInfo():::modelInvariant=" + modelInvariantId);
        vfModuleCtx.setAttribute(prefix + "vfModule.model-invariant-id",
            vfmCtx.getAttribute("vfModuleInfo.model-invariant-id"));
        vfModuleCtx
            .setAttribute(prefix + "vfModule.model-version-id", vfmCtx.getAttribute("vfModuleInfo.model-version-id"));
        log.info("End - getVfModuleInfo");
    }

    public void getModelVersionInfo(Map<String, String> modelParams, SvcLogicContext modelCtx) throws Exception {
        log.info("Received getModelVersionInfo call with params : " + modelParams);
        String prefix = modelParams.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX);
        prefix = StringUtils.isNotBlank(prefix) ? (prefix + ".") : "";

        String modelInvariantId = modelParams.get("model-invariant-id");
        String modelVersionId = modelParams.get("model-version-id");
        String resourceKey = "model.model-invariant-id = '" + modelInvariantId +
            "' AND model-ver.model-version-id = '" + modelVersionId + "'";
        String queryPrefix = "modelInfo";
        String resourceType = "model-ver";
        SvcLogicContext vfmCtx = readResource(resourceKey, queryPrefix, resourceType);
        log.info("getModelVersionInfo():::modelname=" + vfmCtx.getAttribute("modelInfo.model-name"));
        modelCtx.setAttribute(prefix + "vfModule.model-name", vfmCtx.getAttribute("modelInfo.model-name"));
        log.info("End - getModelVersionInfo");

    }

    public void getIdentityUrl(Map<String, String> params, SvcLogicContext ctx) throws Exception{
        log.info("Recieved getIdentityUrl call with params : "+params);
        String prefix = params.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX);
        prefix = StringUtils.isNotBlank(prefix) ? (prefix + ".") : "";

        String cloudOwner = params.get("cloudOwner");
        String cloudRegionId = params.get("cloudRegionId");
        log.debug("cloudOwner" +cloudOwner +"," +"cloudRegionId"+ cloudRegionId);
        String resourceKey = "cloud-region.cloud-owner = '" + cloudOwner +
                "' AND cloud-region.cloud-region-id = '" + cloudRegionId + "'";
        String queryPrefix ="urlInfo";
        String resourceType = "cloud-region";
        SvcLogicContext urlCtx = readResource(resourceKey, queryPrefix, resourceType);
        log.info("IdentityUrl: "+urlCtx.getAttribute("urlInfo.identity-url"));
        ctx.setAttribute(prefix+"cloud-region.identity-url", urlCtx.getAttribute("urlInfo.identity-url"));
 
    }
}
