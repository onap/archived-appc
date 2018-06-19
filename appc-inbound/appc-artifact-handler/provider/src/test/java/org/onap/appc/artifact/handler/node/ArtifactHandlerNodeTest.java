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

package org.onap.appc.artifact.handler.node;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.powermock.reflect.Whitebox;
import org.onap.appc.artifact.handler.dbservices.MockDBService;
import org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants;
import org.onap.appc.artifact.handler.utils.ArtifactHandlerProviderUtilTest;
import java.util.Map;
import java.util.HashMap;
import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ArtifactHandlerNodeTest {

    private ArtifactHandlerNode artifactHandlerNode;

    @Before
    public void setUp() throws Exception {
        artifactHandlerNode = Mockito.spy(ArtifactHandlerNode.class);
        Mockito.doReturn(true)
            .when(artifactHandlerNode)
            .updateStoreArtifacts(Mockito.any(JSONObject.class), Mockito.any(JSONObject.class));
        Mockito.doReturn(true)
            .when(artifactHandlerNode)
            .storeReferenceData(Mockito.any(JSONObject.class), Mockito.any(JSONObject.class));
    }

    @Test
    public void testProcessArtifact() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        Map<String, String> inParams = new HashMap<>();
        JSONObject postData = new JSONObject();
        JSONObject input = new JSONObject();
        inParams.put("response_prefix", "prefix");
        JSONObject requestInfo = new JSONObject();
        JSONObject documentInfo = new JSONObject();
        String artifactContent = IOUtils.toString(ArtifactHandlerProviderUtilTest.class.getClassLoader()
                .getResourceAsStream("templates/reference_template"), Charset.defaultCharset());
        documentInfo.put(SdcArtifactHandlerConstants.ARTIFACT_CONTENTS, artifactContent);
        documentInfo.put(SdcArtifactHandlerConstants.ARTIFACT_NAME, "reference_Junit.json");
        requestInfo.put("RequestInfo", "testValue");
        input.put(SdcArtifactHandlerConstants.DOCUMENT_PARAMETERS, documentInfo);
        input.put(SdcArtifactHandlerConstants.REQUEST_INFORMATION, requestInfo);
        postData.put("input", input);
        inParams.put("postData", postData.toString());
        artifactHandlerNode.processArtifact(inParams, ctx);
    }

    @Ignore("Test is taking 60 seconds")
    @Test(expected = Exception.class)
    public void testStoreReferenceData() throws Exception {
        JSONObject documentInfo = new JSONObject();
        String artifactContent = IOUtils.toString(ArtifactHandlerProviderUtilTest.class.getClassLoader()
                .getResourceAsStream("templates/reference_template"), Charset.defaultCharset());
        documentInfo.put(SdcArtifactHandlerConstants.ARTIFACT_CONTENTS, artifactContent);
        documentInfo.put(SdcArtifactHandlerConstants.ARTIFACT_NAME, "reference_Junit.json");
        JSONObject requestInfo = new JSONObject();
        requestInfo.put("RequestInfo", "testStoreReferenceData");
        artifactHandlerNode.storeReferenceData(requestInfo, documentInfo);
    }

    @Test
    public void testPopulateProtocolReference() throws Exception {
        ArtifactHandlerNode ah = new ArtifactHandlerNode();
        String contentStr =
                "{\"action\": \"TestAction\",\"action-level\": \"vnf\",\"scope\": {\"vnf-type\": \"vDBE-I\",\"vnfc-type\": null},\"template\": \"N\",\"device-protocol\": \"REST\"}";
        JSONObject content = new JSONObject(contentStr);
        MockDBService dbService = MockDBService.initialise();
        Whitebox.invokeMethod(ah, "populateProtocolReference", dbService, content);
    }

    @Test
    public void testProcessAndStoreCapablitiesArtifact() throws Exception {
        ArtifactHandlerNode ah = new ArtifactHandlerNode();
        JSONObject capabilities = new JSONObject();
        JSONObject documentInfo = new JSONObject();
        MockDBService dbService = MockDBService.initialise();
        documentInfo.put(SdcArtifactHandlerConstants.SERVICE_UUID, "testuid");
        documentInfo.put(SdcArtifactHandlerConstants.DISTRIBUTION_ID, "testDist");
        documentInfo.put(SdcArtifactHandlerConstants.SERVICE_NAME, "testName");
        documentInfo.put(SdcArtifactHandlerConstants.SERVICE_DESCRIPTION, "testDesc");
        documentInfo.put(SdcArtifactHandlerConstants.RESOURCE_UUID, "testRes");
        documentInfo.put(SdcArtifactHandlerConstants.RESOURCE_INSTANCE_NAME, "testResIns");
        documentInfo.put(SdcArtifactHandlerConstants.RESOURCE_VERSION, "testVers");
        documentInfo.put(SdcArtifactHandlerConstants.RESOURCE_TYPE, "testResType");
        documentInfo.put(SdcArtifactHandlerConstants.ARTIFACT_UUID, "testArtifactUuid");
        documentInfo.put(SdcArtifactHandlerConstants.ARTIFACT_VERSION, "testArtifactVers");
        documentInfo.put(SdcArtifactHandlerConstants.ARTIFACT_DESRIPTION, "testArtifactDesc");
        Whitebox.invokeMethod(ah, "processAndStoreCapabilitiesArtifact", dbService, documentInfo, capabilities,
                "artifactName", "someVnf");
    }

    @Test
    public void testCleanVnfcInstance() throws Exception {
        ArtifactHandlerNode ah = new ArtifactHandlerNode();
        SvcLogicContext ctx = new SvcLogicContext();
        Whitebox.invokeMethod(ah, "cleanVnfcInstance", ctx);
        assertTrue(true);
    }

    @Ignore("Test is taking 60 seconds")
    @Test(expected = Exception.class)
    public void testGetArtifactIDException() throws Exception {
        ArtifactHandlerNode ah = new ArtifactHandlerNode();
        String yFileName = "yFileName";
        Whitebox.invokeMethod(ah, "getArtifactID", yFileName);
    }

    @Ignore("Test is taking 60 seconds")
    @Test(expected = Exception.class)
    public void testStoreUpdateSdcArtifacts() throws Exception {
        ArtifactHandlerNode ah = new ArtifactHandlerNode();
        String postDataStr =
                "{\"request-information\":{},\"document-parameters\":{\"artifact-name\":\"testArtifact\",\"artifact-contents\":{\"content\":\"TestContent\"}}}";
        JSONObject postData = new JSONObject(postDataStr);
        Whitebox.invokeMethod(ah, "storeUpdateSdcArtifacts", postData);
    }

    @Ignore("Test is taking 60 seconds")
    @Test(expected = Exception.class)
    public void testUpdateStoreArtifacts() throws Exception {
        JSONObject documentInfo = new JSONObject();
        String artifactContent = IOUtils.toString(ArtifactHandlerProviderUtilTest.class.getClassLoader()
                .getResourceAsStream("templates/reference_template"), Charset.defaultCharset());
        documentInfo.put(SdcArtifactHandlerConstants.ARTIFACT_CONTENTS, artifactContent);
        documentInfo.put(SdcArtifactHandlerConstants.ARTIFACT_NAME, "reference_Junit.json");
        JSONObject requestInfo = new JSONObject();
        requestInfo.put("RequestInfo", "testupdateStoreArtifacts");
        artifactHandlerNode.updateStoreArtifacts(requestInfo, documentInfo);
    }

    @Test
    public void testCleanArtifactInstanceData() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        Whitebox.invokeMethod(artifactHandlerNode, "cleanArtifactInstanceData", ctx);
    }

    @Ignore("Test is taking 60 seconds")
    @Test(expected = Exception.class)
    public void testUpdateYangContents() throws Exception {
        String artifactId = "1";
        String yangContents = "SomeContent";
        Whitebox.invokeMethod(artifactHandlerNode, "updateYangContents", artifactId, yangContents);
    }

    @Test
    public void testProcessVmList() throws Exception{
        String contentStr = "{\r\n\t\"action\": \"ConfigScaleOut\",\r\n\t\"action-level\": \"VNF\",\r\n\t\"scope\": "
                + "{\r\n\t\t\"vnf-type\": \"ScaleOutVNF\",\r\n\t\t\"vnfc-type\": \"\"\r\n\t},\r\n\t\"template\": \"Y\",\r\n\t\"vm\": "
                + "[\r\n\t{ \r\n\t\t\"vm-instance\": 1,\r\n\t\t\"template-id\":\"id1\",\r\n\t\t\r\n\t\t\"vnfc\": [{\r\n\t\t\t\"vnfc-instance\": 1,\r\n\t\t\t\"vnfc-type\": \"t1\",\r\n\t\t\t\"vnfc-function-code\": "
                + "\"Testdbg\",\r\n\t\t\t\"group-notation-type\": \"GNType\",\r\n\t\t\t\"ipaddress-v4-oam-vip\": \"N\",\r\n\t\t\t\"group-notation-value\": "
                + "\"GNValue\"\r\n\t\t}]\r\n\t},\r\n\t{ \r\n\t\t\"vm-instance\": 1,\r\n\t\t\"template-id\":\"id2\",\r\n\t\t\r\n\t\t\"vnfc\": [{\r\n\t\t\t\"vnfc-instance\": 1,\r\n\t\t\t\"vnfc-type\": "
                + "\"t1\",\r\n\t\t\t\"vnfc-function-code\": \"Testdbg\",\r\n\t\t\t\"group-notation-type\": \"GNType\",\r\n\t\t\t\"ipaddress-v4-oam-vip\": \"N\",\r\n\t\t\t\"group-notation-value\": "
                + "\"GNValue\"\r\n\t\t},\r\n\t\t{\r\n\t\t\t\"vnfc-instance\": 2,\r\n\t\t\t\"vnfc-type\": \"t2\",\r\n\t\t\t\"vnfc-function-code\": \"Testdbg\",\r\n\t\t\t\"group-notation-type\": "
                + "\"GNType\",\r\n\t\t\t\"ipaddress-v4-oam-vip\": \"Y\",\r\n\t\t\t\"group-notation-value\": \"GNValue\"\r\n\t\t}]\r\n\t},\r\n\t{\r\n\t\t\"vm-instance\": 2,\r\n\t\t\"template-id\":\"id3\",\r\n\t\t\"vnfc\": "
                + "[{\r\n\t\t\t\"vnfc-instance\": 1,\r\n\t\t\t\"vnfc-type\": \"t3\",\r\n\t\t\t\"vnfc-function-code\": \"Testdbg\",\r\n\t\t\t\"group-notation-type\": "
                + "\"GNType\",\r\n\t\t\t\"ipaddress-v4-oam-vip\": \"Y\",\r\n\t\t\t\"group-notation-value\": \"GNValue\"\r\n\t\t}]\r\n\t}],\r\n\t\"device-protocol\": "
                + "\"TEST-PROTOCOL\",\r\n\t\"user-name\": \"Testnetconf\",\r\n\t\"port-number\": \"22\",\r\n\t\"artifact-list\": [{\r\n\t\t\"artifact-name\": \"Testv_template.json\",\r\n\t\t\"artifact-type\": "
                + "\"Testconfig_template\"\r\n\t},\r\n\t{\r\n\t\t\"artifact-name\": \"TESTv_parameter_definitions.json\",\r\n\t\t\"artifact-type\": \"Testparameter_definitions\"\r\n\t},\r\n\t{\r\n\t\t\"artifact-name\": "
                + "\"PD_JunitTESTv_parameter_yang.json\",\r\n\t\t\"artifact-type\": \"PD_definations\"\r\n\t}]\r\n}";
        JSONObject content=new JSONObject(contentStr);
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext context = new SvcLogicContext();
        artifactHandlerNode.processVmList(content, context, dbService);
    }

    @Test
    public void testProcessConfigTypeActions() throws Exception{
        String contentStr = "{\"action\": \"ConfigScaleOut\"}";
        JSONObject content=new JSONObject(contentStr);
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute(SdcArtifactHandlerConstants.DEVICE_PROTOCOL,"Test");
        artifactHandlerNode.processConfigTypeActions(content, dbService, context);
    }

    @Test
    public void testProcessArtifactLists() throws Exception{
        String contentStr = "{\r\n\t\"action\": \"ConfigScaleOut\",\r\n\t\"action-level\": \"VNF\",\r\n\t\"scope\": "
                + "{\r\n\t\t\"vnf-type\": \"ScaleOutVNF\",\r\n\t\t\"vnfc-type\": \"\"\r\n\t},\r\n\t\"template\": \"Y\",\r\n\t\"vm\": "
                + "[\r\n\t{ \r\n\t\t\"vm-instance\": 1,\r\n\t\t\"template-id\":\"id1\",\r\n\t\t\r\n\t\t\"vnfc\": [{\r\n\t\t\t\"vnfc-instance\": 1,\r\n\t\t\t\"vnfc-type\": \"t1\",\r\n\t\t\t\"vnfc-function-code\": "
                + "\"Testdbg\",\r\n\t\t\t\"group-notation-type\": \"GNType\",\r\n\t\t\t\"ipaddress-v4-oam-vip\": \"N\",\r\n\t\t\t\"group-notation-value\": "
                + "\"GNValue\"\r\n\t\t}]\r\n\t},\r\n\t{ \r\n\t\t\"vm-instance\": 1,\r\n\t\t\"template-id\":\"id2\",\r\n\t\t\r\n\t\t\"vnfc\": [{\r\n\t\t\t\"vnfc-instance\": 1,\r\n\t\t\t\"vnfc-type\": "
                + "\"t1\",\r\n\t\t\t\"vnfc-function-code\": \"Testdbg\",\r\n\t\t\t\"group-notation-type\": \"GNType\",\r\n\t\t\t\"ipaddress-v4-oam-vip\": \"N\",\r\n\t\t\t\"group-notation-value\": "
                + "\"GNValue\"\r\n\t\t},\r\n\t\t{\r\n\t\t\t\"vnfc-instance\": 2,\r\n\t\t\t\"vnfc-type\": \"t2\",\r\n\t\t\t\"vnfc-function-code\": \"Testdbg\",\r\n\t\t\t\"group-notation-type\": "
                + "\"GNType\",\r\n\t\t\t\"ipaddress-v4-oam-vip\": \"Y\",\r\n\t\t\t\"group-notation-value\": \"GNValue\"\r\n\t\t}]\r\n\t},\r\n\t{\r\n\t\t\"vm-instance\": 2,\r\n\t\t\"template-id\":\"id3\",\r\n\t\t\"vnfc\": "
                + "[{\r\n\t\t\t\"vnfc-instance\": 1,\r\n\t\t\t\"vnfc-type\": \"t3\",\r\n\t\t\t\"vnfc-function-code\": \"Testdbg\",\r\n\t\t\t\"group-notation-type\": "
                + "\"GNType\",\r\n\t\t\t\"ipaddress-v4-oam-vip\": \"Y\",\r\n\t\t\t\"group-notation-value\": \"GNValue\"\r\n\t\t}]\r\n\t}],\r\n\t\"device-protocol\": "
                + "\"TEST-PROTOCOL\",\r\n\t\"user-name\": \"Testnetconf\",\r\n\t\"port-number\": \"22\",\r\n\t\"artifact-list\": [{\r\n\t\t\"artifact-name\": \"Testv_template.json\",\r\n\t\t\"artifact-type\": "
                + "\"Testconfig_template\"\r\n\t},\r\n\t{\r\n\t\t\"artifact-name\": \"TESTv_parameter_definitions.json\",\r\n\t\t\"artifact-type\": \"Testparameter_definitions\"\r\n\t},\r\n\t{\r\n\t\t\"artifact-name\": "
                + "\"PD_JunitTESTv_parameter_yang.json\",\r\n\t\t\"artifact-type\": \"PD_definations\"\r\n\t}]\r\n}";
        JSONObject content=new JSONObject(contentStr);
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext context = new SvcLogicContext();
        artifactHandlerNode.processArtifactList(content,dbService,context, null);
     }

    @Test
    public void testProcessActionLists() throws Exception {
        String contentStr = "{\r\n\t\"action\": \"HealthCheck\",\r\n\t\"action-level\": \"vm\",\r\n\t\"scope\":"
                + " {\r\n\t\t\"vnf-type\": \"vDBE-I\",\r\n\t\t\"vnfc-type\": null\r\n\t},\r\n\t\"template\": "
                + "\"N\",\r\n\t\"device-protocol\": \"REST\",\r\n\t\"vnfc-function-code-list\": [\"SSC\", \"MMSC\"]\r\n}";
        JSONObject content = new JSONObject(contentStr);
        JSONArray vmActionVnfcFunctionCodesList = new JSONArray();
        JSONArray vnfActionList = new JSONArray();
        JSONArray vfModuleActionList = new JSONArray();
        JSONArray vnfcActionList = new JSONArray();
        String[] actionLevels = { "vnf", "vm", "vf-module", "vnfc" };
        for (String actionLevel : actionLevels) {
            artifactHandlerNode.processActionLists(content, actionLevel, vnfcActionList, vfModuleActionList,
                    vnfActionList, vmActionVnfcFunctionCodesList);
        }
    }

    @Test
    public void testIsCapabilityArtifactNeeded() throws Exception {
        String scopeObjStr1= "{\"vnf-type\":\"someVnf\",\"vnfc-type\":\"somVnfc\"}";
        String scopeObjStr2= "{\"vnf-type\":\"someVnf\",\"vnfc-type\":\"\"}";
        JSONObject scope1 = new JSONObject(scopeObjStr1);
        JSONObject scope2 = new JSONObject(scopeObjStr2);
        SvcLogicContext context = new SvcLogicContext();
        artifactHandlerNode.setVnfcTypeInformation(scope1, context);
        assertFalse(artifactHandlerNode.isCapabilityArtifactNeeded(context));
        artifactHandlerNode.setVnfcTypeInformation(scope2, context);
        assertTrue(artifactHandlerNode.isCapabilityArtifactNeeded(context));
    }

    @Test
    public void testProcessArtifactListsWithMultipleTemplates() throws Exception {
        String contentStr = "{\r\n\t\t\"action\": \"ConfigScaleOut\",\r\n\t\t\"action-level\": \"vnf\",\r\n\t\t\"scope\": {\r\n\t\t\t\"vnf-type\": "
                + "\"vCfgSO-0405\",\r\n\t\t\t\"vnfc-type\": \"\"\r\n\t\t},\r\n\t\t\"template\": \"Y\",\r\n\t\t\"vm\": [{\r\n\t\t\t\"template-id\": "
                + "\"TID-0405-EZ\",\r\n\t\t\t\"vm-instance\": 1,\r\n\t\t\t\"vnfc\": [{\r\n\t\t\t\t\"vnfc-instance\": \"1\",\r\n\t\t\t\t\"vnfc-function-code\": "
                + "\"Cfg-ez\",\r\n\t\t\t\t\"ipaddress-v4-oam-vip\": \"Y\",\r\n\t\t\t\t\"group-notation-type\": \"first-vnfc-name\",\r\n\t\t\t\t\"group-notation-value\":"
                + " \"pair\",\r\n\t\t\t\t\"vnfc-type\": \"vCfg-0405-ez\"\r\n\t\t\t}]\r\n\t\t},\r\n\t\t{\r\n\t\t\t\"template-id\": "
                + "\"TID-0405-EZ\",\r\n\t\t\t\"vm-instance\": 2,\r\n\t\t\t\"vnfc\": [{\r\n\t\t\t\t\"vnfc-instance\": \"1\",\r\n\t\t\t\t\"vnfc-function-code\": "
                + "\"Cfg-ez\",\r\n\t\t\t\t\"ipaddress-v4-oam-vip\": \"Y\",\r\n\t\t\t\t\"group-notation-type\": \"first-vnfc-name\",\r\n\t\t\t\t\"group-notation-value\":"
                + " \"pair\",\r\n\t\t\t\t\"vnfc-type\": \"vCfg-0405-ez\"\r\n\t\t\t}]\r\n\t\t}],\r\n\t\t\"device-protocol\": \"ANSIBLE\",\r\n\t\t\"user-name\": \"root\","
                + "\r\n\t\t\"port-number\": \"22\",\r\n\t\t\"artifact-list\": [{\r\n\t\t\t\"artifact-name\": \"template_ConfigScaleOut_vCfgSO-0405_0.0.1V_TID-0405-EZ.json\","
                + "\r\n\t\t\t\"artifact-type\": \"config_template\"\r\n\t\t},\r\n\t\t{\r\n\t\t\t\"artifact-name\": \"pd_ConfigScaleOut_vCfgSO-0405_0.0.1V_TID-0405-EZ.yaml\","
                + "\r\n\t\t\t\"artifact-type\": \"parameter_definitions\"\r\n\t\t},\r\n\t\t{\r\n\t\t\t\"artifact-name\": "
                + "\"template_ConfigScaleOut_vCfgSO-0405_0.0.1V_TID-0405-EZ-2.json\",\r\n\t\t\t\"artifact-type\": \"config_template\"\r\n\t\t},\r\n\t\t{\r\n\t\t\t\"artifact-name\": "
                + "\"pd_ConfigScaleOut_vCfgSO-0405_0.0.1V_TID-0405-EZ-2.yaml\",\r\n\t\t\t\"artifact-type\": \"parameter_definitions\"\r\n\t\t}],\r\n\t\t\"template-id-list\":"
                + " [\"TID-0405-EZ\",\r\n\t\t\"TID-0405-EZ-2\"],\r\n\t\t\"scopeType\": \"vnf-type\"\r\n\t}";
        JSONObject content = new JSONObject(contentStr);
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("vnf-type", "someVnf");
        context.setAttribute("action", "ConfigScaleOut");
        artifactHandlerNode.processArtifactList(content, dbService, context, null);
    }

    @Test
    public void testProcessArtifactListsWithVnfcTypeList() throws Exception {
        String contentStr = "{\r\n\t\"action\": \"Configure\",\r\n\t\"action-level\": \"vnf\",\r\n\t\"scope\": {\r\n\t\t\"vnf-type\": "
                + "\"newtypeofvnf\",\r\n\t\t\"vnfc-type-list\": [\"vnfctype1\",\"vnfctype2\"]\r\n\t},\r\n\t\"template\": \"Y\",\r\n\t\"vm\":"
                + " [{\r\n\t\t\t\"vm-instance\": 1,\r\n\t\t\t\"template-id\": \"vnfctype1\",\r\n\t\t\t\"vnfc\": [{\r\n\t\t\t\t\"vnfc-instance\": "
                + "\"1\",\r\n\t\t\t\t\"vnfc-function-code\": \"fcx\",\r\n\t\t\t\t\"ipaddress-v4-oam-vip\": \"Y\",\r\n\t\t\t\t\"group-notation-type\": "
                + "\"first-vnfc-name\",\r\n\t\t\t\t\"group-notation-value\": \"pair\",\r\n\t\t\t\t\"vnfc-type\": \"vDBE\"\r\n\t\t\t}]\r\n\t\t},"
                + "\r\n\t\t{\r\n\t\t\t\"vm-instance\": 1,\r\n\t\t\t\"template-id\": \"vnfctype2\",\r\n\t\t\t\"vnfc\": [{\r\n\t\t\t\t\"vnfc-instance\": "
                + "\"1\",\r\n\t\t\t\t\"vnfc-function-code\": \"fcx\",\r\n\t\t\t\t\"ipaddress-v4-oam-vip\": \"Y\",\r\n\t\t\t\t\"group-notation-type\": "
                + "\"first-vnfc-name\",\r\n\t\t\t\t\"group-notation-value\": \"pair\",\r\n\t\t\t\t\"vnfc-type\": \"vDBE\"\r\n\t\t\t}]\r\n\t\t},"
                + "\r\n\t\t{\r\n\t\t\t\"vm-instance\": 2,\r\n\t\t\t\"template-id\": \"vnfctype2\",\r\n\t\t\t\"vnfc\": [{\r\n\t\t\t\t\"vnfc-instance\": "
                + "\"1\",\r\n\t\t\t\t\"vnfc-function-code\": \"fcx\",\r\n\t\t\t\t\"ipaddress-v4-oam-vip\": \"Y\",\r\n\t\t\t\t\"group-notation-type\": "
                + "\"first-vnfc-name\",\r\n\t\t\t\t\"group-notation-value\": \"pair\",\r\n\t\t\t\t\"vnfc-type\": \"vDBE\"\r\n\t\t\t}]\r\n\t\t}\r\n\t],"
                + "\r\n\t\"device-protocol\": \"NETCONF-XML\",\r\n\t\"user-name\": \"netconf\",\r\n\t\"port-number\": \"20\",\r\n\t\"artifact-list\": "
                + "[{\r\n\t\t\t\"artifact-name\": \"template_ConfigScaleOut_newtypeofvnf_0.0.1V_vnfctype1.xml\",\r\n\t\t\t\"artifact-type\": "
                + "\"config_template\"\r\n\t\t},\r\n\t\t{\r\n\t\t\t\"artifact-name\": \"pd_ConfigScaleOut_newtypeofvnf_0.0.1V_vnfctype1.yaml\","
                + "\r\n\t\t\t\"artifact-type\": \"parameter_definitions\"\r\n\t\t},\r\n\t\t{\r\n\t\t\t\"artifact-name\": "
                + "\"template_ConfigScaleOut_newtypeofvnf_0.0.1V_vnfctype2.xml\",\r\n\t\t\t\"artifact-type\": "
                + "\"config_template\"\r\n\t\t},\r\n\t\t{\r\n\t\t\t\"artifact-name\": \"pd_ConfigScaleOut_newtypeofvnf_0.0.1V_vnfctype2.yaml\","
                + "\r\n\t\t\t\"artifact-type\": \"parameter_definitions\"\r\n\t\t}\r\n\t],\r\n\t\"scopeType\": \"vnf-type\"\r\n}";
        JSONObject content = new JSONObject(contentStr);
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext context = new SvcLogicContext();
        context.setAttribute("vnf-type", "someVnf");
        context.setAttribute("action", "Configure");
        JSONObject scope = (JSONObject)content.get("scope");
        JSONArray vnfcTypeList = artifactHandlerNode.setVnfcTypeInformation(scope, context);
        artifactHandlerNode.processArtifactList(content, dbService, context, vnfcTypeList);
        JSONArray vnfcLists = scope.getJSONArray("vnfc-type-list");        
        assertEquals(vnfcLists.toString(), "[\"vnfctype1\",\"vnfctype2\"]");
        assertNotNull (vnfcTypeList);

    }

}
