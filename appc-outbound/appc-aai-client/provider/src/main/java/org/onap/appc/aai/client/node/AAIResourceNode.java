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

package org.onap.appc.aai.client.node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import org.onap.appc.aai.client.AppcAaiClientConstant;
import org.onap.appc.aai.client.aai.AaiService;


import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;


public class AAIResourceNode implements SvcLogicJavaPlugin {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(AAIResourceNode.class);


    public AaiService getAaiService() {
        return new AaiService();
    }
    /* Gets VNF Info and All VServers associated with Vnf */
    public void getVnfInfo(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {

        log.info("Received getVnfInfo call with params : " + inParams);

        String responsePrefix = inParams.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX);

        try {

            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix+".") : "";
            AaiService aai = getAaiService();
            aai.getGenericVnfInfo(inParams,ctx);

            ctx.setAttribute(responsePrefix + AppcAaiClientConstant.OUTPUT_PARAM_STATUS,
                    AppcAaiClientConstant.OUTPUT_STATUS_SUCCESS);
            log.info("getVnfInfo Successful ");
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + AppcAaiClientConstant.OUTPUT_PARAM_STATUS,
                    AppcAaiClientConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + AppcAaiClientConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
            log.error("Failed in getVnfInfo " + e.getMessage());

            throw new SvcLogicException(e.getMessage());
        }
    }


    public void getAllVServersVnfcsInfo(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {

        log.info("Received getAllVServersVnfcsInfo call with params : " + inParams);

        String responsePrefix = inParams.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX);

        try {
            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix+".") : "";
            AaiService aai = getAaiService();



            ArrayList<Map<String, String>> vservers = new ArrayList<Map<String, String>>();

            int vmWithNoVnfcsCount = 0;
            String  vmCountStr = ctx.getAttribute(responsePrefix + "vm-count");

            if ( vmCountStr == null )
                throw new Exception("Unable to get VServers for the VNF");

            int vmCount = Integer.parseInt(vmCountStr);
            for ( int i = 0; i < vmCount; i++ ) {

                SvcLogicContext vmServerCtx = new SvcLogicContext();

                Map<String, String> paramsVm = new HashMap<String, String>();
                paramsVm.put("vserverId", ctx.getAttribute(responsePrefix + "vm[" + i + "].vserver-id"));
                paramsVm.put("tenantId", ctx.getAttribute(responsePrefix +"vm[" + i + "].tenant-id"));
                paramsVm.put("cloudOwner", ctx.getAttribute(responsePrefix +"vm[" + i + "].cloud-owner"));
                paramsVm.put("cloudRegionId", ctx.getAttribute(responsePrefix +"vm[" + i + "].cloud-region-id"));
                paramsVm.put(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX, inParams.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX));



                aai.getVMInfo(paramsVm, vmServerCtx);

                HashMap<String, String> vserverMap = new HashMap<String, String>();
                vserverMap.put("vserver-id", ctx.getAttribute(responsePrefix + "vm[" + i + "].vserver-id"));
                vserverMap.put("tenant-id", ctx.getAttribute(responsePrefix +"vm[" + i + "].tenant-id"));
                vserverMap.put("cloud-owner", ctx.getAttribute(responsePrefix +"vm[" + i + "].cloud-owner"));
                vserverMap.put("cloud-region-id", ctx.getAttribute(responsePrefix +"vm[" + i + "].cloud-region-id"));

                // Parameters returned by getVMInfo
                vserverMap.put("vserver-name", vmServerCtx.getAttribute(responsePrefix + "vm.vserver-name"));
                vserverMap.put("vf-module-id", vmServerCtx.getAttribute(responsePrefix + "vm.vf-module-id"));


                // as Per 17.07 requirements we are supporting only one VNFC per VM.

                String vnfcName = vmServerCtx.getAttribute(responsePrefix + "vm.vnfc[0].vnfc-name");
                vserverMap.put("vnfc-name", vnfcName);


                String vnfcCount = vmServerCtx.getAttribute(responsePrefix + "vm.vnfc-count");
                if ( vnfcCount == null )
                    vnfcCount = "0";

                vserverMap.put("vnfc-count", vnfcCount);

                if ( vnfcName != null  ) {
                    Map<String, String> paramsVnfc = new HashMap<String, String>();
                    paramsVnfc.put("vnfcName", vnfcName);

                    paramsVnfc.put(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX, inParams.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX));

                    SvcLogicContext vnfcCtx = new SvcLogicContext();

                    aai.getVnfcInfo(paramsVnfc, vnfcCtx);

                    vserverMap.put("vnfc-type", vnfcCtx.getAttribute(responsePrefix + "vnfc.vnfc-type"));
                    vserverMap.put("vnfc-function-code", vnfcCtx.getAttribute(responsePrefix + "vnfc.vnfc-function-code"));
                    vserverMap.put("group-notation", vnfcCtx.getAttribute(responsePrefix + "vnfc.group-notation"));
                    vserverMap.put("vnfc-ipaddress-v4-oam-vip", vnfcCtx.getAttribute(responsePrefix + "vnfc.ipaddress-v4-oam-vip"));

                }
                else
                    vmWithNoVnfcsCount++;
                vservers.add(vserverMap);

            } // vmCount

            Collections.sort(vservers, new Comparator<Map<String, String>>() {
                @Override
                public int compare(Map<String, String> o1, Map<String, String> o2) {
                    return o1.get("vserver-name").compareTo(o2.get("vserver-name"));
                }
            });

            log.info("SORTED VSERVERS " + vservers.toString());

            populateContext(vservers, ctx, responsePrefix);

            log.info("VMCOUNT IN GETALLVSERVERS " + vmCount);
            log.info("VMSWITHNOVNFCSCOUNT IN GETALLVSERVERS " + vmWithNoVnfcsCount);
            ctx.setAttribute(responsePrefix+"vnf.vm-count", String.valueOf(vmCount));
            ctx.setAttribute(responsePrefix+"vnf.vm-with-no-vnfcs-count", String.valueOf(vmWithNoVnfcsCount));


        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + AppcAaiClientConstant.OUTPUT_PARAM_STATUS,
                    AppcAaiClientConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + AppcAaiClientConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
            log.error("Failed in getAllVServersVnfcsInfo " + e.getMessage());

            throw new SvcLogicException(e.getMessage());
        }
    }

    public void populateContext(ArrayList<Map<String, String>> vservers, SvcLogicContext ctx, String prefix) {


        log.info("Populating Final Context");
        int ctr = 0;

        for (Map<String, String> entry : vservers) {
            for (String key : entry.keySet()) {
                String value = entry.get(key);

                   ctx.setAttribute(prefix+ "vm[" + ctr + "]."+ key, value);
                log.info("Populating Context Key = " + prefix+ "vm[" + ctr + "]."+ key + " Value = " + value);

            }
            ctr++;
        }

        String firstVServerName = null;
        for  ( int i =0; i < ctr; i++ ) {
            String vnfcName = ctx.getAttribute(prefix + "vm[" + i + "].vnfc-name");
            log.info("VNFCNAME " + i + vnfcName);
            if (  vnfcName == null && firstVServerName == null ) {
                   firstVServerName = ctx.getAttribute(prefix + "vm[" + i + "].vserver-name");
                   ctx.setAttribute("vm-name" , firstVServerName);
                   log.info("Populating Context Key = " +  "vm-name" + " Value = " + firstVServerName);
            }
        }
    }



    public void addVnfcs(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {

        log.info("Received addVnfcs call with params : " + inParams);

        String responsePrefix = inParams.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX);

        int vnfcRefLen =0 ;
        int vmCount = 0;
        int vmWithNoVnfcCount = 0;

        try {

            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix+".") : "";
            AaiService aai = getAaiService();

            //no:of vnfcs from the vnfc_reference table          
            String vnfcRefLenStr = ctx.getAttribute("vnfcReference_length");

            if ( vnfcRefLenStr == null) {
                log.info("Vnfc Reference data is missing");
                throw new Exception("Vnfc Reference data is missing");

            }
            else
                vnfcRefLen = Integer.parseInt(vnfcRefLenStr);

            //Vms without vnfc from A&AI
            String vmWithNoVnfcCountStr = ctx.getAttribute(responsePrefix+"vnf.vm-with-no-vnfcs-count");

        // Modified for 1710

            if ( vmWithNoVnfcCountStr == null) {
                log.info("Parameter VM without VNFCs(vmWithNoVnfcCountStr) from A&AI is Null");
            }
            else
                vmWithNoVnfcCount = Integer.parseInt(vmWithNoVnfcCountStr);

            log.info("No of VM without VNFCs(vmWithNoVnfcCount) from A&AI is " +vmWithNoVnfcCount);

            String vmCountStr = ctx.getAttribute(responsePrefix+"vnf.vm-count");

            if ( vmCountStr == null)
                throw new Exception("VM data from A&AI is missing");
            else
                vmCount = Integer.parseInt(vmCountStr);
    if(vmCount<vnfcRefLen)
                throw new Exception("Vnfc and VM count mismatch");


            log.info("VMCOUNT " + vmCount);
            log.info("VNFCREFLEN " + vnfcRefLen);
            if(StringUtils.isBlank(ctx.getAttribute("vnfc-type"))){
                aai.updateVnfStatusWithOAMAddress(inParams, ctx);
            }

            
            aai.insertVnfcs(inParams,ctx, vnfcRefLen, vmCount);
            


            ctx.setAttribute(responsePrefix + AppcAaiClientConstant.OUTPUT_PARAM_STATUS,
                    AppcAaiClientConstant.OUTPUT_STATUS_SUCCESS);

            log.info("addVnfcs Successful ");
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + AppcAaiClientConstant.OUTPUT_PARAM_STATUS,
                    AppcAaiClientConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + AppcAaiClientConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
            log.error("Failed in addVnfcs " + e.getMessage());

            throw new SvcLogicException(e.getMessage());
        }
    }


    public void updateVnfAndVServerStatus(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {

        log.info("Received updateVnfAndVServerStatus call with params : " + inParams);

        String responsePrefix = inParams.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX);


        int vmCount = 0;


        try {

            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix+".") : "";
            AaiService aai = getAaiService();



            String vmCountStr = ctx.getAttribute(responsePrefix+"vnf.vm-count");

            if ( vmCountStr == null)
                throw new Exception("VM data from A&AI is missing");
            else
                vmCount = Integer.parseInt(vmCountStr);


            log.info("VMCOUNT " + vmCount);


            aai.updateVnfStatus(inParams, ctx);
            aai.updateVServerStatus(inParams,ctx, vmCount);

            ctx.setAttribute(responsePrefix + AppcAaiClientConstant.OUTPUT_PARAM_STATUS,
                    AppcAaiClientConstant.OUTPUT_STATUS_SUCCESS);

            log.info("updateVnfAndVServerStatus Successful ");
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + AppcAaiClientConstant.OUTPUT_PARAM_STATUS,
                    AppcAaiClientConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + AppcAaiClientConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
            log.error("Failed in updateVnfAndVServerStatus " + e.getMessage());

            throw new SvcLogicException(e.getMessage());
        }
    }

    public void getVserverInfo(Map<String, String> inParams, SvcLogicContext ctx) throws SvcLogicException {
        log.info("getVserverInfo()::Retrieving vm and vnfc information for vserver:" + inParams.toString());
        String responsePrefix = inParams.get(AppcAaiClientConstant.INPUT_PARAM_RESPONSE_PREFIX);
        try {
            responsePrefix = StringUtils.isNotBlank(responsePrefix) ? (responsePrefix + ".") : "";
            AaiService aaiService = getAaiService();
            String vServerId = inParams.get("vserverId");
            Map<String, String> params = setVmParams(ctx, vServerId);
            Map<String, String> vnfcParams = new HashMap<String, String>();
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
                vnfcParams.put("vnfcName", vnfcName);
            } else {
                log.info("getVserverInfo()::vnfc Name is blank, not setting vnfc info !!!!");
                return;
            }
            getVnfcInformationForVserver(vnfcParams, newVnfcCtx, inParams, ctx, aaiService, responsePrefix);
        } catch (Exception e) {
            ctx.setAttribute(responsePrefix + AppcAaiClientConstant.OUTPUT_PARAM_STATUS,
                    AppcAaiClientConstant.OUTPUT_STATUS_FAILURE);
            ctx.setAttribute(responsePrefix + AppcAaiClientConstant.OUTPUT_PARAM_ERROR_MESSAGE, e.getMessage());
            log.error("Failed in getVserverInfo " + e.getMessage());
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
        ctx.setAttribute(responsePrefix + "vm.vnfc.vnfc-name", vnfcParams.get("vnfcName"));
        ctx.setAttribute(responsePrefix + "vm.vnfc.vnfc-type", vnfcType);
        ctx.setAttribute(responsePrefix + "vm.vnfc.vnfc-function-code", vnfcFunctionCode);
        ctx.setAttribute(responsePrefix + "vm.vnfc.vnfc-group-notation", vnfcGroupNotation);
        ctx.setAttribute(responsePrefix + "vm.vnfc.vnfc-ipaddress-v4-oam-vip", vnfcV4OamIp);

    }

    public Map<String, String> setVmParams(SvcLogicContext ctx, String vServerId) {
        log.info("setVmParams()::setVmParamsVM level action:" + vServerId);
        Map<String, String> params = new HashMap<String, String>();
        int vmCount = 0, arrayIndex = -1;
        String vmCountStr = ctx.getAttribute("tmp.vnfInfo.vm-count");
        if (StringUtils.isNotBlank(vmCountStr))
            vmCount = Integer.parseInt(vmCountStr);
        for (int cnt = 0; cnt < vmCount; cnt++) {
            String vsId = ctx.getAttribute("tmp.vnfInfo.vm[" + cnt + "].vserver-id");
            log.info("setVmParams():::vserver details::" + cnt + ":" + vsId);
            if (StringUtils.equals(vServerId, vsId)) {
                arrayIndex = cnt;
            }
        }
        if (arrayIndex < 0) {
            log.info("setVmParams()::VserverId not found in context!! Returning null for params!!");
            return null;
        }
        String tenantId = ctx.getAttribute("tmp.vnfInfo.vm[" + arrayIndex + "].tenant-id");
        String cloudOwner = ctx.getAttribute("tmp.vnfInfo.vm[" + arrayIndex + "].cloud-owner");
        String cloudRegionId = ctx.getAttribute("tmp.vnfInfo.vm[" + arrayIndex + "].cloud-region-id");
        log.info("setVmParams()::tenantId=" + tenantId + " cloudOwner=" + cloudOwner + " cloudRegiodId= "
                + cloudRegionId);
        params.put("vserverId", vServerId);
        params.put("tenantId", tenantId);
        params.put("cloudOwner", cloudOwner);
        params.put("cloudRegionId", cloudRegionId);
        log.info("setVmParams()::setVmParamsVM level action:" + params.toString());
        return params;

    }
}
