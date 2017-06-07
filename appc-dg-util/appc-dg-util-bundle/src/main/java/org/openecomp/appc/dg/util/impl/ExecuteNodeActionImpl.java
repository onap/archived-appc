/*-
 * ============LICENSE_START=======================================================
 * APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Amdocs
 * ================================================================================
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */

package org.openecomp.appc.dg.util.impl;

import org.openecomp.appc.dg.util.ExecuteNodeAction;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.i18n.Msg;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.openecomp.sdnc.sli.SvcLogicException;
import org.openecomp.sdnc.sli.SvcLogicResource;
import org.openecomp.sdnc.sli.aai.AAIClient;
import org.openecomp.sdnc.sli.aai.AAIService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class ExecuteNodeActionImpl implements ExecuteNodeAction {

    private AAIService aaiService;
    protected static AAIClient client;
    private static final EELFLogger logger = EELFManager.getInstance().getLogger(ExecuteNodeActionImpl.class);

    public static final String DG_OUTPUT_STATUS_MESSAGE = "output.status.message";
    public ExecuteNodeActionImpl() {
    }

    /**
     * initialize the SDNC adapter (AAIService) by building the context.
     */
    private void initialize() {
        getAAIservice();
    }

    private void getAAIservice() {
        BundleContext bctx = FrameworkUtil.getBundle(AAIService.class).getBundleContext();
        // Get AAIadapter reference
        ServiceReference sref = bctx.getServiceReference(AAIService.class.getName());
        if (sref != null) {
            logger.info("AAIService from bundlecontext");
            aaiService = (AAIService) bctx.getService(sref);

        } else {
            logger.info("AAIService error from bundlecontext");
            logger.error(EELFResourceManager.format(Msg.AAI_CONNECTION_FAILED, "AAIService"));
        }
    }

    /**
     * Method called in TestDG to test timeout scenario
     *
     * @param params waitTime time in millisecond DG is going to sleep
     * @param ctx
     * @throws APPCException
     */
    @Override public void waitMethod(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        try {
            String waitTime = params.get("waitTime");

            logger.info("DG will waits for " + Long.parseLong(waitTime) + "milliseconds");
            Thread.sleep(Long.parseLong(waitTime));
            logger.info("DG waits for " + Long.parseLong(waitTime) + " milliseconds completed");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override public void getResource(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        initialize();
        String resourceType = params.get("resourceType"), ctx_prefix = params.get("prefix"), resourceKey =
                params.get("resourceKey");
        if (logger.isDebugEnabled()) {
            logger.debug("inside getResorce");
            logger.debug("Retrieving " + resourceType + " details from A&AI for Key : " + resourceKey);
        }
        client = aaiService;
        try {
            SvcLogicResource.QueryStatus response =
                    client.query(resourceType, false, null, resourceKey, ctx_prefix, null, ctx);
            logger.info("AAIResponse: " + response.toString());
            ctx.setAttribute("getResource_result", response.toString());
        } catch (SvcLogicException e) {
            logger.error(EELFResourceManager.format(Msg.AAI_GET_DATA_FAILED, resourceKey, "", e.getMessage()));
        }
        if (logger.isDebugEnabled()) {
            logger.debug("exiting getResource======");
        }
    }

    @Override public void postResource(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        initialize();
        String resourceType = params.get("resourceType"), ctx_prefix = params.get("prefix"), resourceKey =
                params.get("resourceKey"), att_name = params.get("attributeName"), att_value =
                params.get("attributeValue");
        if (logger.isDebugEnabled()) {
            logger.debug("inside postResource");
            logger.debug("Updating " + resourceType + " details in A&AI for Key : " + resourceKey);
            logger.debug("Updating " + att_name + " to : " + att_value);
        }
        Map<String, String> data = new HashMap<String, String>();
        data.put(att_name, att_value);
        client = aaiService;

        try {
            SvcLogicResource.QueryStatus response = client.update(resourceType, resourceKey, data, ctx_prefix, ctx);
            logger.info("AAIResponse: " + response.toString());
            ctx.setAttribute("postResource_result", response.toString());
        } catch (SvcLogicException e) {
            logger.error(EELFResourceManager.format(Msg.AAI_UPDATE_FAILED, resourceKey, att_value, e.getMessage()));
        }
        if (logger.isDebugEnabled()) {
            logger.debug("exiting postResource======");
        }
    }

    @Override public void deleteResource(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        initialize();
        String resourceType = params.get("resourceType"), resourceKey = params.get("resourceKey");

        if (logger.isDebugEnabled()) {
            logger.debug("inside deleteResource");
            logger.debug("Deleting " + resourceType + " details From A&AI for Key : " + resourceKey);
        }
        client = aaiService;
        try {
            SvcLogicResource.QueryStatus response = client.delete(resourceType, resourceKey, ctx);
            logger.info("AAIResponse: " + response.toString());
            ctx.setAttribute("deleteResource_result", response.toString());
        } catch (SvcLogicException e) {
            logger.error(EELFResourceManager.format(Msg.AAI_DELETE_FAILED, resourceKey, e.getMessage()));
        }
        if (logger.isDebugEnabled()) {
            logger.debug("exiting deleteResource======");
        }
    }

    @Override public void getVnfHierarchy(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        if (logger.isDebugEnabled()) {
            logger.debug("Inside getVnfHierarchy======");
        }
        //String ctx_prefix = params.get("prefix");
        String resourceKey = params.get("resourceKey");
       // String retrivalVnfKey = "vnf-id = '" + resourceKey + "' AND relationship-key = 'vserver.vserver-id'";
        String retrivalVnfKey = "generic-vnf.vnf-id = '" + resourceKey + "'";
        Map<String, String> paramsVnf = new HashMap<String, String>();
        paramsVnf.put("resourceType", "generic-vnf");
        paramsVnf.put("prefix", "vnfRetrived");
        paramsVnf.put("resourceKey", retrivalVnfKey);
        logger.debug("Retrieving VNF details from A&AI");
        //Retrive all the relations of VNF
        SvcLogicContext vnfCtx = new SvcLogicContext();
        getResource(paramsVnf, vnfCtx);
        if (vnfCtx.getAttribute("getResource_result").equals("SUCCESS")) {
            if (vnfCtx.getAttribute("vnfRetrived.heat-stack-id") != null) {
                ctx.setAttribute("VNF.heat-stack-id", vnfCtx.getAttribute("vnfRetrived.heat-stack-id"));
            }
            ctx.setAttribute("vnf.type",vnfCtx.getAttribute("vnfRetrived.vnf-type"));
            Map<String, String> vnfHierarchyMap = new ConcurrentHashMap<String, String>();

            Map<String, Set<String>> vnfcHierarchyMap = new HashMap<String, Set<String>>();
            int vmCount = 0;
            int vnfcCount = 0;
            Set<String> vmSet = null;
            String vmURL = "";
            logger.debug("Parsing Vserver details from VNF relations");
            for (String ctxKeySet : vnfCtx
                    .getAttributeKeySet()) {     //loop through relationship-list data, to get vserver relations
                if (ctxKeySet.startsWith("vnfRetrived.") && vnfCtx.getAttribute(ctxKeySet).equalsIgnoreCase("vserver")) {
                    String vmKey = ctxKeySet.substring(0, ctxKeySet.length() - "related-to".length());
                    String vserverID = null;
                    String tenantID = null;
                    String cloudOwner = null;
                    String cloudRegionId = null;
                    int relationshipLength = 0;
                    if (vnfCtx.getAttributeKeySet().contains(vmKey + "relationship-data_length")) {
                        relationshipLength = Integer.parseInt(vnfCtx.getAttribute(vmKey + "relationship-data_length"));
                    }

                    for (int j = 0; j
                            < relationshipLength; j++) {      //loop inside relationship data, to get vserver-id and tenant-id
                        String key = vnfCtx.getAttribute(vmKey + "relationship-data[" + j + "].relationship-key");
                        String value = vnfCtx.getAttribute(vmKey + "relationship-data[" + j + "].relationship-value");
                        vnfHierarchyMap.put("VNF.VM[" + vmCount + "]." + key, value);
                        if ("vserver.vserver-id".equals(key)) {
                            vserverID = value;
                        }
                        if ("tenant.tenant-id".equals(key)) {
                            tenantID = value;
                        }
                        if ("cloud-region.cloud-owner".equals(key)) {
                            cloudOwner = value;
                        }
                        if ("cloud-region.cloud-region-id".equals(key)) {
                            cloudRegionId = value;
                        }
                    }
                    int relatedPropertyLength = 0;
                    if (vnfCtx.getAttributeKeySet().contains(vmKey + "related-to-property_length")) {
                        relatedPropertyLength =
                                Integer.parseInt(vnfCtx.getAttribute(vmKey + "related-to-property_length"));
                    }
                    for (int j = 0;
                         j < relatedPropertyLength; j++) {   //loop inside related-to-property data, to get vserver-name
                        String key = vnfCtx.getAttribute(vmKey + "related-to-property[" + j + "].property-key");
                        String value = vnfCtx.getAttribute(vmKey + "related-to-property[" + j + "].property-value");
                        vnfHierarchyMap.put("VNF.VM[" + vmCount + "]." + key, value);
                    }
                    //Retrive VM relations to find vnfc's
                    //VM to VNFC is 1 to 1 relation
                    String vmRetrivalKey = "vserver.vserver-id = '" + vserverID + "' AND tenant.tenant_id = '" + tenantID + "'" + "' AND cloud-region.cloud-owner = '" + cloudOwner + "' AND cloud-region.cloud-region-id = '" + cloudRegionId + "'";	
                    Map<String, String> paramsVm = new HashMap<String, String>();
                    paramsVm.put("resourceType", "vserver");
                    paramsVm.put("prefix", "vmRetrived");
                    paramsVm.put("resourceKey", vmRetrivalKey);
                    SvcLogicContext vmCtx = new SvcLogicContext();

                    logger.debug("Retrieving VM details from A&AI");
                    getResource(paramsVm, vmCtx);
                    if (vmCtx.getAttribute("getResource_result").equals("SUCCESS")) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Parsing VNFC details from VM relations");
                        }
                        vmURL = vmCtx.getAttribute("vmRetrived.vserver-selflink");
                        vnfHierarchyMap.put("VNF.VM[" + vmCount + "].URL",vmURL);
                        for (String ctxVnfcKeySet : vmCtx
                                .getAttributeKeySet()) {    //loop through relationship-list data, to get vnfc relations
                            if (ctxVnfcKeySet.startsWith("vmRetrived.") && vmCtx.getAttribute(ctxVnfcKeySet)
                                    .equalsIgnoreCase("vnfc")) {
                                String vnfcKey = ctxVnfcKeySet.substring(0,
                                        ctxVnfcKeySet.length() - "related-to".length());
                                relationshipLength = 0;
                                if (vmCtx.getAttributeKeySet().contains(vnfcKey + "relationship-data_length")) {
                                    relationshipLength = Integer.parseInt(
                                            vmCtx.getAttribute(vnfcKey + "relationship-data_length"));
                                }
                                for (int j = 0; j
                                        < relationshipLength; j++) {          //loop through relationship data, to get vnfc name
                                    String key = vmCtx.getAttribute(
                                            vnfcKey + "relationship-data[" + j + "].relationship-key");
                                    String value = vmCtx.getAttribute(
                                            vnfcKey + "relationship-data[" + j + "].relationship-value");
                                    if (key.equalsIgnoreCase("vnfc.vnfc-name")) {
                                        vnfHierarchyMap.put("VNF.VM[" + vmCount + "].VNFC", value);
                                        vmSet = vnfcHierarchyMap.get(value);
                                        if(vmSet == null){
                                            vmSet = new HashSet<>();
                                        }
                                        vmSet.add(vmURL);
                                        vnfcHierarchyMap.put(value,vmSet);
                                        break; //VM to VNFC is 1 to 1 relation, once we got the VNFC name we can break the loop
                                    }
                                }
                            }
                        }
                    } else {
                        ctx.setAttribute(DG_OUTPUT_STATUS_MESSAGE, "Error Retrieving VNFC hierarchy");
                        vnfHierarchyMap.put("getVnfHierarchy_result", "FAILURE");
                        logger.error("Failed in getVnfHierarchy, Error retrieving Vserver details. Error message: "
                                + vmCtx.getAttribute("getResource_result"));
                        logger.warn("Incorrect or Incomplete VNF Hierarchy");
                        throw new APPCException("Error Retrieving VNFC hierarchy");
                    }
                    vmCount++;
                }
            }
            vnfHierarchyMap.put("VNF.VMCount", vmCount + "");
            if (vmCount == 0) {
                ctx.setAttribute(DG_OUTPUT_STATUS_MESSAGE, "VM count is 0");
            }
            //code changes for getting vnfcs hirearchy
            populateVnfcsDetailsinContext(vnfcHierarchyMap,ctx);
            //vnf,vnfcCount
            ctx.setAttribute("VNF.VNFCCount",
                    Integer.toString(vnfcHierarchyMap.size()));
            //code changes for getting vnfcs hirearchy
            ctx.setAttribute("getVnfHierarchy_result", "SUCCESS");
            //Finally set all attributes to ctx
            for (String attribute : vnfHierarchyMap.keySet()) {
                ctx.setAttribute(attribute, vnfHierarchyMap.get(attribute));
            }
        } else {
            ctx.setAttribute("getVnfHierarchy_result", "FAILURE");
            ctx.setAttribute(DG_OUTPUT_STATUS_MESSAGE, "Error Retrieving VNFC hierarchy");
            logger.error("Failed in getVnfHierarchy, Error retrieving VNF details. Error message: " + ctx
                    .getAttribute("getResource_result"));
            logger.warn("Incorrect or Incomplete VNF Hierarchy");
            throw new APPCException("Error Retrieving VNFC hierarchy");
        }
        if (logger.isDebugEnabled()) {
            logger.debug("exiting getVnfHierarchy======");
        }
    }

    private void populateVnfcsDetailsinContext(Map<String, Set<String>> vnfcHierarchyMap, SvcLogicContext ctx) throws APPCException {
//        int vnfcCount = vnfcHierarchyMap.size();
        SvcLogicContext vnfcCtx = new SvcLogicContext();
        int vnfcCounter = 0;
        for (String vnfcName : vnfcHierarchyMap.keySet()) {
            String vnfcRetrivalKey = "vnfc-name = '" + vnfcName + "'";
            Map<String, String> paramsVnfc = new HashMap<String, String>();
            paramsVnfc.put("resourceType", "vnfc");
            paramsVnfc.put("prefix", "vnfcRetrived");
            paramsVnfc.put("resourceKey", vnfcRetrivalKey);

            logger.debug("Retrieving VM details from A&AI");
            getResource(paramsVnfc, vnfcCtx);
            if (vnfcCtx.getAttribute("getResource_result").equals("SUCCESS")) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Parsing VNFC details from VM relations");
                }
                //putting required values in the map
                //vnf.vnfc[vnfcIndex].type
                ctx.setAttribute("VNF.VNFC[" + vnfcCounter + "].TYPE",
                        vnfcCtx.getAttribute("vnfcRetrived.vnfc-type"));

                // vnf.vnfc[vnfcIndex].name
                ctx.setAttribute("VNF.VNFC[" + vnfcCounter + "].NAME",
                        vnfcCtx.getAttribute("vnfcRetrived.vnfc-name"));

                //vnf.vnfc[vnfcIndex].vmCount
                Set<String> vmSet = vnfcHierarchyMap.get(vnfcName);
                String vmCountinVnfcs = Integer.toString(vmSet.size());
                ctx.setAttribute("VNF.VNFC[" + vnfcCounter + "].VM_COUNT",
                        vmCountinVnfcs);
                int vmCount =0;
                for(String vmURL:vmSet){
                    ctx.setAttribute("VNF.VNFC[" + vnfcCounter + "].VM[" + vmCount++ + "].URL",vmURL);
                }

            }
            vnfcCounter++;
        }
    }
}
