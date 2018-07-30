/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2018 IBM
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

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.appc.design.dbervices.DbService;
import org.onap.appc.design.dbervices.DesignDBService;
import org.onap.appc.design.services.util.ArtifactHandlerClient;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.reflect.Whitebox;

@RunWith(MockitoJUnitRunner.class)
@PrepareForTest(DesignDBService.class)
public class TestDesignDBServices {

    private DesignDBService designDbService;

    private DesignDBService spyDesign;

    @Mock
    private DbService dbservice;

    @Mock
    private ResultSet rs;

    @Mock
    private ArtifactHandlerClient artifactHandlerClient;

    @Before
    public void setUp() throws Exception {
        Map<String, String> outputMessage = new HashMap<>();
        designDbService = DesignDBService.initialise();
        Whitebox.setInternalState(designDbService, "dbservice", dbservice);
        // Whitebox.setInternalState(designDbService, "ac",
        // artifactHandlerClient);
        Mockito.doReturn(outputMessage).when(artifactHandlerClient).execute(Mockito.anyString(), Mockito.anyString());
        when(dbservice.getDBData(Mockito.anyString(), Mockito.anyList())).thenReturn(rs);
        when(rs.next()).thenReturn(true, true, false);
        Mockito.doReturn(true).when(dbservice).updateDBData(Mockito.anyString(), Mockito.anyList());

        spyDesign = Mockito.spy(designDbService);
    }

    @Test
    public void testSetStatus() throws Exception {
        String payload = "{\"userID\": \"1234\", \"vnf-type\" : \"DesigTest-VNF\",\"artifact_status\":\"TestArtifactStatus\",\"action_status\":\"TestAction\",\"vnfc-type\":\"TestVnfc\" }";
        String json = Whitebox.invokeMethod(spyDesign, "setStatus", payload, "1234");
        assertEquals(true, json.contains("1234"));
    }

    @Test
    public void testGetDesigns() throws Exception {
        String payload = "{\"userID\": \"0000\", \"vnf-type\" : \"DesigTest-VNF\",\"artifact_status\":\"TestArtifactStatus\",\"action_status\":\"TestAction\" }";
        String result = Whitebox.invokeMethod(spyDesign, "getDesigns", payload, "1234");
        assertEquals(true, result.contains("0000"));
    }

    @Test
    public void testGetDesignsWithFilter() throws Exception {
        String payload = "{\"userID\": \"0000\", \"vnf-type\" : \"DesigTest-VNF\",\"filter\":\"TestFilter\" }";
        String result = Whitebox.invokeMethod(spyDesign, "getDesigns", payload, "1234");
        assertEquals(true, result.contains("0000"));
    }

    @Test
    public void testSetInCartr() throws Exception {
        String payload = "{\"userID\": \"0000\", \"vnf-type\" : \"DesigTest-VNF\",\"inCart\":\"TestInCart\",\"vnfc-type\":\"TestVnfc\"}";
        String result = Whitebox.invokeMethod(spyDesign, "setInCart", payload, "1234");
        assertEquals(true, result.contains("success"));
    }

    @Test
    public void testSetProtocolReference() throws Exception {
        String payload = "{\"userID\": \"0000\", \"vnf-type\" : \"DesigTest-VNF\",\"action\":\"TestAction\",\"action-level\":\"TestLevel\",\"protocol\":\"TestProtocol\",\"vnfc-type\":\"TestVnfc\",\"template\":\"TestTemplate\"}";
        String result = Whitebox.invokeMethod(spyDesign, "setProtocolReference", payload, "1234");
        assertEquals(true, result.contains("success"));
    }

    @Ignore
    @Test(expected = ExceptionInInitializerError.class)
    public void TestUploadArtifact() throws Exception {
        String payload = " { \"userID\": \"00000\", \"vnf-type\" : \"DesigTest-VNF\", \"action\" : \"Configure\", \"artifact-name\":\"DesignRestArtifact_reference\",\"artifact-version\" :\"0.01\",\"artifact-type\" :\"DESIGNTOOL-TEST\",\"artifact-contents\":  \"TestContents\"} ";
        Whitebox.invokeMethod(spyDesign, "uploadArtifact", payload, "1234");
    }

    @Test
    public void testLinkstatusRelationShip() throws Exception {
        String payload = "{\"userID\": \"0000\", \"vnf-type\" : \"DesigTest-VNF\",\"action\":\"TestAction\",\"action-level\":\"TestLevel\",\"protocol\":\"TestProtocol\",\"vnfc-type\":\"TestVnfc\",\"template\":\"TestTemplate\"}";
        String result = Whitebox.invokeMethod(spyDesign, "linkstatusRelationShip", 0, 0, payload);
        assertEquals(null, result);
    }

    @Test
    public void testGetSDCReferenceID() throws Exception {
        String payload = "{\"userID\": \"0000\", \"vnf-type\" : \"DesigTest-VNF\",\"action\":\"TestAction\",\"action-level\":\"TestLevel\",\"protocol\":\"TestProtocol\",\"vnfc-type\":\"TestVnfc\",\"artifact-type\" :\"DESIGNTOOL-TEST\", \"artifact-name\":\"DesignRestArtifact_reference\"}";
        int result = Whitebox.invokeMethod(spyDesign, "getSDCReferenceID", payload);
        assertEquals(0, result);
    }

    @Test
    public void testgetDataFromActionStatus() throws Exception {
        String payload = "{\"userID\": \"0000\", \"vnf-type\" : \"DesigTest-VNF\",\"action\":\"TestAction\",\"action-level\":\"TestLevel\",\"protocol\":\"TestProtocol\",\"vnfc-type\":\"TestVnfc\",\"artifact-type\" :\"DESIGNTOOL-TEST\", \"artifact-name\":\"DesignRestArtifact_reference\"}";
        String result = Whitebox.invokeMethod(spyDesign, "getDataFromActionStatus", payload, "Data");
        assertEquals(null, result);
    }

    @Test
    public void testsetActionStatus() throws Exception {
        String payload = "{\"userID\": \"0000\", \"vnf-type\" : \"DesigTest-VNF\",\"action\":\"TestAction\",\"action-level\":\"TestLevel\",\"protocol\":\"TestProtocol\",\"vnfc-type\":\"TestVnfc\",\"template\":\"TestTemplate\"}";
        String result = Whitebox.invokeMethod(spyDesign, "setActionStatus", payload, "Status");
        assertEquals(null, result);
    }

    @Test
    public void testcreateArtifactTrackingRecord() throws Exception {
        String payload = "{\"userID\": \"0000\", \"vnf-type\" : \"DesigTest-VNF\",\"action\":\"TestAction\",\"technology\":\"TestTech\",\"action-level\":\"TestLevel\",\"protocol\":\"TestProtocol\",\"vnfc-type\":\"TestVnfc\",\"template\":\"TestTemplate\"}";
        String result = Whitebox.invokeMethod(spyDesign, "createArtifactTrackingRecord", payload, "1234", 0, 0);
        assertEquals(null, result);
    }

    @Test
    public void testgetSDCArtifactIDbyRequestID() throws Exception {
        String payload = "{\"userID\": \"0000\", \"vnf-type\" : \"DesigTest-VNF\",\"action\":\"TestAction\",\"action-level\":\"TestLevel\",\"protocol\":\"TestProtocol\",\"vnfc-type\":\"TestVnfc\",\"artifact-type\" :\"DESIGNTOOL-TEST\", \"artifact-name\":\"DesignRestArtifact_reference\"}";
        int result = Whitebox.invokeMethod(spyDesign, "getSDCArtifactIDbyRequestID", payload);
        assertEquals(0, result);
    }

    @Test(expected = Exception.class)
    public void testgetArtifact() throws Exception {
        String payload = "{\"userID\": \"0000\", \"vnf-type\" : \"DesigTest-VNF\",\"action\":\"TestAction\",\"action-level\":\"TestLevel\",\"protocol\":\"TestProtocol\",\"vnfc-type\":\"TestVnfc\",\"artifact-type\" :\"DESIGNTOOL-TEST\", \"artifact-name\":\"DesignRestArtifact_reference\"}";
        String result = Whitebox.invokeMethod(spyDesign, "getArtifact", payload, "1234");
    }

    @Test
    public void testgetStatus() throws Exception {
        String payload = "{\"userID\": \"0000\", \"vnf-type\" : \"DesigTest-VNF\",\"action\":\"TestAction\",\"action-level\":\"TestLevel\",\"protocol\":\"TestProtocol\",\"vnfc-type\":\"TestVnfc\",\"artifact-type\" :\"DESIGNTOOL-TEST\", \"artifact-name\":\"DesignRestArtifact_reference\"}";
        String result = Whitebox.invokeMethod(spyDesign, "getStatus", payload, "1234");
        assertEquals(true, result.contains("0000"));
    }

    @Test
    public void testGetAppcTimestampUTC() throws Exception {
             String requestId = "1234";
             DesignDBService design = DesignDBService.initialise();
             String result =  Whitebox.invokeMethod(design, "getAppcTimestampUTC",requestId);
             assertTrue(result.endsWith("Z"));
    }
}