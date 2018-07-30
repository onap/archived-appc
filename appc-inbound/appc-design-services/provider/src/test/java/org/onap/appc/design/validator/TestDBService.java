/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Modifications Copyright (C) 2018 IBM
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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.sql.ResultSet;

import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import org.onap.appc.design.dbervices.DbService;
import org.onap.appc.design.dbervices.DesignDBService;
import org.onap.appc.design.propertyServices.PropertyUpdateService;
import org.powermock.api.mockito.PowerMockito;

import org.powermock.reflect.Whitebox;
import org.slf4j.LoggerFactory;


public class TestDBService {
private final org.slf4j.Logger logger = LoggerFactory.getLogger(TestDBService.class);

    @Ignore("Test is taking 60 seconds")
    @Test
    public void testGetDesigns() {
        try {
            String payload = "{\"userID\": \"0000\", \"vnf-type\" : \"DesigTest-VNF\" }";
            DesignDBService dbservice = DesignDBService.initialise();
            dbservice.execute("getDesigns", payload, "1234");
        } catch (Exception e) {
        }
    }

    @Ignore("Test is taking 120 seconds")
    @Test
    public void testGetStatus() {
        try {
            String content = FileUtils.readFileToString(new File("src/test/resources/uploadArtifact"));
            String payload = " { \"userID\": \"0000\", \"vnf-type\" : \"DesigTest-VNF\", \"action\" : \"Configure\", \"artifact-name\":\"DesignRestArtifact_reference\",\"artifact-version\" :\"0.01\",\"artifact-type\" :\"DESIGNTOOL-TEST\",\"artifact-contents\":  "+ content + " } ";
            DesignDBService dbservice = DesignDBService.initialise();
            DbService db = new DbService();
            Whitebox.invokeMethod(db, "getDbLibService");
            dbservice.execute("getStatus", payload, "1234");
        } catch (Exception e) {
        }
    }

    @Ignore("Test is taking 120 seconds")
    @Test
    public void testUploadArtifact() {
        try {
            String content = FileUtils.readFileToString(new File("src/test/resources/uploadArtifact"));
            String payload = " { \"userID\": \"0000\", \"vnf-type\" : \"DesigTest-VNF\", \"action\" : \"Configure\", \"artifact-name\":\"DesignRestArtifact_reference\",\"artifact-version\" :\"0.01\",\"artifact-type\" :\"DESIGNTOOL-TEST\",\"artifact-contents\":  "+ content + " } ";
            DesignDBService dbservice = DesignDBService.initialise();
            DbService db = new DbService();
            Whitebox.invokeMethod(db, "getDbLibService");
            dbservice.execute("uploadArtifact", payload, "1234");
        } catch (Exception e) {
        }
    }

    @Ignore("Test is taking 120 seconds")
    @Test
    public void testGetArtifact() {
        try {
            String content = FileUtils.readFileToString(new File("src/test/resources/uploadArtifact"));
            String payload = " { \"userID\": \"0000\", \"vnf-type\" : \"DesigTest-VNF\", \"action\" : \"Configure\", \"artifact-name\":\"DesignRestArtifact_reference\",\"artifact-version\" :\"0.01\",\"artifact-type\" :\"DESIGNTOOL-TEST\",\"artifact-contents\":  "+ content + " } ";
            DesignDBService design = DesignDBService.initialise();
            DbService db = new DbService();
            Whitebox.invokeMethod(db, "getDbLibService");
            design.execute("getArtifact", payload, "1234");
        } catch (Exception e) {
        }
    }

    @Ignore("Test is taking 120 seconds")
    @Test
    public void testSetIncart() {
        try {
            String content = FileUtils.readFileToString(new File("src/test/resources/uploadArtifact"));
            String payload = " { \"userID\": \"0000\",\"action-level\":\"VNf\",\"protocol\":\"Test\", \"inCart\":\"Y\",\"vnf-type\" : \"DesigTest-VNF\", \"action\" : \"Configure\", \"artifact-name\":\"DesignRestArtifact_reference\",\"artifact-version\" :\"0.01\",\"artifact-type\" :\"DESIGNTOOL-TEST\",\"artifact-contents\":  "+ content + " } ";
            DesignDBService design = DesignDBService.initialise();
            DbService db = new DbService();
            Whitebox.invokeMethod(db, "getDbLibService");
            design.execute("setInCart", payload, "1234");
        } catch (Exception e) {
        }
    }

    @Ignore("Test is taking 120 seconds")
    @Test
    public void testSetProtocolReference() {
        try {
            String content = FileUtils.readFileToString(new File("src/test/resources/uploadArtifact"));
            String payload = " { \"userID\": \"0000\",\"action-level\":\"VNf\",\"protocol\":\"Test\", \"vnf-type\" : \"DesigTest-VNF\", \"action\" : \"Configure\", \"artifact-name\":\"DesignRestArtifact_reference\",\"artifact-version\" :\"0.01\",\"artifact-type\" :\"DESIGNTOOL-TEST\",\"artifact-contents\":  "+ content + " } ";
            DesignDBService design = DesignDBService.initialise();
            DbService db = new DbService();
            Whitebox.invokeMethod(db, "getDbLibService");
            design.execute("setProtocolReference", payload, "1234");
        } catch (Exception e) {
        }
    }

    @Ignore("Test is taking 120 seconds")
    @Test
    public void testSetStatus() {
        try {
            String content = FileUtils.readFileToString(new File("src/test/resources/uploadArtifact"));
            String payload = " { \"userID\": \"0000\",\"status\":\"Test\", \"vnf-type\" : \"DesigTest-VNF\", \"action\" : \"Configure\", \"artifact-name\":\"DesignRestArtifact_reference\",\"artifact-version\" :\"0.01\",\"artifact-type\" :\"DESIGNTOOL-TEST\",\"artifact-status\":\"\",\"artifact-contents\":  "+ content + " } ";
            DesignDBService design = DesignDBService.initialise();
            DbService db = new DbService();
            Whitebox.invokeMethod(db, "getDbLibService");
            design.execute("setStatus", payload, "1234");
        } catch (Exception e) {
        }
    }

    @Ignore("Test is taking 120 seconds")
    @Test
    public void testGetArtifactReference() {
        try {
            String content = FileUtils.readFileToString(new File("src/test/resources/uploadArtifact"));
            String payload = " { \"userID\": \"0000\",\"status\":\"Test\", \"vnf-type\" : \"DesigTest-VNF\", \"action\" : \"Configure\", \"artifact-name\":\"DesignRestArtifact_reference\",\"artifact-version\" :\"0.01\",\"artifact-type\" :\"DESIGNTOOL-TEST\",\"artifact-status\":\"\",\"artifact-contents\":  "+ content + " } ";
            DesignDBService design = DesignDBService.initialise();
            DbService db = new DbService();
            Whitebox.invokeMethod(db, "getDbLibService");
            design.execute("getArtifactReference", payload, "1234");
        } catch (Exception e) {
        }
    }

    @Ignore("Test is taking 120 seconds")
    @Test
    public void testGetGuiReference() {
        try {
            String content = FileUtils.readFileToString(new File("src/test/resources/uploadArtifact"));
            String payload = " { \"userID\": \"0000\",\"status\":\"Test\", \"vnf-type\" : \"DesigTest-VNF\", \"action\" : \"Configure\", \"artifact-name\":\"DesignRestArtifact_reference\",\"artifact-version\" :\"0.01\",\"artifact-type\" :\"DESIGNTOOL-TEST\",\"artifact-status\":\"\",\"artifact-contents\":  "+ content + " } ";
            DesignDBService design = DesignDBService.initialise();
            DbService db = new DbService();
            Whitebox.invokeMethod(db, "getDbLibService");
            design.execute("getGuiReference", payload, "1234");
        } catch (Exception e) {
        }
    }

    @Test
    public void testPropertyUpdateService() {
        PropertyUpdateService ps = new PropertyUpdateService();
    }

    @Ignore
    @Test
    public void testLinkstatusRelationShip() {        
        try {
            String content = FileUtils.readFileToString(new File("src/test/resources/uploadArtifact"));
            String payload = " { \"userID\": \"0000\",\"status\":\"Test\", \"vnf-type\" : \"DesigTest-VNF\", \"action\" : \"Configure\", \"artifact-name\":\"DesignRestArtifact_reference\",\"artifact-version\" :\"0.01\",\"artifact-type\" :\"DESIGNTOOL-TEST\",\"artifact-status\":\"\",\"artifact-contents\":  "+ content + " } ";
            DesignDBService design = DesignDBService.initialise();
            Whitebox.invokeMethod(design, "linkstatusRelationShip", 1, 1, payload);
        } catch (Exception e) {
        }
    }

    @Ignore
    @Test
    public void testGetSDCReferenceID() {
        
        try {
            String content = FileUtils.readFileToString(new File("src/test/resources/uploadArtifact"));
            String payload = " { \"userID\": \"0000\",\"status\":\"Test\", \"vnf-type\" : \"DesigTest-VNF\", \"action\" : \"Configure\", \"artifact-name\":\"DesignRestArtifact_reference\",\"artifact-version\" :\"0.01\",\"artifact-type\" :\"DESIGNTOOL-TEST\",\"artifact-status\":\"\",\"artifact-contents\":  "+ content + " } ";
            DesignDBService design = DesignDBService.initialise();
            Whitebox.invokeMethod(design, "getSDCReferenceID", payload);
        } catch (Exception e) {
        }
    }

    @Ignore
    @Test
    public void testGetDataFromActionStatus() {        
        try {
            String content = FileUtils.readFileToString(new File("src/test/resources/uploadArtifact"));
            String payload = " { \"userID\": \"0000\",\"status\":\"Test\", \"vnf-type\" : \"DesigTest-VNF\", \"action\" : \"Configure\", \"artifact-name\":\"DesignRestArtifact_reference\",\"artifact-version\" :\"0.01\",\"artifact-type\" :\"DESIGNTOOL-TEST\",\"artifact-status\":\"\",\"artifact-contents\":  "+ content + " } ";
            DesignDBService design = DesignDBService.initialise();
            Whitebox.invokeMethod(design, "getDataFromActionStatus", payload, "Test");
        } catch (Exception e) {
        }
    }

    @Ignore
    @Test
    public void testSetActionStatus() {
        try {
            String content = FileUtils.readFileToString(new File("src/test/resources/uploadArtifact"));
            String payload = " { \"userID\": \"0000\",\"status\":\"Test\", \"vnf-type\" : \"DesigTest-VNF\", \"action\" : \"Configure\", \"artifact-name\":\"DesignRestArtifact_reference\",\"artifact-version\" :\"0.01\",\"artifact-type\" :\"DESIGNTOOL-TEST\",\"artifact-status\":\"\",\"artifact-contents\":  "+ content + " } ";
            DesignDBService design = DesignDBService.initialise();
            Whitebox.invokeMethod(design, "setActionStatus", payload, "Accepted");
        } catch (Exception e) {
        }
    }
 
    @Ignore
    @Test
    public void testGetSDCArtifactIDbyRequestID() {
        try {
            String content = FileUtils.readFileToString(new File("src/test/resources/uploadArtifact"));
            String payload = " { \"userID\": \"0000\",\"status\":\"Test\", \"vnf-type\" : \"DesigTest-VNF\", \"action\" : \"Configure\", \"artifact-name\":\"DesignRestArtifact_reference\",\"artifact-version\" :\"0.01\",\"artifact-type\" :\"DESIGNTOOL-TEST\",\"artifact-status\":\"\",\"artifact-contents\":  "+ content + " } ";
            DesignDBService design = DesignDBService.initialise();
            Whitebox.invokeMethod(design, "getSDCArtifactIDbyRequestID", "0");
        } catch (Exception e) {
        }
    }

    @Ignore
    @Test
    public void testCreateArtifactTrackingRecord() {
            try {
            String content = FileUtils.readFileToString(new File("src/test/resources/uploadArtifact"));
            String payload = " { \"userID\": \"0000\",\"status\":\"Test\", \"vnf-type\" : \"DesigTest-VNF\", \"action\" : \"Configure\", \"artifact-name\":\"DesignRestArtifact_reference\",\"artifact-version\" :\"0.01\",\"artifact-type\" :\"DESIGNTOOL-TEST\",\"artifact-status\":\"\",\"artifact-contents\":  "+ content + " } ";
            DesignDBService design = DesignDBService.initialise();
            Whitebox.invokeMethod(design, "createArtifactTrackingRecord",payload,"0",1,1);
        } catch (Exception e) {
        }
    }

    @Test
    public void testGetAppcTimestampUTC() throws Exception {
             String requestId = "1234";
             DesignDBService design = DesignDBService.initialise();
             String result =  Whitebox.invokeMethod(design, "getAppcTimestampUTC",requestId);
             assertTrue(result.endsWith("Z"));
    }
    
}