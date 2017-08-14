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

package org.openecomp.appc.design.dbervices;

import org.openecomp.appc.design.services.util.ArtifactHandlerClient;
import org.openecomp.appc.design.services.util.DesignServiceConstants;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RequestValidator {
    
    private static final EELFLogger log = EELFManager.getInstance().getLogger(RequestValidator.class);
    public static void validate(String action, String payload) throws Exception {
        log.info("payload"  +  payload);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode payloadObject = objectMapper.readTree(payload);
        log.info("payloadObject"  +   payloadObject.get(DesignServiceConstants.ARTIFACT_CONTENTS));
        
        String errorString = null;
        switch (action) {
        case DesignServiceConstants.GETDESIGNS:
            if(payloadObject.get(DesignServiceConstants.USER_ID) == null || payloadObject.get(DesignServiceConstants.USER_ID).textValue().isEmpty())
                errorString =     DesignServiceConstants.USER_ID;
            break;
        case DesignServiceConstants.GETARTIFACT:
            if(payloadObject.get(DesignServiceConstants.VNF_TYPE) == null || payloadObject.get(DesignServiceConstants.VNF_TYPE).textValue().isEmpty())
                errorString =     DesignServiceConstants.VNF_TYPE;
            else if(payloadObject.get(DesignServiceConstants.ARTIFACT_TYPE) == null || payloadObject.get(DesignServiceConstants.ARTIFACT_TYPE).textValue().isEmpty())
                errorString = DesignServiceConstants.ARTIFACT_TYPE;
            else if(payloadObject.get(DesignServiceConstants.ARTIFACT_NAME) == null || payloadObject.get(DesignServiceConstants.ARTIFACT_NAME).textValue().isEmpty())
                errorString = DesignServiceConstants.ARTIFACT_NAME;
            break;
        case DesignServiceConstants.GETSTATUS:
            if(payloadObject.get(DesignServiceConstants.USER_ID) == null || payloadObject.get(DesignServiceConstants.USER_ID).textValue().isEmpty())
                errorString =     DesignServiceConstants.USER_ID;
            else if(payloadObject.get(DesignServiceConstants.VNF_TYPE) == null || payloadObject.get(DesignServiceConstants.VNF_TYPE).textValue().isEmpty())
                errorString =     DesignServiceConstants.VNF_TYPE;
            break;
        case DesignServiceConstants.SETSTATUS:
            if(payloadObject.get(DesignServiceConstants.USER_ID) == null || payloadObject.get(DesignServiceConstants.USER_ID).textValue().isEmpty())
                errorString =     DesignServiceConstants.USER_ID;
            else if(payloadObject.get(DesignServiceConstants.VNF_TYPE) == null || payloadObject.get(DesignServiceConstants.VNF_TYPE).textValue().isEmpty())
                errorString =     DesignServiceConstants.VNF_TYPE;
            else if(payloadObject.get(DesignServiceConstants.ACTION) == null || payloadObject.get(DesignServiceConstants.ACTION).textValue().isEmpty())
                errorString =     DesignServiceConstants.ACTION;
            else if(payloadObject.get(DesignServiceConstants.ARTIFACT_TYPE) == null || payloadObject.get(DesignServiceConstants.ARTIFACT_TYPE).textValue().isEmpty())
                errorString = DesignServiceConstants.ARTIFACT_TYPE;
            else if(payloadObject.get(DesignServiceConstants.STATUS) == null || payloadObject.get(DesignServiceConstants.STATUS).textValue().isEmpty())
                errorString = DesignServiceConstants.STATUS;
            break;            
        case DesignServiceConstants.UPLOADARTIFACT:
            if(payloadObject.get(DesignServiceConstants.ARTIFACT_NAME) == null || payloadObject.get(DesignServiceConstants.ARTIFACT_NAME).textValue().isEmpty())
                errorString =     DesignServiceConstants.ARTIFACT_NAME;
            else if(! payloadObject.get(DesignServiceConstants.ARTIFACT_NAME).textValue().contains("reference")){
                if(payloadObject.get(DesignServiceConstants.ACTION) == null || payloadObject.get(DesignServiceConstants.ACTION).textValue().isEmpty())
                    errorString =     DesignServiceConstants.ACTION;
            }
            else if(payloadObject.get(DesignServiceConstants.ARTIFACT_VERSOIN) == null || payloadObject.get(DesignServiceConstants.ARTIFACT_VERSOIN).textValue().isEmpty())
                errorString =     DesignServiceConstants.ARTIFACT_VERSOIN;
            else if(payloadObject.get(DesignServiceConstants.ARTIFACT_CONTENTS) == null)
                errorString =     DesignServiceConstants.ARTIFACT_CONTENTS;
            else if(payloadObject.get(DesignServiceConstants.ARTIFACT_TYPE) == null || payloadObject.get(DesignServiceConstants.ARTIFACT_TYPE).textValue().isEmpty())
                errorString = DesignServiceConstants.ARTIFACT_TYPE;
            
            else if(payloadObject.get(DesignServiceConstants.VNF_TYPE) == null || payloadObject.get(DesignServiceConstants.VNF_TYPE).textValue().isEmpty())
                errorString = DesignServiceConstants.VNF_TYPE;
        

            break;
        case DesignServiceConstants.SETPROTOCOLREFERENCE:
            if(payloadObject.get(DesignServiceConstants.ACTION) == null || payloadObject.get(DesignServiceConstants.ACTION).textValue().isEmpty())
                errorString =     DesignServiceConstants.ACTION;
            else if(payloadObject.get(DesignServiceConstants.ACTION_LEVEL) == null || payloadObject.get(DesignServiceConstants.ACTION_LEVEL).textValue().isEmpty())
                errorString =     DesignServiceConstants.ACTION_LEVEL;
            else if(payloadObject.get(DesignServiceConstants.VNF_TYPE) == null || payloadObject.get(DesignServiceConstants.VNF_TYPE).textValue().isEmpty())
                errorString =     DesignServiceConstants.VNF_TYPE;
            else if(payloadObject.get(DesignServiceConstants.PROTOCOL) == null || payloadObject.get(DesignServiceConstants.PROTOCOL).textValue().isEmpty())
                errorString =     DesignServiceConstants.PROTOCOL;
            
        case DesignServiceConstants.SETINCART:
            if(payloadObject.get(DesignServiceConstants.ACTION) == null || payloadObject.get(DesignServiceConstants.ACTION).textValue().isEmpty())
                errorString =     DesignServiceConstants.ACTION;
            else if(payloadObject.get(DesignServiceConstants.ACTION_LEVEL) == null || payloadObject.get(DesignServiceConstants.ACTION_LEVEL).textValue().isEmpty())
                errorString =     DesignServiceConstants.ACTION_LEVEL;
            else if(payloadObject.get(DesignServiceConstants.VNF_TYPE) == null || payloadObject.get(DesignServiceConstants.VNF_TYPE).textValue().isEmpty())
                errorString =     DesignServiceConstants.VNF_TYPE;
            else if(payloadObject.get(DesignServiceConstants.PROTOCOL) == null || payloadObject.get(DesignServiceConstants.PROTOCOL).textValue().isEmpty())
                errorString =     DesignServiceConstants.PROTOCOL;            
            break;
        default: 
            throw new Exception(" Action " + action + " not found while processing request ");            

        }
        if(errorString != null)
            throw new Exception(" Missing input parameter :-" + errorString + " -:");

    }

}


