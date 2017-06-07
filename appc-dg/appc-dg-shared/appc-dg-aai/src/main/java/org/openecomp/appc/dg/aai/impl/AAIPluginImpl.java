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

package org.openecomp.appc.dg.aai.impl;
import org.openecomp.appc.domainmodel.Vnf;
import org.openecomp.appc.domainmodel.Vnfc;
import org.openecomp.appc.domainmodel.Vserver;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.i18n.Msg;
import com.att.eelf.i18n.EELFResourceManager;

import org.openecomp.appc.dg.aai.AAIPlugin;
import org.openecomp.appc.dg.aai.exception.AAIQueryException;
import org.openecomp.appc.dg.aai.objects.AAIQueryResult;
import org.openecomp.appc.dg.aai.objects.Relationship;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.openecomp.sdnc.sli.SvcLogicContext;
import org.openecomp.sdnc.sli.SvcLogicException;
import org.openecomp.sdnc.sli.SvcLogicResource;
import org.openecomp.sdnc.sli.aai.AAIClient;
import org.openecomp.sdnc.sli.aai.AAIService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import java.util.*;


public class AAIPluginImpl implements AAIPlugin {
    private AAIClient aaiClient;
    private static final EELFLogger logger = EELFManager.getInstance().getLogger(AAIPluginImpl.class);

    public AAIPluginImpl() {
        BundleContext bctx = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        ServiceReference sref = bctx.getServiceReference(AAIService.class);
        aaiClient = (AAIClient) bctx.getService(sref);
    }

    @Override
    public void postGenericVnfData(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        String vnf_id = ctx.getAttribute(Constants.VNF_ID_PARAM_NAME);
        String prefix = ctx.getAttribute(Constants.AAI_PREFIX_PARAM_NAME);

        String key = "vnf-id = '" + vnf_id + "'";

        Map<String, String> data = new HashMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String paramKey = entry.getKey();
            int pos = paramKey.indexOf(Constants.AAI_INPUT_DATA);
            if (pos == 0) {
                data.put(paramKey.substring(Constants.AAI_INPUT_DATA.length()+1), entry.getValue());
            }
        }

        try {
            SvcLogicResource.QueryStatus response = aaiClient.update("generic-vnf", key, data, prefix, ctx);
            if (SvcLogicResource.QueryStatus.NOT_FOUND.equals(response)) {
                String msg = EELFResourceManager.format(Msg.VNF_NOT_FOUND, vnf_id);
                ctx.setAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE, msg);
                throw new APPCException(msg);
            }
            logger.info("AAIResponse: " + response.toString());
            if (SvcLogicResource.QueryStatus.FAILURE.equals(response)) {
                String msg = EELFResourceManager.format(Msg.AAI_QUERY_FAILED, vnf_id);
                ctx.setAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE, msg);
                throw new APPCException(msg);
            }
            String msg = EELFResourceManager.format(Msg.SUCCESS_EVENT_MESSAGE, "PostGenericVnfData", "VNF ID " + vnf_id);
            ctx.setAttribute(org.openecomp.appc.Constants.ATTRIBUTE_SUCCESS_MESSAGE, msg);

        } catch (SvcLogicException e) {
            String msg = EELFResourceManager.format(Msg.AAI_QUERY_FAILED, vnf_id);
            ctx.setAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE, msg);
            logger.error(msg);
            throw new APPCException(e);
        }
    }

    @Override
    public void getGenericVnfData(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        String vnf_id = ctx.getAttribute(Constants.VNF_ID_PARAM_NAME);
        String prefix = ctx.getAttribute(Constants.AAI_PREFIX_PARAM_NAME);

        String key = "vnf-id = '" + vnf_id + "'";
        try {
            SvcLogicResource.QueryStatus response = aaiClient.query("generic-vnf", false, null, key, prefix, null, ctx);
            if (SvcLogicResource.QueryStatus.NOT_FOUND.equals(response)) {
                String msg = EELFResourceManager.format(Msg.VNF_NOT_FOUND, vnf_id);
//                String errorMessage = String.format("VNF not found for vnf_id = %s", vnf_id);
                ctx.setAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE, msg);
                throw new APPCException(msg);
            } else if (SvcLogicResource.QueryStatus.FAILURE.equals(response)) {
                String msg = EELFResourceManager.format(Msg.AAI_QUERY_FAILED, vnf_id);
                ctx.setAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE, msg);
                throw new APPCException(msg);
            }
            String aaiEntitlementPoolUuid = ctx.getAttribute(Constants.AAI_ENTITLMENT_POOL_UUID_NAME);
            if (null == aaiEntitlementPoolUuid) aaiEntitlementPoolUuid = "";
            String aaiLicenseKeyGroupUuid = ctx.getAttribute(Constants.AAI_LICENSE_KEY_UUID_NAME);
            if (null == aaiLicenseKeyGroupUuid) aaiLicenseKeyGroupUuid = "";

            ctx.setAttribute(Constants.IS_RELEASE_ENTITLEMENT_REQUIRE, Boolean.toString(!aaiEntitlementPoolUuid.isEmpty()));
            ctx.setAttribute(Constants.IS_RELEASE_LICENSE_REQUIRE, Boolean.toString(!aaiLicenseKeyGroupUuid.isEmpty()));
            String msg = EELFResourceManager.format(Msg.SUCCESS_EVENT_MESSAGE, "GetGenericVnfData","VNF ID " + vnf_id);
            ctx.setAttribute(org.openecomp.appc.Constants.ATTRIBUTE_SUCCESS_MESSAGE, msg);

            logger.info("AAIResponse: " + response.toString());
        } catch (SvcLogicException e) {
            String msg = EELFResourceManager.format(Msg.AAI_QUERY_FAILED, vnf_id);
            ctx.setAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE, msg);
            logger.error(msg);
            throw new APPCException(e);
        }
    }

    @Override
    public void getVnfHierarchy(Map<String, String> params, SvcLogicContext ctx) throws APPCException {

        Map<Vnfc,Set<Vserver>> vnfcMap = new HashMap<>();
        String vnfType,vnfVersion = null;
        String vnfId = params.get("resourceKey");
        AAIQueryResult vnfQueryResult = null;
        int vmCount =0;
        try {
            vnfQueryResult = readVnf(vnfId);

            vnfType = vnfQueryResult.getAdditionProperties().get("vnf-type");
            vnfVersion = vnfQueryResult.getAdditionProperties().get("persona-model-version");

            for(Relationship vnfRelationship:vnfQueryResult.getRelationshipList()){
                if("vserver".equalsIgnoreCase(vnfRelationship.getRelatedTo())){
                    vmCount++;
                    String tenantId = vnfRelationship.getRelationShipDataMap().get("tenant.tenant-id");
                    String vmId = vnfRelationship.getRelationShipDataMap().get("vserver.vserver-id");
                    String vmRelatedLink = vnfRelationship.getRelatedLink();
                    String vmName = vnfRelationship.getRelatedProperties().get("vserver.vserver-name");
                    String cloudOwner = vnfRelationship.getRelationShipDataMap().get("cloud-region.cloud-owner");
                    String cloudRegionId = vnfRelationship.getRelationShipDataMap().get("cloud-region.cloud-region-id");

                    AAIQueryResult vmQueryResult = readVM(vmId,tenantId,cloudOwner,cloudRegionId);
                    String vmURL = vmQueryResult.getAdditionProperties().get("vserver-selflink");

                    Vserver vm = new Vserver(vmURL,tenantId,vmId,vmRelatedLink,vmName);
                    for(Relationship vmRelation:vmQueryResult.getRelationshipList()){

                        if("vnfc".equalsIgnoreCase(vmRelation.getRelatedTo())){
                            String vnfcName = vmRelation.getRelationShipDataMap().get("vnfc.vnfc-name");
                            AAIQueryResult vnfcQueryResult = readVnfc(vnfcName);
                            String vnfcType = vnfcQueryResult.getAdditionProperties().get("vnfc-type");

                            Vnfc vnfc = new Vnfc(vnfcType,null,vnfcName);
                            Set<Vserver> vmSet = vnfcMap.get(vnfc);
                            if(vmSet == null){
                                vmSet = new HashSet<>();
                                vnfcMap.put(vnfc,vmSet);
                            }
                            vmSet.add(vm);
                        }
                    }
                }
            }
            ctx.setAttribute("VNF.VMCount",String.valueOf(vmCount));
        } catch (AAIQueryException e) {
            ctx.setAttribute("getVnfHierarchy_result", "FAILURE");
            String msg = EELFResourceManager.format(Msg.AAI_QUERY_FAILED, vnfId);
            ctx.setAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE, msg);
            logger.error("Failed in getVnfHierarchy, Error retrieving VNF details. Error message: " + ctx
                    .getAttribute("getResource_result"));
            logger.warn("Incorrect or Incomplete VNF Hierarchy");
            throw new APPCException("Error Retrieving VNF hierarchy");
        }

        Vnf vnf = new Vnf(vnfId,vnfType,vnfVersion);
        for(Vnfc vnfc:vnfcMap.keySet()){
            for(Vserver vm:vnfcMap.get(vnfc)){
                vnfc.addVm(vm);
            }
            vnf.addVnfc(vnfc);
        }

        populateContext(vnf,ctx);
        ctx.setAttribute("getVnfHierarchy_result", "SUCCESS");
        String msg = EELFResourceManager.format(Msg.SUCCESS_EVENT_MESSAGE, "GetVNFHierarchy","VNF ID " + vnfId);
        ctx.setAttribute(org.openecomp.appc.Constants.ATTRIBUTE_SUCCESS_MESSAGE, msg);

    }

    private void populateContext(Vnf vnf ,SvcLogicContext ctx) {
        ctx.setAttribute("vnf.type",vnf.getVnfType());
        ctx.setAttribute("vnf.version",vnf.getVnfVersion());
        ctx.setAttribute("vnf.vnfcCount",String.valueOf(vnf.getVnfcs().size()));
        int vnfcCount =0;
        for(Vnfc vnfc:vnf.getVnfcs()){
            ctx.setAttribute("vnf.vnfc["+vnfcCount+"].name",vnfc.getVnfcName());
            ctx.setAttribute("vnf.vnfc["+vnfcCount+"].type",vnfc.getVnfcType());
            ctx.setAttribute("vnf.vnfc["+vnfcCount+"].vm_count",String.valueOf(vnfc.getVserverList().size()));
            int vmCount =0;
            for(Vserver vm:vnfc.getVserverList()){
                ctx.setAttribute("vnf.vnfc["+vnfcCount+"].vm["+ vmCount++ +"].url",vm.getUrl());
            }
            vnfcCount++;
        }
    }

    private AAIQueryResult readVnfc(String vnfcName) throws AAIQueryException {
        String query = "vnfc.vnfc-name = '" + vnfcName + "'";
        String prefix = "VNFC";
        String resourceType = "vnfc";
        SvcLogicContext vnfContext = readResource(query,prefix,resourceType);
        String[] additionalProperties = new String[]{"vnfc-type","vnfc-name",
                "vnfc-function-code","in-maint","prov-status",
                "is-closed-loop-disabled","orchestration-status","resource-version"};
        AAIQueryResult result = readRelationDataAndProperties(prefix, vnfContext,additionalProperties);
        return result;
    }

    private AAIQueryResult readVM(String vmId,String tenantId,String cloudOwner,String cloudRegionId) throws AAIQueryException {
        String query = "vserver.vserver-id = '" + vmId + "' AND tenant.tenant_id = '" + tenantId + "' AND cloud-region.cloud-owner = '"
                + cloudOwner + "' AND cloud-region.cloud-region-id = '" + cloudRegionId + "'";
        String prefix = "VM";
        String resourceType = "vserver";
        SvcLogicContext vnfContext = readResource(query,prefix,resourceType);
        String[] additionalProperties = new String[]{"vserver-id","vserver-selflink",
                                                "vserver-name","in-maint","prov-status","is-closed-loop-disabled",
                                                "vserver-name2","resource-version",};
        AAIQueryResult result = readRelationDataAndProperties(prefix, vnfContext,additionalProperties);

        return result;
    }

    private AAIQueryResult readVnf(String vnfId) throws AAIQueryException {
        String query = "generic-vnf.vnf-id = '" + vnfId + "'";
        String prefix = "VNF";
        String resourceType = "generic-vnf";
        SvcLogicContext vnfContext = readResource(query,prefix,resourceType);

        String[] additionalProperties = new String[]{"vnf-type","vnf-name",
                "in-maint","prov-status","heat-stack-id",
                "is-closed-loop-disabled","orchestration-status","resource-version","persona-model-version"};

        AAIQueryResult result = readRelationDataAndProperties(prefix, vnfContext,additionalProperties);

        return result;
    }

    private AAIQueryResult readRelationDataAndProperties(String prefix, SvcLogicContext context,String[] additionalProperties) {
        AAIQueryResult result = new AAIQueryResult();

        Integer relationsCount = Integer.parseInt(context.getAttribute(prefix + ".relationship-list.relationship_length"));
        for(int i=0;i<relationsCount;i++){
            Relationship relationShip = new Relationship();
            relationShip.setRelatedLink(context.getAttribute(prefix + ".relationship-list.relationship["+i+"].related-link"));
            relationShip.setRelatedTo(context.getAttribute(prefix + ".relationship-list.relationship["+i+"].related-to"));
            Integer relationDataCount = Integer.parseInt(context.getAttribute(prefix + ".relationship-list.relationship["+i+"].relationship-data_length"));
            for(int j=0;j<relationDataCount;j++){
                String key = context.getAttribute(prefix+".relationship-list.relationship["+i+"].relationship-data["+j+"].relationship-key");
                String value = context.getAttribute(prefix+".relationship-list.relationship["+i+"].relationship-data["+j+"].relationship-value");
                relationShip.getRelationShipDataMap().put(key,value);
            }
            Integer relatedPropertyCount = 0;
            String relatedPropertyCountStr = null;
            try{
                relatedPropertyCountStr =context.getAttribute(prefix + ".relationship-list.relationship["+i+"].related-to-property_length");
                relatedPropertyCount = Integer.parseInt(relatedPropertyCountStr);
            }
            catch (NumberFormatException e){
                logger.debug("Invalid value in the context for Related Property Count " + relatedPropertyCountStr);
            }

            for(int j=0;j<relatedPropertyCount;j++){
                String key = context.getAttribute(prefix+".relationship-list.relationship["+i+"].related-to-property["+j+"].property-key");
                String value = context.getAttribute(prefix+".relationship-list.relationship["+i+"].related-to-property["+j+"].property-value");
                relationShip.getRelatedProperties().put(key,value);
            }
            result.getRelationshipList().add(relationShip);
        }

        for(String key:additionalProperties){
            result.getAdditionProperties().put(key,context.getAttribute(prefix+"."+key));
        }
        return result;
    }

    private SvcLogicContext readResource(String query, String prefix, String resourceType) throws AAIQueryException {
        SvcLogicContext resourceContext = new SvcLogicContext();
        try {
            SvcLogicResource.QueryStatus response = aaiClient.query(resourceType,false,null,query,prefix,null,resourceContext);
            logger.info("AAIResponse: " + response.toString());
            if(!SvcLogicResource.QueryStatus.SUCCESS.equals(response)){
                throw new AAIQueryException("Error Retrieving VNF hierarchy from A&AI");
            }
        } catch (SvcLogicException e) {
            logger.error(EELFResourceManager.format(Msg.AAI_GET_DATA_FAILED, query, "", e.getMessage()));
            throw new AAIQueryException("Error Retrieving VNF hierarchy from A&AI");
        }
        return resourceContext;
    }

    @Override public void getResource(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        String resourceType = params.get("resourceType"), ctx_prefix = params.get("prefix"), resourceKey =
                params.get("resourceKey");
        if (logger.isDebugEnabled()) {
            logger.debug("inside getResorce");
            logger.debug("Retrieving " + resourceType + " details from A&AI for Key : " + resourceKey);
        }
        try {
            SvcLogicResource.QueryStatus response =
                    aaiClient.query(resourceType, false, null, resourceKey, ctx_prefix, null, ctx);
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

        try {
            SvcLogicResource.QueryStatus response = aaiClient.update(resourceType, resourceKey, data, ctx_prefix, ctx);
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
        String resourceType = params.get("resourceType"), resourceKey = params.get("resourceKey");

        if (logger.isDebugEnabled()) {
            logger.debug("inside deleteResource");
            logger.debug("Deleting " + resourceType + " details From A&AI for Key : " + resourceKey);
        }
        try {
            SvcLogicResource.QueryStatus response = aaiClient.delete(resourceType, resourceKey, ctx);
            logger.info("AAIResponse: " + response.toString());
            ctx.setAttribute("deleteResource_result", response.toString());
        } catch (SvcLogicException e) {
            logger.error(EELFResourceManager.format(Msg.AAI_DELETE_FAILED, resourceKey, e.getMessage()));
        }
        if (logger.isDebugEnabled()) {
            logger.debug("exiting deleteResource======");
        }
    }
}
