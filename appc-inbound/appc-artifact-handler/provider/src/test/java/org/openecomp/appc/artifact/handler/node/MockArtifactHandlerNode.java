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

package org.openecomp.appc.artifact.handler.node;

import org.json.JSONObject;
import org.openecomp.appc.artifact.handler.dbservices.MockDBService;
import org.openecomp.appc.artifact.handler.utils.SdcArtifactHandlerConstants;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

public class MockArtifactHandlerNode extends ArtifactHandlerNode {

    @Override
    public boolean updateStoreArtifacts(JSONObject request_information, JSONObject document_information)
            throws Exception {
        if (request_information != null && request_information.get("RequestInfo").equals("testupdateStoreArtifacts")) {
            super.updateStoreArtifacts(request_information, document_information);
        }
        SvcLogicContext context = new SvcLogicContext();
        MockDBService dbservice = MockDBService.initialise();
        int intversion = 0;
        context.setAttribute("artifact_name",
                document_information.getString(SdcArtifactHandlerConstants.ARTIFACT_NAME));
        String internal_version = dbservice.getInternalVersionNumber(context,
                document_information.getString(SdcArtifactHandlerConstants.ARTIFACT_NAME), null);
        if (internal_version != null) {
            intversion = Integer.parseInt(internal_version);
            intversion++;
        }

        return true;

    }

    @Override
    public boolean storeReferenceData(JSONObject request_information, JSONObject document_information)
            throws Exception {
        if (request_information != null && request_information.get("RequestInfo").equals("testStoreReferenceData")) {
            super.storeReferenceData(request_information, document_information);
        }
        return true;
    }

}
