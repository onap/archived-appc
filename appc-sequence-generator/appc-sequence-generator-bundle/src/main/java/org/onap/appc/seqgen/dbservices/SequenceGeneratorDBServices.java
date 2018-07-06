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

package org.onap.appc.seqgen.dbservices;

import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;
import org.onap.ccsdk.sli.adaptors.resource.sql.SqlResource;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;
import org.onap.ccsdk.sli.core.dblib.DBResourceManager;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class SequenceGeneratorDBServices {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(SequenceGeneratorDBServices.class);
    private SvcLogicResource serviceLogic;
    private static SequenceGeneratorDBServices dgGeneralDBService = null;

    public static SequenceGeneratorDBServices initialise() {
        if (dgGeneralDBService == null) {
            dgGeneralDBService = new SequenceGeneratorDBServices();
        }
        return dgGeneralDBService;
    }

    public SequenceGeneratorDBServices() {
        if (serviceLogic == null) {
            serviceLogic = new SqlResource();
        }
    }

    public String getOutputPayloadTemplate(SvcLogicContext localContext) throws SvcLogicException {
        String fn = "DBService.getPayloadOutput";
        log.info("Entering getOutputPayloadTemplate()");
        QueryStatus status = null;
        localContext.setAttribute("file_category", "output_payload");
        if (serviceLogic != null && localContext != null) {
            String queryString = "select max(internal_version) as maxInternalVersion, artifact_name as artifactName from "
                    + "asdc_artifacts" + " where artifact_name in (select artifact_name from " + "asdc_artifacts"
                    + " where file_category = '" + "payload" + "' )";

            log.info(fn + "Quersy String : " + queryString);
            status = serviceLogic.query("SQL", false, null, queryString, null, null, localContext);

            if (status.toString().equals("FAILURE"))
                throw new SvcLogicException("Error - while getting output payload template");

            String queryString1 = "select artifact_content from " + "asdc_artifacts"
                    + " where artifact_name = $artifactName  and internal_version = $maxInternalVersion ";

            log.debug(fn + "Query String : " + queryString1);
            status = serviceLogic.query("SQL", false, null, queryString1, null, null, localContext);
            if (status.toString().equals("FAILURE"))
                throw new SvcLogicException("Error - while getting output payload template");

        }
        log.debug("Template for the payload data:" + localContext.getAttribute("artifact-content"));
        return localContext != null ? localContext.getAttribute("artifact-content") : null;
    }
}
