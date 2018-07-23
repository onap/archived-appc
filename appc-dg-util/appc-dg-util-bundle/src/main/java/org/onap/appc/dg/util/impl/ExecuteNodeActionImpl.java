/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications Copyright (C) 2018 Nokia
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
 * 
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.dg.util.impl;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.onap.appc.dg.util.ExecuteNodeAction;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.i18n.Msg;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;


public class ExecuteNodeActionImpl implements ExecuteNodeAction {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(ExecuteNodeActionImpl.class);
    private static final String RESOURCE_TYPE_PARAM = "resourceType";
    private static final String RESOURCE_KEY_PARAM = "resourceKey";
    private static final String PREFIX_PARAM = "prefix";
    private static final String AAI_RESPONSE_STR = "AAIResponse: ";
    private static final String GET_RESOURCE_RESULT = "getResource_result";
    private static final String SUCCESS_PARAM = "SUCCESS";
    private static final String RELATIONSHIP_DATA_LEN_PARAM = "relationship-data_length";
    private static final String RELATIONSHIP_DATA_STR = "relationship-data[";
    private static final String VNFF_VM_STR = "VNF.VM[";
    private static final String VNF_VNFC_STR = "VNF.VNFC[";
    private static final String GET_VNF_HIERARCHY_RESULT_PARAM = "getVnfHierarchy_result";
    private static final String ERROR_RETRIEVING_VNFC_HIERARCHY_PARAM = "Error Retrieving VNFC hierarchy";
    private static final String RELATED_TO_PROPERTY_LEN_PARAM = "related-to-property_length";
    public static final String DG_OUTPUT_STATUS_MESSAGE = "output.status.message";

    private AAIServiceFactory aaiServiceFactory;

    public ExecuteNodeActionImpl(AAIServiceFactory aaiServiceFactory) {
        this.aaiServiceFactory = aaiServiceFactory;
    }

    /**
     * Method called in TestDG to test timeout scenario
     *
     * @param params waitTime time in millisecond DG is going to sleep
     */
    @Override
    public void waitMethod(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        try {
            String waitTime = params.get("waitTime");

            logger.info("DG will waits for " + Long.parseLong(waitTime) + "milliseconds");
            Thread.sleep(Long.parseLong(waitTime));
            logger.info("DG waits for " + Long.parseLong(waitTime) + " milliseconds completed");
        } catch (InterruptedException e) {
            logger.error("Error In ExecuteNodeActionImpl for waitMethod() due to InterruptedException: reason = " + e
                .getMessage());
        }
    }

    @Override
    public void getResource(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        String resourceType = params.get(RESOURCE_TYPE_PARAM);
        String ctxPrefix = params.get(PREFIX_PARAM);
        String resourceKey = params.get(RESOURCE_KEY_PARAM);

        if (logger.isDebugEnabled()) {
            logger.debug("inside getResorce");
            logger.debug("Retrieving " + resourceType + " details from A&AI for Key : " + resourceKey);
        }

        try {
            SvcLogicResource.QueryStatus response =
                aaiServiceFactory.getAAIService().query(resourceType, false, null, resourceKey, ctxPrefix, null, ctx);
            logger.info(AAI_RESPONSE_STR + response.toString());
            ctx.setAttribute(GET_RESOURCE_RESULT, response.toString());
        } catch (SvcLogicException e) {
            logger.error(EELFResourceManager.format(Msg.AAI_GET_DATA_FAILED, resourceKey, ""), e);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("exiting getResource======");
        }
    }

    @Override
    public void postResource(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        String resourceType = params.get(RESOURCE_TYPE_PARAM);
        String ctxPrefix = params.get(PREFIX_PARAM);
        String resourceKey = params.get(RESOURCE_KEY_PARAM);
        String attName = params.get("attributeName");
        String attValue = params.get("attributeValue");
        if (logger.isDebugEnabled()) {
            logger.debug("inside postResource");
            logger.debug("Updating " + resourceType + " details in A&AI for Key : " + resourceKey);
            logger.debug("Updating " + attName + " to : " + attValue);
        }
        Map<String, String> data = new HashMap<>();
        data.put(attName, attValue);

        try {
            SvcLogicResource.QueryStatus response = aaiServiceFactory.getAAIService()
                .update(resourceType, resourceKey, data, ctxPrefix, ctx);
            logger.info(AAI_RESPONSE_STR + response.toString());
            ctx.setAttribute("postResource_result", response.toString());
        } catch (SvcLogicException e) {
            logger.error(EELFResourceManager.format(Msg.AAI_UPDATE_FAILED, resourceKey, attValue), e);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("exiting postResource======");
        }
    }

    @Override
    public void deleteResource(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        String resourceType = params.get(RESOURCE_TYPE_PARAM);
        String resourceKey = params.get(RESOURCE_KEY_PARAM);

        if (logger.isDebugEnabled()) {
            logger.debug("inside deleteResource");
            logger.debug("Deleting " + resourceType + " details From A&AI for Key : " + resourceKey);
        }

        try {
            SvcLogicResource.QueryStatus response = aaiServiceFactory.getAAIService()
                .delete(resourceType, resourceKey, ctx);
            logger.info(AAI_RESPONSE_STR + response.toString());
            ctx.setAttribute("deleteResource_result", response.toString());
        } catch (SvcLogicException e) {
            logger.error(EELFResourceManager.format(Msg.AAI_DELETE_FAILED, resourceKey), e);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("exiting deleteResource======");
        }
    }

    @Override
    public void getVnfHierarchy(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        if (logger.isDebugEnabled()) {
            logger.debug("Inside getVnfHierarchy======");
        }
        String resourceKey = params.get(RESOURCE_KEY_PARAM);
        String retrivalVnfKey = "generic-vnf.vnf-id = '" + resourceKey + "'";
        Map<String, String> paramsVnf = new HashMap<>();
        paramsVnf.put(RESOURCE_TYPE_PARAM, "generic-vnf");
        paramsVnf.put(PREFIX_PARAM, "vnfRetrived");
        paramsVnf.put(RESOURCE_KEY_PARAM, retrivalVnfKey);
        logger.debug("Retrieving VNF details from A&AI");
        //Retrive all the relations of VNF
        SvcLogicContext vnfCtx = new SvcLogicContext();
        getResource(paramsVnf, vnfCtx);
        if (vnfCtx.getAttribute(GET_RESOURCE_RESULT).equals(SUCCESS_PARAM)) {
            trySetHeatStackIDAttribute(ctx, vnfCtx);
            ctx.setAttribute("vnf.type", vnfCtx.getAttribute("vnfRetrived.vnf-type"));
            Map<String, String> vnfHierarchyMap = new ConcurrentHashMap<>();

            Map<String, Set<String>> vnfcHierarchyMap = new HashMap<>();
            int vmCount = 0;
            Set<String> vmSet;
            String vmURL;
            logger.debug("Parsing Vserver details from VNF relations");

            //loop through relationship-list data, to get vserver relations
            for (String ctxKeySet : vnfCtx.getAttributeKeySet()) {
                if (ctxKeySet.startsWith("vnfRetrived.") && "vserver"
                    .equalsIgnoreCase(vnfCtx.getAttribute(ctxKeySet))) {
                    String vmKey = ctxKeySet.substring(0, ctxKeySet.length() - "related-to".length());
                    String vserverID = null;
                    String tenantID = null;
                    String cloudOwner = null;
                    String cloudRegionId = null;
                    int relationshipLength = getAttribute(vnfCtx, vmKey, RELATIONSHIP_DATA_LEN_PARAM);

                    for (int j = 0; j
                        < relationshipLength;
                        j++) {      //loop inside relationship data, to get vserver-id and tenant-id
                        String key = vnfCtx.getAttribute(vmKey + RELATIONSHIP_DATA_STR + j + "].relationship-key");
                        String value = vnfCtx.getAttribute(vmKey + RELATIONSHIP_DATA_STR + j + "].relationship-value");
                        vnfHierarchyMap.put(VNFF_VM_STR + vmCount + "]." + key, value);
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
                    int relatedPropertyLength = getAttribute(vnfCtx, vmKey, RELATED_TO_PROPERTY_LEN_PARAM);
                    for (int j = 0;
                        j < relatedPropertyLength; j++) {   //loop inside related-to-property data, to get vserver-name
                        String key = vnfCtx.getAttribute(vmKey + "related-to-property[" + j + "].property-key");
                        String value = vnfCtx.getAttribute(vmKey + "related-to-property[" + j + "].property-value");
                        vnfHierarchyMap.put(VNFF_VM_STR + vmCount + "]." + key, value);
                    }
                    //Retrive VM relations to find vnfc's
                    //VM to VNFC is 1 to 1 relation
                    String vmRetrivalKey = "vserver.vserver-id = '" + vserverID
                        + "' AND tenant.tenant_id = '" + tenantID
                        + "'" + "' AND cloud-region.cloud-owner = '" + cloudOwner
                        + "' AND cloud-region.cloud-region-id = '" + cloudRegionId + "'";
                    Map<String, String> paramsVm = new HashMap<>();
                    paramsVm.put(RESOURCE_TYPE_PARAM, "vserver");
                    paramsVm.put(PREFIX_PARAM, "vmRetrived");
                    paramsVm.put(RESOURCE_KEY_PARAM, vmRetrivalKey);
                    SvcLogicContext vmCtx = new SvcLogicContext();

                    logger.debug("Retrieving VM details from A&AI");
                    getResource(paramsVm, vmCtx);
                    if (vmCtx.getAttribute(GET_RESOURCE_RESULT).equals(SUCCESS_PARAM)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Parsing VNFC details from VM relations");
                        }
                        vmURL = vmCtx.getAttribute("vmRetrived.vserver-selflink");
                        vnfHierarchyMap.put(VNFF_VM_STR + vmCount + "].URL", vmURL);

                        //loop through relationship-list data, to get vnfc relations
                        for (String ctxVnfcKeySet : vmCtx.getAttributeKeySet()) {
                            if (ctxVnfcKeySet.startsWith("vmRetrived.")
                                && "vnfc".equalsIgnoreCase(vmCtx.getAttribute(ctxVnfcKeySet))) {

                                String vnfcKey =
                                    ctxVnfcKeySet.substring(0, ctxVnfcKeySet.length() - "related-to".length());

                                relationshipLength = getAttribute(vmCtx, vnfcKey, RELATIONSHIP_DATA_LEN_PARAM);

                                for (int j = 0; j
                                    < relationshipLength;
                                    j++) {          //loop through relationship data, to get vnfc name
                                    String key = vmCtx.getAttribute(
                                        vnfcKey + RELATIONSHIP_DATA_STR + j + "].relationship-key");
                                    String value = vmCtx.getAttribute(
                                        vnfcKey + RELATIONSHIP_DATA_STR + j + "].relationship-value");
                                    if ("vnfc.vnfc-name".equalsIgnoreCase(key)) {
                                        vnfHierarchyMap.put(VNFF_VM_STR + vmCount + "].VNFC", value);
                                        vmSet = resolveVmSet(vnfcHierarchyMap, value);
                                        vmSet.add(vmURL);
                                        vnfcHierarchyMap.put(value, vmSet);
                                        break; //VM to VNFC is 1 to 1 relation, once we got the VNFC name we can break the loop
                                    }
                                }
                            }
                        }
                    } else {
                        ctx.setAttribute(DG_OUTPUT_STATUS_MESSAGE, ERROR_RETRIEVING_VNFC_HIERARCHY_PARAM);
                        vnfHierarchyMap.put(GET_VNF_HIERARCHY_RESULT_PARAM, "FAILURE");
                        logger.error("Failed in getVnfHierarchy, Error retrieving Vserver details. Error message: "
                            + vmCtx.getAttribute(GET_RESOURCE_RESULT));
                        logger.warn("Incorrect or Incomplete VNF Hierarchy");
                        throw new APPCException(ERROR_RETRIEVING_VNFC_HIERARCHY_PARAM);
                    }
                    vmCount++;
                }
            }
            vnfHierarchyMap.put("VNF.VMCount", Integer.toString(vmCount));
            if (vmCount == 0) {
                ctx.setAttribute(DG_OUTPUT_STATUS_MESSAGE, "VM count is 0");
            }
            //code changes for getting vnfcs hirearchy
            populateVnfcsDetailsinContext(vnfcHierarchyMap, ctx);
            //vnf,vnfcCount
            ctx.setAttribute("VNF.VNFCCount",
                Integer.toString(vnfcHierarchyMap.size()));
            //code changes for getting vnfcs hirearchy
            ctx.setAttribute(GET_VNF_HIERARCHY_RESULT_PARAM, SUCCESS_PARAM);
            //Finally set all attributes to ctx
            for (Entry<String, String> entry: vnfHierarchyMap.entrySet()) {
                ctx.setAttribute(entry.getKey(), entry.getValue());
            }
        } else {
            ctx.setAttribute(GET_VNF_HIERARCHY_RESULT_PARAM, "FAILURE");
            ctx.setAttribute(DG_OUTPUT_STATUS_MESSAGE, ERROR_RETRIEVING_VNFC_HIERARCHY_PARAM);
            logger.error("Failed in getVnfHierarchy, Error retrieving VNF details. Error message: " + ctx
                .getAttribute(GET_RESOURCE_RESULT));
            logger.warn("Incorrect or Incomplete VNF Hierarchy");
            throw new APPCException(ERROR_RETRIEVING_VNFC_HIERARCHY_PARAM);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("exiting getVnfHierarchy======");
        }
    }

    private void trySetHeatStackIDAttribute(SvcLogicContext ctx, SvcLogicContext vnfCtx) {
        if (vnfCtx.getAttribute("vnfRetrived.heat-stack-id") != null) {
            ctx.setAttribute("VNF.heat-stack-id", vnfCtx.getAttribute("vnfRetrived.heat-stack-id"));
        }
    }

    private Set<String> resolveVmSet(Map<String, Set<String>> vnfcHierarchyMap, String value) {

        Set<String> vmSet = vnfcHierarchyMap.get(value);
        if (vmSet == null) {
            vmSet = new HashSet<>();
        }
        return vmSet;
    }

    private int getAttribute(SvcLogicContext ctx, String key, String param) {
        if (ctx.getAttributeKeySet().contains(key + param)) {
           return Integer.parseInt(ctx.getAttribute(key + param));
        }
        return 0;
    }

    void populateVnfcsDetailsinContext(Map<String, Set<String>> vnfcHierarchyMap, SvcLogicContext ctx)
        throws APPCException {
        SvcLogicContext vnfcCtx = new SvcLogicContext();
        int vnfcCounter = 0;
        for (Entry<String, Set<String>> entry : vnfcHierarchyMap.entrySet()) {
            String vnfcRetrivalKey = "vnfc-name = '" + entry.getKey() + "'";
            Map<String, String> paramsVnfc = new HashMap<>();
            paramsVnfc.put(RESOURCE_TYPE_PARAM, "vnfc");
            paramsVnfc.put(PREFIX_PARAM, "vnfcRetrived");
            paramsVnfc.put(RESOURCE_KEY_PARAM, vnfcRetrivalKey);

            logger.debug("Retrieving VM details from A&AI");
            getResource(paramsVnfc, vnfcCtx);
            if (vnfcCtx.getAttribute(GET_RESOURCE_RESULT).equals(SUCCESS_PARAM)) {
                if (logger.isDebugEnabled()){
                    logger.debug("Parsing VNFC details from VM relations");
                }
                //putting required values in the map
                //vnf.vnfc[vnfcIndex].type
                ctx.setAttribute(VNF_VNFC_STR + vnfcCounter + "].TYPE",
                    vnfcCtx.getAttribute("vnfcRetrived.vnfc-type"));

                // vnf.vnfc[vnfcIndex].name
                ctx.setAttribute(VNF_VNFC_STR + vnfcCounter + "].NAME",
                    vnfcCtx.getAttribute("vnfcRetrived.vnfc-name"));

                //vnf.vnfc[vnfcIndex].vmCount
                Set<String> vmSet = entry.getValue();
                String vmCountinVnfcs = Integer.toString(vmSet.size());
                ctx.setAttribute(VNF_VNFC_STR + vnfcCounter + "].VM_COUNT",
                    vmCountinVnfcs);
                int vmCount = 0;
                for (String vmURL : vmSet) {
                    ctx.setAttribute(VNF_VNFC_STR + vnfcCounter + "].VM[" + vmCount++ + "].URL", vmURL);
                }

            }
            vnfcCounter++;
        }
    }
}
