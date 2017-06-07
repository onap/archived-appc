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

package org.openecomp.appc.dg.licmgr.impl;

import java.util.Map;

import org.openecomp.appc.dg.licmgr.LicenseManagerPlugin;
import org.openecomp.appc.exceptions.APPCException;
import org.openecomp.appc.licmgr.Constants;
import org.openecomp.appc.licmgr.LicenseManager;
import org.openecomp.appc.licmgr.exception.DataAccessException;
import org.openecomp.appc.licmgr.objects.LicenseModel;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.openecomp.sdnc.sli.SvcLogicContext;



public class LicenseManagerPluginImpl implements LicenseManagerPlugin {

    private static EELFLogger logger = EELFManager.getInstance().getApplicationLogger();

    // populated by blueprint framework
    private LicenseManager licenseManager;

    public void setLicenseManager(LicenseManager licenseManager) {
        this.licenseManager = licenseManager;
    }

    /**
     * Retrieves license model from APPC database and populate flags into svc context
     * @param params map with parameters:
     *               org.openecomp.appc.vftype - the vnf type / service type;
     *               org.openecomp.appc.resource-version - the vnf version / service version
     * @param ctx service logic context
     *            1. supposed properties already in context:
     *            aai.input.data.entitlement-assignment-group-uuid - entitlement-group-uuid asset tag already stored in AAI
     *            aai.input.data.license-assignment-group-uuid - license-key-uuid asset tag already stored in AAI
     *            2. properties and flags stored in context after bean execution:
     *            model.entitlement.pool.uuid - entitlement-group-uuid from license model
     *            model.license.key.uuid - license-key-uuid from license model
     *            is.acquire-entitlement.require
     *            is.release-entitlement.require
     *            is.acquire-license.require
     *            is.release-license.require
     *            is.aai-entitlement-update.require
     *            is.aai-license-update.require
     *
     * @throws APPCException throws in case of any error
     */
    @Override
    public void retrieveLicenseModel(Map<String, String> params, SvcLogicContext ctx) throws APPCException {

        try {

            LicenseModel licenseModel = licenseManager.retrieveLicenseModel(params.get(Constants.VNF_TYPE_FIELD_NAME), params.get(Constants.VNF_RESOURCE_VERSION_FIELD_NAME));

            String modelEntitlementPoolUuid = licenseModel.getEntitlementPoolUuid(); if (null == modelEntitlementPoolUuid) modelEntitlementPoolUuid = "";
            String aaiEntitlementPoolUuid = ctx.getAttribute(Constants.AAI_ENTITLMENT_POOL_UUID_NAME); if (null == aaiEntitlementPoolUuid) aaiEntitlementPoolUuid = "";
            boolean isAcquireEntitlementRequire = !modelEntitlementPoolUuid.isEmpty() && !modelEntitlementPoolUuid.equals(aaiEntitlementPoolUuid);
            boolean isReleaseEntitlementRequire = !aaiEntitlementPoolUuid.isEmpty() && (isAcquireEntitlementRequire || modelEntitlementPoolUuid.isEmpty());
            boolean isAAIEntitlementUpdateRequire = isAcquireEntitlementRequire || isReleaseEntitlementRequire;
            ctx.setAttribute(Constants.MODEL_ENTITLMENT_POOL_UUID_NAME, modelEntitlementPoolUuid);
            ctx.setAttribute(Constants.IS_ACQUIRE_ENTITLEMENT_REQUIRE, Boolean.toString(isAcquireEntitlementRequire));
            ctx.setAttribute(Constants.IS_RELEASE_ENTITLEMENT_REQUIRE, Boolean.toString(isReleaseEntitlementRequire));
            ctx.setAttribute(Constants.IS_AAI_ENTITLEMENT_UPDATE_REQUIRE, Boolean.toString(isAAIEntitlementUpdateRequire));


            String modelLicenseKeyGroupUuid = licenseModel.getLicenseKeyGroupUuid(); if (null == modelLicenseKeyGroupUuid) modelLicenseKeyGroupUuid = "";
            String aaiLicenseKeyGroupUuid = ctx.getAttribute(Constants.AAI_LICENSE_KEY_UUID_NAME); if (null == aaiLicenseKeyGroupUuid) aaiLicenseKeyGroupUuid = "";
            String aaiLicenseKeyValue = ctx.getAttribute(Constants.AAI_LICENSE_KEY_VALUE); if (null == aaiLicenseKeyValue) aaiLicenseKeyValue = "";
            boolean isAcquireLicenseRequire = !modelLicenseKeyGroupUuid.isEmpty() && !modelLicenseKeyGroupUuid.equals(aaiLicenseKeyGroupUuid);
            boolean isReleaseLicenseRequire = !aaiLicenseKeyGroupUuid.isEmpty() && (isAcquireLicenseRequire || modelLicenseKeyGroupUuid.isEmpty());
            boolean isAAILicenseUpdateRequire = isAcquireLicenseRequire || isReleaseLicenseRequire;
            ctx.setAttribute(Constants.MODEL_LICENSE_KEY_UUID_NAME, modelLicenseKeyGroupUuid);
            ctx.setAttribute(Constants.IS_ACQUIRE_LICENSE_REQUIRE, Boolean.toString(isAcquireLicenseRequire));
            ctx.setAttribute(Constants.IS_RELEASE_LICENSE_REQUIRE, Boolean.toString(isReleaseLicenseRequire));
            ctx.setAttribute(Constants.IS_AAI_LICENSE_UPDATE_REQUIRE, Boolean.toString(isAAILicenseUpdateRequire));

            ctx.setAttribute("license-key", aaiLicenseKeyValue);

        } catch (DataAccessException le) {
            logger.error("Error " + le.getMessage());
            ctx.setAttribute("output.status.message", le.getMessage());
            throw new APPCException(le);
        }

    }



    //////// code uses jaxb license model, should be fixed
    /*
    final VfLicenseModel.FeatureGroupList featureGroupList = licenseModel.getFeatureGroupList();
    if (null != featureGroupList) {
        final VfLicenseModel.FeatureGroupList.FeatureGroup featureGroup = featureGroupList.getFeatureGroup();
        if (null != featureGroup) {
            final VfLicenseModel.FeatureGroupList.FeatureGroup.EntitlementPoolList
                            entitlementPoolList = featureGroup.getEntitlementPoolList();
            if (null != entitlementPoolList) {
                final VfLicenseModel.FeatureGroupList.FeatureGroup.EntitlementPoolList.EntitlementPool
                                entitlementPool = entitlementPoolList.getEntitlementPool();
                if (null != entitlementPool) {
                    final String entitlementPoolUuid = entitlementPool.getEntitlementPoolUuid();
                    // add entitlementPoolUuid into context
                    ctx.setAttribute(Constants.MODEL_ENTITLMENT_POOL_UUID_NAME, entitlementPoolUuid);
                }
            }

            final VfLicenseModel.FeatureGroupList.FeatureGroup.LicenseKeyGroupList
                            licenseKeyGroupList = featureGroup.getLicenseKeyGroupList();
            if (null != licenseKeyGroupList) {
                final VfLicenseModel.FeatureGroupList.FeatureGroup.LicenseKeyGroupList.LicenseKeyGroup
                                licenseKeyGroup = licenseKeyGroupList.getLicenseKeyGroup();
                if (null != licenseKeyGroup) {
                    final String licenseKeyGroupUuid = licenseKeyGroup.getLicenseKeyGroupUuid();
                    // add licenseKeyGroupUuid into context
                    ctx.setAttribute(Constants.MODEL_LICENSE_KEY_UUID_NAME, licenseKeyGroupUuid);
                }
            }
        }
    }
    */


}
