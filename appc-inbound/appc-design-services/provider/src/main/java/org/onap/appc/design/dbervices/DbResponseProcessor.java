/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.appc.design.dbervices;

import org.onap.appc.design.services.util.DesignServiceConstants;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class DbResponseProcessor {

    private static final EELFLogger log = EELFManager.getInstance().getLogger(DbResponseProcessor.class);
    public String parseResponse(String dbresposne, String action) throws Exception {
        
        log.info("Starting Parsing the response for action :[" + action +  "]\n data:[" + dbresposne +"]" );
        String response ;         
        switch (action) {
        case DesignServiceConstants.GETDESIGNS:
            response =  getDesignsResponse(dbresposne);
            break;
        case DesignServiceConstants.GETAPPCTIMESTAMPUTC:
            response = getAppcTimestampResponse(dbresposne);
            break;
        case DesignServiceConstants.ADDINCART:
            response =  getAddInCartResponse(dbresposne);
            break ;
        case DesignServiceConstants.GETARTIFACTREFERENCE:
            response=  getArtifactReferenceResponse(dbresposne);
            break;
        case DesignServiceConstants.GETARTIFACT:
            response=  getArtifactResponse(dbresposne);
            break;
        case DesignServiceConstants.GETGUIREFERENCE:
            response=  getGuiReferenceResponse(dbresposne);
            break;
        case DesignServiceConstants.GETSTATUS:
            response=  getStatusResponse(dbresposne);
            break;
        case DesignServiceConstants.UPLOADARTIFACT:
            response=  getsetStatusResponse(dbresposne);
            break;    
        case DesignServiceConstants.SETPROTOCOLREFERENCE:
            response=  getsetStatusResponse(dbresposne);
            break;    
        case DesignServiceConstants.SETINCART:
            response=  getsetStatusResponse(dbresposne);
            break;    
        default: 
            log.error("Action " + action + " Not Supported by response Parser");
            throw new Exception(" Action " + action + " not found while processing request ");            

        }        
        return response;            

    }

    private String getArtifactResponse(String dbresposne) {
        // TODO Auto-generated method stub
        return dbresposne;
    }

    private String getsetStatusResponse(String dbresposne) {
        // TODO Auto-generated method stub
        return null;
    }

    private String getStatusResponse(String dbresposne) {
        log.info("Returning reposne from Response Parser " + dbresposne);
        return dbresposne;
    }

    private String getGuiReferenceResponse(String dbresposne) {
        // TODO Auto-generated method stub
        return null;
    }

    private String getArtifactReferenceResponse(String dbresposne) {
        // TODO Auto-generated method stub
        return null;
    }

    private String getAddInCartResponse(String dbresposne) {
        // TODO Auto-generated method stub
        return null;
    }

    private String getDesignsResponse(String dbresposne) {
        return dbresposne;
        
    }

    private String getAppcTimestampResponse(String dbresposne) {
        log.info("getAppcTimestampResponse:["  + dbresposne +"]" );
        return dbresposne;
    }
}
