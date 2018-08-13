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

package org.onap.appc.artifact.handler.dbservices;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.powermock.reflect.Whitebox;

public class TestDBServiceExceptions {

    @Test
    public void testSaveArtifacts() throws Exception {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        String artifactName = "TestArtifact";
        String prefix ="";
        ctx.setAttribute("test", "test");
        String result= dbService.getInternalVersionNumber(ctx, artifactName, prefix);
        assertEquals("1",result);
    }

    @Test
    public void testProcessSdcReferencesForCapability() throws Exception {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        ctx.setAttribute(SdcArtifactHandlerConstants.FILE_CATEGORY, "capability");
        boolean isUpdate = false;
        dbService.processSdcReferences(ctx, isUpdate);
    }

    @Test
    public void testProcessSdcReferences() throws Exception {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        ctx.setAttribute(SdcArtifactHandlerConstants.FILE_CATEGORY, "Test");
        boolean isUpdate = false;
        dbService.processSdcReferences(ctx, isUpdate);
    }

    @Test(expected = Exception.class)
    public void testGetDownLoadDGReference() throws Exception {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        ctx.setAttribute(SdcArtifactHandlerConstants.DEVICE_PROTOCOL, "");
        dbService.getDownLoadDGReference(ctx);
    }

    @Test
    public void testProcessConfigActionDg() throws Exception {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        boolean isUpdate = false;
        ctx.setAttribute(SdcArtifactHandlerConstants.DOWNLOAD_DG_REFERENCE, "Reference");
        dbService.processConfigActionDg(ctx, isUpdate);
    }

    @Test
    public void testProcessConfigActionDgForElse() throws Exception {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        boolean isUpdate = false;
        dbService.processConfigActionDg(ctx, isUpdate);
    }

    @Test
    public void testprocessDpwnloadDGReference() throws Exception {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        boolean isUpdate = false;
        dbService.processDownloadDgReference(ctx, isUpdate);
    }

    @Test
    public void testResolveWhereClause() throws Exception {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        String db="DOWNLOAD_DG_REFERENCE";
        String whereClause="";
        String result =  Whitebox.invokeMethod(dbService, "resolveWhereClause", ctx, db, whereClause);
        assertEquals(true, result.contains("PROTOCOL"));
    }

    @Test
    public void testResolveWhereClauseForDevice_Authentication() throws Exception {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        String db="DEVICE_AUTHENTICATION";
        String whereClause="Test";
        String result =  Whitebox.invokeMethod(dbService, "resolveWhereClause", ctx, db, whereClause);
        assertEquals(true, result.contains("Test"));
    }

    @Test
    public void testResolveWhereClauseCONFIGURE_ACTION_DG() throws Exception {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        String db="CONFIGURE_ACTION_DG";
        String whereClause="Test";
        String result =  Whitebox.invokeMethod(dbService, "resolveWhereClause", ctx, db, whereClause);
        assertEquals(true, result.contains("Test"));
    }

    @Test
    public void testResolveWhereClauseVNFC_REFERENCE() throws Exception {
        MockDBService dbService = MockDBService.initialise();
        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("test", "test");
        String db="VNFC_REFERENCE";
        String whereClause="TestVNFC_REFERENCE";
        String result =  Whitebox.invokeMethod(dbService, "resolveWhereClause", ctx, db, whereClause);
        assertEquals(true, result.contains("TestVNFC_REFERENCE"));
    }

}
