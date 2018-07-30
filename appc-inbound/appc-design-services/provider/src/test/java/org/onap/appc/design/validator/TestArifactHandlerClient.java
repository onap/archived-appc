/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
 *  Modification Copyright (C) 2018 IBM
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

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.onap.appc.design.services.util.ArtifactHandlerClient;
import org.powermock.reflect.Whitebox;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.header.InBoundHeaders;
import com.sun.jersey.spi.MessageBodyWorkers;

public class TestArifactHandlerClient {
    
    @Ignore
    @Test
    public void testCreateArtifactData1(){
        
        try{
        String content = FileUtils.readFileToString(new File("src/test/resources/uploadArtifact"));
        String payload = " { \"userID\": \"00000\", \"vnf-type\" : \"DesigTest-VNF\", \"action\" : \"Configure\", \"artifact-name\":\"DesignRestArtifact_reference\",\"artifact-version\" :\"0.01\",\"artifact-type\" :\"DESIGNTOOL-TEST\",\"artifact-contents\":  "  
        + content + 
         " } ";
        String requestID ="0000";
        ArtifactHandlerClient ahi = new ArtifactHandlerClient();
        String value =  ahi.createArtifactData(payload, requestID);
        Assert.assertTrue(!value.isEmpty());
        }catch(Exception e)
        {
        }
    }
      
    @Ignore
    @Test
    public void testExecute(){

        try{
        String content = FileUtils.readFileToString(new File("src/test/resources/uploadArtifact"));
        String payload = " { \"userID\": \"00000\", \"vnf-type\" : \"DesigTest-VNF\", \"action\" : \"Configure\", \"artifact-name\":\"DesignRestArtifact_reference\",\"artifact-version\" :\"0.01\",\"artifact-type\" :\"DESIGNTOOL-TEST\",\"artifact-contents\":  "
        + content +
         " } ";
        String rpc = "Post";
        ArtifactHandlerClient ahi = new ArtifactHandlerClient();
        ahi.execute(payload, rpc);
        }catch(Exception e)
        {
        }
    }

    @Test
    public void testCreateArtifactData() throws Exception{

        String payload = " { \"userID\": \"00000\", \"vnf-type\" : \"DesigTest-VNF\", \"action\" : \"Configure\", \"artifact-name\":\"DesignRestArtifact_reference\",\"artifact-version\" :\"0.01\",\"artifact-type\" :\"DESIGNTOOL-TEST\",\"artifact-contents\":  \"TestContents\"} ";
        String requestID ="0000";
        ArtifactHandlerClient ahi = new ArtifactHandlerClient();
        String value =  ahi.createArtifactData(payload, requestID);
        assertEquals(true, value.contains("DesignRestArtifact_reference"));
    }

    @Test(expected = Exception.class)
    public void testvalidateClientResponse() throws Exception{

        ClientResponse clientResponse=null;
        ArtifactHandlerClient ahi = new ArtifactHandlerClient();
        Whitebox.invokeMethod(ahi, "validateClientResponse",clientResponse);
    }

}
