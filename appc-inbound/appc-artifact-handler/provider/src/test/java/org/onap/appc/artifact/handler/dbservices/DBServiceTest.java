/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
 * Modifications Copyright (C) 2018 IBM
 * ================================================================================
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

package org.onap.appc.artifact.handler.dbservices;

import org.junit.Ignore;
import org.junit.Test;
import org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import static org.junit.Assert.assertEquals;

public class DBServiceTest {

    @Test
    public void testSaveArtifacts() throws Exception {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        int internalVersion = 1;
        dbService.saveArtifacts(ctx, internalVersion);
    }

    @Test
    public void testSaveArtifactsException() throws Exception {
        MockDBService dbService = MockDBService.initialise();
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


    @Test
    public void testLogDataException() throws Exception {
        MockDBService dbService = MockDBService.initialise();
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

    @Test
    public void testProcessConfigActionDgException() throws Exception {
        MockDBService dbService = MockDBService.initialise();
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

    @Test
    public void testGetModelDataInformationbyArtifactNameException() throws Exception {
        MockDBService dbService = MockDBService.initialise();
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

    @Test
    public void testUpdateYangContentsException() throws Exception {
        MockDBService dbService = MockDBService.initialise();
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


    @Test
    public void testInsertProtocolReferenceException() throws Exception {
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

    @Test
    public void testprocessDpwnloadDGReference() throws Exception {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        boolean isUpdate = true;
        dbService.processDownloadDgReference(ctx, isUpdate);
    }

    @Test
    public void testprocessDpwnloadDGReferenceException() throws Exception {
        MockDBService dbService = MockDBService.initialise();
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
        boolean isUpdate = false;
        dbService.processVnfcReference(ctx, isUpdate);
    }

    @Test
    public void testProcessVnfcReferenceException() throws Exception {
        MockDBService dbService = MockDBService.initialise();
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
        ctx.setAttribute("url", "");
        String expectedKey ="update DEVICE_AUTHENTICATION set USER_NAME = '' , PORT_NUMBER = 0, URL = ''  where VNF_TYPE = $vnf-type  AND PROTOCOL = $device-protocol AND  ACTION = $action";
        boolean isUpdate = true;
        dbService.processDeviceAuthentication(ctx, isUpdate);
        assertEquals(expectedKey,ctx.getAttribute("keys"));
    }

    @Test
    public void testProcessDeviceAuthenticationforFalse() throws Exception {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        ctx.setAttribute("url", "");
        boolean isUpdate = false;
        dbService.processDeviceAuthentication(ctx, isUpdate);
        assertEquals(true,ctx.getAttribute("keys").contains("DEVICE_AUTHENTICATION"));
    }

    //@Test
    public void testProcessDeviceAuthenticationException() throws Exception {
        MockDBService dbService = MockDBService.initialise();
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

    @Test
    public void testProcessDeviceInterfaceProtocolForFalse() throws Exception {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        boolean isUpdate = false;
        dbService.processDeviceInterfaceProtocol(ctx, isUpdate);
        assertEquals(true,ctx.getAttribute("keys").contains("DEVICE_INTERFACE_PROTOCOL"));
    }

    @Test
    public void testProcessDeviceInterfaceProtocolException() throws Exception {
        MockDBService dbService = MockDBService.initialise();
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
        ctx.setAttribute(SdcArtifactHandlerConstants.FILE_CATEGORY, "testCategory");
        boolean isUpdate = true;
        dbService.processSdcReferences(ctx, isUpdate);
    }

    @Ignore
    public void testProcessSdcReferencesException() throws Exception {
        MockDBService dbService = MockDBService.initialise();
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

    @Test
    public void testIsArtifactUpdateRequiredExcetion() throws Exception {
        MockDBService dbService = MockDBService.initialise();
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

    @Test
    public void testgetArtifactIDException() throws Exception {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        String db = "db";
        dbService.getArtifactID(ctx, db);
    }
    @Test
    public void testGetDownLoadDGReference() throws Exception {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        ctx.setAttribute(SdcArtifactHandlerConstants.DEVICE_PROTOCOL, "CLI");
        assertEquals("TestDG", dbService.getDownLoadDGReference(ctx));
    }
}

