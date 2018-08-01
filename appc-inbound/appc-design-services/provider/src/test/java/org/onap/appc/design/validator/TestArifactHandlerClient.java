/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
 * Modification Copyright (C) 2018 IBM.
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

package org.onap.appc.design.validator;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.onap.appc.design.services.util.ArtifactHandlerClient;

import junit.framework.Assert;

public class TestArifactHandlerClient {
        
    @Test
    public void testCreateArtifactData() throws Exception{
        
        
        String content = FileUtils.readFileToString(new File("src/test/resources/uploadArtifact"));
        String payload = " { \"userID\": \"00000\", \"vnf-type\" : \"DesigTest-VNF\", \"action\" : \"Configure\", \"artifact-name\":\"DesignRestArtifact_reference\",\"artifact-version\" :\"0.01\",\"artifact-type\" :\"DESIGNTOOL-TEST\",\"artifact-contents\":  "  
        + content + 
         " } ";
        String requestID ="0000";
        ArtifactHandlerClient ahi = new ArtifactHandlerClient();
        String value =  ahi.createArtifactData(payload, requestID);
        Assert.assertTrue(!value.isEmpty());
        
    }
        
    @Test
    public void testExecute() throws Exception{
        
        
        String content = FileUtils.readFileToString(new File("src/test/resources/uploadArtifact"));
        String payload = " { \"userID\": \"00000\", \"vnf-type\" : \"DesigTest-VNF\", \"action\" : \"Configure\", \"artifact-name\":\"DesignRestArtifact_reference\",\"artifact-version\" :\"0.01\",\"artifact-type\" :\"DESIGNTOOL-TEST\",\"artifact-contents\":  "  
        + content + 
         " } ";
        String rpc = "Post";
        ArtifactHandlerClient ahi = new ArtifactHandlerClient();
        ahi.execute(payload, rpc);
        
    }
}
