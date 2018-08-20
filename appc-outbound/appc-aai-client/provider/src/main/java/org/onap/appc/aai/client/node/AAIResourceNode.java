/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
 * Copyright (C) 2018 IBM.
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

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.StringUtils;
import org.onap.appc.aai.client.AppcAaiClientConstant;
import org.onap.appc.aai.client.aai.AaiService;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;

public class AAIResourceNode implements SvcLogicJavaPlugin {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(AAIResourceNode.class);

    private static final String STR_VSERVER_ID = "].vserver-id";
    private static final String STR_TENANT_ID = "].tenant-id";
    private static final String STR_CLOUD_OWNER = "].cloud-owner";
    private static final String STR_CLOUD_REGION_ID = "].cloud-region-id";
    private static final String STR_VSERVER_SELFLINK ="].vserver-selflink";
    private static final String STR_TMP_VNF_INFO = "tmp.vnfInfo.vm[";

    private static final String PARAM_VSERVER_ID = "vserverId";
    private static final String PARAM_TENANT_ID = "tenantId";
    private static final String PARAM_CLOUD_OWNER = "cloudOwner";
    private static final String PARAM_CLOUD_REGION_ID = "cloudRegionId";
    private static final String PARAM_VSERVER_NAME = "vserver-name";
    private static final String PARAM_VSERVER_SELFLINK = "vserver-selflink";
    private static final String PARAM_VNFC_NAME = "vnfcName";

    private static final String ATTR_VNF_VM_COUNT = "vnf.vm-count";

    public AaiService getAaiService() {
        return new AaiService();
    }

    /* Gets VNF Info and All VServers associated with Vnf */
    public void getVnfInfo(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {

        log.info("Received getVnfInfo call with params : " + inParams);

        String responsePrefix = inParams.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX);

        try {

            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix + ".") : "";
            AaiService aai = getAaiService();
            aai.getGenericVnfInfo(inParams, ctx);

            ctx.setAttribute(responsePrefix + AppcAaiClientConstant.OUTPUT_PARAM_STATUS,
                AppcAaiClientConstant.OUTPUT_STATUS_SUCCESS);
            log.info("getVnfInfo Successful ");
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + AppcAaiClientConstant.OUTPUT_PARAM_STATUS,
                AppcAaiClientConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + AppcAaiClientConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
            log.error("Failed in getVnfInfo", e);

            throw new SvcLogicException(e.getMessage());
        }
    }


    public void getAllVServersVnfcsInfo(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {

        log.info("Received getAllVServersVnfcsInfo call with params : " + inParams);

        String responsePrefix = inParams.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX);

        try {
            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix + ".") : "";
            AaiService aai = getAaiService();

            ArrayList<Map<String, String>> vservers = new ArrayList<>();

            int vmWithNoVnfcsCount = 0;
            int vmsWithNoVnfcsForVfModule = 0;
            int vmCountForVfModule = 0;
            String vmCountStr = ctx.getAttribute(responsePrefix + "vm-count");
            String vfModuleFromRequest =  ctx.getAttribute("req-vf-module-id");
            log.info("getAllVServersVnfcsInfo()::: vfMOdule="+vfModuleFromRequest);

            if (vmCountStr == null) {
                throw new ResourceNodeInternalException("Unable to get VServers for the VNF");
            }

            int vmCount = Integer.parseInt(vmCountStr);
            for (int i = 0; i < vmCount; i++) {

                SvcLogicContext vmServerCtx = new SvcLogicContext();

                Map<String, String> paramsVm = new HashMap<>();
                paramsVm.put(PARAM_VSERVER_ID, ctx.getAttribute(responsePrefix + "vm[" + i + STR_VSERVER_ID));
                paramsVm.put(PARAM_TENANT_ID, ctx.getAttribute(responsePrefix + "vm[" + i + STR_TENANT_ID));
                paramsVm.put(PARAM_CLOUD_OWNER, ctx.getAttribute(responsePrefix + "vm[" + i + STR_CLOUD_OWNER));
                paramsVm.put(PARAM_CLOUD_REGION_ID, ctx.getAttribute(responsePrefix + "vm[" + i + STR_CLOUD_REGION_ID));
                paramsVm.put(PARAM_VSERVER_SELFLINK, ctx.getAttribute(responsePrefix + "vm[" + i + STR_VSERVER_SELFLINK));
                paramsVm.put(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX,
                    inParams.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX));

                aai.getVMInfo(paramsVm, vmServerCtx);

                HashMap<String, String> vserverMap = new HashMap<>();
                vserverMap.put("vserver-id", ctx.getAttribute(responsePrefix + "vm[" + i + STR_VSERVER_ID));
                vserverMap.put("tenant-id", ctx.getAttribute(responsePrefix + "vm[" + i + STR_TENANT_ID));
                vserverMap.put("cloud-owner", ctx.getAttribute(responsePrefix + "vm[" + i + STR_CLOUD_OWNER));
                vserverMap.put("cloud-region-id", ctx.getAttribute(responsePrefix + "vm[" + i + STR_CLOUD_REGION_ID));

                // Parameters returned by getVMInfo
                vserverMap.put(PARAM_VSERVER_NAME, vmServerCtx.getAttribute(responsePrefix + "vm.vserver-name"));
                vserverMap.put("vf-module-id", vmServerCtx.getAttribute(responsePrefix + "vm.vf-module-id"));
                vserverMap.put(PARAM_VSERVER_SELFLINK, vmServerCtx.getAttribute(responsePrefix + "vm.vserver-selflink"));
                
                log.info("VSERVER-LINK VALUE:" + vmServerCtx.getAttribute(responsePrefix + "vm.vserver-selflink"));

                // as Per 17.07 requirements we are supporting only one VNFC per VM.

                String vnfcName = vmServerCtx.getAttribute(responsePrefix + "vm.vnfc[0].vnfc-name");
                vserverMap.put("vnfc-name", vnfcName);

                String vnfcCount = vmServerCtx.getAttribute(responsePrefix + "vm.vnfc-count");
                if (vnfcCount == null) {
                    vnfcCount = "0";
                }

                vserverMap.put("vnfc-count", vnfcCount);
                String vfModuleForVserver = vmServerCtx.getAttribute(responsePrefix + "vm.vf-module-id");

                if (vnfcName != null) {
                    Map<String, String> paramsVnfc = new HashMap<String, String>();
                    paramsVnfc.put(PARAM_VNFC_NAME, vnfcName);

                    paramsVnfc.put(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX,
                        inParams.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX));

                    SvcLogicContext vnfcCtx = new SvcLogicContext();

                    aai.getVnfcInfo(paramsVnfc, vnfcCtx);

                    vserverMap.put("vnfc-type", vnfcCtx.getAttribute(responsePrefix + "vnfc.vnfc-type"));
                    vserverMap
                        .put("vnfc-function-code", vnfcCtx.getAttribute(responsePrefix + "vnfc.vnfc-function-code"));
                    vserverMap.put("group-notation", vnfcCtx.getAttribute(responsePrefix + "vnfc.group-notation"));
                    vserverMap.put("vnfc-ipaddress-v4-oam-vip",
                        vnfcCtx.getAttribute(responsePrefix + "vnfc.ipaddress-v4-oam-vip"));

                } else {
                    vmWithNoVnfcsCount++;
                    //ConfigScaleOut
                    log.info("getAllVServersVnfcsInfo()::Vf Modules: "+vfModuleForVserver+", "+vfModuleFromRequest);
                    if (StringUtils.isNotBlank(vfModuleFromRequest) && StringUtils.isNotBlank(vfModuleForVserver) && StringUtils.equalsIgnoreCase(vfModuleForVserver,vfModuleFromRequest)) {
                        vmsWithNoVnfcsForVfModule++;
                    }
                }

                if (StringUtils.isNotBlank(vfModuleFromRequest) && StringUtils.isNotBlank(vfModuleForVserver) && StringUtils.equalsIgnoreCase(vfModuleForVserver,vfModuleFromRequest)){
                    vmCountForVfModule++;
                }
                vservers.add(vserverMap);

            } // vmCount

            Collections.sort(vservers, Comparator.comparing(o -> o.get(PARAM_VSERVER_NAME)));

            log.info("SORTED VSERVERS " + vservers.toString());

            populateContext(vservers, ctx, responsePrefix);

            log.info("VMCOUNT IN GETALLVSERVERS " + vmCount);
            log.info("VMSWITHNOVNFCSCOUNT IN GETALLVSERVERS " + vmWithNoVnfcsCount);
            log.info("VMSWITHNOVNFCSCOUNTFOR VFMODULE IN GETALLVSERVERS " + vmsWithNoVnfcsForVfModule);
            log.info("VMCOUNT FOR VFMODULE IN GETALLVSERVERS " + vmCountForVfModule);
            ctx.setAttribute(responsePrefix + ATTR_VNF_VM_COUNT, String.valueOf(vmCount));
            ctx.setAttribute(responsePrefix + "vnf.vm-with-no-vnfcs-count", String.valueOf(vmWithNoVnfcsCount));
            ctx.setAttribute(responsePrefix + "vnf.vm-with-no-vnfcs-count-vf-module", String.valueOf(vmsWithNoVnfcsForVfModule));
            ctx.setAttribute(responsePrefix + "vnf.vm-count-for-vf-module", String.valueOf(vmCountForVfModule));


        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + AppcAaiClientConstant.OUTPUT_PARAM_STATUS,
                AppcAaiClientConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + AppcAaiClientConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
            log.error("Failed in getAllVServersVnfcsInfo", e);

            throw new SvcLogicException(e.getMessage());
        }
    }

    public void populateContext(List<Map<String, String>> vservers, SvcLogicContext ctx, String prefix) {

        log.info("Populating Final Context");
        int counter = 0;

        for (Map<String, String> input : vservers) {
            for (Entry<String, String> entry : input.entrySet()) {

                ctx.setAttribute(prefix + "vm[" + counter + "]." + entry.getKey(), entry.getValue());
                log.info("Populating Context Key = " + prefix + "vm[" + counter + "]." + entry.getKey() + " Value = " + entry.getValue());
            }
            counter++;
        }

        String firstVServerName = null;
        for (int i = 0; i < counter; i++) {
            String vnfcName = ctx.getAttribute(prefix + "vm[" + i + "].vnfc-name");
            log.info("VNFCNAME " + i + vnfcName);
            if (vnfcName == null && firstVServerName == null) {
                firstVServerName = ctx.getAttribute(prefix + "vm[" + i + "].vserver-name");
                ctx.setAttribute("vm-name", firstVServerName);
                log.info("Populating Context Key = " + "vm-name" + " Value = " + firstVServerName);
            }
        }
    }


    public void addVnfcs(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {

        log.info("Received addVnfcs call with params : " + inParams);

        String responsePrefix = inParams.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX);

        int vnfcRefLen;
        int vmCount;
        int vmWithNoVnfcCount = 0;

        try {

            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix + ".") : "";
            AaiService aai = getAaiService();

            //no:of vnfcs from the vnfc_reference table
            String vnfcRefLenStr = ctx.getAttribute("vnfcReference_length");

            vnfcRefLen = trySetVnfcRefLen(vnfcRefLenStr);

            //Vms without vnfc from A&AI
            String vmWithNoVnfcCountStr = ctx.getAttribute(responsePrefix + "vnf.vm-with-no-vnfcs-count");

            // Modified for 1710
            if (vmWithNoVnfcCountStr == null) {
                log.info("Parameter VM without VNFCs(vmWithNoVnfcCountStr) from A&AI is Null");
            } else {
                vmWithNoVnfcCount = Integer.parseInt(vmWithNoVnfcCountStr);
            }

            log.info("No of VM without VNFCs(vmWithNoVnfcCount) from A&AI is " + vmWithNoVnfcCount);

            String vmCountStr = ctx.getAttribute(responsePrefix + ATTR_VNF_VM_COUNT);

            if (vmCountStr == null) {
                throw new ResourceNodeInternalException("VM data from A&AI is missing");
            } else {
                vmCount = Integer.parseInt(vmCountStr);
            }
            String vfModuleIdFromRequest = ctx.getAttribute("req-vf-module-id");
            if ((vmCount < vnfcRefLen) &&  StringUtils.isBlank(vfModuleIdFromRequest)) {
                throw new ResourceNodeInternalException("Vnfc and VM count mismatch");
            }

            //ConfigScaleOut
            if (StringUtils.isNotBlank(vfModuleIdFromRequest)) {
                processCheckForVfModule(vfModuleIdFromRequest, ctx, responsePrefix,vnfcRefLen);
            }

            log.info("VMCOUNT " + vmCount);
            log.info("VNFCREFLEN " + vnfcRefLen);


            if (StringUtils.isBlank(ctx.getAttribute("vnfc-type"))) {
                aai.updateVnfStatusWithOAMAddress(inParams, ctx);
            }

            aai.insertVnfcs(inParams, ctx, vnfcRefLen, vmCount,vfModuleIdFromRequest);

            ctx.setAttribute(responsePrefix + AppcAaiClientConstant.OUTPUT_PARAM_STATUS,
                AppcAaiClientConstant.OUTPUT_STATUS_SUCCESS);

            log.info("addVnfcs Successful ");
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + AppcAaiClientConstant.OUTPUT_PARAM_STATUS,
                AppcAaiClientConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + AppcAaiClientConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
            log.error("Failed in addVnfcs", e);

            throw new SvcLogicException(e.getMessage());
        }
    }

    public void processCheckForVfModule(String vfModuleIdFromRequest, SvcLogicContext ctx,
    String responsePrefix, int vnfcRefLen) throws ResourceNodeInternalException {

        log.info("processCheckForVfModule()::vfModuleId From Request"+vfModuleIdFromRequest+"-"+vnfcRefLen);
        int vmsWithoutVnfcsForVfModule = 0;
        String vmsWithoutVnfcsForVfModuleStr = ctx.getAttribute(responsePrefix + "vnf.vm-with-no-vnfcs-count-vf-module");
        if (StringUtils.isBlank(vmsWithoutVnfcsForVfModuleStr) && StringUtils.isNotBlank(vfModuleIdFromRequest)) {
            log.info("addVnfcs()::No vmsWithoutVnfcsForVfModule (is null) for vfmodule="+vfModuleIdFromRequest);
        }
        else {
            vmsWithoutVnfcsForVfModule = Integer.parseInt(vmsWithoutVnfcsForVfModuleStr);
        }
        log.info("addVnfcs():::Number of VMs without vnfcs for vfmodule: "+vmsWithoutVnfcsForVfModule);
        String vmsForVfModuleStr = ctx.getAttribute(responsePrefix +"vnf.vm-count-for-vf-module");
        int vmsForVfModule = 0;
        if (StringUtils.isNotBlank(vmsForVfModuleStr)) {
            vmsForVfModule = Integer.parseInt(vmsForVfModuleStr);
        }
        if ((vmsForVfModule != vnfcRefLen ) &&  StringUtils.isNotBlank(vfModuleIdFromRequest)) {
            throw new ResourceNodeInternalException("Vnfc and VM count mismatch for vfModule in request="+vfModuleIdFromRequest);
        }
        log.info("processCheckForVfModule()::vmsForVfModule " + vmsForVfModule);

    }

    private int trySetVnfcRefLen(String vnfcRefLenStr) throws ResourceNodeInternalException {

        if (vnfcRefLenStr == null) {
            log.info("Vnfc Reference data is missing");
            throw new ResourceNodeInternalException("Vnfc Reference data is missing");

        } else {
            return Integer.parseInt(vnfcRefLenStr);
        }
    }


    public void updateVnfAndVServerStatus(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {

        log.info("Received updateVnfAndVServerStatus call with params : " + inParams);

        String responsePrefix = inParams.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX);

        int vmCount;

        try {

            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix + ".") : "";
            AaiService aai = getAaiService();

            String vmCountStr = ctx.getAttribute(responsePrefix + ATTR_VNF_VM_COUNT);

            if (vmCountStr == null) {
                throw new ResourceNodeInternalException("VM data from A&AI is missing");
            } else {
                vmCount = Integer.parseInt(vmCountStr);
            }

            log.info("VMCOUNT " + vmCount);

            aai.updateVnfStatus(inParams, ctx);
            aai.updateVServerStatus(inParams, ctx, vmCount);

            ctx.setAttribute(responsePrefix + AppcAaiClientConstant.OUTPUT_PARAM_STATUS,
                AppcAaiClientConstant.OUTPUT_STATUS_SUCCESS);

            log.info("updateVnfAndVServerStatus Successful ");
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + AppcAaiClientConstant.OUTPUT_PARAM_STATUS,
                AppcAaiClientConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + AppcAaiClientConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
            log.error("Failed in updateVnfAndVServerStatus", e);

            throw new SvcLogicException(e.getMessage());
        }
    }

    public void getVserverInfo(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {
        log.info("getVserverInfo()::Retrieving vm and vnfc information for vserver:" + inParams.toString());
        String responsePrefix = inParams.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX);
        try {
            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix + ".") : "";
            AaiService aaiService = getAaiService();
            String vServerId = inParams.get(PARAM_VSERVER_ID);
            Map<String, String> params = setVmParams(ctx, vServerId);
            Map<String, String> vnfcParams = new HashMap<>();
            if (null == params) {
                log.error("getVserverInfo()::No Vm Info found!!");
                throw new SvcLogicException("No Vm Info in Context");
            }
            params.put(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX,
                inParams.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX));
            SvcLogicContext newVmCtx = new SvcLogicContext();
            aaiService.getVMInfo(params, newVmCtx);

            String vnfcName = newVmCtx.getAttribute(responsePrefix + "vm.vnfc[0].vnfc-name");
            log.info("getVnfcFunctionCodeForVserver()::vnfcName=" + vnfcName);
            SvcLogicContext newVnfcCtx = new SvcLogicContext();
            if (StringUtils.isNotBlank(vnfcName)) {
                vnfcParams.put(PARAM_VNFC_NAME, vnfcName);
            } else {
                log.info("getVserverInfo()::vnfc Name is blank, not setting vnfc info !!!!");
                return;
            }
            getVnfcInformationForVserver(vnfcParams, newVnfcCtx, inParams, ctx, aaiService, responsePrefix);
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + AppcAaiClientConstant.OUTPUT_PARAM_STATUS,
                AppcAaiClientConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + AppcAaiClientConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
            log.error("Failed in getVserverInfo", e);
        }
    }

    public void getVnfcInformationForVserver(Map<String, String> vnfcParams, SvcLogicContext newVnfcCtx,
        Map<String, String> inParams, SvcLogicContext ctx, AaiService aaiService, String responsePrefix)
        throws Exception {
        log.info("getVnfcInformationForVserver()::vnfcParams:" + vnfcParams.toString());
        vnfcParams.put(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX,
            inParams.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX));

        aaiService.getVnfcInfo(vnfcParams, newVnfcCtx);

        String vnfcType = newVnfcCtx.getAttribute(responsePrefix + "vnfc.vnfc-type");
        String vnfcFunctionCode = newVnfcCtx.getAttribute(responsePrefix + "vnfc.vnfc-function-code");
        String vnfcGroupNotation = newVnfcCtx.getAttribute(responsePrefix + "vnfc.group-notation");
        String vnfcV4OamIp = newVnfcCtx.getAttribute(responsePrefix + "vnfc.ipaddress-v4-oam-vip");

        if (StringUtils.isBlank(vnfcType) || StringUtils.isBlank(vnfcFunctionCode)
            || StringUtils.isBlank(vnfcGroupNotation) || StringUtils.isBlank(vnfcV4OamIp)) {
            log.info("getVnfcInformationForVserver()::Some vnfc parameters are blank!!!!");
        }
        log.info("getVnfcInformationForVserver()::vnfcType=" + vnfcType + ",vnfcFunctionCode=" + vnfcFunctionCode,
            ", vnfc-ipaddress-v4-oam-vip=" + vnfcV4OamIp);
        ctx.setAttribute(responsePrefix + "vm.vnfc.vnfc-name", vnfcParams.get(PARAM_VNFC_NAME));
        ctx.setAttribute(responsePrefix + "vm.vnfc.vnfc-type", vnfcType);
        ctx.setAttribute(responsePrefix + "vm.vnfc.vnfc-function-code", vnfcFunctionCode);
        ctx.setAttribute(responsePrefix + "vm.vnfc.vnfc-group-notation", vnfcGroupNotation);
        ctx.setAttribute(responsePrefix + "vm.vnfc.vnfc-ipaddress-v4-oam-vip", vnfcV4OamIp);
    }

    public Map<String, String> setVmParams(SvcLogicContext ctx, String vServerId) {
        log.info("setVmParams()::setVmParamsVM level action:" + vServerId);
        Map<String, String> params = new HashMap<>();
        int vmCount = 0;
        int arrayIndex = -1;
        String vmCountStr = ctx.getAttribute("tmp.vnfInfo.vm-count");
        if (StringUtils.isNotBlank(vmCountStr)) {
            vmCount = Integer.parseInt(vmCountStr);
        }
        for (int cnt = 0; cnt < vmCount; cnt++) {
            String vsId = ctx.getAttribute(STR_TMP_VNF_INFO + cnt + STR_VSERVER_ID);
            log.info("setVmParams():::vserver details::" + cnt + ":" + vsId);
            if (StringUtils.equals(vServerId, vsId)) {
                arrayIndex = cnt;
            }
        }
        if (arrayIndex < 0) {
            log.info("setVmParams()::VserverId not found in context!! Returning null for params!!");
            return null;
        }
        String tenantId = ctx.getAttribute(STR_TMP_VNF_INFO + arrayIndex + STR_TENANT_ID);
        String cloudOwner = ctx.getAttribute(STR_TMP_VNF_INFO + arrayIndex + STR_CLOUD_OWNER);
        String cloudRegionId = ctx.getAttribute(STR_TMP_VNF_INFO + arrayIndex + STR_CLOUD_REGION_ID);
        String vserverSelflink = ctx.getAttribute(STR_TMP_VNF_INFO + arrayIndex + STR_VSERVER_SELFLINK);
        log.info("setVmParams()::tenantId=" + tenantId + " cloudOwner=" + cloudOwner + " cloudRegiodId= "
            + cloudRegionId);
        params.put(PARAM_VSERVER_ID, vServerId);
        params.put(PARAM_TENANT_ID, tenantId);
        params.put(PARAM_CLOUD_OWNER, cloudOwner);
        params.put(PARAM_CLOUD_REGION_ID, cloudRegionId);
        params.put(PARAM_VSERVER_SELFLINK, vserverSelflink);
        log.info("setVmParams()::setVmParamsVM level action:" + params.toString());
        return params;
    }

    public void getVfModuleModelInfo(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {
        log.info("vfModuleInfo()::Retrieving vf-module information :" + inParams.toString());
        String responsePrefix = inParams.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX);
        try {
            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix + ".") : "";
            AaiService aaiService = getAaiService();
            processForVfModuleModelInfo(aaiService,inParams,ctx);
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + AppcAaiClientConstant.OUTPUT_PARAM_STATUS,
                AppcAaiClientConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + AppcAaiClientConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
            log.error("Failed in vfModuleInfo", e);
        }
    }

    public void processForVfModuleModelInfo(AaiService aaiService, Map<String, String> inParams, SvcLogicContext ctx) {
        log.info("processForVfModuleModelInfo()::Retrieving vf-module information :" + inParams);
        String responsePrefix = inParams.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX);
        try {
            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix + ".") : "";
            Map<String, String> params = new HashMap<>();
            params.put(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX,
                inParams.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX));
            params.put("vnfId", inParams.get("vnf-id"));
            params.put("vfModuleId", inParams.get("vf-module-id"));
            SvcLogicContext vfModuleCtx = new SvcLogicContext();
            aaiService.getVfModuleInfo(params, vfModuleCtx);

            String modelInvariantId = vfModuleCtx.getAttribute(responsePrefix + "vfModule.model-invariant-id");
            String modelVersionId = vfModuleCtx.getAttribute(responsePrefix + "vfModule.model-version-id");
            log.info("processForVfModuleModelInfo()::modelInvariantId=" + modelInvariantId+",modelVersionId="+modelVersionId);

            Map<String, String> modelParams = new HashMap<>();
            modelParams.put(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX,
                    inParams.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX));
            SvcLogicContext modelCtx = new SvcLogicContext();
            if (StringUtils.isNotBlank(modelInvariantId) && StringUtils.isNotBlank(modelVersionId)) {
                modelParams.put("model-invariant-id", modelInvariantId);
                modelParams.put("model-version-id", modelVersionId);

            } else {
                log.info("processForVfModuleModelInfo()::model-invariant-id or model-version-id is blank, not getting model info !!!!");
                return;
            }
            aaiService.getModelVersionInfo(modelParams,modelCtx);
            String modelName = modelCtx.getAttribute(responsePrefix+"vfModule.model-name");
            log.info("processForVfModuleModelInfo()::modelName for vfModule:::"+modelName);
            log.info("Setting context template-model-id as :::"+modelName);
            ctx.setAttribute("template-model-id", modelName);
            log.info("processForVfModuleModelInfo() ::: End");
            }
            catch (Exception e) {
                ctx.setAttribute(responsePrefix + AppcAaiClientConstant.OUTPUT_PARAM_STATUS,
                    AppcAaiClientConstant.OUTPUT_STATUS_FAILURE);
                ctx.setAttribute(responsePrefix + AppcAaiClientConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
                log.error("Failed in vfModuleInfo", e);
            }

    }

    public void getFormattedValue(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {
        log.info("getFormattedValue()::Formatting values :" + inParams.toString());
        String responsePrefix = inParams.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX);
        try {
            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix + ".") : "";
            String inputValue = inParams.get("inputParameter");
            if (StringUtils.isBlank(inputValue)) {
                return;
            }
            String outputValue = StringUtils.replace(inputValue, "/", "_");//change / to _
            outputValue = StringUtils.replace(outputValue," ","");//remove space
            ctx.setAttribute("template-model-id", outputValue);
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + AppcAaiClientConstant.OUTPUT_PARAM_STATUS,
                AppcAaiClientConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + AppcAaiClientConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
            log.error("Failed in getFormattedValue", e);
        }
    }
}
