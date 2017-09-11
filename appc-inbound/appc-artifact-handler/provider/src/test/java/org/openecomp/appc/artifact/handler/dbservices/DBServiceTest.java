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

package org.openecomp.appc.artifact.handler.dbservices;

import java.nio.charset.Charset;
import org.json.JSONObject;
import org.junit.Test;
import org.openecomp.appc.artifact.handler.utils.SdcArtifactHandlerConstants;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.powermock.reflect.Whitebox;

public class DBServiceTest {

    @Test
    public void testSaveArtifacts() throws Exception {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        int internalVersion = 1;
        dbService.saveArtifacts(ctx, internalVersion);
    }

    @Test(expected = Exception.class)
    public void testSaveArtifactsException() throws Exception {
        DBService dbService = DBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        int internalVersion = 1;
        dbService.saveArtifacts(ctx, internalVersion);
    }

    @Test
    public void testLogData() throws Exception {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        String prefix = "test";
        dbService.logData(ctx, prefix);
    }


    @Test(expected = Exception.class)
    public void testLogDataException() throws Exception {
        DBService dbService = DBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        String prefix = "test";
        dbService.logData(ctx, prefix);
    }

    @Test
    public void testProcessConfigActionDg() throws Exception {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        boolean isUpdate = true;
        ctx.setAttribute(SdcArtifactHandlerConstants.DOWNLOAD_DG_REFERENCE, "Reference");
        dbService.processConfigActionDg(ctx, isUpdate);
    }

    @Test(expected = Exception.class)
    public void testProcessConfigActionDgException() throws Exception {
        DBService dbService = DBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        boolean isUpdate = true;
        ctx.setAttribute(SdcArtifactHandlerConstants.DOWNLOAD_DG_REFERENCE, "Reference");
        dbService.processConfigActionDg(ctx, isUpdate);
    }

    @Test
    public void testGetModelDataInformationbyArtifactName() throws Exception {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        String artifactName = "test";
        dbService.getModelDataInformationbyArtifactName(artifactName);
    }

    @Test(expected = Exception.class)
    public void testGetModelDataInformationbyArtifactNameException() throws Exception {
        DBService dbService = DBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        String artifactName = "test";
        dbService.getModelDataInformationbyArtifactName(artifactName);
    }

    @Test
    public void testUpdateYangContents() throws Exception {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        String artifactName = "test";
        String artifactId = "TestArtifact";
        String yangContents = "TestYangContents";
        dbService.updateYangContents(ctx, artifactId, yangContents);
    }

    @Test(expected = Exception.class)
    public void testUpdateYangContentsException() throws Exception {
        DBService dbService = DBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        String artifactName = "test";
        String artifactId = "TestArtifact";
        String yangContents = "TestYangContents";
        dbService.updateYangContents(ctx, artifactId, yangContents);
    }

    @Test
    public void testInsertProtocolReference() throws Exception {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        String vnfType = "testVnf";
        String protocol = "testProtocol";
        String action = "testAction";
        String actionLevel = "testActionLevel";
        String template = "testTemplateData";
        dbService.insertProtocolReference(ctx, vnfType, protocol, action, actionLevel, template);
    }


    @Test(expected = Exception.class)
    public void testInsertProtocolReferenceException() throws Exception {
        DBService dbService = DBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        String vnfType = "testVnf";
        String protocol = "testProtocol";
        String action = "testAction";
        String actionLevel = "testActionLevel";
        String template = "testTemplateData";
        dbService.insertProtocolReference(ctx, vnfType, protocol, action, actionLevel, template);
    }

    @Test
    public void testprocessDpwnloadDGReference() throws Exception {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        boolean isUpdate = true;
        dbService.processDownloadDgReference(ctx, isUpdate);
    }

    @Test(expected = Exception.class)
    public void testprocessDpwnloadDGReferenceException() throws Exception {
        DBService dbService = DBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        boolean isUpdate = true;
        dbService.processDownloadDgReference(ctx, isUpdate);
    }

    @Test
    public void testProcessVnfcReference() throws Exception {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        boolean isUpdate = true;
        dbService.processVnfcReference(ctx, isUpdate);
    }

    @Test(expected = Exception.class)
    public void testProcessVnfcReferenceException() throws Exception {
        DBService dbService = DBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        boolean isUpdate = true;
        dbService.processVnfcReference(ctx, isUpdate);
    }

    @Test
    public void testProcessDeviceAuthentication() throws Exception {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        boolean isUpdate = true;
        dbService.processDeviceAuthentication(ctx, isUpdate);
    }

    @Test(expected = Exception.class)
    public void testProcessDeviceAuthenticationException() throws Exception {
        DBService dbService = DBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        boolean isUpdate = true;
        dbService.processDeviceAuthentication(ctx, isUpdate);
    }

    @Test
    public void testProcessDeviceInterfaceProtocol() throws Exception {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        boolean isUpdate = true;
        dbService.processDeviceInterfaceProtocol(ctx, isUpdate);
    }

    @Test(expected = Exception.class)
    public void testProcessDeviceInterfaceProtocolException() throws Exception {
        DBService dbService = DBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        boolean isUpdate = true;
        dbService.processDeviceInterfaceProtocol(ctx, isUpdate);
    }

    @Test
    public void testProcessSdcReferences() throws Exception {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        boolean isUpdate = true;
        dbService.processSdcReferences(ctx, isUpdate);
    }

    @Test(expected = Exception.class)
    public void testProcessSdcReferencesException() throws Exception {
        DBService dbService = DBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        boolean isUpdate = true;
        dbService.processSdcReferences(ctx, isUpdate);
    }

    @Test
    public void testIsArtifactUpdateRequired() throws Exception {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        String db = "db";
        dbService.isArtifactUpdateRequired(ctx, db);
    }

    @Test(expected = Exception.class)
    public void testIsArtifactUpdateRequiredExcetion() throws Exception {
        DBService dbService = DBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        String db = "db";
        dbService.isArtifactUpdateRequired(ctx, db);
    }


    @Test
    public void testgetArtifactID() throws Exception {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        String db = "db";
        dbService.getArtifactID(ctx, db);
    }

    @Test(expected = Exception.class)
    public void testgetArtifactIDException() throws Exception {
        DBService dbService = DBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        String db = "db";
        dbService.getArtifactID(ctx, db);
    }
}

