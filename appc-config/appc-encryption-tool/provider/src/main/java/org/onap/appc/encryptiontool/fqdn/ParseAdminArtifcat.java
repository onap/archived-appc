/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

package org.onap.appc.encryptiontool.fqdn;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.lang.StringUtils;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;
import org.onap.ccsdk.sli.adaptors.resource.sql.SqlResource;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ParseAdminArtifcat implements SvcLogicJavaPlugin {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(ParseAdminArtifcat.class);
    private static ParseAdminArtifcat parseArtifact = null;
    private static String fqdn = null;
    private SvcLogicResource serviceLogic;

    public static ParseAdminArtifcat initialise() {
        parseArtifact = new ParseAdminArtifcat();
        return parseArtifact;
    }

    public ParseAdminArtifcat() {
        serviceLogic = new SqlResource();
    }

    protected ParseAdminArtifcat(SqlResource svcLogic) {
        serviceLogic = svcLogic;
    }

    protected static Map<String, String> parseAdminArtifact(ArtifactMapper js) {
        List<FqdnList> fqdnList = js.getFqdnList();
        Map<String, String> mp = new HashMap<String, String>();
        for (FqdnList listFqdn : fqdnList) {

            for (CloudOwnerList clList : listFqdn.getCloudOwnerList()) {

                for (RegionIdList rgList : clList.getRegionIdList()) {

                    for (String tenantId : rgList.getTenantIdList()) {
                        mp.put(clList.getCloudOwner().trim() + ";" + rgList.getRegionId().trim() + ";" + tenantId,
                                listFqdn.getVnfManagementServerFqdn());
                        log.info("list of  mapped details  from admin artifact" + clList.getCloudOwner().trim() + ";"
                                + rgList.getRegionId().trim() + ";" + tenantId + "-->"
                                + listFqdn.getVnfManagementServerFqdn());
                    }

                }

            }

        }

        return mp;

    }

    public String retrieveFqdn(SvcLogicContext ctx)
            throws SvcLogicException, RuntimeException, JsonProcessingException, IOException {
        String tenantId = "";
        String cloudOwner = "";
        String cloudRegionId = "";
        String jsonContent = getAdminArtifact(ctx);
        if (jsonContent == null) {
            throw new RuntimeException("Artifact content missing");
        }

        if (StringUtils.isNotBlank(ctx.getAttribute("payloadTenant"))) {
            tenantId = ctx.getAttribute("payloadTenant");
        } else {
            tenantId = ctx.getAttribute("tenantAai");
        }
        if (StringUtils.isNotBlank(ctx.getAttribute("payloadCloudOwner"))) {
            cloudOwner = ctx.getAttribute("payloadCloudOwner");
        } else {
            cloudOwner = ctx.getAttribute("cloudOwneraai");
        }
        if (StringUtils.isNotBlank(ctx.getAttribute("payloadCloudRegion"))) {
            cloudRegionId = ctx.getAttribute("payloadCloudRegion");
        } else {
            cloudRegionId = ctx.getAttribute("cloudRegionAai");

        }

        String key = cloudOwner.trim() + ";" + cloudRegionId.trim() + ";" + tenantId.trim();
        log.info("cloudowner--cloudregion--tenantid retrieved constructed :" + key);
        log.info("tenantid--cloudowner--cloudregion retrieved from payload :" + ctx.getAttribute("payloadTenant") + ":"
                + ctx.getAttribute("payloadCloudOwner") + ":" + ctx.getAttribute("payloadCloudRegion"));
        log.info("tenantid--cloudowner--cloudregion retrieved from a&ai : " + ctx.getAttribute("tenantAai") + ":"
                + ctx.getAttribute("cloudOwneraai") + ":" + ctx.getAttribute("cloudRegionAai"));
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
            ArtifactMapper js = objectMapper.readValue(jsonContent, ArtifactMapper.class);
            Map<String, String> mp = parseAdminArtifact(js);
            for (String t : mp.keySet()) {
                if (t.contains(key)) {
                    log.info("Matching fqdn from admin artifact found for a&ai data :" + key);
                    fqdn = mp.get(key);
                }
            }
            log.info(fqdn + "--> url and port retrieved ");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return fqdn;
    }

    public String getAdminArtifact(SvcLogicContext ctx) {

        QueryStatus status = null;
        String fn = "ParseAdminArtifcat:getAdminArtifact";
        String jsonContent = null;
        String artifcatName = "ansible_admin_FQDN_Artifact_0.0.1V.json";
        try {
            String query = "SELECT ARTIFACT_CONTENT as adminjson" + " FROM ASDC_ARTIFACTS " + "WHERE ARTIFACT_NAME = '"
                    + artifcatName + "' " + "ORDER BY INTERNAL_VERSION DESC LIMIT 1 ";
            log.info("Getting artifact details :" + query);
            status = serviceLogic.query("SQL", false, null, query, null, null, ctx);
            jsonContent = ctx.getAttribute("adminjson");
            log.info("adminjsonblock:" + jsonContent);
            if (status == QueryStatus.FAILURE) {
                log.info(fn + ": Error retrieving artifact details");
                throw new SvcLogicException("Error retrieving artifact details");
            }
        } catch (Exception e) {
            log.debug("Error while  accessing  database" + e.getMessage());
            log.info("Error connecting to database" + e.getMessage());
            log.error("Error accessing database", e);
            throw new RuntimeException(e);
        }
        return jsonContent;

    }

}
