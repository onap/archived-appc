/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications (C) 2019 Ericsson
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

package org.onap.appc.dg.aai.impl;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.att.eelf.i18n.EELFResourceManager;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.onap.appc.dg.aai.AAIPlugin;
import org.onap.appc.dg.aai.exception.AAIQueryException;
import org.onap.appc.dg.aai.objects.AAIQueryResult;
import org.onap.appc.dg.aai.objects.Relationship;
import org.onap.appc.domainmodel.Vnf;
import org.onap.appc.domainmodel.Vnfc;
import org.onap.appc.domainmodel.Vserver;
import org.onap.appc.exceptions.APPCException;
import org.onap.appc.i18n.Msg;
import org.onap.ccsdk.sli.adaptors.aai.AAIClient;
import org.onap.ccsdk.sli.adaptors.aai.AAIService;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;


public class AAIPluginImpl implements AAIPlugin {

    private static final String PARAM_GENERIC_VNF = "generic-vnf";
    private static final String PARAM_RESOURCE_KEY = "resourceKey";

    private static final String STR_AAI_RESPONSE = "AAIResponse: ";
    private static final String STR_VNF_ID = "VNF ID ";
    private static final String STR_VNF_VNFC = "vnf.vnfc[";

    private static final String PROPERTY_IN_MAINT = "in-maint";
    private static final String PROPERTY_PROV_STATUS = "prov-status";
    private static final String PROPERTY_LOOP_DISABLED = "is-closed-loop-disabled";
    private static final String PROPERTY_RESOURCE_VERSION = "resource-version";
    private static final String PROPERTY_VNFC_FUNC_CODE = "vnfc-function-code";
    private static final String PROPERTY_ORCHESTRATION_STATUS = "orchestration-status";
    private static final String PROPERTY_VNFC_TYPE = "vnfc-type";
    private static final String PROPERTY_VNFC_NAME = "vnfc-name";
    private static final String PROPERTY_VSERVER_ID = "vserver-id";
    private static final String PROPERTY_VSERVER_SLINK = "vserver-selflink";
    private static final String PROPERTY_VSERVER_NAME = "vserver-name";
    private static final String PROPERTY_VSERVER_NAME_2 = "vserver-name2";
    private static final String PROPERTY_HEAT_STACK_ID = "heat-stack-id";
    private static final String PROPERTY_VNF_TYPE = "vnf-type";
    private static final String PROPERTY_VNF_NEM = "vnf-name";
    private static final String PARAM_RESOURCE_TYPE = "resourceType";

    private AAIClient aaiClient;

    private final EELFLogger logger = EELFManager.getInstance().getLogger(AAIPluginImpl.class);

    public void initialize() {
        BundleContext bctx = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        ServiceReference sref = bctx.getServiceReference(AAIService.class);
        aaiClient = (AAIClient) bctx.getService(sref);
    }

    @Override
    public void postGenericVnfData(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        String vnfId = ctx.getAttribute(Constants.VNF_ID_PARAM_NAME);
        String prefix = ctx.getAttribute(Constants.AAI_PREFIX_PARAM_NAME);

        String key = "vnf-id = '" + vnfId + "'";

        Map<String, String> data = new HashMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String paramKey = entry.getKey();
            int pos = paramKey.indexOf(Constants.AAI_INPUT_DATA);
            if (pos == 0) {
                data.put(paramKey.substring(Constants.AAI_INPUT_DATA.length() + 1), entry.getValue());
            }
        }

        try {
            SvcLogicResource.QueryStatus response = aaiClient.update(PARAM_GENERIC_VNF, key, data, prefix, ctx);
            if (SvcLogicResource.QueryStatus.NOT_FOUND.equals(response)) {
                String msg = EELFResourceManager.format(Msg.VNF_NOT_FOUND, vnfId);
                ctx.setAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE, msg);
                throw new APPCException(msg);
            }
            logger.info(STR_AAI_RESPONSE + response.toString());
            if (SvcLogicResource.QueryStatus.FAILURE.equals(response)) {
                String msg = EELFResourceManager.format(Msg.AAI_QUERY_FAILED, vnfId);
                ctx.setAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE, msg);
                throw new APPCException(msg);
            }
            String msg = EELFResourceManager
                .format(Msg.SUCCESS_EVENT_MESSAGE, "PostGenericVnfData", STR_VNF_ID + vnfId);
            ctx.setAttribute(org.onap.appc.Constants.ATTRIBUTE_SUCCESS_MESSAGE, msg);

        } catch (SvcLogicException e) {
            String msg = EELFResourceManager.format(Msg.AAI_QUERY_FAILED, vnfId);
            ctx.setAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE, msg);
            logger.error(msg);
            throw new APPCException(e);
        }
    }

    @Override
    public void getGenericVnfData(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        String vnfId = ctx.getAttribute(Constants.VNF_ID_PARAM_NAME);
        String prefix = ctx.getAttribute(Constants.AAI_PREFIX_PARAM_NAME);

        String key = "vnf-id = '" + vnfId + "'";
        try {
            SvcLogicResource.QueryStatus response = aaiClient
                .query(PARAM_GENERIC_VNF, false, null, key, prefix, null, ctx);
            if (SvcLogicResource.QueryStatus.NOT_FOUND.equals(response)) {
                String msg = EELFResourceManager.format(Msg.VNF_NOT_FOUND, vnfId);
                ctx.setAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE, msg);
                throw new APPCException(msg);
            } else if (SvcLogicResource.QueryStatus.FAILURE.equals(response)) {
                String msg = EELFResourceManager.format(Msg.AAI_QUERY_FAILED, vnfId);
                ctx.setAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE, msg);
                throw new APPCException(msg);
            }
            String aaiEntitlementPoolUuid = ctx.getAttribute(Constants.AAI_ENTITLMENT_POOL_UUID_NAME);
            if (null == aaiEntitlementPoolUuid) {
                aaiEntitlementPoolUuid = "";
            }
            String aaiLicenseKeyGroupUuid = ctx.getAttribute(Constants.AAI_LICENSE_KEY_UUID_NAME);
            if (null == aaiLicenseKeyGroupUuid) {
                aaiLicenseKeyGroupUuid = "";
            }

            ctx.setAttribute(Constants.IS_RELEASE_ENTITLEMENT_REQUIRE,
                Boolean.toString(!aaiEntitlementPoolUuid.isEmpty()));
            ctx.setAttribute(Constants.IS_RELEASE_LICENSE_REQUIRE, Boolean.toString(!aaiLicenseKeyGroupUuid.isEmpty()));
            String msg = EELFResourceManager.format(Msg.SUCCESS_EVENT_MESSAGE, "GetGenericVnfData", STR_VNF_ID + vnfId);
            ctx.setAttribute(org.onap.appc.Constants.ATTRIBUTE_SUCCESS_MESSAGE, msg);

            logger.info(STR_AAI_RESPONSE + response.toString());
        } catch (SvcLogicException e) {
            String msg = EELFResourceManager.format(Msg.AAI_QUERY_FAILED, vnfId);
            ctx.setAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE, msg);
            logger.error(msg);
            throw new APPCException(e);
        }
    }

    @Override
    public void getVnfHierarchy(Map<String, String> params, SvcLogicContext ctx) throws APPCException {

        Set<Vnfc> vnfcSet = new HashSet<>();
        String vnfType;
        String vnfVersion;
        String vnfId = params.get(PARAM_RESOURCE_KEY);
        AAIQueryResult vnfQueryResult;
        int vmCount = 0;
        try {
            vnfQueryResult = readVnf(vnfId);

            vnfType = vnfQueryResult.getAdditionProperties().get(PROPERTY_VNF_TYPE);
            vnfVersion = vnfQueryResult.getAdditionProperties().get(Constants.AAI_VNF_MODEL_VERSION_ID);

            Vnf vnf = createVnf(vnfType, vnfVersion, vnfId);

            for (Relationship vnfRelationship : vnfQueryResult.getRelationshipList()) {
                if ("vserver".equalsIgnoreCase(vnfRelationship.getRelatedTo())) {
                    vmCount++;
                    String tenantId = vnfRelationship.getRelationShipDataMap().get("tenant.tenant-id");
                    String vmId = vnfRelationship.getRelationShipDataMap().get("vserver.vserver-id");
                    String vmRelatedLink = vnfRelationship.getRelatedLink();
                    String vmName = vnfRelationship.getRelatedProperties().get("vserver.vserver-name");
                    String cloudOwner = vnfRelationship.getRelationShipDataMap().get("cloud-region.cloud-owner");
                    String cloudRegionId = vnfRelationship.getRelationShipDataMap().get("cloud-region.cloud-region-id");

                    AAIQueryResult vmQueryResult = readVM(vmId, tenantId, cloudOwner, cloudRegionId);
                    String vmURL = vmQueryResult.getAdditionProperties().get(PROPERTY_VSERVER_SLINK);

                    Vserver vm = createVserver(tenantId, vmId, vmRelatedLink, vmName, vmURL);
                    vnf.addVserver(vm);
                    for (Relationship vmRelation : vmQueryResult.getRelationshipList()) {

                        if ("vnfc".equalsIgnoreCase(vmRelation.getRelatedTo())) {
                            String vnfcName = vmRelation.getRelationShipDataMap().get("vnfc.vnfc-name");
                            AAIQueryResult vnfcQueryResult = readVnfc(vnfcName);
                            String vnfcType = vnfcQueryResult.getAdditionProperties().get(PROPERTY_VNFC_TYPE);

                            Vnfc newVnfc = createVnfc(vnfcName, vnfcType);

                            if (vnfcSet.contains(newVnfc)) {
                                Vnfc vnfcFromSet = vnfcSet.stream().filter(vnfc -> vnfc.equals(newVnfc))
                                    .collect(Collectors.toList()).get(0);
                                vnfcFromSet.addVserver(vm);
                                vm.setVnfc(vnfcFromSet);
                            } else {
                                vm.setVnfc(newVnfc);
                                newVnfc.addVserver(vm);
                                vnfcSet.add(newVnfc);
                            }
                        }
                    }
                }
            }
            ctx.setAttribute("VNF.VMCount", String.valueOf(vmCount));
            populateContext(vnf, ctx);
        } catch (AAIQueryException e) {
            ctx.setAttribute("getVnfHierarchy_result", "FAILURE");
            String msg = EELFResourceManager.format(Msg.AAI_QUERY_FAILED, vnfId);
            ctx.setAttribute(Constants.ATTRIBUTE_ERROR_MESSAGE, msg);
            logger.error("Failed in getVnfHierarchy, Error retrieving VNF details. Error message: " + ctx
                .getAttribute("getResource_result"), e);
            logger.warn("Incorrect or Incomplete VNF Hierarchy");
            throw new APPCException("Error Retrieving VNF hierarchy");
        }
        ctx.setAttribute("getVnfHierarchy_result", "SUCCESS");
        String msg = EELFResourceManager.format(Msg.SUCCESS_EVENT_MESSAGE, "GetVNFHierarchy", STR_VNF_ID + vnfId);
        ctx.setAttribute(org.onap.appc.Constants.ATTRIBUTE_SUCCESS_MESSAGE, msg);

    }

    private Vnf createVnf(String vnfType, String vnfVersion, String vnfId) {
        Vnf vnf = new Vnf();
        vnf.setVnfId(vnfId);
        vnf.setVnfType(vnfType);
        vnf.setVnfVersion(vnfVersion);
        return vnf;
    }

    private Vnfc createVnfc(String vnfcName, String vnfcType) {
        Vnfc vnfc = new Vnfc();
        vnfc.setVnfcName(vnfcName);
        vnfc.setVnfcType(vnfcType);
        return vnfc;
    }

    private Vserver createVserver(String tenantId, String vmId, String vmRelatedLink, String vmName, String vmURL) {
        Vserver vserver = new Vserver();
        vserver.setTenantId(tenantId);
        vserver.setId(vmId);
        vserver.setRelatedLink(vmRelatedLink);
        vserver.setName(vmName);
        vserver.setUrl(vmURL);
        return vserver;
    }

    private void populateContext(Vnf vnf, SvcLogicContext ctx) {
        ctx.setAttribute("vnf.type", vnf.getVnfType());
        ctx.setAttribute("vnf.version", vnf.getVnfVersion());
        ctx.setAttribute("vnf.vnfcCount", String.valueOf(vnf.getVnfcs().size()));
        int vnfcCount = 0;
        for (Vnfc vnfc : vnf.getVnfcs()) {
            ctx.setAttribute(STR_VNF_VNFC + vnfcCount + "].name", vnfc.getVnfcName());
            ctx.setAttribute(STR_VNF_VNFC + vnfcCount + "].type", vnfc.getVnfcType());
            ctx.setAttribute(STR_VNF_VNFC + vnfcCount + "].vm_count", String.valueOf(vnfc.getVserverList().size()));
            int vmCount = 0;
            for (Vserver vm : vnfc.getVserverList()) {
                ctx.setAttribute(STR_VNF_VNFC + vnfcCount + "].vm[" + vmCount++ + "].url", vm.getUrl());
            }
            vnfcCount++;
        }
    }

    private AAIQueryResult readVnfc(String vnfcName) throws AAIQueryException {
        String query = "vnfc.vnfc-name = '" + vnfcName + "'";
        String prefix = "VNFC";
        String resourceType = "vnfc";
        SvcLogicContext vnfContext = readResource(query, prefix, resourceType);
        String[] additionalProperties = new String[]{PROPERTY_VNFC_TYPE, PROPERTY_VNFC_NAME,
            PROPERTY_VNFC_FUNC_CODE, PROPERTY_IN_MAINT, PROPERTY_PROV_STATUS,
            PROPERTY_LOOP_DISABLED, PROPERTY_ORCHESTRATION_STATUS, PROPERTY_RESOURCE_VERSION};
        return readRelationDataAndProperties(prefix, vnfContext, additionalProperties);
    }

    protected AAIQueryResult readVM(String vmId, String tenantId, String cloudOwner, String cloudRegionId)
        throws AAIQueryException {
        String query = "vserver.vserver-id = '" + vmId + "' AND tenant.tenant_id = '" + tenantId
            + "' AND cloud-region.cloud-owner = '"
            + cloudOwner + "' AND cloud-region.cloud-region-id = '" + cloudRegionId + "'";
        String prefix = "VM";
        String resourceType = "vserver";
System.out.println("1"+query+"2."+prefix+"3."+resourceType);
        SvcLogicContext vnfContext = readResource(query, prefix, resourceType);
        String[] additionalProperties = new String[]{PROPERTY_VSERVER_ID, PROPERTY_VSERVER_SLINK,
            PROPERTY_VSERVER_NAME, PROPERTY_IN_MAINT, PROPERTY_PROV_STATUS, PROPERTY_LOOP_DISABLED,
            PROPERTY_VSERVER_NAME_2, PROPERTY_RESOURCE_VERSION,};

        return readRelationDataAndProperties(prefix, vnfContext, additionalProperties);
    }

    protected AAIQueryResult readVnf(String vnfId) throws AAIQueryException {
        String query = "generic-vnf.vnf-id = '" + vnfId + "'";
        String prefix = "VNF";
System.out.println("QUERY "+query+"\tPREFIX "+prefix+"\tGEN_VFN_PARAM "+PARAM_GENERIC_VNF);
        SvcLogicContext vnfContext = readResource(query, prefix, PARAM_GENERIC_VNF);

        String[] additionalProperties = new String[]{PROPERTY_VNF_TYPE, PROPERTY_VNF_NEM,
            PROPERTY_IN_MAINT, PROPERTY_PROV_STATUS, PROPERTY_HEAT_STACK_ID,
            PROPERTY_LOOP_DISABLED, PROPERTY_ORCHESTRATION_STATUS, PROPERTY_RESOURCE_VERSION, Constants.AAI_VNF_MODEL_VERSION_ID};

        return readRelationDataAndProperties(prefix, vnfContext, additionalProperties);
    }

    private AAIQueryResult readRelationDataAndProperties(String prefix, SvcLogicContext context,
        String[] additionalProperties) {
        AAIQueryResult result = new AAIQueryResult();
        if (context != null && context.getAttribute(prefix + ".relationship-list.relationship_length") != null) {
            Integer relationsCount = Integer.parseInt(context.getAttribute(
                prefix + ".relationship-list.relationship_length"));
            for (int i = 0; i < relationsCount; i++) {
                String rsKey = prefix + ".relationship-list.relationship[" + i + "]";
                Relationship relationShip = new Relationship();
                relationShip.setRelatedLink(context.getAttribute(rsKey + ".related-link"));
                relationShip.setRelatedTo(context.getAttribute(rsKey + ".related-to"));
                System.out.println(rsKey + ".relationship-data_length");
                Integer relationDataCount = Integer.parseInt(context.getAttribute(rsKey + ".relationship-data_length"));
                for (int j = 0; j < relationDataCount; j++) {
                    String rsDataKey = rsKey + ".relationship-data[" + j + "]";
                    String key = context.getAttribute(rsDataKey + ".relationship-key");
                    String value = context.getAttribute(rsDataKey + ".relationship-value");
                    relationShip.getRelationShipDataMap().put(key, value);
                }
                Integer relatedPropertyCount = 0;
                String relatedPropertyCountStr = null;
                try {
                    System.out.println(rsKey + ".related-to-property_length");
                    relatedPropertyCountStr = context.getAttribute(rsKey + ".related-to-property_length");
                    relatedPropertyCount = Integer.parseInt(relatedPropertyCountStr);
                } catch (NumberFormatException e) {
                    logger.debug("Invalid value in the context for Related Property Count " + relatedPropertyCountStr);
                }

                for (int j = 0; j < relatedPropertyCount; j++) {
                    String rsPropKey = rsKey + ".related-to-property[" + j + "]";
                    String key = context.getAttribute(rsPropKey + ".property-key");
                    String value = context.getAttribute(rsPropKey + ".property-value");
                    relationShip.getRelatedProperties().put(key, value);
                }
                result.getRelationshipList().add(relationShip);
            }
        } else {
            logger.error("Relationship-list not present in the SvcLogicContext attributes set."
                + (context == null ? "" : "Attribute KeySet = " + context.getAttributeKeySet()));
        }

        if (context != null) {
            for (String key : additionalProperties) {
                System.out.println(key+" "+context.getAttribute(prefix + "." + key));
                result.getAdditionProperties().put(key, context.getAttribute(prefix + "." + key));
            }
        }
        return result;
    }

    protected SvcLogicContext readResource(String query, String prefix, String resourceType) throws AAIQueryException {
        SvcLogicContext resourceContext = new SvcLogicContext();
        try {
            SvcLogicResource.QueryStatus response = aaiClient
                .query(resourceType, false, null, query, prefix, null, resourceContext);
            logger.info(STR_AAI_RESPONSE + response.toString());
            if (!SvcLogicResource.QueryStatus.SUCCESS.equals(response)) {
                throw new AAIQueryException("Error Retrieving VNF hierarchy from A&AI");
            }
        } catch (SvcLogicException e) {
            logger.error(EELFResourceManager.format(Msg.AAI_GET_DATA_FAILED, query), e);
            throw new AAIQueryException("Error Retrieving VNF hierarchy from A&AI");
        }
        return resourceContext;
    }

    @Override
    public void getResource(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        String resourceType = params.get(PARAM_RESOURCE_TYPE);
        String ctxPrefix = params.get("prefix");
        String resourceKey = params.get(PARAM_RESOURCE_KEY);
        if (logger.isDebugEnabled()) {
            logger.debug("inside getResorce");
            logger.debug("Retrieving " + resourceType + " details from A&AI for Key : " + resourceKey);
        }
        try {
            SvcLogicResource.QueryStatus response =
                aaiClient.query(resourceType, false, null, resourceKey, ctxPrefix, null, ctx);
            logger.info(STR_AAI_RESPONSE + response.toString());
            ctx.setAttribute("getResource_result", response.toString());
        } catch (SvcLogicException e) {
            logger.error(EELFResourceManager.format(Msg.AAI_GET_DATA_FAILED, resourceKey), e);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("exiting getResource======");
        }
    }

    @Override
    public void postResource(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        String resourceType = params.get(PARAM_RESOURCE_TYPE);
        String ctxPrefix = params.get("prefix");
        String resourceKey = params.get(PARAM_RESOURCE_KEY);
        String attributeName = params.get("attributeName");
        String attributeValue = params.get("attributeValue");
        if (logger.isDebugEnabled()) {
            logger.debug("inside postResource");
            logger.debug("Updating " + resourceType + " details in A&AI for Key : " + resourceKey);
            logger.debug("Updating " + attributeName + " to : " + attributeValue);
        }
        Map<String, String> data = new HashMap<>();
        data.put(attributeName, attributeValue);

        try {
            SvcLogicResource.QueryStatus response = aaiClient.update(resourceType, resourceKey, data, ctxPrefix, ctx);
            logger.info(STR_AAI_RESPONSE + response.toString());
            ctx.setAttribute("postResource_result", response.toString());
        } catch (SvcLogicException e) {
            logger.error(EELFResourceManager.format(Msg.AAI_UPDATE_FAILED, resourceKey, attributeValue), e);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("exiting postResource======");
        }
    }

    @Override
    public void deleteResource(Map<String, String> params, SvcLogicContext ctx) throws APPCException {
        String resourceType = params.get(PARAM_RESOURCE_TYPE);
        String resourceKey = params.get(PARAM_RESOURCE_KEY);

        if (logger.isDebugEnabled()) {
            logger.debug("inside deleteResource");
            logger.debug("Deleting " + resourceType + " details From A&AI for Key : " + resourceKey);
        }
        try {
            SvcLogicResource.QueryStatus response = aaiClient.delete(resourceType, resourceKey, ctx);
            logger.info(STR_AAI_RESPONSE + response.toString());
            ctx.setAttribute("deleteResource_result", response.toString());
        } catch (SvcLogicException e) {
            logger.error(EELFResourceManager.format(Msg.AAI_DELETE_FAILED, resourceKey), e);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("exiting deleteResource======");
        }
    }
}
