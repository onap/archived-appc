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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.appc.artifact.handler.utils.SdcArtifactHandlerConstants;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;

@RunWith(MockitoJUnitRunner.class)
public class TestDBServiceExceptions {

    private MockDBService dbService;

    private MockSvcLogicResource mockSVCLogicResource;

    private SvcLogicContext ctx ;

    @Before
    public void setup(){
           dbService =  MockDBService.initialise();
           mockSVCLogicResource = Mockito.spy(MockSvcLogicResource.class);
           ctx = new SvcLogicContext();
        }

    @Test
    public void testSaveArtifacts() throws Exception {              
        String artifactName = "TestArtifact";
        String prefix ="";
        ctx.setAttribute("test", "test");
        String result= dbService.getInternalVersionNumber(ctx, artifactName, prefix);
        assertEquals("1",result);
       
    }

    @Test
    public void testProcessSdcReferencesForCapability() throws Exception {        
        ctx.setAttribute("test", "test");
        ctx.setAttribute(SdcArtifactHandlerConstants.FILE_CATEGORY, "capability");
        boolean isUpdate = false;
        String expectedKey = "insert into ASDC_REFERENCE set VNFC_TYPE = null  , FILE_CATEGORY = $file-category , " +
                "VNF_TYPE = $vnf-type , ACTION = null  , ARTIFACT_TYPE = null  , ARTIFACT_NAME = $artifact-name";
        dbService.processSdcReferences(ctx, isUpdate);
        assertEquals(expectedKey,ctx.getAttribute("keys"));
        }

    @Test
    public void testProcessSdcReferences() throws Exception {        
        ctx.setAttribute("test", "test");
        ctx.setAttribute(SdcArtifactHandlerConstants.FILE_CATEGORY, "Test");
        String expectedKey = "insert into ASDC_REFERENCE set VNFC_TYPE = $vnfc-type , FILE_CATEGORY = $file-category , VNF_TYPE = $vnf-type , ACTION = $action , ARTIFACT_TYPE = $artifact-type , ARTIFACT_NAME = $artifact-name";
        boolean isUpdate = false;
        dbService.processSdcReferences(ctx, isUpdate);
        assertEquals(expectedKey,ctx.getAttribute("keys"));
    }

    @Test(expected = Exception.class)
    public void testGetDownLoadDGReference() throws Exception {       
        ctx.setAttribute("test", "test");
        ctx.setAttribute(SdcArtifactHandlerConstants.DEVICE_PROTOCOL, "");
        dbService.getDownLoadDGReference(ctx);
    }

    @Test
    public void testProcessConfigActionDg() throws Exception {      
        ctx.setAttribute("test", "test");
        boolean isUpdate = false;
        ctx.setAttribute(SdcArtifactHandlerConstants.DOWNLOAD_DG_REFERENCE, "Reference");
        String expectedKey = "insert into CONFIGURE_ACTION_DG set DOWNLOAD_CONFIG_DG = $download-dg-reference , ACTION = $action , VNF_TYPE = $vnf-type";
        dbService.processConfigActionDg(ctx, isUpdate);
        assertEquals(expectedKey,ctx.getAttribute("keys"));
    }

    @Test
    public void testProcessConfigActionDgForElse() throws Exception {       
        ctx.setAttribute("test", "test");
        boolean isUpdate = false;
        String expectedKey = null;
        dbService.processConfigActionDg(ctx, isUpdate);
        assertEquals(expectedKey,ctx.getAttribute("keys"));
    }

    @Test
    public void testprocessDpwnloadDGReference() throws Exception {        
        ctx.setAttribute("test", "test");
        boolean isUpdate = false;
        String expectedKey = "insert into DOWNLOAD_DG_REFERENCE set DOWNLOAD_CONFIG_DG = $download-dg-reference , PROTOCOL = $device-protocol";
        dbService.processDownloadDgReference(ctx, isUpdate);
        assertEquals(expectedKey,ctx.getAttribute("keys"));
    }

    @Test
    public void testResolveWhereClause() throws Exception {       
        ctx.setAttribute("test", "test");
        String db="DOWNLOAD_DG_REFERENCE";
        String whereClause="";
        String result =  Whitebox.invokeMethod(dbService, "resolveWhereClause", ctx, db, whereClause);
        assertEquals(true, result.contains("PROTOCOL"));
    }

    @Test
    public void testResolveWhereClauseForDevice_Authentication() throws Exception {       
        ctx.setAttribute("test", "test");
        String db="DEVICE_AUTHENTICATION";
        String whereClause="Test";
        String result =  Whitebox.invokeMethod(dbService, "resolveWhereClause", ctx, db, whereClause);
        assertEquals(true, result.contains("Test"));
    }

    @Test
    public void testResolveWhereClauseCONFIGURE_ACTION_DG() throws Exception {        
        ctx.setAttribute("test", "test");
        String db="CONFIGURE_ACTION_DG";
        String whereClause="Test";
        String result =  Whitebox.invokeMethod(dbService, "resolveWhereClause", ctx, db, whereClause);
        assertEquals(true, result.contains("Test"));
    }

    @Test
    public void testResolveWhereClauseVNFC_REFERENCE() throws Exception {      
        ctx.setAttribute("test", "test");
        String db="VNFC_REFERENCE";
        String whereClause="TestVNFC_REFERENCE";
        String result =  Whitebox.invokeMethod(dbService, "resolveWhereClause", ctx, db, whereClause);
        assertEquals(true, result.contains("TestVNFC_REFERENCE"));
    }

}
