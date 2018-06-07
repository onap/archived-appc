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

package org.onap.appc.design.xinterface;

import org.onap.appc.design.services.util.DesignServiceConstants;
import org.onap.appc.design.xinterface.XResponseProcessor;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import com.google.common.base.Strings;

public class XInterfaceService {
    
    private final EELFLogger log = EELFManager.getInstance().getLogger(XInterfaceService.class);
    public String execute(String action, String payload) throws Exception {
        //File targetFile = new File("/tmp/" + action + "-response.txt" );
        String interfaceResponse = null;
        try{
            if(Strings.isNullOrEmpty(payload))
                throw new Exception("Payload is null or empty..");
            if(DesignServiceConstants.GETINSTARDATA.equalsIgnoreCase(action)){
                XResponseProcessor xResponseProcessor =  new XResponseProcessor();
                interfaceResponse = xResponseProcessor.parseResponse(payload, DesignServiceConstants.GETINSTARDATA);
            } else {
                throw new Exception("No Such Action, Please enter valid Action");
            }
        }catch(Exception e){
            e.printStackTrace();
            throw e;
        }
        return interfaceResponse;
    }

}
