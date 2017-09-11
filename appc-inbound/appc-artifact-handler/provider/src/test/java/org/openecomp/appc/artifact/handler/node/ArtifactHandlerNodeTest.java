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

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.powermock.reflect.Whitebox;
import static org.junit.Assert.assertTrue;
import org.openecomp.appc.artifact.handler.dbservices.DBService;
import org.openecomp.appc.artifact.handler.dbservices.MockDBService;
import org.openecomp.appc.artifact.handler.utils.SdcArtifactHandlerConstants;
import org.openecomp.appc.artifact.handler.utils.ArtifactHandlerProviderUtilTest;

import java.util.Map;
import java.util.HashMap;

public class ArtifactHandlerNodeTest {

    @Test
    public void testProcessArtifact() throws Exception {
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        MockArtifactHandlerNode ah = new MockArtifactHandlerNode();
        Map<String, String> inParams = new HashMap<String, String>();
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
        ah.processArtifact(inParams, ctx);
    }

    @Test(expected = Exception.class)
    public void testStoreReferenceData() throws Exception {
        MockArtifactHandlerNode ah = new MockArtifactHandlerNode();
        JSONObject documentInfo = new JSONObject();
        String artifactContent = IOUtils.toString(ArtifactHandlerProviderUtilTest.class.getClassLoader()
                .getResourceAsStream("templates/reference_template"), Charset.defaultCharset());
        documentInfo.put(SdcArtifactHandlerConstants.ARTIFACT_CONTENTS, artifactContent);
        documentInfo.put(SdcArtifactHandlerConstants.ARTIFACT_NAME, "reference_Junit.json");
        JSONObject requestInfo = new JSONObject();
        requestInfo.put("RequestInfo", "testStoreReferenceData");
        ah.storeReferenceData(requestInfo, documentInfo);
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
        documentInfo.put(SdcArtifactHandlerConstants.RESOURCE_VERSOIN, "testVers");
        documentInfo.put(SdcArtifactHandlerConstants.RESOURCE_TYPE, "testResType");
        documentInfo.put(SdcArtifactHandlerConstants.ARTIFACT_UUID, "testArtifactUuid");
        documentInfo.put(SdcArtifactHandlerConstants.ARTIFACT_VERSOIN, "testArtifactVers");
        documentInfo.put(SdcArtifactHandlerConstants.ARTIFACT_DESRIPTION, "testArtifactDesc");
        Whitebox.invokeMethod(ah, "processAndStoreCapablitiesArtifact", dbService, documentInfo, capabilities,
                "artifactName", "someVnf");
    }

    @Test
    public void testCleanVnfcInstance() throws Exception {
        ArtifactHandlerNode ah = new ArtifactHandlerNode();
        SvcLogicContext ctx = new SvcLogicContext();
        Whitebox.invokeMethod(ah, "cleanVnfcInstance", ctx);
        assertTrue(true);
    }

    @Test(expected = Exception.class)
    public void testGetArtifactIDException() throws Exception {
        ArtifactHandlerNode ah = new ArtifactHandlerNode();
        String yFileName = "yFileName";
        Whitebox.invokeMethod(ah, "getArtifactID", yFileName);
    }

    @Test(expected = Exception.class)
    public void testStoreUpdateSdcArtifacts() throws Exception {
        ArtifactHandlerNode ah = new ArtifactHandlerNode();
        String postDataStr =
                "{\"request-information\":{},\"document-parameters\":{\"artifact-name\":\"testArtifact\",\"artifact-contents\":{\"content\":\"TestContent\"}}}";
        JSONObject postData = new JSONObject(postDataStr);
        Whitebox.invokeMethod(ah, "storeUpdateSdcArtifacts", postData);
    }

    @Test(expected = Exception.class)
    public void testUpdateStoreArtifacts() throws Exception {
        MockArtifactHandlerNode ah = new MockArtifactHandlerNode();
        JSONObject documentInfo = new JSONObject();
        String artifactContent = IOUtils.toString(ArtifactHandlerProviderUtilTest.class.getClassLoader()
                .getResourceAsStream("templates/reference_template"), Charset.defaultCharset());
        documentInfo.put(SdcArtifactHandlerConstants.ARTIFACT_CONTENTS, artifactContent);
        documentInfo.put(SdcArtifactHandlerConstants.ARTIFACT_NAME, "reference_Junit.json");
        JSONObject requestInfo = new JSONObject();
        requestInfo.put("RequestInfo", "testupdateStoreArtifacts");
        ah.updateStoreArtifacts(requestInfo, documentInfo);
    }

    @Test
    public void testCleanArtifactInstanceData() throws Exception {
        MockArtifactHandlerNode ah = new MockArtifactHandlerNode();
        SvcLogicContext ctx = new SvcLogicContext();
        Whitebox.invokeMethod(ah, "cleanArtifactInstanceData", ctx);
    }

    @Test(expected = Exception.class)
    public void testUpdateYangContents() throws Exception {
        MockArtifactHandlerNode ah = new MockArtifactHandlerNode();
        String artifactId = "1";
        String yangContents = "SomeContent";
        Whitebox.invokeMethod(ah, "updateYangContents", artifactId, yangContents);
    }
    
}
