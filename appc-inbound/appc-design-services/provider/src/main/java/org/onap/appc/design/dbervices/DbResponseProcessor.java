/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2019 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.design.dbervices;

import org.onap.appc.design.services.util.DesignServiceConstants;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class DbResponseProcessor {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(DbResponseProcessor.class);
    public String parseResponse(String dbresponse, String action) throws Exception {

        log.info("Starting Parsing the response for action :[" + action +  "]\ndata:[" + dbresponse + "]");
        String response;
        switch (action) {
        case DesignServiceConstants.GETDESIGNS:
            response = getDesignsResponse(dbresponse);
            break;
        case DesignServiceConstants.GETAPPCTIMESTAMPUTC:
            response = getAppcTimestampResponse(dbresponse);
            break;
        case DesignServiceConstants.ADDINCART:
            response = getAddInCartResponse(dbresponse);
            break;
        case DesignServiceConstants.GETARTIFACTREFERENCE:
            response = getArtifactReferenceResponse(dbresponse);
            break;
        case DesignServiceConstants.GETARTIFACT:
            response = getArtifactResponse(dbresponse);
            break;
        case DesignServiceConstants.GETGUIREFERENCE:
            response = getGuiReferenceResponse(dbresponse);
            break;
        case DesignServiceConstants.GETSTATUS:
            response = getStatusResponse(dbresponse);
            break;
        case DesignServiceConstants.UPLOADARTIFACT:
            response = getSetStatusResponse(dbresponse);
            break;
        case DesignServiceConstants.SETPROTOCOLREFERENCE:
            response = getSetStatusResponse(dbresponse);
            break;
        case DesignServiceConstants.SETINCART:
            response = getSetStatusResponse(dbresponse);
            break;
        case DesignServiceConstants.UPLOADADMINARTIFACT:
            response = getSetStatusResponse(dbresponse);
            break;
        case DesignServiceConstants.CHECKVNF:
            response = getStatusResponse(dbresponse);
            break;
        case DesignServiceConstants.RETRIEVEVNFPERMISSIONS:
            response = getStatusResponse(dbresponse);
            break;
        case DesignServiceConstants.SAVEVNFPERMISSIONS:
            response = getRetrieveVnfPermissionsResponse(dbresponse);
            break;
        default:
            log.error("Action " + action + " Not Supported by Response Parser");
            throw new Exception("Action " + action + " not found while processing request");

        }
        return response;

    }

    private String getArtifactResponse(String dbresponse) {
        // TODO Auto-generated method stub
        return dbresponse;
    }

    private String getSetStatusResponse(String dbresponse) {
        // TODO Auto-generated method stub
        return null;
    }

    private String getStatusResponse(String dbresponse) {
        log.info("Returning response from Response Parser " + dbresponse);
        return dbresponse;
    }

    private String getGuiReferenceResponse(String dbresponse) {
        // TODO Auto-generated method stub
        return null;
    }

    private String getArtifactReferenceResponse(String dbresponse) {
        // TODO Auto-generated method stub
        return null;
    }

    private String getAddInCartResponse(String dbresponse) {
        // TODO Auto-generated method stub
        return null;
    }

    private String getDesignsResponse(String dbresponse) {
        return dbresponse;
    }

    private String getAppcTimestampResponse(String dbresponse) {
        log.info("getAppcTimestampResponse:[" + dbresponse +"]" );
        return dbresponse;
    }

    private String getRetrieveVnfPermissionsResponse(String dbresponse) {
        log.info("Returning response from Response Parser " + dbresponse);
        return dbresponse;
    }

}
